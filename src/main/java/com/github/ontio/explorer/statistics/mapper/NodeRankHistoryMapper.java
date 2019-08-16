package com.github.ontio.explorer.statistics.mapper;

import com.github.ontio.explorer.statistics.model.NodeRankHistory;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface NodeRankHistoryMapper extends Mapper<NodeRankHistory> {

    Long selectCurrentRoundBlockHeight();

    List<NodeRankHistory> selectNodeRankHistoryListByBlockHeight(@Param("blockHeight") Long blockHeight);

    NodeRankHistory selectNodeRankHistoryByPublicKeyAndBlockHeight(@Param("publicKey") String publicKey, @Param("blockHeight") Long blockHeight);

    void batchInsertSelective(List<NodeRankHistory> nodePositionHistoryList);

}