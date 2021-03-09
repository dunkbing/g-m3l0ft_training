package APP_PACKAGE.iab.helper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ConverterFactory {
	public static final String GSON = "gson";
	public static final String JSON_SIMPLE = "json";

	private static final Map<String, Converter> converterMap;

	static {
		Map<String, Converter> map = new HashMap<String, Converter>();
		map.put(GSON, new GsonConverter());
		map.put(JSON_SIMPLE, new JsonConverter());
		converterMap = Collections.unmodifiableMap(map);
	}

	public static Converter getConverter() {
	    return getConverter(GSON); // default
	}
	
	public static Converter getConverter(String type) {
		return (Converter) ConverterFactory.converterMap.get(type);
	}
}
