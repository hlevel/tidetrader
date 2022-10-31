package tide.trader.bot.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class MartingerParametersDTO {

    private Integer interval;
    private Boolean trend;
    private String currency;
    private List<BigDecimal> positive;
    private List<BigDecimal> loss;
    private BigDecimal lossAdjust;
    private BigDecimal profit;
    private BigDecimal profitAdjust;

}
