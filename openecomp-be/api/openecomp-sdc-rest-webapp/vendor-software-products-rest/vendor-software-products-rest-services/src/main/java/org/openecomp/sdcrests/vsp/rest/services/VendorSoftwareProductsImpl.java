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

import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static org.openecomp.sdc.itempermissions.notifications.NotificationConstants.PERMISSION_USER;
import static org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants.UniqueValues.VENDOR_SOFTWARE_PRODUCT_NAME;
import static org.openecomp.sdc.vendorsoftwareproduct.dao.type.OnboardingMethod.NetworkPackage;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.ITEM_ID;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.ITEM_NAME;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.SUBMIT_DESCRIPTION;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.VERSION_ID;
import static org.openecomp.sdc.versioning.VersioningNotificationConstansts.VERSION_NAME;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.dao.UniqueValueDaoFactory;
import org.openecomp.core.util.UniqueValueUtil;
import org.openecomp.sdc.activitylog.ActivityLogManager;
import org.openecomp.sdc.activitylog.ActivityLogManagerFactory;
import org.openecomp.sdc.activitylog.dao.type.ActivityLogEntity;
import org.openecomp.sdc.activitylog.dao.type.ActivityType;
import org.openecomp.sdc.be.csar.storage.ArtifactStorageManager;
import org.openecomp.sdc.be.csar.storage.StorageFactory;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.datatypes.model.ItemType;
import org.openecomp.sdc.healing.factory.HealingManagerFactory;
import org.openecomp.sdc.itempermissions.PermissionsManager;
import org.openecomp.sdc.itempermissions.PermissionsManagerFactory;
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
import org.openecomp.sdc.versioning.types.ItemStatus;
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
import org.openecomp.sdcrests.vsp.rest.CatalogVspClient;
import org.openecomp.sdcrests.vsp.rest.VendorSoftwareProducts;
import org.openecomp.sdcrests.vsp.rest.exception.VendorSoftwareProductsExceptionSupplier;
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

@Named
@Service("vendorSoftwareProducts")
@Scope(value = "prototype")
public class VendorSoftwareProductsImpl implements VendorSoftwareProducts {

    private static final String VALIDATION_VSP_NAME = "validationOnlyVspName";
    private static final String VALIDATION_VSP_USER = "validationOnlyVspUser";
    private static final String SUBMIT_ITEM_ACTION = "Submit_Item";
    private static final String ATTACHMENT_FILENAME = "attachment; filename=";
    private static final String SUBMIT_HEALED_VERSION_ERROR = "VSP Id %s: Error while submitting version %s created based on Certified version %s for healing purpose.";
    private static final Logger LOGGER = LoggerFactory.getLogger(VendorSoftwareProductsImpl.class);
    private static final Object VALIDATION_VSP_CACHE_LOCK = new Object();
    private static ItemCreationDto cachedValidationVsp;
    private final AsdcItemManager itemManager;
    private final PermissionsManager permissionsManager;
    private final VersioningManager versioningManager;
    private final VendorSoftwareProductManager vendorSoftwareProductManager;
    private final ActivityLogManager activityLogManager;
    private final NotificationPropagationManager notifier;
    private final UniqueValueUtil uniqueValueUtil;
    private final StorageFactory storageFactory;
    private final CatalogVspClient catalogVspClient;

    public VendorSoftwareProductsImpl() {
        this.itemManager = AsdcItemManagerFactory.getInstance().createInterface();
        this.permissionsManager = PermissionsManagerFactory.getInstance().createInterface();
        this.versioningManager = VersioningManagerFactory.getInstance().createInterface();
        this.vendorSoftwareProductManager = VspManagerFactory.getInstance().createInterface();
        this.activityLogManager = ActivityLogManagerFactory.getInstance().createInterface();
        this.notifier = NotificationPropagationManagerFactory.getInstance().createInterface();
        this.uniqueValueUtil = new UniqueValueUtil(UniqueValueDaoFactory.getInstance().createInterface());
        this.storageFactory = new StorageFactory();
        this.catalogVspClient = new CatalogVspClientImpl();
    }

