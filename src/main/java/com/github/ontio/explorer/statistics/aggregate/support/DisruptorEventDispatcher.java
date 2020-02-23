package com.github.ontio.explorer.statistics.aggregate.support;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author LiuQi
 */
@Component
@Slf4j
public class DisruptorEventDispatcher implements DisruptorEventPublisher, EventHandler<DisruptorEvent> {

	private Map<Class<?>, Collection<DisruptorEventPublisher>> publishers;

	@Getter
	private RingBuffer<DisruptorEvent> ringBuffer;

	public DisruptorEventDispatcher() {
		this.publishers = new HashMap<>();
		Disruptor<DisruptorEvent> disruptor = createDisruptor(131072, ProducerType.MULTI);
		disruptor.handleEventsWith(this).then(DisruptorEvent.CLEANER);
		disruptor.start();
		this.ringBuffer = disruptor.getRingBuffer();
	}

	public <T> void dispatch(T event) {
		publish(event);
	}

	public synchronized void registerPublisher(Class<?> eventType, DisruptorEventPublisher publisher) {
		publishers.computeIfAbsent(eventType, type -> new HashSet<>()).add(publisher);
	}

	@Override
	public void onEvent(DisruptorEvent disruptorEvent, long sequence, boolean endOfBatch) {
		Object event = disruptorEvent.getEvent();
		Class<?> eventType = event.getClass();
		Collection<DisruptorEventPublisher> publishers = this.publishers.get(eventType);
		if (publishers != null) {
			publishers.forEach(publisher -> publisher.publish(event));
		} else {
			log.warn("could not find publisher for event type: {}", eventType);
		}
	}

}
