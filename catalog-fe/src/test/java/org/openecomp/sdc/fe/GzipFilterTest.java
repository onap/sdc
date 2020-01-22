/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.fe;

import org.junit.Test;
import org.openecomp.sdc.fe.filters.GzipFilter;

import javax.servlet.FilterConfig;


public class GzipFilterTest {

	private GzipFilter createTestSubject() {
		return new GzipFilter();
	}

	

	
	@Test
	public void testInit() throws Exception {
		GzipFilter testSubject;
		FilterConfig filterConfig = null;

		// default test
		testSubject = createTestSubject();
		testSubject.init(filterConfig);
	}

	
	@Test
	public void testDestroy() throws Exception {
		GzipFilter testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.destroy();
	}
}
