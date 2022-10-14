/*
 * Copyright © 2016-2017 European Support Limited
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
package org.openecomp.sdc.vendorsoftwareproduct.impl;

import static org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder.getErrorWithParameters;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.enrichment.types.MonitoringUploadType;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.MonitoringUploadsManager;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentArtifactDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentMonitoringUploadEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.MonitoringUploadErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.MonitoringUploadStatus;
import org.openecomp.sdc.vendorsoftwareproduct.utils.VendorSoftwareProductUtils;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersionableSubEntityNotFoundErrorBuilder;

public class MonitoringUploadsManagerImpl implements MonitoringUploadsManager {

    private final ComponentArtifactDao componentArtifactDao;

    MonitoringUploadsManagerImpl(ComponentArtifactDao componentArtifactDao) {
        this.componentArtifactDao = componentArtifactDao;
        componentArtifactDao.registerVersioning(VendorSoftwareProductConstants.VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE);
    }

    @Override
    public void delete(String vspId, Version version, String componentId, MonitoringUploadType monitoringUploadType) {
        ComponentMonitoringUploadEntity componentMonitoringUploadEntity = setValuesForComponentArtifactEntityUpload(vspId, version, null, componentId,
            null, monitoringUploadType, null);
        Optional<ComponentMonitoringUploadEntity> retrieved = componentArtifactDao.getByType(componentMonitoringUploadEntity);
        if (!retrieved.isPresent()) {
            throw new CoreException(
                new VersionableSubEntityNotFoundErrorBuilder(componentMonitoringUploadEntity.getEntityType(), monitoringUploadType.name(),
                    VspDetails.ENTITY_TYPE, componentMonitoringUploadEntity.getFirstClassCitizenId(), version).build());
        }
        componentArtifactDao.delete(retrieved.get());
    }

    @Override
    public void upload(InputStream object, String filename, String vspId, Version version, String componentId, MonitoringUploadType type) {
        if (object == null) {
            throw new CoreException(
                new MonitoringUploadErrorBuilder(getErrorWithParameters(Messages.NO_FILE_WAS_UPLOADED_OR_FILE_NOT_EXIST.getErrorMessage(), "zip"))
                    .build());
        } else {
            Map<String, List<ErrorMessage>> errors = new HashMap<>();
            try {
                byte[] uploadedFileData = FileUtils.toByteArray(object);
                final FileContentHandler upload = validateZip(vspId, version, uploadedFileData, errors);
                if (type.equals(MonitoringUploadType.VES_EVENTS)) {
                    validateVesEventUpload(upload, errors);
                }
                if (MapUtils.isNotEmpty(errors)) {
                    throw new CoreException(new MonitoringUploadErrorBuilder(errors.values().iterator().next().get(0).getMessage()).build());
                }
                createArtifactInDatabase(vspId, version, filename, componentId, type, uploadedFileData);
            } catch (Exception exception) {
                throw new CoreException(new MonitoringUploadErrorBuilder(exception.getMessage()).build());
            }
        }
    }

    private void validateVesEventUpload(FileContentHandler upload, Map<String, List<ErrorMessage>> errors) {
        if (!CommonUtil.validateAllFilesYml(upload)) {
            ErrorMessage.ErrorMessageUtil.addMessage(SdcCommon.UPLOAD_FILE, errors)
                .add(new ErrorMessage(ErrorLevel.ERROR, Messages.VES_ZIP_SHOULD_CONTAIN_YML_ONLY.getErrorMessage()));
            throw new CoreException(new MonitoringUploadErrorBuilder(Messages.VES_ZIP_SHOULD_CONTAIN_YML_ONLY.getErrorMessage()).build());
        }
    }

    private void createArtifactInDatabase(String vspId, Version version, String filename, String componentId, MonitoringUploadType type,
                                          byte[] uploadedFileData) {
        String artifactId = CommonMethods.nextUuId();
        ComponentMonitoringUploadEntity componentMonitoringUploadEntity = setValuesForComponentArtifactEntityUpload(vspId, version, filename,
            componentId, artifactId, type, uploadedFileData);
        componentArtifactDao.create(componentMonitoringUploadEntity);
    }

    @Override
    public MonitoringUploadStatus listFilenames(String vspId, Version version, String componentId) {
        ComponentMonitoringUploadEntity current = new ComponentMonitoringUploadEntity(vspId, version, componentId, null);
        return setMonitoringUploadStatusValues(current);
    }

    private MonitoringUploadStatus setMonitoringUploadStatusValues(ComponentMonitoringUploadEntity componentMonitoringUploadEntity) {
        MonitoringUploadStatus monitoringUploadStatus = new MonitoringUploadStatus();
        Collection<ComponentMonitoringUploadEntity> artifactNames = componentArtifactDao.list(componentMonitoringUploadEntity);
        Map<MonitoringUploadType, String> artifactTypeToFilename = VendorSoftwareProductUtils.mapArtifactsByType(artifactNames);
        if (MapUtils.isNotEmpty(artifactTypeToFilename)) {
            if (artifactTypeToFilename.containsKey(MonitoringUploadType.SNMP_TRAP)) {
                monitoringUploadStatus.setSnmpTrap(artifactTypeToFilename.get(MonitoringUploadType.SNMP_TRAP));
            }
            if (artifactTypeToFilename.containsKey(MonitoringUploadType.SNMP_POLL)) {
                monitoringUploadStatus.setSnmpPoll(artifactTypeToFilename.get(MonitoringUploadType.SNMP_POLL));
            }
            if (artifactTypeToFilename.containsKey(MonitoringUploadType.VES_EVENTS)) {
                monitoringUploadStatus.setVesEvent(artifactTypeToFilename.get(MonitoringUploadType.VES_EVENTS));
            }
        }
        return monitoringUploadStatus;
    }

    private ComponentMonitoringUploadEntity setValuesForComponentArtifactEntityUpload(String vspId, Version version, String filename,
                                                                                      String componentId, String artifactId,
                                                                                      MonitoringUploadType monitoringUploadType,
                                                                                      byte[] uploadedFileData) {
        ComponentMonitoringUploadEntity entity = new ComponentMonitoringUploadEntity();
        entity.setVspId(vspId);
        entity.setVersion(version);
        entity.setComponentId(componentId);
        entity.setId(artifactId);
        entity.setType(monitoringUploadType);
        entity.setArtifactName(filename);
        if (Objects.nonNull(uploadedFileData)) {
            entity.setArtifact(ByteBuffer.wrap(uploadedFileData));
        }
        return entity;
    }

    private FileContentHandler validateZip(String vspId, Version version, byte[] uploadedFileData, Map<String, List<ErrorMessage>> errors) {
        FileContentHandler contentMap;
        try {
            contentMap = CommonUtil.validateAndUploadFileContent(OnboardingTypesEnum.ZIP, uploadedFileData);
            VendorSoftwareProductUtils.validateContentZipData(contentMap, errors);
        } catch (IOException exception) {
            throw new CoreException(new MonitoringUploadErrorBuilder(vspId, version, Messages.INVALID_ZIP_FILE.getErrorMessage()).build());
        }
        return contentMap;
    }
}
