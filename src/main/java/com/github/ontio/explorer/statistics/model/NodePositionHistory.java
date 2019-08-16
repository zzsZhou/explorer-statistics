package com.github.ontio.explorer.statistics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tbl_node_position_history")
public class NodePositionHistory {
    @Id
    @Column(name = "public_key")
    @GeneratedValue(generator = "JDBC")
    private String publicKey;

    @Id
    @Column(name = "block_height")
    private Long blockHeight;

    private String address;

    private String name;

    @Column(name = "node_rank")
    private Integer nodeRank;

    public NodePositionHistory(NodeInfoOnChain nodeInfoOnChain, long blockHeight) {
        this.publicKey = nodeInfoOnChain.getPublicKey();
        this.blockHeight = blockHeight;
        this.address = nodeInfoOnChain.getAddress();
        this.name = nodeInfoOnChain.getName();
        this.nodeRank = nodeInfoOnChain.getNodeRank();
    }

}