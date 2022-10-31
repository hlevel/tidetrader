package tide.trader.bot;

import org.springframework.context.ApplicationContext;
import tide.trader.bot.dto.account.AccountDTO;
import tide.trader.bot.dto.position.PositionStatusDTO;
import tide.trader.bot.dto.position.PositionTypeDTO;
import tide.trader.bot.dto.util.CurrencyDTO;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.dto.util.GainDTO;
import tide.trader.bot.strategy.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import tide.trader.bot.test.mock.TickerFluxMock;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Map;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Simple strategy test.
 */
@SpringBootTest
@Import(TickerFluxMock.class)
@DisplayName("SimulatedStrategyTest")
public class SimulatedStrategyTest {

	@Autowired
	private TickerFluxMock tickerFluxMock;
	@Autowired
	private ApplicationContext context;

	@Test
	@DisplayName("ExampleTa4jStrategy")
	public void ExampleTa4jStrategyTest() {
		ExampleTa4jStrategy strategy = context.getBean(ExampleTa4jStrategy.class);
		await().forever().until(() -> tickerFluxMock.isFluxDone(strategy.getRequestedCurrencyPairs().stream().limit(1).findFirst().get()));

		final Map<CurrencyDTO, GainDTO> gains = strategy.getGains();

		AccountDTO account = strategy.getTradeAccount().get();
		System.out.println("Account balances:");
		account.getBalances().forEach(balanceDTO -> System.out.println(" - " + balanceDTO.getCurrency() + " : " + balanceDTO.getTotal()));

		System.out.println("Account openPositions:");
		account.getOpenPositions().forEach(openPosition -> System.out.println(" - " + openPosition));

		System.out.println("Cumulated gains:");
		gains.forEach((currency, gain) -> System.out.println(" - " + currency + " : " + gain.getAmount()));

		System.out.println("Position closed:");
		strategy.getPositions()
				.values()
				.stream()
				.filter(p -> p.getStatus().equals(PositionStatusDTO.CLOSED))
				.forEach(p -> System.out.println(" - " + p.getDescription() + "  \n  + " + p.getOpeningOrder().getDescription() + "  \n  + " + p.getClosingOrder().getDescription()));

		//Volatility Oscillator
		//Normalized MACD
		System.out.println("Position not closed:");
		strategy.getPositions()
				.values()
				.stream()
				.filter(p -> !p.getStatus().equals(PositionStatusDTO.CLOSED))
				.forEach(p -> System.out.println(" - " + p.getDescription() + "  \n  + " + p.getOpeningOrder().getDescription() + "  \n  + " + p.getLatestGainPrice()));


		DecimalFormat decimalFormat = new DecimalFormat("###.##");

		long positive = strategy.getPositions().values().stream().filter(p -> p.getGain().getPercentage() > 0).count();
		long negative = strategy.getPositions().values().stream().filter(p -> p.getGain().getPercentage() <= 0).count();
		double positivePct = ((double) positive/(positive+negative)) * 100;
		double negativePct = ((double) negative/(positive+negative)) * 100;

		System.out.println("Cumulated probability:");
		System.out.println("  Positive: " + positive + " trades, pct: " + decimalFormat.format(positivePct) + "%");
		System.out.println("  Negative: " + negative + " trades, pct: " + decimalFormat.format(negativePct) + "%");

		long orderPositive = strategy.getPositions().values().stream().filter(p -> p.getType() == PositionTypeDTO.LONG).count();
		long orderNegative = strategy.getPositions().values().stream().filter(p -> p.getType() == PositionTypeDTO.SHORT).count();
		double orderPositivePct = ((double) orderPositive/(orderPositive+orderNegative)) * 100;
		double orderNegativePct = ((double) orderNegative/(orderPositive+orderNegative)) * 100;

		System.out.println("Side probability:");
		System.out.println("  Long: " + orderPositive + " trades, pct: " + decimalFormat.format(orderPositivePct) + "%");
		System.out.println("  Short: " + orderNegative + " trades, pct: " + decimalFormat.format(orderNegativePct) + "%");
		// Waiting to see if the strategy received the accounts update.
		await().untilAsserted(() -> assertEquals(strategy.getAccounts().size(), 2));
	}


}
