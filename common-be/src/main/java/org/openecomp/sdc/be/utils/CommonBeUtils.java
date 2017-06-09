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
	 * Compares two ASDC versions of a component. It's for internal usage, so the assumption is that the versions are in valid format.
	 * 
	 * @param firstVersion
	 *            - version in format major.minor or just major (e.g, 2.0 or 2)
	 * @param secondVersion
	 *            - version in format major.minor or just major (e.g, 2.0 or 2)
	 * @return Returns true iff:<br>
	 *         1) first version's major number is higher than second's (e.g., firstVersion = 1.1, secondVersion = 0.3)<br>
	 *         2) major version are equal, but first's minor version is higher than second's (e.g., firstVersion = 0.10, secondVersion = 0.9) <br>
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

	
	/**
	 * Compares two version strings. 
	 * 
	 * Use this instead of String.compareTo() for a non-lexicographical 
	 * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
	 * 
	 * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
	 * 
	 * @param str1 a string of ordinal numbers separated by decimal points. 
	 * @param str2 a string of ordinal numbers separated by decimal points.
	 * @return The result is a negative integer if str1 is _numerically_ less than str2. 
	 *         The result is a positive integer if str1 is _numerically_ greater than str2. 
	 *         The result is zero if the strings are _numerically_ equal.
	 */
	public static int conformanceLevelCompare(String str1, String str2) {
	    String[] vals1 = str1.split("\\.");
	    String[] vals2 = str2.split("\\.");
	    int i = 0;
	    // set index to first non-equal ordinal or length of shortest version string
	    while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
	      i++;
	    }
	    // compare first non-equal ordinal number
	    if (i < vals1.length && i < vals2.length) {
	        int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
	        return Integer.signum(diff);
	    }
	    // the strings are equal or one string is a substring of the other
	    // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
	    return Integer.signum(vals1.length - vals2.length);
	}
	
	public static String generateToscaResourceName(String resourceType, String resourceSystemName) {
		return Constants.USER_DEFINED_RESOURCE_NAMESPACE_PREFIX + resourceType.toLowerCase() + "." + resourceSystemName;
	}

}
