package br.com.devsuite.spring.dynamicquery.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import br.com.devsuite.spring.dynamicquery.exception.DynamicQueryException;
import br.com.devsuite.spring.dynamicquery.model.ParenthesisBlock;
import br.com.devsuite.spring.dynamicquery.model.QueryConditionalClause;

/**
 * 
 *@author Welington Gomides - welingtong@gmail.com
 *
 */
public final class QueryParser {

	private static final int ZERO = 0;

	private static final int FOUR = 4;

	private static final String BLOCK_OPEN = " (";

	private static final String EMPTY_STRING = "";

	private static final String SINGLE_SPACE = " ";

	private static final String PATTERN_NUMBER_START = "^\\d+";

	private static final String PATTERN_SPACE_REPEAT = "\\s+";

	private static final String TOKEN_SEPARATOR = "___CoNdItIoNaL_SePaRaToR___";

	private static final String TOKEN_ID = "___CoNdItIoNaL_Id___";

	private static final ConcurrentHashMap<String, List<Object>> CACHED_QUERY_PARTS = new ConcurrentHashMap<>();

	private static final ConcurrentHashMap<String, Set<String>> CACHED_QUERY_REQUIRED_PARAMS = new ConcurrentHashMap<>();

	private static final QueryParser INSTANCE = new QueryParser();

	private QueryParser() {
	}

	public static QueryParser getInstance() {
		return INSTANCE;
	}

	public Query parseQuery(final EntityManager em, final Boolean nativeQuery, final Object resultClass,
			final String query, final Map<String, Object> params, final boolean isCount,
			final List<PostParser> postParsers) throws DynamicQueryException {
		List<Object> queryParts;
		Set<String> queryRequiredParams;
		if (CACHED_QUERY_PARTS.containsKey(query)) {
			queryParts = CACHED_QUERY_PARTS.get(query);
			queryRequiredParams = CACHED_QUERY_REQUIRED_PARAMS.get(query);
		} else {
			final String sql = cleanQuery(query);
			queryRequiredParams = new HashSet<>();
			queryParts = parseConditional(sql, queryRequiredParams);
			CACHED_QUERY_PARTS.put(query, queryParts);
			CACHED_QUERY_REQUIRED_PARAMS.put(query, queryRequiredParams);
		}

		return QueryGenerator.getInstance().generateQuery(em, nativeQuery, resultClass, queryParts, queryRequiredParams,
				params, isCount, postParsers);
	}

	private static String cleanQuery(final String sql) {
		String out = sql;
		out = (Pattern.compile("\\s+\\(\\s+").matcher(out)).replaceAll(BLOCK_OPEN);
		out = (Pattern.compile("\\s+\\)\\s+").matcher(out)).replaceAll(") ");
		out = (Pattern.compile(PATTERN_SPACE_REPEAT).matcher(out)).replaceAll(SINGLE_SPACE);
		return out;
	}

	private static Set<String> generateParams(final String sql) {
		final Set<String> queryParams = new HashSet<>();
		final Pattern pattern = Pattern.compile("[^:\\\\]:([a-zA-Z_][0-9a-zA-Z_]+)");
		final Matcher matcher = pattern.matcher(SINGLE_SPACE + sql);
		while (matcher.find()) {
			queryParams.add(matcher.group(1));
		}
		return queryParams;
	}

	private static List<Object> parseConditional(final String sql, final Set<String> requiredParams) {
		final Pattern pattern = Pattern.compile("(\\))|(\\()");
		final Matcher matcher = pattern.matcher(sql);

		ParenthesisBlock actualBlock = null;
		final List<ParenthesisBlock> rootBlocks = new ArrayList<>();

		while (matcher.find()) {
			if (matcher.group(1) == null) {
				// abre parentese
				final ParenthesisBlock newBlock = new ParenthesisBlock(sql, matcher.start(2) + 1);
				parseParenthesisBlock(matcher, sql, newBlock, actualBlock, rootBlocks);
				actualBlock = newBlock;

			} else {
				// fecha parentese
				if (actualBlock != null) {
					actualBlock.cropContent(matcher.start(1));
					actualBlock = actualBlock.getParent();
				}
			}
		}

		return toQueryParts(sql, extractConditionals(rootBlocks), requiredParams);
	}

	private static void parseParenthesisBlock(final Matcher matcher, final String sql, final ParenthesisBlock newBlock,
			final ParenthesisBlock actualBlock, final List<ParenthesisBlock> rootBlocks) {
		// ...AND|OR (...
		int subStart = matcher.start(2) - FOUR;

		if (subStart < ZERO) {
			subStart++;
		}
		if (subStart >= ZERO) {
			final String conditionalBlockType = sql.substring(subStart, matcher.start(2) - 1)
					.replaceAll(PATTERN_SPACE_REPEAT, EMPTY_STRING);
			final boolean isCondBlock = ("AND".equalsIgnoreCase(conditionalBlockType)
					|| "OR".equalsIgnoreCase(conditionalBlockType));
			if (isCondBlock) {
				// iniciou um bloco condicional
				newBlock.setConditionalType(conditionalBlockType);
			}
		}

		if (actualBlock == null) {
			rootBlocks.add(newBlock);
		} else {
			actualBlock.addChild(newBlock);
		}
	}

	private static List<QueryConditionalClause> extractConditionals(final List<ParenthesisBlock> rootBlocks) {
		final List<QueryConditionalClause> rootConds = new ArrayList<>();
		for (ParenthesisBlock block : rootBlocks) {
			rootConds.addAll(block.getAllConditions());
		}
		return rootConds;
	}

	public static List<Object> toQueryParts(final String sql, final List<QueryConditionalClause> conds,
			final Set<String> requiredParams) {
		String sqlTmp = sql;
		for (QueryConditionalClause cond : conds) {
			final String target = cond.getType() + BLOCK_OPEN + cond.getSql() + ")";
			final String replacement = TOKEN_SEPARATOR + TOKEN_ID + cond.getId();
			sqlTmp = sqlTmp.replace(target, replacement);
			cond.setQueryParts(toQueryParts(cond.getSql(), cond.getChildren(), cond.getRequiredParams()));
		}
		requiredParams.addAll(generateParams(sqlTmp));
		final List<Object> queryParts = new ArrayList<>();
		parseQueryParts(sqlTmp, conds, queryParts);
		return queryParts;
	}

	private static void parseQueryParts(final String query, final List<QueryConditionalClause> conds,
			final List<Object> queryParts) {
		final String[] parts = query.split(TOKEN_SEPARATOR);

		for (String part : parts) {
			if (part.startsWith(TOKEN_ID)) {
				parseQueryPartConditional(part, conds, queryParts);
			} else {
				queryParts.add(part);
			}
		}
	}

	private static void parseQueryPartConditional(final String part, final List<QueryConditionalClause> conds,
			final List<Object> queryParts) {
		// é um condicional
		String str = part.replace(TOKEN_ID, EMPTY_STRING).trim();
		final Matcher matcher = Pattern.compile(PATTERN_NUMBER_START).matcher(str);
		matcher.find();
		final String partIdStr = matcher.group();
		final int partId = Integer.parseInt(partIdStr);
		for (QueryConditionalClause cond : conds) {
			if (cond.getId() == partId) {
				queryParts.add(cond);
			}
		}

		// a query pode ter continuaçao, além do condicional
		str = (Pattern.compile(PATTERN_NUMBER_START).matcher(str)).replaceAll(EMPTY_STRING).trim();
		if (!str.isEmpty()) {
			queryParts.add(str);
		}
	}
}
