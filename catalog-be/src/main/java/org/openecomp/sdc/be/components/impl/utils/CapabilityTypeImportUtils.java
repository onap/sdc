package org.openecomp.sdc.be.components.impl.utils;

import org.openecomp.sdc.be.model.CapabilityTypeDefinition;

import java.util.Objects;

public class CapabilityTypeImportUtils {

    private CapabilityTypeImportUtils() {
    }

    public static boolean isCapabilityTypesEquals(CapabilityTypeDefinition capabilityType1, CapabilityTypeDefinition capabilityType2) {
        if (capabilityType1 == capabilityType2) {
            return true;
        }
        
        if (capabilityType1 == null || capabilityType2 == null) {
            return false;
        }
        
        return Objects.equals(capabilityType1.getType(), capabilityType2.getType()) &&
                Objects.equals(capabilityType1.getVersion(), capabilityType2.getVersion()) &&
                Objects.equals(capabilityType1.getDerivedFrom(), capabilityType2.getDerivedFrom()) &&
                Objects.equals(capabilityType1.getValidSourceTypes(), capabilityType2.getValidSourceTypes()) &&
                Objects.equals(capabilityType1.getDescription(), capabilityType2.getDescription()) &&
                Objects.equals(capabilityType1.getProperties(), capabilityType2.getProperties());
    }
}
