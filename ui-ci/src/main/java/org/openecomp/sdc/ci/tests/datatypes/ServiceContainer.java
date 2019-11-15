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

package org.openecomp.sdc.ci.tests.datatypes;

import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;


public class ServiceContainer {
    private Service service;
    private Resource resource;
    private VendorSoftwareProductObject vendorSoftwareProductObject;
    private VendorLicenseModel vendorLicenseModel;

    public ServiceContainer(Service service, Resource resource, VendorSoftwareProductObject vendorSoftwareProductObject, VendorLicenseModel vendorLicenseModel) {
        this.service = service;
        this.resource = resource;
        this.vendorSoftwareProductObject = vendorSoftwareProductObject;
        this.vendorLicenseModel = vendorLicenseModel;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public VendorSoftwareProductObject getVendorSoftwareProductObject() {
        return vendorSoftwareProductObject;
    }

    public void setVendorSoftwareProductObject(VendorSoftwareProductObject vendorSoftwareProductObject) {
        this.vendorSoftwareProductObject = vendorSoftwareProductObject;
    }

    public VendorLicenseModel getVendorLicenseModel() {
        return vendorLicenseModel;
    }

    public void setVendorLicenseModel(VendorLicenseModel vendorLicenseModel) {
        this.vendorLicenseModel = vendorLicenseModel;
    }
}
