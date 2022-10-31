package tide.trader.bot.dto.position;

import lombok.Getter;

/**
 * Position ShouldBeClose result for {@link PositionDTO}.
 */
@Getter
public final class PositionCloseResultDTO {

    private PositionDTO position;

    /** Position exit. */
    private boolean close;

    /**
     * Constructor for successful position creation.
     * @param close
     */
    public PositionCloseResultDTO(final PositionDTO position, final boolean close) {
        this(position, close, "");
    }

    /**
     * Constructor for unsuccessful position creation.
     * @param position
     * @param close
     * @param reason
     */
    public PositionCloseResultDTO(final PositionDTO position, final boolean close, final String reason) {
        this.position = position;
        this.close = close;
    }



    @Override
    public String toString() {
        return "PositionCloseResultDTO{"
                + " position='" + position + '\''
                + " close='" + close + '\''
                + '}';
    }

}
