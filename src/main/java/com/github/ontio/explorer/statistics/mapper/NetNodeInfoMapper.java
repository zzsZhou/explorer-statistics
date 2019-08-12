package com.github.ontio.explorer.statistics.mapper;

import com.github.ontio.explorer.statistics.model.NetNodeInfo;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface NetNodeInfoMapper extends Mapper<NetNodeInfo> {

    Integer updateWithLatestInfo(NetNodeInfo netNodeInfo);

}