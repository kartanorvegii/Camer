package com.example.ahrytsavets.camera;

import android.util.Log;

import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

/**
 * Created by a.hrytsavets on 6/23/2017.
 */

public class MailSender extends javax.mail.Authenticator {
    private String mailhost = "smtp.gmail.com";
    private String user;
    private String password;
    private Session session;

    static {
        Security.addProvider(new JSSEProvider());
    }

    public MailSender(String user, String password) {
        this.user = user;
        this.password = password;

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mailhost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getDefaultInstance(props, this);
    }
    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }
    public synchronized void sendMail(String subject, String body, String sender, String recipients, String filename) throws Exception {
        try{
            Multipart multipart = new MimeMultipart();
            MimeMessage message = new MimeMessage(session);
//            DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plane"));
            message.setSender(new InternetAddress(sender));
            message.setSubject(subject);
//            message.setDataHandler(handler);
//            message.setFileName(filename);

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);
            multipart.addBodyPart(messageBodyPart);

            if (!filename.equalsIgnoreCase("")) {
                BodyPart attachBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(filename);
                attachBodyPart.setDataHandler(new DataHandler(source));
                attachBodyPart.setFileName(filename);

                multipart.addBodyPart(attachBodyPart);

            }

            message.setContent(multipart);

            if (recipients.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            else
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
            Transport.send(message);
            Log.d("MailSender", "Mail sended");
        }catch(Exception e){
            Log.d("MailSender: ", e.getMessage(), e.getCause());
        }
    }
}
