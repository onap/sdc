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

package org.openecomp.sdc.asdctool.impl.validator.executers;

import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.testng.Assert;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class ArtifactValidatorExecuterTest {

	private ArtifactValidatorExecuter createTestSubject() {
		JanusGraphDao janusGraphDaoMock = mock(JanusGraphDao.class);
		ToscaOperationFacade toscaOperationFacade = mock(ToscaOperationFacade.class);

		return new ArtifactValidatorExecuter(janusGraphDaoMock, toscaOperationFacade);
	}

	@Test
	public void testGetName() throws Exception {
		ArtifactValidatorExecuter testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test(expected=NullPointerException.class)
	public void testGetVerticesToValidate() throws Exception {
		ArtifactValidatorExecuter testSubject;
		VertexTypeEnum type = null;
		Map<GraphPropertyEnum, Object> hasProps = null;

		// default test
		testSubject = createTestSubject();
		testSubject.getVerticesToValidate(type, hasProps);
	}

	@Test
	public void testSetName() throws Exception {
		ArtifactValidatorExecuter testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	@Test
	public void testValidate() {
		ArtifactValidatorExecuter testSubject;
		Map<String, List<Component>> vertices = new HashMap<>();
		LinkedList<Component> linkedList = new LinkedList<Component>();
		linkedList.add(new Resource());
		vertices.put("stam", linkedList);
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validate(vertices);
		Assert.assertFalse(result);
	}
}