    public VendorSoftwareProductsImpl(AsdcItemManager itemManager,
                                      PermissionsManager permissionsManager,
                                      VersioningManager versioningManager,
                                      VendorSoftwareProductManager vendorSoftwareProductManager,
                                      ActivityLogManager activityLogManager,
                                      NotificationPropagationManager notifier,
                                      UniqueValueUtil uniqueValueUtil,
                                      final StorageFactory storageFactory,
                                      CatalogVspClient catalogVspClient) {
        this.itemManager = itemManager;
        this.permissionsManager = permissionsManager;
        this.versioningManager = versioningManager;
        this.vendorSoftwareProductManager = vendorSoftwareProductManager;
        this.activityLogManager = activityLogManager;
        this.notifier = notifier;
        this.uniqueValueUtil = uniqueValueUtil;
        this.storageFactory = storageFactory;
        this.catalogVspClient = catalogVspClient;
    }

    @Override
    public Response createVsp(VspRequestDto vspRequestDto, String user) {
        ItemCreationDto vspCreationDto = createVspItem(vspRequestDto, user);
        return Response.ok(vspCreationDto).build();
    }

    private ItemCreationDto createVspItem(VspRequestDto vspRequestDto, String user) {
        OnboardingMethod onboardingMethod = null;
        try {
            onboardingMethod = OnboardingMethod.valueOf(vspRequestDto.getOnboardingMethod());
        } catch (IllegalArgumentException e) {
            LOGGER.error("Error while creating VSP. Message: " + e.getMessage());
            throwUnknownOnboardingMethodException(e);
        }
        ItemCreationDto itemCreationDto = null;
        if (onboardingMethod == NetworkPackage || onboardingMethod == OnboardingMethod.Manual) {
            itemCreationDto = createItem(vspRequestDto, user, onboardingMethod);
        } else {
            throwUnknownOnboardingMethodException(new IllegalArgumentException("Wrong parameter Onboarding Method"));
        }
        return itemCreationDto;
    }

    private ItemCreationDto createItem(VspRequestDto vspRequestDto, String user, OnboardingMethod onboardingMethod) {
        Item item = new MapVspDescriptionDtoToItem().applyMapping(vspRequestDto, Item.class);
        item.setType(ItemType.vsp.name());
        item.setOwner(user);
        item.setStatus(ItemStatus.ACTIVE);
        item.addProperty(VspItemProperty.ONBOARDING_METHOD, onboardingMethod.name());
        uniqueValueUtil.validateUniqueValue(VENDOR_SOFTWARE_PRODUCT_NAME, item.getName());
        item = itemManager.create(item);
        uniqueValueUtil.createUniqueValue(VENDOR_SOFTWARE_PRODUCT_NAME, item.getName());
        Version version = versioningManager.create(item.getId(), new Version(), null);
        VspDetails vspDetails = new MapVspDescriptionDtoToVspDetails().applyMapping(vspRequestDto, VspDetails.class);
        vspDetails.setId(item.getId());
        vspDetails.setVersion(version);
        vspDetails.setOnboardingMethod(vspRequestDto.getOnboardingMethod());
        vendorSoftwareProductManager.createVsp(vspDetails);
        versioningManager.publish(item.getId(), version, "Initial vsp:" + vspDetails.getName());
        ItemCreationDto itemCreationDto = new ItemCreationDto();
        itemCreationDto.setItemId(item.getId());
        itemCreationDto.setVersion(new MapVersionToDto().applyMapping(version, VersionDto.class));
        activityLogManager.logActivity(new ActivityLogEntity(vspDetails.getId(), version, ActivityType.Create, user, true, "", ""));
        return itemCreationDto;
    }

    private void throwUnknownOnboardingMethodException(IllegalArgumentException e) {
        ErrorCode onboardingMethodUpdateErrorCode = OnboardingMethodErrorBuilder.getInvalidOnboardingMethodErrorBuilder();
        throw new CoreException(onboardingMethodUpdateErrorCode, e);
    }

    @Override
    public Response listVsps(String versionStatus, String itemStatus, String user) {
        GenericCollectionWrapper<VspDetailsDto> results = new GenericCollectionWrapper<>();
        MapItemToVspDetailsDto mapper = new MapItemToVspDetailsDto();
        getVspList(versionStatus, itemStatus, user).forEach(vspItem -> results.add(mapper.applyMapping(vspItem, VspDetailsDto.class)));
        return Response.ok(results).build();
    }

