package com.github.ontio.explorer.statistics.aggregate;

import com.github.ontio.explorer.statistics.aggregate.model.Tick;
import com.github.ontio.explorer.statistics.aggregate.model.TransactionInfo;
import com.github.ontio.explorer.statistics.aggregate.support.DisruptorEventDispatcher;
import com.github.ontio.explorer.statistics.common.ParamsConfig;
import com.github.ontio.explorer.statistics.mapper.CurrentMapper;
import com.github.ontio.explorer.statistics.mapper.TxDetailMapper;
import com.github.ontio.explorer.statistics.model.TxDetail;
import com.google.common.util.concurrent.RateLimiter;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.RowBounds;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * @author LiuQi
 */
@RequiredArgsConstructor
@Component
public class AggregateSourceProducer {

	private static final int BATCH_SIZE = 5000;

	private static final int BLOCK_BATCH_SIZE = 1000;

	private final DisruptorEventDispatcher dispatcher;

	private final TxDetailMapper txDetailMapper;

	private final CurrentMapper currentMapper;

	private final ParamsConfig config;

	private RateLimiter rateLimiter;

	private volatile int startTxDetailId;

	private volatile int startBlockHeight;

	public void produceTransactionInfo() {
		int id = startTxDetailId;

		while (true) {
			Example example = new Example(TxDetail.class);
			example.and().andGreaterThan("id", id).andIn("eventType", Arrays.asList(2, 3));
			example.orderBy("id");
			List<TxDetail> details = txDetailMapper.selectByExampleAndRowBounds(example, new RowBounds(0, BATCH_SIZE));

			if (details == null || details.isEmpty()) {
				break;
			}

			for (TxDetail detail : details) {
				if (rateLimiter != null) {
					rateLimiter.acquire();
				}
				TransactionInfo transactionInfo = TransactionInfo.wrap(detail);
				dispatcher.dispatch(transactionInfo);
				id = detail.getId();
			}
		}

		this.startTxDetailId = id;
	}

	@Scheduled(initialDelay = 5000, fixedRate = 5000)
	public void productTransactionInfoByBlockHeight() {
		Integer latestBlockHeight = txDetailMapper.findLatestBlockHeight();
		if (latestBlockHeight == null) {
			return;
		}
		int blockHeight = this.startBlockHeight;

		while (blockHeight < latestBlockHeight) {
			Example example = new Example(TxDetail.class);
			example.and()
					.andGreaterThan("blockHeight", blockHeight)
					.andLessThanOrEqualTo("blockHeight", blockHeight + BLOCK_BATCH_SIZE)
					.andIn("eventType", Arrays.asList(2, 3));
			example.orderBy("blockHeight").orderBy("blockIndex").orderBy("txIndex");
			List<TxDetail> details = txDetailMapper.selectByExample(example);

			if (details == null || details.isEmpty()) {
				blockHeight += BLOCK_BATCH_SIZE;
				continue;
			}

			for (TxDetail detail : details) {
				if (rateLimiter != null) {
					rateLimiter.acquire();
				}
				TransactionInfo transactionInfo = TransactionInfo.wrap(detail);
				dispatcher.dispatch(transactionInfo);
				blockHeight = detail.getBlockHeight();
			}
		}

		this.startBlockHeight = blockHeight;
	}

	@Scheduled(initialDelay = 5000, fixedRate = 5000)
	public void flushTotalAggregations() {
		dispatcher.dispatch(new Tick(Duration.ofSeconds(5)));
	}

	@PostConstruct
	public void initialize() {
		startBlockHeight = currentMapper.findLastStatBlockHeight();
		Integer id = txDetailMapper.findLastIdBeforeBlockHeight(startBlockHeight + 1);
		if (id != null) {
			this.startTxDetailId = id;
		}
		if (config.getAggregationRateLimit() > 0) {
			this.rateLimiter = RateLimiter.create(config.getAggregationRateLimit());
		}
	}

}
