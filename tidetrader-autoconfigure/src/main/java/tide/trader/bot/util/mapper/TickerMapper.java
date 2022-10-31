package tide.trader.bot.util.mapper;

import org.knowm.xchange.dto.marketdata.Kline;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.UserTrade;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import tide.trader.bot.domain.ImportedTicker;
import tide.trader.bot.dto.market.TickerDTO;
import tide.trader.bot.dto.util.CurrencyAmountDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * Ticker mapper.
 */
@Mapper(uses = {CurrencyMapper.class})
public interface TickerMapper {

    // =================================================================================================================
    // XChange to DTO.

    @Mapping(source = "instrument", target = "currencyPair")
    TickerDTO mapToTickerDTO(Ticker source);

    // =================================================================================================================
    // Domain to DTO.

    TickerDTO mapToTickerDTO(ImportedTicker source);


    @Mapping(source = "openTime", target = "timestamp", qualifiedByName = "mapOpenTimeToTimestamp")
    TickerDTO mapToTickerDTO(Kline kline);

    @Named("mapOpenTimeToTimestamp")
    default ZonedDateTime mapOpenTimeToTimestamp(long openTime) {
        return ZonedDateTime.ofInstant((new Date(openTime)).toInstant(), ZoneId.systemDefault());
    }
}
