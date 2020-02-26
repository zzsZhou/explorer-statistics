package com.github.ontio.explorer.statistics.aggregate.ranking;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;

import java.time.Duration;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author LiuQi
 */
@RequiredArgsConstructor
public enum RankingDuration {
	/**
	 * Ranking duration of latest 24 hours, can start at any time.
	 */
	ONE_DAY(Duration.ofHours(24)),
	/**
	 * Ranking duration of latest 3 days, can only start at 0 o'clock
	 */
	THREE_DAYS(Duration.ofDays(3)),
	/**
	 * Ranking duration of latest 7 days, can only start at 0 o'clock
	 */
	SEVEN_DAYS(Duration.ofDays(7));

	@Getter
	private final Duration duration;

	public int getTxTimeBarrier(DateTime dt) {
		DateTime history = dt.plus(-duration.toMillis());
		if (this != ONE_DAY) {
			history = history.withTime(0, 0, 0, 0);
		} else {
			history = history.withTime(history.getHourOfDay(), 0, 0, 0);
		}
		return (int) (history.getMillis() / 1000);
	}

	public Collection<RankingDuration> involved() {
		return Stream.of(RankingDuration.values())
				.filter(duration -> duration.getDuration().compareTo(this.getDuration()) <= 0)
				.collect(Collectors.toList());
	}

	@RequiredArgsConstructor
	public static class Begin {
		@Getter
		private final RankingDuration duration;
		@Getter
		private final int txTimeBarrier;
	}

	@RequiredArgsConstructor
	public static class End {
		@Getter
		private final RankingDuration duration;
	}

}
