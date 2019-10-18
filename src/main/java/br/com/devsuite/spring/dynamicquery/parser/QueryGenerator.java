package br.com.devsuite.spring.dynamicquery.parser;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.devsuite.spring.dynamicquery.exception.DynamicQueryException;
import br.com.devsuite.spring.dynamicquery.model.NullCondition;
import br.com.devsuite.spring.dynamicquery.model.QueryConditionalClause;

/**
 * 
 * @author Welington Gomides - welingtong@gmail.com
 *
 */
public final class QueryGenerator {

	private static final String BLOCK_CLOSE = ")";

	private static final String WHITE_SPACE = " ";

	private static final QueryGenerator INSTANCE = new QueryGenerator();

	private static final Logger LOG = LoggerFactory.getLogger(QueryGenerator.class);

	private QueryGenerator() {
	}

	public static QueryGenerator getInstance() {
		return INSTANCE;
	}

	private boolean existsRequiredParams(final Set<String> requiredParams, final Map<String, Object> params) {
		boolean out;
		// verifica se possui todos os parametros obrigatórios para esse condicional
		if (requiredParams == null || requiredParams.isEmpty()) {
			out = true;
		} else if (params == null || params.isEmpty()) {
			out = false;
		} else {
			out = params.keySet().containsAll(requiredParams);
		}
		return out;
	}

	private void parseQuery(final QueryConditionalClause cond, final Map<String, Object> params,
			final StringBuilder finalSql, final Map<String, Object> finalParams) {
		final List<Object> condQueryParts = cond.getQueryParts();
		final Set<String> requiredParams = cond.getRequiredParams();
		if (existsRequiredParams(requiredParams, params)) {
			finalSql.append(WHITE_SPACE).append(cond.getType()).append(" (");
			parseQuery(condQueryParts, requiredParams, params, finalSql, finalParams);
			finalSql.append(BLOCK_CLOSE);
			if (requiredParams != null) {
				for (String param : requiredParams) {
					finalParams.put(param, params.get(param));
				}
			}
		}
	}

	private void parseQuery(final List<Object> queryParts, final Set<String> requiredParams,
			final Map<String, Object> params, final StringBuilder finalSql, final Map<String, Object> finalParams) {
		if (!existsRequiredParams(requiredParams, params)) {
			return;
		}
		for (Object part : queryParts) {
			if (part instanceof QueryConditionalClause) {
				final QueryConditionalClause cond = (QueryConditionalClause) part;
				parseQuery(cond, params, finalSql, finalParams);
			} else if (part instanceof String) {
				finalSql.append(WHITE_SPACE).append((String) part).append(WHITE_SPACE);
			}
		}
	}

	public Query generateQuery(final EntityManager em, final Boolean nativeQuery, final Object resultClass,
			final List<Object> queryParts, final Set<String> queryRequiredParams, final Map<String, Object> params,
			final boolean isCount, final List<PostParser> postParsers) throws DynamicQueryException {
		if (!existsRequiredParams(queryRequiredParams, params)) {
			throw new DynamicQueryException("Parametros requeridos nao informados: " + queryRequiredParams.toString());
		}
		final ConcurrentHashMap<String, Object> finalParams = new ConcurrentHashMap<>();
		final String sql = getQueryString(queryParts, nativeQuery, queryRequiredParams, params, finalParams, isCount,
				postParsers);
		return makeQuery(em, nativeQuery, resultClass, sql, finalParams);
	}

	public String getQueryString(final List<Object> queryParts, final Boolean nativeQuery,
			final Set<String> queryRequiredParams, final Map<String, Object> params,
			final ConcurrentHashMap<String, Object> finalParams, final boolean isCount,
			final List<PostParser> postParsers) {
		final StringBuilder queryBuilder = new StringBuilder();
		addRequiredParams(queryRequiredParams, params, finalParams);
		parseQuery(queryParts, queryRequiredParams, params, queryBuilder, finalParams);
		String sql = queryBuilder.toString();
		if (isCount) {
			sql = queryCount(nativeQuery, sql);
		}
		sql = removeNullConditionalParameters(sql, finalParams);
		return usePostParsers(postParsers, sql, finalParams);
	}

	private String queryCount(final boolean nativeQuery, String sql) {
		if (nativeQuery) {
			return sql = new StringBuilder("SELECT COUNT (0) FROM (").append(sql).append(BLOCK_CLOSE).toString();
		}
		int fromPos = sql.toUpperCase().indexOf("FROM");
		sql = sql.substring(fromPos + 5);
		return new StringBuilder("SELECT COUNT (0) FROM ").append(sql).toString();

	}

	private void addRequiredParams(final Set<String> queryRequiredParams, final Map<String, Object> params,
			final ConcurrentHashMap<String, Object> finalParams) {
		if (queryRequiredParams != null) {
			for (String param : queryRequiredParams) {
				finalParams.put(param, params.get(param));
			}
		}
	}

	private Query makeQuery(final EntityManager em, final Boolean nativeQuery, final Object resultClass,
			final String sql, final Map<String, Object> finalParams) throws DynamicQueryException {
		Query query = createQuery(em, nativeQuery, resultClass, sql);
		// Seta os parametros da query
		for (Map.Entry<String, Object> entrySet : finalParams.entrySet()) {
			query.setParameter(entrySet.getKey(), entrySet.getValue());
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("CatchAllQuery - query gerada");
			LOG.debug(sql);
			LOG.debug("atributos");
			for (Map.Entry<String, Object> entrySet : finalParams.entrySet()) {
				LOG.debug(entrySet.getKey() + ": " + entrySet.getValue());
			}
		}
		return query;
	}

	private Query createQuery(final EntityManager em, final Boolean nativeQuery, final Object resultClass,
			final String sql) {
		if (nativeQuery) {
			return createNativeQuery(em, sql, resultClass);
		}
		return createQuery(em, sql);
	}

	private Query createQuery(final EntityManager em, final String hql) {
		return em.createQuery(hql);
	}

	private Query createNativeQuery(final EntityManager em, final String sql, final Object resultClass)
			throws DynamicQueryException {
		final Query query;
		if (resultClass == null) {
			query = em.createNativeQuery(sql);
		} else if (resultClass instanceof Class) {
			query = em.createNativeQuery(sql, (Class<?>) resultClass);
		} else if (resultClass instanceof String) {
			query = em.createNativeQuery(sql, (String) resultClass);
		} else {
			throw new DynamicQueryException("resultClass inválido, espera Class ou String como argumento.");
		}
		return query;
	}

	private String usePostParsers(final List<PostParser> postParsers, final String sql,
			final ConcurrentHashMap<String, Object> finalParams) {
		String innerSql = sql;
		if (postParsers != null) {
			for (PostParser postParser : postParsers) {
				innerSql = postParser.parse(innerSql, finalParams);
			}
		}
		return innerSql;
	}

	private String removeNullConditionalParameters(final String sql, final Map<String, Object> finalParams) {
		String innerSql = sql;
		for (Map.Entry<String, Object> entrySet : finalParams.entrySet()) {
			if (entrySet.getValue() instanceof NullCondition) {
				innerSql = innerSql.replaceAll(
						"((>|<)?=|((NOT|not)\\s+)?(IN|in|LIKE|like))\\s*" + "(:" + entrySet.getKey() + "|\\(:"
								+ entrySet.getKey() + "\\))",
						WHITE_SPACE + ((NullCondition) entrySet.getValue()).getText());
				finalParams.remove(entrySet.getKey());
			}
		}
		return innerSql;
	}

}
