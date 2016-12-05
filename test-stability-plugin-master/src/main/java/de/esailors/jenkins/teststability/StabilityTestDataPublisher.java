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

import javax.mail.MessagingException;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.scm.ChangeLogSet;
import hudson.tasks.junit.TestDataPublisher;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction.Data;
import hudson.tasks.test.TabulatedResult;
import hudson.tasks.junit.CaseResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.twilio.sdk.*;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import de.esailors.jenkins.teststability.StabilityTestData.Result;

/**
 * {@link TestDataPublisher} for the test stability history.
 * 
 * @author ckutz, KGBTeam_UIUC
 */
public class StabilityTestDataPublisher extends TestDataPublisher {

    public static final boolean DEBUG = false;
    private boolean useFilters = false;
    private boolean useMails = false;
    private boolean useTexts = false;
    private boolean sendToCulprits = false;
    private String domain = "";
    private String recipients = "";
    private String testName = "";
    private String textMessageRecipients = "";

    @DataBoundConstructor
    /**
     * Takes all inputs from Configure Jenkins page and stores them as variables
     * in the back end
     * 
     * @param sendToCulprits
     *            Boolean that enables/disables sending email reports to the
     *            culprit of a regressed build
     * @param useFilters
     *            Boolean that enables/disables filtering for builds
     * @param useMails
     *            Boolean that enables/disables sending email reports
     * @param useTexts
     *            Boolean that enables/disables sending text reports
     * @param recipients
     *            List of email address to send reports to
     * @param testName
     *            Name of the test to filter
     * @param textMessageRecipients
     *            List of phone numbers to send reports to
     * @param domain
     *            domain of email address to send reports to
     */
    public StabilityTestDataPublisher(boolean sendToCulprits, boolean useFilters, boolean useMails, boolean useTexts,
            String recipients, String testName, String textMessageRecipients, String domain) {
        this.useMails = useMails;
        this.recipients = recipients;
        this.sendToCulprits = sendToCulprits;
        this.domain = domain;

        this.useFilters = useFilters;
        this.testName = testName;

        this.useTexts = useTexts;
        this.textMessageRecipients = textMessageRecipients;
    }

    /**
     * Getter for Text Message Recipients
     * 
     * @return String of all text message recipients
     */
    public String getTextMessageRecipients() {
        return this.textMessageRecipients;
    }

    /**
     * Getter for email domain name
     * 
     * @return String of the domain name
     */
    public String getDomain() {
        return this.domain;
    }

    /**
     * Getter for filter boolean
     * 
     * @return Boolean to use filters or not
     */
    public boolean getUseFilters() {
        return this.useFilters;
    }

    /**
     * Getter for texts boolean
     * 
     * @return Boolean toggle to receive text notifications
     */
    public boolean getUseTexts() {
        return this.useTexts;
    }

    /**
     * Getter for Send email to culprits boolean
     * 
     * @return Boolean toggle to send email to culrpit of regressed build
     */
    public boolean getSendToCulprits() {
        return this.sendToCulprits;
    }

    /**
     * Getter for test name filter string
     * 
     * @return String of name of test to be filtered
     */
    public String getTestName() {
        return this.testName;
    }

    /**
     * Getter for sending email notifications boolean
     * 
     * @return Boolean toggle for sending emails for build status
     */
    public boolean getUseMails() {
        return useMails;
    }

    /**
     * Getter for list of email addresses
     * 
     * @return List of whitespace separated email addresses for email
     *         notifications
     */
    public String getRecipients() {
        return recipients;
    }

    @Override
    /**
     * Builds the circular stability history from previous histories and adds
     * the current build data through the addResultToMap function. If the build
     * was triggered from an SCM poll, it will check for and send email/text
     * notifications to the specified list of recipients.
     * 
     * @param build
     *            Object representing the current Jenkins build
     * @param launcher
     *            Object that starts a process and inherits environemtn
     *            variables
     * @param listener
     *            Object that listens for build notifications
     * @param testResult
     *            Object that contains the root of all test results for one
     *            build
     * @return A new stability test data that contains information about the
     *         build
     * @throws IOException
     * @throws InterruptedException
     */
    public Data getTestData(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener, TestResult testResult)
            throws IOException, InterruptedException {

        String author = "";

        Map<String, CircularStabilityHistory> stabilityHistoryPerTest = new HashMap<String, CircularStabilityHistory>();
        int maxHistoryLength = getDescriptor().getMaxHistoryLength();

        CircularStabilityHistory.emptyTestsList();

        // Build the new CircularStabilityHistory
        addResultToMap(build.getNumber(), listener, stabilityHistoryPerTest, testResult, maxHistoryLength);
        CircularStabilityHistory c = stabilityHistoryPerTest.values().iterator().next();
        if (c != null) {
            while (c.getParent() != null) {
                c = c.getParent();
            }
            c.updateResultForChildren();
        }

        ChangeLogSet<? extends ChangeLogSet.Entry> changeSet = build.getChangeSet();
        if (changeSet != null && !changeSet.isEmptySet()) {
            Object item = changeSet.getItems()[0];
            author = ((ChangeLogSet.Entry) item).getAuthor().getId();

            if (this.useTexts || this.useMails) {

                RegressionReportNotifier rrNotifier = new RegressionReportNotifier();
                for (Map.Entry<String, CircularStabilityHistory> e : stabilityHistoryPerTest.entrySet()) {
                    CircularStabilityHistory h = e.getValue();
                    if (h != null && h.isMostRecentTestRegressed() && h.isShouldPublish()) {
                        rrNotifier.addResult(e.getKey(), h);
                    }
                }

                if (this.useTexts) {
                    if (textMessageRecipients != null && !textMessageRecipients.isEmpty()) {
                        List<String> numbers = Arrays.asList(textMessageRecipients.split(","));

                        for (String number : numbers) {

                            try {
                                rrNotifier.textReport(number, author, listener, build);
                            }
                            catch (TwilioRestException e) {

                            }
                        }
                    }
                }

                if (this.useMails) {
                    String finalRecipients = sendToCulprits ? this.recipients + "," + author + domain : this.recipients;
                    try {
                        rrNotifier.mailReport(finalRecipients, author, listener, build);
                    }
                    catch (MessagingException e) {

                    }
                }

            }
        }

        return new StabilityTestData(stabilityHistoryPerTest);
    }

