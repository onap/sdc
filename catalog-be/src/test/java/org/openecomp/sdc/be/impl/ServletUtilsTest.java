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

package org.openecomp.sdc.be.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;

import mockit.Deencapsulation;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:application-context-test.xml"})
public class ServletUtilsTest {
	@Autowired
	private ServletUtils servletUtils;

	@Test
	@Ignore("migration to Junit 5 depends on migration to Spring 5")
	public void testCtrServletUtils() {
		// default test
		assertThat(servletUtils)
				.isNotNull()
				.isInstanceOf(ServletUtils.class);

		ComponentsUtils componentsUtils = Deencapsulation.getField(servletUtils, "componentsUtils");
		UserBusinessLogic userBusinessLogic = Deencapsulation.getField(servletUtils, "userAdmin");
		Gson gson = Deencapsulation.getField(servletUtils, "gson");

		assertThat(gson)
				.isNotNull()
				.isInstanceOf(Gson.class)
				.isEqualTo(servletUtils.getGson());
		assertThat(componentsUtils)
				.isNotNull()
				.isInstanceOf(ComponentsUtils.class)
				.isEqualTo(servletUtils.getComponentsUtils());
		assertThat(userBusinessLogic)
				.isNotNull()
				.isInstanceOf(UserBusinessLogic.class)
				.isEqualTo(servletUtils.getUserAdmin());
	}
}