    @Override
    public Response getVsp(String vspId, String versionId, String user) {
        Version version = versioningManager.get(vspId, new Version(versionId));
        VspDetails vspDetails = vendorSoftwareProductManager.getVsp(vspId, version);
        try {
            HealingManagerFactory.getInstance().createInterface().healItemVersion(vspId, version, ItemType.vsp, false).ifPresent(healedVersion -> {
                vspDetails.setVersion(healedVersion);
                if (version.getStatus() == VersionStatus.Certified) {
                    submitHealedVersion(vspDetails, versionId, user);
                }
            });
        } catch (Exception e) {
            LOGGER.error(String.format("Error while auto healing VSP with Id %s and version %s", vspId, versionId), e);
        }
        VspDetailsDto vspDetailsDto = new MapVspDetailsToDto().applyMapping(vspDetails, VspDetailsDto.class);
        addNetworkPackageInfo(vspId, vspDetails.getVersion(), vspDetailsDto);
        return Response.ok(vspDetailsDto).build();
    }

    @Override
    public Response getLatestVsp(final String vspId, final String user) {
        final List<Version> versions = versioningManager.list(vspId);
        final Version version = versions.stream().filter(ver -> VersionStatus.Certified == ver.getStatus())
            .max(Comparator.comparingDouble(o -> Double.parseDouble(o.getName())))
            .orElseThrow(() -> new CoreException(new PackageNotFoundErrorBuilder(vspId).build()));
        return getVsp(vspId, version.getId(), user);
    }

    private void submitHealedVersion(VspDetails vspDetails, String baseVersionId, String user) {
        try {
            if (vspDetails.getVlmVersion() != null) {
                // sync vlm if not exists on user space
                versioningManager.get(vspDetails.getVendorId(), vspDetails.getVlmVersion());
            }
            submit(vspDetails.getId(), vspDetails.getVersion(), "Submit healed Vsp", user).ifPresent(validationResponse -> {
                throw new IllegalStateException("Certified vsp after healing failed on validation");
            });
            vendorSoftwareProductManager.createPackage(vspDetails.getId(), vspDetails.getVersion());
        } catch (Exception ex) {
            LOGGER.error(String.format(SUBMIT_HEALED_VERSION_ERROR, vspDetails.getId(), vspDetails.getVersion().getId(), baseVersionId), ex);
        }
    }

    @Override
    public Response updateVsp(String vspId, String versionId, VspDescriptionDto vspDescriptionDto, String user) {
        VspDetails vspDetails = new MapVspDescriptionDtoToVspDetails().applyMapping(vspDescriptionDto, VspDetails.class);
        vspDetails.setId(vspId);
        vspDetails.setVersion(new Version(versionId));
        vendorSoftwareProductManager.updateVsp(vspDetails);
        updateVspItem(vspId, vspDescriptionDto);
        return Response.ok().build();
    }

    @Override
    public Response deleteVsp(final String vspId, final String user) {
        final Item vsp = itemManager.get(vspId);
        if (!ItemType.vsp.getName().equals(vsp.getType())) {
            throw VendorSoftwareProductsExceptionSupplier.vspNotFound(vspId).get();
        }

        checkIfCanDeleteVsp(vsp, user);

        try {
            deleteVspFromStorage(vspId, user);
        } catch (final Exception e) {
            logDeleteFromStorageFailure(vspId, user);
            throw VendorSoftwareProductsExceptionSupplier.deleteVspFromStorageFailure(vspId).get();
        }

        try {
            deleteVsp(vspId, user, vsp);
        } catch (final Exception e) {
            throw VendorSoftwareProductsExceptionSupplier.deleteVspFromDatabaseFailure(vspId).get();
        }

        return Response.ok().build();
    }

    private void deleteUserPermissions(String vspId) {
        permissionsManager.listItemPermissions(vspId).forEach(itemPermissionsEntity -> {
            Set<String> usersToDelete = new HashSet<>();
            usersToDelete.add(itemPermissionsEntity.getUserId());
            permissionsManager.updateItemPermissions(vspId, itemPermissionsEntity.getPermission(), new HashSet<>(), usersToDelete);
        });
    }

    private void checkIfCanDeleteVsp(final Item vsp, final String user) {
        final String vspId = vsp.getId();

        checkIfVspInUse(user, vspId);

        if (isVspItemNeverCertified(vsp)) {
            return;
        }
        if (!isVspItemArchived(vspId, user)) {
            throw VendorSoftwareProductsExceptionSupplier.deleteNotArchivedVsp(vspId).get();
        }
    }

