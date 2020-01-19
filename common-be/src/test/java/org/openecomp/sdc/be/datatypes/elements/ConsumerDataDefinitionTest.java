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
package org.openecomp.sdc.be.datatypes.elements;

import org.junit.Assert;
import org.junit.Test;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.junit.Assert.assertThat;

public class ConsumerDataDefinitionTest {

	private ConsumerDataDefinition createTestSubject() {
		return new ConsumerDataDefinition();
	}

	@Test
	public void shouldHaveValidGettersAndSetters() {
		assertThat(ConsumerDataDefinition.class,
			hasValidGettersAndSettersExcluding("empty", "ownerIdIfEmpty", "type", "version"));
	}
	
	@Test
	public void testEquals() throws Exception {
		ConsumerDataDefinition testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		obj = null;
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
		result = testSubject.equals(testSubject);
		Assert.assertEquals(true, result);
		result = testSubject.equals(new ConsumerDataDefinition(testSubject));
		Assert.assertEquals(true, result);
	}
}
