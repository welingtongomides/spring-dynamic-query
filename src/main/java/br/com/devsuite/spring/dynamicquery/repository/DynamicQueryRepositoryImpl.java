package br.com.devsuite.spring.dynamicquery.repository;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import br.com.devsuite.spring.dynamicquery.DynamicQuery;
import br.com.devsuite.spring.dynamicquery.cache.NamedQueryCache;
import br.com.devsuite.spring.dynamicquery.exception.DynamicQueryException;
import br.com.devsuite.spring.dynamicquery.parameters.DynamicParameters;

/**
 * 
 * @author Welington Gomides - welingtong@gmail.com
 *
 */
@SuppressWarnings("unchecked")
@Repository
public class DynamicQueryRepositoryImpl implements DynamicQueryRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public <T> List<T> find(String namedQuery, DynamicParameters parameters, Class<T> entityClass) {
		DynamicQuery dynamicQuery = new DynamicQuery(entityManager, getQuery(namedQuery),
				parameters.getMapParameters());
		return dynamicQuery.getQuery().getResultList();
	}

	@Override
	public <T> Page<T> find(String namedQuery, DynamicParameters parameters, Pageable pageable, Class<T> entityClass) {
		String query = getQuery(namedQuery);
		DynamicQuery dynamicQuery = new DynamicQuery(entityManager, query, parameters.getMapParameters());
		return new PageImpl<>(dynamicQuery.getPaginationQuery(pageable).getResultList(), pageable,
				(Long) dynamicQuery.getCountQuery().getSingleResult());
	}

	@Override
	public <T> T findOne(String namedQuery, DynamicParameters parameters, Class<T> entityClass) {
		String query = getQuery(namedQuery);
		DynamicQuery dynamicQuery = new DynamicQuery(entityManager, query, parameters.getMapParameters());
		return (T) dynamicQuery.getQuery().getSingleResult();
	}

	@Override
	public int executeUpdate(final String namedQuery, final DynamicParameters parameters) {
		String query = getQuery(namedQuery);
		DynamicQuery dynamicQuery = new DynamicQuery(entityManager, query, parameters.getMapParameters());
		return dynamicQuery.getQuery().executeUpdate();
	}

	private String getQuery(final String source) {
		Optional<String> query = NamedQueryCache.get().getQuery(source);
		query.orElseThrow(() -> new DynamicQueryException("Dynamic query source not found: " + source));
		return query.get();
	}

}