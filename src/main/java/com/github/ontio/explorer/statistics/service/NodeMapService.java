package com.github.ontio.explorer.statistics.service;

import com.alibaba.fastjson.JSONArray;
import com.github.ontio.explorer.statistics.common.ParamsConfig;
import com.github.ontio.explorer.statistics.mapper.NetNodeInfoMapper;
import com.github.ontio.explorer.statistics.model.NetNodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;

@Slf4j
@Service
public class NodeMapService {

    private RestTemplate restTemplate;

    private ParamsConfig paramsConfig;

    private NetNodeInfoMapper netNodeInfoMapper;

    @Autowired
    public NodeMapService(ParamsConfig paramsConfig, NetNodeInfoMapper netNodeInfoMapper) {
        this.restTemplate = new RestTemplate();
        this.paramsConfig = paramsConfig;
        this.netNodeInfoMapper = netNodeInfoMapper;
    }

    public void getNodesInfo() {
        JSONArray result;
        try {
            result = restTemplate.getForObject(paramsConfig.getNodeMapUrl(), JSONArray.class);
        } catch (Exception e) {
            log.warn("Getting nodes in network failed: {}", e.getMessage());
            return;
        }
        if (result == null) {
            log.warn("Getting nodes in network failed: null result received");
            return;
        }
        for (Object obj : result) {
            LinkedHashMap jsonObject = (LinkedHashMap) obj;
            NetNodeInfo netNodeInfo = new NetNodeInfo(jsonObject);
            try {
                if (netNodeInfoMapper.existsWithPrimaryKey(netNodeInfo.getIp())) {
                    int updateResult = netNodeInfoMapper.updateWithLatestInfo(netNodeInfo);
                    if (updateResult != 0) {
                        log.info("Update network node with ip {}", netNodeInfo.getIp());
                    }
                } else {
                    netNodeInfoMapper.insert(netNodeInfo);
                    log.info("Insert network node with ip {}", netNodeInfo.getIp());
                }
            } catch (Exception e) {
                log.warn("Update node info {} failed: {}", netNodeInfo, e.getMessage());
            }
        }
    }

}
