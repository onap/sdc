package org.openecomp.sdcrests.vsp.rest.services;

import org.apache.commons.beanutils.BeanUtils;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.MdcUtil;
import org.openecomp.sdc.logging.messages.AuditMessages;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.types.FileDataStructureDto;
import org.openecomp.sdc.vendorsoftwareproduct.types.OrchestrationTemplateActionResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.OrchestrationTemplateActionResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.UploadFileResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ValidationResponseDto;
import org.openecomp.sdcrests.vsp.rest.OrchestrationTemplateCandidate;
import org.openecomp.sdcrests.vsp.rest.mapping.MapUploadFileResponseToUploadFileResponseDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapValidationResponseToDto;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Named
@Service("orchestrationTemplateCandidate")
@Scope(value = "prototype")
public class OrchestrationTemplateCandidateImpl implements OrchestrationTemplateCandidate {

  private OrchestrationTemplateCandidateManager candidateManager =
      OrchestrationTemplateCandidateManagerFactory.getInstance().createInterface();
  private static final Logger logger =
          LoggerFactory.getLogger(OrchestrationTemplateCandidateImpl.class);

  @Override
  public Response upload(String vspId, String versionId, InputStream heatFileToUpload,
                         String user) {
    MdcUtil.initMdc(LoggerServiceName.Upload_File.toString());
    logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.UPLOAD_HEAT + vspId);

    UploadFileResponse uploadFileResponse = candidateManager
        .upload(vspId, resolveVspVersion(vspId, null, user, VersionableEntityAction
            .Write), heatFileToUpload, user);
    UploadFileResponseDto uploadFileResponseDto = new MapUploadFileResponseToUploadFileResponseDto()
        .applyMapping(uploadFileResponse, UploadFileResponseDto.class);

    return Response.ok(uploadFileResponseDto).build();
  }

  @Override
  public Response get(String vspId, String versionId, String user) throws IOException {
    Optional<byte[]> zipFile =
        candidateManager
            .get(vspId, resolveVspVersion(vspId, null, user, VersionableEntityAction.Read), user);

    if (!zipFile.isPresent()) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    Response.ResponseBuilder response = Response.ok(zipFile.get());
    response.header("Content-Disposition", "attachment; filename=HeatCandidate.zip");
    return response.build();
  }

  @Override
  public Response process(String vspId, String versionId, String user)
      throws InvocationTargetException, IllegalAccessException {

    OrchestrationTemplateActionResponse response =
        candidateManager
            .process(vspId, resolveVspVersion(vspId, null, user, VersionableEntityAction.Write),
                user);
    OrchestrationTemplateActionResponseDto responseDto =
        new OrchestrationTemplateActionResponseDto();
    BeanUtils.copyProperties(responseDto, response);

    return Response.ok(responseDto).build();
  }



  @Override
  public Response updateFilesDataStructure(
      String vspId, String versionId, FileDataStructureDto fileDataStructureDto, String user)
      throws Exception {

    FilesDataStructure fileDataStructure = new FilesDataStructure();
    try {
      BeanUtils.copyProperties(fileDataStructure, fileDataStructureDto);
    } catch (IllegalAccessException | InvocationTargetException exception) {
      String errorWithParameters = ErrorMessagesFormatBuilder
          .getErrorWithParameters(Messages.MAPPING_OBJECTS_FAILURE.getErrorMessage(),
              fileDataStructureDto.toString(), fileDataStructure.toString());
      throw new Exception(errorWithParameters, exception);
    }
    ValidationResponse response = candidateManager
        .updateFilesDataStructure(vspId,
            resolveVspVersion(vspId, null, user, VersionableEntityAction
                .Write), user, fileDataStructure);

    if (!response.isValid()) {
      return Response.status(Response.Status.EXPECTATION_FAILED).entity(
          new MapValidationResponseToDto()
              .applyMapping(response, ValidationResponseDto.class)).build();
    }
    return Response.ok(fileDataStructureDto).build();
  }

  @Override
  public Response getFilesDataStructure(String vspId, String versionId, String user)
      throws Exception {
    Optional<FilesDataStructure> filesDataStructure = candidateManager
        .getFilesDataStructure(vspId, resolveVspVersion(vspId, null, user, VersionableEntityAction
            .Read), user);
    FileDataStructureDto fileDataStructureDto = new FileDataStructureDto();
    if (filesDataStructure.isPresent()) {
      try {
        BeanUtils.copyProperties(fileDataStructureDto, filesDataStructure.get());
      } catch (IllegalAccessException | InvocationTargetException exception) {
        String errorWithParameters = ErrorMessagesFormatBuilder
            .getErrorWithParameters(Messages.MAPPING_OBJECTS_FAILURE.getErrorMessage(),
                filesDataStructure.toString(), fileDataStructureDto.toString());
        throw new Exception(errorWithParameters, exception);
      }
    }
    return Response.ok(fileDataStructureDto).build();
  }
}
