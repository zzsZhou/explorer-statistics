package com.github.ontio.explorer.statistics.aggregate.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author LiuQi
 */
@RequiredArgsConstructor
public class StagingAggregateKeys implements Serializable {
	
	@Getter
	private final Collection<AggregateKey> aggregateKeys;
	
}
