package project.uam.util;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

//Email utility class to send OTP.
public class EmailUtil {
	
    public static void sendEmail(String recipientEmail, String subject, String message) throws MessagingException {
        String fromEmail = "";
        String password = "";

        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(fromEmail));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        msg.setSubject(subject);
        msg.setText(message);

        Transport.send(msg);
    }
}
