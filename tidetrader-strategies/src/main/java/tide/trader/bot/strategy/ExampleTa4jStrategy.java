package tide.trader.bot.strategy;

import org.ta4j.core.Rule;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import tide.trader.bot.dto.account.AccountDTO;
import tide.trader.bot.dto.market.TickerDTO;
import tide.trader.bot.dto.position.*;
import tide.trader.bot.dto.trade.SideDTO;
import tide.trader.bot.dto.util.CurrencyDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.util.ta4j.BaseIndicatorRule;
import tide.trader.bot.util.ta4j.DurationMaximumBar;
import tide.trader.bot.util.ta4j.IndicatorRule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@CassandreStrategy(strategyId = "2", strategyName = "ExampleTa4jStrategy")
public class ExampleTa4jStrategy extends BasicTa4jCassandreStrategy {

    @Override
    public Set<DurationMaximumBar> getRequestedDurationMaximumBars() {
        //三个参数分别为 品种、周期、最大k线线数
        return Set.of(new DurationMaximumBar(new CurrencyPairDTO(CurrencyDTO.BTC, CurrencyDTO.USDT), Duration.ofDays(1), 30));
    }

    @Override
    public IndicatorRule getIndicatorRule(DurationMaximumBar bar) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(getSeries(bar));
        SMAIndicator sma = new SMAIndicator(closePrice, 10);

        Rule entryLongRule = new UnderIndicatorRule(sma, closePrice);
        Rule entryShortRule = new OverIndicatorRule(sma, closePrice);
        Rule exitLongRule = new OverIndicatorRule(sma, closePrice);
        Rule exitShortRule = new OverIndicatorRule(sma, closePrice);
        return new BaseIndicatorRule(entryLongRule, entryShortRule, exitLongRule, exitShortRule);
    }

    @Override
    public void shouldPosition(SideDTO side, TickerDTO durationTicker) {
        if(side == SideDTO.LONG || side == SideDTO.SHORT) {
            Optional<PositionDTO> position = this.getPositions(durationTicker.getCurrencyPair(), PositionStatusDTO.OPENING, PositionStatusDTO.OPENED).values().stream().findFirst();
            if(position.isPresent()) {
                PositionDTO p = position.get();
                //如果存在相同方向则只做一单
                if((p.getType() == PositionTypeDTO.LONG && side == SideDTO.LONG)
                        || (p.getType() == PositionTypeDTO.SHORT && side == SideDTO.SHORT)) {
                    return;
                }
                //反方向则平单
                this.closePosition(p.getPositionId(), p.getCurrencyPair(), "Reverse position");
            }

            BigDecimal quantity = new BigDecimal("500").divide(durationTicker.getLast(), 8, RoundingMode.DOWN);

            // Create rules.
            PositionRulesDTO rules = PositionRulesDTO.builder()
                    .stopGainPercentage(10f)
                    .stopLossPercentage(5f)
                    .build();
            PositionCreationResultDTO positionCreationResult = null;
            // Create position.
            if(side == SideDTO.LONG && this.canBuy(durationTicker.getCurrencyPair(), quantity)) {
                positionCreationResult = createLongPosition(durationTicker.getCurrencyPair(), quantity, rules);
            } else if (side == SideDTO.SHORT && this.canSell(durationTicker.getCurrencyPair(), quantity)) {
                positionCreationResult = createShortPosition(durationTicker.getCurrencyPair(), quantity, rules);
            }
            if(positionCreationResult != null && !positionCreationResult.isSuccessful()) {
                this.addMessage((durationTicker.getCurrencyPair() +" Order failed"), "Reason: " + positionCreationResult.getErrorMessage());
            }
        } else {
            //flat postion
            this.getPositions(durationTicker.getCurrencyPair(), PositionStatusDTO.OPENED).forEach((positionId, position) -> {
                if((side == SideDTO.EXITSHORT && position.getType() == PositionTypeDTO.SHORT)
                        || (side == SideDTO.EXITLONG && position.getType() == PositionTypeDTO.LONG)) {
                    this.closePosition(position.getPositionId(), position.getCurrencyPair(), "10 Day line exit");
                }
            });
        }
    }

    @Override
    public Optional<AccountDTO> getTradeAccount(Set<AccountDTO> accounts) {
        // From all the accounts we have on the exchange, we must return the one we use for trading.
        if (accounts.size() == 1) {
            // If there is only one on the exchange, we choose this one.
            return accounts.stream().findFirst();
        } else {
            // If there are several accounts on the exchange, we choose the one with the name "trade".
            return accounts.stream()
                    .filter(a -> "trade".equalsIgnoreCase(a.getName()))
                    .findFirst();
        }
    }
}
