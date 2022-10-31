package tide.trader.bot.dto.util;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Builder
@Value
public class CurrencyPairMetaDataDTO {

    private CurrencyPairDTO currencyPair;
    private BigDecimal tradingFee;
    private Integer baseScale;
    private Integer priceScale;
    private BigDecimal minimumAmount;
    private BigDecimal counterMinimumAmount;

}
