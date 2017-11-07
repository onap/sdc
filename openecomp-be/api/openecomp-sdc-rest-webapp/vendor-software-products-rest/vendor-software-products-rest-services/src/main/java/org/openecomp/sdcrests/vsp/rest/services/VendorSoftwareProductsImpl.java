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

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.MdcUtil;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.messages.AuditMessages;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.VspManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.OnboardingMethodErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.VersionedVendorSoftwareProductInfo;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.VersionInfo;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.*;
import org.openecomp.sdcrests.vsp.rest.VendorSoftwareProducts;
import org.openecomp.sdcrests.vsp.rest.mapping.*;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.openecomp.sdcrests.wrappers.StringWrapperResponse;
import org.slf4j.MDC;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.openecomp.sdc.logging.messages.AuditMessages.SUBMIT_VSP_ERROR;


@Named
@Service("vendorSoftwareProducts")
@Scope(value = "prototype")
public class VendorSoftwareProductsImpl implements VendorSoftwareProducts {

  private final VendorSoftwareProductManager vendorSoftwareProductManager =
      VspManagerFactory.getInstance().createInterface();

  private static final Logger logger =
      LoggerFactory.getLogger(VendorSoftwareProductsImpl.class);

  @Override
  public Response createVsp(VspDescriptionDto vspDescriptionDto, String user) {
    MdcUtil.initMdc(LoggerServiceName.Create_VSP.toString());
    logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.CREATE_VSP
        + vspDescriptionDto.getName());

    OnboardingMethod onboardingMethod;

    try {
      onboardingMethod = OnboardingMethod.valueOf(vspDescriptionDto.getOnboardingMethod());
    } catch (IllegalArgumentException e) {
      return handleUnknownOnboardingMethod();
    }

