package tide.trader.bot.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public class JacksonUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        /*
         * 默认false：不解析含有注释符（即：true时解析含有注释的json字符串）<br>
         * 该特性，决定了解析器是否可以解析含有Java/C++注释样式(如：/*或//的注释符)<br>
         * 注意：标准的json字符串格式没有含有注释符（非标准），然而则经常使用<br>
         */
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        /*
         * 默认false：不解析含有结束语的字符<br> 该特性，决定了解析器是否可以解析该字符（结束语字段符，一般在js中出现）<br>
         * 注意：标准的json字符串格式没有含有注释符（非标准），然而则经常使用<br>
         */
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        /*
         * 默认false：不解析含有单引号的字符串或字符<br>
         * 该特性，决定了解析器是否可以解析单引号的字符串或字符(如：单引号的字符串，单引号'\'')<br>
         * 注意：可作为其他可接受的标记，但不是JSON的规范<br>
         */
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        /*
         * 允许：默认false_不解析含有结束语控制字符<br>
         * 该特性，决定了解析器是否可以解析结束语控制字符(如：ASCII<32，如包含tab或换行符)<br>
         * 注意：设置false（默认）时，若解析则抛出异常;若true时，则用引号即可转义<br>
         */
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        /*
         * 可解析反斜杠引用的所有字符，默认：false，不可解析
         */
        objectMapper.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
        /**
         * 按字母顺序排序
         */
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        /**
         * 忽略null
         */
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setLocale(Locale.CHINA);
        objectMapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);

        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);

        // 将locadatetime 按照yyyy-MM-dd HH:mm:ss打印
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        objectMapper.registerModule(javaTimeModule);
    }

    /**
     * 对象转JSON
     * @param object 对象
     * @return String
     */
    public static String toJSONString(Object object) {
        String json = "";
        if (null == object) {
            return json;
        }
        try {
            json = objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            //LOGGER.error("Java 转 JSON 出错！", e);
            e.printStackTrace();
        }
        return json;
    }

    /**
     * 对象转JSON
     * @param object 对象
     * @param pattern 字符串时间格式规则， 如 "yyyy-MM-dd HH:mm:ss"
     * @return String
     */
    public static String toJSONString(Object object, String pattern) {
        String json = "";
        if (null == object) {
            return json;
        }
        ObjectMapper mapper = objectMapper;
        try {
            DateFormat dateFormat = new SimpleDateFormat(pattern);
            mapper = mapper.setDateFormat(dateFormat);
            json = mapper.writeValueAsString(object);
        } catch (IOException e) {
            //LOGGER.error("Java 转 JSON 出错！", e);
            e.printStackTrace();
        }
        return json;
    }

    /**
     * 将json字符串转换为java对象
     * @param json json字符串
     * @param typeReference 复杂的java对象类型
     * @param <T>
     * @return
     */
    public static <T> T jsonToBean(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            //LOGGER.error("jsonToBeanByTypeReference", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * map 转JavaBean
     */
    public static <T> T map2bean(Map map, Class<T> clazz) {
        return objectMapper.convertValue(map, clazz);
    }

}
