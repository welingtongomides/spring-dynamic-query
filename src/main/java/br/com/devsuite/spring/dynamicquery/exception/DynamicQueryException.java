package br.com.devsuite.spring.dynamicquery.exception;

/**
 * 
 * @author Welington Gomides - welingtong@gmail.com
 *
 */
public class DynamicQueryException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DynamicQueryException() {
		super("Dynamic query erro!");
	}

	public DynamicQueryException(String message) {
		super(message);
	}

}
