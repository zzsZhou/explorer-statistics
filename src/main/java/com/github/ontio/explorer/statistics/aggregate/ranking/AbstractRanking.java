package com.github.ontio.explorer.statistics.aggregate.ranking;

import com.github.ontio.explorer.statistics.aggregate.model.TransactionInfo;

/**
 * @author LiuQi
 */
public abstract class AbstractRanking implements Ranking {

	private transient String lastTxHash;

	@Override
	public void rank(TransactionInfo transactionInfo) {
		doRank(transactionInfo);
		this.lastTxHash = transactionInfo.getTxHash();
	}

	protected abstract void doRank(TransactionInfo transactionInfo);

	protected boolean isTxHashChanged(TransactionInfo transactionInfo) {
		return !transactionInfo.getTxHash().equals(lastTxHash);
	}

}
