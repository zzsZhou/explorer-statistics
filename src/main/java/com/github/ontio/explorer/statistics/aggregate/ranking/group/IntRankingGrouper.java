package com.github.ontio.explorer.statistics.aggregate.ranking.group;

import com.github.ontio.explorer.statistics.aggregate.ranking.Ranking;
import com.github.ontio.explorer.statistics.aggregate.support.MutableShort;
import com.github.ontio.explorer.statistics.common.ParamsConfig;
import com.github.ontio.explorer.statistics.model.TRanking;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.RoundingMode.HALF_UP;

/**
 * @author LiuQi
 */
@RequiredArgsConstructor
@AllArgsConstructor
public class IntRankingGrouper<R extends Ranking> implements RankingGrouper<R> {

	private final ParamsConfig config;

	@Getter
	private final short rankingGroup;

	@Getter
	private final short rankingId;

	private final ToIntFunction<R> toInt;

	private boolean noPercentage;

	@Override
	public List<TRanking> group(R[] rankings) {
		MutableShort ranking = new MutableShort();
		int total = noPercentage ? 0 : Stream.of(rankings).mapToInt(toInt).sum();
		return Stream.of(rankings)
				.sorted(Comparator.comparingInt(toInt).reversed())
				.limit(config.getRankingLevel())
				.map(r -> build(r, ranking.incrementAndGet(), total))
				.collect(Collectors.toList());
	}

	private TRanking build(R r, short ranking, int total) {
		int amount = toInt.applyAsInt(r);

		TRanking result = new TRanking();
		result.setRankingGroup(getRankingGroup());
		result.setRankingId(rankingId);
		result.setRankingDuration((short) r.getDuration().getDuration().toDays());
		result.setRanking(ranking);
		result.setMember(r.getMember());
		result.setAmount(BigDecimal.valueOf(amount));
		if (noPercentage) {
			result.setPercentage(BigDecimal.ZERO);
		} else {
			result.setPercentage(BigDecimal.valueOf((double) amount / (double) total * 100d).setScale(2, HALF_UP));
		}
		return result;
	}

}
