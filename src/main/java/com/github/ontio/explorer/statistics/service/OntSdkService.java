package com.github.ontio.explorer.statistics.service;

import com.github.ontio.OntSdk;
import com.github.ontio.core.governance.GovernanceView;
import com.github.ontio.explorer.statistics.common.ParamsConfig;
import com.github.ontio.network.exception.ConnectorException;
import com.github.ontio.sdk.exception.SDKException;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.LinkOption;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Slf4j
@Service
@NoArgsConstructor
public class OntSdkService {

    private OntSdk sdk;

    private int nodeCount;

    private AtomicInteger currentNodeIndex;

    private ParamsConfig paramsConfig;

    @Autowired
    public OntSdkService(ParamsConfig paramsConfig) {
        this.paramsConfig = paramsConfig;
        this.nodeCount = paramsConfig.getHosts().size();
        this.currentNodeIndex = new AtomicInteger(0);
        this.sdk = OntSdk.getInstance();
        try {
            sdk.getRestful();
        } catch (SDKException e) {
            sdk.setRestful(paramsConfig.getHosts().get(this.currentNodeIndex.get()));
        }
    }

    GovernanceView getGovernanceView() {
        try {
            return sdk.nativevm().governance().getGovernanceView();
        } catch (ConnectorException | IOException | SDKException e) {
            log.warn("Getting governance view failed: {}", e.getMessage());
            switchSyncNode();
            log.info("Getting governance view again");
            return getGovernanceView();
        }
    }

    int getBlockHeight() {
        try {
            return sdk.getRestful().getBlockHeight();
        } catch (ConnectorException | IOException | SDKException e) {
            log.warn("Getting block height failed: {}", e.getMessage());
            switchSyncNode();
            log.info("Getting block height again");
            return getBlockHeight();
        }
    }

    void switchSyncNode() {
        if (currentNodeIndex.get() >= nodeCount) {
            currentNodeIndex.set(0);
        }
        sdk.setRestful(paramsConfig.getHosts().get(currentNodeIndex.getAndAdd(1)));
        try {
            log.warn("Using node: {}", sdk.getRestful().toString());
        } catch (SDKException e) {
            log.warn("Getting REST URL failed: {}", e.getMessage());
        }
    }

}
