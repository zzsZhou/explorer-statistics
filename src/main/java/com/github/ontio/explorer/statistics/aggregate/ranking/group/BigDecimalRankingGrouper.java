package com.github.ontio.explorer.statistics.aggregate.ranking.group;

import com.github.ontio.explorer.statistics.aggregate.ranking.Ranking;
import com.github.ontio.explorer.statistics.aggregate.support.MutableShort;
import com.github.ontio.explorer.statistics.common.ParamsConfig;
import com.github.ontio.explorer.statistics.model.TRanking;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.math.RoundingMode.HALF_UP;

/**
 * @author LiuQi
 */
@RequiredArgsConstructor
public class BigDecimalRankingGrouper<R extends Ranking> implements RankingGrouper<R> {

	private final ParamsConfig config;

	@Getter
	private final short rankingGroup;

	@Getter
	private final short rankingId;

	private final Function<R, BigDecimal> toBigDecimal;

	@Override
	public List<TRanking> group(R[] rankings) {
		MutableShort ranking = new MutableShort();
		BigDecimal total = Stream.of(rankings).map(toBigDecimal).reduce(BigDecimal.ZERO, BigDecimal::add);
		return Stream.of(rankings)
				.sorted(Comparator.comparing(toBigDecimal).reversed())
				.limit(config.getRankingLevel())
				.map(r -> build(r, ranking.incrementAndGet(), total))
				.collect(Collectors.toList());
	}

	private TRanking build(R r, short ranking, BigDecimal total) {
		BigDecimal amount = toBigDecimal.apply(r);

		TRanking result = new TRanking();
		result.setRankingGroup(RankingGrouper.RANKING_GROUP_ADDRESS);
		result.setRankingId(rankingId);
		result.setRankingDuration((short) r.getDuration().getDuration().toDays());
		result.setRanking(ranking);
		result.setMember(r.getMember());
		result.setAmount(amount);
		result.setPercentage(amount.multiply(BigDecimal.valueOf(100)).divide(total, 2, HALF_UP));
		return result;
	}

}
