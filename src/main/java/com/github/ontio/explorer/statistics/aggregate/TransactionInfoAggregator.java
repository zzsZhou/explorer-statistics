package com.github.ontio.explorer.statistics.aggregate;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.ontio.explorer.statistics.aggregate.model.AddressAggregate.AddressAggregateKey;
import com.github.ontio.explorer.statistics.aggregate.model.Aggregate;
import com.github.ontio.explorer.statistics.aggregate.model.AggregateKey;
import com.github.ontio.explorer.statistics.aggregate.model.AggregateSnapshot;
import com.github.ontio.explorer.statistics.aggregate.model.ContractAggregate.ContractAggregateKey;
import com.github.ontio.explorer.statistics.aggregate.model.StagingAggregateKeys;
import com.github.ontio.explorer.statistics.aggregate.model.Tick;
import com.github.ontio.explorer.statistics.aggregate.model.TokenAggregate.TokenAggregateKey;
import com.github.ontio.explorer.statistics.aggregate.model.TotalAggregationSnapshot;
import com.github.ontio.explorer.statistics.aggregate.model.TransactionInfo;
import com.github.ontio.explorer.statistics.aggregate.service.AggregateService;
import com.github.ontio.explorer.statistics.aggregate.support.DateIdUtil;
import com.github.ontio.explorer.statistics.aggregate.support.DisruptorEvent;
import com.github.ontio.explorer.statistics.aggregate.support.DisruptorEventPublisherAdapter;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.ontio.explorer.statistics.aggregate.AggregateContext.VIRTUAL_CONTRACT_ALL;
import static com.github.ontio.explorer.statistics.aggregate.AggregateContext.VIRTUAL_CONTRACT_NATIVE;
import static com.github.ontio.explorer.statistics.aggregate.AggregateContext.VIRTUAL_CONTRACT_OEP4;

/**
 * @author LiuQi
 */
@Component
@Slf4j
public class TransactionInfoAggregator extends DisruptorEventPublisherAdapter {

	private final AggregateContext context;

	private final AggregateService aggregateService;

	private final AggregationSinker aggregationSinker;
	//当天最新的统计信息
	private Map<AggregateKey, Aggregate<?, ?>> currentAggregates;
	//缓存前一天的统计信息（防止0点左右baseline过期且数据还未插入db，此时数据不准，可能是前两天的数据）
	private Map<AggregateKey, Aggregate<?, ?>> stagingAggregates;
	//DB中前一天的统计信息
	private LoadingCache<AggregateKey, Aggregate<?, ?>> baselineAggregates;

	private int aggregated;

	public TransactionInfoAggregator(AggregateContext context, AggregateService aggregateService,
			AggregationSinker aggregationSinker) {
		super(131072, ProducerType.SINGLE);
		this.context = context;
		this.aggregateService = aggregateService;
		this.aggregationSinker = aggregationSinker;
	}

	@Override
	protected Collection<Class<?>> eventTypes() {
		return Arrays.asList(TransactionInfo.class, StagingAggregateKeys.class, Tick.class);
	}

	@Override
	public void onEvent(DisruptorEvent disruptorEvent, long sequence, boolean endOfBatch) {
		Object event = disruptorEvent.getEvent();
		if (event instanceof TransactionInfo) {
			//记录当天统计数据
			aggregate((TransactionInfo) event);
		} else if (event instanceof StagingAggregateKeys) {
			//清除缓存stagingAggregates里的该key对应的统计数据，在数据插入db完成时触发
			StagingAggregateKeys keys = (StagingAggregateKeys) event;
			log.info("clearing staging aggregates");
			keys.getAggregateKeys().forEach(this.stagingAggregates::remove);
		} else if (event instanceof Tick) {
			//定时刷新汇总统计数据到db
			flushTotalAggregations();
		}
	}

	private void aggregate(TransactionInfo transactionInfo) {
		int currentDateId = context.getDateId();
		if (transactionInfo.getDateId() < currentDateId) {
			log.warn("received overdue transaction info: {}", transactionInfo);
			return;
		}
		log.debug("aggregating transaction info: {}", transactionInfo);

		//0点之后的第一次跑批触发，插入前一天的统计数据
		if (transactionInfo.getDateId() > currentDateId) {
			if (currentDateId != 0) {
				sinkSnapshot();
			}
			context.setDateId(transactionInfo.getDateId());
		}
		//记录统计信息到currentAggregates
		selectAggregateKeys(transactionInfo).forEach(key -> {
			Aggregate<?, ?> aggregate = getAggregate(key);
			aggregate.aggregate(transactionInfo);
		});

		context.setBlockHeight(transactionInfo.getBlockHeight());

		if (++this.aggregated % 1000 == 0) {
			log.info("{} transactions have been aggregated", this.aggregated);
		}
	}

