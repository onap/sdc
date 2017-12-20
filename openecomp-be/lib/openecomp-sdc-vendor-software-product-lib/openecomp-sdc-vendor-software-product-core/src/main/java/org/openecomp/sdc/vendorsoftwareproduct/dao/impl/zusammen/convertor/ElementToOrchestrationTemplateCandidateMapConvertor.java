package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;

/**
 * Created by ayalaben on 9/6/2017
 */
public class ElementToOrchestrationTemplateCandidateMapConvertor
    extends ElementConvertor<FilesDataStructure> {

  @Override
  public FilesDataStructure convert(Element element) {
    return JsonUtil.json2Object(new String(FileUtils.toByteArray(element.getData())),
        FilesDataStructure.class);
  }
}
