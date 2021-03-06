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

import org.jvnet.localizer.Localizable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import hudson.model.HealthReport;
import hudson.tasks.junit.TestAction;
import de.esailors.jenkins.teststability.StabilityTestData.Result;

/**
 * {@link TestAction} for the test stability history.
 * 
 * @author ckutz, KGBTeam_UIUC
 */
class StabilityTestAction extends TestAction {

    private CircularStabilityHistory ringBuffer;

    // Temporarily setting to public to use for debug output
    public String description;

    // These fields are so data can be remembered when the pages are loaded
    // after the build runs
    private int total;
    private int failed;
    private int stability = 100;
    private int flakiness = 0;

    private String stackTrace = "";

    private String nameOfFlakiestChild = "";
    private String nameOfLeastStableChild = "";
    private int flakinessOfFlakiestChild = -1;
    private int stabilityOfLeastStableChild = -1;

    private boolean showGraph = true;
    private String hiddenTestsString = "";
    private String dataJson = null;

    /**
     * Constructs the StabilityTestAction object from data within the ringBuffer
     * 
     * @param ringBuffer
     *            CircularStabilityHistory object that data for this object is
     *            taken from
     */
    public StabilityTestAction(CircularStabilityHistory ringBuffer) {

        this.ringBuffer = ringBuffer;
        this.dataJson = "null";

        if (ringBuffer != null) {

            this.hiddenTestsString = CircularStabilityHistory.getHiddenTestsString();

            Result[] data = ringBuffer.getData();
            this.total = data.length;

            this.flakiness = ringBuffer.getFlakiness();
            this.stability = ringBuffer.getStability();
            this.failed = ringBuffer.getFailed();
            this.dataJson = initDataJson();
            this.stackTrace = ringBuffer.getStackTrace();
        }

        if (this.stability == 100) {
            this.description = "No known failures. Flakiness 0%, Stability 100%";
        }
        else {
            this.description = String.format("Failed %d times in the last %d runs. Flakiness: %d%%, Stability: %d%%",
                    failed, total, flakiness, stability);
        }

        this.nameOfFlakiestChild = this.findNameOfFlakiestChild();
        this.nameOfLeastStableChild = this.findNameOfLeastStableChild();
        this.flakinessOfFlakiestChild = this.findFlakinessOfFlakiestChild();
        this.stabilityOfLeastStableChild = this.findStabilityOfLeastStableChild();

    }

    /**
     * Gets the ringbuffer CircularStabilityHistory object
     * 
     * @return CircularStabilityHistory object for builds history
     */
    public CircularStabilityHistory getRingBuffer() {
        return this.ringBuffer;
    }

    /**
     * Shows the graph on the screen
     * 
     * @return Boolean toggle to show or hide the history graph
     */
    public boolean getShowGraph() {
        return this.showGraph;
    }

    /**
     * Getter function for the description
     * 
     * @return String that says the number of failures and percentages
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Finds and returns the name of the flakiest child
     * 
     * @return String name of the flakiest child
     */
    private String findNameOfFlakiestChild() {
        if (this.ringBuffer != null && this.ringBuffer.getFlakiestChild() != null) {
            return this.ringBuffer.getFlakiestChild().getName();
        }
        return "No flaky tests";
    }

    /**
     * Finds and returns the name of the least stable child,
     * 
     * @return String of name of the least stable child
     */
    private String findNameOfLeastStableChild() {
        if (this.ringBuffer != null && this.ringBuffer.getLeastStableChild() != null) {
            return this.ringBuffer.getLeastStableChild().getName();
        }
        return "No unstable tests";
    }

    /**
     * Finds and returns stability of the least stable child.
     * 
     * @return int that represents the stability of the least stable child
     */
    private int findStabilityOfLeastStableChild() {
        if (this.ringBuffer != null && this.ringBuffer.getLeastStableChild() != null) {
            return this.ringBuffer.getLeastStableChild().getStability();
        }
        return -1;
    }