    private void checkIfVspInUse(final String user, final String vspId) {
        final Optional<String> vfNameThatUsesVspOpt;
        try {
            vfNameThatUsesVspOpt = catalogVspClient.findNameOfVfUsingVsp(vspId, user);
        } catch (final Exception e) {
            throw VendorSoftwareProductsExceptionSupplier.deleteGenericError(vspId).get();
        }
        if (vfNameThatUsesVspOpt.isPresent()) {
            final String vfName = vfNameThatUsesVspOpt.get();
            throw VendorSoftwareProductsExceptionSupplier.vspInUseByVf(vfName).get();
        }
    }

    private boolean isVspItemArchived(final String vspId, final String user) {
        return getVspList(null, ItemStatus.ARCHIVED.name(), user).stream().anyMatch(item -> vspId.equals(item.getId()));
    }

    private boolean isVspItemNeverCertified(final Item vsp) {
        final Integer certifiedVersionsCounter = vsp.getVersionStatusCounters().get(VersionStatus.Certified);
        return certifiedVersionsCounter == null || certifiedVersionsCounter == 0;
    }

    private void deleteVspFromStorage(final String vspId, final String user) {
        final ArtifactStorageManager artifactStorageManager = storageFactory.createArtifactStorageManager();
        if (artifactStorageManager.isEnabled()) {
            artifactStorageManager.delete(vspId);
            logDeleteFromStorageAllSuccess(vspId, user);
        }
    }

    private void logDeleteFromStorageFailure(final String vspId, final String user) {
        final String message = Messages.DELETE_VSP_FROM_STORAGE_ERROR.formatMessage(vspId);
        try {
            versioningManager.list(vspId).forEach(version -> activityLogManager.logActivity(
                new ActivityLogEntity(vspId, version, ActivityType.Delete_From_Storage, user, false, message, message)
            ));
        } catch (final Exception e) {
            LOGGER.error("Could not log activity '{}'", message, e);
        }
    }
    private void logDeleteFromStorageAllSuccess(final String vspId, final String user) {
        final String message = String.format("VSP '%s' fully deleted from the storage", vspId);
        try {
            versioningManager.list(vspId).forEach(version -> activityLogManager.logActivity(
                new ActivityLogEntity(vspId, version, ActivityType.Delete_From_Storage, user, true, message, message)
            ));
        } catch (final Exception e) {
            LOGGER.error("Could not log activity '{}'", message, e);
        }
    }

    private void deleteVsp(final String vspId, final String user, final Item vsp) {
        updatePackageDetails(vspId);
        deleteUserPermissions(vspId);
        versioningManager.list(vspId).forEach(version -> vendorSoftwareProductManager.deleteVsp(vspId, version));
        itemManager.delete(vsp);
        permissionsManager.deleteItemPermissions(vspId);
        uniqueValueUtil.deleteUniqueValue(VENDOR_SOFTWARE_PRODUCT_NAME, vsp.getName());
        notifyUsers(vspId, vsp.getName(), null, null, user, NotificationEventTypes.DELETE);
    }

    private void updatePackageDetails(final String vspId) {
        final List<VspDetails> listVsp = new ArrayList<>();
        versioningManager.list(vspId).forEach(version -> listVsp.add(vendorSoftwareProductManager.getVsp(vspId, version)));
        listVsp.forEach(vspDetail ->
                vendorSoftwareProductManager.listPackages(vspDetail.getCategory(), vspDetail.getSubCategory())
                        .stream().filter(packageInfo -> packageInfo.getVspId().equals(vspId)).collect(Collectors.toList())
                        .forEach(packInfo -> {
                            packInfo.setTranslatedFile(ByteBuffer.wrap(new byte[0]));
                            vendorSoftwareProductManager.updatePackage(packInfo);
                        })
        );
    }

