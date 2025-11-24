package com.softpeces.infra;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class MailService {
    private final boolean enabled;
    private final Session session;
    private final String from;

    public MailService() {
        String host = AppConfig.s("mail.smtp.host", "").trim();
        String fromAddress = AppConfig.s("mail.from", "").trim();
        boolean flag = AppConfig.b("mail.enabled", true);

        System.out.println("MailService config - host: " + host + ", from: " + fromAddress + ", enabled: " + flag);

        if (!flag || host.isEmpty() || fromAddress.isEmpty()) {
            this.enabled = false;
            this.session = null;
            this.from = fromAddress;
            System.err.println("MailService is disabled due to missing configuration. Check app.properties.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", AppConfig.s("mail.smtp.port", "587"));
        props.put("mail.smtp.starttls.enable", Boolean.toString(AppConfig.b("mail.smtp.starttls.enable", true)));
        props.put("mail.smtp.ssl.enable", Boolean.toString(AppConfig.b("mail.smtp.ssl.enable", false)));

        String user = AppConfig.s("mail.smtp.username", "").trim();
        String pass = AppConfig.s("mail.smtp.password", "");
        System.out.println("MailService auth - user: " + user + ", pass: " + (pass.isEmpty() ? "not set" : "set"));

        Authenticator authenticator = null;
        if (!user.isEmpty()) {
            props.put("mail.smtp.auth", "true");
            authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(user, pass);
                }
            };
        } else {
            props.put("mail.smtp.auth", "false");
        }

        this.session = Session.getInstance(props, authenticator);
        this.enabled = true;
        this.from = fromAddress;
        System.out.println("MailService initialized successfully with properties: " + props);
    }

    public boolean send(String to, String subject, String body) {
        if (!enabled) {
            System.out.println("[Mail] Envío deshabilitado. Verifique la configuración mail.* en data/app.properties");
            return false;
        }

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
            message.setSubject(subject, StandardCharsets.UTF_8.name());
            message.setText(body, StandardCharsets.UTF_8.name());
            System.out.println("Sending email to: " + to + " with subject: " + subject);
            Transport.send(message);
            System.out.println("Email sent successfully to: " + to);
            return true;
        } catch (MessagingException e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}