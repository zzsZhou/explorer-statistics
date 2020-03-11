package com.github.ontio.explorer.statistics.aggregate.ranking;

import com.github.ontio.explorer.statistics.aggregate.AggregateContext;
import com.github.ontio.explorer.statistics.aggregate.model.TransactionInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LiuQi
 */
@RequiredArgsConstructor
@Getter
public class AddressRanking extends AbstractRanking {

	private final AggregateContext context;

	private final RankingDuration duration;

	private final String address;

	private Map<String, InternalRanking> internalRankings = new HashMap<>(2);

	@Getter
	private BigDecimal feeAmount = BigDecimal.ZERO;

	@Getter
	private int txCount;

	@Override
	public void doRank(TransactionInfo transactionInfo) {
		String tokenContractHash = transactionInfo.getContractHash();
		String from = transactionInfo.getFromAddress();
		String to = transactionInfo.getToAddress();

		if (isTxHashChanged(transactionInfo)) {
			txCount++;
		}
		if (context.isNativeContract(tokenContractHash)) {
			InternalRanking internal = internalRankings.computeIfAbsent(tokenContractHash, key -> new InternalRanking());
			if (address.equals(from)) {
				if (isTxHashChanged(transactionInfo)) {
					internal.withdrawTxCount++;
				}
				if (transactionInfo.isGas()) {
					feeAmount = feeAmount.add(transactionInfo.getFee());
				}
				internal.withdrawAmount = internal.withdrawAmount.add(transactionInfo.getAmount());
			} else if (address.equals(to)) {
				if (isTxHashChanged(transactionInfo)) {
					internal.depositTxCount++;
				}
				internal.depositAmount = internal.depositAmount.add(transactionInfo.getAmount());
			}
		}
	}

	@Override
	public String getMember() {
		return address;
	}

	private static class InternalRanking {
		private int depositTxCount;
		private int withdrawTxCount;
		private BigDecimal depositAmount = BigDecimal.ZERO;
		private BigDecimal withdrawAmount = BigDecimal.ZERO;
	}

	public int getDepositTxCount(String contractHash) {
		return internalRankings.containsKey(contractHash) ? internalRankings.get(contractHash).depositTxCount : 0;
	}

	public int getWithdrawTxCount(String contractHash) {
		return internalRankings.containsKey(contractHash) ? internalRankings.get(contractHash).withdrawTxCount : 0;
	}

	public BigDecimal getDepositAmount(String contractHash) {
		return internalRankings.containsKey(contractHash) ? internalRankings.get(contractHash).depositAmount : BigDecimal.ZERO;
	}

	public BigDecimal getWithdrawAmount(String contractHash) {
		return internalRankings.containsKey(contractHash) ? internalRankings.get(contractHash).withdrawAmount : BigDecimal.ZERO;
	}

}
