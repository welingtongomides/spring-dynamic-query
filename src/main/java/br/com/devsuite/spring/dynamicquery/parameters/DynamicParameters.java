package br.com.devsuite.spring.dynamicquery.parameters;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.devsuite.spring.dynamicquery.model.NullCondition;

/**
 * Classe para criação de parametros dinâmicos
 * 
 * @author Welington Gomides - welingtong@gmail.com
 *
 */
public class DynamicParameters {

	private static final Logger LOG = LoggerFactory.getLogger(DynamicParameters.class.getName());

	private Map<String, Object> mapParameters = new ConcurrentHashMap<>();

	public static DynamicParameters get() {
		return new DynamicParameters();
	}

	private DynamicParameters() {

	}

	public DynamicParameters appendSimNao(final String field, final Boolean value) {
		append(field, (value != null && value) ? "S" : "N");
		return this;
	}

	public DynamicParameters appendLike(final String field, final String value) {
		return appendLike(field, value, "\\");
	}

	public DynamicParameters appendLike(final String field, final String value, final String escapeChar) {
		if (value != null) {
			final String term = value.replace("%", escapeChar.concat("%")).replace("_", escapeChar.concat("_"));
			append(field, String.format("%%%s%%", term));
		}
		return this;
	}

	public DynamicParameters append(final String field, final Object value) {
		boolean addValue = true;
		if (value == null) {
			addValue = false;
		} else if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
			addValue = false;
		}

		if (addValue) {
			mapParameters.put(field, value);
		}
		return this;
	}

	public DynamicParameters appendDate(final String field, final LocalDate value) {
		if (value != null) {
			append(field, Date.from(value.atStartOfDay(ZoneId.systemDefault()).toInstant()));
		}
		return this;
	}

	public DynamicParameters appendOrIsNull(final String field, final Object value) {
		append(field, value);
		if (!mapParameters.containsKey(field)) {
			append(field, NullCondition.IS_NULL);
		}
		return this;
	}

	public DynamicParameters appendOrIsNotNull(final String field, final Object value) {
		append(field, value);
		if (!mapParameters.containsKey(field)) {
			append(field, NullCondition.IS_NOT_NULL);
		}
		return this;
	}

	public Map<String, Object> getMapParameters() {
		return mapParameters;
	}

	/**
	 * Esse metodo é ulizado para geração Dynamic Parameters a com os valores
	 * obtidos de uma classe
	 *
	 * Ex: DynamicParameters.from(usuario, "nome", "email",
	 * "usuarioComplemento.bairro");
	 *
	 * Os parametros tem o mesmo nome dos atributos da classe Para subclasses o
	 * separador deve ser o ponto "." e o atributo no mapa separado por "_". Ex:
	 * "usuarioComplemento.bairro" no mapa fica "usuarioComplemento_bairro"
	 *
	 */
	public static DynamicParameters from(Object source, String... fields) {
		DynamicParameters dynamicParameters = get();
		Object fieldValue = null;
		try {
			for (String field : fields) {
				String mapField = field.replace(".", "_");

				if (field.contains(".")) {
					fieldValue = getValorClassesInternas(source.getClass(), field, source);
				} else {
					Method metodo = source.getClass().getDeclaredMethod("get" + StringUtils.capitalize(field));
					fieldValue = (Object) metodo.invoke(source, new Object[] {});
				}
				dynamicParameters.append(mapField, fieldValue);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		return dynamicParameters;
	}

	public static DynamicParameters from(Optional<Object> source, String... fields) {
		if (source.isPresent()) {
			return from(source.get(), fields);
		}
		return get();
	}

	public static Object getValorClassesInternas(Class<?> classe, String key, Object entidade) throws Exception {
		String[] atributos = key.split("[.]");
		Object obj = entidade;
		Class<?> interna = null;
		for (String item : atributos) {

			if (obj == null) {
				obj = interna.newInstance();
			}
			Field propriedade = getPropriedade(obj.getClass(), item);
			interna = propriedade.getType();
			propriedade.setAccessible(true);
			obj = propriedade.get(obj);

		}
		return obj;

	}

	public static Field getPropriedade(Class<?> c, String key) throws NoSuchFieldException, SecurityException {
		Field propriedade = null;

		for (String nome : key.split("[.]")) {
			if (propriedade != null) {
				c = propriedade.getType();
				propriedade = c.getDeclaredField(nome);
			} else {
				propriedade = c.getDeclaredField(nome);
			}
		}

		return propriedade;
	}
}
