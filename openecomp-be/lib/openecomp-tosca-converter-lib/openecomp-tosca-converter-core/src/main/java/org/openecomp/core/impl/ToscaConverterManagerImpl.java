package org.openecomp.core.impl;

import org.openecomp.core.converter.ToscaConverter;
import org.openecomp.core.converter.api.ToscaConverterManager;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ToscaConverterManagerImpl implements ToscaConverterManager {

  private static List<ToscaConverter> toscaConverters;
  private static final String toscaConverterFileName = "ToscaConverters.json";

  static {
    toscaConverters = getConvertersList();
  }

  @Override
  public ToscaServiceModel convert(String csarName, FileContentHandler fileContentHandler) {
    return null;
  }

  private static List<ToscaConverter> getConvertersList(){
    List<ToscaConverter> toscaConvertersList = new ArrayList<>();
    Map<String, String> convertersMap = FileUtils.readViaInputStream(toscaConverterFileName,
            stream -> JsonUtil.json2Object(stream, Map.class));
    return getToscaConvertersList(toscaConvertersList, convertersMap);
  }

  private static List<ToscaConverter> getToscaConvertersList(
      List<ToscaConverter> toscaConvertersList, Map<String, String> convertersMap) {
    for(String implClassName : convertersMap.values()){
      try{
        Class<?> clazz = Class.forName(implClassName);
        Constructor<?> constructor = clazz.getConstructor();
        toscaConvertersList.add((ToscaConverter) constructor.newInstance());
      }catch (Exception e){
        continue;
      }
    }
    return toscaConvertersList;
  }
}
