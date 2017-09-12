package org.openecomp.core.converter;

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;

import java.io.IOException;

public interface ToscaConverter {

  ToscaServiceModel convert(FileContentHandler fileContentHandler)
      throws IOException;
}
