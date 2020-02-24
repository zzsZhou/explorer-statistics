package com.github.ontio.explorer.statistics.aggregate;

import com.github.ontio.explorer.statistics.aggregate.model.TransactionInfo;
import com.github.ontio.explorer.statistics.aggregate.support.DisruptorEventDispatcher;
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
import java.util.Arrays;
import java.util.List;

/**
 * @author LiuQi
 */
@RequiredArgsConstructor
@Component
public class AggregateSourceProducer {

	private static final int BATCH_SIZE = 5000;

	private final DisruptorEventDispatcher dispatcher;

	private final TxDetailMapper txDetailMapper;

	private final CurrentMapper currentMapper;

	private RateLimiter rateLimiter;

	private volatile int startTxDetailId;

	@Scheduled(initialDelay = 5000, fixedRate = 5000)
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
				rateLimiter.acquire();
				TransactionInfo transactionInfo = TransactionInfo.wrap(detail);
				dispatcher.dispatch(transactionInfo);
				id = detail.getId();
			}
		}

		this.startTxDetailId = id;
	}

	@PostConstruct
	public void initializeStartId() {
		int txTime = currentMapper.findLastStatTxTime();
		Integer id = txDetailMapper.findLastIdBeforeTxTime(txTime);
		if (id != null) {
			this.startTxDetailId = id;
		}
		this.rateLimiter = RateLimiter.create(100.0);
	}

}
