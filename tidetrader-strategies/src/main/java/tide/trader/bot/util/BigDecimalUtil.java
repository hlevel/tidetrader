package tide.trader.bot.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Objects;

public class BigDecimalUtil {
    public static final BigDecimal ZERO;
    public static final BigDecimal ONE;
    public static final BigDecimal TWO;
    public static final BigDecimal TEN;
    public static final BigDecimal HUNDRED;
    public static final BigDecimal THOUSAND;
    public static final int QUOTE_DIGIT = 8;
    public static final RoundingMode QUOTE_MODE = RoundingMode.DOWN;


    public BigDecimalUtil() {
    }

    public static BigDecimal add(BigDecimal augend, BigDecimal addend) {
        Objects.requireNonNull(augend, "augend");
        Objects.requireNonNull(addend, "addend");
        return augend.add(addend).setScale(QUOTE_DIGIT, QUOTE_MODE);
    }

    public static BigDecimal sub(BigDecimal minuend, BigDecimal subtrahend) {
        Objects.requireNonNull(minuend, "minuend");
        Objects.requireNonNull(subtrahend, "subtrahend");
        return minuend.subtract(subtrahend).setScale(QUOTE_DIGIT, QUOTE_MODE);
    }

    public static BigDecimal mul(BigDecimal multiplicand, BigDecimal multiplier) {
        Objects.requireNonNull(multiplicand, "multiplicand");
        Objects.requireNonNull(multiplier, "multiplier");
        return multiplicand.multiply(multiplier).setScale(QUOTE_DIGIT, QUOTE_MODE);
    }

    public static BigDecimal div(BigDecimal dividend, BigDecimal divisor, int scale, RoundingMode roundingMode) {
        Objects.requireNonNull(dividend, "dividend");
        Objects.requireNonNull(divisor, "divisor");
        if(dividend.compareTo(ZERO) == 0 || divisor.compareTo(ZERO) == 0){
            return ZERO;
        }
        return dividend.divide(divisor, scale, roundingMode);
    }

    public static BigDecimal div(BigDecimal dividend, BigDecimal divisor, int scale) {
        return div(dividend, divisor, scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal div(BigDecimal dividend, BigDecimal divisor) {
        return div(dividend, divisor, QUOTE_DIGIT, RoundingMode.HALF_UP);
    }

    public static BigDecimal rem(BigDecimal dividend, BigDecimal divisor) {
        Objects.requireNonNull(dividend, "dividend");
        Objects.requireNonNull(divisor, "divisor");
        return dividend.remainder(divisor).setScale(QUOTE_DIGIT, QUOTE_MODE);
    }

    public static BigDecimal sum(BigDecimal... addend) {
        Objects.requireNonNull(addend, "addend");
        BigDecimal[] var1 = addend;
        int var2 = addend.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            BigDecimal a = var1[var3];
            Objects.requireNonNull(a, "addend");
        }

        return Arrays.stream(addend).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(QUOTE_DIGIT, QUOTE_MODE);
    }

    public static BigDecimal avg(int scale, RoundingMode roundingMode, BigDecimal... addend) {
        Objects.requireNonNull(addend, "addend");
        BigDecimal[] var3 = addend;
        int var4 = addend.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            BigDecimal a = var3[var5];
            Objects.requireNonNull(a, "addend");
        }

        if (0 == addend.length) {
            return ZERO;
        } else {
            BigDecimal sum = (BigDecimal)Arrays.stream(addend).reduce(BigDecimal.ZERO, BigDecimal::add);
            return sum.divide(BigDecimal.valueOf((long)addend.length), scale, roundingMode);
        }
    }

    public static BigDecimal avg(int scale, BigDecimal... addend) {
        return avg(scale, RoundingMode.HALF_UP, addend);
    }

    public static BigDecimal avg(BigDecimal... addend) {
        return avg(8, RoundingMode.HALF_UP, addend);
    }

    public static BigDecimal neg(BigDecimal number) {
        Objects.requireNonNull(number, "number");
        return number.negate();
    }

    public static BigDecimal abs(BigDecimal number) {
        Objects.requireNonNull(number, "number");
        return number.abs();
    }

    public static BigDecimal abs(BigDecimal number1, BigDecimal number2) {
        Objects.requireNonNull(number1, "number1");
        Objects.requireNonNull(number2, "number2");
        return number1.subtract(number2).abs();
    }

    private static void checkNumbers(BigDecimal[] numbers) {
        Objects.requireNonNull(numbers, "numbers");
        BigDecimal[] var1 = numbers;
        int var2 = numbers.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            BigDecimal n = var1[var3];
            Objects.requireNonNull(n, "numbers");
        }

