package tide.trader.bot.util.parameters;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import tide.trader.bot.util.validator.Rate;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Parameters from application.properties.
 */
@Validated
@Getter
@Setter
@ToString
@EnableConfigurationProperties({ExchangeParameters.class,
        ExchangeParameters.Modes.class,
        ExchangeParameters.Rates.class,
        ExchangeParameters.Mail.class,
        ExchangeParameters.Wechat.class,
        ExchangeParameters.Dingding.class})
@ConfigurationProperties(prefix = "trading.bot.exchange")
public class ExchangeParameters {

    /** Driver class name. For example: org.knowm.xchange.coinbasepro.CoinbaseProExchange, kraken, kucoin. */
    @NotEmpty(message = "Driver class name required, for example: org.knowm.xchange.coinbasepro.CoinbaseProExchange")
    private String driverClassName;

    /** Trading domain */
    @NotEmpty(message = "domain required, for example: tide.trader.bot.dto.strategy.StrategyDomainDTO")
    private String domain;

    /** API username. */
    @NotEmpty(message = "API username required")
    private String username;

    /** API passphrase and Receive webhook signal to verify key. */
    private String passphrase;

    /** API key. */
    @NotEmpty(message = "API key required")
    private String key;

    /** API secret. */
    @NotEmpty(message = "API secret required")
    private String secret;

    /** Proxy host. */
    private String proxyHost;

    /** Proxy port. */
    private Integer proxyPort;

    /** Secure API endpoint. */
    private String sslUri;

    /** Plain text API endpoint. */
    private String plainTextUri;

    /** Exchange port parameter. */
    private String host;

    /** Exchange port parameter. */
    private Integer port;

    /** Modes. */
    @Valid
    private Modes modes = new Modes();

    /** API Calls rates. */
    @Valid
    private Rates rates = new Rates();

    @Valid
    private Mail mail = new Mail();

    @Valid
    private Wechat wechat = new Wechat();

    @Valid
    private Dingding dingding = new Dingding();

    /** StrategyParameters. */
    private Map<String, String> strategyParameters = new HashMap<>();

    /** Exchange modes. */
    @Validated
    @Getter
    @Setter
    @ToString
    @ConfigurationProperties(prefix = "trading.bot.exchange.modes")
    public static class Modes {

        /** Set it to true to use the sandbox. */
        @NotNull(message = "Sandbox parameter required, set it to true to use the exchange sandbox")
        private Boolean sandbox;

        /** Set it to true to use the dry mode. */
        @NotNull(message = "Dry parameter required, set it to true to use the dry mode (simulated exchange)")
        private Boolean dry;

        //@NotNull(message = "Leverage parameter required, set it to true to use the PerpetualSwap leverage")
        private String leverage;

    }

    /** Exchange API rate calls. */
    @Validated
    @Getter
    @Setter
    @ToString
    @ConfigurationProperties(prefix = "trading.bot.exchange.rates")
    public static class Rates {

        /** Delay between calls to account API. */
        @NotNull(message = "Delay between calls to account API is mandatory")
        @Rate(message = "Invalid account rate - Enter a long value (ex: 123) or a standard ISO 8601 duration (ex: PT10H)")
        private String account;

        /** Delay between calls to ticker API. */
        @NotNull(message = "Delay between calls to ticker API is mandatory")
        @Rate(message = "Invalid ticker rate - Enter a long value (ex: 123) or a standard ISO 8601 duration (ex: PT10H)")
        private String ticker;

        /** Delay between calls to trade API. */
        @NotNull(message = "Delay between calls to trade API is mandatory")
        @Rate(message = "Invalid trade rate - Enter a long value (ex: 123) or a standard ISO 8601 duration (ex: PT10H)")
        private String trade;

        /** tvsignalexpire. */
        @NotNull(message = "Receiving webhook signal failure")
        @Rate(message = "Invalid expire rate - Enter a long value (ex: 123) or a standard ISO 8601 duration (ex: PT10H)")
        private String expire;

        /**
         * Returns account rate value in ms.
         *
         * @return account rate value in ms
         */
        public long getAccountValueInMs() {
            return getRateValue(account);
        }

        /**
         * Returns ticker rate value in ms.
         *
         * @return ticker rate value in ms
         */
        public long getTickerValueInMs() {
            return getRateValue(ticker);
        }

        /**
         * Returns trade rate value in ms.
         *
         * @return trade rate value in ms
         */
        public long getTradeValueInMs() {
            return getRateValue(ticker);
        }

        /**
         * Returns expire rate value in ms.
         *
         * @return expire rate value in ms
         */
        public long getExpireValueInSec() {
            return Duration.ofMillis(getRateValue(expire)).toSeconds();
        }

        /**
         * Return rate value in ms.
         *
         * @param stringValue string value
         * @return long value (ms)
         */
        private static long getRateValue(final String stringValue) {
            if (NumberUtils.isCreatable(stringValue)) {
                return Long.parseLong(stringValue);
            } else {
                return Duration.parse(stringValue).toMillis();
            }
        }

    }

    /** Exchange modes. */
    @Validated
    @Getter
    @Setter
    @ToString
    @ConfigurationProperties(prefix = "trading.bot.exchange.mail")
    public static class Mail {

        /** enable . */
        private Boolean enable;

        /** host of the mail. */
        private String host;

        /** port of the mail. */
        private Integer port;

        /** ssl . */
        private Boolean ssl;

        /** User name of the mail. */
        private String username;

        /** password of the mail. */
        private String password;

        /** protocol of the mail. */
        private String timout;

        /** defaultEncoding of the mail. */
        private String encoding;

        /** mail recipient . */
        private String to;


    }

    @Validated
    @Getter
    @Setter
    @ToString
    @ConfigurationProperties(prefix = "trading.bot.exchange.wechat")
    public static class Wechat {

        /** enable . */
        private Boolean enable;

        /** username . */
        private String username;

        /** Wechat url . */
        private String url;

        /** Wechat message . */
        private String message;

    }

    @Validated
    @Getter
    @Setter
    @ToString
    @ConfigurationProperties(prefix = "trading.bot.exchange.dingding")
    public static class Dingding {

        /** enable . */
        private Boolean enable;

        /** username . */
        private String username;

        /** atMobiles . */
        private String atMobiles;

        /** Dingding webhook . */
        private String url;

        /** Dingding secret . */
        private String secret;

    }

}
