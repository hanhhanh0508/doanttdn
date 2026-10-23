package com.sapota.seo.service;

import com.sapota.seo.util.AppSettings;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * Gửi email qua SMTP (mặc định Gmail, cổng 587 + STARTTLS; cổng 465 dùng SSL).
 * Với Gmail cần bật xác minh 2 bước rồi tạo "Mật khẩu ứng dụng" (App Password) để điền vào ô mật khẩu.
 */
public class EmailService {

    public static boolean isConfigured() {
        return AppSettings.isEmailEnabled()
                && !AppSettings.getSmtpHost().isEmpty()
                && !AppSettings.getSmtpUser().isEmpty()
                && !AppSettings.getSmtpPassword().isEmpty()
                && !AppSettings.getEmailTo().isEmpty();
    }

    /** Gửi 1 email văn bản thuần tới địa chỉ nhận trong cài đặt. Ném MessagingException nếu lỗi. */
    public void send(String subject, String body) throws MessagingException {
        final String host = AppSettings.getSmtpHost();
        final int port = AppSettings.getSmtpPort();
        final String user = AppSettings.getSmtpUser();
        final String password = AppSettings.getSmtpPassword();

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "15000");
        if (port == 465) {
            props.put("mail.smtp.ssl.enable", "true");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(user));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(AppSettings.getEmailTo()));
        msg.setSubject(subject, "UTF-8");
        msg.setText(body, "UTF-8");
        Transport.send(msg);
    }
}
