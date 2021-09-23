package org.onap.sdc.frontend.ci.tests.datatypes;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ToscaTypeUploadEnum {

    DATA("datatypes", "dataTypes", "data-types", "dataTypesZip"),
    CAPABILITY("capability", "capabilityTypes", "capability-types", "capabilityTypeZip"),
    RELATIONSHIP("relationship", "relationshipTypes", "relationship-types", "relationshipTypeZip"),
    INTERFACE("interfaceLifecycle", "interfaceLifecycleTypes", "interface-lifecycle-types", "interfaceLifecycleTypeZip"),
    GROUP("grouptypes", "groupTypes", "group-types", "groupTypesZip", true),
    POLICY("policytypes", "policyTypes", "policy-types", "policyTypesZip", true);

    private ToscaTypeUploadEnum(final String urlSuffix, final String modelParam, final String directory, final String zipParam) {
        this.urlSuffix = urlSuffix;
        this.modelParam = modelParam;
        this.directory = directory;
        this.zipParam = zipParam;
        this.metadata = false;
    }

    private String urlSuffix;
    private String modelParam;
    private String directory;
    private String zipParam;
    private boolean metadata;
}