package com.identity4j.connector.office365.filter;

public class StartsWith implements Filter {

	private Filter expression;
	private Filter value;

	public StartsWith(Filter expression, String value) {
		this(expression, new Text(value));
	}

	protected StartsWith(Filter expression, Filter value) {
		this.expression = expression;
		this.value = value;
	}
	
	@Override
	public String encode() {
		StringBuilder b = new StringBuilder();
		b.append("startswith(");
		b.append(expression.encode());
		b.append(",");
		b.append(value.encode());
		b.append(")");
		return b.toString();
	}

}
