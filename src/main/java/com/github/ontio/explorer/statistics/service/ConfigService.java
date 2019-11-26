package com.github.ontio.explorer.statistics.service;

import com.github.ontio.explorer.statistics.common.ParamsConfig;
import com.github.ontio.explorer.statistics.mapper.ConfigMapper;
import com.github.ontio.explorer.statistics.model.Config;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@NoArgsConstructor
public class ConfigService {

    private ParamsConfig paramsConfig;

    private ConfigMapper configMapper;

    private OntSdkService ontSdkService;

    @Autowired
    public ConfigService(ParamsConfig paramsConfig, ConfigMapper configMapper, OntSdkService ontSdkService) {
        this.paramsConfig = paramsConfig;
        this.configMapper = configMapper;
        this.ontSdkService = ontSdkService;
    }


    public String getmaxStakingChangeCount() {
        Config config = configMapper.selectByPrimaryKey(ParamsConfig.Field.stakingRoundBlockCount);
        return config == null ? "" : config.getValue();
    }

    public String updateMaxStakingChangeCount() {
        int maxStakingChangeCount = ontSdkService.getStakingChangeCount();
        Config config = new Config(ParamsConfig.Field.stakingRoundBlockCount, String.valueOf(maxStakingChangeCount));
        Config selectConfig = configMapper.selectByPrimaryKey(ParamsConfig.Field.stakingRoundBlockCount);
        int result;
        if (selectConfig == null) {
            result = configMapper.insert(config);
        } else {
            result = configMapper.updateByPrimaryKeySelective(config);
        }
        if (result != 1) {
            log.warn("Updating max block change view to {} failed", config.getValue());
            return "";
        }
        log.info("Updating max block change view to {} success", config.getValue());
        paramsConfig.setMaxStakingChangeCount(maxStakingChangeCount);
        log.info("Max staking change count has been updated to {}", paramsConfig.getMaxStakingChangeCount());
        return config.getValue();
    }


}
