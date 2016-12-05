package de.esailors.jenkins.teststability;

import org.junit.Test;

import java.util.logging.*;

import org.junit.Assert;
import de.esailors.jenkins.teststability.StabilityTestData.Result;

import java.io.PrintStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Folder;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.NoSuchProviderException;

import de.saly.javamail.mock2.MockMailbox;

import hudson.model.Project;
import hudson.model.AbstractBuild;
import org.mockito.Mockito;

import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;

import com.twilio.sdk.*;
import com.twilio.sdk.resource.factory.*;
import com.twilio.sdk.resource.instance.*;
import com.twilio.sdk.resource.list.*;
import org.apache.http.*;
import org.apache.http.message.*;

public class RegressionReportNotifierTest {

    private void initMailbox(String prefix) {
        String testAddress = prefix + "kgb@unknown.com";
        MockMailbox mb = null;
        try {
            mb = MockMailbox.get(testAddress);
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
    }

    private CircularStabilityHistory initCircularStabilityHistory() {
        CircularStabilityHistory c = new CircularStabilityHistory(30);
        Result a = new Result(1, true);
        Result b = new Result(2, false);
        c.add(a);
        c.add(b);
        return c;
    }

    @Test
    public void noEmailTest() {
        String prefix = "no";
        initMailbox(prefix);
        try {
            RegressionReportNotifier rrNotifier = new RegressionReportNotifier();
            rrNotifier.mailReport(prefix + "kgb@unknown.com", "ballet2", null,
                    (AbstractBuild) new TestBuild(Mockito.mock(Project.class)));
            Session session = Session.getInstance(new Properties());
            final Store store = session.getStore("pop3");
            store.connect("unknown.com", prefix + "kgb", "bleh");
            final Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            Assert.assertEquals(0, inbox.getMessageCount());
            inbox.close(true);
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Test
    public void basicEmailTest() {
        String prefix = "basic";
        initMailbox(prefix);
        try {
            RegressionReportNotifier rrNotifier = new RegressionReportNotifier();
            rrNotifier.addResult("testHistory", initCircularStabilityHistory());
            rrNotifier.mailReport(prefix + "kgb@unknown.com", "ballet2", null,
                    (AbstractBuild) new TestBuild(Mockito.mock(Project.class)));
            Session session = Session.getInstance(new Properties());
            final Store store = session.getStore("pop3");
            store.connect("unknown.com", prefix + "kgb", "bleh");
            final Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            Assert.assertEquals(1, inbox.getMessageCount());
            Assert.assertNotNull(inbox.getMessage(1));
            Assert.assertEquals("Regression Report", inbox.getMessage(1).getSubject());
            inbox.close(true);
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Test
    public void multipleEmailContentTest() {
        String prefix = "multiple";
        initMailbox(prefix);
        try {
            RegressionReportNotifier rrNotifier = new RegressionReportNotifier();
            rrNotifier.addResult("testHistory", initCircularStabilityHistory());
            rrNotifier.addResult("testHistory2", initCircularStabilityHistory());
            rrNotifier.mailReport(prefix + "kgb@unknown.com", "ballet2", null,
                    (AbstractBuild) new TestBuild(Mockito.mock(Project.class)));
            Session session = Session.getInstance(new Properties());
            final Store store = session.getStore("pop3");
            store.connect("unknown.com", prefix + "kgb", "bleh");
            final Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            Assert.assertEquals(1, inbox.getMessageCount());
            Assert.assertNotNull(inbox.getMessage(1));
            Assert.assertTrue(inbox.getMessage(1).getContent().toString().contains("2 regression(s) found."));
            inbox.close(true);
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Test
    public void successfulSendTextTest() {
        String ACCOUNT_SID = "AC71d3d1e38cc69b6d900eb5ac23ba0d68";
        String AUTH_TOKEN = "cf6cdc24c1331e5a2b07e1952fbecd24";
        TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);

        String msg = "Unit Testing Message";

        // Build the parameters for sending a message
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("To", "+16303355884"));
        params.add(new BasicNameValuePair("From", "+15005550006"));
        params.add(new BasicNameValuePair("Body", msg));

        String result = "";
        try {
            MessageFactory messageFactory = client.getAccount().getMessageFactory();
            Message message = messageFactory.create(params);
            System.out.println(message.getSid());
            result = "success";
        }
        catch (TwilioRestException e) {
            result = "fail";
        }
        Assert.assertEquals("success", result);
    }

    @Test
    public void notVerifiedNumberTextTest() {
        String ACCOUNT_SID = "AC71d3d1e38cc69b6d900eb5ac23ba0d68";
        String AUTH_TOKEN = "cf6cdc24c1331e5a2b07e1952fbecd24";
        TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);

        String msg = "Unit Testing Message";

        // Build the parameters for sending a message
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("To", "+15005550001"));
        params.add(new BasicNameValuePair("From", "+15005550006"));
        params.add(new BasicNameValuePair("Body", msg));

        String result = "";
        try {
            MessageFactory messageFactory = client.getAccount().getMessageFactory();
            Message message = messageFactory.create(params);
            System.out.println(message.getSid());
            result = "success";
        }
        catch (TwilioRestException e) {
            result = "fail";
        }
        Assert.assertEquals("fail", result);
    }

    @Test
    public void notRealPhoneNumberTextTest() {
        String ACCOUNT_SID = "AC71d3d1e38cc69b6d900eb5ac23ba0d68";
        String AUTH_TOKEN = "cf6cdc24c1331e5a2b07e1952fbecd24";
        TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);

        String msg = "Unit Testing Message";

        // Build the parameters for sending a message
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("To", "+10"));
        params.add(new BasicNameValuePair("From", "+15005550006"));
        params.add(new BasicNameValuePair("Body", msg));

        String result = "";
        try {
            MessageFactory messageFactory = client.getAccount().getMessageFactory();
            Message message = messageFactory.create(params);
            System.out.println(message.getSid());
            result = "success";
        }
        catch (TwilioRestException e) {
            result = "fail";
        }
        Assert.assertEquals("fail", result);
    }

}
