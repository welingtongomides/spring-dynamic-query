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
 * opcionais
 * 
 * @author Welington Gomides - welingtong@gmail.com
 *
 */
public class DynamicQuery {

	private final Map<String, Object> params;

	private final EntityManager em;

	private String query;

	private List<PostParser> postParsers;

	public DynamicQuery(final EntityManager em, final String query, Map<String, Object> params) {
		this.em = em;
		this.query = query;
		this.params = params;
	}

	public Query getQuery() throws DynamicQueryException {
		return QueryParser.getInstance().parseQuery(em, false, null, query, params, false, postParsers);
	}

	public Query getCountQuery() throws DynamicQueryException {
		return QueryParser.getInstance().parseQuery(em, false, null, query, params, true, postParsers);
	}

	public Query getPaginationQuery(Pageable pagination) throws DynamicQueryException {
		if (pagination == null) {
			throw new DynamicQueryException("Dever ser informado o campo Pageable!");
		}
		String paginationQuery = query;
		if (pagination.getSort().isSorted()) {
			paginationQuery += " ORDER BY " + pagination.getSort().toString().replace(":", "");
		}
		Query hqlQuery = QueryParser.getInstance().parseQuery(em, false, null, paginationQuery, params, false,
				postParsers);
		hqlQuery.setMaxResults(pagination.getPageSize());
		hqlQuery.setFirstResult(pagination.getPageNumber() * pagination.getPageSize());
		return hqlQuery;
	}

}
