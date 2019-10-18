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
public interface DynamicNativeQueryRepository {

	public <T> List<T> findNative(final String source, final DynamicParameters parameters);

	public <T> Page<T> findNative(String source, DynamicParameters parameters, Pageable pagination);

	public <T> T findOneNative(final String source, final DynamicParameters parameters);

	public int executeUpdateNative(final String source, final DynamicParameters parameters);
}