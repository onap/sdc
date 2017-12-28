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

import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.activitylog.ActivityLogManagerFactory;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.activitylog.dao.type.ActivityType;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.datatypes.model.ItemType;
import org.openecomp.sdc.healing.factory.HealingManagerFactory;
import org.openecomp.sdc.itempermissions.ItemPermissionsManager;
import org.openecomp.sdc.itempermissions.ItemPermissionsManagerFactory;
import org.openecomp.sdc.itempermissions.impl.types.PermissionTypes;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.MdcUtil;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.messages.AuditMessages;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.notification.dtos.Event;
import org.openecomp.sdc.notification.factories.NotificationPropagationManagerFactory;
import org.openecomp.sdc.notification.services.NotificationPropagationManager;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.VspManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OnboardingMethod;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.CreatePackageForNonFinalVendorSoftwareProductErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.OnboardingMethodErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.errors.PackageNotFoundErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.versioning.ItemManager;
import org.openecomp.sdc.versioning.ItemManagerFactory;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.errors.RequestedVersionInvalidErrorBuilder;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.NotificationEventTypes;
import org.openecomp.sdcrests.item.rest.mapping.MapVersionToDto;
import org.openecomp.sdcrests.item.types.ItemCreationDto;
import org.openecomp.sdcrests.item.types.VersionDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.PackageInfoDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ValidationResponseDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VersionSoftwareProductActionRequestDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspComputeDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspDescriptionDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspDetailsDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VspRequestDto;
import org.openecomp.sdcrests.vsp.rest.VendorSoftwareProducts;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComputeEntityToVspComputeDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapItemToVspDetailsDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapPackageInfoToPackageInfoDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapQuestionnaireResponseToQuestionnaireResponseDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapValidationResponseToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapVspDescriptionDtoToItem;
import org.openecomp.sdcrests.vsp.rest.mapping.MapVspDescriptionDtoToVspDetails;
import org.openecomp.sdcrests.vsp.rest.mapping.MapVspDetailsToDto;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.PERMISSION_USER;
import static org.openecomp.sdc.logging.messages.AuditMessages.SUBMIT_VSP_ERROR;
import static org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME;
import static org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants.VALIDATION_VSP_NAME;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.ITEM_ID;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.ITEM_NAME;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.SUBMIT_DESCRIPTION;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.VERSION_ID;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.VERSION_NAME;


@Named
@Service("vendorSoftwareProducts")
@Scope(value = "prototype")
public class VendorSoftwareProductsImpl implements VendorSoftwareProducts {

  private static final String SUBMIT_ITEM_ACTION = "Submit_Item";
  private static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
  private static final Logger LOGGER = LoggerFactory.getLogger(VendorSoftwareProductsImpl.class);

  private static ItemCreationDto validationVsp;

  private ItemManager itemManager = ItemManagerFactory.getInstance().createInterface();
  private ItemPermissionsManager permissionsManager =
      ItemPermissionsManagerFactory.getInstance().createInterface();
  private VersioningManager versioningManager =
      VersioningManagerFactory.getInstance().createInterface();
  private VendorSoftwareProductManager vendorSoftwareProductManager =
      VspManagerFactory.getInstance().createInterface();
  private ActivityLogManager activityLogManager =
      ActivityLogManagerFactory.getInstance().createInterface();
  private NotificationPropagationManager notifier =
      NotificationPropagationManagerFactory.getInstance().createInterface();

