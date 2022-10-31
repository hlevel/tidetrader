package tide.trader.bot.util;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class EnumLookup<T extends Enum<T>> {

    private final Map<String, T> map = new HashMap<>();
    private final String enumName;

    public EnumLookup(Class<T> clazz) {
        enumName = clazz.getName();
        for (T item : EnumSet.allOf(clazz)) {
            map.put(item.toString(), item);
        }
    }

    public T lookup(String name) {
        if (!map.containsKey(name)) {
            log.error("[Enum] Cannot found " + name + " in Enum " + enumName);
            return null;
        }
        return map.get(name);
    }
}
