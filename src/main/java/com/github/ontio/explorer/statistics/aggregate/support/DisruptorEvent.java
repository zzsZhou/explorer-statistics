package com.github.ontio.explorer.statistics.aggregate.support;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author LiuQi
 */
public class DisruptorEvent implements Serializable {

	public static final EventFactory<DisruptorEvent> FACTORY = DisruptorEvent::new;

	public static final EventHandler<DisruptorEvent> CLEANER = (event, sequence, endOfBatch) -> event.setEvent(null);

	@Getter
	@Setter
	private Object event;

}
