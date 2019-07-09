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

package org.openecomp.sdc.be.model;

import org.junit.Test;


public class PointTest {

	private Point createTestSubject() {
		return new Point();
	}

	@Test
	public void testCtor() throws Exception {
		new Point("mock", "mock");
	}
	
	@Test
	public void testGetX() throws Exception {
		Point testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getX();
	}

	
	@Test
	public void testSetX() throws Exception {
		Point testSubject;
		String x = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setX(x);
	}

	
	@Test
	public void testGetY() throws Exception {
		Point testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getY();
	}

	
	@Test
	public void testSetY() throws Exception {
		Point testSubject;
		String y = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setY(y);
	}

	
	@Test
	public void testToString() throws Exception {
		Point testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}
