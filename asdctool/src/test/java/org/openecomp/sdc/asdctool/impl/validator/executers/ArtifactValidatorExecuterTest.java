package org.openecomp.sdc.asdctool.impl.validator.executers;

import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ArtifactValidatorExecuterTest {

	private ArtifactValidatorExecuter createTestSubject() {
		return new ArtifactValidatorExecuter();
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

	@Test(expected=NullPointerException.class)
	public void testValidate() throws Exception {
		ArtifactValidatorExecuter testSubject;
		Map<String, List<Component>> vertices = new HashMap<>();
		LinkedList<Component> linkedList = new LinkedList<Component>();
		linkedList.add(new Resource());
		vertices.put("stam", linkedList);
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validate(vertices);
	}
}