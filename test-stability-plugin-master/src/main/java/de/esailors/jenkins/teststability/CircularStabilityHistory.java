/*
 * The MIT License
 * 
 * Copyright (c) 2013, eSailors IT Solutions GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.esailors.jenkins.teststability;

import jenkins.model.Jenkins;

import java.util.*;
import java.util.logging.*;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import de.esailors.jenkins.teststability.StabilityTestData.Result;

/**
 * Circular history of test results.
 * <p>
 * Old records are dropped when <code>maxSize</code> is exceeded.
 * 
 * @author ckutz, KGBTeam_UIUC
 */
public class CircularStabilityHistory {
    
    private static final Logger myLog = Logger.getLogger(StabilityTestDataPublisher.class.getName());
    
    // List of hidden tests name (used by frontend)
    private static List<String> hiddenTests = new ArrayList<String>();
    private static String hiddenTestsString = "";
    
    private Result[] data;
    private int head; 
    private int tail;
    private int failed = 0;
    private int testStatusChanges = 0;
    private boolean shouldPublish = false;
    private String name = "";
    private String stackTrace = "";
    
    // number of elements in queue
    private int size = 0;
    
    private int flakiness;
    private int stability;
    
    private Set<CircularStabilityHistory> children = new HashSet<CircularStabilityHistory>();
    private CircularStabilityHistory parent = null;

    private CircularStabilityHistory() {}

    /**
     * Constructor for CircularStabilityHistory
     * @param maxSize The max number of results that can be storde in this history
     */
    public CircularStabilityHistory(int maxSize) {
        flakiness = 0;
        stability = 100;
        data = new Result[maxSize];
        head = 0;
        tail = 0;
    }
    
    /**
    * This looks at the children of this history to see if all failing children are filtered out.
    * If they are, it marks this as a pass rather than fail.  
    */
    public void updateResultForChildren() {
        int oldTail = tail - 1;
        if (tail == 0) {
            oldTail = data.length - 1;
        } 
        if (children.isEmpty()) {
            return;
        }
        boolean newResult = true;
    
        data[oldTail].passed = newResult;
    }
    
    /**
     * Checks to see if the latest test result in this history is regressed
     * @return true if regressed, false if not
     */
    public boolean isMostRecentTestRegressed() {
        if (size < 2) {
            return false;
        }
        
        int oldTail = (tail - 1 + this.data.length) % this.data.length;
        int oldPrevTail = (oldTail - 1 + this.data.length) % this.data.length;
        
        return (this.data[oldPrevTail].passed && !this.data[oldTail].passed);
    }

    /**
     * Returns whether the test this history is for should be published by notifiers
     * @return true if the test should be published, false otherwise
     */
    public boolean isShouldPublish() {
        return this.shouldPublish;
    }

    /**
     * Sets whether the test this history is for should be published by the notifiers
     * @param shouldPublish new value
     */
    public void setShouldPublish(boolean shouldPublish) {
        this.shouldPublish = shouldPublish;
    }
    
    /**
     * Gives the name of the test this history is for
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gives the size of this history - the number of results in it
     * @return the size
     */
    public int getSize() {
        return size;
    }
    
    /**
     * Adds a test to the list of tests that should be hidden
     * @param testName the test to add
     */
    public static void addToHiddenTests(String testName) {
        if (!hiddenTests.contains(testName)) {
            hiddenTests.add(testName);
            hiddenTestsString = hiddenTests.toString();
        }
    }
    
    /**
     * Clears the list of tests that should be hidden
     */
    public static void emptyTestsList(){
            hiddenTests.clear();
    }
    
    /**
     * Gives the list of tests the should be hidden
     * @return the list as a comma separated string
     */
    public static String getHiddenTestsString(){
        return hiddenTestsString;
    }
    
    /**
     * Sets the name of the test this history is for
     * @param newName the new value
     */
    public void setName(String newName) {
        name = newName;
    }
    
    /**
     * Sets the stack trace associated with this history
     * @param newStackTrace the new value
     */
    public void setStackTrace(String newStackTrace) {
        stackTrace = newStackTrace;
      }
      
    /**
     * Gives the stack trace associated with this history
     * @return the stack trace
     */
    public String getStackTrace() {
        return stackTrace;
    }
    
    /**
     * Adds a new result to the history
     * @param value the result to add
     * @return true on success
     */
    public boolean add(Result value) {
        data[tail] = value;
        tail++;
        if (tail == data.length) {
            tail = 0;
        }
        
        if (size == data.length) {
            head = (head + 1) % data.length;
        } else {
            size++;
        }
        
        return true;
    }
      
    /**
     * Gives the data of this history as a simple array, with the earliest result at index 0
     * @return array of results
     */
    public Result[] getData() {
        Result[] copy = new Result[size];
        
        for (int i = 0; i < size; i++) {
          copy[i] = data[(head + i) % data.length];
        }
        
        return copy;
    }
    
    /**
     * Tells whether the history is empty
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return data.length == 0;
    }
    
    /**
     * Gives the max number of results this history can hold
     * @return the max size
     */
    public int getMaxSize() {
        return this.data.length;
    }
    
    private void computeStability() {
        this.failed = 0;
        
        for (Result r : this.getData()) {
            if (!r.passed) {
                this.failed++;
            }
        }
        
        this.stability = 100 * (size - failed) / (size == 0 ? 1 : size);
    }
    
    /**
     * Computes the flakiness in percent. (Is this really flakiness? Doesn't check if code changed or not, but this is a problem for later)
     */
    private void computeFlakiness() {
        Boolean previousPassed = null;
        
        this.testStatusChanges = 0;
        
        for (Result r : this.getData()) {
            boolean thisPassed = r.passed;
            if (previousPassed != null && previousPassed != thisPassed) {
                this.testStatusChanges++;
            }
            previousPassed = thisPassed;
        }
        
        if (size > 1) {
            this.flakiness = 100 * testStatusChanges / (size - 1);
        } else {
            this.flakiness = 0;
        }
    }
    
