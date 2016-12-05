package de.esailors.jenkins.teststability;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;

import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.scm.ChangeLogSet;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;

import java.lang.InterruptedException;
import java.io.IOException;
import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author KGBTeam
 */
public class StabilityTestBuildWrapper extends BuildWrapper {

    private String committerName = "";
    private String SCMKeyword = "ignore";

    @DataBoundConstructor
    /**
     * Constructor for initializing a new build wrapper (before a build starts)
     * 
     * @param committerName
     *            Name of the person who commited the build
     * @param SCMKeyword
     *            Ignore any builds that have this keyword in the commit message
     */
    public StabilityTestBuildWrapper(String committerName, String SCMKeyword) {
        this.committerName = committerName;
        this.SCMKeyword = SCMKeyword;
    }

    /**
     * Gets the name of the person who committed the latest build
     * 
     * @return name of the person who committed the latest build
     */
    public String getCommitterName() {
        return this.committerName;
    }

    /**
     * Gets the keywords to look for in commit messages to ignore builds
     * 
     * @return the keyword to ignore builds
     */
    public String getSCMKeyword() {
        return this.SCMKeyword;
    }

    // ----- ----- ----- ----- ----- ----- ----- ----- -----

    @Override
    /**
     * Check for the specified parameters in the build's changeSet (author and
     * message) If the author or message match the specified parameters then
     * stop and ignore the build
     * 
     * @param build
     *            the current build object
     * @param launcher
     *            Object that starts a process and inherits environemtn
     *            variables
     * @param listener
     *            Object that listens for build notifications
     * @return The new build environment, if it has not been canceled
     * @throws InterruptedException
     * @throws IOException
     */
    public BuildWrapper.Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        String author = "";
        String msg = "";

        // loop through the build's change set to get the most recent SVN commit
        // items
        for (Object item : build.getChangeSet().getItems()) {

            msg = ((ChangeLogSet.Entry) item).getMsg();
            author = ((ChangeLogSet.Entry) item).getAuthor().getId();

            // if the commit message contains the keyword, stop the build
            if (this.getSCMKeyword() != "" && ((ChangeLogSet.Entry) item).getMsg().contains(this.getSCMKeyword())) {
                try {
                    build.doStop();
                    throw new InterruptedException("Build cancelled by commit message keyword");
                }
                catch (ServletException e) {
                    e.printStackTrace();
                }

            }

            // else if the build was started by a specific author, stop the
            // build
            else if (((ChangeLogSet.Entry) item).getAuthor().getId().equals(this.getCommitterName())) {
                try {
                    build.doStop();
                    throw new InterruptedException("Build cancelled because of author");
                }
                catch (ServletException e) {
                    e.printStackTrace();
                }
            }
        }
        return new Environment(author, msg);
    }

    @Override
    public Descriptor<BuildWrapper> getDescriptor() {
        return DESCRIPTOR;
    }

    /**
     * Variables and objects that are created and stored each time a build is
     * run
     * 
     * @author KGBTeam
     *
     */
    public class Environment extends BuildWrapper.Environment {
        private String author;
        private String commitMessage;
        private long timestamp;

        /**
         * Environment constructor
         * 
         * @param authorIn
         *            String of author name for who started the build
         * @param commitMessageIn
         *            String of the entire SVN commit message
         */
        public Environment(String authorIn, String commitMessageIn) {
            author = authorIn;
            commitMessage = commitMessageIn;
        }

        /**
         * Gets the author of the build/commit
         * 
         * @return String of the author who committed and started the build
         */
        public String getAuthor() {
            return author;
        }

        /**
         * Gets the commit message from the commit that started the build
         * 
         * @return the commit message of the commit that started the build
         */
        public String getCommitMessage() {
            return commitMessage;
        }

    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static final class DescriptorImpl extends BuildWrapperDescriptor {
        DescriptorImpl() {
            super(StabilityTestBuildWrapper.class);
        }

        public String getDisplayName() {
            return "test stability build ignoring";
        }

        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }
    }
}