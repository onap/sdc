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

package org.openecomp.sdc.be.ecomp.converters;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fj.data.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.externalapi.servlet.representation.AssetMetadata;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.exception.ResponseFormat;

@ExtendWith(MockitoExtension.class)
class AssetMetadataConverterTest {

    @InjectMocks
    @Spy
    private AssetMetadataConverter testSubject;
    @Mock
    private ComponentsUtils componentsUtils;
    @Mock
    private ToscaOperationFacade toscaOperationFacade;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConvertToAssetMetadata_emptyComponentList() throws Exception {
        List<? extends Component> componentList = null;
        String serverBaseURL = "";
        boolean detailed = false;
        Either<List<? extends AssetMetadata>, ResponseFormat> result;

        result = testSubject.convertToAssetMetadata(componentList, serverBaseURL, detailed, null);
        assertTrue(result.isLeft());
    }

    @Test
    void testConvertToAssetMetadata_withComponentList_Service() throws Exception {
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setEsId("mock");
        artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
        deploymentArtifacts.put("mock", artifactDefinition);
        Service component = new Service();
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        component.setComponentType(ComponentTypeEnum.SERVICE);
        component.setDistributionStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setComponentUid("mock");
        componentInstance.setOriginType(OriginTypeEnum.VFC);
        componentInstance.setDeploymentArtifacts(deploymentArtifacts);
        component.setComponentInstances(Collections.singletonList(componentInstance));
        component.setDeploymentArtifacts(deploymentArtifacts);
        component.setCategories(Collections.singletonList(new CategoryDefinition()));
        Map<String, String> categorySpecificMetadata = new HashMap<>();
        categorySpecificMetadata.put("mock_key", "mock_value");
        component.setCategorySpecificMetadata(categorySpecificMetadata);
        String serverBaseURL = "";
        boolean detailed = true;

        when(toscaOperationFacade.getToscaElement(anyString())).thenReturn(Either.left(new Resource()));

        Either<List<? extends AssetMetadata>, ResponseFormat> result;
        List<String> additionalMetadataKeysToInclude = new ArrayList<>();
        additionalMetadataKeysToInclude.add("description");
        additionalMetadataKeysToInclude.add("mock_key");
        result = testSubject.convertToAssetMetadata(Collections.singletonList(component), serverBaseURL, detailed, additionalMetadataKeysToInclude);
        assertTrue(result.isLeft());
    }

    @Test
    void testConvertToAssetMetadata_withComponentList_Resource() throws Exception {
        ArtifactDefinition artifactDefinition = new ArtifactDefinition();
        artifactDefinition.setEsId("mock");
        artifactDefinition.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
        Map<String, ArtifactDefinition> deploymentArtifacts = new HashMap<>();
        deploymentArtifacts.put("mock", artifactDefinition);
        Resource component = new Resource();
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        component.setComponentType(ComponentTypeEnum.RESOURCE);
        ComponentInstance componentInstance = new ComponentInstance();
        componentInstance.setComponentUid("mock");
        componentInstance.setOriginType(OriginTypeEnum.VFC);
        componentInstance.setDeploymentArtifacts(deploymentArtifacts);
        componentInstance.setNormalizedName("mock");
        component.setComponentInstances(Collections.singletonList(componentInstance));
        component.setDeploymentArtifacts(deploymentArtifacts);
        CategoryDefinition categoryDefinition = new CategoryDefinition();
        categoryDefinition.setSubcategories(Collections.singletonList(new SubCategoryDefinition()));
        component.setCategories(Collections.singletonList(categoryDefinition));
        Map<String, String> categorySpecificMetadata = new HashMap<>();
        categorySpecificMetadata.put("mock_key", "mock_value");
        component.setCategorySpecificMetadata(categorySpecificMetadata);
        String serverBaseURL = "";
        boolean detailed = true;

        when(toscaOperationFacade.getToscaElement(anyString())).thenReturn(Either.left(new Resource()));

        Either<List<? extends AssetMetadata>, ResponseFormat> result;
        List<String> additionalMetadataKeysToInclude = new ArrayList<>();
        additionalMetadataKeysToInclude.add("description");
        additionalMetadataKeysToInclude.add("mock_key");
//        additionalMetadataKeysToInclude.add("NoSuchElementException");
        result = testSubject.convertToAssetMetadata(Collections.singletonList(component), serverBaseURL, detailed, additionalMetadataKeysToInclude);
        assertTrue(result.isLeft());
    }

    @Test
    void testConvertToSingleAssetMetadata_Resource() throws Exception {

        Resource component = new Resource();
        String serverBaseURL = "";
        boolean detailed = false;
        Either<? extends AssetMetadata, ResponseFormat> result;
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        component.setComponentType(ComponentTypeEnum.RESOURCE);

        result = testSubject.convertToSingleAssetMetadata(component, serverBaseURL, detailed, null);
        assertTrue(result.isLeft());
    }

    @Test
    void testConvertToSingleAssetMetadata_Product() throws Exception {

        Product component = new Product();
        String serverBaseURL = "";
        boolean detailed = false;
        Either<? extends AssetMetadata, ResponseFormat> result;
        component.setComponentType(ComponentTypeEnum.PRODUCT);

        when(componentsUtils.getResponseFormatAdditionalProperty(ActionStatus.COMPONENT_INVALID_CATEGORY)).thenReturn(new ResponseFormat());

        result = testSubject.convertToSingleAssetMetadata(component, serverBaseURL, detailed, null);
        assertTrue(result.isRight());
    }

    @Test
    void testConvertToSingleAssetMetadata_Service() throws Exception {

        Service component = new Service();
        String serverBaseURL = "";
        boolean detailed = false;
        Either<? extends AssetMetadata, ResponseFormat> result;
        component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
        component.setComponentType(ComponentTypeEnum.SERVICE);
        component.setDistributionStatus(DistributionStatusEnum.DISTRIBUTION_NOT_APPROVED);

        result = testSubject.convertToSingleAssetMetadata(component, serverBaseURL, detailed, null);
        assertTrue(result.isLeft());
    }

}
