package tech.geekcity.open.geek.tools.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.devtools.common.options.Converter;
import com.google.devtools.common.options.OptionsBase;
import com.google.devtools.common.options.OptionsParsingException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author ben.wangz
 */
public class BasicOption extends OptionsBase {
    public static final String NO_DEFAULT_VALUE = "null";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static boolean nullOrDefault(String value) {
        return null == value || NO_DEFAULT_VALUE.equals(value);
    }

    public String print() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
    }

    public static class IntegerConverter implements Converter<Integer> {

        @Override
        public Integer convert(String input) throws OptionsParsingException {
            return Integer.valueOf(input);
        }

        @Override
        public String getTypeDescription() {
            return null;
        }
    }

    public static class LongConverter implements Converter<Long> {

        @Override
        public Long convert(String input) throws OptionsParsingException {
            return Long.valueOf(input);
        }

        @Override
        public String getTypeDescription() {
            return null;
        }
    }

    public static class BooleanConverter implements Converter<Boolean> {

        @Override
        public Boolean convert(String input) throws OptionsParsingException {
            return Boolean.valueOf(input);
        }

        @Override
        public String getTypeDescription() {
            return null;
        }
    }

    public static class ArbitraryMapConverter implements Converter<Map<String, Object>> {

        @Override
        public Map<String, Object> convert(String input) throws OptionsParsingException {
            try {
                return OBJECT_MAPPER.readValue(input, new TypeReference<Map<String, Object>>() {
                });
            } catch (IOException e) {
                throw new OptionsParsingException(
                        String.format("exception caught while parsing map(%s): %s",
                                input, e.getMessage()),
                        e);
            }
        }

        @Override
        public String getTypeDescription() {
            return null;
        }
    }

    public static class MapConverter implements Converter<Map<String, String>> {

        @Override
        public Map<String, String> convert(String input) throws OptionsParsingException {
            try {
                return OBJECT_MAPPER.readValue(input, new TypeReference<Map<String, String>>() {
                });
            } catch (IOException e) {
                throw new OptionsParsingException(
                        String.format("exception caught while parsing map(%s): %s",
                                input, e.getMessage()),
                        e);
            }
        }

        @Override
        public String getTypeDescription() {
            return null;
        }
    }

    public static class ListConverter implements Converter<List<String>> {

        @Override
        public List<String> convert(String input) throws OptionsParsingException {
            try {
                return OBJECT_MAPPER.readValue(input, new TypeReference<List<String>>() {
                });
            } catch (IOException e) {
                throw new OptionsParsingException(
                        String.format("exception caught while parsing map(%s): %s",
                                input, e.getMessage()),
                        e);
            }
        }

        @Override
        public String getTypeDescription() {
            return null;
        }
    }

    public static Map<String, Object> readFileJsonToMap(String filePath) throws IOException {
        return OBJECT_MAPPER.readValue(
                FileUtils.readFileToString(new File(filePath)),
                new TypeReference<Map<String, Object>>() {
                });
    }
}
