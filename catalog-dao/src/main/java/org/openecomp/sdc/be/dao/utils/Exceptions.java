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

package org.openecomp.sdc.be.dao.utils;

public final class Exceptions {
	private Exceptions() {
	}

	public static RuntimeException convertToRuntimeEx(Throwable t) {
		return Exceptions.<RuntimeException>convertToRTException(t);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> T convertToRTException(Throwable t) throws T {
		throw (T) t;
	}
}
