package org.openecomp.sdcrests.vsp.rest.services;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.logging.context.MdcUtil;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.DeploymentFlavorManager;
import org.openecomp.sdc.vendorsoftwareproduct.DeploymentFlavorManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;
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

import java.util.Collection;
import javax.inject.Named;
import javax.ws.rs.core.Response;

@Named
@Service("deploymentFlavors")
@Scope(value = "prototype")
public class DeploymentFlavorsImpl implements DeploymentFlavors
{
  private DeploymentFlavorManager deploymentFlavorManager =
      DeploymentFlavorManagerFactory.getInstance().createInterface();

  @Override
  public Response create(DeploymentFlavorRequestDto request, String vspId, String versionId,
                         String user) {
    MdcUtil.initMdc(LoggerServiceName.Create_Deployment_Flavor.toString());
    DeploymentFlavorEntity deploymentFlavorEntity =
        new MapDeploymentFlavorRequestDtoToDeploymentFlavorEntity()
            .applyMapping(request, DeploymentFlavorEntity.class);
    deploymentFlavorEntity.setVspId(vspId);
    deploymentFlavorEntity.setVersion(resolveVspVersion(vspId, null, user,
        VersionableEntityAction.Write));
    DeploymentFlavorEntity createdDeploymentFlavor =
        deploymentFlavorManager.createDeploymentFlavor(deploymentFlavorEntity, user);
    MapDeploymentFlavorEntityToDeploymentFlavorCreationDto mapping =
        new MapDeploymentFlavorEntityToDeploymentFlavorCreationDto();
    DeploymentFlavorCreationDto deploymentFlavorCreatedDto= mapping.applyMapping
        (createdDeploymentFlavor,DeploymentFlavorCreationDto.class);
    return Response
        .ok(createdDeploymentFlavor != null ? deploymentFlavorCreatedDto : null)
        .build();
  }

  @Override
  public Response list(String vspId,String version,String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_List_Deployment_flavor.toString());
    Collection<DeploymentFlavorEntity> deploymentFlavors =
        deploymentFlavorManager.listDeploymentFlavors(vspId, resolveVspVersion(vspId, version, user,
            VersionableEntityAction.Read), user);

    MapDeploymentFlavorEntityDeploymentFlavorToListResponse mapper = new
        MapDeploymentFlavorEntityDeploymentFlavorToListResponse();
    GenericCollectionWrapper<DeploymentFlavorListResponseDto> results = new GenericCollectionWrapper<>();
    for (DeploymentFlavorEntity deploymentFlavor : deploymentFlavors) {
      results.add(mapper.applyMapping(deploymentFlavor, DeploymentFlavorListResponseDto.class));
    }
    return Response.ok(results).build();
  }

  @Override
  public Response get(String vspId, String version, String deploymentFlavorId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_Deployment_flavor.toString());
    CompositionEntityResponse<DeploymentFlavor> response = deploymentFlavorManager
        .getDeploymentFlavor(vspId, resolveVspVersion(vspId, version, user,
            VersionableEntityAction.Read), deploymentFlavorId, user);

    CompositionEntityResponseDto<DeploymentFlavorDto> responseDto = new
        CompositionEntityResponseDto<>();
    new MapCompositionEntityResponseToDto<>(new MapDeploymentFlavorToDeploymentDto(), DeploymentFlavorDto.class)
        .doMapping(response, responseDto);
    return Response.ok(responseDto).build();
  }

  @Override
  public Response getSchema(String vspId, String version, String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_Deployment_flavor.toString());
    CompositionEntityResponse<DeploymentFlavor> response = deploymentFlavorManager
        .getDeploymentFlavorSchema(vspId, Version.valueOf(version), user);
    return Response.ok(response).build();
  }

  @Override
  public Response delete(String vspId, String versionId, String deploymentFlavorId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Delete_Deployment_flavor.toString());
    Version version = resolveVspVersion(vspId, null, user, VersionableEntityAction.Write);
    deploymentFlavorManager.deleteDeploymentFlavor(vspId, version, deploymentFlavorId, user);
    return Response.ok().build();
  }

  @Override
  public Response update(DeploymentFlavorRequestDto request, String vspId, String versionId,
                         String deploymentFlavorId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Update_Deployment_flavor.toString());
    DeploymentFlavorEntity deploymentFlavorEntity =
        new MapDeploymentFlavorRequestDtoToDeploymentFlavorEntity().applyMapping(request,
            DeploymentFlavorEntity.class);
    deploymentFlavorEntity.setVspId(vspId);
    deploymentFlavorEntity.setVersion(resolveVspVersion(vspId, null, user, VersionableEntityAction.Write));
    deploymentFlavorEntity.setId(deploymentFlavorId);

    CompositionEntityValidationData validationData =
        deploymentFlavorManager.updateDeploymentFlavor(deploymentFlavorEntity, user);
    return validationData != null && CollectionUtils.isNotEmpty(validationData.getErrors())
        ? Response.status(Response.Status.EXPECTATION_FAILED).entity(
        new MapCompositionEntityValidationDataToDto().applyMapping(validationData,
            CompositionEntityValidationDataDto.class)).build() : Response.ok().build();
  }
}
