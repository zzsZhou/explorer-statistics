package com.github.ontio.explorer.statistics.aggregate.ranking;

import com.github.ontio.explorer.statistics.aggregate.model.TransactionInfo;

import java.io.Serializable;

/**
 * @author LiuQi
 */
public interface Ranking extends Serializable {

	default String getMember() {
		throw new UnsupportedOperationException();
	}

	RankingDuration getDuration();

	void rank(TransactionInfo transactionInfo);

}
