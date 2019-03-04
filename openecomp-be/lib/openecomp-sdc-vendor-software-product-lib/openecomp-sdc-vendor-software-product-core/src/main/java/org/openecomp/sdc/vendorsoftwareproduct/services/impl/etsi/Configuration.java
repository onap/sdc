package org.openecomp.sdc.vendorsoftwareproduct.services.impl.etsi;

import java.util.Map;

public class Configuration {
    private Map<String, NonManoType> nonManoKeyFolderMapping;

    public Map<String, NonManoType> getNonManoKeyFolderMapping() {
        return nonManoKeyFolderMapping;
    }

    public void setNonManoKeyFolderMapping(Map<String, NonManoType> nonManoKeyFolderMapping) {
        this.nonManoKeyFolderMapping = nonManoKeyFolderMapping;
    }
}