    switch (onboardingMethod) {
      case NetworkPackage:
      case Manual:
        VspDetails vspDetails = new MapVspDescriptionDtoToVspDetails().
            applyMapping(vspDescriptionDto, VspDetails.class);

        vspDetails = vendorSoftwareProductManager.createVsp(vspDetails, user);

        MapVspDetailsToVspCreationDto mapping = new MapVspDetailsToVspCreationDto();
        VspCreationDto vspCreationDto = mapping.applyMapping(vspDetails, VspCreationDto.class);
        return Response.ok(vspCreationDto).build();
      default:
        return handleUnknownOnboardingMethod();
    }
  }

  private Response handleUnknownOnboardingMethod() {
    ErrorCode onboardingMethodUpdateErrorCode = OnboardingMethodErrorBuilder
        .getInvalidOnboardingMethodErrorBuilder();
    MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
        LoggerTragetServiceName.ADD_VSP, ErrorLevel.ERROR.name(),
        LoggerErrorCode.DATA_ERROR.getErrorCode(), onboardingMethodUpdateErrorCode.message());
    throw new CoreException(onboardingMethodUpdateErrorCode);
  }

  @Override
  public Response listVsps(String versionFilter, String user) {
    MdcUtil.initMdc(LoggerServiceName.List_VSP.toString());
    List<VersionedVendorSoftwareProductInfo> vspList =
        vendorSoftwareProductManager.listVsps(versionFilter, user);

    GenericCollectionWrapper<VspDetailsDto> results = new GenericCollectionWrapper<>();
    if (!vspList.isEmpty()) {
      MapVersionedVendorSoftwareProductInfoToVspDetailsDto mapper =
          new MapVersionedVendorSoftwareProductInfoToVspDetailsDto();
      for (VersionedVendorSoftwareProductInfo versionedVsp : vspList) {
        results.add(mapper.applyMapping(versionedVsp, VspDetailsDto.class));
      }
    }

    return Response.ok(results).build();
  }

  @Override
  public Response getVsp(String vspId, String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_VSP.toString());


    VspDetails vspDetails =
        vendorSoftwareProductManager
            .getVsp(vspId, resolveVspVersion(vspId, versionId, user, VersionableEntityAction.Read),
                user);

    VersionInfo versionInfo = getVersionInfo(vspId, VersionableEntityAction.Read, user);


    if (vspDetails.getOldVersion() != null && !"".equals(vspDetails.getOldVersion())) {
      if (Version.valueOf(versionId).equals(versionInfo.getActiveVersion())) {
        try {
          Version healedVersion = vendorSoftwareProductManager.callAutoHeal(vspId, versionInfo,
              vspDetails, user);
          vspDetails =
              vendorSoftwareProductManager
                  .getVsp(vspId, resolveVspVersion(vspId, healedVersion.toString(), user,
                      VersionableEntityAction.Read), user);
          versionInfo = getVersionInfo(vspId, VersionableEntityAction.Read, user);
        } catch (Exception e) {
          logger.error(e.getMessage(), e);
        }
      }
    }

    VspDetailsDto vspDetailsDto = vspDetails == null
        ? null
        : new MapVersionedVendorSoftwareProductInfoToVspDetailsDto()
            .applyMapping(new VersionedVendorSoftwareProductInfo(vspDetails, versionInfo),
                VspDetailsDto.class);

    return Response.ok(vspDetailsDto).build();
  }

  @Override
  public Response updateVsp(String vspId, String versionId, VspDescriptionDto vspDescriptionDto,
                            String user) {
    MdcUtil.initMdc(LoggerServiceName.Update_VSP.toString());
    VspDetails vspDetails =
        new MapVspDescriptionDtoToVspDetails().applyMapping(vspDescriptionDto, VspDetails.class);
    vspDetails.setId(vspId);
    vspDetails.setVersion(resolveVspVersion(vspId, null, user, VersionableEntityAction.Write));

    vendorSoftwareProductManager.updateVsp(vspDetails, user);

    return Response.ok().build();
  }

  @Override
  public Response deleteVsp(String vspId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Delete_VSP.toString());
    vendorSoftwareProductManager.deleteVsp(vspId, user);

    return Response.ok().build();
  }

  @Override
  public Response actOnVendorSoftwareProduct(String vspId, String versionId,
                                             VersionSoftwareProductActionRequestDto request,
                                             String user) throws IOException {

    switch (request.getAction()) {
      case Checkout:
        MDC.put(LoggerConstants.SERVICE_NAME, LoggerServiceName.Checkout_VSP.toString());
        vendorSoftwareProductManager.checkout(vspId, user);
        logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.CHECK_OUT_VSP + vspId);
        break;
      case Undo_Checkout:
        MDC.put(LoggerConstants.SERVICE_NAME, LoggerServiceName.Undo_Checkout_VSP.toString());
        vendorSoftwareProductManager.undoCheckout(vspId, user);
        break;
      case Checkin:
        MDC.put(LoggerConstants.SERVICE_NAME, LoggerServiceName.Checkin_VSP.toString());
        vendorSoftwareProductManager.checkin(vspId, user);
        logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.CHECK_IN_VSP + vspId);
        break;
      case Submit:
        MDC.put(LoggerConstants.SERVICE_NAME, LoggerServiceName.Submit_VSP.toString());
        ValidationResponse validationResponse = vendorSoftwareProductManager.submit(vspId, user);
        if (!validationResponse.isValid()) {
          logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.SUBMIT_VSP_FAIL + vspId);
          if (validationResponse.getVspErrors() != null) {
            validationResponse.getVspErrors().forEach(errorCode -> logger.audit(AuditMessages
                .AUDIT_MSG + String.format(SUBMIT_VSP_ERROR, errorCode.message(), vspId)));
          }
          if (validationResponse.getUploadDataErrors() != null) {
            validationResponse.getUploadDataErrors().values().forEach(errorMessages
                -> printAuditForErrors(errorMessages, vspId, SUBMIT_VSP_ERROR));
          }

          return Response.status(Response.Status.EXPECTATION_FAILED).entity(
              new MapValidationResponseToDto()
                  .applyMapping(validationResponse, ValidationResponseDto.class)).build();
        }
        logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.SUBMIT_VSP + vspId);
        break;
      case Create_Package:
        MDC.put(LoggerConstants.SERVICE_NAME, LoggerServiceName.Create_Package.toString());

        PackageInfo packageInfo = vendorSoftwareProductManager.createPackage(vspId,
            resolveVspVersion(vspId, null, user, VersionableEntityAction.Read), user);
        return Response.ok(packageInfo == null
            ? null
            : new MapPackageInfoToPackageInfoDto().applyMapping(packageInfo, PackageInfoDto.class))
            .build();
      default:
    }

    return Response.ok().build();
  }

  @Override
  public Response getValidationVsp(String user)
      throws Exception {
    String validationVspId = vendorSoftwareProductManager.fetchValidationVsp(user);
    StringWrapperResponse response = new StringWrapperResponse(validationVspId);
    return Response.ok(response).build();
  }


  @Override
  public Response getOrchestrationTemplate(String vspId, String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_Uploaded_File.toString());
    byte[] orchestrationTemplateFile =
        vendorSoftwareProductManager
            .getOrchestrationTemplateFile(vspId,
                resolveVspVersion(vspId, versionId, user, VersionableEntityAction.Read), user);

    if (orchestrationTemplateFile == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    Response.ResponseBuilder response = Response.ok(orchestrationTemplateFile);
    response.header("Content-Disposition", "attachment; filename=LatestHeatPackage.zip");
    return response.build();
  }

  @Override
  public Response listPackages(String category, String subCategory, String user) {
    MdcUtil.initMdc(LoggerServiceName.List_Packages.toString());
    List<PackageInfo> packageInfoList =
        vendorSoftwareProductManager.listPackages(category, subCategory);

    GenericCollectionWrapper<PackageInfoDto> results = new GenericCollectionWrapper<>();
    MapPackageInfoToPackageInfoDto mapper = new MapPackageInfoToPackageInfoDto();

    if (packageInfoList != null) {
      for (PackageInfo packageInfo : packageInfoList) {
        results.add(mapper.applyMapping(packageInfo, PackageInfoDto.class));
      }
    }
    return Response.ok(results).build();
  }

  @Override
  public Response getTranslatedFile(String vspId, String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_Translated_File.toString());

    Version version = Version.valueOf(versionId);
    Version resolvedVersion = version == null
        ? getVersionInfo(vspId, VersionableEntityAction.Read, user).getLatestFinalVersion()
        : version;

    File zipFile = vendorSoftwareProductManager.getTranslatedFile(vspId, resolvedVersion, user);

    Response.ResponseBuilder response = Response.ok(zipFile);
    if (zipFile == null) {
      logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.IMPORT_FAIL + vspId);
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    response.header("Content-Disposition", "attachment; filename=" + zipFile.getName());

    logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.IMPORT_SUCCESS + vspId);
    return response.build();
  }

  @Override
  public Response getQuestionnaire(String vspId, String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_Questionnaire_VSP.toString());
    QuestionnaireResponse questionnaireResponse =
        vendorSoftwareProductManager.getVspQuestionnaire(vspId,
            resolveVspVersion(vspId, versionId, user, VersionableEntityAction.Read), user);

    if (questionnaireResponse.getErrorMessage() != null) {
      return Response.status(Response.Status.EXPECTATION_FAILED).entity(
          new MapQuestionnaireResponseToQuestionnaireResponseDto()
              .applyMapping(questionnaireResponse, QuestionnaireResponseDto.class)).build();
    }

    QuestionnaireResponseDto result = new MapQuestionnaireResponseToQuestionnaireResponseDto()
        .applyMapping(questionnaireResponse, QuestionnaireResponseDto.class);
    return Response.ok(result).build();
  }

  @Override
  public Response updateQuestionnaire(String questionnaireData, String vspId, String
      versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Update_Questionnaire_VSP.toString());
    vendorSoftwareProductManager.updateVspQuestionnaire(vspId,
        resolveVspVersion(vspId, null, user, VersionableEntityAction.Write),
        questionnaireData, user);
    return Response.ok().build();
  }

  @Override
  public Response heal(String vspId, String versionId, String user) {
    vendorSoftwareProductManager.heal(vspId, Version.valueOf(versionId), user);

    return Response.ok().build();
  }

  @Override
  public Response getVspInformationArtifact(String vspId, String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_Information_Artifact.toString());
    File textInformationArtifact =
        vendorSoftwareProductManager.getInformationArtifact(vspId,
            resolveVspVersion(vspId, versionId, user, VersionableEntityAction.Read), user);

    Response.ResponseBuilder response = Response.ok(textInformationArtifact);
    if (textInformationArtifact == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    response
        .header("Content-Disposition", "attachment; filename=" + textInformationArtifact.getName());
    return response.build();
  }

  public Response listCompute(String vspId, String version, String user) {

    Collection<ComputeEntity> computes = vendorSoftwareProductManager.getComputeByVsp(vspId,
        resolveVspVersion(vspId, version, user, VersionableEntityAction.Read), user);

    MapComputeEntityToVspComputeDto mapper = new MapComputeEntityToVspComputeDto();
    GenericCollectionWrapper<VspComputeDto> results = new GenericCollectionWrapper<>();
    for (ComputeEntity compute : computes) {
      results.add(mapper.applyMapping(compute, VspComputeDto.class));
    }

    return Response.ok(results).build();
  }

  @Override
  public Response reSubmitAll(String user) throws IOException {

    MDC.put(LoggerConstants.SERVICE_NAME, LoggerServiceName.Re_Submit_ALL_Final_VSPs.toString());
    logger.audit(AuditMessages.AUDIT_MSG + AuditMessages.RESUBMIT_ALL_FINAL_VSPS);

    List<VersionedVendorSoftwareProductInfo> latestFinalVsps = Objects
        .requireNonNull(vendorSoftwareProductManager.listVsps(VersionStatus.Final.name(), user));

    List<VersionedVendorSoftwareProductInfo> nonLockedLatestFinalVsps = latestFinalVsps.stream()
        .filter(vsp ->
            !isVspLocked(vsp.getVspDetails().getId(), vsp.getVspDetails().getName(), user))
        .collect(Collectors.toList());

    logger.info("Removed {} VSPs out of {} from processing due to status LOCKED.\n" +
            "Total number of VSPs: {}. Performing healing and resubmit for all non-Manual VSPs " +
            "in submitted status.\n No need to pre-set oldVersion field",
        latestFinalVsps.size() - nonLockedLatestFinalVsps.size(), latestFinalVsps.size(),
        nonLockedLatestFinalVsps.size());

    int healingCounter = 0;
    int failedCounter = 0;
    for (int counter = 0; counter < nonLockedLatestFinalVsps.size(); counter++) {
      VersionedVendorSoftwareProductInfo versionVspInfo = nonLockedLatestFinalVsps.get(counter);
      try {
        final VspDetails vspDetails = versionVspInfo.getVspDetails();
        if (!OnboardingMethod.Manual.name().equals(vspDetails.getOnboardingMethod())) {
          logger.info("Starting on healing and resubmit for VSP [{}], #{} out of total {}",
              vspDetails.getName(), counter + 1, nonLockedLatestFinalVsps.size());
          reSubmit(vspDetails, user);
          healingCounter++;
        }
      } catch (Exception e) {
        failedCounter++;
      }
    }

    logger.info("Total VSPs processed {}. Completed running healing and resubmit for {} VSPs out" +
            " of total # of {} submitted VSPs.  Failures count during resubmitAll: {}",
        nonLockedLatestFinalVsps.size(), healingCounter, latestFinalVsps.size(), failedCounter);

    return Response.ok().build();
  }

  private boolean isVspLocked(String vspId, String vspName, String user) {
    final VersionInfo versionInfo = getVersionInfo(vspId, VersionableEntityAction.Read, user);

    if (versionInfo.getStatus().equals(VersionStatus.Locked)) {
      logger.info("VSP name [{}]/id [{}] status is LOCKED", vspName, vspId);
      return true;
    }
    logger.info("VSP Name {}, VSP id [{}], Active Version {} , Status {}, Latest Final Version {}",
        vspName, vspId, versionInfo.getActiveVersion().toString(), versionInfo.getStatus(),
        versionInfo.getLatestFinalVersion().toString());
    return false;
  }


  private void reSubmit(VspDetails vspDetails, String user) throws Exception {
    final Version versionBefore = vspDetails.getVersion();
    vspDetails.setOldVersion("true");

    Version finalVersion;
    try {
      finalVersion =
          vendorSoftwareProductManager
              .healAndAdvanceFinalVersion(vspDetails.getId(), vspDetails, user);
    } catch (Exception e) {
      logger.error("Failed during resubmit, VSP [{}] , version before:{}, version after:{}, " +
              "status after:{}, with exception:{}",
          vspDetails.getName(), versionBefore.toString(), vspDetails.getVersion().toString(),
          vspDetails
              .getVersion().getStatus().name(), e.getMessage());
      throw e;
    }

    logger.info("Completed healing and resubmit for VSP [{}], version before:{}, version after:" +
        " {}", vspDetails.getName(), versionBefore.toString(), finalVersion);
  }

  private static void printAuditForErrors(List<ErrorMessage> errorList, String vspId,
                                          String auditType) {
    errorList.forEach(errorMessage -> {
      if (errorMessage.getLevel().equals(ErrorLevel.ERROR)) {
        logger.audit(AuditMessages.AUDIT_MSG + String.format(auditType, errorMessage.getMessage(),
            vspId));
      }
    });
  }
}
