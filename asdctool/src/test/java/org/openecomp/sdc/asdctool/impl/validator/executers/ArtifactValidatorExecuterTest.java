package org.openecomp.sdc.asdctool.impl.validator.executers;

import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.testng.Assert;

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