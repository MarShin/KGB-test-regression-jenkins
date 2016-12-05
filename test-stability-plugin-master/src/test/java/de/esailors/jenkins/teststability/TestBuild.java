package de.esailors.jenkins.teststability;

import hudson.model.*;
import hudson.scm.ChangeLogSet;
import hudson.scm.ChangeLogSet.Entry;
import java.io.IOException;
import java.util.Calendar;
import java.io.File;

public class TestBuild<P extends Project<P, B>, B extends Build<P, B>> extends Build<P, B> {

    /**
     * Creates a new build.
     */
    protected TestBuild(P project) throws IOException {
        super(project);
    }

    protected TestBuild(P job, Calendar timestamp) {
        super(job, timestamp);
    }

    /**
     * Loads a build from a log file.
     */
    protected TestBuild(P project, File buildDir) throws IOException {
        super(project, buildDir);
    }

    @Override
    public String getUrl() {
        return "http://test.com";
    }

    protected Result doRun(BuildListener listener) throws Exception, RunnerAbortedException {
        return null;
    }

    public org.kohsuke.stapler.HttpResponse doStop() throws IOException, javax.servlet.ServletException {
        return null;
    }

    protected void post2(BuildListener listener) throws Exception {

    }

    public ChangeLogSet<? extends Entry> getChangeSet() {
        return new TestChangeLogSet(this);
    }
}
