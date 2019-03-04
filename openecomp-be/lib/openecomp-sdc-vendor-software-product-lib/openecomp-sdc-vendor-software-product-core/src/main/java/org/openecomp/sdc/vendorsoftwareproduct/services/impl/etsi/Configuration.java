package org.openecomp.sdc.vendorsoftwareproduct.services.impl.etsi;

import java.util.Map;

import lombok.Data;

@Data
public class Configuration {
    private Map<String, NonManoType> nonManoKeyFolderMapping;
}
