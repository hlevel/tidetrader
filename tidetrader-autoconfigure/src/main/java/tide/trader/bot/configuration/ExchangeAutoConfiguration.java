package tide.trader.bot.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.derivative.Domain;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import si.mazi.rescu.HttpStatusIOException;
import tide.trader.bot.batch.*;
import tide.trader.bot.repository.OrderRepository;
import tide.trader.bot.repository.PositionRepository;
import tide.trader.bot.repository.TradeRepository;
import tide.trader.bot.service.*;
import tide.trader.bot.util.exception.ConfigurationException;
import tide.trader.bot.util.notification.MessageNotify;
import tide.trader.bot.util.parameters.ExchangeParameters;
import tide.trader.bot.util.base.configuration.BaseConfiguration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * ExchangeConfiguration configures the exchange connection.
 */
@Configuration
@EnableConfigurationProperties(ExchangeParameters.class)
@RequiredArgsConstructor
public class ExchangeAutoConfiguration extends BaseConfiguration {

    /** Unauthorized http status code. */
    private static final int UNAUTHORIZED_STATUS_CODE = 401;

    /** Application context. */
    private final ApplicationContext applicationContext;

    /** Exchange parameters. */
    private final ExchangeParameters exchangeParameters;

    /** Order repository. */
    private final OrderRepository orderRepository;

    /** Trade repository. */
    private final TradeRepository tradeRepository;

    /** Position repository. */
    private final PositionRepository positionRepository;

    /** XChange. */
    private Exchange xChangeExchange;

    /** XChange account service. */
    private AccountService xChangeAccountService;

    /** XChange market data service. */
    private MarketDataService xChangeMarketDataService;

    /** XChange trade service. */
    private org.knowm.xchange.service.trade.TradeService xChangeTradeService;

    /** Exchange service. */
    private ExchangeService exchangeService;

    /** User service. */
    private UserService userService;

    /** Market service. */
    private MarketService marketService;

    /** Trade service. */
    private TradeService tradeService;

    /** Position service. */
    private PositionService positionService;

    /** Message service. */
    private MessageService messageService;

    /** Account flux. */
    private AccountFlux accountFlux;

    /** Ticker flux. */
    private TickerFlux tickerFlux;

    /** Order flux. */
    private OrderFlux orderFlux;

    /** Trade flux. */
    private TradeFlux tradeFlux;

    /** Position flux. */
    private PositionFlux positionFlux;

