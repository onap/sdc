package org.openecomp.sdc.be.info;

import mockit.Deencapsulation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.openecomp.sdc.be.model.ArtifactDefinition;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MergedArtifactInfoTest {

	private MergedArtifactInfo createTestSubject() {
		MergedArtifactInfo testSubject = new MergedArtifactInfo();
		testSubject.setJsonArtifactTemplate(new ArtifactTemplateInfo());
		LinkedList<ArtifactDefinition> createdArtifact = new LinkedList<>();
		ArtifactDefinition e = new ArtifactDefinition();
		e.setArtifactName("mock");
		createdArtifact.add(e);
		testSubject.setCreatedArtifact(createdArtifact);
		return testSubject;
	}

	@Test
	public void testGetCreatedArtifact() throws Exception {
		MergedArtifactInfo testSubject;
		List<ArtifactDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreatedArtifact();
	}

	@Test
	public void testSetCreatedArtifact() throws Exception {
		MergedArtifactInfo testSubject;
		List<ArtifactDefinition> createdArtifact = new LinkedList<>();

		// default test
		testSubject = createTestSubject();
		testSubject.setCreatedArtifact(createdArtifact);
	}

	@Test
	public void testGetJsonArtifactTemplate() throws Exception {
		MergedArtifactInfo testSubject;
		ArtifactTemplateInfo result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getJsonArtifactTemplate();
	}

	@Test
	public void testSetJsonArtifactTemplate() throws Exception {
		MergedArtifactInfo testSubject;
		ArtifactTemplateInfo jsonArtifactTemplate = new ArtifactTemplateInfo();

		// default test
		testSubject = createTestSubject();
		testSubject.setJsonArtifactTemplate(jsonArtifactTemplate);
	}

	@Test
	public void testGetListToAssociateArtifactToGroup() throws Exception {
		MergedArtifactInfo testSubject;
		List<ArtifactTemplateInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getListToAssociateArtifactToGroup();
	}

	@Test
	public void testGetListToDissotiateArtifactFromGroup() throws Exception {
		MergedArtifactInfo testSubject;
		List<ArtifactDefinition> deletedArtifacts = new LinkedList<>();
		List<ArtifactDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getListToDissotiateArtifactFromGroup(deletedArtifacts);
	}

	@Test
	public void testGetListToUpdateArtifactInGroup() throws Exception {
		MergedArtifactInfo testSubject;
		List<ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>> result;

		// default test
		testSubject = createTestSubject();
		
		result = testSubject.getListToUpdateArtifactInGroup();
	}

	@Test
	public void testGetUpdateArtifactsInGroup() throws Exception {
		MergedArtifactInfo testSubject;
		List<ImmutablePair<ArtifactDefinition, ArtifactTemplateInfo>> resList = new LinkedList<>();
		List<ArtifactTemplateInfo> jsonArtifacts = new LinkedList<>();

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "getUpdateArtifactsInGroup", resList, resList);
	}

	@Test
	public void testGetNewArtifactsInGroup() throws Exception {
		MergedArtifactInfo testSubject;
		List<ArtifactTemplateInfo> resList = new LinkedList<>();
		List<ArtifactTemplateInfo> jsonArtifacts = new LinkedList<>();

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "getNewArtifactsInGroup", resList, resList);
	}

	@Test
	public void testCreateArtifactsGroupSet() throws Exception {
		MergedArtifactInfo testSubject;
		List<ArtifactTemplateInfo> parsedGroupTemplateList = new LinkedList<>();
		Set<String> parsedArtifactsName = new HashSet<>();

		// default test
		testSubject = createTestSubject();
		Deencapsulation.invoke(testSubject, "createArtifactsGroupSet", parsedGroupTemplateList, parsedArtifactsName);
	}
}