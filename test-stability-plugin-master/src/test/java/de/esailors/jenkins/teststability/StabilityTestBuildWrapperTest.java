package de.esailors.jenkins.teststability;

import org.junit.Test;
import org.junit.Assert;

import hudson.model.Project;
import hudson.model.AbstractBuild;
import hudson.tasks.BuildWrapper;

import de.esailors.jenkins.teststability.StabilityTestData.Result;

import java.io.IOException;
import java.lang.InterruptedException;

import org.mockito.Mockito;

import java.util.logging.*;

public class StabilityTestBuildWrapperTest {

    private static Logger myLog = Logger.getLogger(StabilityTestBuildWrapperTest.class.getName());

    @Test
    public void authorNameMustBeCorrect() {
        StabilityTestBuildWrapper wrapper = new StabilityTestBuildWrapper("blah", "blah");
        try {
            BuildWrapper.Environment env = wrapper.setUp(((AbstractBuild) new TestBuild(Mockito.mock(Project.class))),
                    null, null);
            Assert.assertEquals("testuser", ((StabilityTestBuildWrapper.Environment) env).getAuthor());
        }
        catch (InterruptedException e) {
            Assert.fail();
        }
        catch (IOException e) {
            Assert.fail();
        }
    }

    @Test
    public void commitMessageMustBeCorrect() {
        StabilityTestBuildWrapper wrapper = new StabilityTestBuildWrapper("blah", "blah");
        try {
            BuildWrapper.Environment env = wrapper.setUp(((AbstractBuild) new TestBuild(Mockito.mock(Project.class))),
                    null, null);
            Assert.assertEquals("test message", ((StabilityTestBuildWrapper.Environment) env).getCommitMessage());
        }
        catch (InterruptedException e) {
            Assert.fail();
        }
        catch (IOException e) {
            Assert.fail();
        }
    }

    @Test
    public void testFilterByAuthor() {
        StabilityTestBuildWrapper wrapper = new StabilityTestBuildWrapper("testuser", "testuser");
        Boolean test = false;
        try {
            BuildWrapper.Environment env = wrapper.setUp(((AbstractBuild) new TestBuild(Mockito.mock(Project.class))),
                    null, null);
        }
        catch (InterruptedException e) {
            test = true;
        }
        catch (IOException e) {
            Assert.fail();
        }
        Assert.assertTrue(test);
    }

    @Test
    public void testFilterByComment() {
        StabilityTestBuildWrapper wrapper = new StabilityTestBuildWrapper("blah", "test message");
        Boolean test = false;
        try {
            BuildWrapper.Environment env = wrapper.setUp(((AbstractBuild) new TestBuild(Mockito.mock(Project.class))),
                    null, null);
        }
        catch (InterruptedException e) {
            test = true;
        }
        catch (IOException e) {
            Assert.fail();
        }
        Assert.assertTrue(test);
    }

}