    /**
     * Instantiating the exchange services based on user parameters.
     */
    @PostConstruct
    public void configure() {
        try {
            // Instantiate exchange class.
            Class<? extends Exchange> exchangeClass = Class.forName(getExchangeClassName()).asSubclass(Exchange.class);
            ExchangeSpecification exchangeSpecification = new ExchangeSpecification(exchangeClass);

            // Exchange configuration.
            exchangeSpecification.setUserName(exchangeParameters.getUsername());
            exchangeSpecification.setApiKey(exchangeParameters.getKey());
            exchangeSpecification.setSecretKey(exchangeParameters.getSecret());
            exchangeSpecification.getResilience().setRateLimiterEnabled(true);
            exchangeSpecification.setExchangeSpecificParametersItem("Use_Sandbox", exchangeParameters.getModes().getSandbox());
            exchangeSpecification.setExchangeSpecificParametersItem("passphrase", exchangeParameters.getPassphrase());
            if(StringUtils.isNotBlank(exchangeParameters.getProxyHost())) {
                exchangeSpecification.setProxyHost(exchangeParameters.getProxyHost());
                exchangeSpecification.setProxyPort(exchangeParameters.getProxyPort());
            }
            exchangeSpecification.setSslUri(exchangeParameters.getSslUri());
            exchangeSpecification.setPlainTextUri(exchangeParameters.getPlainTextUri());
            exchangeSpecification.setHost(exchangeParameters.getHost());
            if (exchangeParameters.getPort() != null) {
                exchangeSpecification.setPort(exchangeParameters.getPort());
            }

            // Creates XChange services.
            xChangeExchange = ExchangeFactory.INSTANCE.createExchange(exchangeSpecification, Domain.valueOf(exchangeParameters.getDomain().toUpperCase()));
            xChangeAccountService = xChangeExchange.getAccountService();
            xChangeMarketDataService = xChangeExchange.getMarketDataService();
            xChangeTradeService = xChangeExchange.getTradeService();

            // Force login to check credentials.
            logger.info("Exchange connection with driver {}", exchangeParameters.getDriverClassName());
            xChangeAccountService.getAccountInfo();
            logger.info("Exchange connection successful with username {} (Dry mode: {} / Sandbox: {} / Domain: {} / Leverage: {})",
                    exchangeParameters.getUsername(),
                    exchangeParameters.getModes().getDry(),
                    exchangeParameters.getModes().getSandbox(),
                    exchangeParameters.getDomain(),
                    exchangeParameters.getModes().getLeverage());
        } catch (ClassNotFoundException e) {
            // If we can't find the exchange class.
            throw new ConfigurationException("Impossible to find the exchange driver class you requested: " + exchangeParameters.getDriverClassName(),
                    "Choose and configure a valid exchange (https://trading-bot.cassandre.tech/learn/exchange-connection-configuration.html#how-does-it-works)");
        } catch (HttpStatusIOException e) {
            if (e.getHttpStatusCode() == UNAUTHORIZED_STATUS_CODE) {
                // Authorization failure.
                throw new ConfigurationException("Invalid credentials for " + exchangeParameters.getDriverClassName(),
                        "Check your exchange credentials: " + e.getMessage() + " - login used: " + exchangeParameters.getUsername());
            } else {
                // Another HTTP failure.
                throw new ConfigurationException("Error while connecting to the exchange: " + e.getMessage());
            }
        } catch (IOException e) {
            throw new ConfigurationException("IO error: " + e.getMessage());
        }
    }

    /**
     * Returns the XChange class based on the exchange name.
     * This is used in case the full driver class with package is not given in the parameters.
     *
     * @return XChange class name
     */
    private String getExchangeClassName() {
        // If the name contains a dot, it means that the user set the complete XChange class name in the configuration.
        if (exchangeParameters.getDriverClassName() != null && exchangeParameters.getDriverClassName().contains(".")) {
            return exchangeParameters.getDriverClassName();
        } else {
            // Try to guess the XChange class package name from the exchange name parameter.
            return "org.knowm.xchange."                                                             // Package (org.knowm.xchange.).
                    .concat(exchangeParameters.getDriverClassName().toLowerCase())                  // domain (kucoin).
                    .concat(".")                                                                    // A dot (.)
                    .concat(exchangeParameters.getDriverClassName().substring(0, 1).toUpperCase())  // First letter uppercase (K).
                    .concat(exchangeParameters.getDriverClassName().substring(1).toLowerCase())     // The rest of the exchange name (ucoin).
                    .concat("Exchange");                                                            // Adding exchange (Exchange).
        }
    }

    /**
     * Getter xChangeExchange.
     *
     * @return xChangeExchange
     */
    @Bean
    public Exchange getXChangeExchange() {
        return xChangeExchange;
    }

    /**
     * Getter xChangeAccountService.
     *
     * @return xChangeAccountService
     */
    @Bean
    public AccountService getXChangeAccountService() {
        return xChangeAccountService;
    }

    /**
     * Getter xChangeMarketDataService.
     *
     * @return xChangeMarketDataService
     */
    @Bean
    public MarketDataService getXChangeMarketDataService() {
        return xChangeMarketDataService;
    }

    /**
     * Getter xChangeTradeService.
     *
     * @return xChangeTradeService
     */
    @Bean
    public org.knowm.xchange.service.trade.TradeService getXChangeTradeService() {
        return xChangeTradeService;
    }

