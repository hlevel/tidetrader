package tide.trader.bot.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tide.trader.bot.domain.BacktestingTicker;
import tide.trader.bot.dto.market.TickerDTO;

/**
 * Backtesting ticker mapper.
 */
@Mapper(uses = CurrencyMapper.class)
public interface BacktestingTickerMapper {

    // =================================================================================================================
    // TickerDTO to BacktestingTicker.
    @Mapping(target = "id", ignore = true)
    BacktestingTicker mapToBacktestingTicker(TickerDTO source);

    // =================================================================================================================
    // TickerDTO to BacktestingTicker.
    @Mapping(target = "currencyPair", source = "id.currencyPair")
    TickerDTO mapToTickerDTO(BacktestingTicker source);

}
