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

package org.openecomp.sdc.be.components;

import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.category.CategoryDefinition;

public class ResourceTestUtils {

	public static Resource prepareResource(int resourceIndex) {
		Resource r = new Resource();
		r.setName("resource_" + resourceIndex);
		r.setDescription("description");
		r.setVendorName("vendor name");
		r.setVendorRelease("vendor release");
		r.setContactId("as123y");
		r.addCategory("Generic", "Infrastructure");
		List<String> arr = new ArrayList<String>();
		arr.add("tosca.nodes.Root");
		r.setDerivedFrom(arr);
		List<String> arr1 = new ArrayList<String>();
		arr1.add(r.getName());
		r.setTags(arr1);
		r.setIcon("borderElement");
		return r;
	}

	public static Resource prepareResource(int resourceIndex, ResourceTypeEnum resourceType) {
		Resource r = new Resource();
		r.setName("resource_" + resourceIndex);
		r.setToscaResourceName("resource_" + resourceIndex);
		r.setDescription("description");
		r.setVendorName("vendor name");
		r.setVendorRelease("vendor release");
		r.setContactId("as123y");
		r.setResourceType(resourceType);
		r.addCategory("Generic", "Infrastructure");
		List<String> arr = new ArrayList<String>();
		arr.add("tosca.nodes.Root");
		r.setDerivedFrom(arr);
		List<String> arr1 = new ArrayList<String>();
		arr1.add(r.getName());
		r.setTags(arr1);
		r.setIcon("borderElement");
		return r;
	}

	public static Service prepareService(int serviceIndex) {
		Service service = new Service();
		service.setName("service_" + serviceIndex);
		service.setDescription("desc");
		service.setIcon("icon-service-red1");
		List<String> tags = new ArrayList<String>();
		tags.add(service.getName());
		service.setTags(tags);
		CategoryDefinition category = new CategoryDefinition();
		category.setName("Mobility");
		List<CategoryDefinition> categories = new ArrayList<>();
		categories.add(category);
		service.setCategories(categories);
		service.setContactId("as123y");
		service.setProjectCode("123456");

		return service;
	}

}
