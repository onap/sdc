/*

 * Copyright (c) 2018 AT&T Intellectual Property.

 *

 * Licensed under the Apache License, Version 2.0 (the "License");

 * you may not use this file except in compliance with the License.

 * You may obtain a copy of the License at

 *

 *     http://www.apache.org/licenses/LICENSE-2.0

 *

 * Unless required by applicable law or agreed to in writing, software

 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

 * See the License for the specific language governing permissions and

 * limitations under the License.

 */
package org.openecomp.sdc.be.components.impl;

import fj.data.Either;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.config.Configuration;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.jsongraph.TitanDao;
import org.openecomp.sdc.be.dao.jsongraph.types.JsonParseFlagEnum;
import org.openecomp.sdc.be.datatypes.elements.AdditionalInfoParameterInfo;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.AdditionalInformationDefinition;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.jsontitan.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IAdditionalInformationOperation;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.ConfigurationSource;
import org.openecomp.sdc.common.impl.ExternalConfiguration;
import org.openecomp.sdc.common.impl.FSConfigurationSource;
import org.openecomp.sdc.exception.ResponseFormat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AdditionalInformationBusinessLogicTest {
	@InjectMocks
	private AdditionalInformationBusinessLogic additionalInformationBusinessLogic;
	@Mock
	private IGraphLockOperation graphLockOperation;
	@Mock
	private UserValidations userValidations;
	@Mock
	private ToscaOperationFacade toscaOperationFacade;
	@Mock
	private ComponentsUtils componentsUtils;
	@Mock
	private TitanDao titanDao;
	@Mock
	private IAdditionalInformationOperation additionalInformationOperation;

	private AdditionalInformationBusinessLogic createTestSubject() {
		return new AdditionalInformationBusinessLogic();
	}

	static ConfigurationSource configurationSource = new FSConfigurationSource(ExternalConfiguration.getChangeListener(), "src/test/resources/config/catalog-be");


	@Test
	public void testCreateAdditionalInformation() {
		Either<AdditionalInfoParameterInfo, ResponseFormat> result;
		String resourceId = "resourceId";
		AdditionalInfoParameterInfo additionalInfoParameterInfo = new AdditionalInfoParameterInfo();
		additionalInfoParameterInfo.setKey("key");
		additionalInfoParameterInfo.setValue("value");
		AdditionalInformationDefinition additionalInformationDefinition = new AdditionalInformationDefinition();
		List<AdditionalInfoParameterInfo> parameters = new ArrayList<>();
		parameters.add(additionalInfoParameterInfo);
		additionalInformationDefinition.setParameters(parameters);
		User user = new User();
		String userId = "userId";
		user.setUserId(userId);
		ConfigurationManager configurationManager = new ConfigurationManager(configurationSource);
		configurationManager.setConfiguration(new Configuration());
		configurationManager.getConfiguration().setAdditionalInformationMaxNumberOfKeys(0);
		Resource component = new Resource();
		component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
		component.setLastUpdaterUserId(userId);
		when(additionalInformationBusinessLogic.validateUserExists(anyString(), anyString(), eq(false))).thenReturn(user);
		when(toscaOperationFacade.getToscaElement(ArgumentMatchers.eq("resourceId"), ArgumentMatchers.eq(JsonParseFlagEnum.ParseMetadata))).thenReturn(Either.left(component));
		when(graphLockOperation.lockComponent(resourceId, NodeTypeEnum.Resource)).thenReturn(StorageOperationStatus.OK);
		when(additionalInformationOperation.getNumberOfAdditionalInformationParameters(NodeTypeEnum.Resource,resourceId,true)).thenReturn(Either.left(0));
		when(additionalInformationOperation.createAdditionalInformationParameter(NodeTypeEnum.Resource, resourceId, additionalInfoParameterInfo.getKey(),additionalInfoParameterInfo.getValue(), true)).thenReturn(Either.left(additionalInformationDefinition));

		result = additionalInformationBusinessLogic.createAdditionalInformation(NodeTypeEnum.Resource,resourceId,additionalInfoParameterInfo,userId);
		assertThat(result.isLeft());
	}


	@Test
	public void testUpdateAdditionalInformation() throws Exception {
		AdditionalInformationBusinessLogic testSubject;
		NodeTypeEnum nodeType = null;
		String resourceId = "";
		AdditionalInfoParameterInfo additionalInfoParameterInfo = null;
		String additionalInformationUid = "";
		String userId = "";
		Either<AdditionalInfoParameterInfo, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}


	@Test
	public void testDeleteAdditionalInformation() throws Exception {
		AdditionalInformationBusinessLogic testSubject;
		NodeTypeEnum nodeType = null;
		String resourceId = "";
		AdditionalInfoParameterInfo additionalInfoParameterInfo = null;
		String additionalInformationUid = "";
		String userId = "";
		Either<AdditionalInfoParameterInfo, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}


	@Test
	public void testGetAdditionalInformation() throws Exception {
		AdditionalInformationBusinessLogic testSubject;
		NodeTypeEnum nodeType = null;
		String resourceId = "";
		AdditionalInfoParameterInfo additionalInfoParameterInfo = null;
		String additionalInformationUid = "";
		String userId = "";
		Either<AdditionalInfoParameterInfo, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}


	@Test
	public void testGetAllAdditionalInformation() throws Exception {
		AdditionalInformationBusinessLogic testSubject;
		NodeTypeEnum nodeType = null;
		String resourceId = "";
		String additionalInformationUid = "";
		String userId = "";
		Either<AdditionalInformationDefinition, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
	}
}