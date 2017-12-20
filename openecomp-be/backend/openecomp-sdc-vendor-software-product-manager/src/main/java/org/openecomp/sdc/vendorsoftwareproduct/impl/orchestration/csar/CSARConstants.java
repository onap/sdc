package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar;

import com.google.common.collect.ImmutableSet;

import static com.google.common.collect.ImmutableSet.of;
public class CSARConstants {

    public static final ImmutableSet<String> ELIGBLE_FOLDERS = of("Artifacts/","Definitions/",
            "Licenses/", "TOSCA-Metadata/");

    public static final String MAIN_SERVICE_TEMPLATE_MF_FILE_NAME = "MainServiceTemplate.mf";
    public static final String MAIN_SERVICE_TEMPLATE_YAML_FILE_NAME = "MainServiceTemplate.yaml";
    public static final ImmutableSet<String> ELIGIBLE_FILES =
            of(MAIN_SERVICE_TEMPLATE_MF_FILE_NAME,MAIN_SERVICE_TEMPLATE_YAML_FILE_NAME);

    public static final String METADATA_MF_ATTRIBUTE = "metadata";
    public static final String SOURCE_MF_ATTRIBUTE = "source";
    public static final String SEPERATOR_MF_ATTRIBUTE = ":";
}
