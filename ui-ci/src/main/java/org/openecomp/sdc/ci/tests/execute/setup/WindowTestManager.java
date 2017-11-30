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

package org.openecomp.sdc.ci.tests.execute.setup;

import java.util.HashMap;

public class WindowTestManager {
	
	private static HashMap<Long, WindowTest> windowMap = new HashMap<Long, WindowTest>();

	public static synchronized WindowTest getWindowMap() {
		Long currentThreadId = Thread.currentThread().getId();
		boolean containsKey = windowMap.containsKey(currentThreadId);
		if (!containsKey){
			setWindowMap(currentThreadId);
		}
		return windowMap.get(currentThreadId);
	}

	private static synchronized void setWindowMap(Long currentThreadId) {
		WindowTestManager.windowMap.put(currentThreadId, new WindowTest());
	}
	
	public static synchronized void removeWindowTest(){
		windowMap.remove(Thread.currentThread().getId());
	}
	
	public static synchronized HashMap<Long, WindowTest> getWholeMap(){
		return windowMap;
	}
	
}