  @Override
  public Response createVsp(VspRequestDto vspRequestDto, String user) {
    MdcUtil.initMdc(LoggerServiceName.Create_VSP.toString());
    LOGGER.audit(AuditMessages.AUDIT_MSG + AuditMessages.CREATE_VSP + vspRequestDto.getName());

    ItemCreationDto itemCreationDto;

    OnboardingMethod onboardingMethod;
    try {
      onboardingMethod = OnboardingMethod.valueOf(vspRequestDto.getOnboardingMethod());
    } catch (IllegalArgumentException e) {
      throw getUnknownOnboardingMethod();
    }
    switch (onboardingMethod) {
      case NetworkPackage:
      case Manual:
        Item item = new MapVspDescriptionDtoToItem().applyMapping(vspRequestDto, Item.class);
        item.setType(ItemType.vsp.name());
        item.addProperty(VspItemProperty.ONBOARDING_METHOD, onboardingMethod.name());

        UniqueValueUtil.validateUniqueValue(VENDOR_SOFTWARE_PRODUCT_NAME, item.getName());
        item = itemManager.create(item);
        UniqueValueUtil.createUniqueValue(VENDOR_SOFTWARE_PRODUCT_NAME, item.getName());

        Version version = versioningManager.create(item.getId(), new Version(), null);

        VspDetails vspDetails =
            new MapVspDescriptionDtoToVspDetails().applyMapping(vspRequestDto, VspDetails.class);
        vspDetails.setId(item.getId());
        vspDetails.setVersion(version);
        vspDetails.setOnboardingMethod(vspRequestDto.getOnboardingMethod());

        vendorSoftwareProductManager.createVsp(vspDetails);
        versioningManager.publish(item.getId(), version, "Initial vsp:" + vspDetails.getName());

        itemCreationDto = new ItemCreationDto();
        itemCreationDto.setItemId(item.getId());
        itemCreationDto.setVersion(new MapVersionToDto().applyMapping(version, VersionDto.class));

        activityLogManager.logActivity(new ActivityLogEntity(vspDetails.getId(), version,
            ActivityType.Create, user, true, "", ""));
        break;
      default:
        throw getUnknownOnboardingMethod();
    }

    return Response.ok(itemCreationDto).build();
  }

