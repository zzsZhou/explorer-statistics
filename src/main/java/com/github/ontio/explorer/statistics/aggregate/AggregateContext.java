package com.github.ontio.explorer.statistics.aggregate;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.ontio.explorer.statistics.aggregate.model.ContractType;
import com.github.ontio.explorer.statistics.common.ParamsConfig;
import com.github.ontio.explorer.statistics.mapper.ContractMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author LiuQi
 */
@Component
@RequiredArgsConstructor
public class AggregateContext {

	/**
	 * 虚拟合约，表示对所有合约加总的统计
	 */
	public static final String VIRTUAL_CONTRACT_ALL = "$$ALL$$";

	/**
	 * 虚拟合约，表示对 ONT 和 ONG 加总的统计
	 */
	public static final String VIRTUAL_CONTRACT_NATIVE = "$$NATIVE$$";

	/**
	 * 虚拟合约，表示对所有 OEP4 合约加总的统计
	 */
	public static final String VIRTUAL_CONTRACT_OEP4 = "$$OEP4$$";

	private static final Collection<String> VIRTUAL_CONTRACTS;

	static {
		Set<String> virtualContracts = new HashSet<>(Arrays.asList(VIRTUAL_CONTRACT_ALL, VIRTUAL_CONTRACT_NATIVE,
				VIRTUAL_CONTRACT_OEP4));
		VIRTUAL_CONTRACTS = Collections.unmodifiableSet(virtualContracts);
	}

	@Getter
	@Setter
	private int dateId;

	@Getter
	@Setter
	private int blockHeight;

	private final ContractMapper contractMapper;

	@Getter
	private final ParamsConfig config;

	private LoadingCache<String, ContractType> contractTypes;

	public Collection<String> virtualContracts() {
		return VIRTUAL_CONTRACTS;
	}

	public boolean isVirtualAll(String contractHash) {
		return VIRTUAL_CONTRACT_ALL.equals(contractHash);
	}

	public boolean isVirtualNative(String contractHash) {
		return VIRTUAL_CONTRACT_NATIVE.equals(contractHash);
	}

	public boolean isVirtualOep4(String contractHash) {
		return VIRTUAL_CONTRACT_OEP4.equals(contractHash);
	}

	public boolean isNativeContract(String contractHash) {
		return isOnt(contractHash) || isOng(contractHash);
	}

	public boolean isOnt(String contractHash) {
		return config.getOntContractHash().equals(contractHash);
	}

	public boolean isOng(String contractHash) {
		return config.getOngContractHash().equals(contractHash);
	}

	public boolean isOep4Contract(String contractHash) {
		return contractTypes.get(contractHash).isOep4();
	}

	public boolean isOep5Contract(String contractHash) {
		return contractTypes.get(contractHash).isOep5();
	}

	public boolean isOep8Contract(String contractHash) {
		return contractTypes.get(contractHash).isOep8();
	}

	@PostConstruct
	public void init() {
		contractTypes = Caffeine.newBuilder()
				.maximumSize(4096)
				.expireAfterAccess(Duration.ofHours(1))
				.build(contractHash -> {
					ContractType contractType = contractMapper.findContractType(contractHash);
					return contractType == null ? ContractType.NULL : contractType;
				});
	}

}
