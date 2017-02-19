/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.utils;

import org.openecomp.sdc.common.api.Constants;

public class CommonBeUtils {
	/**
	 * Compares two ASDC versions of a component. It's for internal usage, so
	 * the assumption is that the versions are in valid format.
	 * 
	 * @param firstVersion
	 *            - version in format major.minor or just major (e.g, 2.0 or 2)
	 * @param secondVersion
	 *            - version in format major.minor or just major (e.g, 2.0 or 2)
	 * @return Returns true iff:<br>
	 *         1) first version's major number is higher than second's (e.g.,
	 *         firstVersion = 1.1, secondVersion = 0.3)<br>
	 *         2) major version are equal, but first's minor version is higher
	 *         than second's (e.g., firstVersion = 0.10, secondVersion = 0.9)
	 *         <br>
	 */
	public static boolean compareAsdcComponentVersions(String firstVersion, String secondVersion) {
		String[] firstVersionNums = firstVersion.split("\\.");
		String[] secondVersionNums = secondVersion.split("\\.");
		int firstMajor = Integer.parseInt(firstVersionNums[0]);
		int secondMajor = Integer.parseInt(secondVersionNums[0]);
		int compareRes = Integer.compare(firstMajor, secondMajor);
		if (compareRes == 0) {
			int firstMinor = (firstVersionNums.length == 2 ? Integer.parseInt(firstVersionNums[1]) : 0);
			int secondMinor = (secondVersionNums.length == 2 ? Integer.parseInt(secondVersionNums[1]) : 0);
			compareRes = Integer.compare(firstMinor, secondMinor);
			return (compareRes > 0);
		} else {
			return (compareRes > 0);
		}
	}

	public static String generateToscaResourceName(String resourceType, String resourceSystemName) {
		return Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX + resourceType.toLowerCase() + "." + resourceSystemName;
	}

}