    /**
     * Returns flakiness of the most flaky child.
     * 
     * @return int that represents the flakiness of the flakiest child
     */
    private int findFlakinessOfFlakiestChild() {
        if (this.ringBuffer != null && this.ringBuffer.getFlakiestChild() != null) {
            return this.ringBuffer.getFlakiestChild().getFlakiness();
        }
        return -1;
    }

    /**
     * Returns the name of the most flaky child,
     * 
     * @return String name of the flakiest child
     */
    public String getNameOfFlakiestChild() {
        return this.nameOfFlakiestChild;
    }

    /**
     * Returns the name of the least stable child,
     * 
     * @return String name of the least stable child
     */
    public String getNameOfLeastStableChild() {
        return this.nameOfLeastStableChild;
    }

    /**
     * Gets and returns the stability of the least stable child
     * 
     * @return int that represents the stability of the least stable child
     */
    public int getStabilityOfLeastStableChild() {
        return this.stabilityOfLeastStableChild;
    }

    /**
     * Gets and returns the flakiness of the flakiest child
     * 
     * @return int that represents the flakiness of the flakiest child
     */
    public int getFlakinessOfFlakiestChild() {
        return this.flakinessOfFlakiestChild;
    }

    /**
     * Returns the file name of the icon
     */
    public String getIconFileName() {
        return null;
    }

    /**
     * Returns the display name of this action
     */
    public String getDisplayName() {
        return null;
    }

    /**
     * Returns the url name of this action
     */
    public String getUrlName() {
        return null;
    }

    /**
     * Gets the flakiness for each child
     * 
     * @return int that represents the flakiness of a child
     */
    public int getFlakiness() {
        return this.flakiness;
    }

    /**
     * Gets the stability for each child
     * 
     * @return int that represents the stability of a child
     */
    public int getStablity() {
        return this.stability;
    }

    /**
     * Returns the path to the big image corresponding to this test's health
     */
    public String getBigImagePath() {
        HealthReport healthReport = new HealthReport(100 - flakiness, (Localizable) null);
        return healthReport.getIconUrl("32x32");
    }

    /**
     * Returns the path to the small image corresponding to this test's health
     */
    public String getSmallImagePath() {
        HealthReport healthReport = new HealthReport(100 - flakiness, (Localizable) null);
        return healthReport.getIconUrl("16x16");
    }

    /**
     * Gets the stack trace for a build
     * 
     * @return String of the entire stack trace after a build runs
     */
    public String getStackTrace() {
        return this.stackTrace;
    }

    /**
     * Gets the JSON data for the graph
     * 
     * @return String of the JSON array to display the chart data
     */
    public String getDataJson() {
        return this.dataJson;
    }

    /**
     * Gets the string that represents all of the hidden filtered tests
     * 
     * @return String that represents all of the hidden filtered tests
     */
    public String getHiddenTestsString() {
        return this.hiddenTestsString;
    }

    private String initDataJson() {
        // there should be no graph if there is no history
        if (this.ringBuffer == null) {
            return "should not appear";
        }

        // JSON array of history data
        JsonArray arr = new JsonArray();
        // Boolean to keep track of previous build's status
        Boolean previousPassed = null;

        // loop through the entire history and check if the current test is
        // passed or failed
        // check the previous build to determine if the current build should be
        // regressed or fixed
        for (Result r : this.ringBuffer.getData()) {
            boolean thisPassed = r.passed;
            String testStatus = thisPassed ? "Pass" : "Fail";
            if (previousPassed != null) {
                if (previousPassed && !thisPassed) {
                    testStatus = "Regression";
                }
                else if (!previousPassed && thisPassed) {
                    testStatus = "Fixed";
                }
            }

            // create a new JSON object to match the form of the graph data
            // add each JSON object to a JSON array
            JsonObject result = new JsonObject();
            result.addProperty("status", testStatus);
            result.addProperty("build_number", r.buildNumber);
            arr.add(result);
            previousPassed = thisPassed;
        }

        if (arr.size() == 0) {
            return "no_data";
        }
        else {
            return arr.toString();
        }
    }

}
