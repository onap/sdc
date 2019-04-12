/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdc.be.info;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSetters;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class ServiceVersionInfoTest {

	private static final String NAME = "NAME";
	private static final String VERSION = "VERSION";
	private static final String CONTEXT = "http://localhost:1000/";
	private static final String URL = "http://localhost:1000/services/NAME/VERSION";

	@Test
	public void shouldHaveValidGettersAndSetters() {
		assertThat(ServiceVersionInfo.class, hasValidGettersAndSetters());
	}

	@Test
	public void shouldConstructCorrectObject() {
		ServiceVersionInfo serviceVersionInfo = new ServiceVersionInfo(NAME, VERSION, CONTEXT);
		assertThat(serviceVersionInfo.getUrl(), is(URL));
		assertThat(serviceVersionInfo.getVersion(), is(VERSION));
	}
}