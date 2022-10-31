package tide.trader.bot.util.dry;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import tide.trader.bot.strategy.CassandreStrategy;
import tide.trader.bot.strategy.internal.CassandreStrategyInterface;
import tide.trader.bot.util.base.service.BaseService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * AOP for exchange service in dry mode.
 */
@Aspect
@Component
@ConditionalOnExpression("${trading.bot.exchange.modes.dry:true}")
@RequiredArgsConstructor
public class ExchangeServiceDryModeAOP extends BaseService {

    /** Application context. */
    private final ApplicationContext applicationContext;

    /**
     * getExchangeMetaData() AOP for dry mode.
     *
     * @param pjp ProceedingJoinPoint
     * @return list of supported currency pairs
     */
    @Around("execution(* org.knowm.xchange.Exchange.getExchangeMetaData())")
    public final ExchangeMetaData getExchangeMetaData(final ProceedingJoinPoint pjp) {
        return new ExchangeMetaData(applicationContext
                .getBeansWithAnnotation(CassandreStrategy.class)
                .values()  // We get the list of all required cp of all strategies.
                .stream()
                .map(o -> (CassandreStrategyInterface) o)
                .map(CassandreStrategyInterface::getRequestedCurrencyPairs)
                .flatMap(Set::stream)
                .map(CURRENCY_MAPPER::mapToCurrencyPair)
                .collect(HashMap::new, (map, cp) -> map.put(cp, null), Map::putAll),
                null,
                null,
                null,
                null);
    }

}
