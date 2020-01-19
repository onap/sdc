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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.be.components;

import fj.data.Either;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.DummyConfigurationManager;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentBusinessLogic;
import org.openecomp.sdc.be.components.impl.ComponentInstanceBusinessLogic;
import org.openecomp.sdc.be.components.utils.ComponentBusinessLogicMock;
import org.openecomp.sdc.be.components.utils.ResourceBuilder;
import org.openecomp.sdc.be.components.validation.component.ComponentNameValidator;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.ui.model.UiComponentDataTransfer;
import org.openecomp.sdc.be.ui.model.UiLeftPaletteComponent;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.openecomp.sdc.be.components.impl.utils.TestGenerationUtils.getComponentsUtils;
import static org.openecomp.sdc.common.util.GeneralUtility.getCategorizedComponents;

@RunWith(MockitoJUnitRunner.class)
public class ComponentBusinessLogicTest extends ComponentBusinessLogicMock {

    private static final User USER = new User();
    private static final String ARTIFACT_LABEL = "toscaArtifact1";
    private static final String ARTIFACT_LABEL2 = "toscaArtifact2";

    private ComponentBusinessLogic testInstance = new ComponentBusinessLogic(elementDao, groupOperation, groupInstanceOperation,
        groupTypeOperation, groupBusinessLogic, interfaceOperation, interfaceLifecycleTypeOperation, artifactsBusinessLogic,
        artifactToscaOperation,componentContactIdValidator, componentNameValidator, componentTagsValidator, componentValidator,
            componentIconValidator, componentProjectCodeValidator, componentDescriptionValidator) {
        @Override
        public Either<List<String>, ResponseFormat> deleteMarkedComponents() {
            return null;
        }

        @Override
        public ComponentInstanceBusinessLogic getComponentInstanceBL() {
            return null;
        }

        @Override
        public Either<List<ComponentInstance>, ResponseFormat> getComponentInstancesFilteredByPropertiesAndInputs(String componentId, String userId) {
            return null;
        }

        @Override
        public Either<UiComponentDataTransfer, ResponseFormat> getUiComponentDataTransferByComponentId(String componentId, List<String> dataParamsToReturn) {
            return null;
        }
    };


    DummyConfigurationManager dummyConfigurationManager = new DummyConfigurationManager();;

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
        when(dummyConfigurationManager.getConfigurationMock().getToscaArtifacts()).thenReturn(artifactsFromConfig);
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

    @Test
    public void categorizeOneResource(){
        List<Component> componentList = new ArrayList<>();
        String subCategoryName = "Load Balancer";
        String categoryName = "Application L4+";
        Component component = initComponent(ComponentTypeEnum.RESOURCE, subCategoryName, categoryName);
        componentList.add(component);
        Map<String,Map<String,List<UiLeftPaletteComponent>>> response = getCategorizedComponents(getComponentsUtils().convertComponentToUiLeftPaletteComponentObject(componentList));
        assertThat(response.get(categoryName).get(subCategoryName).size()).isEqualTo(1);
    }

    @Test
    public void categorizeResourcesSameCategoryDifferentSubcategory(){
        List<Component> componentList = new ArrayList<>();
        String categoryName = "Application L4+";
        String subCategoryName = "Load Balancer";
        Component component = initComponent(ComponentTypeEnum.RESOURCE, subCategoryName, categoryName);
        componentList.add(component);
        String subCategoryName2 = "Database";
        Component component2 = initComponent(ComponentTypeEnum.RESOURCE, subCategoryName2, categoryName);
        componentList.add(component2);
        Map<String,Map<String,List<UiLeftPaletteComponent>>> response = getCategorizedComponents(getComponentsUtils().convertComponentToUiLeftPaletteComponentObject(componentList));
        assertThat(response.get(categoryName).get(subCategoryName).size()).isEqualTo(1);
        assertThat(response.get(categoryName).get(subCategoryName2).size()).isEqualTo(1);
    }

