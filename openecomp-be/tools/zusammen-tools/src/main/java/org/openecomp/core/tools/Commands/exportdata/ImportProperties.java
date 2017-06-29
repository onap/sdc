package org.openecomp.core.tools.Commands.exportdata;

import java.io.File;

public class ImportProperties {
    public static final String VERSION_FILE_PREFIX = "version_";
    public static final String VERSION_INFO_FILE_PREFIX = "version_info_";
    public static final String JSON_POSTFIX = ".json";

    public static final String ELEMENT_INFO_PREFIX = "elem_info";
    public static final String ELEMENT_RELATION_PREFIX = "elem_relations";
    public static final String ELEMENT_DATA_PREFIX = "elem_data";
    public static final String ELEMENT_VISUALIZATION_PREFIX = "elem_visualization";
    public static final String ELEMENT_SEARCHABLE_PREFIX = "elem_searchableData";
    public static final String ELEMENT_NAMESPACE_SPLITTER = "/";
    public static String ROOT_DIRECTORY;
    public static final void initParams(){
         ROOT_DIRECTORY = System.getProperty("user.home")+File.separator+ "onboarding_import";
    }
}
