package tide.trader.bot.common.aspect;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import tide.trader.bot.common.exception.RateLimitException;
import tide.trader.bot.common.mvc.BaseController;

@ControllerAdvice(assignableTypes = {BaseController.class})
public class GlobalExceptionHandler {

    @ExceptionHandler(value = RateLimitException.class)
    public String rateExceptionHandler(Exception e) {
        return "limit";
    }

    @ExceptionHandler(value = Exception.class)
    public String exceptionHandler(Exception e) {
        return "error";
    }

}
