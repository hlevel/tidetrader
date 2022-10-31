package tide.trader.bot.util.java;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.ZonedDateTime;

@RequiredArgsConstructor
@Getter
public class ZonedDateTimeBetween {

    private final ZonedDateTime start;
    private final ZonedDateTime end;

    /**
     * Add one to the total amount
     * @param duration
     * @param maximum
     * @return
     */
    public static ZonedDateTimeBetween ofDuration(Duration duration, long maximum) {
        ZonedDateTime now = ZonedDateTime.now();
        long remainderSecond = now.toEpochSecond() % duration.toSeconds();
        ZonedDateTime end = now.minusSeconds(remainderSecond).minus(duration);
        ZonedDateTime start = end.minusSeconds(duration.toSeconds() * maximum);
        return new ZonedDateTimeBetween(start, end);
    }

    public static ZonedDateTimeBetween ofStartDuration(Duration duration, ZonedDateTime start) {
        ZonedDateTime now = ZonedDateTime.now();
        long remainderSecond = now.toEpochSecond() % duration.toSeconds();
        ZonedDateTime end = now.minusSeconds(remainderSecond);
        long diffSecond = end.toEpochSecond() - start.toEpochSecond();
        if(diffSecond == 0)  {
            diffSecond = -1;
        }
        long durationNum = diffSecond / duration.toSeconds();
        if(durationNum == 0) {
            durationNum = 1;
        }
        return new ZonedDateTimeBetween(end.minusSeconds(duration.toSeconds() * durationNum), end);
    }

    public long getStartToSecond() {
        return start.toEpochSecond();
    }

    public long getEndToSecond() {
        return end.toEpochSecond();
    }

    public long getStartToMilli() {
        return getStartToSecond() * 1000;
    }

    public long getEndToMilli() {
        return getEndToSecond() * 1000;
    }

}
