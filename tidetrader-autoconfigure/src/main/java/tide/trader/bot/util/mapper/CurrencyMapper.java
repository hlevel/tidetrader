package tide.trader.bot.util.mapper;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.meta.CurrencyPairMetaData;
import org.knowm.xchange.instrument.Instrument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import tide.trader.bot.dto.util.CurrencyAmountDTO;
import tide.trader.bot.dto.util.CurrencyDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.dto.util.CurrencyPairMetaDataDTO;
import tide.trader.bot.util.jpa.CurrencyAmount;

import java.math.BigDecimal;

/**
 * Currency mapper.
 */
@Mapper
public interface CurrencyMapper {

    // =================================================================================================================
    // XChange to DTO.

    default String mapToCurrencyString(CurrencyDTO source) {
        if (source != null) {
            return source.toString();
        } else {
            return null;
        }
    }

    default CurrencyDTO mapToCurrencyDTO(String value) {
        return new CurrencyDTO(value);
    }

    @Mapping(source = "currencyCode", target = "code")
    CurrencyDTO mapToCurrencyDTO(Currency source);

    default String mapToCurrencyPairString(CurrencyPairDTO source) {
        return source.toString();
    }

    default CurrencyPairDTO mapToCurrencyPairDTO(Instrument source) {
        final CurrencyPair cp = (CurrencyPair) source;
        CurrencyDTO base = new CurrencyDTO(cp.base.getCurrencyCode());
        CurrencyDTO quote = new CurrencyDTO(cp.counter.getCurrencyCode());
        return CurrencyPairDTO.builder().baseCurrency(base).quoteCurrency(quote).build();
    }

    default CurrencyPairDTO mapToCurrencyPairDTO(String source) {
        return new CurrencyPairDTO(source);
    }

    @Mapping(source = "base", target = "baseCurrency")
    @Mapping(source = "counter", target = "quoteCurrency")
    @Mapping(target = "baseCurrencyPrecision", ignore = true)
    @Mapping(target = "quoteCurrencyPrecision", ignore = true)
    CurrencyPairDTO mapToCurrencyPairDTO(CurrencyPair source);

    @Mapping(source = "value", target = "value")
    @Mapping(source = "currency", target = "currency")
    CurrencyAmountDTO mapToCurrencyAmountDTO(CurrencyAmount source);

    // =================================================================================================================
    // XChange to DTO.

    default Currency mapToCurrency(CurrencyDTO source) {
        if (source != null) {
            return new Currency(source.getCode());
        } else {
            return null;
        }
    }

    default CurrencyPair mapToCurrencyPair(CurrencyPairDTO source) {
        return new CurrencyPair(source.getBaseCurrency().getCode(), source.getQuoteCurrency().getCode());
    }

    default Instrument mapToInstrument(CurrencyPairDTO source) {
        return mapToCurrencyPair(source);
    }

    default Instrument mapToInstrument(CurrencyPair source) {
        return mapToCurrencyPair(mapToCurrencyPairDTO(source));
    }


    @Named("mapToCurrencyDTO")
    default CurrencyPairMetaDataDTO mapToCurrencyPairMetaDataDTO(CurrencyPairDTO currencyPair, CurrencyPairMetaData currencyPairMetaData){
        if(currencyPair != null && currencyPairMetaData != null) {
            return CurrencyPairMetaDataDTO.builder()
                    .currencyPair(currencyPair)
                    .tradingFee(currencyPairMetaData.getTradingFee())
                    .baseScale(currencyPairMetaData.getBaseScale())
                    .priceScale(currencyPairMetaData.getPriceScale())
                    .minimumAmount(currencyPairMetaData.getMinimumAmount())
                    .counterMinimumAmount(currencyPairMetaData.getCounterMinimumAmount() == null ? BigDecimal.ZERO : currencyPairMetaData.getCounterMinimumAmount())
                    .build();
        } else {
            return null;
        }
    }

    @Named("mapToCurrencyDTO")
    default CurrencyPairMetaDataDTO mapToCurrencyPairMetaDataDTO(CurrencyPair currencyPair, CurrencyPairMetaData currencyPairMetaData){
        return mapToCurrencyPairMetaDataDTO(mapToCurrencyPairDTO(currencyPair), currencyPairMetaData);
    }

}