  private CoreException getUnknownOnboardingMethod() {
    ErrorCode onboardingMethodUpdateErrorCode = OnboardingMethodErrorBuilder
        .getInvalidOnboardingMethodErrorBuilder();
    MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
        LoggerTragetServiceName.ADD_VSP, ErrorLevel.ERROR.name(),
        LoggerErrorCode.DATA_ERROR.getErrorCode(), onboardingMethodUpdateErrorCode.message());
    return new CoreException(onboardingMethodUpdateErrorCode);
  }

  @Override
  public Response listVsps(String versionStatus, String user) {
    MdcUtil.initMdc(LoggerServiceName.List_VSP.toString());

    Predicate<Item> itemPredicate;
    if (VersionStatus.Certified.name().equals(versionStatus)) {
      itemPredicate = item -> ItemType.vsp.name().equals(item.getType()) &&
          item.getVersionStatusCounters().containsKey(VersionStatus.Certified);

    } else if (VersionStatus.Draft.name().equals(versionStatus)) {
      itemPredicate = item -> ItemType.vsp.name().equals(item.getType()) &&
          item.getVersionStatusCounters().containsKey(VersionStatus.Draft) &&
          userHasPermission(item.getId(), user);

    } else {
      itemPredicate = item -> ItemType.vsp.name().equals(item.getType());
    }

    GenericCollectionWrapper<VspDetailsDto> results = new GenericCollectionWrapper<>();
    MapItemToVspDetailsDto mapper = new MapItemToVspDetailsDto();
    itemManager.list(itemPredicate).stream()
        .sorted((o1, o2) -> o2.getModificationTime().compareTo(o1.getModificationTime()))
        .forEach(vspItem -> results.add(mapper.applyMapping(vspItem, VspDetailsDto.class)));

    return Response.ok(results).build();
  }

  @Override
  public Response getVsp(String vspId, String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_VSP.toString());

    Version version = versioningManager.get(vspId, new Version(versionId));
    VspDetails vspDetails = vendorSoftwareProductManager.getVsp(vspId, version);
    vspDetails.setWritetimeMicroSeconds(version.getModificationTime().getTime());

    try {
      Optional<Version> healedVersion = HealingManagerFactory.getInstance().createInterface()
          .healItemVersion(vspId, version, ItemType.vsp, false);

      if (healedVersion.isPresent()) {
        vspDetails.setVersion(healedVersion.get());
        if (version.getStatus() == VersionStatus.Certified) {
          submitHealedVersion(vspId, healedVersion.get(), versionId, user);
        }
      }
    } catch (Exception e) {
      LOGGER.error(String.format("Error while auto healing VSP with Id %s and version %s: %s",
          vspId, versionId, e.getMessage()));
    }

    VspDetailsDto vspDetailsDto =
        new MapVspDetailsToDto().applyMapping(vspDetails, VspDetailsDto.class);
    addNetworkPackageInfo(vspId, version, vspDetailsDto);

    return Response.ok(vspDetailsDto).build();
  }

  private void submitHealedVersion(String vspId, Version healedVersion, String baseVersionId,
                                   String user) {
    try {
      Optional<ValidationResponse>
          validationResponse = submit(vspId, healedVersion, "Submit healed Vsp", user);
      if (validationResponse.isPresent()) {
        // TODO: 8/9/2017 before collaboration checkout was done at this scenario (equivalent
        // to new version in collaboration). need to decide what should be done now.
        throw new IllegalStateException("Certified vsp after healing failed on validation");
      }
      vendorSoftwareProductManager.createPackage(vspId, healedVersion);
    } catch (Exception ex) {
      LOGGER.error("VSP Id {}: Error while submitting version {} " +
              "created based on Certified version {} for healing purpose.",
          vspId, healedVersion.getId(), baseVersionId, ex.getMessage());
    }
  }

  @Override
  public Response updateVsp(String vspId, String versionId, VspDescriptionDto vspDescriptionDto,
                            String user) {
    MdcUtil.initMdc(LoggerServiceName.Update_VSP.toString());
    VspDetails vspDetails =
        new MapVspDescriptionDtoToVspDetails().applyMapping(vspDescriptionDto, VspDetails.class);
    vspDetails.setId(vspId);
    vspDetails.setVersion(new Version(versionId));

    vendorSoftwareProductManager.updateVsp(vspDetails);

    return Response.ok().build();
  }

  @Override
  public Response deleteVsp(String vspId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Delete_VSP.toString());
    vendorSoftwareProductManager.deleteVsp(vspId);

    return Response.ok().build();
  }

  @Override
  public Response actOnVendorSoftwareProduct(VersionSoftwareProductActionRequestDto request,
                                             String vspId, String versionId,
                                             String user) throws IOException {
    Version version = new Version(versionId);

    switch (request.getAction()) {
      case Submit:
        if (!permissionsManager.isAllowed(vspId, user, SUBMIT_ITEM_ACTION)) {
          return Response.status(Response.Status.FORBIDDEN)
              .entity(new Exception(Messages.PERMISSIONS_ERROR.getErrorMessage())).build();
        }
        String message =
            request.getSubmitRequest() == null ? "Submit" : request.getSubmitRequest().getMessage();
        Optional<ValidationResponse> validationResponse = submit(vspId, version, message, user);

        if (validationResponse.isPresent()) {
          ValidationResponseDto validationResponseDto = new MapValidationResponseToDto()
              .applyMapping(validationResponse.get(), ValidationResponseDto.class);
          return Response.status(Response.Status.EXPECTATION_FAILED).entity(validationResponseDto)
              .build();
        }

        notifyUsers(vspId, version, message, user, NotificationEventTypes.SUBMIT);
        break;
      case Create_Package:
        return createPackage(vspId, version);
      default:
    }

    return Response.ok().build();
  }

  @Override
  public Response getValidationVsp(String user) throws Exception {
    if (validationVsp != null) {
      return Response.ok(validationVsp).build();
    }

    VspRequestDto validationVspRequest = new VspRequestDto();
    validationVspRequest.setOnboardingMethod("HEAT");
    validationVspRequest.setName(VALIDATION_VSP_NAME);

    try {
      validationVsp = (ItemCreationDto) createVsp(validationVspRequest, user).getEntity();
      return Response.ok(validationVsp).build();

    } catch (CoreException validationVspAlreadyExistException) {
      // find validationVsp
      String validationVspId = itemManager.list(item ->
          ItemType.vsp.name().equals(item.getType()) && VALIDATION_VSP_NAME.equals(item.getName()))
          .stream().findFirst().orElseThrow(() -> new IllegalStateException("Vsp with name %s " +
              "does not exist even though the name exists according to unique value util")).getId();
      Version validationVspVersion = versioningManager.list(validationVspId).iterator().next();

      validationVsp = new ItemCreationDto();
      validationVsp.setItemId(validationVspId);
      validationVsp
          .setVersion(new MapVersionToDto().applyMapping(validationVspVersion, VersionDto.class));

      return Response.ok(validationVsp).build();
    }
  }

  @Override
  public Response getOrchestrationTemplate(String vspId, String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_Uploaded_File.toString());
    byte[] orchestrationTemplateFile =
        vendorSoftwareProductManager.getOrchestrationTemplateFile(vspId, new Version(versionId));

    if (orchestrationTemplateFile == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    Response.ResponseBuilder response = Response.ok(orchestrationTemplateFile);
    response.header(CONTENT_DISPOSITION_HEADER, "attachment; filename=LatestHeatPackage.zip");
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
  public Response getTranslatedFile(String vspId, String versionName, String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_Translated_File.toString());

    List<Version> versions = versioningManager.list(vspId);
    Version version;
    if (versionName == null) {
      version = versions.stream().filter(ver -> VersionStatus.Certified == ver.getStatus())
          .max((o1, o2) -> ((Double) Double.parseDouble(o1.getName()))
              .compareTo(Double.parseDouble(o2.getName()))).orElseThrow(() -> {
            MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
                LoggerTragetServiceName.GET_TRANSLATED_FILE, ErrorLevel.ERROR.name(),
                LoggerErrorCode.DATA_ERROR.getErrorCode(), "Package not found");
            return new CoreException(new PackageNotFoundErrorBuilder(vspId).build());
          });
    } else {
      version = versions.stream().filter(ver -> versionName.equals(ver.getName()))
          .findFirst().orElseThrow(() -> {
            MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
                LoggerTragetServiceName.GET_TRANSLATED_FILE, ErrorLevel.ERROR.name(),
                LoggerErrorCode.DATA_ERROR.getErrorCode(), "Package not found");
            return new CoreException(new PackageNotFoundErrorBuilder(vspId).build());
          });

      if (version.getStatus() != VersionStatus.Certified) {
        MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
            LoggerTragetServiceName.GET_VERSION_INFO, ErrorLevel.ERROR.name(),
            LoggerErrorCode.DATA_ERROR.getErrorCode(), "Invalid requested version");
        throw new CoreException(new RequestedVersionInvalidErrorBuilder().build());
      }
    }

    File zipFile = vendorSoftwareProductManager.getTranslatedFile(vspId, version);

    Response.ResponseBuilder response = Response.ok(zipFile);
    if (zipFile == null) {
      LOGGER.audit(AuditMessages.AUDIT_MSG + AuditMessages.IMPORT_FAIL + vspId);
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    response.header(CONTENT_DISPOSITION_HEADER, "attachment; filename=" + zipFile.getName());

    LOGGER.audit(AuditMessages.AUDIT_MSG + AuditMessages.IMPORT_SUCCESS + vspId);
    return response.build();
  }

  @Override
  public Response getQuestionnaire(String vspId, String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_Questionnaire_VSP.toString());
    QuestionnaireResponse questionnaireResponse =
        vendorSoftwareProductManager.getVspQuestionnaire(vspId, new Version(versionId));

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
    vendorSoftwareProductManager
        .updateVspQuestionnaire(vspId, new Version(versionId), questionnaireData);
    return Response.ok().build();
  }

  @Override
  public Response heal(String vspId, String versionId, String user) {
    HealingManagerFactory.getInstance().createInterface()
        .healItemVersion(vspId, new Version(versionId), ItemType.vsp, true);
    return Response.ok().build();
  }

  @Override
  public Response getVspInformationArtifact(String vspId, String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.Get_Information_Artifact.toString());
    File textInformationArtifact =
        vendorSoftwareProductManager.getInformationArtifact(vspId, new Version(versionId));

    Response.ResponseBuilder response = Response.ok(textInformationArtifact);
    if (textInformationArtifact == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    response.header(CONTENT_DISPOSITION_HEADER,
        "attachment; filename=" + textInformationArtifact.getName());
    return response.build();
  }

  @Override
  public Response listComputes(String vspId, String version, String user) {

    Collection<ComputeEntity> computes =
        vendorSoftwareProductManager.getComputeByVsp(vspId, new Version(version));

    MapComputeEntityToVspComputeDto mapper = new MapComputeEntityToVspComputeDto();
    GenericCollectionWrapper<VspComputeDto> results = new GenericCollectionWrapper<>();
    for (ComputeEntity compute : computes) {
      results.add(mapper.applyMapping(compute, VspComputeDto.class));
    }

    return Response.ok(results).build();
  }

  private Optional<ValidationResponse> submit(String vspId, Version version, String message,
                                              String user) throws IOException {
    MdcUtil.initMdc(LoggerServiceName.Submit_VSP.toString());
    LOGGER.audit(AuditMessages.AUDIT_MSG + AuditMessages.SUBMIT_VSP + vspId);

    ValidationResponse validationResponse = vendorSoftwareProductManager.validate(vspId, version);
    Map<String, List<ErrorMessage>> compilationErrors =
        vendorSoftwareProductManager.compile(vspId, version);
    if (!validationResponse.isValid() || MapUtils.isNotEmpty(compilationErrors)) {
      LOGGER.audit(AuditMessages.AUDIT_MSG + AuditMessages.SUBMIT_VSP_FAIL + vspId);
      if (validationResponse.getVspErrors() != null) {
        validationResponse.getVspErrors().forEach(errorCode -> LOGGER.audit(AuditMessages
            .AUDIT_MSG + String.format(SUBMIT_VSP_ERROR, errorCode.message(), vspId)));
      }
      if (validationResponse.getUploadDataErrors() != null) {
        validationResponse.getUploadDataErrors().values().forEach(errorMessages
            -> printAuditForErrors(errorMessages, vspId, SUBMIT_VSP_ERROR));
      }
      activityLogManager.logActivity(
          new ActivityLogEntity(vspId, version, ActivityType.Submit, user, false,
              "Failed on validation before submit", ""));
      return Optional.of(validationResponse);
    }

    versioningManager.submit(vspId, version, message);

    LOGGER.audit(AuditMessages.AUDIT_MSG + AuditMessages.SUBMIT_VSP + vspId);
    activityLogManager.logActivity(
        new ActivityLogEntity(vspId, version, ActivityType.Submit, user, true, "", message));
    return Optional.empty();
  }

  private void notifyUsers(String itemId, Version version, String message,
                           String userName, NotificationEventTypes eventType) {
    Map<String, Object> eventProperties = new HashMap<>();
    eventProperties.put(ITEM_NAME, itemManager.get(itemId).getName());
    eventProperties.put(ITEM_ID, itemId);

    Version ver = versioningManager.get(itemId, version);
    eventProperties.put(VERSION_NAME, ver.getName());
    eventProperties.put(VERSION_ID, ver.getId());

    eventProperties.put(SUBMIT_DESCRIPTION, message);
    eventProperties.put(PERMISSION_USER, userName);

    Event syncEvent = new SyncEvent(eventType.getEventName(), itemId, eventProperties, itemId);
    try {
      notifier.notifySubscribers(syncEvent, userName);
    } catch (Exception e) {
      LOGGER.error("Failed to send sync notification to users subscribed o item '" + itemId);
    }
  }

  private class SyncEvent implements Event {

    private String eventType;
    private String originatorId;
    private Map<String, Object> attributes;
    private String entityId;

    public SyncEvent(String eventType, String originatorId,
                     Map<String, Object> attributes, String entityId) {
      this.eventType = eventType;
      this.originatorId = originatorId;
      this.attributes = attributes;
      this.entityId = entityId;
    }

    @Override
    public String getEventType() {
      return eventType;
    }

    @Override
    public String getOriginatorId() {
      return originatorId;
    }

    @Override
    public Map<String, Object> getAttributes() {
      return attributes;
    }

    @Override
    public String getEntityId() {
      return entityId;
    }
  }

  private Response createPackage(String vspId, Version version) throws IOException {
    MdcUtil.initMdc(LoggerServiceName.Create_Package.toString());

    Version retrievedVersion = versioningManager.get(vspId, version);
    if (retrievedVersion.getStatus() != VersionStatus.Certified) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.CREATE_PACKAGE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Can't create package");
      throw new CoreException(
          new CreatePackageForNonFinalVendorSoftwareProductErrorBuilder(vspId, version)
              .build());
    }
    PackageInfo packageInfo =
        vendorSoftwareProductManager.createPackage(vspId, retrievedVersion);
    return Response.ok(packageInfo == null
        ? null
        : new MapPackageInfoToPackageInfoDto().applyMapping(packageInfo, PackageInfoDto.class))
        .build();
  }

  private void addNetworkPackageInfo(String vspId, Version version, VspDetailsDto vspDetailsDto) {
    OrchestrationTemplateEntity orchestrationTemplateInfo =
        vendorSoftwareProductManager.getOrchestrationTemplateInfo(vspId, version);

    vspDetailsDto.setValidationData(orchestrationTemplateInfo.getValidationDataStructure());
    vspDetailsDto.setNetworkPackageName(orchestrationTemplateInfo.getFileName());
    vspDetailsDto.setOnboardingOrigin(orchestrationTemplateInfo.getFileSuffix() == null
        ? OnboardingTypesEnum.NONE.toString()
        : orchestrationTemplateInfo.getFileSuffix());

    OrchestrationTemplateCandidateData candidateInfo =
        OrchestrationTemplateCandidateManagerFactory.getInstance().createInterface()
            .getInfo(vspId, version);

    //todo - remove after fix missing candidate element
    if (candidateInfo == null) {
      candidateInfo = new OrchestrationTemplateCandidateData();
      candidateInfo.setFileSuffix("zip");
    }

    vspDetailsDto
        .setCandidateOnboardingOrigin(candidateInfo.getFileSuffix()
            == null
            ? OnboardingTypesEnum.NONE.toString()
            : candidateInfo.getFileSuffix());
  }

  private boolean userHasPermission(String itemId, String userId) {
    String permission = permissionsManager.getUserItemPermiission(itemId, userId);
    return (permission != null && permission
        .matches(PermissionTypes.Contributor.name() + "|" + PermissionTypes.Owner.name()));
  }

  private void printAuditForErrors(List<ErrorMessage> errorList, String vspId, String auditType) {
    errorList.forEach(errorMessage -> {
      if (errorMessage.getLevel().equals(ErrorLevel.ERROR)) {
        LOGGER.audit(AuditMessages.AUDIT_MSG + String.format(auditType, errorMessage.getMessage(),
            vspId));
      }
    });
  }
}
