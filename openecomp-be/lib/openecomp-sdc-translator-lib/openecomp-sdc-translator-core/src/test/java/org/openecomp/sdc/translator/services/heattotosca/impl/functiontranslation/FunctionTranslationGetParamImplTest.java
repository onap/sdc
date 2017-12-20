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

package org.openecomp.sdc.translator.services.heattotosca.impl.functiontranslation;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation.BaseResourceTranslationTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FunctionTranslationGetParamImplTest extends BaseResourceTranslationTest {

  @Override
  @Before
  public void setUp() throws IOException {
    // do not delete this function. it prevents the superclass setup from running
  }

  @Test
  public void testStringGetParamFuncValue() throws Exception {
    FunctionTranslationGetParamImpl translationImpl = new FunctionTranslationGetParamImpl();
    String functionValue = "parameter1";

    Object translatedFunc = translationImpl.translateFunction(null, null, null, "get_param",
        functionValue, "dummy", null, null, null);

    assertEquals(true, translatedFunc instanceof Map);
    if (translatedFunc instanceof Map) {
      assertNotNull(((Map) translatedFunc).get("get_input"));
      assertEquals(functionValue, ((Map) translatedFunc).get("get_input"));
    }

  }

  @Test
  public void testListGetParamFuncValue() throws Exception {
    FunctionTranslationGetParamImpl translationImpl = new FunctionTranslationGetParamImpl();
    List functionValue = new ArrayList();
    functionValue.add("parameter1");
    functionValue.add(0);
    functionValue.add("key1");

    Object translatedFunc = translationImpl.translateFunction(null, null, null,"get_param",
        functionValue, "dummy", null, null, null);

    assertEquals(true, translatedFunc instanceof Map);
    if (translatedFunc instanceof Map) {
      assertNotNull(((Map) translatedFunc).get("get_input"));
      Object translatedFunValue = ((Map) translatedFunc).get("get_input");
      assertEquals(true, translatedFunValue instanceof List);
      if (translatedFunValue instanceof List) {

        assertEquals(functionValue.get(0), ((List) translatedFunValue).get(0));
        assertEquals(functionValue.get(1), ((List) translatedFunValue).get(1));
        assertEquals(functionValue.get(2), ((List) translatedFunValue).get(2));
      }
    }

  }

  @Test
  public void testMapGetParamFuncValue() throws Exception {
    // input heat function expression
    //{get_param: [parameter1, {get_param:indexParam}, key1]}
    // output translated function expression
    //{get_input: [parameter1, {get_input:indexParam}, key1]}

    FunctionTranslationGetParamImpl translationImpl = new FunctionTranslationGetParamImpl();
    List functionValue = new ArrayList();
    functionValue.add("parameter1");
    Map innerParamMap = new HashMap();
    innerParamMap.put("get_param", "indexParam");
    functionValue.add(innerParamMap);
    functionValue.add("key1");

    Object translatedFunc = translationImpl.translateFunction(null, null, null, "get_param",
        functionValue, "dummy", null, null, null);

    assertEquals(true, translatedFunc instanceof Map);
    if (translatedFunc instanceof Map) {
      assertNotNull(((Map) translatedFunc).get("get_input"));
      Object translatedFunValue = ((Map) translatedFunc).get("get_input");
      assertEquals(true, translatedFunValue instanceof List);
      if (translatedFunValue instanceof List) {
        assertEquals(functionValue.get(0), ((List) translatedFunValue).get(0));
        assertEquals(functionValue.get(2), ((List) translatedFunValue).get(2));
        assertEquals(true, ((List) translatedFunValue).get(1) instanceof Map);
        if (((List) translatedFunValue).get(1) instanceof Map) {
          assertEquals(innerParamMap.get("get_param"), ((Map) ((List) translatedFunValue).get(1))
              .get("get_input"));
        }
      }

    }
  }

  @Test
  public void testMapWithMapGetParamFuncValue() throws Exception {
    // input heat function expression
    //{get_param: [parameter1, {get_param:[parameter2, {get_param:indexParam}]}, key1]}
    // output translated function expression
    //{get_input: [parameter1, {get_input:[parameter2, {get_input:indexParam}]}, key1]}

    FunctionTranslationGetParamImpl translationImpl = new FunctionTranslationGetParamImpl();
    List functionValue = new ArrayList();
    functionValue.add("parameter1");
    Map firstInnerParamMap = new HashMap();
    Map secondInnerParamMap = new HashMap();
    secondInnerParamMap.put("get_param", "indexParam");
    List innerfunction = new ArrayList();
    innerfunction.add("parameter2");
    innerfunction.add(secondInnerParamMap);
    firstInnerParamMap.put("get_param", innerfunction);
    functionValue.add(firstInnerParamMap);
    functionValue.add("key1");

    Object translatedFunc = translationImpl.translateFunction(null, null, null, "get_param",
        functionValue, "dummy", null, null, null);

    assertEquals(true, translatedFunc instanceof Map);
    if (translatedFunc instanceof Map) {
      assertNotNull(((Map) translatedFunc).get("get_input"));
      Object translatedFunValue = ((Map) translatedFunc).get("get_input");
      assertEquals(true, translatedFunValue instanceof List);
      if (translatedFunValue instanceof List) {
        assertEquals(functionValue.get(0), ((List) translatedFunValue).get(0));
        assertEquals(functionValue.get(2), ((List) translatedFunValue).get(2));
        assertEquals(true, ((List) translatedFunValue).get(1) instanceof Map);
        if (((List) translatedFunValue).get(1) instanceof Map) {
          assertEquals(true, ((Map) ((List) translatedFunValue).get(1)).get("get_input")
              instanceof List);
          List innerTranslatedFunction =
              (List) ((Map) ((List) translatedFunValue).get(1)).get("get_input");
          assertEquals(innerfunction.get(0), innerTranslatedFunction.get(0));
          assertEquals(true, innerTranslatedFunction.get(1) instanceof Map);
          assertEquals(secondInnerParamMap.get("get_param"),
              ((Map) innerTranslatedFunction.get(1)).get("get_input"));
        }
      }

    }
  }

  @Test
  public void testInnerNotSupportedFuncGetParamFuncValue() throws Exception {
    // input heat function expression
    //{get_param: [parameter1, {str_replace: {template:$AAkgiru, AA:{get_param:prameter2}}}, key1]}
    // output translated function expression
    //{get_input: [parameter1, {str_replace: {template:$AAkgiru, AA:{get_input:parameter2}}}, key1]}

    FunctionTranslationGetParamImpl translationImpl = new FunctionTranslationGetParamImpl();
    List functionValue = new ArrayList();
    functionValue.add("parameter1");

    Map templateMap = new HashMap();
    templateMap.put("template", "$AAkgiru");

    Map strReplaceFuncMap = new HashMap();
    Map getParamMapInner = new HashMap();
    getParamMapInner.put("get_param", "parameter2");
    templateMap.put("AA", getParamMapInner);

    Map innerParamValue = new HashMap();
    innerParamValue.putAll(templateMap);
    innerParamValue.putAll(strReplaceFuncMap);

    strReplaceFuncMap.put("str_replace", innerParamValue);
    functionValue.add(strReplaceFuncMap);

    functionValue.add("key1");

    Object translatedFunc = translationImpl.translateFunction(null, null, null, "get_param",
        functionValue, "dummy", null, null, null);

    assertEquals(true, translatedFunc instanceof Map);
    if (translatedFunc instanceof Map) {
      assertNotNull(((Map) translatedFunc).get("get_input"));
      Object translatedFunValue = ((Map) translatedFunc).get("get_input");
      assertEquals(true, translatedFunValue instanceof List);
      if (translatedFunValue instanceof List) {
        assertEquals(functionValue.get(0), ((List) translatedFunValue).get(0));
        assertEquals(functionValue.get(2), ((List) translatedFunValue).get(2));
        assertEquals(true, ((List) translatedFunValue).get(1) instanceof Map);
        if (((List) translatedFunValue).get(1) instanceof Map) {
          assertEquals(strReplaceFuncMap.get("get_param"),
              ((Map) ((List) translatedFunValue).get(1))
                  .get("get_input"));
        }
        assertEquals(true, ((List) translatedFunValue).get(1) instanceof Map);
        if (((List) translatedFunValue).get(1) instanceof Map) {
          assertEquals(true, ((Map) ((List) translatedFunValue).get(1)).get("str_replace") instanceof Map);
          Map strReplacefunctionValue = (Map) ((Map) ((List) translatedFunValue).get(1)).get("str_replace");
          assertEquals(templateMap.get("template"), strReplacefunctionValue.get("template"));
          assertEquals(true, strReplacefunctionValue.get("AA") instanceof Map);
          if (strReplacefunctionValue.get("AA") instanceof Map) {
            assertEquals(getParamMapInner.get("get_param"), ((Map) strReplacefunctionValue.get
                ("AA")).get("get_input"));
          }
        }
      }
    }
  }

  @Test
  public void testTranslateHeatPseudoParamUsedFromMainHeat() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/heatPseudoParameters/usedFromMainHeat/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/heatPseudoParameters/usedFromMainHeat/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }

  @Test
  public void testTranslateHeatPseudoParamUsedFromNestedHeat() throws Exception {
    inputFilesPath =
        "/mock/services/heattotosca/heatPseudoParameters/usedFromNestedHeat/inputfiles";
    outputFilesPath =
        "/mock/services/heattotosca/heatPseudoParameters/usedFromNestedHeat/expectedoutputfiles";
    initTranslatorAndTranslate();
    testTranslation();
  }


}
