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

package org.openecomp.sdc.be.components.lifecycle;

import org.junit.Test;
import org.openecomp.sdc.be.components.lifecycle.LifecycleChangeInfoWithAction.LifecycleChanceActionEnum;

import static org.assertj.core.api.Assertions.assertThat;

public class LifecycleChangeInfoWithActionTest {

	private LifecycleChangeInfoWithAction createTestSubject() {
		return new LifecycleChangeInfoWithAction();
	}

	@Test
	public void testConstructor() throws Exception {
		LifecycleChangeInfoWithAction obj1 = new LifecycleChangeInfoWithAction("mock");
		LifecycleChangeInfoWithAction obj2 = new LifecycleChangeInfoWithAction("mock", LifecycleChanceActionEnum.CREATE_FROM_CSAR);
		assertThat(obj1).isInstanceOf(LifecycleChangeInfoWithAction.class);
		assertThat(obj2).isInstanceOf(LifecycleChangeInfoWithAction.class);
	}

	@Test
	public void testAction() throws Exception {
		LifecycleChangeInfoWithAction testSubject;
		LifecycleChanceActionEnum action = LifecycleChanceActionEnum.CREATE_FROM_CSAR;

		// default test
		testSubject = createTestSubject();
		testSubject.setAction(action);
		LifecycleChanceActionEnum result = testSubject.getAction();
		assertThat(result).isEqualTo(action);
	}
}
