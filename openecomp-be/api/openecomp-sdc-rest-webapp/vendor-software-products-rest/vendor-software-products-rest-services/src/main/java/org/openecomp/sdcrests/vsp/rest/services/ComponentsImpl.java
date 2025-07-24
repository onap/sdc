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
package org.openecomp.sdcrests.vsp.rest.services;

import java.util.Collection;
import javax.inject.Named;
import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentCreationDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentRequestDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityValidationDataDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireResponseDto;
import org.openecomp.sdcrests.vsp.rest.Components;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComponentDataToComponentDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComponentEntityToComponentCreationDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComponentEntityToComponentDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComponentRequestDtoToComponentEntity;
import org.openecomp.sdcrests.vsp.rest.mapping.MapCompositionEntityResponseToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapCompositionEntityValidationDataToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapQuestionnaireResponseToQuestionnaireResponseDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Named
@Service("components")
@Scope(value = "prototype")
public class ComponentsImpl implements Components {

    private final ComponentManager componentManager;

    public ComponentsImpl() {
        this.componentManager = ComponentManagerFactory.getInstance().createInterface();
    }

    public ComponentsImpl(ComponentManager componentManager) {
        this.componentManager = componentManager;
    }

    @Override
    public ResponseEntity list(String vspId, String versionId, String user) {
        Collection<ComponentEntity> components = componentManager.listComponents(vspId, new Version(versionId));
        MapComponentEntityToComponentDto mapper = new MapComponentEntityToComponentDto();
        GenericCollectionWrapper<ComponentDto> results = new GenericCollectionWrapper<>();
        for (ComponentEntity component : components) {
            results.add(mapper.applyMapping(component, ComponentDto.class));
        }
        return ResponseEntity.ok(results);
    }

    @Override
    public ResponseEntity deleteList(String vspId, String versionId, String user) {
        componentManager.deleteComponents(vspId, new Version(versionId));
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity create(ComponentRequestDto request, String vspId, String versionId, String user) {
        ComponentEntity component = new MapComponentRequestDtoToComponentEntity().applyMapping(request, ComponentEntity.class);
        component.setVspId(vspId);
        component.setVersion(new Version(versionId));
        ComponentEntity createdComponent = componentManager.createComponent(component);
        MapComponentEntityToComponentCreationDto mapping = new MapComponentEntityToComponentCreationDto();
        ComponentCreationDto createdComponentDto = mapping.applyMapping(createdComponent, ComponentCreationDto.class);
        return ResponseEntity.ok(createdComponent != null ? createdComponentDto : null);
    }

    @Override
    public ResponseEntity get(String vspId, String versionId, String componentId, String user) {
        CompositionEntityResponse<ComponentData> response = componentManager.getComponent(vspId, new Version(versionId), componentId);
        CompositionEntityResponseDto<ComponentDto> responseDto = new CompositionEntityResponseDto<>();
        new MapCompositionEntityResponseToDto<>(new MapComponentDataToComponentDto(), ComponentDto.class).doMapping(response, responseDto);
        return ResponseEntity.ok(responseDto);
    }

    @Override
    public ResponseEntity delete(String vspId, String versionId, String componentId, String user) {
        componentManager.deleteComponent(vspId, new Version(versionId), componentId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity update(ComponentRequestDto request, String vspId, String versionId, String componentId, String user) {
        ComponentEntity componentEntity = new MapComponentRequestDtoToComponentEntity().applyMapping(request, ComponentEntity.class);
        componentEntity.setVspId(vspId);
        componentEntity.setVersion(new Version(versionId));
        componentEntity.setId(componentId);
        CompositionEntityValidationData validationData = componentManager.updateComponent(componentEntity);
        return validationData != null && CollectionUtils.isNotEmpty(validationData.getErrors()) ? ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
            .body(new MapCompositionEntityValidationDataToDto().applyMapping(validationData, CompositionEntityValidationDataDto.class))
            : ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity getQuestionnaire(String vspId, String versionId, String componentId, String user) {
        QuestionnaireResponse questionnaireResponse = componentManager.getQuestionnaire(vspId, new Version(versionId), componentId);
        QuestionnaireResponseDto result = new MapQuestionnaireResponseToQuestionnaireResponseDto()
            .applyMapping(questionnaireResponse, QuestionnaireResponseDto.class);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity updateQuestionnaire(String questionnaireData, String vspId, String versionId, String componentId, String user) {
        componentManager.updateQuestionnaire(vspId, new Version(versionId), componentId, questionnaireData);
        return ResponseEntity.ok().build();
    }
}
