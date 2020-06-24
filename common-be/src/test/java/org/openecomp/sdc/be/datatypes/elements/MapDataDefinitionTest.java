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

package org.openecomp.sdc.be.datatypes.elements;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;

public class MapDataDefinitionTest {

	private MapDataDefinition mapDataDefinition = new MapDataDefinition();
	private ArtifactDataDefinition artifactDataDefinition1 = new ArtifactDataDefinition();

	@BeforeEach
	public void initMapDataDefinition() {
		artifactDataDefinition1.setToscaPresentationValue(JsonPresentationFields.UNIQUE_ID, "testUniqueId");
		mapDataDefinition.put("key1", artifactDataDefinition1);
		mapDataDefinition.setOwnerIdIfEmpty("testOwner1");

		ArtifactDataDefinition artifactDataDefinition2 = new ArtifactDataDefinition();
		artifactDataDefinition2.setToscaPresentationValue(JsonPresentationFields.UNIQUE_ID, "testUniqueId2");
		mapDataDefinition.put("key2", artifactDataDefinition2);
	}

	@Test
	public void testCopyConstructor() throws Exception {
		MapDataDefinition mapDataDefinitionNew = new MapDataDefinition(mapDataDefinition);
		assertTrue(mapDataDefinitionNew.findByKey("key1").equals(artifactDataDefinition1));
		assertNotNull(mapDataDefinitionNew.findByKey("key2"));
	}

	@Test
	public void testPut() throws Exception {
		ArtifactDataDefinition artifactDataDefinition = new ArtifactDataDefinition();
		mapDataDefinition.put("key3", artifactDataDefinition);
		assertTrue(mapDataDefinition.findByKey("key3").equals(artifactDataDefinition));
	}

	@Test
	public void testDelete() throws Exception {
		mapDataDefinition.delete("key1");
		assertNull(mapDataDefinition.findByKey("key1"));
	}

	@Test
	public void testSetOwnerIdIfEmpty() throws Exception {
		mapDataDefinition.setOwnerIdIfEmpty("testOwner2");
		assertTrue(mapDataDefinition.findByKey("key1").getOwnerId().equals("testOwner1"));
		assertTrue(mapDataDefinition.findByKey("key2").getOwnerId().equals("testOwner2"));
	}

	@Test
	public void testFindByKey() throws Exception {
		assertNull(mapDataDefinition.findByKey("wrongKey"));
		assertTrue(mapDataDefinition.findByKey("key1").equals(artifactDataDefinition1));
	}

	@Test
	public void testFindKeyByItemUidMatch() throws Exception {
		assertNull(mapDataDefinition.findKeyByItemUidMatch(null));
		assertNull(mapDataDefinition.findKeyByItemUidMatch("wrongUniqueId"));
		assertTrue(mapDataDefinition.findKeyByItemUidMatch("testUniqueId").equals("key1"));
		assertTrue(mapDataDefinition.findKeyByItemUidMatch("testUniqueId2").equals("key2"));
	}

	@Test
	public void testRemoveByOwnerId() throws Exception {
		Set<String> ownerIdSet =  new HashSet<String> ();
		ownerIdSet.add("testOwner1");
		mapDataDefinition.removeByOwnerId(ownerIdSet);

		assertNull(mapDataDefinition.findByKey("key1"));
		assertNotNull(mapDataDefinition.findByKey("key2"));
	}

	@Test
	public void testUpdateIfExist() throws Exception {
		MapDataDefinition mapDataDefinitionNew = new MapDataDefinition();
		ArtifactDataDefinition artifactDataDefinition2 = new ArtifactDataDefinition();
		artifactDataDefinition2.setToscaPresentationValue(JsonPresentationFields.UNIQUE_ID, "testUniqueId2");
		mapDataDefinitionNew.put("key2", artifactDataDefinition2);
		mapDataDefinitionNew.setOwnerIdIfEmpty("testOwner2");

		assertNull(mapDataDefinition.findByKey("key2").getOwnerId());
		mapDataDefinition.updateIfExist(mapDataDefinitionNew, true);
		assertTrue(mapDataDefinition.findByKey("key2").getOwnerId().equals("testOwner2"));
	}

	@Test
	public void testIsEmpty() throws Exception {
		assertTrue(!mapDataDefinition.isEmpty());
	}
}
