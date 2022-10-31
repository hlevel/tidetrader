package tide.trader.bot.util.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import tide.trader.bot.dto.util.MessageDTO;
import tide.trader.bot.util.parameters.ExchangeParameters;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Message notification
 */
@Slf4j
@Component
public class MessageEmailNotify implements MessageNotify{

    /** Mail sender. */
    private final JavaMailSender mailSender;

    /** mail  parameters */
    private final ExchangeParameters.Mail mail;

    public MessageEmailNotify(ExchangeParameters parameters) {
        this.mail = parameters.getMail();
        this.mailSender = this.getMailSender(parameters.getMail());
    }

    @Override
    public boolean isEnable() {
        return mail.getEnable();
    }

    @Override
    public boolean notify(MessageDTO message) {
        log.debug("NotifyMessage[Email]:{}", message);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, mail.getEncoding());
            helper.setFrom(mail.getUsername());
            helper.setTo(mail.getTo());
            //helper.setSubject("【"+name+"】"+title);
            helper.setSubject(message.getTitle());
            helper.setText(message.getBody().replace("\n", "<br/>"), true);
        } catch (MessagingException e) {
            log.error("Unable to write e-mail", e);
            return false;
        }

        try {
            mailSender.send(mimeMessage);
            return true;
        } catch (MailException e) {
            log.error("Error sending e-mail", e);
            if(StringUtils.contains(e.getMessage(), "Invalid Addresses")) {
                return false;
            }
            return false;
        }
    }

    /**
     * Build mail service
     * @param mail
     * @return
     */
    private JavaMailSenderImpl getMailSender(ExchangeParameters.Mail mail) {
        if(!mail.getEnable()) {
            return null;
        }
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(mail.getHost());
        sender.setPort(mail.getPort());
        sender.setUsername(mail.getUsername());
        sender.setPassword(mail.getPassword());
        sender.setDefaultEncoding(mail.getEncoding());
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.timeout", mail.getTimout());
        properties.setProperty("mail.smtp.auth", "false");
        if(mail.getSsl()) {
            properties.setProperty("mail.smtp.ssl.enable", "true");
        }
        sender.setJavaMailProperties(properties);
        return sender;
    }

}
