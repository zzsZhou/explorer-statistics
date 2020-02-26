package com.github.ontio.explorer.statistics.aggregate.ranking.group;

import com.github.ontio.explorer.statistics.aggregate.ranking.AddressRanking;
import com.github.ontio.explorer.statistics.aggregate.ranking.TokenRanking;
import com.github.ontio.explorer.statistics.common.ParamsConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.github.ontio.explorer.statistics.aggregate.ranking.group.RankingGrouper.RANKING_GROUP_ADDRESS;
import static com.github.ontio.explorer.statistics.aggregate.ranking.group.RankingGrouper.RANKING_GROUP_TOKEN;

/**
 * @author LiuQi
 */
@Configuration
public class RankingGroupers {

	@Bean
	RankingGrouper<AddressRanking> ontDepositTxCountGrouper(ParamsConfig config) {
		return new IntRankingGrouper<>(config, RANKING_GROUP_ADDRESS, RankingGrouper.RANKING_ID_ONT_DEPOSIT_COUNT,
				ranking -> ranking.getDepositTxCount(config.getOntContractHash()));
	}

	@Bean
	RankingGrouper<AddressRanking> ontWithdrawTxCountGrouper(ParamsConfig config) {
		return new IntRankingGrouper<>(config, RANKING_GROUP_ADDRESS, RankingGrouper.RANKING_ID_ONT_WITHDRAW_COUNT,
				ranking -> ranking.getWithdrawTxCount(config.getOntContractHash()));
	}

	@Bean
	RankingGrouper<AddressRanking> ontDepositAmountGrouper(ParamsConfig config) {
		return new BigDecimalRankingGrouper<>(config, RANKING_GROUP_ADDRESS, RankingGrouper.RANKING_ID_ONT_DEPOSIT_AMOUNT,
				ranking -> ranking.getDepositAmount(config.getOntContractHash()));
	}

	@Bean
	RankingGrouper<AddressRanking> ontWithdrawAmountGrouper(ParamsConfig config) {
		return new BigDecimalRankingGrouper<>(config, RANKING_GROUP_ADDRESS, RankingGrouper.RANKING_ID_ONT_WITHDRAW_AMOUNT,
				ranking -> ranking.getWithdrawAmount(config.getOntContractHash()));
	}

	@Bean
	RankingGrouper<AddressRanking> ongDepositTxCountGrouper(ParamsConfig config) {
		return new IntRankingGrouper<>(config, RANKING_GROUP_ADDRESS, RankingGrouper.RANKING_ID_ONG_DEPOSIT_COUNT,
				ranking -> ranking.getDepositTxCount(config.getOngContractHash()));
	}

	@Bean
	RankingGrouper<AddressRanking> ongWithdrawTxCountGrouper(ParamsConfig config) {
		return new IntRankingGrouper<>(config, RANKING_GROUP_ADDRESS, RankingGrouper.RANKING_ID_ONG_WITHDRAW_COUNT,
				ranking -> ranking.getWithdrawTxCount(config.getOngContractHash()));
	}

	@Bean
	RankingGrouper<AddressRanking> ongDepositAmountGrouper(ParamsConfig config) {
		return new BigDecimalRankingGrouper<>(config, RANKING_GROUP_ADDRESS, RankingGrouper.RANKING_ID_ONG_DEPOSIT_AMOUNT,
				ranking -> ranking.getDepositAmount(config.getOngContractHash()));
	}

	@Bean
	RankingGrouper<AddressRanking> ongWithdrawAmountGrouper(ParamsConfig config) {
		return new BigDecimalRankingGrouper<>(config, RANKING_GROUP_ADDRESS, RankingGrouper.RANKING_ID_ONG_WITHDRAW_AMOUNT,
				ranking -> ranking.getWithdrawAmount(config.getOngContractHash()));
	}

	@Bean
	RankingGrouper<AddressRanking> addressTxCountGrouper(ParamsConfig config) {
		return new IntRankingGrouper<>(config, RANKING_GROUP_ADDRESS, RankingGrouper.RANKING_ID_TX_COUNT,
				AddressRanking::getTxCount);
	}

	@Bean
	RankingGrouper<AddressRanking> addressFeeGrouper(ParamsConfig config) {
		return new BigDecimalRankingGrouper<>(config, RANKING_GROUP_ADDRESS, RankingGrouper.RANKING_ID_FEE_AMOUNT,
				AddressRanking::getFeeAmount);
	}

	// token rankings
	@Bean
	RankingGrouper<TokenRanking> tokenTxCountGrouper(ParamsConfig config) {
		return new IntRankingGrouper<>(config, RANKING_GROUP_TOKEN, RankingGrouper.RANKING_ID_TX_COUNT, TokenRanking::getTxCount);
	}

	@Bean
	RankingGrouper<TokenRanking> tokenDepositAddressCountGrouper(ParamsConfig config) {
		return new IntRankingGrouper<>(config, RANKING_GROUP_TOKEN, RankingGrouper.RANKING_ID_DEPOSIT_ADDR,
				TokenRanking::getDepositAddressCount, true);
	}

	@Bean
	RankingGrouper<TokenRanking> tokenWithdrawAddressCountGrouper(ParamsConfig config) {
		return new IntRankingGrouper<>(config, RANKING_GROUP_TOKEN, RankingGrouper.RANKING_ID_DEPOSIT_ADDR,
				TokenRanking::getWithdrawAddressCount, true);
	}

	@Bean
	RankingGrouper<TokenRanking> tokenTxAddressCountGrouper(ParamsConfig config) {
		return new IntRankingGrouper<>(config, RANKING_GROUP_TOKEN, RankingGrouper.RANKING_ID_TX_ADDR,
				TokenRanking::getTxAddressCount, true);
	}

}
