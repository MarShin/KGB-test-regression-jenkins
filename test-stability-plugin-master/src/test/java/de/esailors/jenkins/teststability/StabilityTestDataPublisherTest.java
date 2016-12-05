package de.esailors.jenkins.teststability;

import org.junit.Test;

import org.junit.Assert;
import de.esailors.jenkins.teststability.StabilityTestData.Result;

import org.mockito.Mockito;

import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.TestResult;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;

import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TouchBuilder;
import org.jvnet.hudson.test.recipes.LocalData;

import org.junit.Before;
import org.junit.Rule;

import de.esailors.jenkins.teststability.StabilityTestDataPublisher.DescriptorImpl;

public class StabilityTestDataPublisherTest {

    @Test
    public void filterTestName() {

    }

    @Test
    public void getsStackTraceOfLastFailureWhenFixed() {
        String expected = "This is the stack trace";

        CaseResult failed = Mockito.mock(CaseResult.class);
        CaseResult fixed = Mockito.mock(CaseResult.class);

        Mockito.when(failed.getStatus()).thenReturn(CaseResult.Status.FAILED);
        Mockito.when(failed.getErrorStackTrace()).thenReturn(expected);

        Mockito.when(fixed.getStatus()).thenReturn(CaseResult.Status.FIXED);
        Mockito.when(fixed.getPreviousResult()).thenReturn(failed);

        StabilityTestDataPublisher publisher = Mockito.mock(StabilityTestDataPublisher.class);
        DescriptorImpl desc = Mockito.mock(DescriptorImpl.class);

        Mockito.when(publisher.getDescriptor()).thenReturn(desc);
        Mockito.when(publisher.addResultToMap(2, null, new HashMap<String, CircularStabilityHistory>(), failed, 5))
                .thenCallRealMethod();
        Mockito.when(publisher.addResultToMap(3, null, new HashMap<String, CircularStabilityHistory>(), fixed, 5))
                .thenCallRealMethod();
        Mockito.when(desc.getMaxHistoryLength()).thenReturn(5);

        CircularStabilityHistory failedRingBuffer = publisher.addResultToMap(2, null,
                new HashMap<String, CircularStabilityHistory>(), failed, 5);

        StabilityTestAction failedAction = new StabilityTestAction(failedRingBuffer);

        Mockito.when(failed.getTestAction(StabilityTestAction.class)).thenReturn(failedAction);

        CircularStabilityHistory fixedRingBuffer = publisher.addResultToMap(3, null,
                new HashMap<String, CircularStabilityHistory>(), fixed, 5);

        Assert.assertEquals(expected, fixedRingBuffer.getStackTrace());
    }

    @Test
    public void noStackTraceFromTwoFails() {
        String expected = "This is the stack trace";

        CaseResult failed1 = Mockito.mock(CaseResult.class);
        CaseResult failed2 = Mockito.mock(CaseResult.class);

        Mockito.when(failed1.getStatus()).thenReturn(CaseResult.Status.FAILED);
        Mockito.when(failed1.getErrorStackTrace()).thenReturn(expected);

        Mockito.when(failed2.getStatus()).thenReturn(CaseResult.Status.FAILED);
        Mockito.when(failed2.getErrorStackTrace()).thenReturn(expected);
        Mockito.when(failed2.getPreviousResult()).thenReturn(failed1);

        StabilityTestDataPublisher publisher = Mockito.mock(StabilityTestDataPublisher.class);
        DescriptorImpl desc = Mockito.mock(DescriptorImpl.class);

        Mockito.when(publisher.getDescriptor()).thenReturn(desc);
        Mockito.when(publisher.addResultToMap(2, null, new HashMap<String, CircularStabilityHistory>(), failed1, 5))
                .thenCallRealMethod();
        Mockito.when(publisher.addResultToMap(3, null, new HashMap<String, CircularStabilityHistory>(), failed2, 5))
                .thenCallRealMethod();
        Mockito.when(desc.getMaxHistoryLength()).thenReturn(5);

        CircularStabilityHistory failedRingBuffer1 = publisher.addResultToMap(2, null,
                new HashMap<String, CircularStabilityHistory>(), failed1, 5);

        StabilityTestAction failedAction = new StabilityTestAction(failedRingBuffer1);

        Mockito.when(failed1.getTestAction(StabilityTestAction.class)).thenReturn(failedAction);

        CircularStabilityHistory failedRingBuffer2 = publisher.addResultToMap(3, null,
                new HashMap<String, CircularStabilityHistory>(), failed2, 5);

        Assert.assertEquals("", failedRingBuffer2.getStackTrace());
    }

