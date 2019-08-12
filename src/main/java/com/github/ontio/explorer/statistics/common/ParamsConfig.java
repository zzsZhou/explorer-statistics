package com.github.ontio.explorer.statistics.common;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "config")
public class ParamsConfig {

    private List<String> hosts = new ArrayList<>();

    private String consensusNodeDetailUrl;

    private String nodeMapUrl;

    private Boolean isTestNet;

}