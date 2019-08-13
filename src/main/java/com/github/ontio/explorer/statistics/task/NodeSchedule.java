/*
 * Copyright (C) 2018 The ontology Authors
 * This file is part of The ontology library.
 * The ontology is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * The ontology is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with The ontology.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.ontio.explorer.statistics.task;

import com.github.ontio.explorer.statistics.service.ConsensusNodeService;
import com.github.ontio.explorer.statistics.service.NodeMapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
public class NodeSchedule {

    private final NodeMapService nodeMapService;

    private final ConsensusNodeService consensusNodeService;

    @Autowired
    public NodeSchedule(NodeMapService nodeMapService, ConsensusNodeService consensusNodeService) {
        this.nodeMapService = nodeMapService;
        this.consensusNodeService = consensusNodeService;
    }

    @Scheduled(fixedDelay = 300000)
    public void updateNodeInfo() {
        try {
            log.info("Updating consensus node information start");
            consensusNodeService.updateConsensusNodeInfo();
            log.info("Updating consensus node information task end");
        } catch (Exception e) {
            log.warn("Updating consensus node information task failed: {}", e.getMessage());
            log.info("Updating consensus node information task again");
            updateNodeInfo();
        }
    }

    @Scheduled(fixedDelay = 20000)
    public void updateNetNodesInfo() {
        try {
            log.info("Updating global network nodes info task begin");
            nodeMapService.getNodesInfo();
            log.info("Updating global network nodes info task end");
        } catch (Exception e) {
            log.warn("Updating global network nodes info task failed: {}", e.getMessage());
            log.info("Updating global network nodes task again");
            updateNetNodesInfo();
        }
    }

    @Scheduled(fixedDelay = 1000)
    public void updateBlockCountToNextRound() {
        try {
            log.info("Updating block count to next round task begin");
            consensusNodeService.updateBlockCountToNextRound();
            log.info("Updating block count to next round task end");
        } catch (Exception e) {
            log.warn("Updating block count to next round failed: {}", e.getMessage());
            log.info("Updating block count to next round again");
            updateNetNodesInfo();
        }
    }
}
