/* HEADER* */
package com.identity4j.connector;

/**
 * Object for return numeric results from a connector operation along with a 
 * tag that may be used in the future to only retrieve results that have 
 * changed since that tag.
 * @param <N> type of count
 * @see {@link TagResult}
 */
public class Count<N extends Number>  implements TagResult {

	private N amount;
	private String tag;
	
	/**
	 * Constructor.
	 * 
	 * @param amount amount
	 * @param tag tag
	 */
	public Count(N amount) {
		this(amount, null);
	}
	
	/**
	 * Constructor.
	 * 
	 * @param amount amount
	 * @param tag tag
	 */
	public Count(N amount, String tag) {
		this.amount = amount;
		this.tag = tag;
	}
	
	/**
	 * Get the amount
	 * 
	 * @return amount
	 */
	public N amount() {
		return amount;
	}

	@Override
	public String tag() {
		return tag;
	}
}
