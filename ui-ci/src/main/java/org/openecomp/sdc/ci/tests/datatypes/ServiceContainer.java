package org.openecomp.sdc.ci.tests.datatypes;

import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;


public class ServiceContainer {
    private Service service;
    private Resource resource;
    private VendorSoftwareProductObject vendorSoftwareProductObject;
    private AmdocsLicenseMembers amdocsLicenseMembers;

    public ServiceContainer(Service service, Resource resource, VendorSoftwareProductObject vendorSoftwareProductObject, AmdocsLicenseMembers amdocsLicenseMembers) {
        this.service = service;
        this.resource = resource;
        this.vendorSoftwareProductObject = vendorSoftwareProductObject;
        this.amdocsLicenseMembers = amdocsLicenseMembers;
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

    public AmdocsLicenseMembers getAmdocsLicenseMembers() {
        return amdocsLicenseMembers;
    }

    public void setAmdocsLicenseMembers(AmdocsLicenseMembers amdocsLicenseMembers) {
        this.amdocsLicenseMembers = amdocsLicenseMembers;
    }
}
