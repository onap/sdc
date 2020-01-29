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

package org.openecomp.sdc.be.components.distribution.engine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.components.BeConfDependentTest;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.common.api.ArtifactTypeEnum;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ArtifactInfoImplTest extends BeConfDependentTest {

	private ArtifactInfoImpl createTestSubject() {
		return new ArtifactInfoImpl();
	}

	@Test
	public void testConvertToArtifactInfoImpl() throws Exception {
		Service service = new Service();
		ComponentInstance resourceInstance = new ComponentInstance();
		Collection<ArtifactDefinition> list = new LinkedList<>();
		ArtifactDefinition artifactDefinition = new ArtifactDefinition();
		List<ArtifactInfoImpl> result;

		// test 1
		result = ArtifactInfoImpl.convertToArtifactInfoImpl(service, resourceInstance, list);
		Assert.assertEquals(new LinkedList<>(), result);

		// test 2
		artifactDefinition.setUniqueId("mock");
		list.add(artifactDefinition);
		result = ArtifactInfoImpl.convertToArtifactInfoImpl(service, resourceInstance, list);
		Assert.assertFalse(result.isEmpty());

		// test 3
		artifactDefinition.setGeneratedFromId("mock");
		result = ArtifactInfoImpl.convertToArtifactInfoImpl(service, resourceInstance, list);
		Assert.assertFalse(result.isEmpty());
	}

	@Test
	public void testConvertServiceArtifactToArtifactInfoImpl() throws Exception {
		Service service = new Service();
		Collection<ArtifactDefinition> list = new LinkedList<>();
		ArtifactDefinition artifactDefinition = new ArtifactDefinition();
		List<ArtifactInfoImpl> result = new LinkedList<>();

		// test 1
		result = ArtifactInfoImpl.convertServiceArtifactToArtifactInfoImpl(service, list);
		Assert.assertEquals(new LinkedList<>(), result);

		// test 2
		artifactDefinition.setUniqueId("mock");
		list.add(artifactDefinition);

		result = ArtifactInfoImpl.convertServiceArtifactToArtifactInfoImpl(service, list);
		Assert.assertFalse(result.isEmpty());

		// test 3
		artifactDefinition.setGeneratedFromId("mock");

		result = ArtifactInfoImpl.convertServiceArtifactToArtifactInfoImpl(service, list);
		Assert.assertFalse(result.isEmpty());
	}

	@Test
	public void testGetUpdatedRequiredArtifactsFromNamesToUuids() throws Exception {
		ArtifactDefinition artifactDefinition = null;
		Map<String, ArtifactDefinition> artifacts = new HashMap<String, ArtifactDefinition>();
		List<String> result;

		// test 1
		artifactDefinition = null;
		result = Deencapsulation.invoke(ArtifactInfoImpl.class, "getUpdatedRequiredArtifactsFromNamesToUuids",
				new Object[] { ArtifactDefinition.class, artifacts.getClass() });
		Assert.assertEquals(null, result);

		// test 2
		artifactDefinition = new ArtifactDefinition();
		result = Deencapsulation.invoke(ArtifactInfoImpl.class, "getUpdatedRequiredArtifactsFromNamesToUuids",
				new Object[] { ArtifactDefinition.class, artifacts.getClass() });
		Assert.assertEquals(null, result);
	}

	@Test
	public void testGetArtifactName() throws Exception {
		ArtifactInfoImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactName();
	}

	@Test
	public void testSetArtifactName() throws Exception {
		ArtifactInfoImpl testSubject;
		String artifactName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactName(artifactName);
	}

	@Test
	public void testSetArtifactType() {
		final ArtifactInfoImpl testSubject = createTestSubject();
		final String expectedType = ArtifactTypeEnum.AAI_SERVICE_MODEL.getType();
		testSubject.setArtifactType(expectedType);
		assertThat("Artifact type should be the same", testSubject.getArtifactType(), is(expectedType));
	}

	@Test
	public void testGetArtifactURL() throws Exception {
		ArtifactInfoImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactURL();
	}

	@Test
	public void testSetArtifactURL() throws Exception {
		ArtifactInfoImpl testSubject;
		String artifactURL = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactURL(artifactURL);
	}

	@Test
	public void testGetArtifactChecksum() throws Exception {
		ArtifactInfoImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactChecksum();
	}

	@Test
	public void testSetArtifactChecksum() throws Exception {
		ArtifactInfoImpl testSubject;
		String artifactChecksum = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactChecksum(artifactChecksum);
	}

	@Test
	public void testGetArtifactDescription() throws Exception {
		ArtifactInfoImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactDescription();
	}

	@Test
	public void testSetArtifactDescription() throws Exception {
		ArtifactInfoImpl testSubject;
		String artifactDescription = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactDescription(artifactDescription);
	}

	@Test
	public void testGetArtifactTimeout() throws Exception {
		ArtifactInfoImpl testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactTimeout();
	}

	@Test
	public void testSetArtifactTimeout() throws Exception {
		ArtifactInfoImpl testSubject;
		Integer artifactTimeout = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactTimeout(artifactTimeout);
	}

	@Test
	public void testGetRelatedArtifacts() throws Exception {
		ArtifactInfoImpl testSubject;
		List<String> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRelatedArtifacts();
	}

	@Test
	public void testSetRelatedArtifacts() throws Exception {
		ArtifactInfoImpl testSubject;
		List<String> relatedArtifacts = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setRelatedArtifacts(relatedArtifacts);
	}

	@Test
	public void testToString() throws Exception {
		ArtifactInfoImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	@Test
	public void testGetArtifactUUID() throws Exception {
		ArtifactInfoImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactUUID();
	}

	@Test
	public void testSetArtifactUUID() throws Exception {
		ArtifactInfoImpl testSubject;
		String artifactUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactUUID(artifactUUID);
	}

	@Test
	public void testGetArtifactVersion() throws Exception {
		ArtifactInfoImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getArtifactVersion();
	}

	@Test
	public void testSetArtifactVersion() throws Exception {
		ArtifactInfoImpl testSubject;
		String artifactVersion = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setArtifactVersion(artifactVersion);
	}

	@Test
	public void testGetGeneratedFromUUID() throws Exception {
		ArtifactInfoImpl testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGeneratedFromUUID();
	}

	@Test
	public void testSetGeneratedFromUUID() throws Exception {
		ArtifactInfoImpl testSubject;
		String generatedFromUUID = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setGeneratedFromUUID(generatedFromUUID);
	}

}
