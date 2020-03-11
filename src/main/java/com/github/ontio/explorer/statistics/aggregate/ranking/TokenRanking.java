package com.github.ontio.explorer.statistics.aggregate.ranking;

import com.github.ontio.explorer.statistics.aggregate.model.TransactionInfo;
import com.github.ontio.explorer.statistics.aggregate.support.UniqueCounter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author LiuQi
 */
@RequiredArgsConstructor
public class TokenRanking extends AbstractRanking {

	@Getter
	private final RankingDuration duration;

	private final String tokenContractHash;

	@Getter
	private int txCount;

	private UniqueCounter<String> depositAddressCounter = new UniqueCounter.SimpleUniqueCounter<>();

	private UniqueCounter<String> withdrawAddressCounter = new UniqueCounter.SimpleUniqueCounter<>();

	private UniqueCounter<String> txAddressCounter = new UniqueCounter.SimpleUniqueCounter<>();

	@Override
	public void doRank(TransactionInfo transactionInfo) {
		String from = transactionInfo.getFromAddress();
		String to = transactionInfo.getToAddress();

		if (isTxHashChanged(transactionInfo)) {
			txCount++;
		}
		depositAddressCounter.count(to);
		withdrawAddressCounter.count(from);
		txAddressCounter.count(from, to);
	}

	public int getDepositAddressCount() {
		return depositAddressCounter.getCount();
	}

	public int getWithdrawAddressCount() {
		return withdrawAddressCounter.getCount();
	}

	public int getTxAddressCount() {
		return txAddressCounter.getCount();
	}

	@Override
	public String getMember() {
		return tokenContractHash;
	}

}
