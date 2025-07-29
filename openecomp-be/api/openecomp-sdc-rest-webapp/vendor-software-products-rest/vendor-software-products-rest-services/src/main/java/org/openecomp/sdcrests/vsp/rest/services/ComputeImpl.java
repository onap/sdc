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
package org.openecomp.sdcrests.vsp.rest.services;

import java.util.Collection;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ComputeManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComputeManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ListComputeResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComputeData;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityValidationDataDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComputeCreationDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComputeDetailsDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComputeDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireResponseDto;
import org.openecomp.sdcrests.vsp.rest.Compute;
import org.openecomp.sdcrests.vsp.rest.mapping.MapCompositionEntityResponseToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapCompositionEntityValidationDataToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComputeDataToComputeDetailsDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComputeDetailsDtoToComputeEntity;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComputeEntityToComputeCreationDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComputeEntityToComputeDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapQuestionnaireResponseToQuestionnaireResponseDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.context.annotation.ScopedProxyMode;
@Named
@Service("computes")
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ComputeImpl implements Compute {

    private final ComputeManager computetManager;
    private final ComponentManager componentManager;

    public ComputeImpl() {
        this.computetManager = ComputeManagerFactory.getInstance().createInterface();
        this.componentManager = ComponentManagerFactory.getInstance().createInterface();
    }

    public ComputeImpl(ComputeManager computetManager, ComponentManager componentManager) {
        this.computetManager = computetManager;
        this.componentManager = componentManager;
    }

    @Override
    public ResponseEntity list(String vspId, String versionId, String componentId, String user) {
        Version version = new Version(versionId);
        componentManager.validateComponentExistence(vspId, version, componentId);
        Collection<ListComputeResponse> computes = computetManager.listComputes(vspId, version, componentId);
        MapComputeEntityToComputeDto mapper = new MapComputeEntityToComputeDto();
        GenericCollectionWrapper<ComputeDto> results = new GenericCollectionWrapper<>();
        for (ListComputeResponse compute : computes) {
            results.add(mapper.applyMapping(compute, ComputeDto.class));
        }
        return ResponseEntity.ok(results);
    }

    @Override
    public ResponseEntity get(String vspId, String versionId, String componentId, String computeId, String user) {
        Version version = new Version(versionId);
        componentManager.validateComponentExistence(vspId, version, componentId);
        CompositionEntityResponse<ComputeData> response = computetManager.getCompute(vspId, version, componentId, computeId);
        CompositionEntityResponseDto<ComputeDetailsDto> responseDto = new CompositionEntityResponseDto<>();
        new MapCompositionEntityResponseToDto<>(new MapComputeDataToComputeDetailsDto(), ComputeDetailsDto.class).doMapping(response, responseDto);
        return ResponseEntity.ok(responseDto);
    }

    @Override
    public ResponseEntity create(ComputeDetailsDto request, String vspId, String versionId, String componentId, String user) {
        ComputeEntity compute = new MapComputeDetailsDtoToComputeEntity().applyMapping(request, ComputeEntity.class);
        compute.setVspId(vspId);
        compute.setVersion(new Version(versionId));
        compute.setComponentId(componentId);
        componentManager.validateComponentExistence(vspId, compute.getVersion(), componentId);
        ComputeEntity createdCompute = computetManager.createCompute(compute);
        MapComputeEntityToComputeCreationDto mapper = new MapComputeEntityToComputeCreationDto();
        ComputeCreationDto createdComputeDto = mapper.applyMapping(createdCompute, ComputeCreationDto.class);
        return ResponseEntity.ok(createdComputeDto != null ? createdComputeDto : null);
    }

    @Override
    public ResponseEntity update(ComputeDetailsDto request, String vspId, String versionId, String componentId, String computeFlavorId, String user) {
        ComputeEntity compute = new MapComputeDetailsDtoToComputeEntity().applyMapping(request, ComputeEntity.class);
        compute.setVspId(vspId);
        compute.setVersion(new Version(versionId));
        compute.setComponentId(componentId);
        compute.setId(computeFlavorId);
        componentManager.validateComponentExistence(vspId, compute.getVersion(), componentId);
        CompositionEntityValidationData validationData = computetManager.updateCompute(compute);
        return validationData != null && CollectionUtils.isNotEmpty(validationData.getErrors()) ? ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
            .body(new MapCompositionEntityValidationDataToDto().applyMapping(validationData, CompositionEntityValidationDataDto.class))
            : ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity delete(String vspId, String versionId, String componentId, String computeFlavorId, String user) {
        Version version = new Version(versionId);
        componentManager.validateComponentExistence(vspId, version, componentId);
        computetManager.deleteCompute(vspId, version, componentId, computeFlavorId);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity getQuestionnaire(String vspId, String versionId, String componentId, String computeFlavorId, String user) {
        Version version = new Version(versionId);
        componentManager.validateComponentExistence(vspId, version, componentId);
        QuestionnaireResponse questionnaireResponse = computetManager.getComputeQuestionnaire(vspId, version, componentId, computeFlavorId);
        QuestionnaireResponseDto result = new MapQuestionnaireResponseToQuestionnaireResponseDto()
            .applyMapping(questionnaireResponse, QuestionnaireResponseDto.class);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity updateQuestionnaire(String questionnaireData, String vspId, String versionId, String componentId, String computeFlavorId,
                                        String user) {
        Version version = new Version(versionId);
        componentManager.validateComponentExistence(vspId, version, componentId);
        computetManager.updateComputeQuestionnaire(vspId, version, componentId, computeFlavorId, questionnaireData);
        return ResponseEntity.ok().build();
    }
}
