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

package org.openecomp.sdc.be.datatypes.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ComponentTypeEnumTest {

	private ComponentTypeEnum createTestSubject() {
		return ComponentTypeEnum.PRODUCT;
	}

	@Test
	public void testGetValue() throws Exception {
		ComponentTypeEnum testSubject = createTestSubject();
		assertEquals("Product", testSubject.getValue());
	}

	@Test
	public void testGetNodeType() {
		assertEquals(NodeTypeEnum.Resource, ComponentTypeEnum.RESOURCE.getNodeType());
		assertEquals(NodeTypeEnum.Product, ComponentTypeEnum.PRODUCT.getNodeType());
		assertEquals(NodeTypeEnum.Service, ComponentTypeEnum.SERVICE.getNodeType());
		assertEquals(NodeTypeEnum.ResourceInstance, ComponentTypeEnum.RESOURCE_INSTANCE.getNodeType());
		assertThrows(UnsupportedOperationException.class, () -> {
			ComponentTypeEnum.SERVICE_INSTANCE.getNodeType();
		});
	}

	@Test
	public void testFindByValue() {
		assertNull(ComponentTypeEnum.findByValue(""));
		assertEquals(ComponentTypeEnum.RESOURCE, ComponentTypeEnum.findByValue("Resource"));
		assertEquals(ComponentTypeEnum.SERVICE, ComponentTypeEnum.findByValue("Service"));
		assertEquals(ComponentTypeEnum.PRODUCT, ComponentTypeEnum.findByValue("Product"));
	}

	@Test
	public void testFindByParamName() {
		assertNull(ComponentTypeEnum.findByParamName(""));
		assertEquals(ComponentTypeEnum.RESOURCE, ComponentTypeEnum.findByParamName("resources"));
		assertEquals(ComponentTypeEnum.SERVICE, ComponentTypeEnum.findByParamName("services"));
		assertEquals(ComponentTypeEnum.PRODUCT, ComponentTypeEnum.findByParamName("products"));
	}

	@Test
	public void testFindParamByType() {
		assertNull(ComponentTypeEnum.findParamByType(null));
		assertNull(ComponentTypeEnum.findParamByType(ComponentTypeEnum.RESOURCE_INSTANCE));
		assertEquals("resources", ComponentTypeEnum.findParamByType(ComponentTypeEnum.RESOURCE));
		assertEquals("services", ComponentTypeEnum.findParamByType(ComponentTypeEnum.SERVICE));
		assertEquals("products", ComponentTypeEnum.findParamByType(ComponentTypeEnum.PRODUCT));
	}
}
