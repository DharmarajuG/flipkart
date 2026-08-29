package shop.krishna.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import shop.krishna.notification.config.NotificationProperties;

/**
 * Thin wrapper over {@link JavaMailSender}. In local/dev the mail host points at
 * MailHog (localhost:1025) so nothing leaves the machine; in production it would
 * target a real SMTP relay or SES.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailSender {

    private final JavaMailSender mailSender;
    private final NotificationProperties properties;

    public void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.getFromAddress());
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("Email sent to={} subject='{}'", to, subject);
    }
}
