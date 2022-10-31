package tide.trader.bot.util.exception;

public final class IllegalConfigurationException extends RuntimeException {
    private static final long serialVersionUID = 8697369823358345165L;

    /**
     * Constructs a new {@code IllegalConfigurationException} exception with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause of the exception.
     */
    public IllegalConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code IllegalConfigurationException} exception with the specified detail message, and no cause.
     *
     * @param message the detail message.
     */
    public IllegalConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code IllegalConfigurationException} exception with the specified cause of error.
     *
     * @param cause the cause of the exception.
     */
    public IllegalConfigurationException(Throwable cause) {
        super(cause);
    }
}
