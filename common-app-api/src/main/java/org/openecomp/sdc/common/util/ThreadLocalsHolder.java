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

package org.openecomp.sdc.common.util;

import org.openecomp.sdc.common.api.FilterDecisionEnum;
import org.openecomp.sdc.common.datastructure.UserContext;

public class ThreadLocalsHolder {

	private static final ThreadLocal<String> uuidThreadLocal = new ThreadLocal<>();
	private static final ThreadLocal<Long> requestStartTimeThreadLocal = new ThreadLocal<>();
	private static final ThreadLocal<FilterDecisionEnum> apiType = new ThreadLocal<>();
	private static final ThreadLocal<Boolean> isMdcProcessedThreadLocal = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return false;
		}
	};


	private static final ThreadLocal<UserContext> userContextThreadLocal = new ThreadLocal<>();

	public static final UserContext getUserContext() {return userContextThreadLocal.get();	}

	public static void setUserContext(UserContext userContext) {userContextThreadLocal.set(userContext);	}

	public static void setMdcProcessed(Boolean isMdcProcessed) {
		isMdcProcessedThreadLocal.set(isMdcProcessed);
	}

	public static void setUuid(String uuid) {
		uuidThreadLocal.set(uuid);
	}

	public static void setRequestStartTime(Long requestStartTime) {
		requestStartTimeThreadLocal.set(requestStartTime);
	}

	public static String getUuid() {
		return uuidThreadLocal.get();
	}

	public static Long getRequestStartTime() {
		return requestStartTimeThreadLocal.get();
	}

	public static Boolean isMdcProcessed() {
		return isMdcProcessedThreadLocal.get();
	}

	public static void cleanup() {
		uuidThreadLocal.remove();
		requestStartTimeThreadLocal.remove();
		isMdcProcessedThreadLocal.remove();
		userContextThreadLocal.remove();
		apiType.remove();
	}

	public static FilterDecisionEnum getApiType() {
		return apiType.get();
	}
	public static void setApiType(FilterDecisionEnum filterDecisionEnum) {
		apiType.set(filterDecisionEnum);
	}


    public static boolean isInternalRequest() {
		if (getApiType().equals(FilterDecisionEnum.INTERNAL)){
			return true;
		} else return false;
    }

	public static boolean isExternalRequest() {
		if (getApiType().equals(FilterDecisionEnum.EXTERNAL)){
			return true;
		} else return false;
	}
}
