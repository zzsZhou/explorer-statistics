package com.github.ontio.explorer.statistics.aggregate;

import com.github.ontio.explorer.statistics.aggregate.model.AggregateSnapshot;
import com.github.ontio.explorer.statistics.aggregate.model.StagingAggregateKeys;
import com.github.ontio.explorer.statistics.aggregate.model.TotalAggregationSnapshot;
import com.github.ontio.explorer.statistics.aggregate.service.AggregateService;
import com.github.ontio.explorer.statistics.aggregate.support.DateIdUtil;
import com.github.ontio.explorer.statistics.aggregate.support.DisruptorEvent;
import com.github.ontio.explorer.statistics.aggregate.support.DisruptorEventDispatcher;
import com.github.ontio.explorer.statistics.aggregate.support.DisruptorEventPublisher;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author LiuQi
 */
@Component
@Slf4j
public class AggregationSinker implements DisruptorEventPublisher, EventHandler<DisruptorEvent> {

	private final AggregateService aggregateService;

	private final DisruptorEventDispatcher dispatcher;

	@Getter
	private RingBuffer<DisruptorEvent> ringBuffer;

	public AggregationSinker(AggregateService aggregateService, DisruptorEventDispatcher dispatcher) {
		this.aggregateService = aggregateService;
		this.dispatcher = dispatcher;
		Disruptor<DisruptorEvent> disruptor = createDisruptor(32, ProducerType.SINGLE);
		disruptor.handleEventsWith(this).then(DisruptorEvent.CLEANER);
		disruptor.start();
		this.ringBuffer = disruptor.getRingBuffer();
	}

	public <T> void sink(T event) {
		publish(event);
	}

	@Override
	public void onEvent(DisruptorEvent disruptorEvent, long sequence, boolean endOfBatch) {
		Object event = disruptorEvent.getEvent();
		try {
			if (event instanceof AggregateSnapshot) {
				//记录前一天的统计数据信息
				persistAggregations((AggregateSnapshot) event);
			} else if (event instanceof TotalAggregationSnapshot) {
				//刷新汇总统计信息
				flushTotalAggregations((TotalAggregationSnapshot) event);
			}
		} catch (Exception e) {
			log.error("error saving/flushing aggregations", e);
		}
	}

	private void persistAggregations(AggregateSnapshot snapshot) {
		String date = DateIdUtil.toDateString(snapshot.getCurrentDateId());
		if (log.isInfoEnabled()) {
			log.info("saving aggregations of date {}", date);
		}
		aggregateService.saveAggregateSnapshot(snapshot);
		//分发该key，说明该key对应的数据已经存入db
		dispatcher.dispatch(new StagingAggregateKeys(snapshot.getAggregateKeys()));
		log.info("saved {} address aggregations, {} token aggregations, {} contract aggregations of date {}",
				snapshot.getAddressAggregations().size(), snapshot.getTokenAggregations().size(),
				snapshot.getContractAggregations().size(), date);
	}

	private void flushTotalAggregations(TotalAggregationSnapshot snapshot) {
		log.info("flushing current total aggregations");
		aggregateService.saveTotalAggregationSnapshot(snapshot);
		log.info("flushed {} total address aggregations, {} total token aggregations, {} total contract aggregations",
				snapshot.getAddressAggregations().size(), snapshot.getTokenAggregations().size(),
				snapshot.getContractAggregations().size());
	}

}
