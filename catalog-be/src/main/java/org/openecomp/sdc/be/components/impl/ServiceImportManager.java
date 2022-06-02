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

import javax.servlet.ServletContext;
import org.openecomp.sdc.be.datatypes.components.ServiceMetadataDataDefinition;
import org.openecomp.sdc.be.impl.WebAppContextWrapper;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.UploadServiceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

//upload Service model by Shiyong1989@hotmail.com
@Component("ServiceImportManager")
public class ServiceImportManager {

    private ServletContext servletContext;
    private ResponseFormatManager responseFormatManager;
    @Autowired
    private ServiceBusinessLogic serviceBusinessLogic;
    @Autowired
    private ServiceImportBusinessLogic serviceImportBusinessLogic;

    public ServiceImportBusinessLogic getServiceImportBusinessLogic() {
        return serviceImportBusinessLogic;
    }

    public void setServiceImportBusinessLogic(ServiceImportBusinessLogic serviceImportBusinessLogic) {
        this.serviceImportBusinessLogic = serviceImportBusinessLogic;
    }

    public boolean isServiceExist(String serviceName) {
        return serviceBusinessLogic.isServiceExist(serviceName);
    }

    public ServiceBusinessLogic getServiceBusinessLogic() {
        return serviceBusinessLogic;
    }

    public void setServiceBusinessLogic(ServiceBusinessLogic serviceBusinessLogic) {
        this.serviceBusinessLogic = serviceBusinessLogic;
    }

    public void populateServiceMetadata(UploadServiceInfo serviceMetaData, Service service) {
        if (service != null && serviceMetaData != null) {
            service.setDescription(serviceMetaData.getDescription());
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

    public synchronized void init(ServletContext servletContext) {
        if (this.servletContext == null) {
            this.servletContext = servletContext;
            responseFormatManager = ResponseFormatManager.getInstance();
            serviceBusinessLogic = getServiceBL(servletContext);
        }
    }

    private ServiceBusinessLogic getServiceBL(ServletContext context) {
        WebAppContextWrapper webApplicationContextWrapper = (WebAppContextWrapper) context
            .getAttribute(org.openecomp.sdc.common.api.Constants.WEB_APPLICATION_CONTEXT_WRAPPER_ATTR);
        WebApplicationContext webApplicationContext = webApplicationContextWrapper.getWebAppContext(context);
        return webApplicationContext.getBean(ServiceBusinessLogic.class);
    }
}
