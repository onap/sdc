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

package org.openecomp.sdc.ci.tests.utils.validation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedResourceAuditJavaObject;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;

public class ServiceValidationUtils {

	public static void validateServiceResponseMetaData(ServiceReqDetails serviceDetails, Service service, User user,
			LifecycleStateEnum lifecycleState) {
		validateServiceResponseMetaData(serviceDetails, service, user, user, lifecycleState);
	}

	public static void validateServiceResponseMetaData(ServiceReqDetails expectedService, Service service,
			User creatorUser, User updaterUser, LifecycleStateEnum lifeCycleState) {
		List<String> expectedTags = expectedService.getTags();
		expectedTags.add(expectedService.getName());
		List<String> receivedTags = service.getTags();
		if (expectedTags != null) {
			Set<String> hs = new LinkedHashSet<>(expectedTags);
			expectedTags.clear();
			expectedTags.addAll(hs);
		}

		assertEquals("Check service name on response after create service", expectedService.getName(),
				service.getName());
		// check size of list
		assertEquals("Check only 1 category returned on response after create service", 1,
				expectedService.getCategories().size());
		assertEquals("Check service name on response after create service", expectedService.getName(),
				service.getName());
		assertEquals("Check categories on response after create service",
				expectedService.getCategories().get(0).getName(), service.getCategories().get(0).getName());
		assertEquals("Check tag list on response after create service", expectedTags, receivedTags);
		assertEquals("Check description on response after create service", expectedService.getDescription(),
				service.getDescription());
		// assertEquals("Check vendor name on response after create service",
		// expectedService.getVendorName(), service.getVendorName());
		// assertEquals("Check vendor release on response after create service",
		// expectedService.getVendorRelease(), service.getVendorRelease());
		assertEquals("Check attContant name on response after create service",
				expectedService.getContactId().toLowerCase(), service.getContactId());
		assertEquals("Check icon name on response after create service", expectedService.getIcon(), service.getIcon());
		assertEquals("Check LastUpdaterUserId after create service", updaterUser.getUserId(),
				service.getLastUpdaterUserId());
		assertEquals("Check LastUpdaterName after create service",
				updaterUser.getFirstName() + " " + updaterUser.getLastName(), service.getLastUpdaterFullName());
		assertEquals("Check CreatorUserId after create service", creatorUser.getUserId(), service.getCreatorUserId());
		assertEquals("Check CreatorName after create service",
				creatorUser.getFirstName() + " " + creatorUser.getLastName(), service.getCreatorFullName());
		assertEquals("Check version after create service", expectedService.getVersion(), service.getVersion());
		// assertEquals("Check UniqueId after create service", SERVICE_PREFIX +
		// serviceDetails.getServiceName().toLowerCase()+"." +
		// serviceBaseVersion, service.getUniqueId());
		assertFalse("Check uuid after create service", service.getUUID().isEmpty());

		// assertTrue("check creation date after create service",
		// service.getCreationDate() != null);
		// assertTrue("check update date after create service",
		// service.getLastUpdateDate() != null);

		if (lifeCycleState != null)
			assertEquals("Check LifecycleState after create service", lifeCycleState, service.getLifecycleState());
		else
			assertEquals("Check LifecycleState after create service", LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT,
					service.getLifecycleState());
	}

	public static ExpectedResourceAuditJavaObject constructFieldsForAuditValidation(ServiceReqDetails serviceReqDetails,
			String serviceVersion, User sdncUserDetails) {

		ExpectedResourceAuditJavaObject expectedResourceAuditJavaObject = new ExpectedResourceAuditJavaObject();

		expectedResourceAuditJavaObject.setAction("Create");
		expectedResourceAuditJavaObject.setModifierUid(sdncUserDetails.getUserId());
		String userFirstLastName = sdncUserDetails.getFirstName() + " " + sdncUserDetails.getLastName();
		expectedResourceAuditJavaObject.setModifierName(userFirstLastName);
		expectedResourceAuditJavaObject.setStatus("200");
		expectedResourceAuditJavaObject.setDesc("OK");
		expectedResourceAuditJavaObject.setResourceName(serviceReqDetails.getName());
		expectedResourceAuditJavaObject.setResourceType("Service");
		expectedResourceAuditJavaObject.setPrevVersion(String.valueOf(Float.parseFloat(serviceVersion) - 0.1f));
		expectedResourceAuditJavaObject.setCurrVersion(serviceVersion);
		expectedResourceAuditJavaObject.setPrevState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setCurrState((LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT).toString());
		expectedResourceAuditJavaObject.setComment(null);

		return expectedResourceAuditJavaObject;

	}

	public static void validateDistrubtionStatusValue(RestResponse response,
			DistributionStatusEnum expectedDistributionValue) throws Exception {
		String actualDistributionValue = ResponseParser.getValueFromJsonResponse(response.getResponse(),
				"distributionStatus");
		assertEquals(expectedDistributionValue.name(), actualDistributionValue);
	}

}
