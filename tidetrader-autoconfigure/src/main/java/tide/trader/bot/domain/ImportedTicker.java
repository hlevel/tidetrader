package tide.trader.bot.domain;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.Hibernate;
import tide.trader.bot.dto.util.CurrencyPairDTO;
import tide.trader.bot.configuration.DatabaseAutoConfiguration;
import tide.trader.bot.util.csv.EpochToZonedDateTime;
import tide.trader.bot.util.test.ExcludeFromCoverageGeneratedReport;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Imported tickers (map "IMPORTED_TICKERS" table).
 * Feature described here: https://trading-bot.cassandre.tech/learn/import-historical-data.html#overview
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "IMPORTED_TICKERS")
public class ImportedTicker {

    /** Technical ID. */
    @Id
    @Column(name = "UID")
    private Long uid;

    /** The currency-pair. */
    @CsvBindByName(column = "CURRENCY_PAIR")
    @Column(name = "CURRENCY_PAIR")
    private String currencyPair;

    /** The opening price is the first trade price that was recorded during the day’s trading. */
    @CsvBindByName(column = "OPEN")
    @Column(name = "OPEN", precision = DatabaseAutoConfiguration.PRECISION, scale = DatabaseAutoConfiguration.SCALE)
    private BigDecimal open;

    /** Last trade field is the price set during the last trade. */
    @CsvBindByName(column = "LAST")
    @Column(name = "LAST", precision = DatabaseAutoConfiguration.PRECISION, scale = DatabaseAutoConfiguration.SCALE)
    private BigDecimal last;

    /** The bid price shown represents the highest bid price. */
    @CsvBindByName(column = "BID")
    @Column(name = "BID", precision = DatabaseAutoConfiguration.PRECISION, scale = DatabaseAutoConfiguration.SCALE)
    private BigDecimal bid;

    /** The ask price shown represents the lowest bid price. */
    @CsvBindByName(column = "ASK")
    @Column(name = "ASK", precision = DatabaseAutoConfiguration.PRECISION, scale = DatabaseAutoConfiguration.SCALE)
    private BigDecimal ask;

    /** The day’s high price. */
    @CsvBindByName(column = "HIGH")
    @Column(name = "HIGH", precision = DatabaseAutoConfiguration.PRECISION, scale = DatabaseAutoConfiguration.SCALE)
    private BigDecimal high;

    /** The day’s low price. */
    @CsvBindByName(column = "LOW")
    @Column(name = "LOW", precision = DatabaseAutoConfiguration.PRECISION, scale = DatabaseAutoConfiguration.SCALE)
    private BigDecimal low;

    /** Volume-weighted average price (VWAP) is the ratio of the value traded to total volume traded over a particular time horizon (usually one day). */
    @CsvBindByName(column = "VWAP")
    @Column(name = "VWAP", precision = DatabaseAutoConfiguration.PRECISION, scale = DatabaseAutoConfiguration.SCALE)
    private BigDecimal vwap;

    /** Volume is the number of shares or contracts traded. */
    @CsvBindByName(column = "VOLUME")
    @Column(name = "VOLUME", precision = DatabaseAutoConfiguration.PRECISION, scale = DatabaseAutoConfiguration.SCALE)
    private BigDecimal volume;

    /** Quote volume. */
    @CsvBindByName(column = "QUOTE_VOLUME")
    @Column(name = "QUOTE_VOLUME", precision = DatabaseAutoConfiguration.PRECISION, scale = DatabaseAutoConfiguration.SCALE)
    private BigDecimal quoteVolume;

    /** The bid size represents the quantity of a security that investors are willing to purchase at a specified bid price. */
    @CsvBindByName(column = "BID_SIZE")
    @Column(name = "BID_SIZE", precision = DatabaseAutoConfiguration.PRECISION, scale = DatabaseAutoConfiguration.SCALE)
    private BigDecimal bidSize;

    /** The ask size represents the quantity of a security that investors are willing to sell at a specified selling price. */
    @CsvBindByName(column = "ASK_SIZE")
    @Column(name = "ASK_SIZE", precision = DatabaseAutoConfiguration.PRECISION, scale = DatabaseAutoConfiguration.SCALE)
    private BigDecimal askSize;

    /** Information timestamp. */
    @CsvCustomBindByName(column = "TIMESTAMP", converter = EpochToZonedDateTime.class)
    @Column(name = "TIMESTAMP")
    private ZonedDateTime timestamp;

    /**
     * Returns currency pair DTO.
     *
     * @return currency pair DTO
     */
    public CurrencyPairDTO getCurrencyPairDTO() {
        if (currencyPair != null) {
            return new CurrencyPairDTO(currencyPair);
        } else {
            return null;
        }
    }

    @Override
    @ExcludeFromCoverageGeneratedReport
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        ImportedTicker that = (ImportedTicker) o;
        return Objects.equals(uid, that.uid);
    }

    @Override
    @ExcludeFromCoverageGeneratedReport
    public final int hashCode() {
        return new HashCodeBuilder()
                .append(uid)
                .toHashCode();
    }

}
