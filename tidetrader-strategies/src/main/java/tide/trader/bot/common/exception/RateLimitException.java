package tide.trader.bot.common.exception;

public class RateLimitException extends RuntimeException{

    public RateLimitException(String message) {
        super(message);
    }
}
