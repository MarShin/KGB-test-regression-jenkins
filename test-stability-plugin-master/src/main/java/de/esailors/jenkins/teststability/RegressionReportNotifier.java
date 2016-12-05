package de.esailors.jenkins.teststability;

import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.tasks.Mailer;

import java.util.Date;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import jenkins.model.Jenkins;
import jenkins.model.JenkinsLocationConfiguration;

import com.twilio.sdk.*;
import com.twilio.sdk.resource.factory.*;
import com.twilio.sdk.resource.instance.*;
import org.apache.http.*;
import org.apache.http.message.*;

/**
 * The RegressionReportNotifier provides the ability to send mails/texts
 * containing information about regressed tests in current build to a specified
 * list of recipients
 *
 * @author eller86 (Kengo TODA), KGBTeam_UIUC
 */
public class RegressionReportNotifier {

    static interface MailSender {
        void send(MimeMessage message) throws MessagingException;
    }

    private MailSender mailSender = new RegressionReportNotifier.MailSender() {
        @Override
        public void send(MimeMessage message) throws MessagingException {
            Transport.send(message);
        }
    };

    private Map<String, CircularStabilityHistory> regressions = new HashMap<String, CircularStabilityHistory>();

    /**
     * The addResult method is used to incrementally fill list of regressed test
     * to be included in the mail
     *
     * @param testName
     *            The name of the test to be added
     * @param author
     *            The author of the build
     */
    public void addResult(String testName, CircularStabilityHistory history) {
        regressions.put(testName, history);
    }

    /**
     * The mailReport method sends a mail the to list of recipients passed as
     * argument, containing the list of regressed tests, and the author of the
     * build
     *
     * @param testName
     *            The name of the test to be added
     * @param author
     *            The author of the build
     *
     * @throws MessagingException
     */
    public void mailReport(String recipients, String author, BuildListener listener, AbstractBuild<?, ?> build)
            throws MessagingException {
        if (regressions.isEmpty()) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        String rootUrl = "";
        Session session = null;
        InternetAddress adminAddress = null;
        if (Jenkins.getInstance() != null) {
            rootUrl = Jenkins.getInstance().getRootUrl();
            session = Mailer.descriptor().createSession();
            adminAddress = new InternetAddress(JenkinsLocationConfiguration.get().getAdminAddress());
        }
        builder.append(Util.encode(rootUrl));
        builder.append(Util.encode(build.getUrl()));
        builder.append("\n\n");
        builder.append(regressions.size() + " regression(s) found. Author: " + author);
        builder.append("\n");
        for (Map.Entry<String, CircularStabilityHistory> e : regressions.entrySet()) {
            builder.append("  ");
            builder.append(e.getKey());
            builder.append(" ");
            CircularStabilityHistory h = e.getValue();
            if (h != null) {
                builder.append(String.format("Failed %d times in the last %d runs. Flakiness: %d%%, Stability: %d%%,",
                        h.getFailed(), h.getSize(), h.getFlakiness(), h.getStability()));
            }
            builder.append("\n");
        }

        List<Address> recipentList = parse(recipients, listener);

        MimeMessage message = new MimeMessage(session);
        // Add some better mail subject
        message.setSubject("Regression Report");
        message.setRecipients(RecipientType.TO, recipentList.toArray(new Address[recipentList.size()]));
        message.setContent("", "text/plain");
        message.setFrom(adminAddress);
        message.setText(builder.toString());
        message.setSentDate(new Date());

        mailSender.send(message);
    }

    /**
     * The textReport method sends a text the to list of recipients passed as
     * argument, containing the number of regressed tests, and the author of the
     * build
     *
     * @param textMessageRecipents
     *            The list of all text recipients
     * @param author
     *            The author of the build
     * @throws TwilioRestException
     */
    public void textReport(String number, String author, BuildListener listener, AbstractBuild<?, ?> build)
            throws TwilioRestException {
        if (regressions.isEmpty()) {
            return;
        }
        String ACCOUNT_SID = "ACfd72788d1ff93dbf058c7227eab3b1ee";
        String AUTH_TOKEN = "26f486bcea1d893728f2ba2d18103930";
        TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);

        String msg = regressions.size() + " regression(s) in the last build started by " + author;

        // Build the parameters for sending a message
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("To", number));
        params.add(new BasicNameValuePair("From", "+18157820348"));
        params.add(new BasicNameValuePair("Body", msg));

        MessageFactory messageFactory = client.getAccount().getMessageFactory();
        Message message = messageFactory.create(params);
        System.out.println(message.getSid());

    }

    // Parses a list of recipients delimited with comma
    private List<Address> parse(String recipients, BuildListener listener) {
        List<Address> list = new ArrayList<Address>();
        List<String> recipAddresses = Arrays.asList(recipients.split(","));

        for (String address : recipAddresses) {
            try {
                list.add(new InternetAddress(address));
            }
            catch (AddressException e) {
                e.printStackTrace(listener.error(e.getMessage()));
            }
        }
        return list;
    }

}
