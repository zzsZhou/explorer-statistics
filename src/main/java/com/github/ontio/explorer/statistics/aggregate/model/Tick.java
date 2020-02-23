package com.github.ontio.explorer.statistics.aggregate.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.time.Duration;

/**
 * @author LiuQi
 */
@RequiredArgsConstructor
public final class Tick implements Serializable {

	@Getter
	private final Duration duration;

}
