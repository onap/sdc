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

package org.openecomp.sdc.be.dao.utils;

import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DaoUtilsTest {

	@Test
	public void testConvertToJson() throws Exception {
		Object object = new Object();
		String result;

		// test 1
		result = DaoUtils.convertToJson(object);
		Assert.assertEquals("{}", result);
		
		assertThatThrownBy(()->DaoUtils.convertToJson(null)).isInstanceOf(RuntimeException.class);
	}

	@Test
	public void testConvertFromJson() throws Exception {
		Class clazz = Object.class;
		String json = null;
		Object result;

		// default test
		result = DaoUtils.convertFromJson(clazz, json);
		Assert.assertEquals(null, result);
		
		try {
			result = DaoUtils.convertFromJson(null, json);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
