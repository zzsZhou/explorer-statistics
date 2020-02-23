package com.github.ontio.explorer.statistics.aggregate.model;

import com.github.ontio.explorer.statistics.aggregate.support.DateIdUtil;
import com.github.ontio.explorer.statistics.model.TxDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author LiuQi
 */
@Builder
@Getter
@ToString
@AllArgsConstructor
public class TransactionInfo implements Serializable {

	public static final TransactionInfo ONE_DAY_RANK_BEGIN = new TransactionInfo();
	public static final TransactionInfo ONE_DAY_RANK_END = new TransactionInfo();

	public static final TransactionInfo RANK_BEGIN = new TransactionInfo();
	public static final TransactionInfo RANK_END = new TransactionInfo();

	private static final int EVENT_TYPE_TRANSFER = 3;

	private static final int EVENT_TYPE_GAS = 2;

	private TransactionInfo() {
	}

	private String txHash;

	private Integer txType;

	private int txTime;

	private BigDecimal amount;

	private BigDecimal fee;

	private String assetName;

	private String fromAddress;

	private String toAddress;

	private int eventType;

	private String contractHash;

	private String calledContractHash;

	private int dateId;

	public boolean isTransfer() {
		return EVENT_TYPE_TRANSFER == eventType;
	}

	public boolean isGas() {
		return EVENT_TYPE_GAS == eventType;
	}

	public boolean isSelfTransaction() {
		return fromAddress.equals(toAddress);
	}

	public static TransactionInfo wrap(TxDetail txDetail) {
		TransactionInfoBuilder builder = new TransactionInfoBuilder()
				.txHash(txDetail.getTxHash())
				.txType(txDetail.getTxType())
				.txTime(txDetail.getTxTime())
				.amount(txDetail.getAmount())
				.fee(txDetail.getFee())
				.assetName(txDetail.getAssetName())
				.fromAddress(txDetail.getFromAddress())
				.toAddress(txDetail.getToAddress())
				.eventType(txDetail.getEventType())
				.contractHash(txDetail.getContractHash())
				.calledContractHash(txDetail.getCalledContractHash())
				.dateId(DateIdUtil.parseDateId(txDetail.getTxTime()));
		return builder.build();
	}

}
