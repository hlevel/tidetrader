package tide.trader.bot.dto;

import lombok.Data;
import lombok.ToString;
import lombok.Value;
import tide.trader.bot.domain.Signal;
import tide.trader.bot.dto.strategy.StrategyDTO;
import tide.trader.bot.dto.trade.SignalDTO;

import java.math.BigDecimal;

/**
 * The signal sent by TV's webhook
 * {
 * name:"MACDLONGStrategy",
 * price:{{strategy.order.price}},
 * type:{{strategy.order.action}},
 * side:{{strategy.market_position}}
 * time:{{time}}
 * secret:"a"
 * }
 */
@Value
@ToString
public class TvInstructDTO {

    private String name;
    private BigDecimal price;
    private String type;
    private String side;
    private String time;
    private String secret;

}
