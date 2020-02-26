package com.github.ontio.explorer.statistics.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@Scope(value = "singleton")
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "config")
public class ParamsConfig {

    private List<String> hosts = new ArrayList<>();

    private String consensusNodeDetailUrl;

    private String nodeMapUrl;

    private Boolean isTestNet;

    private int maxStakingChangeCount;

    private String ontContractHash = "0100000000000000000000000000000000000000";

    private String ongContractHash = "0200000000000000000000000000000000000000";

    private double aggregationRateLimit = 100;

    private int rankingLevel = 10;

    public interface Field {

        String maxStakingChangeCount = "maxStakingChangeCount";

    }

}