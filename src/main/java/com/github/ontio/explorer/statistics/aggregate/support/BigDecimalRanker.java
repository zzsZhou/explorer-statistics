package com.github.ontio.explorer.statistics.aggregate.support;

import com.google.common.base.Preconditions;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * @author LiuQi
 */
public class BigDecimalRanker {

	@Getter
	private BigDecimal max = BigDecimal.ZERO;

	@Getter
	private BigDecimal min = BigDecimal.valueOf(Long.MAX_VALUE);
	
	public BigDecimalRanker(BigDecimal initial){
		rank(initial);
	}

	public void rank(BigDecimal value) {
		Preconditions.checkArgument(value != null, "value mustn't be null");
		if (value.compareTo(max) > 0) {
			max = value;
		}
		if (value.compareTo(min) < 0) {
			min = value;
		}
	}

}
