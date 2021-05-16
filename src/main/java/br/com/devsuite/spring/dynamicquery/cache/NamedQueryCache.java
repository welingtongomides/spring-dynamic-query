package br.com.devsuite.spring.dynamicquery.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.metamodel.EntityType;

/**
 * Cache para named queries
 * 
 * @author Welington Gomides - welingtong@gmail.com
 *
 */
public final class NamedQueryCache {
	private static final Map<String, String> mapQueryCache = new HashMap<>();

	private static NamedQueryCache instance;

	private NamedQueryCache() {
		super();
	}

	public static NamedQueryCache get() {
		if (instance == null) {
			instance = new NamedQueryCache();
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
				NamedQueries namedQueries = entityClass.getAnnotation(NamedQueries.class);
				if (namedQueries != null) {
					for (NamedQuery namedQuery : namedQueries.value()) {
						mapQueryCache.put(namedQuery.name(), namedQuery.query());
					}
				}
				NamedQuery namedQuery = entityClass.getAnnotation(NamedQuery.class);
				if (namedQuery != null) {
					mapQueryCache.put(namedQuery.name(), namedQuery.query());
				}
			}
		}
	}

}
