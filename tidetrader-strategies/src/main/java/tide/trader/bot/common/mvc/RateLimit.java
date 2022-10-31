package tide.trader.bot.common.mvc;

public class RateLimit {

    /**
     * 频率限制，若为5000，则在下个5000毫秒内，达到次数返回true
     */
    private final long limit;
    private final long rate;

    private long last;
    private long number;

    public RateLimit(long limit, long rate) {
        this.limit = limit;
        this.rate = rate;
        this.last = 0;
        this.number = 0;
    }

    /**
     * limit内不会再次返回true
     * @param update 是否更新
     * @return 是否到了下一次
     */
    public boolean isLimit(boolean update) {
        if (limit <= 0) {
            return true;
        }

        long now = System.currentTimeMillis();
        boolean result = last + limit < now;
        if (update && result) {
            this.last = now;
            this.number = 1;
            return false;
        }
        number ++;
        return !result && number > rate;
    }

    /**
     * limit内不会再次返回true
     * @return 是否到了下一次
     */
    public boolean isLimit() {
        return this.isLimit(true);
    }

    @Override
    public String toString() {
        return "RateLimit{" +
                "limit=" + limit +
                "rate=" + rate +
                '}';
    }

}
