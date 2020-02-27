package com.github.ontio.explorer.statistics.aggregate;

import com.github.ontio.explorer.statistics.aggregate.model.TransactionInfo;
import com.github.ontio.explorer.statistics.aggregate.ranking.CompositeRanking;
import com.github.ontio.explorer.statistics.aggregate.ranking.Ranking;
import com.github.ontio.explorer.statistics.aggregate.ranking.RankingDuration;
import com.github.ontio.explorer.statistics.aggregate.service.RankingService;
import com.github.ontio.explorer.statistics.aggregate.support.DisruptorEvent;
import com.github.ontio.explorer.statistics.aggregate.support.DisruptorEventPublisher;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LiuQi
 */
@Component
@Slf4j
public class TransactionInfoRanker implements DisruptorEventPublisher, EventHandler<DisruptorEvent> {

	private final AggregateContext context;

	private final RankingService rankingService;

	@Getter
	private RingBuffer<DisruptorEvent> ringBuffer;

	private Map<RankingDuration, Ranking> rankings;

	private Map<RankingDuration, Integer> rankingTimeBarrier;

	public TransactionInfoRanker(AggregateContext context, RankingService rankingService) {
		this.context = context;
		this.rankingService = rankingService;
		Disruptor<DisruptorEvent> disruptor = createDisruptor(65536, ProducerType.MULTI);
		disruptor.handleEventsWith(this).then(DisruptorEvent.CLEANER);
		disruptor.start();
		this.ringBuffer = disruptor.getRingBuffer();
		this.rankings = new HashMap<>();
		this.rankingTimeBarrier = new HashMap<>();
	}

	@Override
	public void onEvent(DisruptorEvent disruptorEvent, long sequence, boolean endOfBatch) {
		Object event = disruptorEvent.getEvent();
		try {
			if (event instanceof RankingDuration.Begin) {
				beginRanking((RankingDuration.Begin) event);
			} else if (event instanceof RankingDuration.End) {
				endRanking((RankingDuration.End) event);
			} else if (event instanceof TransactionInfo) {
				doRank((TransactionInfo) event);
			}
		} catch (Exception e) {
			log.error("error processing ranking", e);
		}
	}

	private void doRank(TransactionInfo transactionInfo) {
		rankingTimeBarrier.forEach((duration, txTimeBarrier) -> {
			if (txTimeBarrier <= transactionInfo.getTxTime()) {
				rankings.get(duration).rank(transactionInfo);
			}
		});
	}

	private void beginRanking(RankingDuration.Begin begin) {
		RankingDuration duration = begin.getDuration();
		rankingTimeBarrier.put(duration, begin.getTxTimeBarrier());
		rankings.put(duration, new CompositeRanking(context, duration));
	}

	private void endRanking(RankingDuration.End end) {
		RankingDuration duration = end.getDuration();
		rankingService.saveRanking(rankings.remove(duration));
		rankingTimeBarrier.remove(duration);
	}

}
