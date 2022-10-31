package tide.trader.bot.service;

import lombok.RequiredArgsConstructor;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.dto.util.CurrencyPairMetaDataDTO;
import tide.trader.bot.util.base.Base;
import tide.trader.bot.util.base.service.BaseService;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Exchange service - XChange implementation of {@link ExchangeService}.
 */
@RequiredArgsConstructor
public class ExchangeServiceXChangeImplementation extends BaseService implements ExchangeService {

    /** XChange service. */
    private final Exchange exchange;
    /** XChange fee. */
    private final Map<String, BigDecimal> tradingFee = new HashMap<>();

    @Override
    @SuppressWarnings("checkstyle:DesignForExtension")
    public Set<CurrencyPairDTO> getAvailableCurrencyPairs() {
        logger.debug("Retrieving available currency pairs");
        return exchange.getExchangeMetaData()
                .getCurrencyPairs()
                .keySet()
                .stream()
                .peek(cp -> logger.debug(" - {} available", cp))
                .map(Base.CURRENCY_MAPPER::mapToCurrencyPairDTO)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public CurrencyPairMetaDataDTO getCurrencyPairMetaData(CurrencyPairDTO currencyPair) {
        logger.debug("Retrieving available currency pairs metaData");
        CurrencyPairMetaData currencyPairMetaData = exchange.getExchangeMetaData().getCurrencyPairs().get(Base.CURRENCY_MAPPER.mapToCurrencyPair(currencyPair));
        return Base.CURRENCY_MAPPER.mapToCurrencyPairMetaDataDTO(currencyPair, currencyPairMetaData);
    }

    @Override
    public boolean isSimulatedExchange() {
        //logger.debug("Retrieving available Exchange name");
        return exchange.getExchangeSpecification().getExchangeName().equalsIgnoreCase("Simulated");
    }

    @Override
    public BigDecimal getTradingFee(){
        if(tradingFee.containsKey(exchange.getExchangeSpecification().getExchangeName())) {
            return tradingFee.get(exchange.getExchangeSpecification().getExchangeName());
        }
        List<CurrencyPairMetaData> currencyPairMetaDataList = exchange.getExchangeMetaData()
                .getCurrencyPairs()
                .values()
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        BigDecimal fee;
        if(currencyPairMetaDataList.isEmpty()) {
            fee = new BigDecimal("0.002");
        } else {
            fee = currencyPairMetaDataList.stream()
                    .map(CurrencyPairMetaData::getTradingFee)
                    .max(Comparator.naturalOrder())
                    .get()
                    .divide(new BigDecimal("100"));
        }
        tradingFee.put(exchange.getExchangeSpecification().getExchangeName(), fee);
        return fee;
    }

}
