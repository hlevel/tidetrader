package tide.trader.bot.dto.position;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;


@Getter
@Builder
public class OpenPositionResultDTO {

    private OpenPositionDTO openPosition;
    private BigDecimal quoteBalance;

    /**
     * Getter successful.
     *
     * @return successful
     */
    public boolean isSuccessful() {
        return openPosition != null || (openPosition == null && quoteBalance != null && quoteBalance.compareTo(BigDecimal.ZERO) == 0);
    }

    @Override
    public String toString() {
        return "OpenPositionResultDTO{"
                + " quoteBalance=" + quoteBalance
                + " openPosition=" + openPosition
                + "}";
    }

}