        if (numbers.length == 0) {
            throw new RuntimeException("the length of numbers can not be 0");
        }
    }

    public static BigDecimal max(BigDecimal... numbers) {
        checkNumbers(numbers);
        return Arrays.stream(numbers).max(BigDecimal::compareTo).orElse(null);
    }

    public static BigDecimal min(BigDecimal... numbers) {
        checkNumbers(numbers);
        return Arrays.stream(numbers).min(BigDecimal::compareTo).orElse(null);
    }

    public static boolean positive(BigDecimal number) {
        Objects.requireNonNull(number, "number");
        return 0 < number.compareTo(ZERO);
    }

    public static boolean negative(BigDecimal number) {
        Objects.requireNonNull(number, "number");
        return number.compareTo(ZERO) < 0;
    }

    public static BigDecimal reciprocate(BigDecimal number, int scale, RoundingMode roundingMode) {
        Objects.requireNonNull(number, "number");
        if (0 == number.compareTo(ZERO)) {
            throw new RuntimeException("can not find the reciprocal of number: " + number);
        } else {
            return ONE.divide(number, scale, roundingMode);
        }
    }

    public static BigDecimal reciprocate(BigDecimal number, int scale) {
        return reciprocate(number, scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal reciprocate(BigDecimal number) {
        return reciprocate(number, 8, RoundingMode.HALF_UP);
    }

    public static BigDecimal minimum(int scale) {
        if (scale == 0) {
            return ONE;
        } else {
            StringBuilder sb = new StringBuilder("1");
            if (0 < scale) {
                while(0 < scale--) {
                    sb.insert(0, "0");
                }

                return new BigDecimal(sb.replace(0, 1, "0.").toString());
            } else {
                while(scale++ < 0) {
                    sb.append("0");
                }

                return new BigDecimal(sb.toString());
            }
        }
    }

    /**
     * 缩小百分比
     * @param number
     * @param scale
     * @return
     */
    public static BigDecimal reducePercentage(double number, int scale) {
        Objects.requireNonNull(number, "number");
        return reducePercentage(new BigDecimal(String.valueOf(number)), scale);
    }

    /**
     * 缩小百分比(保留2位小数)
     * @param number
     * @return
     */
    public static BigDecimal reducePercentage(double number) {
        return reducePercentage(number, 4);
    }

    /**
     * 缩小百分比(保留2位小数)
     * @param number
     * @return
     */
    public static BigDecimal reducePercentage(BigDecimal number) {
        Objects.requireNonNull(number, "number");
        return div(number, HUNDRED, 4);
    }

    /**
     * 缩小百分比
     * @param number
     * @param scale
     * @return
     */
    public static BigDecimal reducePercentage(BigDecimal number, int scale) {
        Objects.requireNonNull(number, "number");
        return div(number, HUNDRED, scale);
    }

    /**
     * 放大百分比 保留2位
     * @param number
     * @return
     */
    public static BigDecimal increasePercentage(double number) {
        return increasePercentage(number, 2);
    }

    /**
     * 放大百分比 保留2位
     * @param number
     * @return
     */
    public static BigDecimal increasePercentage(BigDecimal number) {
        return increasePercentage(number, 2);
    }

    /**
     * 放大百分比
     * @param number
     * @param scale 小数位数
     * @return
     */
    public static BigDecimal increasePercentage(double number, int scale) {
        Objects.requireNonNull(number, "number");
        return increasePercentage(new BigDecimal(String.valueOf(number)), scale);
    }

    /**
     * 放大百分比
     * @param number
     * @param scale 小数位数
     * @return
     */
    public static BigDecimal increasePercentage(BigDecimal number, int scale) {
        Objects.requireNonNull(number, "number");
        return mul(number, HUNDRED).setScale(scale, RoundingMode.HALF_DOWN).stripTrailingZeros();
    }



    public static int scale(BigDecimal number) {
        Objects.requireNonNull(number, "number");
        return number.scale();
    }

    public static boolean greater(BigDecimal number1, BigDecimal number2) {
        Objects.requireNonNull(number1, "number1");
        Objects.requireNonNull(number2, "number2");
        return 0 < number1.compareTo(number2);
    }

    /**
     * number1 >= number2
     * @param number1
     * @param number2
     * @return
     */
    public static boolean greaterOrEqual(BigDecimal number1, BigDecimal number2) {
        Objects.requireNonNull(number1, "number1");
        Objects.requireNonNull(number2, "number2");
        return 0 <= number1.compareTo(number2);
    }

    public static boolean less(BigDecimal number1, BigDecimal number2) {
        Objects.requireNonNull(number1, "number1");
        Objects.requireNonNull(number2, "number2");
        return number1.compareTo(number2) < 0;
    }

    /**
     * number1 <= number2
     * @param number1
     * @param number2
     * @return
     */
    public static boolean lessOrEqual(BigDecimal number1, BigDecimal number2) {
        Objects.requireNonNull(number1, "number1");
        Objects.requireNonNull(number2, "number2");
        return number1.compareTo(number2) <= 0;
    }

    public static boolean equal(BigDecimal number1, BigDecimal number2) {
        Objects.requireNonNull(number1, "number1");
        Objects.requireNonNull(number2, "number2");
        return 0 == number1.compareTo(number2);
    }

    static {
        ZERO = BigDecimal.ZERO;
        ONE = BigDecimal.ONE;
        TWO = new BigDecimal("2");
        TEN = BigDecimal.TEN;
        HUNDRED = new BigDecimal("100");
        THOUSAND = new BigDecimal("1000");
    }
}