    @Test
    public void categorizeResourceAndServiceSameCategoryDifferentSubcategory(){
        List<Component> componentList = new ArrayList<>();
        String categoryName = "Generic";
        String subCategoryName = "Load Balancer";
        Component component = initComponent(ComponentTypeEnum.RESOURCE, subCategoryName, categoryName);
        componentList.add(component);
        Component component2 = initComponent(ComponentTypeEnum.SERVICE, null, categoryName);
        componentList.add(component2);
        Map<String,Map<String,List<UiLeftPaletteComponent>>> response = getCategorizedComponents(getComponentsUtils().convertComponentToUiLeftPaletteComponentObject(componentList));
        assertThat(response.get(categoryName).get(subCategoryName).size()).isEqualTo(1);
        assertThat(response.get("Generic").get("Generic").size()).isEqualTo(1);
    }

    @Test
    public void categorizeResourcesSameCategorySameSubcategory(){
        List<Component> componentList = new ArrayList<>();
        String categoryName = "Application L4+";
        String subCategoryName = "Load Balancer";
        Component component = initComponent(ComponentTypeEnum.RESOURCE, subCategoryName, categoryName);
        componentList.add(component);
        componentList.add(component);
        Map<String,Map<String,List<UiLeftPaletteComponent>>> response = getCategorizedComponents(getComponentsUtils().convertComponentToUiLeftPaletteComponentObject(componentList));
        assertThat(response.get(categoryName).get(subCategoryName).size()).isEqualTo(2);
    }

    @Test
    public void categorizeTwoServices(){
        List<Component> componentList = new ArrayList<>();
        String categoryName = "Application L4+";
        String categoryName2 = "IP Mux Demux";
        Component component = initComponent(ComponentTypeEnum.SERVICE, null, categoryName);
        componentList.add(component);
        Component component2 = initComponent(ComponentTypeEnum.SERVICE, null, categoryName2);
        componentList.add(component2);
        Map<String,Map<String,List<UiLeftPaletteComponent>>> response = getCategorizedComponents(getComponentsUtils().convertComponentToUiLeftPaletteComponentObject(componentList));
        assertThat(response.get("Generic").get("Generic").size()).isEqualTo(2);
    }

    @Test
    public void categorizeTwoResourcesDiffCategory(){
        List<Component> componentList = new ArrayList<>();
        String categoryName = "Application L4+";
        String categoryName2 = "IP Mux Demux";
        String subCategoryName = "Load Balancer";
        Component component = initComponent(ComponentTypeEnum.RESOURCE, subCategoryName, categoryName);
        componentList.add(component);
        Component component2 = initComponent(ComponentTypeEnum.RESOURCE, subCategoryName, categoryName2);
        componentList.add(component2);
        Map<String,Map<String,List<UiLeftPaletteComponent>>> response = getCategorizedComponents(getComponentsUtils().convertComponentToUiLeftPaletteComponentObject(componentList));
        assertThat(response.get(categoryName).get(subCategoryName).size()).isEqualTo(1);
        assertThat(response.get(categoryName2).get(subCategoryName).size()).isEqualTo(1);
    }

    private Component initComponent(ComponentTypeEnum componentTypeEnum, String subCategoryName, String categoryName) {
        Component component = null;
        if(componentTypeEnum == ComponentTypeEnum.RESOURCE){
            component = new Resource();
        }
        if(componentTypeEnum == ComponentTypeEnum.SERVICE){
            component = new Service();
        }
        component.setComponentType(componentTypeEnum);
        CategoryDefinition categoryDefinition = new CategoryDefinition();
        SubCategoryDefinition subCategoryDefinition = new SubCategoryDefinition();
        subCategoryDefinition.setName(subCategoryName);
        List<SubCategoryDefinition> subCategoryDefinitionList = new ArrayList<>();
        subCategoryDefinitionList.add(subCategoryDefinition);
        categoryDefinition.setSubcategories(subCategoryDefinitionList);
        categoryDefinition.setName(categoryName);
        List<CategoryDefinition> categoryDefinitionList = new ArrayList<>();
        categoryDefinitionList.add(categoryDefinition);
        component.setCategories(categoryDefinitionList);
        List<String> tags = new ArrayList<>();
        tags.add("tag1");
        tags.add("tag2");
        component.setTags(tags);
        return component;
    }
}
