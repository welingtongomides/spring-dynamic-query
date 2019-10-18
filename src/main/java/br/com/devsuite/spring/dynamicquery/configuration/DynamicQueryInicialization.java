package br.com.devsuite.spring.dynamicquery.configuration;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import br.com.devsuite.spring.dynamicquery.cache.FileNativeQueryCache;
import br.com.devsuite.spring.dynamicquery.cache.NamedNativeQueryCache;
import br.com.devsuite.spring.dynamicquery.cache.NamedQueryCache;

/**
 * Inicialização dos caches.
 * 
 * @author Welington Gomides - welingtong@gmail.com
 *
 */
@Configuration
@Component
public class DynamicQueryInicialization {

	private final EntityManager entityManager;
	private final AnnotationConfigServletWebServerApplicationContext resourceLoader;

	@Autowired
	public DynamicQueryInicialization(EntityManager entityManager,
			AnnotationConfigServletWebServerApplicationContext resourceLoader) {
		this.entityManager = entityManager;
		this.resourceLoader = resourceLoader;
	}

	@PostConstruct
	public void init() {
		NamedQueryCache.get().inicializeCache(entityManager);
		NamedNativeQueryCache.get().inicializeCache(entityManager);
		FileNativeQueryCache.get().inicializeCache(resourceLoader);
	}
}
