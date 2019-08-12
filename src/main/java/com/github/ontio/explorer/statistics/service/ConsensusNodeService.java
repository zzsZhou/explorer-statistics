///*
// * Copyright (C) 2018 The ontology Authors
// * This file is part of The ontology library.
// * The ontology is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Lesser General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// * The ontology is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Lesser General Public License for more details.
// * You should have received a copy of the GNU Lesser General Public License
// * along with The ontology.  If not, see <http://www.gnu.org/licenses/>.
// */

package com.github.ontio.explorer.statistics.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ontio.OntSdk;
import com.github.ontio.core.governance.PeerPoolItem;
import com.github.ontio.explorer.statistics.common.ParamsConfig;
import com.github.ontio.explorer.statistics.mapper.NodeInfoOffChainMapper;
import com.github.ontio.explorer.statistics.mapper.NodeInfoOnChainMapper;
import com.github.ontio.explorer.statistics.model.NodeInfoOnChain;
import com.github.ontio.sdk.exception.SDKException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@NoArgsConstructor
public class ConsensusNodeService {

    private ParamsConfig paramsConfig;

    private ObjectMapper objectMapper;

    private NodeInfoOnChainMapper nodeInfoOnChainMapper;

    private NodeInfoOffChainMapper nodeInfoOffChainMapper;

    private OntSdkService ontSdkService;

    @Autowired
    public ConsensusNodeService(ParamsConfig paramsConfig,
                                ObjectMapper objectMapper,
                                OntSdkService ontSdkService,
                                NodeInfoOnChainMapper nodeInfoOnChainMapper,
                                NodeInfoOffChainMapper nodeInfoOffChainMapper) {
        this.paramsConfig = paramsConfig;
        this.ontSdkService = ontSdkService;
        this.objectMapper = objectMapper;
        this.nodeInfoOnChainMapper = nodeInfoOnChainMapper;
        this.nodeInfoOffChainMapper = nodeInfoOffChainMapper;
    }

    public void updateConsensusNodeInfo() {
        Map peerPool = getPeerPool();
        List<NodeInfoOnChain> nodes = getNodesWithAttributes(peerPool);
        nodes.sort((v1, v2) -> Long.compare(v2.getInitPos() + v2.getTotalPos(), v1.getInitPos() + v1.getTotalPos()));
        List<NodeInfoOnChain> nodeInfos = calcNodeInfo(nodes);
        nodes = matchNodeName(nodeInfos);
        updateNodesTable(nodes);
    }

    private Map getPeerPool() {
        try {
            return ontSdkService.getSdk().nativevm().governance().getPeerPoolMap();
        } catch (Exception e) {
            log.error("Get peer pool map failed: {}", e.getMessage());
            ontSdkService.switchSyncNode();
            return getPeerPool();
        }
    }

    private List<NodeInfoOnChain> getNodesWithAttributes(Map peerPool) {
        List<NodeInfoOnChain> nodes = new ArrayList<>();
        for (Object obj : peerPool.values()) {
            PeerPoolItem item = (PeerPoolItem) obj;
            // candidate nodes and consensus nodes
            if (item.status != 1 && item.status != 2) {
                continue;
            }
            HashMap<String, Object> attribute = getAttributes(item.peerPubkey);
            NodeInfoOnChain node = new NodeInfoOnChain(item);
            node.setMaxAuthorize(Long.parseLong(attribute.get("maxAuthorize").toString()));
            node.setNodeProportion((100 - (int) attribute.get("t1PeerCost")) + "%");
            nodes.add(node);
        }
        return nodes;
    }

    private HashMap<String, Object> getAttributes(String pubKey) {
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
        };
        try {
            String result = ontSdkService.getSdk().nativevm().governance().getPeerAttributes(pubKey);
            return objectMapper.readValue(result, typeRef);
        } catch (Exception e) {
            log.error("Getting {}'s peer attributes failed: {}", pubKey, e.getMessage());
            ontSdkService.switchSyncNode();
            return getAttributes(pubKey);
        }
    }

    private List<NodeInfoOnChain> calcNodeInfo(List<NodeInfoOnChain> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            NodeInfoOnChain node = nodes.get(i);
            node.setNodeRank(i + 1);
            BigDecimal currentPos = new BigDecimal(node.getInitPos()).add(new BigDecimal(node.getTotalPos()));
            BigDecimal targetPos = new BigDecimal(node.getInitPos()).add(new BigDecimal(node.getMaxAuthorize()));
            node.setCurrentStake(currentPos.longValue());
            node.setProgress(currentPos.multiply(new BigDecimal(100)).divide(targetPos, 2, RoundingMode.HALF_UP) + "%");
            node.setDetailUrl(paramsConfig.getConsensusNodeDetailUrl() + node.getPublicKey());
            nodes.set(i, node);
        }
        return nodes;
    }

    private void updateNodesTable(List<NodeInfoOnChain> nodes) {
        if (nodes.size() == 0) {
            log.warn("Updating NodeInfoOnchain table failed, nodes list is empty.");
            return;
        }
        int result = nodeInfoOnChainMapper.deleteAll();
        log.info("Updating NodeInfoOnchain table: delete {} nodes info.", result);
        try {
            result = nodeInfoOnChainMapper.batchInsert(nodes);
            log.info("Updating tbl_node_info_on_chain: insert {} nodes info.", result);
        } catch (Exception e) {
            log.error("Inserting {} into tbl_node_info_on_chain failed.", nodes.toString());
            log.error("Updating tbl_node_info_on_chain failed: {}", e.getMessage());
        }
    }

    private List<NodeInfoOnChain> matchNodeName(List<NodeInfoOnChain> nodeInfos) {
        int i = 0;
        for (NodeInfoOnChain info : nodeInfos) {
            try {
                String name = nodeInfoOffChainMapper.selectNameByPublicKey(info.getPublicKey());
                if (name == null) {
                    name = "";
                    log.warn("Selecting name by public key {} failed.", info.getPublicKey());
                }
                info.setName(name);
                nodeInfos.set(i, info);
            } catch (Exception e) {
                log.error("Matching node name failed: {}", info.getPublicKey());
                log.error(e.getMessage());
            }
            i++;
        }
        return nodeInfos;
    }
}
