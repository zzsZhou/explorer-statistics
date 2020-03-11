package com.github.ontio.explorer.statistics.aggregate.service;

import com.github.ontio.explorer.statistics.aggregate.ranking.AddressRanking;
import com.github.ontio.explorer.statistics.aggregate.ranking.CompositeRanking;
import com.github.ontio.explorer.statistics.aggregate.ranking.Ranking;
import com.github.ontio.explorer.statistics.aggregate.ranking.RankingDuration;
import com.github.ontio.explorer.statistics.aggregate.ranking.TokenRanking;
import com.github.ontio.explorer.statistics.aggregate.ranking.group.RankingGrouper;
import com.github.ontio.explorer.statistics.common.ParamsConfig;
import com.github.ontio.explorer.statistics.mapper.TRankingMapper;
import com.github.ontio.explorer.statistics.model.TRanking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author LiuQi
 */
@Service
@RequiredArgsConstructor
public class RankingService {

	private final ParamsConfig config;

	private final List<RankingGrouper<AddressRanking>> addressRankingGroupers;

	private final List<RankingGrouper<TokenRanking>> tokenRankingGroupers;

	private final TRankingMapper rankingMapper;

	@Transactional
	public void saveRanking(Ranking ranking) {
		if (ranking instanceof CompositeRanking) {
			saveRanking((CompositeRanking) ranking);
		}
	}

	private void saveRanking(CompositeRanking ranking) {
		addressRankingGroupers.forEach(rankingGrouper -> {
			List<TRanking> rankings = rankingGrouper.group(ranking.getAddressRankings());
			save(ranking.getDuration(), rankingGrouper, rankings);
		});
		tokenRankingGroupers.forEach(rankingGrouper -> {
			List<TRanking> rankings = rankingGrouper.group(ranking.getTokenRankings());
			save(ranking.getDuration(), rankingGrouper, rankings);
		});
	}

	private void save(RankingDuration duration, RankingGrouper<?> rankingGrouper, List<TRanking> rankings) {
		if (rankings == null || rankings.isEmpty()) {
			return;
		}
		if (rankings.size() < config.getRankingLevel()) {
			rankingMapper.deleteRankings(rankingGrouper.getRankingGroup(), rankingGrouper.getRankingId(),
					(short) duration.getDuration().toDays());
		}
		rankingMapper.saveRankings(rankings);
	}

}