    /**
     * Gives the flakiness of the test this history is for
     * @return the flakiness as a percentage
     */
    public int getFlakiness() {
        computeFlakiness();
        return this.flakiness;
    }
    
    /**
     * Gives the stability of the test this history is for
     * @return the stability as a percentage
     */
    public int getStability() {
        computeStability();
        return this.stability;
    }
    
    /**
     * Gives a count of how many times this test has failed in the recorded history
     * @return the count of failures
     */
    public int getFailed() {
        this.failed = 0;
        
        for (Result r : this.getData()) {
            if (!r.passed) {
                this.failed++;
            }
        }
        
        return this.failed;
    }
    
    static {
        Jenkins.XSTREAM2.registerConverter(new ConverterImpl());
    }
    
    /**
     * Gives the children of this history
     * @return a Set of the children
     */
    public Set<CircularStabilityHistory> getChildren() {
        return children;
    }
    
    /**
     * Gives this history's parent
     * @return the parent
     */
    public CircularStabilityHistory getParent() {
        return parent;
    }
    
    /**
     * Adds a child to this history
     * @param newChild the child to add
     */
    public void addChild(CircularStabilityHistory newChild) {
        if (children.add(newChild)){
            newChild.parent = this;
        }   
    }

    /**
     * Gives the child with the highest flakiness
     * @return the flakiest child
     */
    public CircularStabilityHistory getFlakiestChild() {
        CircularStabilityHistory flakiestChild = null;
        
        // myLog.log(Level.FINE, "Finding flakiest child of " + this.getName());
        
        for (CircularStabilityHistory child : children) {
            // myLog.log(Level.FINE, "Checking flakiness of child " + child.getName());
            if (flakiestChild == null || flakiestChild.getFlakiness() < child.getFlakiness()) {
                myLog.log(Level.FINE, "Child " + child.getName() + " is the flakiest child of " + this.getName());
                flakiestChild = child;
            }
        }
        
        return flakiestChild;
    }

    /**
     * Gives the child with the lowest stability
     * @return the least stable child
     */
    public CircularStabilityHistory getLeastStableChild() {
        CircularStabilityHistory leastStableChild = null;
        
        for (CircularStabilityHistory child : children) {
            if (leastStableChild == null || leastStableChild.getStability() > child.getStability()) {
                leastStableChild = child;
            }
        }
        
        return leastStableChild;
    }
    
    /**
     * Adds all results in the provided array
     * @param results array of results to add
     */
    public void addAll(Result[] results) {
        for (Result b : results) {
            add(b);
        }
    }
    
    /**
     * Adds a new result made from the given information
     * @param buildNumber the build number of the result to add
     * @param passed true if the result passed, false if it failed
     */
    public void add(int buildNumber, boolean passed) {
        add(new Result(buildNumber, passed));
    }
    
    /**
     * Gives whether the test this history is for has always passed
     * @return true if the test has never failed, false if it has
     */
    public boolean isAllPassed() {
        if (size == 0) {
            return true;
        }
        
        for (Result r : data) {
            if (r != null && !r.passed) {
                return false;
            }
        }
        
        return true;
    }
    
    public static class ConverterImpl implements Converter {
        
        public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
            return CircularStabilityHistory.class.isAssignableFrom(type);
        }
        
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            CircularStabilityHistory b = (CircularStabilityHistory) source;
            
            writer.startNode("head");
            writer.setValue(Integer.toString(b.head));
            writer.endNode();
            
            writer.startNode("tail");
            writer.setValue(Integer.toString(b.tail));
            writer.endNode();
            
            writer.startNode("size");
            writer.setValue(Integer.toString(b.size));
            writer.endNode();
            
            writer.startNode("data");
            writer.setValue(dataToString(b.data));
            writer.endNode();
        }
        
        private String dataToString(Result[] data) {
            StringBuilder buf = new StringBuilder();
            for (Result d : data) {
                if(d == null) {
                    buf.append(",");
                    continue;
                }
                if (d.passed) {
                    buf.append(d.buildNumber).append(";").append("1,");
                } else {
                    buf.append(d.buildNumber).append(";").append("0,");
                }
            }
            
            if (buf.length() > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            
            return buf.toString();
        }
        
        public CircularStabilityHistory unmarshal(HierarchicalStreamReader r, UnmarshallingContext context) {
            
            r.moveDown();
            int head = Integer.parseInt(r.getValue());
            r.moveUp();
            
            r.moveDown();
            int tail = Integer.parseInt(r.getValue());
            r.moveUp();
            
            r.moveDown();
            int size = Integer.parseInt(r.getValue());
            r.moveUp();
            
            r.moveDown();
            String data = r.getValue();
            r.moveUp();
            
            CircularStabilityHistory buf = new CircularStabilityHistory();
            Result[] b = stringToData(data);
            
            buf.data = b;
            buf.head = head;
            buf.size = size;
            buf.tail = tail;
            
            return buf;
        }
        
        private  Result[] stringToData(String s) {
            String[] split = s.split(",", -1);
            Result d[] = new Result[split.length];
            
            int i = 0;
            for(String testResult : split) {
                
                if (testResult.isEmpty()) {
                    i++;
                    continue;
                }
                
                String[] split2 = testResult.split(";");
                int buildNumber = Integer.parseInt(split2[0]);
                
                boolean buildResult = "1".equals(split2[1]) ? true : false;
                
                d[i] = new Result(buildNumber, buildResult);
                
                i++;
            }
            
            return d;
        }
    }
}
