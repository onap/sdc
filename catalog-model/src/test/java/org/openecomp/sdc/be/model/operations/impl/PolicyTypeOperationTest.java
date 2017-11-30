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

import java.util.Iterator;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.datatypes.elements.PolicyTypeDataDefinition;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.PolicyTypeDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;

import fj.data.Either;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")

public class PolicyTypeOperationTest extends ModelTestBase {

	@Resource(name = "policy-type-operation")
	private PolicyTypeOperation policyTypeOperation;

	@BeforeClass
	public static void setupBeforeClass() {
		ModelTestBase.init();

	}

	@Before
	public void cleanUp() {
		TitanGenericDao titanGenericDao = policyTypeOperation.titanGenericDao;
		Either<TitanGraph, TitanOperationStatus> graphResult = titanGenericDao.getGraph();
		TitanGraph graph = graphResult.left().value();

		Iterable<TitanVertex> vertices = graph.query().vertices();
		if (vertices != null) {
			Iterator<TitanVertex> iterator = vertices.iterator();
			while (iterator.hasNext()) {
				TitanVertex vertex = iterator.next();
				vertex.remove();
			}

		}
		titanGenericDao.commit();
	}

	@Test
	public void testAddPolicyType() {

		PolicyTypeDefinition policyTypePreCreate = createPolicyTypeDef();
		assertTrue(StringUtils.isEmpty(policyTypePreCreate.getUniqueId()));
		Either<PolicyTypeDefinition, StorageOperationStatus> addPolicyType = policyTypeOperation.addPolicyType(policyTypePreCreate);
		assertTrue(addPolicyType.isLeft());
		PolicyTypeDefinition policyTypePostCreate = addPolicyType.left().value();
		assertEquals(policyTypePostCreate.getType(), policyTypePreCreate.getType());
		assertEquals(policyTypePostCreate.getDescription(), policyTypePreCreate.getDescription());

		assertTrue(!StringUtils.isEmpty(policyTypePostCreate.getUniqueId()));
	}

	@Test
	public void testGetLatestPolicyTypeByType() {
		PolicyTypeDefinition policyTypeCreated = policyTypeOperation.addPolicyType(createPolicyTypeDef()).left().value();
		Either<PolicyTypeDefinition, StorageOperationStatus> eitherPolicyTypeFetched = policyTypeOperation.getLatestPolicyTypeByType(policyTypeCreated.getType());
		assertTrue(eitherPolicyTypeFetched.isLeft());
		PolicyTypeDefinition policyTypeFetched = eitherPolicyTypeFetched.left().value();
		assertEquals(policyTypeFetched.toString(), policyTypeCreated.toString());

	}

	private PolicyTypeDefinition createPolicyTypeDef() {
		PolicyTypeDataDefinition policyTypeDataDefinition = new PolicyTypeDataDefinition();
		policyTypeDataDefinition.setDescription("description: The TOSCA Policy Type all other TOSCA Policy Types derive from");
		policyTypeDataDefinition.setType("tosca.policies.Root");
		PolicyTypeDefinition policyTypeDefinition = new PolicyTypeDefinition(policyTypeDataDefinition);
		policyTypeDefinition.setHighestVersion(true);
		policyTypeDefinition.setVersion("1.0");
		return policyTypeDefinition;
	}

}
