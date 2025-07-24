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

import javax.inject.Named;
import org.openecomp.core.enrichment.types.MonitoringUploadType;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.MonitoringUploadsManager;
import org.openecomp.sdc.vendorsoftwareproduct.MonitoringUploadsManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.MonitoringUploadStatus;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.MonitoringUploadStatusDto;
import org.openecomp.sdcrests.vsp.rest.ComponentMonitoringUploads;
import org.openecomp.sdcrests.vsp.rest.mapping.MapMonitoringUploadStatusToDto;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author katyr
 * @since June 26, 2017
 */
@Named
@Service("componentMonitoringUploads")
@Scope(value = "prototype")
//@Validated
public class ComponentMonitoringUploadsImpl implements ComponentMonitoringUploads {

    private final MonitoringUploadsManager monitoringUploadsManager;
    private final ComponentManager componentManager;

    public ComponentMonitoringUploadsImpl() {
        this.monitoringUploadsManager = MonitoringUploadsManagerFactory.getInstance().createInterface();
        this.componentManager = ComponentManagerFactory.getInstance().createInterface();
    }

    public ComponentMonitoringUploadsImpl(MonitoringUploadsManager monitoringUploadsManager, ComponentManager componentManager) {
        this.monitoringUploadsManager = monitoringUploadsManager;
        this.componentManager = componentManager;
    }

    @Override
    public ResponseEntity upload(MultipartFile multipartFile, String vspId, String versionId, String componentId, String type, String user) throws Exception {
        Version version = new Version(versionId);
        componentManager.validateComponentExistence(vspId, version, componentId);
        MonitoringUploadType monitoringUploadType = getMonitoringUploadType(vspId, componentId, type);

        // Use MultipartFile's input stream and original filename
        monitoringUploadsManager.upload(
            multipartFile.getInputStream(),
            multipartFile.getOriginalFilename(),
            vspId,
            version,
            componentId,
            monitoringUploadType
        );

        return ResponseEntity.ok().build();
    }

    private MonitoringUploadType getMonitoringUploadType(String vspId, String componentId, String type) throws Exception {
        MonitoringUploadType monitoringUploadType;
        try {
            monitoringUploadType = MonitoringUploadType.valueOf(type);
        } catch (IllegalArgumentException exception) {
            String errorWithParameters = ErrorMessagesFormatBuilder
                .getErrorWithParameters(Messages.ILLEGAL_MONITORING_ARTIFACT_TYPE.getErrorMessage(), componentId, vspId);
            throw new Exception(errorWithParameters, exception);
        }
        return monitoringUploadType;
    }

    @Override
    public ResponseEntity delete(String vspId, String versionId, String componentId, String type, String user) throws Exception {
        MonitoringUploadType monitoringUploadType = getMonitoringUploadType(vspId, componentId, type);
        Version version = new Version(versionId);
        componentManager.validateComponentExistence(vspId, version, componentId);
        monitoringUploadsManager.delete(vspId, version, componentId, monitoringUploadType);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity list(String vspId, String versionId, String componentId, String user) {
        Version version = new Version(versionId);
        componentManager.validateComponentExistence(vspId, version, componentId);
        MonitoringUploadStatus response = monitoringUploadsManager.listFilenames(vspId, version, componentId);
        MonitoringUploadStatusDto returnEntity = new MapMonitoringUploadStatusToDto().applyMapping(response, MonitoringUploadStatusDto.class);
        return ResponseEntity.status(HttpStatus.OK).body(returnEntity);
    }
}