    @Override
    public Response actOnVendorSoftwareProduct(VersionSoftwareProductActionRequestDto request, String vspId, String versionId, String user)
        throws IOException {
        Version version = new Version(versionId);
        if (request.getAction() == VendorSoftwareProductAction.Submit) {
            if (!permissionsManager.isAllowed(vspId, user, SUBMIT_ITEM_ACTION)) {
                return Response.status(Response.Status.FORBIDDEN).entity(new Exception(Messages.PERMISSIONS_ERROR.getErrorMessage())).build();
            }
            String message = request.getSubmitRequest() == null ? "Submit" : request.getSubmitRequest().getMessage();
            Optional<ValidationResponse> validationResponse = submit(vspId, version, message, user);
            if (validationResponse.isPresent()) {
                ValidationResponseDto validationResponseDto = new MapValidationResponseToDto()
                    .applyMapping(validationResponse.get(), ValidationResponseDto.class);
                return Response.status(Response.Status.EXPECTATION_FAILED).entity(validationResponseDto).build();
            }
            notifyUsers(vspId, null, version, message, user, NotificationEventTypes.SUBMIT);
        } else if (request.getAction() == VendorSoftwareProductAction.Create_Package) {
            return createPackage(vspId, version);
        }
        return Response.ok().build();
    }

    @Override
    public Response getValidationVsp(String user) {
        ItemCreationDto validationVsp = retrieveValidationVsp();
        return Response.ok(validationVsp).build();
    }

    private ItemCreationDto retrieveValidationVsp() {
        synchronized (VALIDATION_VSP_CACHE_LOCK) {
            if (cachedValidationVsp != null) {
                return cachedValidationVsp;
            }
            VspRequestDto validationVspRequest = new VspRequestDto();
            validationVspRequest.setOnboardingMethod(NetworkPackage.toString());
            validationVspRequest.setName(VALIDATION_VSP_NAME);
            try {
                cachedValidationVsp = createVspItem(validationVspRequest, VALIDATION_VSP_USER);
                return cachedValidationVsp;
            } catch (CoreException vspCreateException) {
                LOGGER.debug("Failed to create validation VSP", vspCreateException);
                Predicate<Item> validationVspFilter = item -> ItemType.vsp.name().equals(item.getType()) && VALIDATION_VSP_NAME
                    .equals(item.getName());
                String validationVspId = itemManager.list(validationVspFilter).stream().findFirst().orElseThrow(() -> new IllegalStateException(
                        "Vsp with name " + VALIDATION_VSP_NAME + " does not exist even though the name exists according to " + "unique value util"))
                    .getId();
                Version validationVspVersion = versioningManager.list(validationVspId).iterator().next();
                cachedValidationVsp = new ItemCreationDto();
                cachedValidationVsp.setItemId(validationVspId);
                cachedValidationVsp.setVersion(new MapVersionToDto().applyMapping(validationVspVersion, VersionDto.class));
                return cachedValidationVsp;
            }
        }
    }

    @Override
    public Response getOrchestrationTemplate(String vspId, String versionId, String user) {
        byte[] orchestrationTemplateFile = vendorSoftwareProductManager.getOrchestrationTemplateFile(vspId, new Version(versionId));
        if (orchestrationTemplateFile == null || orchestrationTemplateFile.length == 0) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Response.ResponseBuilder response = Response.ok(orchestrationTemplateFile);
        response.header(CONTENT_DISPOSITION, ATTACHMENT_FILENAME + "LatestHeatPackage.zip");
        return response.build();
    }

