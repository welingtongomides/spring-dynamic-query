package br.com.devsuite.spring.dynamicquery.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.metamodel.EntityType;

/**
 * Cache para native named queries
 * 
 * @author Welington Gomides - welingtong@gmail.com
 * 
 * 
 *
 */
public final class NamedNativeQueryCache {
	private static final Map<String, String> mapQueryCache = new HashMap<>();

	private static NamedNativeQueryCache instance;

	private NamedNativeQueryCache() {
		super();
	}

	public static NamedNativeQueryCache get() {
		if (instance == null) {
			instance = new NamedNativeQueryCache();
		}
		return instance;
	}

	public Boolean containsKey(String namedQueryName) {
		return mapQueryCache.containsKey(namedQueryName);
	}

	public Optional<String> getQuery(String namedQueryName) {
		if (mapQueryCache.containsKey(namedQueryName)) {
			return Optional.of(mapQueryCache.get(namedQueryName));
		}
		return Optional.empty();
	}

	public void inicializeCache(EntityManager entityManager) {
		if (mapQueryCache.isEmpty()) {
			Set<EntityType<?>> entityTypes = entityManager.getMetamodel().getEntities();
			for (EntityType<?> entityType : entityTypes) {
				Class<?> entityClass = entityType.getBindableJavaType();
				NamedNativeQueries namedQueries = entityClass.getAnnotation(NamedNativeQueries.class);
				if (namedQueries != null) {
					for (NamedNativeQuery namedQuery : namedQueries.value()) {
						mapQueryCache.put(namedQuery.name(), namedQuery.query());
					}
				}
				NamedNativeQuery namedQuery = entityClass.getAnnotation(NamedNativeQuery.class);
				if (namedQuery != null) {
					mapQueryCache.put(namedQuery.name(), namedQuery.query());
				}
			}
		}
	}

}
