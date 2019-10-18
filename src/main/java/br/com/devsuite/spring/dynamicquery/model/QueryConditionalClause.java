package br.com.devsuite.spring.dynamicquery.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstração para blocos de query condicional
 *
 *
 *@author Welington Gomides - welingtong@gmail.com
 *
 */
public class QueryConditionalClause {

	private int id;

	private String sql;

	private String type;

	private Set<String> requiredParams;

	private List<Object> queryParts;

	private List<QueryConditionalClause> children;

	private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

	public QueryConditionalClause(final String sql, final String type) {
		this.sql = sql;
		this.type = type;
		id = SEQUENCE.incrementAndGet();
		queryParts = new ArrayList<>();
		requiredParams = new HashSet<>();
	}

	public List<Object> getQueryParts() {
		return queryParts;
	}

	public void setQueryParts(final List<Object> queryParts) {
		this.queryParts = queryParts;
	}

	public List<QueryConditionalClause> getChildren() {
		return children;
	}

	public void setChildren(final List<QueryConditionalClause> children) {
		this.children = children;
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public String getSql() {
		return sql;
	}

	public void setSql(final String sql) {
		this.sql = sql;
	}

	public String getType() {
		return type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public Set<String> getRequiredParams() {
		return requiredParams;
	}

	public void setRequiredParams(final Set<String> requiredParams) {
		this.requiredParams = requiredParams;
	}

}
