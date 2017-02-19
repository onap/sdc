package org.openecomp.sdc.uici.tests.datatypes;

import java.util.Arrays;
import java.util.Optional;

/**
 * enum that represents possible methods to clean DB before and after tests.
 * 
 * @author mshitrit
 *
 */
public enum CleanTypeEnum {
	FULL,
	/** Unreliable should be only used in dev **/
	PARTIAL, NONE;

	/**
	 * Returns CleanType enum by it name
	 * 
	 * @param cleanType
	 * @return
	 */
	public static CleanTypeEnum findByName(String cleanType) {
		final Optional<CleanTypeEnum> findAny = Arrays.asList(CleanTypeEnum.values()).stream()
				.filter(e -> e.name().equals(cleanType)).findAny();
		return findAny.get();
	}
}
