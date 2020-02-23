package com.github.ontio.explorer.statistics.aggregate.model;

import com.github.ontio.explorer.statistics.aggregate.AggregateContext;
import com.github.ontio.explorer.statistics.aggregate.support.UniqueCounter;
import com.github.ontio.explorer.statistics.model.ContractDailyAggregation;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Optional;

import static java.math.BigDecimal.ZERO;

/**
 * @author LiuQi
 */
public class ContractAggregate extends AbstractAggregate<ContractAggregate.ContractAggregateKey, ContractDailyAggregation> {

	private int txCount;

	private BigDecimal txAmount;

	private UniqueCounter<String> txAddressCounter;

	private UniqueCounter<String> depositAddressCounter;

	private UniqueCounter<String> withdrawAddressCounter;

	private BigDecimal feeAmount;

	private UniqueCounter<String> contractCounter;

	private transient boolean changed;

	private TotalAggregate total;

	public ContractAggregate(AggregateContext context, ContractAggregateKey key) {
		super(context, key);
	}

	@Override
	protected void aggregateTransfer(TransactionInfo transactionInfo) {
		String tokenContractHash = key().getTokenContractHash();
		if (context.virtualContracts().contains(tokenContractHash)) {
			String from = transactionInfo.getFromAddress();
			String to = transactionInfo.getToAddress();

			this.txCount++;
			this.depositAddressCounter.count(to);
			this.withdrawAddressCounter.count(from);
			this.txAddressCounter.count(from, to);
			this.contractCounter.count(transactionInfo.getContractHash());

			this.total.txCount++;
		} else {
			BigDecimal amount = transactionInfo.getAmount();
			String from = transactionInfo.getFromAddress();
			String to = transactionInfo.getToAddress();

			this.txCount++;
			this.txAmount = this.txAmount.add(amount);
			this.txAddressCounter.count(from, to);

			this.total.txCount++;
			this.total.txAmount = this.total.txAmount.add(amount);
		}
		this.changed = true;
		this.total.changed = true;
	}

	@Override
	protected void aggregateGas(TransactionInfo transactionInfo) {
		total.txCount++;
		total.feeAmount = total.feeAmount.add(transactionInfo.getFee());
		total.changed = true;
		if (context.isVirtualAll(key().getTokenContractHash())) {
			txCount++;
			feeAmount = feeAmount.add(transactionInfo.getFee());
			changed = true;
		}
	}

	@Override
	protected void populateBaseline(ContractDailyAggregation baseline) {
		this.txCount = 0;
		this.txAmount = ZERO;
		this.depositAddressCounter = new UniqueCounter.SimpleUniqueCounter<>();
		this.withdrawAddressCounter = new UniqueCounter.SimpleUniqueCounter<>();
		this.txAddressCounter = new UniqueCounter.SimpleUniqueCounter<>();
		this.feeAmount = ZERO;
		this.contractCounter = new UniqueCounter.SimpleUniqueCounter<>();
		this.changed = false;

		if (this.total != null) {
			this.total.changed = false;
		}
	}

	@Override
	protected void populateTotal(ContractDailyAggregation total) {
		if (this.total == null) {
			this.total = new TotalAggregate();
		}
		if (total != null) {
			this.total.txCount = total.getTxCount();
			this.total.txAmount = total.getTxAmount();
			this.total.feeAmount = total.getFeeAmount();
		}
	}

	@Override
	protected Optional<ContractDailyAggregation> snapshot() {
		if (!changed) {
			return Optional.empty();
		}

		ContractDailyAggregation snapshot = new ContractDailyAggregation();
		snapshot.setContractHash(key().getContractHash());
		snapshot.setTokenContractHash(key().getTokenContractHash());
		snapshot.setDateId(context.getDateId());
		snapshot.setTxCount(txCount);
		snapshot.setTxAmount(txAmount);
		snapshot.setDepositAddressCount(depositAddressCounter.getCount());
		snapshot.setWithdrawAddressCount(withdrawAddressCounter.getCount());
		snapshot.setTxAddressCount(txAddressCounter.getCount());
		snapshot.setFeeAmount(feeAmount);
		snapshot.setContractCount(contractCounter.getCount());
		snapshot.setIsVirtual(context.virtualContracts().contains(key().getTokenContractHash()));
		return Optional.of(snapshot);
	}

	@Override
	protected Optional<ContractDailyAggregation> snapshotTotal() {
		if (!total.changed) {
			return Optional.empty();
		}

		ContractDailyAggregation snapshot = new ContractDailyAggregation();
		snapshot.setContractHash(key().getContractHash());
		snapshot.setTokenContractHash(key().getTokenContractHash());
		snapshot.setDateId(0);
		snapshot.setTxCount(total.txCount);
		snapshot.setTxAmount(total.txAmount);
		snapshot.setDepositAddressCount(0);
		snapshot.setWithdrawAddressCount(0);
		snapshot.setTxAddressCount(0);
		snapshot.setFeeAmount(total.feeAmount);
		snapshot.setContractCount(0);
		snapshot.setIsVirtual(context.virtualContracts().contains(key().getTokenContractHash()));
		return Optional.of(snapshot);
	}

	@RequiredArgsConstructor
	@EqualsAndHashCode
	@Getter
	@ToString
	public static class ContractAggregateKey implements AggregateKey {
		private final String contractHash;
		private final String tokenContractHash;
	}

	private static class TotalAggregate {
		private int txCount;
		private BigDecimal txAmount = ZERO;
		private BigDecimal feeAmount = ZERO;
		private transient boolean changed;
	}

}
