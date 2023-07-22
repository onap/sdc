/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 CMCC Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.components.impl;

import lombok.Getter;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.UploadServiceInfo;
import org.springframework.stereotype.Component;

//upload Service model by Shiyong1989@hotmail.com
@Component("ServiceImportManager")
@Getter
public class ServiceImportManager {

    private final ServiceBusinessLogic serviceBusinessLogic;
    private final ServiceImportBusinessLogic serviceImportBusinessLogic;

    public ServiceImportManager(ServiceBusinessLogic serviceBusinessLogic, ServiceImportBusinessLogic serviceImportBusinessLogic) {
        this.serviceBusinessLogic = serviceBusinessLogic;
        this.serviceImportBusinessLogic = serviceImportBusinessLogic;
    }

    public boolean isServiceExist(String serviceName) {
        return serviceBusinessLogic.isServiceExist(serviceName);
    }

    public void populateServiceMetadata(UploadServiceInfo serviceMetaData, Service service) {
        if (service != null && serviceMetaData != null) {
            service.setDescription(serviceMetaData.getDescription());
            service.setTenant(serviceMetaData.getTenant());
            service.setTags(serviceMetaData.getTags());
            service.setCategories(serviceMetaData.getCategories());
            service.setContactId(serviceMetaData.getContactId());
            service.setName(serviceMetaData.getName());
            service.setIcon(serviceMetaData.getServiceIconPath());
            service.setServiceVendorModelNumber(serviceMetaData.getServiceVendorModelNumber());
            ServiceMetadataDataDefinition serviceMetadataDataDefinition = (ServiceMetadataDataDefinition) service.getComponentMetadataDefinition()
                    .getMetadataDataDefinition();
            serviceMetadataDataDefinition.getServiceVendorModelNumber();
            service.setServiceType(serviceMetaData.getServiceType());
            service.setServiceRole(serviceMetaData.getServiceRole());
            service.setNamingPolicy(serviceMetaData.getNamingPolicy());
            boolean ecompGeneratedNaming = serviceMetaData.getEcompGeneratedNaming() == null
                    || serviceMetaData.getEcompGeneratedNaming().equals("true");
            service.setEcompGeneratedNaming(ecompGeneratedNaming);
            service.setServiceFunction(serviceMetaData.getServiceFunction());
            service.setInstantiationType(serviceMetaData.getInstantiationType());
            service.setEnvironmentContext(serviceMetaData.getEnvironmentContext());
            service.setProjectCode(serviceMetaData.getProjectCode());
            service.setModel(serviceMetaData.getModel());
            if (serviceMetaData.getVendorName() != null) {
                service.setVendorName(serviceMetaData.getVendorName());
            }
            if (serviceMetaData.getVendorRelease() != null) {
                service.setVendorRelease(serviceMetaData.getVendorRelease());
            }
            service.setCategorySpecificMetadata(serviceMetaData.getCategorySpecificMetadata());
            service.setDerivedFromGenericType(serviceMetaData.getDerivedFromGenericType());
            service.setDerivedFromGenericVersion(serviceMetaData.getDerivedFromGenericVersion());
        }
    }

}
