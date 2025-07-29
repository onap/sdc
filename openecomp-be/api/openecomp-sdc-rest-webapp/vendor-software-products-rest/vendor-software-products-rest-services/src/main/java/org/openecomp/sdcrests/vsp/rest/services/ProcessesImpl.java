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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */
package org.openecomp.sdcrests.vsp.rest.services;

import javax.inject.Named;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ProcessRequestDto;
import org.openecomp.sdcrests.vsp.rest.ComponentProcesses;
import org.openecomp.sdcrests.vsp.rest.Processes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.context.annotation.ScopedProxyMode;
@Named
@Service("processes")
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ProcessesImpl implements Processes {

    private final ComponentProcesses componentProcesses;

    @Autowired
    public ProcessesImpl(ComponentProcesses componentProcesses) {
        this.componentProcesses = componentProcesses;
    }

    @Override
    public ResponseEntity list(String vspId, String versionId, String user) {
        return componentProcesses.list(vspId, versionId, null, user);
    }

    @Override
    public ResponseEntity deleteList(String vspId, String versionId, String user) {
        return componentProcesses.deleteList(vspId, versionId, null, user);
    }

    @Override
    public ResponseEntity create(ProcessRequestDto request, String vspId, String versionId, String user) {
        return componentProcesses.create(request, vspId, versionId, null, user);
    }

    @Override
    public ResponseEntity get(String vspId, String versionId, String processId, String user) {
        return componentProcesses.get(vspId, versionId, null, processId, user);
    }

    @Override
    public ResponseEntity delete(String vspId, String versionId, String processId, String user) {
        return componentProcesses.delete(vspId, versionId, null, processId, user);
    }

    @Override
    public ResponseEntity update(ProcessRequestDto request, String vspId, String versionId, String processId, String user) {
        return componentProcesses.update(request, vspId, versionId, null, processId, user);
    }

    @Override
    public ResponseEntity<byte[]> getUploadedFile(String vspId, String versionId, String processId, String user) {
        return componentProcesses.getUploadedFile(vspId, versionId, null, processId, user);
    }

    @Override
    public ResponseEntity deleteUploadedFile(String vspId, String versionId, String processId, String user) {
        return componentProcesses.deleteUploadedFile(vspId, versionId, null, processId, user);
    }

   @Override
    public ResponseEntity uploadFile(MultipartFile multipartFile, String vspId, String versionId, String processId, String user) {
        return componentProcesses.uploadFile(multipartFile, vspId, versionId, null, processId, user);
    }
    
}