    @Override
    public Response listPackages(String status, String category, String subCategory, String user) {
        List<String> vspsIds = getVspList(null, status != null ? ItemStatus.valueOf(status).name() : null, user).stream().map(Item::getId)
            .collect(Collectors.toList());
        List<PackageInfo> packageInfoList = vendorSoftwareProductManager.listPackages(category, subCategory);
        packageInfoList = packageInfoList.stream().filter(packageInfo -> vspsIds.contains(packageInfo.getVspId())).collect(Collectors.toList());
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
        final List<Version> versions = versioningManager.list(vspId);
        final Version version;
        if (versionId == null) {
            version = versions.stream().filter(ver -> VersionStatus.Certified == ver.getStatus())
                .max(Comparator.comparingDouble(o -> Double.parseDouble(o.getName())))
                .orElseThrow(() -> new CoreException(new PackageNotFoundErrorBuilder(vspId).build()));
        } else {
            version = versions.stream()
                .filter(ver -> versionId.equals(ver.getName()) || versionId.equals(ver.getId()))
                .findFirst()
                .orElseThrow(() -> new CoreException(new PackageNotFoundErrorBuilder(vspId, versionId).build()));
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
        QuestionnaireResponse questionnaireResponse = vendorSoftwareProductManager.getVspQuestionnaire(vspId, new Version(versionId));
        if (questionnaireResponse.getErrorMessage() != null) {
            return Response.status(Response.Status.EXPECTATION_FAILED)
                .entity(new MapQuestionnaireResponseToQuestionnaireResponseDto().applyMapping(questionnaireResponse, QuestionnaireResponseDto.class))
                .build();
        }
        QuestionnaireResponseDto result = new MapQuestionnaireResponseToQuestionnaireResponseDto()
            .applyMapping(questionnaireResponse, QuestionnaireResponseDto.class);
        return Response.ok(result).build();
    }

    @Override
    public Response updateQuestionnaire(String questionnaireData, String vspId, String versionId, String user) {
        vendorSoftwareProductManager.updateVspQuestionnaire(vspId, new Version(versionId), questionnaireData);
        return Response.ok().build();
    }

    @Override
    public Response heal(String vspId, String versionId, String user) {
        HealingManagerFactory.getInstance().createInterface().healItemVersion(vspId, new Version(versionId), ItemType.vsp, true);
        return Response.ok().build();
    }

    @Override
    public Response getVspInformationArtifact(String vspId, String versionId, String user) {
        File textInformationArtifact = vendorSoftwareProductManager.getInformationArtifact(vspId, new Version(versionId));
        Response.ResponseBuilder response = Response.ok(textInformationArtifact);
        if (textInformationArtifact == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        response.header(CONTENT_DISPOSITION, ATTACHMENT_FILENAME + textInformationArtifact.getName());
        return response.build();
    }

    @Override
    public Response listComputes(String vspId, String version, String user) {
        Collection<ComputeEntity> computes = vendorSoftwareProductManager.getComputeByVsp(vspId, new Version(version));
        MapComputeEntityToVspComputeDto mapper = new MapComputeEntityToVspComputeDto();
        GenericCollectionWrapper<VspComputeDto> results = new GenericCollectionWrapper<>();
        for (ComputeEntity compute : computes) {
            results.add(mapper.applyMapping(compute, VspComputeDto.class));
        }
        return Response.ok(results).build();
    }

    private void updateVspItem(String vspId, VspDescriptionDto vspDescriptionDto) {
        Item retrievedItem = itemManager.get(vspId);
        Item item = new MapVspDescriptionDtoToItem().applyMapping(vspDescriptionDto, Item.class);
        item.setId(vspId);
        item.setType(retrievedItem.getType());
        item.setOwner(retrievedItem.getOwner());
        item.setStatus(retrievedItem.getStatus());
        item.setVersionStatusCounters(retrievedItem.getVersionStatusCounters());
        item.setCreationTime(retrievedItem.getCreationTime());
        item.setModificationTime(new Date());
        item.addProperty(VspItemProperty.ONBOARDING_METHOD, retrievedItem.getProperties().get(VspItemProperty.ONBOARDING_METHOD));
        itemManager.update(item);
    }

    private Optional<ValidationResponse> submit(String vspId, Version version, String message, String user) throws IOException {
        VspDetails vspDetails = vendorSoftwareProductManager.getVsp(vspId, version);
        if (vspDetails.getVlmVersion() != null) {
            vspDetails.setVlmVersion(versioningManager.get(vspDetails.getVendorId(), vspDetails.getVlmVersion()));
        }
        ValidationResponse validationResponse = vendorSoftwareProductManager.validate(vspDetails);
        Map<String, List<ErrorMessage>> compilationErrors = vendorSoftwareProductManager.compile(vspId, version);
        if (!validationResponse.isValid() || MapUtils.isNotEmpty(compilationErrors)) {
            activityLogManager
                .logActivity(new ActivityLogEntity(vspId, version, ActivityType.Submit, user, false, "Failed on validation before submit", ""));
            return Optional.of(validationResponse);
        }
        versioningManager.submit(vspId, version, message);
        activityLogManager.logActivity(new ActivityLogEntity(vspId, version, ActivityType.Submit, user, true, "", message));
        return Optional.empty();
    }

    private void notifyUsers(String itemId, String itemName, Version version, String message, String userName, NotificationEventTypes eventType) {
        Map<String, Object> eventProperties = new HashMap<>();
        eventProperties.put(ITEM_NAME, itemName == null ? itemManager.get(itemId).getName() : itemName);
        eventProperties.put(ITEM_ID, itemId);
        if (version != null) {
            eventProperties.put(VERSION_NAME, version.getName() == null ? versioningManager.get(itemId, version).getName() : version.getName());
            eventProperties.put(VERSION_ID, version.getId());
        }
        eventProperties.put(SUBMIT_DESCRIPTION, message);
        eventProperties.put(PERMISSION_USER, userName);
        Event syncEvent = new SyncEvent(eventType.getEventName(), itemId, eventProperties, itemId);
        try {
            notifier.notifySubscribers(syncEvent, userName);
        } catch (Exception e) {
            LOGGER.error("Failed to send sync notification to users subscribed o item '" + itemId, e);
        }
    }

    private Response createPackage(String vspId, Version version) throws IOException {
        Version retrievedVersion = versioningManager.get(vspId, version);
        if (retrievedVersion.getStatus() != VersionStatus.Certified) {
            throw new CoreException(new CreatePackageForNonFinalVendorSoftwareProductErrorBuilder(vspId, version).build());
        }
        PackageInfo packageInfo = vendorSoftwareProductManager.createPackage(vspId, retrievedVersion);
        return Response.ok(packageInfo == null ? null : new MapPackageInfoToPackageInfoDto().applyMapping(packageInfo, PackageInfoDto.class)).build();
    }

    private void addNetworkPackageInfo(String vspId, Version version, VspDetailsDto vspDetailsDto) {
        Optional<OrchestrationTemplateCandidateData> candidateInfo = OrchestrationTemplateCandidateManagerFactory.getInstance().createInterface()
            .getInfo(vspId, version);
        if (candidateInfo.isPresent()) {
            if (candidateInfo.get().getValidationDataStructure() != null) {
                vspDetailsDto.setValidationData(candidateInfo.get().getValidationDataStructure());
            }
            vspDetailsDto.setNetworkPackageName(candidateInfo.get().getFileName());
            vspDetailsDto.setCandidateOnboardingOrigin(candidateInfo.get().getFileSuffix());
        } else {
            OrchestrationTemplateEntity orchestrationTemplateInfo = vendorSoftwareProductManager.getOrchestrationTemplateInfo(vspId, version);
            if (Objects.nonNull(orchestrationTemplateInfo) && Objects.nonNull(orchestrationTemplateInfo.getFileSuffix())) {
                if (orchestrationTemplateInfo.getValidationDataStructure() != null) {
                    vspDetailsDto.setValidationData(orchestrationTemplateInfo.getValidationDataStructure());
                }
                vspDetailsDto.setNetworkPackageName(orchestrationTemplateInfo.getFileName());
                vspDetailsDto.setOnboardingOrigin(orchestrationTemplateInfo.getFileSuffix());
            }
        }
    }

    private boolean userHasPermission(String itemId, String userId) {
        return permissionsManager.getUserItemPermission(itemId, userId)
            .map(permission -> permission.matches(PermissionTypes.Contributor.name() + "|" + PermissionTypes.Owner.name())).orElse(false);
    }

    private Predicate<Item> createItemPredicate(String versionStatus, String itemStatus, String user) {
        Predicate<Item> itemPredicate = item -> ItemType.vsp.name().equals(item.getType());
        if (ItemStatus.ARCHIVED.name().equals(itemStatus)) {
            itemPredicate = itemPredicate.and(item -> ItemStatus.ARCHIVED.equals(item.getStatus()));
        } else {
            itemPredicate = itemPredicate.and(item -> ItemStatus.ACTIVE.equals(item.getStatus()));
            if (VersionStatus.Certified.name().equals(versionStatus)) {
                itemPredicate = itemPredicate.and(item -> item.getVersionStatusCounters().containsKey(VersionStatus.Certified));
            } else if (VersionStatus.Draft.name().equals(versionStatus)) {
                itemPredicate = itemPredicate
                    .and(item -> item.getVersionStatusCounters().containsKey(VersionStatus.Draft) && userHasPermission(item.getId(), user));
            }
        }
        return itemPredicate;
    }

    private List<Item> getVspList(String versionStatus, String itemStatus, String user) {
        Predicate<Item> itemPredicate = createItemPredicate(versionStatus, itemStatus, user);
        return itemManager.list(itemPredicate).stream().sorted((o1, o2) -> o2.getModificationTime().compareTo(o1.getModificationTime()))
            .collect(Collectors.toList());
    }

    private class SyncEvent implements Event {

        private final String eventType;
        private final String originatorId;
        private final Map<String, Object> attributes;
        private final String entityId;

        SyncEvent(String eventType, String originatorId, Map<String, Object> attributes, String entityId) {
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
}
