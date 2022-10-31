package tide.trader.bot.indicator.bollinger;

import org.ta4j.core.Bar;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.Num;
import tide.trader.bot.indicator.NumLinkedHashMap;
import tide.trader.bot.util.DateTimeUtil;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Squeeze + kdj
 */
public class SqueezeKDJLongIndicator extends CachedIndicator<Num> {

    private final Num oversold = this.numOf(20);
    private final Num overbought = this.numOf(80);

    private final BandKeltnerSqueezeIndicator bandKeltnerSqueeze;
    private final KDJIndicator kdj;

    private final Map<Integer, Long> squeezes = new LinkedHashMap<>();

    private long periodEpochSecond = 0;
    private boolean prepare = false;

    public SqueezeKDJLongIndicator(BandKeltnerSqueezeIndicator bandKeltnerSqueeze, KDJIndicator kdj) {
        super(bandKeltnerSqueeze);
        this.bandKeltnerSqueeze = bandKeltnerSqueeze;
        this.kdj = kdj;
    }

    @Override
    protected Num calculate(int index) {
        if(this.getBarSeries().getBarCount() < 2) {
            return this.numOf(0);
        }
        Bar prevbar = this.getBarSeries().getBar(index - 1);
        Bar currbar = this.getBarSeries().getBar(index);
        long prevEpochSecond = prevbar.getEndTime().toEpochSecond();
        long currEpochSecond = currbar.getEndTime().toEpochSecond();

        if((currEpochSecond - prevEpochSecond) != periodEpochSecond) {
            periodEpochSecond = currEpochSecond - prevEpochSecond;
        }
        Num prevJ = this.kdj.getJ(index-1);
        Num currJ = this.kdj.getJ(index);
        //挤压
        if(this.bandKeltnerSqueeze.getValue(index).isEqual(this.numOf(1))) {
            squeezes.putIfAbsent((index), currEpochSecond);

            //连续挤压逻辑
            if(squeezes.size() > 1) {
                List<Long> squeezeList = squeezes.values().stream().collect(Collectors.toList());
                long prve = squeezeList.get(squeezeList.size() - 2);
                long curr = squeezeList.get(squeezeList.size() - 1);
                //非连续性清理
                if((prve + periodEpochSecond) != curr) {
                    squeezes.clear();
                    squeezes.putIfAbsent((index), currEpochSecond);
                    prepare = false;
                }
            }

            //符合挤压并且符合溢出标准 或者 已经开始记录再继续记录
            if((bandKeltnerSqueeze.isUnderBand2SDLower(index) && currJ.isLessThan(oversold))) {
                prepare = true;
            }
        }
        //if(currEpochSecond > 1666350000) {

        //System.out.println(currbar.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "," + currEpochSecond + ",long,prepare=" + prepare + ",prevJ=" + prevJ + ",currJ=" + currJ + ",reuslt=" + (prepare && currbar.getClosePrice().isGreaterThan(currbar.getOpenPrice()) && (prevJ.isLessThan(oversold) && currJ.isGreaterThan(oversold))));
        //}

        //收价大于开价=阳线 前线在20以下,当前上穿20第一根线
        if(prepare && currbar.getClosePrice().isGreaterThan(currbar.getOpenPrice()) && (prevJ.isLessThan(oversold) && currJ.isGreaterThan(oversold))) {
            //if((prevJ.isLessThan(oversold) && currJ.isGreaterThan(oversold)) || (this.kdj.getJ(index-2).isLessThan(oversold) && currJ.isGreaterThan(oversold))) {
            return this.numOf(1);
            //}
        }

        //前线在80以上,当前下穿80第一根线
        if(prevJ.isGreaterThan(overbought) && currJ.isLessThan(overbought)) {
            return this.numOf(-1);
        }

        return this.numOf(0);
    }

}
