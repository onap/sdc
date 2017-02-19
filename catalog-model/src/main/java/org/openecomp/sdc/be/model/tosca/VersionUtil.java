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

package org.openecomp.sdc.be.model.tosca;

import java.util.regex.Pattern;

import org.openecomp.sdc.be.model.tosca.version.ApplicationVersionException;
import org.openecomp.sdc.be.model.tosca.version.Version;

public final class VersionUtil {

	/** Utility class should not have public constructor. */
	private VersionUtil() {
	}

	/**
	 * The version must begin with a bloc of numbers, and then it can have one
	 * or more bloc of numbers separated by '.' and then it can have alpha
	 * numeric bloc separated by '.' or '-'
	 */
	public static final Pattern VERSION_PATTERN = Pattern.compile("\\d+(?:\\.\\d+)*(?:[\\.-]\\p{Alnum}+)*");
	private static final String SNAPSHOT_IDENTIFIER = "SNAPSHOT";

	/**
	 * Check if a version is a SNAPSHOT (development) version.
	 * 
	 * @param version
	 *            The actual version string.
	 * @return True if the version is a SNAPSHOT version, false if not (RELEASE
	 *         version).
	 */
	public static boolean isSnapshot(String version) {
		return version.toUpperCase().contains(SNAPSHOT_IDENTIFIER);
	}

	/**
	 * Check if a version is valid
	 * 
	 * @param version
	 *            version string to parse
	 * @return true if it's following the defined version pattern
	 */
	public static boolean isValid(String version) {
		return VERSION_PATTERN.matcher(version).matches();
	}

	/**
	 * Parse the version's text to produce a comparable version object
	 * 
	 * @param version
	 *            version text to parse
	 * @return a comparable version object
	 * @throws ApplicationVersionException
	 *             if the version text is not following the defined version
	 *             pattern
	 */
	public static Version parseVersion(String version) {
		if (!isValid(version)) {
			throw new ApplicationVersionException(
					"This version is not valid [" + version + "] as it does not match [" + VERSION_PATTERN + "]");
		} else {
			return new Version(version);
		}
	}

	/**
	 * Compare 2 versions
	 * 
	 * @param versionLeft
	 * @param versionRight
	 * @return
	 */
	public static int compare(String versionLeft, String versionRight) {
		return parseVersion(versionLeft).compareTo(parseVersion(versionRight));
	}
}
