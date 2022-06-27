package br.com.devsuite.spring.dynamicquery;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.springframework.data.domain.Pageable;

import br.com.devsuite.spring.dynamicquery.exception.DynamicQueryException;
import br.com.devsuite.spring.dynamicquery.parser.PostParser;
import br.com.devsuite.spring.dynamicquery.parser.QueryParser;

/**
 * Abstração para queries de pesquisa , que pode possuir diversos parametros
 * opcionais para querys Nativas
 *
 * @author Welington Gomides - welingtong@gmail.com
 *
 *
 */
public class DynamicNativeQuery {

	private final Map<String, Object> params;

	private Object resultClass;

	private final EntityManager em;

	private String query;

	private List<PostParser> postParsers;

	public DynamicNativeQuery(final EntityManager em, final String query, Map<String, Object> params) {
		this.em = em;
		this.query = query;
		this.params = params;
	}

	public Query getQuery() throws DynamicQueryException {
		return QueryParser.getInstance().parseQuery(em, true, resultClass, query, params, false, postParsers);
	}

	public Query getCountQuery() throws DynamicQueryException {
		return QueryParser.getInstance().parseQuery(em, true, resultClass, query, params, true, postParsers);
	}

	public Query getPaginationQuery(Pageable pagination) throws DynamicQueryException {
		if (pagination == null) {
			throw new DynamicQueryException("Dever ser informado o campo Pageable!");
		}

        String paginationQuery = query;
        if (pagination.getSort().isSorted()) {
            paginationQuery += " ORDER BY " + pagination.getSort().toString().replace(":", "");
        }

		Query queryNative = QueryParser.getInstance().parseQuery(em, true, resultClass, paginationQuery, params, false,
				postParsers);

		queryNative.setMaxResults(pagination.getPageSize());
		queryNative.setFirstResult(pagination.getPageNumber() * pagination.getPageSize());

		return queryNative;

	}

	public void setResultClass(Object resultClass) {
		this.resultClass = resultClass;
	}

}
