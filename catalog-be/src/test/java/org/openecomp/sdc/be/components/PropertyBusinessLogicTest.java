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

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import org.junit.Before;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.PropertyBusinessLogic;
import org.openecomp.sdc.be.components.impl.ResourceBusinessLogic;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.operations.api.IPropertyOperation;
import org.openecomp.sdc.be.model.operations.api.IResourceOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.resources.data.EntryData;
import org.openecomp.sdc.be.user.Role;
import org.openecomp.sdc.be.user.UserBusinessLogic;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fj.data.Either;
import junit.framework.Assert;

public class PropertyBusinessLogicTest {

	private static Logger log = LoggerFactory.getLogger(PropertyBusinessLogicTest.class.getName());
	final ServletContext servletContext = Mockito.mock(ServletContext.class);
	final IPropertyOperation propertyOperation = Mockito.mock(IPropertyOperation.class);
	final IResourceOperation resourceOperation = Mockito.mock(IResourceOperation.class);
	WebAppContextWrapper webAppContextWrapper = Mockito.mock(WebAppContextWrapper.class);
	UserBusinessLogic mockUserAdmin = Mockito.mock(UserBusinessLogic.class);
	WebApplicationContext webAppContext = Mockito.mock(WebApplicationContext.class);
	PropertyBusinessLogic bl = new PropertyBusinessLogic();
	User user = null;
	Resource resourceResponse = null;
	ResourceBusinessLogic blResource = new ResourceBusinessLogic();
	PropertyBusinessLogic spy = null;
	String resourceId = "resourceforproperty.0.1";

	public PropertyBusinessLogicTest() {

	}

	@Before
	public void setup() {

		ExternalConfiguration.setAppName("catalog-be");

		// Init Configuration
		String appConfigDir = "src/test/resources/config/catalog-be";
		ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), appConfigDir);
		ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);

		// User data and management
		user = new User();
		user.setUserId("jh003");
		user.setFirstName("Jimmi");
		user.setLastName("Hendrix");
		user.setRole(Role.ADMIN.name());

		Either<User, ActionStatus> eitherGetUser = Either.left(user);
		when(mockUserAdmin.getUser("jh003", false)).thenReturn(eitherGetUser);

		// Servlet Context attributes
		when(servletContext.getAttribute(Constants.CONFIGURATION_MANAGER_ATTR)).thenReturn(configurationManager);
		when(servletContext.getAttribute(Constants.PROPERTY_OPERATION_MANAGER)).thenReturn(propertyOperation);
		when(servletContext.getAttribute(Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR)).thenReturn(webAppContextWrapper);
		when(servletContext.getAttribute(Constants.RESOURCE_OPERATION_MANAGER)).thenReturn(resourceOperation);
		when(webAppContextWrapper.getWebAppContext(servletContext)).thenReturn(webAppContext);

		// Resource Operation mock methods
		// getCount
		Either<Integer, StorageOperationStatus> eitherCount = Either.left(0);
		when(resourceOperation.getNumberOfResourcesByName("MyResourceName".toLowerCase())).thenReturn(eitherCount);
		Either<Integer, StorageOperationStatus> eitherCountExist = Either.left(1);
		when(resourceOperation.getNumberOfResourcesByName("alreadyExist".toLowerCase())).thenReturn(eitherCountExist);
		Either<Integer, StorageOperationStatus> eitherCountRoot = Either.left(1);
		when(resourceOperation.getNumberOfResourcesByName("Root".toLowerCase())).thenReturn(eitherCountRoot);

		Either<Resource, StorageOperationStatus> eitherGetResource = Either.left(createResourceObject(true));
		when(resourceOperation.getResource(resourceId)).thenReturn(eitherGetResource);

		// // createResource
		// resourceResponse = createResourceObject(true);
		// Either<Resource, StorageOperationStatus> eitherCreate =
		// Either.left(resourceResponse);
		// when(resourceOperation.createResource(Mockito.any(Resource.class))).thenReturn(eitherCreate);

		// BL object
		// bl = PropertyBusinessLogic.getInstance(servletContext);
		// PropertyBusinessLogic spy = PowerMockito.spy(bl);
		// when(spy, method(PropertyBusinessLogic.class, "getResource",
		// String.class)).withArguments(resource).thenReturn(true);

	}

	private Resource createResourceObject(boolean afterCreate) {
		Resource resource = new Resource();
		resource.setName("MyResourceName");
		resource.addCategory("Generic", "VoIP");
		resource.setDescription("My short description");
		List<String> tgs = new ArrayList<String>();
		tgs.add("test");
		resource.setTags(tgs);
		List<String> template = new ArrayList<String>();
		template.add("Root");
		resource.setDerivedFrom(template);
		resource.setVendorName("Motorola");
		resource.setVendorRelease("1.0.0");
		resource.setContactId("yavivi");
		resource.setIcon("MyIcon.jpg");

		if (afterCreate) {
			resource.setName(resource.getName().toLowerCase());
			resource.setVersion("0.1");
			;
			resource.setUniqueId(resourceId);
			resource.setCreatorUserId(user.getUserId());
			resource.setCreatorFullName(user.getFirstName() + " " + user.getLastName());
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		log.debug(gson.toJson(resource));
		return resource;
	}

	// @Test
	public void testHappyScenario() {

		String propertyName = "disk_size";
		PropertyDefinition newPropertyDefinition = createPropertyObject(propertyName, resourceId);
		Either<EntryData<String, PropertyDefinition>, ResponseFormat> either = bl.createProperty(resourceId, propertyName, newPropertyDefinition, user.getUserId());

		if (either.isRight()) {
			Assert.assertFalse(true);
		}
		Assert.assertEquals(newPropertyDefinition, either.left().value());
	}

	private PropertyDefinition createPropertyObject(String propertyName, String resourceId) {
		PropertyDefinition pd = new PropertyDefinition();
		List<PropertyConstraint> constraints = new ArrayList<PropertyConstraint>();
		pd.setConstraints(null);
		pd.setDefaultValue("100");
		pd.setDescription("Size of thasdasdasdasde local disk, in Gigabytes (GB), available to applications running on the Compute node");
		pd.setPassword(false);
		pd.setRequired(true);
		pd.setType("Integer");
		pd.setUniqueId(resourceId + "." + propertyName);
		return pd;
	}
}
