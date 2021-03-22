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
import org.openecomp.sdc.vendorsoftwareproduct.DeploymentFlavorManager;
import org.openecomp.sdc.vendorsoftwareproduct.DeploymentFlavorManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.CompositionEntityValidationDataDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.DeploymentFlavorCreationDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.DeploymentFlavorDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.DeploymentFlavorListResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.DeploymentFlavorRequestDto;
import org.openecomp.sdcrests.vsp.rest.DeploymentFlavors;
import org.openecomp.sdcrests.vsp.rest.mapping.MapCompositionEntityResponseToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapCompositionEntityValidationDataToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapDeploymentFlavorEntityDeploymentFlavorToListResponse;
import org.openecomp.sdcrests.vsp.rest.mapping.MapDeploymentFlavorEntityToDeploymentFlavorCreationDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapDeploymentFlavorRequestDtoToDeploymentFlavorEntity;
import org.openecomp.sdcrests.vsp.rest.mapping.MapDeploymentFlavorToDeploymentDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Named
@Service("deploymentFlavors")
@Scope(value = "prototype")
public class DeploymentFlavorsImpl implements DeploymentFlavors {

    private final DeploymentFlavorManager deploymentFlavorManager;

    public DeploymentFlavorsImpl() {
        this.deploymentFlavorManager = DeploymentFlavorManagerFactory.getInstance().createInterface();
    }

    public DeploymentFlavorsImpl(DeploymentFlavorManager deploymentFlavorManager) {
        this.deploymentFlavorManager = deploymentFlavorManager;
    }

    @Override
    public Response create(DeploymentFlavorRequestDto request, String vspId, String versionId, String user) {
        DeploymentFlavorEntity deploymentFlavorEntity = new MapDeploymentFlavorRequestDtoToDeploymentFlavorEntity()
            .applyMapping(request, DeploymentFlavorEntity.class);
        deploymentFlavorEntity.setVspId(vspId);
        deploymentFlavorEntity.setVersion(new Version(versionId));
        DeploymentFlavorEntity createdDeploymentFlavor = deploymentFlavorManager.createDeploymentFlavor(deploymentFlavorEntity);
        MapDeploymentFlavorEntityToDeploymentFlavorCreationDto mapping = new MapDeploymentFlavorEntityToDeploymentFlavorCreationDto();
        DeploymentFlavorCreationDto deploymentFlavorCreatedDto = mapping.applyMapping(createdDeploymentFlavor, DeploymentFlavorCreationDto.class);
        return Response.ok(createdDeploymentFlavor != null ? deploymentFlavorCreatedDto : null).build();
    }

    @Override
    public Response list(String vspId, String versionId, String user) {
        Collection<DeploymentFlavorEntity> deploymentFlavors = deploymentFlavorManager.listDeploymentFlavors(vspId, new Version(versionId));
        MapDeploymentFlavorEntityDeploymentFlavorToListResponse mapper = new MapDeploymentFlavorEntityDeploymentFlavorToListResponse();
        GenericCollectionWrapper<DeploymentFlavorListResponseDto> results = new GenericCollectionWrapper<>();
        for (DeploymentFlavorEntity deploymentFlavor : deploymentFlavors) {
            results.add(mapper.applyMapping(deploymentFlavor, DeploymentFlavorListResponseDto.class));
        }
        return Response.ok(results).build();
    }

    @Override
    public Response get(String vspId, String versionId, String deploymentFlavorId, String user) {
        CompositionEntityResponse<DeploymentFlavor> response = deploymentFlavorManager
            .getDeploymentFlavor(vspId, new Version(versionId), deploymentFlavorId);
        CompositionEntityResponseDto<DeploymentFlavorDto> responseDto = new CompositionEntityResponseDto<>();
        new MapCompositionEntityResponseToDto<>(new MapDeploymentFlavorToDeploymentDto(), DeploymentFlavorDto.class).doMapping(response, responseDto);
        return Response.ok(responseDto).build();
    }

    @Override
    public Response getSchema(String vspId, String versionId, String user) {
        CompositionEntityResponse<DeploymentFlavor> response = deploymentFlavorManager.getDeploymentFlavorSchema(vspId, new Version(versionId));
        return Response.ok(response).build();
    }

    @Override
    public Response delete(String vspId, String versionId, String deploymentFlavorId, String user) {
        Version version = new Version(versionId);
        deploymentFlavorManager.deleteDeploymentFlavor(vspId, version, deploymentFlavorId);
        return Response.ok().build();
    }

    @Override
    public Response update(DeploymentFlavorRequestDto request, String vspId, String versionId, String deploymentFlavorId, String user) {
        DeploymentFlavorEntity deploymentFlavorEntity = new MapDeploymentFlavorRequestDtoToDeploymentFlavorEntity()
            .applyMapping(request, DeploymentFlavorEntity.class);
        deploymentFlavorEntity.setVspId(vspId);
        deploymentFlavorEntity.setVersion(new Version(versionId));
        deploymentFlavorEntity.setId(deploymentFlavorId);
        CompositionEntityValidationData validationData = deploymentFlavorManager.updateDeploymentFlavor(deploymentFlavorEntity);
        return validationData != null && CollectionUtils.isNotEmpty(validationData.getErrors()) ? Response.status(Response.Status.EXPECTATION_FAILED)
            .entity(new MapCompositionEntityValidationDataToDto().applyMapping(validationData, CompositionEntityValidationDataDto.class)).build()
            : Response.ok().build();
    }
}
