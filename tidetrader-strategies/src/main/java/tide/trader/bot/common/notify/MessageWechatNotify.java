package tide.trader.bot.common.notify;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tide.trader.bot.dto.util.MessageDTO;
import tide.trader.bot.util.notification.MessageNotify;
import tide.trader.bot.util.parameters.ExchangeParameters;

import java.util.HashMap;
import java.util.Map;

/**
 * Message notification
 */
@Slf4j
@Component
public class MessageWechatNotify implements MessageNotify {

    /** Mail sender. */
    private final RestTemplate restTemplate;

    /** mail  parameters */
    private final ExchangeParameters.Wechat wechat;

    public MessageWechatNotify(ExchangeParameters parameters, RestTemplateBuilder restTemplateBuilder) {
        this.wechat = parameters.getWechat();
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public boolean isEnable() {
        return wechat.getEnable();
    }

    @Override
    public boolean notify(MessageDTO message) {
        try{
            log.debug("NotifyMessage[Wechat]:{}", message);
            String encodeText = org.springframework.web.util.UriUtils.encode((StringUtils.isNotBlank(wechat.getUsername()) ? "User: " + wechat.getUsername() +"\n" : "" ) + message.getTitle() + "\n" + message.getBody(), "UTF8");
            String text = wechat.getMessage().replace("{text}", encodeText);
            Map<String, String> params = new HashMap<>();
            params.put("data", text);
            ResponseEntity<HashMap> responseEntity = restTemplate.postForEntity(wechat.getUrl(), params, HashMap.class);
            log.debug("NotifyMessage[Wechat]:{}", responseEntity.getBody());
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }


}
