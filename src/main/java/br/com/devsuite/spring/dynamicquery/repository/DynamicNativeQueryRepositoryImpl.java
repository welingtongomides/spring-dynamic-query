package br.com.devsuite.spring.dynamicquery.repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import br.com.devsuite.spring.dynamicquery.DynamicNativeQuery;
import br.com.devsuite.spring.dynamicquery.cache.FileNativeQueryCache;
import br.com.devsuite.spring.dynamicquery.cache.NamedNativeQueryCache;
import br.com.devsuite.spring.dynamicquery.exception.DynamicQueryException;
import br.com.devsuite.spring.dynamicquery.parameters.DynamicParameters;

/**
 * 
 * @author Welington Gomides - welingtong@gmail.com
 *
 */
@SuppressWarnings("unchecked")
@Repository
public class DynamicNativeQueryRepositoryImpl implements DynamicNativeQueryRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public <T> List<T> findNative(final String source, final DynamicParameters parameters) {
		DynamicNativeQuery dynamicQuery = new DynamicNativeQuery(entityManager, getQuerySql(source),
				parameters.getMapParameters());
		return dynamicQuery.getQuery().getResultList();
	}

	@Override
	public <T> Page<T> findNative(String source, DynamicParameters parameters, Pageable pageable) {
		DynamicNativeQuery dynamicQuery = new DynamicNativeQuery(entityManager, getQuerySql(source),
				parameters.getMapParameters());
		
		Object count = dynamicQuery.getCountQuery().getSingleResult();
		Long countLong = null; 
		if (count instanceof BigInteger) {
		    countLong = ((BigInteger) count).longValue();
		} else if (count instanceof BigDecimal) {
		    countLong = ((BigDecimal) count).longValue();
		} else {
		    countLong = Long.valueOf(count.toString());
		}
		
		return new PageImpl<>(dynamicQuery.getPaginationQuery(pageable).getResultList(), pageable, countLong);
	}

	@Override
	public <T> T findOneNative(final String source, final DynamicParameters parameters) {
		DynamicNativeQuery dynamicQuery = new DynamicNativeQuery(entityManager, getQuerySql(source),
				parameters.getMapParameters());
		return (T) dynamicQuery.getQuery().getSingleResult();
	}

	@Override
	public int executeUpdateNative(final String source, final DynamicParameters parameters) {
		DynamicNativeQuery dynamicQuery = new DynamicNativeQuery(entityManager, getQuerySql(source),
				parameters.getMapParameters());
		return dynamicQuery.getQuery().executeUpdate();
	}

	private String getQuerySql(final String source) {
		Optional<String> query = NamedNativeQueryCache.get().getQuery(source);
		if (!query.isPresent()) {
			query = FileNativeQueryCache.get().getQuery(source);
		}
		query.orElseThrow(() -> new DynamicQueryException("Dynamic query source not found: " + source));
		return query.get();
	}

}