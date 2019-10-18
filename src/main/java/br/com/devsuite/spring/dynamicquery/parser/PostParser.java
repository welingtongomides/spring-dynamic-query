package br.com.devsuite.spring.dynamicquery.parser;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Interface para implementar o parser após a construção da query 
 * 
 * @author Welington Gomides - welingtong@gmail.com
 *
 */
public interface PostParser {
	public String parse(final String sql, final ConcurrentHashMap<String, Object> finalParams);
}