    /**
     * Adds a test result and its children to the map, creating
     * CircularStabilityHistory objects for them
     * 
     * @param buildNumber
     *            The build number
     * @param listener
     *            The BuildListener
     * @param stabilityHistoryPerTest
     *            The map storing all histories
     * @param result
     *            The test result to add to the map
     * @param maxHistoryLength
     *            The max number of results stored in each history
     * @return The CircularStabilityHistory generated for this TestResult
     */
    public CircularStabilityHistory addResultToMap(int buildNumber, BuildListener listener,
            Map<String, CircularStabilityHistory> stabilityHistoryPerTest, hudson.tasks.test.TestResult result,
            int maxHistoryLength) {

        CircularStabilityHistory history = getPreviousHistory(result);
        if (history == null) {
            history = new CircularStabilityHistory(maxHistoryLength);
            buildUpInitialHistory(history, result, maxHistoryLength - 1);
        }

        if (history != null) {
            if (result.isPassed()) {
                history.add(buildNumber, true);
            }
            else if (result.getFailCount() > 0) {
                history.add(buildNumber, false);
            }
            // else test is skipped and we leave history unchanged

            if ((result instanceof TabulatedResult)) {
                for (hudson.tasks.test.TestResult child : ((TabulatedResult) result).getChildren()) {
                    CircularStabilityHistory childBuffer = null;
                    if (useFilters && checkTestName(child.getName())) {
                        CircularStabilityHistory.addToHiddenTests(child.getName());
                    }
                    else {
                        childBuffer = addResultToMap(buildNumber, listener, stabilityHistoryPerTest, child,
                                maxHistoryLength);
                    }

                    if (childBuffer != null) {
                        history.addChild(childBuffer);
                    }
                }
            }

            if ((result instanceof CaseResult && ((CaseResult) result).getStatus() == CaseResult.Status.FIXED)) {
                history.setStackTrace(result.getPreviousResult().getErrorStackTrace());
            }

            history.setName(result.getName());
            if (result instanceof CaseResult) {
                history.setShouldPublish(true);
            }
            stabilityHistoryPerTest.put(result.getId(), history);

            return history;
        }
        return null;
    }

    private boolean checkTestName(String testName) {
        return (!testName.equals("") && testName.equals(this.testName));
    }

    private CircularStabilityHistory getPreviousHistory(hudson.tasks.test.TestResult result) {
        hudson.tasks.test.TestResult previous = getPreviousResult(result);

        if (previous != null) {
            StabilityTestAction previousAction = previous.getTestAction(StabilityTestAction.class);
            if (previousAction != null) {
                CircularStabilityHistory prevHistory = previousAction.getRingBuffer();

                if (prevHistory == null) {
                    return null;
                }

                CircularStabilityHistory newHistory = new CircularStabilityHistory(
                        getDescriptor().getMaxHistoryLength());
                newHistory.addAll(prevHistory.getData());
                return newHistory;
            }
        }
        return null;
    }

    private void buildUpInitialHistory(CircularStabilityHistory ringBuffer, hudson.tasks.test.TestResult result,
            int number) {
        List<Result> testResultsFromNewestToOldest = new ArrayList<Result>(number);
        hudson.tasks.test.TestResult previousResult = getPreviousResult(result);
        while (previousResult != null) {
            testResultsFromNewestToOldest
                    .add(new Result(previousResult.getOwner().getNumber(), previousResult.isPassed()));
            previousResult = previousResult.getPreviousResult();
        }

        for (int i = testResultsFromNewestToOldest.size() - 1; i >= 0; i--) {
            ringBuffer.add(testResultsFromNewestToOldest.get(i));
        }
    }

    private hudson.tasks.test.TestResult getPreviousResult(hudson.tasks.test.TestResult result) {
        try {
            return result.getPreviousResult();
        }
        catch (RuntimeException e) {
            return null;
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<TestDataPublisher> {

        private int maxHistoryLength = 30;

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            this.maxHistoryLength = json.getInt("maxHistoryLength");
            save();
            return super.configure(req, json);
        }

        public int getMaxHistoryLength() {
            return this.maxHistoryLength;
        }

        @Override
        public String getDisplayName() {
            return "Test stability history";
        }

    }
}
