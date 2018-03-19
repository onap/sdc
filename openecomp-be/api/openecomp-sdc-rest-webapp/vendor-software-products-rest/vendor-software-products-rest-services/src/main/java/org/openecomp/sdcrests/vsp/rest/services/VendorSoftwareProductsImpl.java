/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdcrests.vsp.rest.services;

import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.dao.UniqueValueDaoFactory;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.activitylog.ActivityLogManagerFactory;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.activitylog.dao.type.ActivityType;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.datatypes.model.ItemType;
import org.openecomp.sdc.healing.factory.HealingManagerFactory;
import org.openecomp.sdc.itempermissions.ItemPermissionsManager;
import org.openecomp.sdc.itempermissions.ItemPermissionsManagerFactory;
import org.openecomp.sdc.itempermissions.impl.types.PermissionTypes;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
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
import org.openecomp.sdc.versioning.AsdcItemManager;
import org.openecomp.sdc.versioning.AsdcItemManagerFactory;
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
import org.openecomp.sdcrests.vendorsoftwareproducts.types.VendorSoftwareProductAction;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.PERMISSION_USER;
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
  private static final String ATTACHMENT_FILENAME = "attachment; filename=";
  private static final String SUBMIT_HEALED_VERSION_ERROR =
      "VSP Id %s: Error while submitting version %s created based on Certified version %s for healing purpose.";
  private static final Logger LOGGER = LoggerFactory.getLogger(VendorSoftwareProductsImpl.class);

  private ItemCreationDto validationVsp;

  private final AsdcItemManager itemManager = AsdcItemManagerFactory.getInstance()
      .createInterface();
  private final ItemPermissionsManager permissionsManager =
      ItemPermissionsManagerFactory.getInstance().createInterface();
  private final VersioningManager versioningManager =
      VersioningManagerFactory.getInstance().createInterface();
  private final VendorSoftwareProductManager vendorSoftwareProductManager =
      VspManagerFactory.getInstance().createInterface();
  private final ActivityLogManager activityLogManager =
      ActivityLogManagerFactory.getInstance().createInterface();
  private final NotificationPropagationManager notifier =
      NotificationPropagationManagerFactory.getInstance().createInterface();
  private final UniqueValueUtil uniqueValueUtil = new UniqueValueUtil(UniqueValueDaoFactory
      .getInstance().createInterface());

  @Override
  public Response createVsp(VspRequestDto vspRequestDto, String user) {

    OnboardingMethod onboardingMethod = null;
    try {
      onboardingMethod = OnboardingMethod.valueOf(vspRequestDto.getOnboardingMethod());
    } catch (IllegalArgumentException e) {
      LOGGER.error("Error while creating VSP. Message: " + e.getMessage());
      throwUnknownOnboardingMethodException(e);
    }
    ItemCreationDto itemCreationDto = null;
    if (onboardingMethod == OnboardingMethod.NetworkPackage
        || onboardingMethod == OnboardingMethod.Manual) {
      itemCreationDto = getItemCreationDto(vspRequestDto, user, onboardingMethod);

    } else {
      throwUnknownOnboardingMethodException(
          new IllegalArgumentException("Wrong parameter Onboarding Method"));
    }

    return Response.ok(itemCreationDto).build();
  }

  private ItemCreationDto getItemCreationDto(VspRequestDto vspRequestDto,
                                             String user,
                                             OnboardingMethod onboardingMethod) {

    Item item = new MapVspDescriptionDtoToItem().applyMapping(vspRequestDto, Item.class);
    item.setType(ItemType.vsp.name());
    item.setOwner(user);
    item.addProperty(VspItemProperty.ONBOARDING_METHOD, onboardingMethod.name());

    uniqueValueUtil.validateUniqueValue(VENDOR_SOFTWARE_PRODUCT_NAME, item.getName());
    item = itemManager.create(item);
    uniqueValueUtil.createUniqueValue(VENDOR_SOFTWARE_PRODUCT_NAME, item.getName());

    Version version = versioningManager.create(item.getId(), new Version(), null);

    VspDetails vspDetails =
        new MapVspDescriptionDtoToVspDetails().applyMapping(vspRequestDto, VspDetails.class);
    vspDetails.setId(item.getId());
    vspDetails.setVersion(version);
    vspDetails.setOnboardingMethod(vspRequestDto.getOnboardingMethod());

    vendorSoftwareProductManager.createVsp(vspDetails);
    versioningManager.publish(item.getId(), version, "Initial vsp:" + vspDetails.getName());
    ItemCreationDto itemCreationDto = new ItemCreationDto();
    itemCreationDto.setItemId(item.getId());
    itemCreationDto.setVersion(new MapVersionToDto().applyMapping(version, VersionDto.class));

    activityLogManager.logActivity(new ActivityLogEntity(vspDetails.getId(), version,
        ActivityType.Create, user, true, "", ""));
    return itemCreationDto;
  }

  private void throwUnknownOnboardingMethodException(IllegalArgumentException e) {
    ErrorCode onboardingMethodUpdateErrorCode = OnboardingMethodErrorBuilder
        .getInvalidOnboardingMethodErrorBuilder();
    throw new CoreException(onboardingMethodUpdateErrorCode, e);
  }

  @Override
  public Response listVsps(String versionStatus, String user) {
    Predicate<Item> itemPredicate;
    if (VersionStatus.Certified.name().equals(versionStatus)) {
      itemPredicate = item -> ItemType.vsp.name().equals(item.getType())
          && item.getVersionStatusCounters().containsKey(VersionStatus.Certified);

    } else if (VersionStatus.Draft.name().equals(versionStatus)) {
      itemPredicate = item -> ItemType.vsp.name().equals(item.getType())
          && item.getVersionStatusCounters().containsKey(VersionStatus.Draft)
          && userHasPermission(item.getId(), user);

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
    Version version = versioningManager.get(vspId, new Version(versionId));
    VspDetails vspDetails = vendorSoftwareProductManager.getVsp(vspId, version);
    vspDetails.setWritetimeMicroSeconds(version.getModificationTime().getTime());

    try {
      Optional<Version> healedVersion = HealingManagerFactory.getInstance().createInterface()
          .healItemVersion(vspId, version, ItemType.vsp, false);

      healedVersion.ifPresent(version1 -> {
        vspDetails.setVersion(version1);
        if (version.getStatus() == VersionStatus.Certified) {
          submitHealedVersion(vspId, version1, versionId, user);
        }
      });
    } catch (Exception e) {
      LOGGER.error(
          String.format("Error while auto healing VSP with Id %s and version %s", vspId, versionId),
          e);
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
      // TODO: 8/9/2017 before collaboration checkout was done at this scenario (equivalent
      // to new version in collaboration). need to decide what should be done now.
      validationResponse.ifPresent(validationResponse1 -> {
        throw new IllegalStateException("Certified vsp after healing failed on validation");
      });
      vendorSoftwareProductManager.createPackage(vspId, healedVersion);
    } catch (Exception ex) {
      LOGGER.error(
          String.format(SUBMIT_HEALED_VERSION_ERROR, vspId, healedVersion.getId(), baseVersionId),
          ex);
    }
  }

  @Override
  public Response updateVsp(String vspId, String versionId, VspDescriptionDto vspDescriptionDto,
                            String user) {
    VspDetails vspDetails =
        new MapVspDescriptionDtoToVspDetails().applyMapping(vspDescriptionDto, VspDetails.class);
    vspDetails.setId(vspId);
    vspDetails.setVersion(new Version(versionId));

    vendorSoftwareProductManager.updateVsp(vspDetails);

    return Response.ok().build();
  }

  @Override
  public Response deleteVsp(String vspId, String user) {
    Item vsp = itemManager.get(vspId);

    if (!vsp.getType().equals(ItemType.vsp.name())) {
      throw new CoreException((new ErrorCode.ErrorCodeBuilder()
          .withMessage(String.format("Vsp with id %s does not exist.",
              vspId)).build()));
    }

    Integer certifiedVersionsCounter = vsp.getVersionStatusCounters().get(VersionStatus.Certified);
    if (Objects.isNull(certifiedVersionsCounter) || certifiedVersionsCounter == 0) {
      versioningManager.list(vspId)
          .forEach(version -> vendorSoftwareProductManager.deleteVsp(vspId, version));
      itemManager.delete(vsp);
      permissionsManager.deleteItemPermissions(vspId);
      uniqueValueUtil.deleteUniqueValue(VENDOR_SOFTWARE_PRODUCT_NAME, vsp.getName());
      notifyUsers(vspId, vsp.getName(), null, "VSP was deleted", user,
          NotificationEventTypes.DELETE);

      return Response.ok().build();
    } else {
      return Response.status(Response.Status.FORBIDDEN)
          .entity(new Exception(Messages.DELETE_VSP_ERROR.getErrorMessage())).build();
    }
  }

  @Override
  public Response actOnVendorSoftwareProduct(VersionSoftwareProductActionRequestDto request,
                                             String vspId, String versionId,
                                             String user) throws IOException {
    Version version = new Version(versionId);

    if (request.getAction() == VendorSoftwareProductAction.Submit) {
      if (!permissionsManager.isAllowed(vspId, user, SUBMIT_ITEM_ACTION)) {
        return Response.status(Response.Status.FORBIDDEN)
            .entity(new Exception(Messages.PERMISSIONS_ERROR.getErrorMessage())).build();
      }
      String message = request.getSubmitRequest() == null ? "Submit"
          : request.getSubmitRequest().getMessage();
      Optional<ValidationResponse> validationResponse = submit(vspId, version, message, user);

      if (validationResponse.isPresent()) {
        ValidationResponseDto validationResponseDto = new MapValidationResponseToDto()
            .applyMapping(validationResponse.get(), ValidationResponseDto.class);
        return Response.status(Response.Status.EXPECTATION_FAILED).entity(validationResponseDto)
            .build();
      }

      notifyUsers(vspId, null, version, message, user, NotificationEventTypes.SUBMIT);

    } else if (request.getAction() == VendorSoftwareProductAction.Create_Package) {
      return createPackage(vspId, version);
    }

    return Response.ok().build();
  }

  @Override
  public staic Response getValidationVsp(String user) {
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
          .stream().findFirst().orElseThrow(() -> new IllegalStateException("Vsp with name %s "
              + "does not exist even though the name exists according to unique value util"))
          .getId();
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
    byte[] orchestrationTemplateFile =
        vendorSoftwareProductManager.getOrchestrationTemplateFile(vspId, new Version(versionId));

    if (orchestrationTemplateFile == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    Response.ResponseBuilder response = Response.ok(orchestrationTemplateFile);
    response.header(CONTENT_DISPOSITION, ATTACHMENT_FILENAME + "LatestHeatPackage.zip");
    return response.build();
  }

  @Override
  public Response listPackages(String category, String subCategory, String user) {
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
    List<Version> versions = versioningManager.list(vspId);
    Version version;
    if (versionName == null) {
      version = versions.stream().filter(ver -> VersionStatus.Certified == ver.getStatus())
          .max(Comparator.comparingDouble(o -> Double.parseDouble(o.getName())))
          .orElseThrow(() -> new CoreException(new PackageNotFoundErrorBuilder(vspId).build()));
    } else {
      version = versions.stream().filter(ver -> versionName.equals(ver.getName()))
          .findFirst()
          .orElseThrow(() -> new CoreException(new PackageNotFoundErrorBuilder(vspId).build()));

      if (version.getStatus() != VersionStatus.Certified) {
        throw new CoreException(new RequestedVersionInvalidErrorBuilder().build());
      }
    }

    File zipFile = vendorSoftwareProductManager.getTranslatedFile(vspId, version);

    Response.ResponseBuilder response = Response.ok(zipFile);
    if (zipFile == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    response.header(CONTENT_DISPOSITION, ATTACHMENT_FILENAME + zipFile.getName());

    return response.build();
  }

  @Override
  public Response getQuestionnaire(String vspId, String versionId, String user) {
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
    File textInformationArtifact =
        vendorSoftwareProductManager.getInformationArtifact(vspId, new Version(versionId));

    Response.ResponseBuilder response = Response.ok(textInformationArtifact);
    if (textInformationArtifact == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    response
        .header(CONTENT_DISPOSITION, ATTACHMENT_FILENAME + textInformationArtifact.getName());
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

    ValidationResponse validationResponse = vendorSoftwareProductManager.validate(vspId, version);
    Map<String, List<ErrorMessage>> compilationErrors =
        vendorSoftwareProductManager.compile(vspId, version);
    if (!validationResponse.isValid() || MapUtils.isNotEmpty(compilationErrors)) {

      activityLogManager.logActivity(
          new ActivityLogEntity(vspId, version, ActivityType.Submit, user, false,
              "Failed on validation before submit", ""));
      return Optional.of(validationResponse);
    }

    versioningManager.submit(vspId, version, message);

    activityLogManager.logActivity(
        new ActivityLogEntity(vspId, version, ActivityType.Submit, user, true, "", message));
    return Optional.empty();
  }

  private void notifyUsers(String itemId, String itemName, Version version, String message,
                           String userName, NotificationEventTypes eventType) {
    Map<String, Object> eventProperties = new HashMap<>();
    eventProperties
        .put(ITEM_NAME, itemName == null ? itemManager.get(itemId).getName() : itemName);
    eventProperties.put(ITEM_ID, itemId);

    if (version != null) {
      eventProperties.put(VERSION_NAME, version.getName() == null
          ? versioningManager.get(itemId, version).getName()
          : version.getName());
      eventProperties.put(VERSION_ID, version.getId());
    }

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

    private final String eventType;
    private final String originatorId;
    private final Map<String, Object> attributes;
    private final String entityId;

    SyncEvent(String eventType, String originatorId,
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
    Version retrievedVersion = versioningManager.get(vspId, version);
    if (retrievedVersion.getStatus() != VersionStatus.Certified) {
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
    OrchestrationTemplateCandidateData candidateInfo =
        OrchestrationTemplateCandidateManagerFactory.getInstance().createInterface()
            .getInfo(vspId, version);
    if (Objects.nonNull(candidateInfo) && Objects.nonNull(candidateInfo.getFileSuffix())) {
      vspDetailsDto.setValidationData(candidateInfo.getValidationDataStructure());
      vspDetailsDto.setNetworkPackageName(candidateInfo.getFileName());
      vspDetailsDto.setCandidateOnboardingOrigin(candidateInfo.getFileSuffix());
    } else {
      OrchestrationTemplateEntity orchestrationTemplateInfo =
          vendorSoftwareProductManager.getOrchestrationTemplateInfo(vspId, version);
      if (Objects.nonNull(orchestrationTemplateInfo)) {
        vspDetailsDto.setValidationData(orchestrationTemplateInfo.getValidationDataStructure());
        vspDetailsDto.setNetworkPackageName(orchestrationTemplateInfo.getFileName());
        vspDetailsDto.setOnboardingOrigin(orchestrationTemplateInfo.getFileSuffix());
      }
    }
  }

  private boolean userHasPermission(String itemId, String userId) {
    String permission = permissionsManager.getUserItemPermiission(itemId, userId);
    return permission != null && permission
        .matches(PermissionTypes.Contributor.name() + "|" + PermissionTypes.Owner.name());
  }
}
