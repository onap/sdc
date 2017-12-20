package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import org.openecomp.convertor.ElementConvertor;

import java.util.HashMap;
import java.util.Map;

import static org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToProcessConvertor.ARTIFACT_NAME;

/**
 * Created by ayalaben on 9/5/2017.
 */

public class ElementToMonitoringUploadMapConvertor extends  ElementConvertor<Map<String ,String>> {

  @Override
  public Map<String ,String> convert(Element element) {
    HashMap<String,String> map = new HashMap<>();
    map.put("File Name",element.getInfo().getProperty(ARTIFACT_NAME));
    return map;
  }
}
