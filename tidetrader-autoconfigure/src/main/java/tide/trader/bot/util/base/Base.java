package tide.trader.bot.util.base;

import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tide.trader.bot.util.mapper.AccountMapper;
import tide.trader.bot.util.mapper.CurrencyMapper;
import tide.trader.bot.util.mapper.OrderMapper;
import tide.trader.bot.util.mapper.PositionMapper;
import tide.trader.bot.util.mapper.StrategyMapper;
import tide.trader.bot.util.mapper.TickerMapper;
import tide.trader.bot.util.mapper.TradeMapper;
import tide.trader.bot.util.mapper.UtilMapper;

/**
 * Base.
 */
public abstract class Base {

    /** Logger. */
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    /** Type mapper. */
    protected static final UtilMapper UTIL_MAPPER = Mappers.getMapper(UtilMapper.class);

    /** Currency mapper. */
    protected static final CurrencyMapper CURRENCY_MAPPER = Mappers.getMapper(CurrencyMapper.class);

    /** Strategy mapper. */
    protected static final StrategyMapper STRATEGY_MAPPER = Mappers.getMapper(StrategyMapper.class);

    /** Account mapper. */
    protected static final AccountMapper ACCOUNT_MAPPER = Mappers.getMapper(AccountMapper.class);

    /** Ticker mapper. */
    protected static final TickerMapper TICKER_MAPPER = Mappers.getMapper(TickerMapper.class);

    /** Order mapper. */
    protected static final OrderMapper ORDER_MAPPER = Mappers.getMapper(OrderMapper.class);

    /** Trade mapper. */
    protected static final TradeMapper TRADE_MAPPER = Mappers.getMapper(TradeMapper.class);

    /** Position mapper. */
    protected static final PositionMapper POSITION_MAPPER = Mappers.getMapper(PositionMapper.class);

}
