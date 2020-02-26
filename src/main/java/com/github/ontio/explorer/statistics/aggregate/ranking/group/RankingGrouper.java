package com.github.ontio.explorer.statistics.aggregate.ranking.group;

import com.github.ontio.explorer.statistics.aggregate.ranking.Ranking;
import com.github.ontio.explorer.statistics.model.TRanking;

import java.util.List;

/**
 * @author LiuQi
 */
public interface RankingGrouper<R extends Ranking> {

	/**
	 * 排名分组：地址排名
	 */
	short RANKING_GROUP_ADDRESS = 1;
	/**
	 * 排名分组：Token 排名
	 */
	short RANKING_GROUP_TOKEN = 2;

	/**
	 * ONT 入金交易数量
	 */
	short RANKING_ID_ONT_DEPOSIT_COUNT = 1;
	/**
	 * ONT 出金交易数量
	 */
	short RANKING_ID_ONT_WITHDRAW_COUNT = 2;
	/**
	 * ONT 入金交易金额
	 */
	short RANKING_ID_ONT_DEPOSIT_AMOUNT = 3;
	/**
	 * ONT 出金交易金额
	 */
	short RANKING_ID_ONT_WITHDRAW_AMOUNT = 4;

	/**
	 * ONG 入金交易数量
	 */
	short RANKING_ID_ONG_DEPOSIT_COUNT = 5;
	/**
	 * ONG 出金交易数量
	 */
	short RANKING_ID_ONG_WITHDRAW_COUNT = 6;
	/**
	 * ONG 入金交易金额
	 */
	short RANKING_ID_ONG_DEPOSIT_AMOUNT = 7;
	/**
	 * ONG 出金交易金额
	 */
	short RANKING_ID_ONG_WITHDRAW_AMOUNT = 8;

	/**
	 * 总交易数量
	 */
	short RANKING_ID_TX_COUNT = 9;
	/**
	 * 去重入金地址数
	 */
	short RANKING_ID_DEPOSIT_ADDR = 10;
	/**
	 * 去重出金地址数
	 */
	short RANKING_ID_WITHDRAW_ADDR = 11;
	/**
	 * 去重交易地址数
	 */
	short RANKING_ID_TX_ADDR = 12;

	/**
	 * 消耗手续费金额
	 */
	short RANKING_ID_FEE_AMOUNT = 13;

	short getRankingGroup();

	short getRankingId();

	List<TRanking> group(R[] rankings);

}
