package org.knowm.xchange.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.utils.Assert;
import org.knowm.xchange.utils.DateUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Objects;

/**
 * A class encapsulating the information a "Ticker" can contain. Some fields can be empty if not
 * provided by the exchange.
 *
 * <p>A ticker contains data representing the latest trade.
 */
@JsonDeserialize(builder = Ticker.Builder.class)
public class Kline {

    private final CurrencyPair currencyPair;
    private final BigDecimal open;
    private final BigDecimal last;
    private final BigDecimal high;
    private final BigDecimal low;
    private final BigDecimal volume;
    private final BigDecimal quoteVolume;
    private final long openTime;
    private final long lastTime;
    private final long numberOfTrades;

    private final BigDecimal takerBuyBaseAssetVolume;
    private final BigDecimal takerBuyQuoteAssetVolume;

    /**
     * Constructor
     *
     * @param currencyPair The tradable identifier (e.g. BTC in BTC/USD)
     * @param last Last price
     * @param high High price
     * @param low Low price
     * @param volume volume in base currency
     * @param quoteVolume  volume in counter currency
     * @param lastTime - the timestamp of the ticker according to the exchange's server, null if not
     *     provided
     * @param numberOfTrades The instantaneous size at the bid price
     * @param takerBuyBaseAssetVolume The instantaneous size at the ask price
     * @param takerBuyQuoteAssetVolume Price percentage change. Is compared against the last price value. Will
     *     be null if not provided and cannot be calculated. Should be represented as percentage (e.g.
     *     0.5 equal 0.5%, 1 equal 1%, 50 equal 50%, 100 equal 100%)
     */
    private Kline(
            CurrencyPair currencyPair,
            BigDecimal open,
            BigDecimal last,
            BigDecimal high,
            BigDecimal low,
            BigDecimal volume,
            BigDecimal quoteVolume,
            long openTime,
            long lastTime,
            long numberOfTrades,
            BigDecimal takerBuyBaseAssetVolume,
            BigDecimal takerBuyQuoteAssetVolume) {
        this.currencyPair = currencyPair;
        this.open = open;
        this.last = last;
        this.high = high;
        this.low = low;
        this.volume = volume;
        this.quoteVolume = quoteVolume;
        this.openTime = openTime;
        this.lastTime = lastTime;
        this.numberOfTrades = numberOfTrades;
        this.takerBuyBaseAssetVolume = takerBuyBaseAssetVolume;
        this.takerBuyQuoteAssetVolume = takerBuyQuoteAssetVolume;
    }

    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }

    public BigDecimal getOpen() {

        return open;
    }

    public BigDecimal getLast() {

        return last;
    }

    public BigDecimal getHigh() {

        return high;
    }

    public BigDecimal getLow() {

        return low;
    }

    public long getOpenTime() {

        return openTime;
    }
    public long getLastTime() {

        return lastTime;
    }

    public BigDecimal getVolume() {
        if (volume == null && quoteVolume != null && last != null && !last.equals(BigDecimal.ZERO)) {
            return quoteVolume.divide(last, RoundingMode.HALF_UP);
        }

        return volume;
    }

    public BigDecimal getQuoteVolume() {
        if (quoteVolume == null && volume != null && last != null) {
            return volume.multiply(last);
        }
        return quoteVolume;
    }


    public long getNumberOfTrades() {
        return numberOfTrades;
    }

    public BigDecimal getTakerBuyBaseAssetVolume() {
        return takerBuyBaseAssetVolume;
    }

    public BigDecimal getTakerBuyQuoteAssetVolume() {
        return takerBuyQuoteAssetVolume;
    }

    @Override
    public String toString() {

        return "Kline [currencyPair="
                + currencyPair
                + ", open="
                + open
                + ", last="
                + last
                + ", high="
                + high
                + ", low="
                + low
                + ", volume="
                + volume
                + ", quoteVolume="
                + quoteVolume
                + ", openTime="
                + openTime
                + ", lastTime="
                + lastTime
                + ", numberOfTrades="
                + numberOfTrades
                + ", takerBuyBaseAssetVolume="
                + takerBuyBaseAssetVolume
                + ", takerBuyQuoteAssetVolume="
                + takerBuyQuoteAssetVolume
                + "]";
    }

    /**
     * Builder to provide the following to {@link Ticker}:
     *
     * <ul>
     *   <li>Provision of fluent chained construction interface
     * </ul>
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

        private CurrencyPair currencyPair;
        private BigDecimal open;
        private BigDecimal last;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal volume;
        private BigDecimal quoteVolume;
        private long openTime;
        private long lastTime;
        private long numberOfTrades;

        private BigDecimal takerBuyBaseAssetVolume;
        private BigDecimal takerBuyQuoteAssetVolume;

        // Prevent repeat builds
        private boolean isBuilt = false;

        public Kline build() {

            validateState();

            Kline kline =
                    new Kline(
                            currencyPair,
                            open,
                            last,
                            high,
                            low,
                            volume,
                            quoteVolume,
                            openTime,
                            lastTime,
                            numberOfTrades,
                            takerBuyBaseAssetVolume,
                            takerBuyQuoteAssetVolume);

            isBuilt = true;

            return kline;
        }

        public Kline.Builder from(CurrencyPair currencyPair, Object[] obj) {
            this.currencyPair = currencyPair;
            //this.interval = interval;
            this.openTime = Long.valueOf(obj[0].toString());
            this.open = new BigDecimal(obj[1].toString());
            this.high = new BigDecimal(obj[2].toString());
            this.low = new BigDecimal(obj[3].toString());
            this.last = new BigDecimal(obj[4].toString());
            this.volume = new BigDecimal(obj[5].toString());
            this.lastTime = Long.valueOf(obj[6].toString());
            this.quoteVolume = new BigDecimal(obj[7].toString());
            this.numberOfTrades = Long.valueOf(obj[8].toString());
            this.takerBuyBaseAssetVolume = new BigDecimal(obj[9].toString());
            this.takerBuyQuoteAssetVolume = new BigDecimal(obj[10].toString());
            return this;
        }

        private void validateState() {

            if (isBuilt) {
                throw new IllegalStateException("The entity has been built");
            }
        }


        /** Use {@link #currencyPair */
        public Kline.Builder currencyPair(CurrencyPair currencyPair) {
            this.currencyPair = currencyPair;
            return this;
        }

        public Kline.Builder open(BigDecimal open) {

            this.open = open;
            return this;
        }

        public Kline.Builder last(BigDecimal last) {

            this.last = last;
            return this;
        }

        public Kline.Builder high(BigDecimal high) {

            this.high = high;
            return this;
        }

        public Kline.Builder low(BigDecimal low) {

            this.low = low;
            return this;
        }


        public Kline.Builder volume(BigDecimal volume) {

            this.volume = volume;
            return this;
        }

        public Kline.Builder quoteVolume(BigDecimal quoteVolume) {

            this.quoteVolume = quoteVolume;
            return this;
        }

        public Kline.Builder openTime(long openTime) {

            this.openTime = openTime;
            return this;
        }
        public Kline.Builder lastTime(long lastTime) {

            this.lastTime = lastTime;
            return this;
        }

        public Kline.Builder numberOfTrades(long numberOfTrades) {
            this.numberOfTrades = numberOfTrades;
            return this;
        }

        public Kline.Builder takerBuyBaseAssetVolume(BigDecimal takerBuyBaseAssetVolume) {
            this.takerBuyBaseAssetVolume = takerBuyBaseAssetVolume;
            return this;
        }

        public Kline.Builder takerBuyQuoteAssetVolume(BigDecimal takerBuyQuoteAssetVolume) {
            this.takerBuyQuoteAssetVolume = takerBuyQuoteAssetVolume;
            return this;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kline kline = (Kline) o;
        return Objects.equals(getCurrencyPair(), kline.getCurrencyPair())
                && Objects.equals(getOpen(), kline.getOpen())
                && Objects.equals(getLast(), kline.getLast())
                && Objects.equals(getHigh(), kline.getHigh())
                && Objects.equals(getLow(), kline.getLow())
                && Objects.equals(getVolume(), kline.getVolume())
                && Objects.equals(getQuoteVolume(), kline.getQuoteVolume())
                && Objects.equals(getOpenTime(), kline.getOpenTime())
                && Objects.equals(getLastTime(), kline.getLastTime())
                && Objects.equals(getNumberOfTrades(), kline.getNumberOfTrades())
                && Objects.equals(getTakerBuyBaseAssetVolume(), kline.getTakerBuyBaseAssetVolume())
                && Objects.equals(getTakerBuyQuoteAssetVolume(), kline.getTakerBuyQuoteAssetVolume());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getCurrencyPair(),
                getOpen(),
                getLast(),
                getHigh(),
                getLow(),
                getVolume(),
                getQuoteVolume(),
                getOpenTime(),
                getLastTime(),
                getNumberOfTrades(),
                getTakerBuyBaseAssetVolume(),
                getTakerBuyQuoteAssetVolume());
    }
}
