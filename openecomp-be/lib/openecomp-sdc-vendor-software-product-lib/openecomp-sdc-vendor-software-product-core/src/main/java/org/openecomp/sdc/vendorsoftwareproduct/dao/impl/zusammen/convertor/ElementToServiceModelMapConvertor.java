package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import org.openecomp.convertor.ElementConvertor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ayalaben on 9/11/2017
 */
public class ElementToServiceModelMapConvertor extends ElementConvertor<Map<String ,String>> {
  @Override
  public Map<String ,String> convert(Element element) {
    //TODO : after merge with 1802 change to heat fle name/TOSCA file name
    HashMap<String,String> map = new HashMap<>();
    map.put("Service Model Definition Entry", element.getInfo().getProperty("base"));
    return map;
  }
}
