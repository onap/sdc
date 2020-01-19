/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.components.merge.property;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import fj.data.Either;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.sdc.be.components.utils.PropertyDataDefinitionBuilder;
import org.openecomp.sdc.be.datatypes.elements.GetInputValueDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PropertyDataValueMergeBusinessLogicTest {

    private ObjectMapper mapper = new ObjectMapper();
    
    private PropertyDataValueMergeBusinessLogic testInstance;

    @Mock
    private ApplicationDataTypeCache applicationDataTypeCache;

    @Before
    public void setUp() throws Exception {
        PropertyValueMerger propertyValueMerger = new PropertyValueMerger();
        
        testInstance = new PropertyDataValueMergeBusinessLogic(propertyValueMerger, applicationDataTypeCache);
    }

    @Test
    public void mergeProperties_emptyOldAndNewValues() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.STRING.getType(), null, null);
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.STRING.getType(), null, null);
        testMergeProps(oldProp, newProp, null);
    }

    @Test
    public void mergeProperties_emptyOldValue() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.STRING.getType(), null, null);
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.STRING.getType(), null, "newVal");
        testMergeProps(oldProp, newProp, "newVal");
    }

    @Test
    public void mergeSimpleStringType_copyOldValueIfNoNewValue() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.STRING.getType(), null, "val1");
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.STRING.getType(), null, null);
        testMergeProps(oldProp, newProp, "val1");
    }

    @Test
    public void mergeSimpleStringType_dontCopyOldValIfHasNewVal() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.STRING.getType(), null, "val1");
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.STRING.getType(), null, "newVal");
        testMergeProps(oldProp, newProp, "newVal");
    }

    @Test
    public void mergeSimpleIntType_copyOldValueIfNoNewValue() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.INTEGER.getType(), null, "44");
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.INTEGER.getType(), null, null);
        testMergeProps(oldProp, newProp, "44");
    }

    @Test
    public void mergeSimpleIntType_dontCopyOldValIfHasNewVal() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.INTEGER.getType(), null, "44");
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.INTEGER.getType(), null, "45");
        testMergeProps(oldProp, newProp, "45");
    }

    @Test
    public void mergeSimpleBooleanType_copyOldValueIfNoNewValue() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.BOOLEAN.getType(), null, "false");
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.BOOLEAN.getType(), null, null);
        testMergeProps(oldProp, newProp, "false");
    }

    @Test
    public void mergeSimpleBooleanType_dontCopyOldValIfHasNewVal() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.BOOLEAN.getType(), null, "false");
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.BOOLEAN.getType(), null, "true");
        testMergeProps(oldProp, newProp, "true");
    }

    @Test
    public void mergeSimpleListType_copyOldValuesByIndex() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.LIST.getType(), "string", "[\"a\", \"b\"]");
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.LIST.getType(), "string", "[\"x\", \"\"]");
        testMergeProps(oldProp, newProp, "[\"x\",\"b\"]");
    }
    
    @Test
    public void mergeSimpleListType_differentSize() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.LIST.getType(), "string", "[\"a\", \"b\", \"c\"]");
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.LIST.getType(), "string", "[\"x\", \"\"]");
        testMergeProps(oldProp, newProp, "[\"x\",\"\"]");
    }

    @Test
    public void mergeSimpleListType_jsonList() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", ToscaPropertyType.LIST.getType(), "json", "[[\"a\", \"b\"], \"c\"]");
        PropertyDataDefinition newProp = createProp("prop1", ToscaPropertyType.LIST.getType(), "json", "[[\"a\"], \"d\"]");
        testMergeProps(oldProp, newProp, "[[\"a\"],\"d\"]");
    }

    /**                                   
     * Old property:                       New property:                           Expected:                           
     *   {                                  {                                       {                                 
     *     "mac_range_plan": "y",             "mac_range_plan": "",                   "mac_range_plan": "y",          
     *     "mac_count_required": {            "mac_count_required": {                 "mac_count_required": {         
     *       "is_required": true,               "is_required": false,                   "is_required": false,         
     *       "count": 44                        "mac_address": "myAddress"              "mac_address": "myAddress"    
     *     }                                  }                                       }                               
     *   }                                  }                                       }                                 
     *                                                                                                                
     */
    @Test
    public void mergeComplexType() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", "myType", null, "{\"mac_range_plan\":\"y\", \"mac_count_required\":{\"is_required\":true,\"count\":44}}");
        PropertyDataDefinition newProp = createProp("prop1", "myType", null, "{\"mac_range_plan\":\"\",  \"mac_count_required\":{\"is_required\":false, \"mac_address\":\"myAddress\"}}");
        
        DataTypeDefinition myType = new DataTypeDefinition();
        myType.setName("myType");
      
        PropertyDefinition mac_range_plan = new PropertyDefinition();
        mac_range_plan.setName("mac_range_plan");
        mac_range_plan.setType("string");

        PropertyDefinition mac_count_required = new PropertyDefinition();
        mac_count_required.setName("mac_count_required");
        mac_count_required.setType("map");
        
        myType.setProperties(Arrays.asList(mac_range_plan, mac_count_required));
        Map<String, DataTypeDefinition> dataTypes = Collections.singletonMap(myType.getName(), myType);

        when(applicationDataTypeCache.getAll()).thenReturn(Either.left(dataTypes));
        
        testInstance.mergePropertyValue(oldProp, newProp, Collections.emptyList());
        
        assertEquals("myType", "{\"mac_range_plan\":\"y\",\"mac_count_required\":{\"is_required\":false,\"mac_address\":\"myAddress\"}}", newProp.getValue());
    }
    
    
    
    
    /**                                                                                    Expected property:
     * Old property:                            New property:                                {
     *   {                                        {                                            "mac_range_plan": "n",
     *     "mac_range_plan": "y",   "               "mac_range_plan": "n",                     "mymap": {
     * 	   "mymap": {                           	"mymap": {                             	       "mac_count_required": {      
     * 		  "mac_count_required": {           		"mac_count_required": {            		      "is_required": false,
     * 		    "is_required": true,            		  "is_required": false                        "count": 44		  
     * 		    "count": 44                    		    },                                 		   },
     * 		  },                                		"host":"localhost",                		   "host":"localhost",
     * 		  "backup-mode":"daily",            		"ip":"127.0.0.1"                   		   "ip":"127.0.0.1"
     * 		  "ip":"0.0.0.0"                    	}                                      	   }                            
     * 	   }                                      }                                          }
     *   }                                                                                      
     *                                                                                       
     */
    @Test
    public void mergeComplexType_containingMapWithComplexType() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", "myType", null, "{\"mac_range_plan\":\"y\",\"mymap\": {\"mac_count_required\": {\"is_required\":true,\"count\":44},\"backup-mode\":\"daily\",\"ip\":\"0.0.0.0\"}}");
        PropertyDataDefinition newProp = createProp("prop1", "myType", null, "{\"mac_range_plan\":\"n\",\"mymap\": {\"mac_count_required\": {\"is_required\":false},\"host\":\"localhost\",\"ip\":\"127.0.0.1\"}}");
        
        DataTypeDefinition myType = new DataTypeDefinition();
        myType.setName("myType");
      
        PropertyDefinition mac_range_plan = new PropertyDefinition();
        mac_range_plan.setName("mac_range_plan");
        mac_range_plan.setType("string");
        
        PropertyDefinition mymap = new PropertyDefinition();
        mymap.setName("mymap");
        mymap.setType("map");

        PropertyDefinition mac_count_required = new PropertyDefinition();
        mac_count_required.setName("mac_count_required");
        mac_count_required.setType("MacType");
        
        SchemaDefinition entrySchema = new SchemaDefinition();
        entrySchema.setProperty(mac_count_required);
        mymap.setSchema(entrySchema);
        
        myType.setProperties(Arrays.asList(mac_range_plan, mymap, mac_count_required));
        Map<String, DataTypeDefinition> dataTypes = Collections.singletonMap(myType.getName(), myType);

        when(applicationDataTypeCache.getAll()).thenReturn(Either.left(dataTypes));
        
        testInstance.mergePropertyValue(oldProp, newProp, Collections.emptyList());
        
        assertEquals("myType", "{\"mac_range_plan\":\"n\",\"mymap\":{\"ip\":\"127.0.0.1\",\"mac_count_required\":{\"is_required\":false,\"count\":44},\"host\":\"localhost\"}}", newProp.getValue());
    }


    /*                                                               
     *  Old Property:                      New Property:                          Expected:          
     *  [                                  [                                      [                      
     *    {                                   {                                      {                   
     *      "prop1": "val1",                    "prop2": {                             "prop2": {        
     *      "prop2": {                            "prop3": false                         "prop3": false  
     *        "prop3": true,                    }                                      }                 
     *        "prop4": 44                     }                                      }                   
     *      }                              ]                                      ]                      
     *    },                                                         
     *    {
     *      "prop1": "val2",
     *      "prop2": {
     *        "prop3": true
     *      }
     *    }
     *  ]
     *  
     */
    @Test
    public void mergeListOfComplexType_differentSize() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", "list", "myType", "[{\"prop1\":\"val1\", \"prop2\":{\"prop3\":true,\"prop4\":44}}, " +
                                                                                       "{\"prop1\":\"val2\", \"prop2\":{\"prop3\":true}}]");
        PropertyDataDefinition newProp = createProp("prop1", "list", "myType", "[{\"prop2\":{\"prop3\":false}}]");

        Map<String, DataTypeDefinition> dataTypes = buildDataTypes();
        when(applicationDataTypeCache.getAll()).thenReturn(Either.left(dataTypes));
        testInstance.mergePropertyValue(oldProp, newProp, Collections.emptyList());
        assertEquals("myType", "[{\"prop2\":{\"prop3\":false}}]", newProp.getValue());
    }
    
    
    /*
     *  Old Property:                     New Property:                 Expected:
     *  [                                 [                             [
     *    {                                 {                            {
     *      "prop1": "val1",                  "prop1": "",                 "prop1": "val1",
     *      "prop2": {                        "prop2": {                   "prop2": {
     *        "prop3": true,                    "prop4": 45                  "prop3": true
     *        "prop4": 44                     }                              "prop4": 45
     *      }                               },                             }                       
     *    },                                {                            },                        
     *    {                                                              {                         
     *      "prop1": "val2",                  "prop2": {                   "prop1": "val2",                        
     *      "prop2": {                          "prop3": false             "prop2": {              
     *        "prop3": true                   }                              "prop3": false        
     *      }                               }                              }                       
     *    }                               ]                              }                         
     *  ]                                                              ]                           
     *                                                                                             
     */                                                                                            
    @Test
    public void mergeListOfComplexType() throws Exception {
        PropertyDataDefinition oldProp = createProp("lprop", "list", "myType", "[{\"prop1\":\"val1\", \"prop2\":{\"prop3\":true,\"prop4\":44}}, " +
                                                                                "{\"prop1\":\"val2\", \"prop2\":{\"prop3\":true}}]");
        PropertyDataDefinition newProp = createProp("lprop", "list", "myType", "[{\"prop1\":\"\", \"prop2\":{\"prop4\":45}}, {\"prop2\":{\"prop3\":false}}]");

        
        DataTypeDefinition myType = new DataTypeDefinition();
        myType.setName("myType");
        
        Map<String, DataTypeDefinition> dataTypes = buildDataTypes();
        when(applicationDataTypeCache.getAll()).thenReturn(Either.left(dataTypes));
        testInstance.mergePropertyValue(oldProp, newProp, Collections.emptyList());
        assertEquals("lprop",  "[{\"prop2\":{\"prop4\":45,\"prop3\":true},\"prop1\":\"val1\"},{\"prop2\":{\"prop3\":false},\"prop1\":\"val2\"}]", newProp.getValue());
    }

    @Test
    public void mergeListOfMapsWithJsonAsInnerType() throws Exception {
        PropertyDataDefinition oldProp = createProp("value_spec", "list", "json", "[{\"prop1\":\"val1\", \"prop2\":\"prop3\",\"prop4\":44}]");
        PropertyDataDefinition newProp = createProp("value_spec", "list", "json", "[{\"prop22\":{\"prop221\":45,\"prop222\":\"val222\",\"prop223\":\"false\"}}]");

        Map<String, DataTypeDefinition> dataTypes = buildDataTypes();
        when(applicationDataTypeCache.getAll()).thenReturn(Either.left(dataTypes));
        testInstance.mergePropertyValue(oldProp, newProp, Collections.emptyList());
        assertEquals("value_spec", "[{\"prop22\":{\"prop223\":\"false\",\"prop221\":45,\"prop222\":\"val222\"}}]", newProp.getValue());
    }



    /*
     * Old Property:                          New Property:                               Expected:                          
     * {                                      {                                           {
     *   "lprop": [                             "lprop": [                                  "lprop": [
     *     {                                      {                                           {
     *       "prop1": "val1",                       "prop2": [                                  "prop1": "val1",
     *       "prop2": [                               {                                         "prop2": [
     *         {                                        "prop3": true                             {
     *           "prop3": true,                       },                                            "prop3": true,
     *           "prop4": 44                  		  {                                             "prop4": 44
     *         },                             	 	    "prop4":69                                },
     *         {                              		  }                                   		  {
     *           "prop3": false,                    ]                                               "prop3": false,
     *           "prop4": 96                      },                                                "prop4": 69
     *         }                                  {                                               }
     *       ]                                      "prop1": "val1",                            ]
     *     },                                       "prop2": [                                },
     *     {                                          {                                       {
     *       "prop1": "val2",                           "prop3": false                          "prop1": "val1",
     *       "prop2": [                               }                                         "prop2": [
     *         {                                    ]                                             {
     *           "prop3": true                    }                                                 "prop3": false
     *         }                                ],                                                }
     *       ]                                  "prop5": "value05"                              ]
     *     }                                  }                                               }
     *   ],                                                                                 ],
     *   "prop5": "value5"                                                                  "prop5": "value05"
     * }                                                                                  }   
     *
     *
     */                                                                                            
    @Test
    public void mergeComplexType_containsListOfComplexType() throws Exception {
        PropertyDataDefinition oldProp = createProp("complexProp", "complexType", null, 
                "{\"lprop\":[{\"prop1\":\"val1\",\"prop2\":[{\"prop3\":true,\"prop4\":44},{\"prop3\":false,\"prop4\":96}]}," + 
                "{\"prop1\":\"val2\",\"prop2\":[{\"prop3\":true}]}],\"prop5\":\"value5\"} ");
        PropertyDataDefinition newProp = createProp("complexProp", "complexType", null,
                "{\"lprop\":[{\"prop2\":[{\"prop3\":true},{\"prop4\":69}]},{\"prop1\":\"val1\",\"prop2\":[{\"prop3\":false}]}],\"prop5\":\"value05\"}");

        DataTypeDefinition complexType = new DataTypeDefinition();
        complexType.setName("complexType");

        PropertyDefinition lprop = new PropertyDefinition(createProp("lprop", "list", "myType", null));

        PropertyDefinition prop5 = new PropertyDefinition();
        prop5.setName("prop5");
        prop5.setType("string");
        
        DataTypeDefinition complexProp = new DataTypeDefinition();
        complexProp.setName("complexType");
        complexType.setProperties(Arrays.asList(lprop, prop5));
        
        DataTypeDefinition myType = new DataTypeDefinition();
        myType.setName("myType");

        PropertyDefinition prop1 = new PropertyDefinition();
        prop1.setName("prop1");
        prop1.setType("string");
        
        DataTypeDefinition myInnerType = new DataTypeDefinition();
        myInnerType.setName("myInnerType");

        PropertyDefinition prop2 = new PropertyDefinition(createProp("prop2", "list", "myInnerType", null));

        PropertyDefinition prop3 = new PropertyDefinition();
        prop3.setName("prop3");
        prop3.setType("boolean");

        PropertyDefinition prop4 = new PropertyDefinition();
        prop4.setName("prop4");
        prop4.setType("integer");

        complexType.setProperties(Arrays.asList(lprop, prop5));
        myType.setProperties(Arrays.asList(prop1, prop2));
        myInnerType.setProperties(Arrays.asList(prop3, prop4));
        
        Map<String, DataTypeDefinition> dataTypes = Stream.of(complexType, myType, myInnerType)
                                                           .collect(Collectors.toMap(DataTypeDefinition::getName, Function.identity()));
        
        when(applicationDataTypeCache.getAll()).thenReturn(Either.left(dataTypes));
        
        testInstance.mergePropertyValue(oldProp, newProp, Collections.emptyList());
        
        assertEquals("complexProp", 
                "{\"lprop\":[{\"prop2\":[{\"prop4\":44,\"prop3\":true},{\"prop4\":69,\"prop3\":false}],\"prop1\":\"val1\"},{\"prop2\":[{\"prop3\":false}],\"prop1\":\"val1\"}],\"prop5\":\"value05\"}",
                newProp.getValue());
    }
    
    
    @Test
    public void mergeMapType_differentSize() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", "map", "string", "{\"prop1\":\"val1\", \"prop2\":\"val2\", \"prop3\":\"val3\"}");
        PropertyDataDefinition newProp = createProp("prop1", "map", "string", "{\"prop1\":\"valY\", \"prop2\":\"\"}");
        
        HashMap<String, String> expected = Maps.newHashMap();
        expected.put("prop1", "valY");
        expected.put("prop2", "val2");
        verifyMapMerge(getMergedMapProp(oldProp, newProp, Collections.emptyList()), expected);
    }
    
    
    @Test
    public void mergeMapType() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", "map", "string", "{\"prop1\":\"val1\", \"prop2\":\"val2\", \"prop3\":\"\", \"prop4\":\"val4\"}");
        PropertyDataDefinition newProp = createProp("prop1", "map", "string", "{\"prop1\":\"valY\", \"prop2\":\"\", \"prop3\":\"val3\", \"prop5\":\"val5\"}");
        
        
        HashMap<String, String> expected = Maps.newHashMap();
        expected.put("prop1", "valY");
        expected.put("prop2", "val2");
        expected.put("prop3", "val3");
        expected.put("prop5", "val5");
        verifyMapMerge(getMergedMapProp(oldProp, newProp, Collections.emptyList()), expected);
    }

    @Test
    public void mergeMapTypeWhenNewValueIsEmpty() throws Exception {
        PropertyDataDefinition oldProp = createProp("prop1", "map", "string", "{\"prop1\":\"val1\", \"prop2\":\"val2\", \"prop3\":\"val3\"}");
        PropertyDataDefinition newProp = createProp("prop1", "map", "string", null);
        HashMap<String, String> expected = Maps.newHashMap();
        expected.put("prop1", "val1");
        expected.put("prop2", "val2");
        expected.put("prop3", "val3");
        verifyMapMerge(getMergedMapProp(oldProp, newProp, Collections.singletonList("input1")), expected);
    }

    @Test
    public void mergeGetInputValue() throws Exception {
        PropertyDataDefinition oldProp = createGetInputProp("prop1", "string", null, "input1");
        PropertyDataDefinition newProp = createProp("prop1", "string", null, "");
        testMergeProps(oldProp, newProp, oldProp.getValue(), Collections.singletonList("input1"));
        assertEquals(oldProp.getGetInputValues(), newProp.getGetInputValues());
    }

    @Test
    public void mergeGetInputValue_valueIsNull_InNewProp() throws Exception {
        PropertyDataDefinition oldProp = createGetInputProp("prop1", "string", null, "input1");
        PropertyDataDefinition newProp = createProp("prop1", "string", null, null);
        testMergeProps(oldProp, newProp,"{\"get_input\":\"input1\"}", Collections.singletonList("input1"));
        assertGetInputValues(newProp, "input1");
    }

    /*
     * Old property:                      New property:                       Expected:              
     * [                                  [                                   [                          
     *   {                                  {                                   {                         
     *     "mac_range_plan": {                "mac_count_required": {           "mac_range_plan": {     
     *       "get_input": "input1"              "is_required": true               "get_input": "input1" 
     *     },                                 }                                 },                      
     *     "mac_count_required": {          }                                   "mac_count_required": {                                    
     *       "is_required": true,                                                 "is_required": true         
     *       "count": {                   inputs: intput1, input2               }                             
     *         "get_input": "input2"      ]                                   }                               
     *       }                                                                                                
     *     }                                                                  inputs: input2                    
     *   }                                                                  ]                                 
     *                                                                        
     *   inputs: intput1, input2                                                                            
     * ]                                                                                                    
     * 
     * 
     * 
     */
    @Test
    public void mergeComplexGetInputValue() throws Exception {
        PropertyDataDefinition oldProp = new PropertyDataDefinitionBuilder().addGetInputValue("input1").addGetInputValue("input2").setName("prop1").setType("myType").setValue("{\"mac_range_plan\":{\"get_input\": \"input1\"}, \"mac_count_required\":{\"is_required\":true,\"count\":{\"get_input\": \"input2\"}}}").build();
        PropertyDataDefinition newProp = new PropertyDataDefinitionBuilder().addGetInputValue("input2").setName("prop1").setType("myType").setValue("{\"mac_count_required\":{\"is_required\":true}}").build();
        testMergeProps(oldProp, newProp,"{\"mac_range_plan\":{},\"mac_count_required\":{\"is_required\":true}}", Collections.singletonList("input2"));
        assertGetInputValues(newProp, "input2");
    }

    @Test
    public void mergeListValueWithMultipleGetInputs() throws Exception {
        PropertyDataDefinition oldProp = new PropertyDataDefinitionBuilder()
                .addGetInputValue("input1").addGetInputValue("input2").addGetInputValue("input3")
                .setName("prop1")
                .setType("list").setSchemaType("string")
                .setValue("[{\"get_input\": \"input2\"},{\"get_input\": \"input3\"},{\"get_input\": \"input1\"}]")
                .build();

        PropertyDataDefinition newProp = new PropertyDataDefinitionBuilder()
                .addGetInputValue("input3")
                .addGetInputValue("input5")
                .setName("prop1")
                .setType("list").setSchemaType("string")
                .setValue("[{\"get_input\": \"input5\"}, {\"get_input\": \"input3\"}]")
                .build();

        testMergeProps(oldProp, newProp,"[{\"get_input\":\"input5\"},{\"get_input\":\"input3\"}]");
        assertGetInputValues(newProp, "input3", "input5");
    }

    private void assertGetInputValues(PropertyDataDefinition newProp, String ... expectedInputNames) {
        assertTrue(newProp.isGetInputProperty());
        assertEquals(newProp.getGetInputValues().size(), expectedInputNames.length);
        for (int i = 0; i < expectedInputNames.length; i++) {
            String expectedInputName = expectedInputNames[i];
            GetInputValueDataDefinition getInputValueDataDefinition = newProp.getGetInputValues().get(i);
            assertEquals(getInputValueDataDefinition.getInputName(), expectedInputName);
        }
    }
    
    private void testMergeProps(PropertyDataDefinition oldProp, PropertyDataDefinition newProp, String expectedValue) {
        testMergeProps(oldProp, newProp, expectedValue, Collections.emptyList());
    }

    private void testMergeProps(PropertyDataDefinition oldProp, PropertyDataDefinition newProp, String expectedValue,  List<String> getInputsToMerge) {
        when(applicationDataTypeCache.getAll()).thenReturn(Either.left(Collections.emptyMap()));
        testInstance.mergePropertyValue(oldProp, newProp, getInputsToMerge);
        assertEquals(expectedValue, newProp.getValue());
    }


    private String getMergedMapProp(PropertyDataDefinition oldProp, PropertyDataDefinition newProp, List<String> getInputsToMerge) {
        when(applicationDataTypeCache.getAll()).thenReturn(Either.left(Collections.emptyMap()));
        testInstance.mergePropertyValue(oldProp, newProp, getInputsToMerge);
        return newProp.getValue();
    }

    private void verifyMapMerge(String newValue, Map<String, String> expectedValues) {
        Map<String, String> values = convertJsonToMap(newValue);
        assertThat(values).isNotNull();
        assertThat(values).containsAllEntriesOf(expectedValues);
    }

    private PropertyDataDefinition createProp(String name, String type, String innerType, String val) {
        return new PropertyDataDefinitionBuilder()
                .setType(type)
                .setSchemaType(innerType)
                .setValue(val)
                .setName(name)
                .build();
    }

    private PropertyDataDefinition createGetInputProp(String name, String type, String innerType, String inputName) {
        String val = String.format("{\"get_input\":\"%s\"}", inputName);
        return new PropertyDataDefinitionBuilder()
                .setType(type)
                .setSchemaType(innerType)
                .setValue(val)
                .addGetInputValue(inputName)
                .setName(name)
                .build();

    }

    private Map<String, String> convertJsonToMap(String jsonString) {
        try {
            return mapper.readValue(jsonString, new TypeReference<Map<String, String>>(){});
        } catch (IOException e) {
            return null;
        }
    }

    private Map<String, DataTypeDefinition> buildDataTypes() {
        DataTypeDefinition myType = new DataTypeDefinition();
        myType.setName("myType");
        DataTypeDefinition myInnerType = new DataTypeDefinition();
        myInnerType.setName("myInnerType");

        PropertyDefinition prop1 = new PropertyDefinition();
        prop1.setName("prop1");
        prop1.setType("string");

        PropertyDefinition prop2 = new PropertyDefinition();
        prop2.setName("prop2");
        prop2.setType("myInnerType");

        PropertyDefinition prop3 = new PropertyDefinition();
        prop3.setName("prop3");

        PropertyDefinition prop4 = new PropertyDefinition();
        prop4.setName("prop4");

        myType.setProperties(Arrays.asList(prop1, prop2));
        myInnerType.setProperties(Arrays.asList(prop3, prop4));

        return Stream.of(myType, myInnerType).collect(Collectors.toMap(DataTypeDefinition::getName, Function.identity()));
    }


}