    /**
     * Getter for exchangeService.
     *
     * @return exchangeService
     */
    @Bean
    @DependsOn("getXChangeExchange")
    public ExchangeService getExchangeService() {
        if (exchangeService == null) {
            exchangeService = new ExchangeServiceXChangeImplementation(xChangeExchange);
        }
        return exchangeService;
    }

    /**
     * Getter for userService.
     *
     * @return userService
     */
    @Bean
    @DependsOn("getXChangeAccountService")
    public UserService getUserService() {
        if (userService == null) {
            userService = new UserServiceXChangeImplementation(
                    exchangeParameters.getRates().getAccountValueInMs(),
                    getXChangeAccountService());
        }
        return userService;
    }

    /**
     * Getter for marketService.
     *
     * @return marketService
     */
    @Bean
    @DependsOn("getXChangeMarketDataService")
    public MarketService getMarketService() {
        if (marketService == null) {
            marketService = new MarketServiceXChangeImplementation(
                    exchangeParameters.getRates().getTickerValueInMs(),
                    getXChangeMarketDataService());
        }
        return marketService;
    }

    /**
     * Getter for tradeService.
     *
     * @return tradeService
     */
    @Bean
    @DependsOn("getXChangeTradeService")
    public TradeService getTradeService() {
        if (tradeService == null) {
            tradeService = new TradeServiceXChangeImplementation(
                    exchangeParameters.getRates().getTradeValueInMs(),
                    orderRepository,
                    getXChangeTradeService());
        }
        return tradeService;
    }

    @Bean
    @DependsOn("getExchangeService")
    public MessageService getMessageService() {
        if (messageService == null) {
            messageService = new MessageServiceImplementation(getExchangeService().isSimulatedExchange(),
                    applicationContext.getBeansOfType(MessageNotify.class)
                            .values()
                            .stream()
                            .filter(MessageNotify::isEnable)
                            .collect(Collectors.toSet()));
        }
        return messageService;
    }

    /**
     * Getter for accountFlux.
     *
     * @return accountFlux
     */
    @Bean
    @DependsOn("getXChangeTradeService")
    public AccountFlux getAccountFlux() {
        if (accountFlux == null) {
            accountFlux = new AccountFlux(getUserService());
        }
        return accountFlux;
    }

    /**
     * Getter for tickerFlux.
     *
     * @return tickerFlux
     */
    @Bean
    @DependsOn("getMarketService")
    public TickerFlux getTickerFlux() {
        if (tickerFlux == null) {
            tickerFlux = new TickerFlux(applicationContext, getMarketService());
        }
        return tickerFlux;
    }

    /**
     * Getter for orderFlux.
     *
     * @return orderFlux
     */
    @Bean
    @DependsOn("getTradeService")
    public OrderFlux getOrderFlux() {
        if (orderFlux == null) {
            orderFlux = new OrderFlux(orderRepository, getTradeService());
        }
        return orderFlux;
    }

    /**
     * Getter for tradeFlux.
     *
     * @return tradeFlux
     */
    @Bean
    @DependsOn("getTradeService")
    public TradeFlux getTradeFlux() {
        if (tradeFlux == null) {
            tradeFlux = new TradeFlux(orderRepository, tradeRepository, getTradeService());
        }
        return tradeFlux;
    }

    /**
     * Getter for positionFlux.
     *
     * @return positionFlux
     */
    @Bean
    @DependsOn("getTradeService")
    public PositionFlux getPositionFlux() {
        if (positionFlux == null) {
            positionFlux = new PositionFlux(positionRepository);
        }
        return positionFlux;
    }

    /**
     * Getter for positionService.
     *
     * @return positionService
     */
    @Bean
    @DependsOn({"getTradeService", "getPositionFlux"})
    public PositionService getPositionService() {
        if (positionService == null) {
            positionService = new PositionServiceCassandreImplementation(positionRepository, getTradeService(), positionFlux);
        }
        return positionService;
    }

}
