package com.github.ontio.explorer.statistics.aggregate.support;

import java.io.Serializable;

/**
 * @author LiuQi
 */
public class MutableShort implements Serializable {

	private short value;

	public short incrementAndGet() {
		return ++value;
	}

}
