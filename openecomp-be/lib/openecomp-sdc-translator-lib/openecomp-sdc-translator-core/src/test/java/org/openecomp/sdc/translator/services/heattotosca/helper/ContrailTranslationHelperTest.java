/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.translator.services.heattotosca.helper;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * @author SHIRIA
 * @since December 21, 2016.
 */
public class ContrailTranslationHelperTest {

  @Test
  public void testgetComputeNodeTypeId()
      throws Exception {
    Resource serviceTemplate = new Resource();
    serviceTemplate.setProperties(new HashMap<>());
    serviceTemplate.getProperties().put("image_name", "aaaa");
    Map flavor = new HashMap<>();
    flavor.put("get_param", "bbb_flavor_name");
    serviceTemplate.getProperties().put("flavor", flavor);
    String computeNodeTypeId =
        new ContrailTranslationHelper()
            .getComputeNodeTypeId(serviceTemplate, "123", "123", new TranslationContext());
    Assert.assertEquals("org.openecomp.resource.vfc.nodes.heat.bbb", computeNodeTypeId);
  }

  @Test
  public void testTranslateFnSplitFunctionExp1() {
    // property value = { "Fn::Split" : [ ",", "management,left,right,other" ] }
    Map propertyValue = new HashMap();
    List funcListVal = new ArrayList<>();
    funcListVal.add(",");
    funcListVal.add("management,left,right,other");
    propertyValue.put("Fn::Split", funcListVal);
    Optional<List<Map<String, List>>> translatedFun =
        new ContrailTranslationHelper().translateFnSplitFunction(propertyValue, 4, false);

    assertEquals(true, translatedFun.isPresent());
    if (translatedFun.isPresent()) {
      assertEquals(4, translatedFun.get().size());
      for (int i = 0; i < translatedFun.get().size(); i++) {
        assertEquals("management,left,right,other", translatedFun.get().get(i).get("token").get(0));
        assertEquals(",", translatedFun.get().get(i).get("token").get(1));
        assertEquals(i, translatedFun.get().get(i).get("token").get(2));
      }
    }
  }

  @Test
  public void testTranslateFnSplitFunctionBoolean() {
    // property value = { "Fn::Split" : [ ";", "n;false;false;false" ] }
    Map propertyValue = new HashMap();
    List funcListVal = new ArrayList<>();
    funcListVal.add(";");
    funcListVal.add("n;false;false;false");
    propertyValue.put("Fn::Split", funcListVal);
    Optional<List<Map<String, List>>> translatedFun =
        new ContrailTranslationHelper().translateFnSplitFunction(propertyValue, 4, true);

    assertEquals(true, translatedFun.isPresent());
    if (translatedFun.isPresent()) {
      assertEquals(4, translatedFun.get().size());
      for (int i = 0; i < translatedFun.get().size(); i++) {
        assertEquals("false;false;false;false", translatedFun.get().get(i).get("token").get(0));
        assertEquals(";", translatedFun.get().get(i).get("token").get(1));
        assertEquals(i, translatedFun.get().get(i).get("token").get(2));
      }
    }
  }

  @Test
  public void testTranslateFnSplitFunctionExp2() {
    // property value =  { "Fn::Split" : [ ";", "n;false;false;false" ] }
    Map propertyValue = new HashMap();
    List funcListVal = new ArrayList<>();
    funcListVal.add(";");
    funcListVal.add("n;false;false;false");
    propertyValue.put("Fn::Split", funcListVal);
    Optional<List<Map<String, List>>> translatedFun =
        new ContrailTranslationHelper().translateFnSplitFunction(propertyValue, 4, false);

    assertEquals(true, translatedFun.isPresent());
    if (translatedFun.isPresent()) {
      assertEquals(4, translatedFun.get().size());
      for (int i = 0; i < translatedFun.get().size(); i++) {
        assertEquals("n;false;false;false", translatedFun.get().get(i).get("token").get(0));
        assertEquals(";", translatedFun.get().get(i).get("token").get(1));
        assertEquals(i, translatedFun.get().get(i).get("token").get(2));
      }
    }
  }

  @Test
  public void testTranslateFnSplitFunctionWithParam() {
    // property value = { "Fn::Split" : [ ",", Ref: st_shared_ip_list ] }
    Map propertyValue = new HashMap();
    List funcListVal = new ArrayList<>();
    funcListVal.add(",");
    Map innerMap = new HashMap();
    innerMap.put("Ref", "st_shared_ip_list");
    funcListVal.add(innerMap);
    propertyValue.put("Fn::Split", funcListVal);
    Optional<List<Map<String, List>>> translatedFun =
        new ContrailTranslationHelper().translateFnSplitFunction(propertyValue, 5, false);

    assertEquals(true, translatedFun.isPresent());
    if (translatedFun.isPresent()) {
      assertEquals(5, translatedFun.get().size());
      for (int i = 0; i < translatedFun.get().size(); i++) {
        assertEquals("st_shared_ip_list",

            ((Map) translatedFun.get().get(i).get("token").get(0)).get("get_input"));
        assertEquals(",", translatedFun.get().get(i).get("token").get(1));
        assertEquals(i, translatedFun.get().get(i).get("token").get(2));
      }
    }
  }
}

