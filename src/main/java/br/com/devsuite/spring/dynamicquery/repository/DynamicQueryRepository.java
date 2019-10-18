package br.com.devsuite.spring.dynamicquery.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.devsuite.spring.dynamicquery.parameters.DynamicParameters;

/**
 * 
 * @author Welington Gomides - welingtong@gmail.com
 *
 */
public interface DynamicQueryRepository {

	public <T> List<T> find(final String namedQuery, final DynamicParameters parameters, Class<T> entityClass);

	public <T> Page<T> find(String namedQuery, DynamicParameters parameters, Pageable pageable, Class<T> entityClass);

	public <T> T findOne(final String namedQuery, final DynamicParameters parameters, Class<T> entityClass);

	public int executeUpdate(final String namedQuery, final DynamicParameters parameters);
}