package com.github.ontio.explorer.statistics.aggregate.support;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

/**
 * @author LiuQi
 */
public abstract class DisruptorEventPublisherAdapter implements DisruptorEventPublisher, EventHandler<DisruptorEvent> {

	@Getter
	private RingBuffer<DisruptorEvent> ringBuffer;

	protected DisruptorEventDispatcher dispatcher;

	protected DisruptorEventPublisherAdapter(int bufferSize, ProducerType producerType) {
		Disruptor<DisruptorEvent> disruptor = createDisruptor(bufferSize, producerType);
		disruptor.handleEventsWith(this);
		disruptor.start();
		this.ringBuffer = disruptor.getRingBuffer();
	}

	protected abstract Collection<Class<?>> eventTypes();

	@Autowired
	public void setDispatcher(DisruptorEventDispatcher dispatcher) {
		Collection<Class<?>> eventTypes = eventTypes();
		if (eventTypes != null) {
			eventTypes.forEach(eventType -> dispatcher.registerPublisher(eventType, this));
		}
		this.dispatcher = dispatcher;
	}

}
