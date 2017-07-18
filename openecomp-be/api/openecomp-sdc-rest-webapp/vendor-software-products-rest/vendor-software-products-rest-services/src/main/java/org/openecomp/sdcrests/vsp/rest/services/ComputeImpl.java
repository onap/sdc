package org.openecomp.sdcrests.vsp.rest.services;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.logging.context.MdcUtil;
import org.openecomp.sdc.logging.types.LoggerServiceName;
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
import org.openecomp.sdc.versioning.types.VersionableEntityAction;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collection;
import javax.inject.Named;
import javax.ws.rs.core.Response;

@Named
@Service("computes")
@Scope(value = "prototype")
public class ComputeImpl implements Compute {
  private ComputeManager computetManager =
      ComputeManagerFactory.getInstance().createInterface();
  private ComponentManager componentManager =
      ComponentManagerFactory.getInstance().createInterface();

  @Override
  public Response list(String vspId, String version, String componentId, String user) {
    MdcUtil.initMdc(LoggerServiceName.List_Computes.toString());
    Version vspVersion = resolveVspVersion(vspId, version, user, VersionableEntityAction.Read);
    componentManager.validateComponentExistence(vspId, vspVersion, componentId, user);
    Collection<ListComputeResponse> computes =
        computetManager
            .listCompute(vspId, vspVersion, componentId, user);

    MapComputeEntityToComputeDto mapper = new MapComputeEntityToComputeDto();
    GenericCollectionWrapper<ComputeDto> results = new GenericCollectionWrapper<>();
    for (ListComputeResponse compute : computes) {
      results.add(mapper.applyMapping(compute, ComputeDto.class));
    }

    return Response.ok(results).build();
  }

  @Override
  public Response get(String vspId, String version, String componentId, String computeId,
                      String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_Compute.toString());
    Version vspVersion = resolveVspVersion(vspId, version, user, VersionableEntityAction.Read);
    componentManager.validateComponentExistence(vspId, vspVersion, componentId, user);
    CompositionEntityResponse<ComputeData> response = computetManager
        .getCompute(vspId, vspVersion, componentId, computeId, user);

    CompositionEntityResponseDto<ComputeDetailsDto> responseDto = new
        CompositionEntityResponseDto<>();
    new MapCompositionEntityResponseToDto<>(new MapComputeDataToComputeDetailsDto(),
        ComputeDetailsDto.class).doMapping(response, responseDto);
    return Response.ok(responseDto).build();
  }

  @Override
  public Response create(ComputeDetailsDto request, String vspId, String versionId,
                         String componentId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Create_Compute.toString());
    ComputeEntity compute = new MapComputeDetailsDtoToComputeEntity().applyMapping(request,
        ComputeEntity.class);
    compute.setVspId(vspId);
    compute.setComponentId(componentId);
    compute.setVersion(resolveVspVersion(vspId, null, user, VersionableEntityAction.Write));
    componentManager.validateComponentExistence(vspId, compute.getVersion(), componentId, user);

    ComputeEntity createdCompute = computetManager.createCompute(compute, user);

    MapComputeEntityToComputeCreationDto mapper = new MapComputeEntityToComputeCreationDto();
    ComputeCreationDto createdComputeDto = mapper.applyMapping(createdCompute, ComputeCreationDto
        .class);
    return Response.ok(createdComputeDto != null ? createdComputeDto : null)
        .build();
  }

  @Override
  public Response update(ComputeDetailsDto request, String vspId, String versionId, String
      componentId, String computeFlavorId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Update_Compute.toString());
    ComputeEntity compute = new MapComputeDetailsDtoToComputeEntity().applyMapping(request,
        ComputeEntity.class);
    compute.setVspId(vspId);
    compute.setComponentId(componentId);
    compute.setVersion(resolveVspVersion(vspId, null, user, VersionableEntityAction.Write));
    compute.setId(computeFlavorId);

    componentManager.validateComponentExistence(vspId, compute.getVersion(), componentId, user);
    CompositionEntityValidationData validationData =
        computetManager.updateCompute(compute, user);
    return validationData != null && CollectionUtils.isNotEmpty(validationData.getErrors())
        ? Response.status(Response.Status.EXPECTATION_FAILED).entity(
        new MapCompositionEntityValidationDataToDto().applyMapping(validationData,
            CompositionEntityValidationDataDto.class)).build() : Response.ok().build();
  }

  @Override
  public Response delete(String vspId, String versionId, String componentId, String
      computeFlavorId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Delete_Compute.toString());
    Version version = resolveVspVersion(vspId, null, user, VersionableEntityAction.Write);
    componentManager.validateComponentExistence(vspId, version, componentId, user);
    computetManager.deleteCompute(vspId, version, componentId, computeFlavorId, user);
    return Response.ok().build();
  }

  @Override
  public Response getQuestionnaire(String vspId, String versionId, String componentId, String
      computeFlavorId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_Questionnaire_Compute.toString());
    Version vspVersion = resolveVspVersion(vspId, versionId, user, VersionableEntityAction.Read);
    componentManager.validateComponentExistence(vspId, vspVersion, componentId, user);
    QuestionnaireResponse questionnaireResponse = computetManager
        .getComputeQuestionnaire(vspId, vspVersion, componentId, computeFlavorId, user);

    QuestionnaireResponseDto result = new MapQuestionnaireResponseToQuestionnaireResponseDto()
        .applyMapping(questionnaireResponse, QuestionnaireResponseDto.class);
    return Response.ok(result).build();
  }

  @Override
  public Response updateQuestionnaire(String questionnaireData, String vspId, String versionId,
                                      String componentId,
                                      String computeFlavorId, String user) {
    MdcUtil
        .initMdc(LoggerServiceName.Update_Questionnaire_Compute.toString()
        );
    Version version = resolveVspVersion(vspId, null, user, VersionableEntityAction.Write);
    componentManager.validateComponentExistence(vspId, version, componentId, user);
    computetManager
        .updateComputeQuestionnaire(vspId, version, componentId, computeFlavorId,
            questionnaireData, user);
    return Response.ok().build();
  }
}
