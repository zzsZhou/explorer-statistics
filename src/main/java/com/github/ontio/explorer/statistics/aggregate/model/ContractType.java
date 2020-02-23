package com.github.ontio.explorer.statistics.aggregate.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author LiuQi
 */
@Getter
@Setter
public class ContractType implements Serializable {
	
	private String contractHash;
	
	private boolean oep4;
	
	private boolean oep5;
	
	private boolean oep8;
	
}
