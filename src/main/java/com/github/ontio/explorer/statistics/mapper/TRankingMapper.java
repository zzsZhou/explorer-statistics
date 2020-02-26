package com.github.ontio.explorer.statistics.mapper;

import com.github.ontio.explorer.statistics.model.TRanking;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface TRankingMapper extends Mapper<TRanking> {

	int deleteRankings(@Param("group") short rankingGroup, @Param("id") short rankingId, @Param("duration") short rankingDuration);

	int saveRankings(List<TRanking> rankings);

}