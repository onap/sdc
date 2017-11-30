package org.openecomp.sdc.be.datatypes.elements;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;


public class ArtifactDataDefinitionTest {

	private ArtifactDataDefinition createTestSubject() {
		return new ArtifactDataDefinition();
	}

	
	@Test
	public void testGetArtifactName() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactName();
	}

	
	@Test
	public void testGetArtifactType() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactType();
	}

	
	@Test
	public void testSetArtifactType() throws Exception {
		ArtifactDataDefinition testSubject;
		String artifactType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactType(artifactType);
	}

	
	@Test
	public void testGetArtifactRef() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactRef();
	}

	
	@Test
	public void testSetArtifactRef() throws Exception {
		ArtifactDataDefinition testSubject;
		String artifactRef = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactRef(artifactRef);
	}

	
	@Test
	public void testGetArtifactRepository() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactRepository();
	}

	
	@Test
	public void testSetArtifactRepository() throws Exception {
		ArtifactDataDefinition testSubject;
		String artifactRepository = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactRepository(artifactRepository);
	}

	
	@Test
	public void testSetArtifactName() throws Exception {
		ArtifactDataDefinition testSubject;
		String artifactName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactName(artifactName);
	}

	
	@Test
	public void testGetArtifactChecksum() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactChecksum();
	}

	
	@Test
	public void testSetArtifactChecksum() throws Exception {
		ArtifactDataDefinition testSubject;
		String artifactChecksum = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactChecksum(artifactChecksum);
	}

	
	@Test
	public void testGetUserIdCreator() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUserIdCreator();
	}

	
	@Test
	public void testSetUserIdCreator() throws Exception {
		ArtifactDataDefinition testSubject;
		String userIdCreator = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUserIdCreator(userIdCreator);
	}

	
	@Test
	public void testGetUserIdLastUpdater() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUserIdLastUpdater();
	}

	
	@Test
	public void testSetUserIdLastUpdater() throws Exception {
		ArtifactDataDefinition testSubject;
		String userIdLastUpdater = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUserIdLastUpdater(userIdLastUpdater);
	}

	
	@Test
	public void testGetCreatorFullName() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreatorFullName();
	}

	
	@Test
	public void testSetCreatorFullName() throws Exception {
		ArtifactDataDefinition testSubject;
		String creatorFullName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCreatorFullName(creatorFullName);
	}

	
	@Test
	public void testGetUpdaterFullName() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUpdaterFullName();
	}

	
	@Test
	public void testSetUpdaterFullName() throws Exception {
		ArtifactDataDefinition testSubject;
		String updaterFullName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUpdaterFullName(updaterFullName);
	}

	
	@Test
	public void testGetCreationDate() throws Exception {
		ArtifactDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreationDate();
	}

	
	@Test
	public void testSetCreationDate() throws Exception {
		ArtifactDataDefinition testSubject;
		Long creationDate = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setCreationDate(creationDate);
	}

	
	@Test
	public void testGetLastUpdateDate() throws Exception {
		ArtifactDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastUpdateDate();
	}

	
	@Test
	public void testSetLastUpdateDate() throws Exception {
		ArtifactDataDefinition testSubject;
		Long lastUpdateDate = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setLastUpdateDate(lastUpdateDate);
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testSetUniqueId() throws Exception {
		ArtifactDataDefinition testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testSetDescription() throws Exception {
		ArtifactDataDefinition testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	
	@Test
	public void testGetArtifactLabel() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactLabel();
	}

	
	@Test
	public void testSetArtifactLabel() throws Exception {
		ArtifactDataDefinition testSubject;
		String artifactLabel = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactLabel(artifactLabel);
	}

	
	@Test
	public void testGetEsId() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getEsId();
	}

	
	@Test
	public void testSetEsId() throws Exception {
		ArtifactDataDefinition testSubject;
		String esId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setEsId(esId);
	}

	
	@Test
	public void testGetArtifactCreator() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactCreator();
	}

	
	@Test
	public void testSetArtifactCreator() throws Exception {
		ArtifactDataDefinition testSubject;
		String artifactCreator = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactCreator(artifactCreator);
	}

	
	@Test
	public void testGetMandatory() throws Exception {
		ArtifactDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMandatory();
	}

	
	@Test
	public void testSetMandatory() throws Exception {
		ArtifactDataDefinition testSubject;
		Boolean mandatory = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setMandatory(mandatory);
	}

	
	@Test
	public void testGetArtifactDisplayName() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactDisplayName();
	}

	
	@Test
	public void testSetArtifactDisplayName() throws Exception {
		ArtifactDataDefinition testSubject;
		String artifactDisplayName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactDisplayName(artifactDisplayName);
	}

	
	@Test
	public void testGetApiUrl() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getApiUrl();
	}

	
	@Test
	public void testSetApiUrl() throws Exception {
		ArtifactDataDefinition testSubject;
		String apiUrl = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setApiUrl(apiUrl);
	}

	
	@Test
	public void testGetServiceApi() throws Exception {
		ArtifactDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceApi();
	}

	
	@Test
	public void testSetServiceApi() throws Exception {
		ArtifactDataDefinition testSubject;
		Boolean serviceApi = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceApi(serviceApi);
	}

	
	@Test
	public void testGetArtifactGroupType() throws Exception {
		ArtifactDataDefinition testSubject;
		ArtifactGroupTypeEnum result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactGroupType();
	}

	
	@Test
	public void testSetArtifactGroupType() throws Exception {
		ArtifactDataDefinition testSubject;
		ArtifactGroupTypeEnum artifactGroupType = null;

		// test 1
		testSubject = createTestSubject();
		artifactGroupType = null;
		testSubject.setArtifactGroupType(artifactGroupType);
	}

	
	@Test
	public void testGetTimeout() throws Exception {
		ArtifactDataDefinition testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimeout();
	}

	
	@Test
	public void testSetTimeout() throws Exception {
		ArtifactDataDefinition testSubject;
		Integer timeout = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimeout(timeout);
	}

	
	@Test
	public void testGetArtifactVersion() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactVersion();
	}

	
	@Test
	public void testSetArtifactVersion() throws Exception {
		ArtifactDataDefinition testSubject;
		String artifactVersion = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactVersion(artifactVersion);
	}

	
	@Test
	public void testGetArtifactUUID() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactUUID();
	}

	
	@Test
	public void testSetArtifactUUID() throws Exception {
		ArtifactDataDefinition testSubject;
		String artifactUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactUUID(artifactUUID);
	}

	
	@Test
	public void testGetPayloadUpdateDate() throws Exception {
		ArtifactDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPayloadUpdateDate();
	}

	
	@Test
	public void testSetPayloadUpdateDate() throws Exception {
		ArtifactDataDefinition testSubject;
		Long payloadUpdateDate = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setPayloadUpdateDate(payloadUpdateDate);
	}

	
	@Test
	public void testGetHeatParamsUpdateDate() throws Exception {
		ArtifactDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHeatParamsUpdateDate();
	}

	
	@Test
	public void testSetHeatParamsUpdateDate() throws Exception {
		ArtifactDataDefinition testSubject;
		Long heatParamsUpdateDate = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setHeatParamsUpdateDate(heatParamsUpdateDate);
	}

	
	@Test
	public void testGetRequiredArtifacts() throws Exception {
		ArtifactDataDefinition testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequiredArtifacts();
	}

	
	@Test
	public void testSetRequiredArtifacts() throws Exception {
		ArtifactDataDefinition testSubject;
		List<String> requiredArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRequiredArtifacts(requiredArtifacts);
	}

	
	@Test
	public void testGetGenerated() throws Exception {
		ArtifactDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGenerated();
	}

	
	@Test
	public void testSetGenerated() throws Exception {
		ArtifactDataDefinition testSubject;
		Boolean generated = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setGenerated(generated);
	}

	
	@Test
	public void testGetDuplicated() throws Exception {
		ArtifactDataDefinition testSubject;
		Boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDuplicated();
	}

	
	@Test
	public void testSetDuplicated() throws Exception {
		ArtifactDataDefinition testSubject;
		Boolean duplicated = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDuplicated(duplicated);
	}

	
	@Test
	public void testGetHeatParameters() throws Exception {
		ArtifactDataDefinition testSubject;
		List<HeatParameterDataDefinition> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHeatParameters();
	}

	
	@Test
	public void testSetHeatParameters() throws Exception {
		ArtifactDataDefinition testSubject;
		List<HeatParameterDataDefinition> properties = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setHeatParameters(properties);
	}

	
	@Test
	public void testGetGeneratedFromId() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGeneratedFromId();
	}

	
	@Test
	public void testSetGeneratedFromId() throws Exception {
		ArtifactDataDefinition testSubject;
		String generatedFromId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setGeneratedFromId(generatedFromId);
	}

	
	@Test
	public void testToString() throws Exception {
		ArtifactDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testHashCode() throws Exception {
		ArtifactDataDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		ArtifactDataDefinition testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		obj = null;
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
	}
}