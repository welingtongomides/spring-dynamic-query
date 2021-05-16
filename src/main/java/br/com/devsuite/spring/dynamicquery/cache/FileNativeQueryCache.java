
package br.com.devsuite.spring.dynamicquery.cache;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;

import br.com.devsuite.spring.dynamicquery.exception.DynamicQueryException;

/**
 * Cache de query nativas
 * 
 * @author Welington Gomides - welingtong@gmail.com
 *
 */
public final class FileNativeQueryCache {
	private static final Logger LOG = LoggerFactory.getLogger(FileNativeQueryCache.class.getName());

	private static final String CLASSPATH_QUERYS = "classpath*:/querys";

	private static final String SQL = ".sql";

	private static final Map<String, String> mapQueryCache = new HashMap<>();

	private static FileNativeQueryCache instance;

	private FileNativeQueryCache() {
		super();
	}

	public static FileNativeQueryCache get() {
		if (instance == null) {
			instance = new FileNativeQueryCache();
		}
		return instance;
	}

	public Boolean containsKey(String fileSource) {
		return mapQueryCache.containsKey(fileSource + SQL);
	}

	public Optional<String> getQuery(String fileSource) {
		fileSource += SQL;
		if (mapQueryCache.containsKey(fileSource)) {
			return Optional.of(mapQueryCache.get(fileSource));
		}
		return Optional.empty();
	}

	@SuppressWarnings("deprecation")
	public void inicializeCache(AnnotationConfigServletWebServerApplicationContext resourceLoader)
			throws DynamicQueryException {
		try {
			Arrays.stream(resourceLoader.getResources(String.format("%s/*%s", CLASSPATH_QUERYS, SQL))).parallel()
					.forEach(resource -> {
						try {
							mapQueryCache.put(resource.getFilename(), IOUtils.toString(resource.getInputStream()));
						} catch (IOException e) {
							LOG.error(e.getMessage(), e);
						}
					});
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}
}
