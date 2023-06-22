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

package org.onap.sdc.backend.ci.tests.utils.general;

import com.google.gson.Gson;
import fj.data.Either;
import org.apache.commons.codec.binary.Base64;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.VendorLicenseModel;
import org.onap.sdc.backend.ci.tests.datatypes.VendorSoftwareProductObject;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OnboardingUtilsViaApis {

    public VendorSoftwareProductObject createVspViaApis(ResourceReqDetails resourceReqDetails, String filepath, String vnfFile, User user) throws Exception {

        VendorLicenseModel vendorLicenseModel = new VendorLicenseModelRestUtils().createVendorLicense(user);
        return new VendorSoftwareProductRestUtils().createVendorSoftwareProduct(resourceReqDetails, vnfFile, filepath, user,
                vendorLicenseModel);
    }

    public Resource createResourceFromVSP(ResourceReqDetails resourceDetails) throws Exception {
        Resource resource = new AtomicOperationUtils().createResourceByResourceDetails(resourceDetails, UserRoleEnum.DESIGNER, true).left().value();
        return resource;

    }

    public Resource createResourceFromVSP(ResourceReqDetails resourceDetails, UserRoleEnum userRole) throws Exception {
        return new AtomicOperationUtils().createResourceByResourceDetails(resourceDetails, userRole, true).left().value();
    }

    public void downloadToscaCsarToDirectory(Component component, File file) {
        try {
            Either<String, RestResponse> componentToscaArtifactPayload = new AtomicOperationUtils().getComponenetArtifactPayload(component, "assettoscacsar");
            if (componentToscaArtifactPayload.left().value() != null) {
                convertPayloadToFile(componentToscaArtifactPayload.left().value(), file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void convertPayloadToFile(String payload, File file) throws IOException {

        Gson gson = new Gson();
        @SuppressWarnings("unchecked")
        Map<String, String> fromJson = gson.fromJson(payload, Map.class);
        String string = fromJson.get("base64Contents").toString();
        byte[] byteArray = Base64.decodeBase64(string.getBytes(StandardCharsets.UTF_8));
        File downloadedFile = new File(file.getAbsolutePath());
        FileOutputStream fos = new FileOutputStream(downloadedFile);
        fos.write(byteArray);
        fos.flush();
        fos.close();
    }

    public ResourceReqDetails prepareOnboardedResourceDetailsBeforeCreate(ResourceReqDetails resourceDetails, VendorSoftwareProductObject vendorSoftwareProductObject) {

        List<String> tags = new ArrayList<>();
        tags.add(vendorSoftwareProductObject.getName());
        resourceDetails.setCsarUUID(vendorSoftwareProductObject.getVspId());
        resourceDetails.setCsarVersion(vendorSoftwareProductObject.getVersion());
        resourceDetails.setCsarVersionId(vendorSoftwareProductObject.getVersionId());
        resourceDetails.setName(vendorSoftwareProductObject.getName());
        resourceDetails.setTags(tags);
        resourceDetails.setDescription(vendorSoftwareProductObject.getDescription());
        resourceDetails.setVendorName(vendorSoftwareProductObject.getVendorName());
        resourceDetails.setResourceType("VF");
        resourceDetails.setResourceVendorModelNumber("666");
        resourceDetails.setContactId(vendorSoftwareProductObject.getAttContact());

        return resourceDetails;
    }

    public ServiceReqDetails prepareServiceDetailsBeforeCreate(User user) {
        ServiceReqDetails serviceDetails = new ElementFactory().getDefaultService(ServiceCategoriesEnum.NETWORK_L4, user);
        serviceDetails.setServiceType("MyServiceType");
        serviceDetails.setServiceRole("MyServiceRole");
        serviceDetails.setNamingPolicy("MyServiceNamingPolicy");
        serviceDetails.setEcompGeneratedNaming(false);
        serviceDetails.setDerivedFromGenericType("org.openecomp.resource.abstract.nodes.service");
        serviceDetails.setDerivedFromGenericVersion("1.0");

        return serviceDetails;
    }
}
