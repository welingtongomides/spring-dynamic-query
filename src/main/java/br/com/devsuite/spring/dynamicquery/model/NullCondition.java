package br.com.devsuite.spring.dynamicquery.model;

/**
 * Condição de nulo.
 * 
 * @author Welington Gomides - welingtong@gmail.com
 *
 */
public enum NullCondition {

	IS_NULL("IS NULL"), IS_NOT_NULL("IS NOT NULL");

	private String text;

	NullCondition(final String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

}
