/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.model.operations.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.api.IAdditionalInformationOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.util.OperationTestsUtil;
import org.openecomp.sdc.be.resources.data.UserData;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thinkaurelius.titan.core.TitanGraph;
//import com.tinkerpop.blueprints.Vertex;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
public class AdditionalInformationOperationTest extends ModelTestBase {

	private static String USER_ID = "muUserId";
	private static String CATEGORY_NAME = "category/mycategory";

	@javax.annotation.Resource(name = "titan-generic-dao")
	private TitanGenericDao titanDao;

	@javax.annotation.Resource(name = "resource-operation")
	private ResourceOperation resourceOperation;

	@javax.annotation.Resource(name = "additional-information-operation")
	private IAdditionalInformationOperation additionalInformationOperation;

	@Before
	public void createUserAndCategory() {
		deleteAndCreateCategory(CATEGORY_NAME);
		deleteAndCreateUser(USER_ID, "first_" + USER_ID, "last_" + USER_ID);

	}

	@BeforeClass
	public static void setupBeforeClass() {

		ModelTestBase.init();

	}

	@Test
	public void testDummy() {

		assertTrue(additionalInformationOperation != null);

	}

	private int getNumberOfVerticesOnGraph() {
		Either<TitanGraph, TitanOperationStatus> graphResult = titanDao.getGraph();
		TitanGraph graph = graphResult.left().value();

		int i = 0;
		Iterable<TitanVertex> vertices = graph.query().vertices();
		if (vertices != null) {
			Iterator<TitanVertex> iterator = vertices.iterator();
			while (iterator.hasNext()) {
				TitanVertex vertex = iterator.next();
				i++;
			}

		}

		titanDao.commit();

		return i;
	}

	@Test
	public void testCreateAndDeleteResource() {

		int before = getNumberOfVerticesOnGraph();

		Resource newResource = createResource(USER_ID, CATEGORY_NAME, "testCreateAndDeleteResource", "0.1", null, false, true);
		String resourceId = newResource.getUniqueId();

		Either<Resource, StorageOperationStatus> deleteResource = resourceOperation.deleteResource(resourceId);
		assertTrue(deleteResource.isLeft());

		int after = getNumberOfVerticesOnGraph();

		assertEquals("check number of vertices not changed", before, after);
	}

	private Resource buildResourceMetadata(String userId, String category, String resourceName, String resourceVersion) {

		Resource resource = new Resource();
		resource.setName(resourceName);
		resource.setVersion(resourceVersion);
		;
		resource.setDescription("description 1");
		resource.setAbstract(false);
		resource.setCreatorUserId(userId);
		resource.setContactId("contactId@sdc.com");
		resource.setVendorName("vendor 1");
		resource.setVendorRelease("1.0.0");
		String[] categoryArr = category.split("/");
		resource.addCategory(categoryArr[0], categoryArr[1]);
		resource.setIcon("images/my.png");
		// List<String> tags = new ArrayList<String>();
		// tags.add("TAG1");
		// tags.add("TAG2");
		// resource.setTags(tags);
		return resource;
	}

	private UserData deleteAndCreateUser(String userId, String firstName, String lastName) {
		UserData userData = new UserData();
		userData.setUserId(userId);
		userData.setFirstName(firstName);
		userData.setLastName(lastName);

		titanDao.deleteNode(UniqueIdBuilder.getKeyByNodeType(NodeTypeEnum.User), userId, UserData.class);
		titanDao.createNode(userData, UserData.class);
		titanDao.commit();

		return userData;
	}

	private void deleteAndCreateCategory(String category) {
		String[] names = category.split("/");
		OperationTestsUtil.deleteAndCreateResourceCategory(names[0], names[1], titanDao);
	}

	public Resource createResource(String userId, String category, String resourceName, String resourceVersion, String parentResourceName, boolean isAbstract, boolean isHighestVersion) {

		List<String> derivedFrom = new ArrayList<String>();
		if (parentResourceName != null) {
			derivedFrom.add(parentResourceName);
		}
		Resource resource = buildResourceMetadata(userId, category, resourceName, resourceVersion);

		resource.setAbstract(isAbstract);
		resource.setHighestVersion(isHighestVersion);

		Either<Resource, StorageOperationStatus> result = resourceOperation.createResource(resource, true);

		assertTrue(result.isLeft());
		Resource resultResource = result.left().value();

		assertEquals("check resource state", LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT, resultResource.getLifecycleState());

		return resultResource;

	}

}