    @Test
    public void noStackTraceFromTwoPasses() {
        String expected = "This is the stack trace";

        CaseResult passed1 = Mockito.mock(CaseResult.class);
        CaseResult passed2 = Mockito.mock(CaseResult.class);

        Mockito.when(passed1.getStatus()).thenReturn(CaseResult.Status.PASSED);

        Mockito.when(passed2.getStatus()).thenReturn(CaseResult.Status.PASSED);
        Mockito.when(passed2.getPreviousResult()).thenReturn(passed1);

        StabilityTestDataPublisher publisher = Mockito.mock(StabilityTestDataPublisher.class);
        DescriptorImpl desc = Mockito.mock(DescriptorImpl.class);

        Mockito.when(publisher.getDescriptor()).thenReturn(desc);
        Mockito.when(publisher.addResultToMap(2, null, new HashMap<String, CircularStabilityHistory>(), passed1, 5))
                .thenCallRealMethod();
        Mockito.when(publisher.addResultToMap(3, null, new HashMap<String, CircularStabilityHistory>(), passed2, 5))
                .thenCallRealMethod();
        Mockito.when(desc.getMaxHistoryLength()).thenReturn(5);

        CircularStabilityHistory passedRingBuffer1 = publisher.addResultToMap(2, null,
                new HashMap<String, CircularStabilityHistory>(), passed1, 5);

        StabilityTestAction passedAction = new StabilityTestAction(passedRingBuffer1);

        Mockito.when(passed1.getTestAction(StabilityTestAction.class)).thenReturn(passedAction);

        CircularStabilityHistory passedRingBuffer2 = publisher.addResultToMap(3, null,
                new HashMap<String, CircularStabilityHistory>(), passed2, 5);

        Assert.assertEquals("", passedRingBuffer2.getStackTrace());
    }

    @Test
    public void getsStackTraceWhenRegressionFixed() {
        String expected = "This is the stack trace";

        CaseResult passed = Mockito.mock(CaseResult.class);
        CaseResult regressed = Mockito.mock(CaseResult.class);
        CaseResult fixed = Mockito.mock(CaseResult.class);

        Mockito.when(passed.getStatus()).thenReturn(CaseResult.Status.PASSED);

        Mockito.when(regressed.getStatus()).thenReturn(CaseResult.Status.REGRESSION);
        Mockito.when(regressed.getErrorStackTrace()).thenReturn(expected);
        Mockito.when(regressed.getPreviousResult()).thenReturn(passed);

        Mockito.when(fixed.getStatus()).thenReturn(CaseResult.Status.FIXED);
        Mockito.when(fixed.getPreviousResult()).thenReturn(regressed);

        StabilityTestDataPublisher publisher = Mockito.mock(StabilityTestDataPublisher.class);
        DescriptorImpl desc = Mockito.mock(DescriptorImpl.class);

        Mockito.when(publisher.getDescriptor()).thenReturn(desc);
        Mockito.when(publisher.addResultToMap(2, null, new HashMap<String, CircularStabilityHistory>(), passed, 5))
                .thenCallRealMethod();
        Mockito.when(publisher.addResultToMap(3, null, new HashMap<String, CircularStabilityHistory>(), regressed, 5))
                .thenCallRealMethod();
        Mockito.when(publisher.addResultToMap(4, null, new HashMap<String, CircularStabilityHistory>(), fixed, 5))
                .thenCallRealMethod();
        Mockito.when(desc.getMaxHistoryLength()).thenReturn(5);

        CircularStabilityHistory passedRingBuffer = publisher.addResultToMap(2, null,
                new HashMap<String, CircularStabilityHistory>(), passed, 5);

        StabilityTestAction passedAction = new StabilityTestAction(passedRingBuffer);

        Mockito.when(passed.getTestAction(StabilityTestAction.class)).thenReturn(passedAction);

        CircularStabilityHistory regressedRingBuffer = publisher.addResultToMap(3, null,
                new HashMap<String, CircularStabilityHistory>(), regressed, 5);

        StabilityTestAction regressedAction = new StabilityTestAction(regressedRingBuffer);

        Mockito.when(regressed.getTestAction(StabilityTestAction.class)).thenReturn(regressedAction);

        CircularStabilityHistory fixedRingBuffer = publisher.addResultToMap(4, null,
                new HashMap<String, CircularStabilityHistory>(), fixed, 5);

        Assert.assertEquals(expected, fixedRingBuffer.getStackTrace());
    }
}
