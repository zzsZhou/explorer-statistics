package com.github.ontio.explorer.statistics.mapper;

import com.github.ontio.explorer.statistics.model.NodePositionHistory;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface NodePositionHistoryMapper extends Mapper<NodePositionHistory> {
    Long selectLatestBlockHeight();

    void batchInsertSelective(List<NodePositionHistory> nodePositionHistoryList);
}