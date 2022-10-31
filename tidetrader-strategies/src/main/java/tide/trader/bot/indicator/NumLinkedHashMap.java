package tide.trader.bot.indicator;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Limit size map
 * @param <K>
 * @param <V>
 */
public class NumLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

    private final int maximumSize;

    public NumLinkedHashMap(int maximumSize) {
        this.maximumSize = maximumSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > maximumSize;
    }

}