	private void sinkSnapshot() {
		if (log.isInfoEnabled()) {
			log.info("sinking aggregations of date {}", DateIdUtil.toDateString(context.getDateId()));
		}
		//将当天统计数据放入缓存stagingAggregates
		currentAggregates.forEach((key, aggregate) -> stagingAggregates.put(key, aggregate));
		AggregateSnapshot snapshot = new AggregateSnapshot(context.getDateId(), context.getBlockHeight());
		snapshot.append(currentAggregates.values());
		//清空当天的统计数据
		currentAggregates = new HashMap<>();
		aggregationSinker.sink(snapshot);
	}

	private Aggregate<?, ?> getAggregate(AggregateKey key) {
		return currentAggregates.computeIfAbsent(key, k -> {
			Aggregate<?, ?> aggregate = stagingAggregates.containsKey(k) ? stagingAggregates.remove(k) : baselineAggregates.get(k);
			aggregate.rebase();// TODO tuning
			return aggregate;
		});
	}

	private Collection<AggregateKey> selectAggregateKeys(TransactionInfo transactionInfo) {
		List<AggregateKey> keys = new ArrayList<>();
		keys.addAll(selectAddressAggregateKeys(transactionInfo));
		keys.addAll(selectTokenAggregateKey(transactionInfo));
		keys.addAll(selectContractAggregateKey(transactionInfo));
		return keys;
	}

	private Collection<AggregateKey> selectAddressAggregateKeys(TransactionInfo transactionInfo) {
		List<String> addresses = new ArrayList<>();
		addresses.add(transactionInfo.getFromAddress());
		if (!transactionInfo.isSelfTransaction()) {
			addresses.add(transactionInfo.getToAddress());
		}
		List<String> tokenContractHashes = selectTokenContractHashes(transactionInfo.getContractHash());

		List<AggregateKey> keys = new ArrayList<>(8);
		tokenContractHashes.forEach(hash -> addresses.forEach(address -> keys.add(new AddressAggregateKey(address, hash))));
		return keys;
	}

	private Collection<AggregateKey> selectTokenAggregateKey(TransactionInfo transactionInfo) {
		return Collections.singleton(new TokenAggregateKey(transactionInfo.getContractHash()));
	}

	private Collection<AggregateKey> selectContractAggregateKey(TransactionInfo transactionInfo) {
		String contractHash = transactionInfo.getCalledContractHash();
		List<String> tokenContractHashes = selectTokenContractHashes(transactionInfo.getContractHash());

		List<AggregateKey> keys = new ArrayList<>(4);
		tokenContractHashes.forEach(hash -> keys.add(new ContractAggregateKey(contractHash, hash)));
		return keys;
	}

	private List<String> selectTokenContractHashes(String contractHash) {
		List<String> tokenContractHashes = new ArrayList<>();
		tokenContractHashes.add(contractHash);
		tokenContractHashes.add(VIRTUAL_CONTRACT_ALL);
		if (context.isNativeContract(contractHash)) {
			tokenContractHashes.add(VIRTUAL_CONTRACT_NATIVE);
		}
		if (context.isOep4Contract(contractHash)) {
			tokenContractHashes.add(VIRTUAL_CONTRACT_OEP4);
		}
		return tokenContractHashes;
	}

	private void flushTotalAggregations() {
		int currentDateId = DateIdUtil.parseDateId((int) (System.currentTimeMillis() / 1000));
		if (currentDateId == context.getDateId() && !currentAggregates.isEmpty()) {
			TotalAggregationSnapshot snapshot = new TotalAggregationSnapshot();
			snapshot.append(currentAggregates.values());
			aggregationSinker.sink(snapshot);
		}
	}

	@PostConstruct
	public void init() {
		currentAggregates = new HashMap<>(8192);
		stagingAggregates = new HashMap<>(8192);
		baselineAggregates = Caffeine.newBuilder()
				.maximumSize(32768)
				.expireAfterAccess(Duration.ofHours(1))
				.build(aggregateService::createBaselineAggregate);
	}

}
