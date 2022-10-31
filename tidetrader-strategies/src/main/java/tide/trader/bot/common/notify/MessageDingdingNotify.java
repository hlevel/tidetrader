package tide.trader.bot.common.notify;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;
import tide.trader.bot.dto.util.MessageDTO;
import tide.trader.bot.util.notification.MessageNotify;
import tide.trader.bot.util.parameters.ExchangeParameters;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.net.URLEncoder;

/**
 * Message notification
 */
@Slf4j
@Component
public class MessageDingdingNotify implements MessageNotify {

    /** Mail sender. */
    private final RestTemplate restTemplate;

    /** mail  parameters */
    private final ExchangeParameters.Dingding dingding;

    /** dingding template */
    private final String text;

    public MessageDingdingNotify(ExchangeParameters parameters, RestTemplateBuilder restTemplateBuilder) {
        this.dingding = parameters.getDingding();
        this.restTemplate = restTemplateBuilder.build();
        if(StringUtils.isNotBlank(this.dingding.getAtMobiles())){
            text = "{\"msgtype\": \"text\", \"text\": {\"content\": \"{message}\"}, \"at\": {\"atMobiles\": [" + this.dingding.getAtMobiles() + "], \"isAtAll\": false}}";
        } else {
            text = "{\"msgtype\": \"text\", \"text\": {\"content\": \"{message}\"}}";
        }
    }

    @Override
    public boolean isEnable() {
        return dingding.getEnable();
    }

    @Override
    public boolean notify(MessageDTO message) {
        try{
            log.debug("NotifyMessage[Dingding]:{}", message);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String content = text.replace("{message}", (StringUtils.isNotBlank(dingding.getUsername()) ? "User: " + dingding.getUsername() +"\n" : "" ) + message.getTitle() + "\n" + message.getBody());

            HttpEntity<String> request = new HttpEntity<>(content, headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> postForEntity = restTemplate.postForEntity(dingding.getUrl() + getUrlsign(), request, String.class);
            String body = postForEntity.getBody();
            log.debug("NotifyMessage[Dingding]:{}", body);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    private String getUrlsign(){
        Long timestamp = System.currentTimeMillis();
        try {
            String stringToSign = timestamp + "\n" + dingding.getSecret();
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(dingding.getSecret().getBytes("UTF-8"), "HmacSHA256"));
            byte[] signData = mac.doFinal(stringToSign.getBytes("UTF-8"));
            String sign = URLEncoder.encode(new String(Base64Utils.encode(signData)),"UTF-8");
            return "&timestamp=" + timestamp + "&sign=" + sign;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }


}
