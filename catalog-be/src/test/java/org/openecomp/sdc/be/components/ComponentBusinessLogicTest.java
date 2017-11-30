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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openecomp.sdc.be.DummyConfigurationManager;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import fj.data.Either;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComponentBusinessLogicTest {

	private static final User USER = new User();
	private static final String ARTIFACT_LABEL = "toscaArtifact1";
	private static final String ARTIFACT_LABEL2 = "toscaArtifact2";

	@InjectMocks
	private ComponentBusinessLogic testInstance = new ComponentBusinessLogic() {
		@Override
		public Either<List<String>, ResponseFormat> deleteMarkedComponents() {
			return null;
		}

		@Override
		public ComponentInstanceBusinessLogic getComponentInstanceBL() {
			return null;
		}

		@Override
		public Either<List<ComponentInstance>, ResponseFormat> getComponentInstancesFilteredByPropertiesAndInputs(String componentId, ComponentTypeEnum componentTypeEnum, String userId, String searchText) {
			return null;
		}

		@Override
		public Either<UiComponentDataTransfer, ResponseFormat> getUiComponentDataTransferByComponentId(String componentId, List<String> dataParamsToReturn) {
			return null;
		}
	};

	@Mock
	private ArtifactsBusinessLogic artifactsBusinessLogic;

	@BeforeClass
	public static void setUp() throws Exception {
		new DummyConfigurationManager();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void setToscaArtifactsPlaceHolders_normalizeArtifactName() throws Exception {
		Resource resource = new ResourceBuilder().setUniqueId("uid")
				.setComponentType(ComponentTypeEnum.RESOURCE)
				.setSystemName("myResource")
				.build();
		Map<String, Object> artifactsFromConfig = new HashMap<>();
		artifactsFromConfig.put(ARTIFACT_LABEL, buildArtifactMap("artifact:not normalized.yml"));
		artifactsFromConfig.put(ARTIFACT_LABEL2, buildArtifactMap("alreadyNormalized.csar"));
		when(ConfigurationManager.getConfigurationManager().getConfiguration().getToscaArtifacts()).thenReturn(artifactsFromConfig);
		when(artifactsBusinessLogic.createArtifactPlaceHolderInfo(resource.getUniqueId(), ARTIFACT_LABEL, (Map<String, Object>) artifactsFromConfig.get(ARTIFACT_LABEL), USER, ArtifactGroupTypeEnum.TOSCA))
				.thenReturn(buildArtifactDef(ARTIFACT_LABEL));
		when(artifactsBusinessLogic.createArtifactPlaceHolderInfo(resource.getUniqueId(), ARTIFACT_LABEL2, (Map<String, Object>) artifactsFromConfig.get(ARTIFACT_LABEL2), USER, ArtifactGroupTypeEnum.TOSCA))
				.thenReturn(buildArtifactDef(ARTIFACT_LABEL2));
		testInstance.setToscaArtifactsPlaceHolders(resource, USER);
		
		Map<String, ArtifactDefinition> toscaArtifacts = resource.getToscaArtifacts();
		assertThat(toscaArtifacts).hasSize(2);
		ArtifactDefinition artifactDefinition = toscaArtifacts.get(ARTIFACT_LABEL);
		assertThat(artifactDefinition.getArtifactName()).isEqualTo("resource-myResourceartifactnot-normalized.yml");
		ArtifactDefinition artifactDefinition2 = toscaArtifacts.get(ARTIFACT_LABEL2);
		assertThat(artifactDefinition2.getArtifactName()).isEqualTo("resource-myResourcealreadyNormalized.csar");
	}

	private Map<String, Object> buildArtifactMap(String artifactName) {
		Map<String, Object> artifact = new HashMap<>();
		artifact.put("artifactName", artifactName);
		return artifact;
	}

	private ArtifactDefinition buildArtifactDef(String artifactLabel) {
		ArtifactDefinition artifactDefinition = new ArtifactDefinition();
		artifactDefinition.setArtifactLabel(artifactLabel);
		return artifactDefinition;
	}
}
