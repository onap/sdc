package org.openecomp.sdc.asdctool.impl;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;
import org.openecomp.sdc.be.model.Component;

import java.util.List;
import java.util.Map;

public class ArtifactUuidFixTest {

	private ArtifactUuidFix createTestSubject() {
		return new ArtifactUuidFix();
	}

	@Test(expected=NullPointerException.class)
	public void testDoFix() throws Exception {
		ArtifactUuidFix testSubject;
		String fixComponent = "";
		String runMode = "";
		boolean result;

		// test 1
		testSubject = createTestSubject();
		fixComponent = "vf_only";
		result = testSubject.doFix(fixComponent, runMode);
		Assert.assertEquals(false, result);

		// test 2
		testSubject = createTestSubject();
		runMode = "service_vf";
		result = testSubject.doFix(fixComponent, runMode);
		Assert.assertEquals(false, result);

		// test 3
		testSubject = createTestSubject();
		runMode = "fix";
		result = testSubject.doFix(fixComponent, runMode);
		Assert.assertEquals(false, result);

		// test 4
		testSubject = createTestSubject();
		runMode = "fix";
		result = testSubject.doFix(fixComponent, runMode);
		Assert.assertEquals(false, result);

		// test 5
		testSubject = createTestSubject();
		runMode = "fix_only_services";
		result = testSubject.doFix(fixComponent, runMode);
		Assert.assertEquals(false, result);
	}

	@Test
	public void testDoFixTosca() throws Exception {
		ArtifactUuidFix testSubject;
		Map<String, List<Component>> nodeToFix = null;
		Map<String, List<Component>> vfToFix = null;
		Map<String, List<Component>> serviceToFix = null;
		boolean result;

		// default test
		testSubject = createTestSubject();
	}

	@Test(expected=NullPointerException.class)
	public void testGetVerticesToValidate() throws Exception {
		ArtifactUuidFix testSubject;
		VertexTypeEnum type = null;
		Map<GraphPropertyEnum, Object> hasProps = null;
		Map<String, List<Component>> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVerticesToValidate(type, hasProps);
	}

	@Test(expected=NullPointerException.class)
	public void testValidateTosca() throws Exception {
		ArtifactUuidFix testSubject;
		Map<String, List<Component>> vertices = null;
		Map<String, List<Component>> compToFix = null;
		String name = "";
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.validateTosca(vertices, compToFix, name);
	}
}