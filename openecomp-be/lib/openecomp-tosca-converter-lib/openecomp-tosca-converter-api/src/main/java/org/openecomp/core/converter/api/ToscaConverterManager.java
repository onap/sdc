package org.openecomp.core.converter.api;

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;

public interface ToscaConverterManager {

  ToscaServiceModel convert(String csarName, FileContentHandler fileContentHandler);
}
