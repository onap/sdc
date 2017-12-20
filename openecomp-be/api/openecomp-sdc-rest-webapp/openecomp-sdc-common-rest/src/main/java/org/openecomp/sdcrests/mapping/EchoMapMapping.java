package org.openecomp.sdcrests.mapping;

import java.util.HashMap;

/**
 * Created by ayalaben on 9/12/2017
 */
public class EchoMapMapping extends  MappingBase<HashMap<String,String>,HashMap<String,String>> {

  @Override
public void doMapping(HashMap<String,String> source, HashMap<String, String> target) {
    target.putAll(source);
    }
}
