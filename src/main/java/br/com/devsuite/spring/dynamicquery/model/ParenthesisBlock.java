package br.com.devsuite.spring.dynamicquery.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Bloco para delimitação dos parenteses
 * 
 * @author Welington Gomides - welingtong@gmail.com
 *
 */
public class ParenthesisBlock {

	private ParenthesisBlock parent;

	private final List<ParenthesisBlock> children;

	private boolean isConditional;

	private String conditionalType;

	private String content;

	private final int contentStart;

	public ParenthesisBlock(final String content, final int contentStart) {
		this.content = content;
		this.contentStart = contentStart;
		isConditional = false;
		children = new ArrayList<>();
	}

	public void addChild(final ParenthesisBlock newBlock) {
		newBlock.setParent(this);
		children.add(newBlock);
	}

	public void cropContent(final int contentEnd) {
		this.content = this.content.substring(contentStart, contentEnd);
	}

	public ParenthesisBlock getParent() {
		return parent;
	}

	public void setParent(final ParenthesisBlock parent) {
		this.parent = parent;
	}

	public boolean isIsConditional() {
		return isConditional;
	}

	public void setIsConditional(final boolean isConditional) {
		this.isConditional = isConditional;
	}

	public String getContent() {
		return content;
	}

	public void setConditionalType(final String conditionalType) {
		this.conditionalType = conditionalType;
		this.isConditional = true;
	}

	public QueryConditionalClause getConditionalClause() {
		if (this.isConditional) {
			final QueryConditionalClause out = new QueryConditionalClause(content, conditionalType);
			out.setChildren(getConditionalChildren());
			return out;
		}
		return null;
	}

	private List<QueryConditionalClause> getConditionalChildren() {
		final List<QueryConditionalClause> out = new ArrayList<>();
		for (ParenthesisBlock child : children) {
			final QueryConditionalClause childCond = child.getConditionalClause();
			if (childCond == null) {
				out.addAll(child.getConditionalChildren());
			} else {
				out.add(childCond);
			}
		}
		return out;
	}

	public List<QueryConditionalClause> getAllConditions() {
		final List<QueryConditionalClause> out = new ArrayList<>();
		if (this.isConditional) {
			out.add(getConditionalClause());
		} else {
			out.addAll(getConditionalChildren());
		}
		return out;
	}

}
