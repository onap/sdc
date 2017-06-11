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

package org.openecomp.sdc.ci.tests.execute.property;

import static org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils.STATUS_CODE_INVALID_CONTENT;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.PropertyReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.PropertyTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ComponentProperty extends ComponentBaseTest {

	@Rule
	public static TestName name = new TestName();

	public ComponentProperty() {
		super(name, ComponentProperty.class.getName());
	}

	@DataProvider
	private static final Object[][] propertiesListDefaultValueSuccessFlow() throws IOException, Exception {
		return new Object[][] {
				// integer
				{ "integer", "[1,2]", "[1,2]" },
				{ "tosca.datatypes.Credential",
						"[{\"protocol\":\"protocol1\",\"token\":\"token1\"},{\"protocol\":\"protocol2\",\"token\":\"token2\"}]",
						"[{\"protocol\":\"protocol1\",\"token\":\"token1\"},{\"protocol\":\"protocol2\",\"token\":\"token2\"}]" },
				{ "tosca.datatypes.Credential",
						"[{\"protocol\":\"protocol1\",\"token\":\"token1\"},{\"protocol\":\"protocol<br>2\",\"token\":\"token2  2\"}]",
						"[{\"protocol\":\"protocol1\",\"token\":\"token1\"},{\"protocol\":\"protocol2\",\"token\":\"token2 2\"}]" },
				{ "tosca.datatypes.Credential", null, null }, { "tosca.datatypes.Credential", "[]", "[]" },
				{ "integer", "[1,2,1,2]", "[1,2,1,2]" }, { "integer", "[1,,2]", "[1,2]" },
				{ "integer", "[1,null,2]", "[1,2]" }, { "integer", "[1,2,null]", "[1,2]" },
				{ "integer", "[null,1,2]", "[1,2]" }, { "integer", "[1,,2]", "[1,2]" },
				{ "integer", "[,1,2]", "[1,2]" },
				// {"integer",
				// "[1000000000000000000000000000000000000000000000000000,2]" ,
				// "[1000000000000000000000000000000000000000000000000000,2]"},
				{ "integer", "[100000000,2]", "[100000000,2]" }, // Andrey, in
																	// success
																	// flow
																	// integer
																	// max value
																	// is
																	// 2147483647
				{ "integer", null, null }, // no default value
				{ "integer",
						"[1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2]",
						"[1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2]" },
				// boolean
				{ "boolean", "[true,false]", "[true,false]" },
				{ "boolean", "[true,false,false]", "[true,false,false]" },
				{ "boolean", "[null,true,false]", "[true,false]" }, { "boolean", "[true,false,null]", "[true,false]" },
				{ "boolean", "[true,,false]", "[true,false]" }, { "boolean", "[true,false,]", "[true,false]" },
				{ "boolean", "[,true,false]", "[true,false]" }, { "boolean", null, null },
				// DE199713 - Default value for property type Boolean should
				// support also the following values: "true", "t" , "on" , "1" ,
				// "false", "f" , "off" , "0"
				{ "boolean", "[on,off]", "[true,false]" }, { "boolean", "[ON,OFF]", "[true,false]" },
				{ "boolean", "[On,Off]", "[true,false]" }, { "boolean", "[yes,no]", "[true,false]" },
				{ "boolean", "[YES,NO]", "[true,false]" }, { "boolean", "[Yes,No]", "[true,false]" },
				{ "boolean", "[y,n]", "[true,false]" }, { "boolean", "[Y,N]", "[true,false]" },
				// float
				{ "float", "[10.0,0.0]", "[10.0,0.0]" }, { "float", "[10,0]", "[10,0]" }, // contain
																							// integer
				{ "float", "[-10,-5.30]", "[-10,-5.30]" }, // Negative numbers
				{ "float", "[10,null,0]", "[10,0]" }, { "float", "[null,10,0]", "[10,0]" },
				{ "float", "[10,0,null]", "[10,0]" },
				{ "float", "[10,0.1111111111111111111111111111111111111111]",
						"[10,0.1111111111111111111111111111111111111111]" },
				{ "float", "[10,   ,7.3  ]", "[10,7.3]" }, { "float", "[10 , 7.3 , ]", "[10,7.3]" },
				{ "float", "[, , 10 , 7.3 , ]", "[10,7.3]" }, { "float", "[4.7f, -5.5f ]", "[4.7,-5.5]" },
				{ "float", "[4.7f, 6.3 ,6.3, 4.7f]", "[4.7,6.3,6.3,4.7]" }, // duplicate
																			// value
				{ "float", null, null }, { "string", "[aaaa , AAAA  ]", "[\"aaaa\",\"AAAA\"]" },

				{ "string",
						"[1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2,1,2]",
						"[\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\",\"1\",\"2\"]" },
				{ "string", "[aaaa , AAAA, 1, off , true, false  ]",
						"[\"aaaa\",\"AAAA\",\"1\",\"off\",\"true\",\"false\"]" },
				{ "string", "[aaaa , AAAA, aaaa, Aaaa , aaaa  ]", "[\"aaaa\",\"AAAA\",\"aaaa\",\"Aaaa\",\"aaaa\"]" },
				{ "string", "[aaaa , AAAA, ,  ]", "[\"aaaa\",\"AAAA\"]" },
				{ "string", "[ , aaaa , AAAA ]", "[\"aaaa\",\"AAAA\"]" },
				{ "string", "[ aaaa , ,  AAAA ]", "[\"aaaa\",\"AAAA\"]" },
				{ "string", "[ aaaa ,  AAAA, null ]", "[\"aaaa\",\"AAAA\"]" },
				{ "string", "[ null,  aaaa ,  AAAA ]", "[\"aaaa\",\"AAAA\"]" },
				{ "string", "[  aaaa , null ,  AAAA ]", "[\"aaaa\",\"AAAA\"]" }, { "string", null, null }, // without
																											// default
																											// values
																											// -
																											// Property
																											// will
																											// be
																											// without
																											// default
																											// parameter
				{ "string", "[ <b>AAA</b> ]", "[\"AAA\"]" }, // BUG DE199715 -
																// Error 400
																// response
																// received
																// while adding
																// property with
																// default value
																// contain HTML
																// tags.
																// Need to check
																// whether / is
																// legal in yaml

		};
	}

	@DataProvider
	private static final Object[][] invalidListProperties() throws IOException, Exception {
		return new Object[][] {

				{ "integer", "[1,aaa]" },
				{ "tosca.datatypes.Credential",
						"[{\"protocol\":\"protocol1\",\"token\":\"token1\"},{\"protocol\":\"protocol2\",\"token1\":\"token2\"}]" },
				{ "integer", "[1,false]" }, { "integer", "[1,3.5]" }, { "integer", "[1,3#]" },
				{ "boolean", "[true,3.5]" }, { "boolean", "[true,1000]" }, { "boolean", "[false,trueee]" },
				{ "boolean", "[true,false!]" }, { "float", "[5.0000001,true]" }, { "float", "[0.0001,koko]" },
				{ "float", "[0.0001,6.3@]" }, { "float", "[0.0001f,6.3x]" }, };
	}

	@DataProvider
	private static final Object[][] updatePropertiesListDefaultValueSuccessFlow() throws IOException, Exception {
		return new Object[][] {
				// integer
				// Setting --- update properties
				// -----------------------------------------------------------------------
				{ "integer", "[1,2]", "[1,2]", "integer", "[200,100]", "[200,100]" },
				{ "integer", "[1,2]", "[1,2]", "integer", "[200,100,null]", "[200,100]" },
				{ "integer", "[1,2]", "[1,2]", "integer", "[null, 200,100]", "[200,100]" },
				{ "integer", "[1,2]", "[1,2]", "integer", "[200,null,100]", "[200,100]" },
				{ "integer", "[1,2]", "[1,2]", "integer", "[200,100,    ]", "[200,100]" },
				{ "integer", "[1,2]", "[1,2]", "integer", "[  , 200,100 ]", "[200,100]" },
				{ "integer", "[1,2]", "[1,2]", "integer", "[200 ,  ,100 ]", "[200,100]" },
				{ "integer", "[1,2]", "[1,2]", "integer", null, null },
				{ "integer", "[1,2]", "[1,2]", "integer", "[200 , 100 , 200, 100]", "[200,100,200,100]" },
				//
				// ////DE199829 update resource property schema_type is not
				// updated
				{ "integer", "[1,2]", "[1,2]", "string", "[aaaa , bbbb ]", "[\"aaaa\",\"bbbb\"]" },
				{ "integer", "[1,2]", "[1,2]", "boolean", "[true , false ]", "[true,false]" },
				{ "integer", "[1,2]", "[1,2]", "float", "[3.5,4.8f ]", "[3.5,4.8]" },
				// {"string", "[aaa,bbb]" , "[\"aaa\",\"bbb\"]","integer","[100,
				// 200]" , "[\"100\",\"200\"]"},
				{ "string", "[aaa,bbb]", "[\"aaa\",\"bbb\"]", "integer", "[100, 200]", "[100,200]" },
				{ "string", "[aaa,bbb]", "[\"aaa\",\"bbb\"]", "float", "[0.1f, 3.01]", "[0.1,3.01]" },
				{ "string", "[aaa,bbb]", "[\"aaa\",\"bbb\"]", "boolean", "[true, false]", "[true,false]" },
				{ "float", "[1.2,2.3]", "[1.2,2.3]", "boolean", "[true, false]", "[true,false]" },
				{ "float", "[1.2,2.3]", "[1.2,2.3]", "integer", "[100, 200]", "[100,200]" },
				{ "float", "[1.2,2.3]", "[1.2,2.3]", "string", "[koko, moko]", "[\"koko\",\"moko\"]" },
				{ "boolean", "[true,false]", "[true,false]", "string", "[koko, moko]", "[\"koko\",\"moko\"]" },
				// {"boolean", "[true,false]" ,
				// "[\"true\",\"false\"]","integer","[100, 300000000000000]" ,
				// "[\"100\",\"300000000000000\"]"},// Andrey, value not valid
				// for integer success flow
				{ "boolean", "[true,false]", "[true,false]", "integer", "[100,2147483647]", "[100,2147483647]" }, // Andrey,
																													// in
																													// success
																													// flow
																													// integer
																													// max
																													// value
																													// is
																													// 2147483647
				{ "boolean", "[true,false]", "[true,false]", "float", "[3.000000000000002, 5.67f]",
						"[3.000000000000002,5.67]" },
				// ////DE199829
				//
				{ "string", "[aaa,bbb]", "[\"aaa\",\"bbb\"]", "string", "[xxx, yyy]", "[\"xxx\",\"yyy\"]" },
				{ "string", "[aaa,bbb]", "[\"aaa\",\"bbb\"]", "string", "[xxx , yyy ,null]", "[\"xxx\",\"yyy\"]" },
				{ "string", "[aaa,bbb]", "[\"aaa\",\"bbb\"]", "string", "[null, xxx, yyy]", "[\"xxx\",\"yyy\"]" },
				{ "string", "[aaa,bbb]", "[\"aaa\",\"bbb\"]", "string", "[xxx ,null,yyy]", "[\"xxx\",\"yyy\"]" },
				{ "string", "[aaa,bbb]", "[\"aaa\",\"bbb\"]", "string", "[xxx ,yyy,    ]", "[\"xxx\",\"yyy\"]" },
				{ "string", "[aaa,bbb]", "[\"aaa\",\"bbb\"]", "string", "[  , xxx,yyy ]", "[\"xxx\",\"yyy\"]" },
				{ "string", "[aaa,bbb]", "[\"aaa\",\"bbb\"]", "string", "[xxx ,  ,yyy ]", "[\"xxx\",\"yyy\"]" },
				{ "string", "[aaa,bbb]", "[\"aaa\",\"bbb\"]", "string", "[ xxx , yyy , xxx , yyy]",
						"[\"xxx\",\"yyy\",\"xxx\",\"yyy\"]" },
				{ "string", "[aaa,bbb]", "[\"aaa\",\"bbb\"]", "string", null, null },
				{ "string", "[aaa,bbb]", "[\"aaa\",\"bbb\"]", "string", "[xxx_-x, y__y--y]",
						"[\"xxx_-x\",\"y__y--y\"]" },
				// DE199715
				// {"string", "[aaa,bbb]" , "[\"aaa\",\"bbb\"]", "string" ,
				// "[\"<b>xxx</b>\", \"<b>yyy</b>\"]" , "[\"xxx\",\"yyy\"]"},
				//
				{ "float", "[1.00,0.02]", "[1.00,0.02]", "float", "[2.1 , -0.1]", "[2.1,-0.1]" },
				{ "float", "[1.00,0.02]", "[1.00,0.02]", "float", "[2.1, 0.1 ,null]", "[2.1,0.1]" },
				{ "float", "[1.00,0.02]", "[1.00,0.02]", "float", "[null , 2.1, 0.1]", "[2.1,0.1]" },
				{ "float", "[1.00,0.02]", "[1.00,0.02]", "float", "[2.1,null,0.1]", "[2.1,0.1]" },
				{ "float", "[1.00,0.02]", "[1.00,0.02]", "float", "[2.1,0.1,    ]", "[2.1,0.1]" },
				// {"float", "[1.00,0.02]" , "[1.00,0.02]","float","[ ,
				// 2.00000000000001,0.00000000000000100 ]" ,
				// "[2.00000000000001,0.00000000000000100]"},
				{ "float", "[1.00,0.02]", "[1.00,0.02]", "float", "[2.1 ,  ,0.1 ]", "[2.1,0.1]" },
				{ "float", "[1.00,0.02]", "[1.00,0.02]", "float", null, null },
				{ "float", "[1.00,0.02]", "[1.00,0.02]", "float", "[2.1f ,  ,0.1f ]", "[2.1,0.1]" },
				{ "float", "[1.00,0.02]", "[1.00,0.02]", "float", "[2.1 , 0.1 , 2.1, 0.1]", "[2.1,0.1,2.1,0.1]" },
				{ "float", "[1.00,0.02]", "[1.00,0.02]", "float", "[200 , 100.11]", "[200,100.11]" },
				{ "float", "[1.00,0.02]", "[1.00,0.02]", "float", "[-2.35 , 100.11]", "[-2.35,100.11]" },
				//
				{ "boolean", "[true,false]", "[true,false]", "boolean", "[false , false]", "[false,false]" },
				{ "boolean", "[true,false]", "[true,false]", "boolean", "[false, true ,null]", "[false,true]" },
				{ "boolean", "[true,false]", "[true,false]", "boolean", "[null , false, true]", "[false,true]" },
				{ "boolean", "[true,false]", "[true,false]", "boolean", "[false,null,true]", "[false,true]" },
				{ "boolean", "[true,false]", "[true,false]", "boolean", "[false ,true ,    ]", "[false,true]" },
				{ "boolean", "[true,false]", "[true,false]", "boolean", "[  , false, true ]", "[false,true]" },
				{ "boolean", "[true,false]", "[true,false]", "boolean", "[false ,  ,true ]", "[false,true]" },
				{ "boolean", "[true,false]", "[true,false]", "boolean", null, null }, { "boolean", "[true,false]",
						"[true,false]", "boolean", "[false , true , false, true]", "[false,true,false,true]" }, };
	}

	@DataProvider
	private static final Object[][] updatePropertiesListDefaultValueFailureFlow() throws IOException, Exception {
		return new Object[][] {
				// integer
				// Setting --- update properties
				// -----------------------------------------------------------------------
				{ "integer", "[1,2]", "[1,2]", "integer", "[aaa,bbb]" },
				{ "integer", "[1,2]", "[1,2]", "integer", "[true,false]" },
				{ "integer", "[1,2]", "[1,2]", "integer", "[1.0,100]" },
				{ "integer", "[1,2]", "[1,2]", "integer", "[@12,100]" },
				{ "float", "[0.11,0.22]", "[0.11,0.22]", "float", "[aaa, bbb]" },
				{ "float", "[0.11,0.22]", "[0.11,0.22]", "float", "[0.88, false]" },
				{ "float", "[0.11,0.22]", "[0.11,0.22]", "float", "[0.88g, 0.3]" },
				{ "float", "[0.11,0.22]", "[0.11,0.22]", "float", "[@0.88, 0.3]" },
				{ "boolean", "[true, false]", "[true,false]", "boolean", "[true, 100]" },
				{ "boolean", "[true, false]", "[true,false]", "boolean", "[false, 0.01]" },
				{ "boolean", "[true, false]", "[true,false]", "boolean", "[koko, true]" },
				{ "boolean", "[true, false]", "[true,false]", "boolean", "[@false, true]" },

		};
	}

	// Map properties
	@DataProvider
	private static final Object[][] updatePropertiesMapDefaultValueSuccessFlow() throws IOException, Exception {
		return new Object[][] {
				// entrySchemaType , propertyDefaultValues ,
				// expectedDefaultValue , newEntrySchemaType ,
				// newPropertyDefaultValue , newExpectedDefaultValue
				// integer
				{ "integer", "{\"key1\":1 , \"key2\":2}", "{\"key1\":1,\"key2\":2}", "integer",
						"{\"key1\":200,\"key2\":null , \"key3\":300}", "{\"key1\":200,\"key2\":null,\"key3\":300}" },
				{ "integer", "{\"key1\":1 , \"key2\":2}", "{\"key1\":1,\"key2\":2}", "integer",
						"{\"key1\":null,\"key2\":200 , \"key3\":100}", "{\"key1\":null,\"key2\":200,\"key3\":100}" },
				// string
				{ "integer", "{\"key1\":1 , \"key2\":2}", "{\"key1\":1,\"key2\":2}", "string",
						"{\"key1\":\"aaaa\" , \"key2\":\"aaaa\"}", "{\"key1\":\"aaaa\",\"key2\":\"aaaa\"}" },
				{ "integer", "{\"key1\":1 , \"key2\":2}", "{\"key1\":1,\"key2\":2}", "boolean",
						"{\"key1\":true , \"key2\":false}", "{\"key1\":true,\"key2\":false}" },
				{ "integer", "{\"key1\":1 , \"key2\":2}", "{\"key1\":1,\"key2\":2}", "float",
						"{\"key1\":3.5 , \"key2\":4.8f}", "{\"key1\":3.5,\"key2\":4.8}" },
				// string
				{ "string", "{\"key1\":aaa , \"key2\":bbb}", "{\"key1\":\"aaa\",\"key2\":\"bbb\"}", "string",
						"{\"key1\":xxx , \"key2\":yyy}", "{\"key1\":\"xxx\",\"key2\":\"yyy\"}" },
				// float
				{ "float", "{\"key1\":1.00 , \"key2\":0.02}", "{\"key1\":1.00,\"key2\":0.02}", "float",
						"{\"key1\":2.1, \"key2\":-0.1}", "{\"key1\":2.1,\"key2\":-0.1}" },
				{ "float", "{\"key1\":1.00 , \"key2\":0.02}", "{\"key1\":1.00,\"key2\":0.02}", "float",
						"{\"key1\":2.1 , \"key2\":0.1 , \"key3\":null}", "{\"key1\":2.1,\"key2\":0.1,\"key3\":null}" },
				// boolean
				{ "boolean", "{\"key1\":true , \"key2\":false}", "{\"key1\":true,\"key2\":false}", "boolean",
						"{\"key1\":false , \"key2\":false}", "{\"key1\":false,\"key2\":false}" },
				{ "boolean", "{\"key1\":true , \"key2\":false}", "{\"key1\":true,\"key2\":false}", "boolean",
						"{\"key1\":false , \"key2\":true , \"key3\":null}",
						"{\"key1\":false,\"key2\":true,\"key3\":null}" },
				// null
				{ "boolean", "{\"key1\":null , \"key2\":false}", "{\"key1\":null,\"key2\":false}", "boolean",
						"{\"key1\":false , \"key2\":true , \"key3\":null}",
						"{\"key1\":false,\"key2\":true,\"key3\":null}" },
				// tosca.datatypes.Credential
				{ "tosca.datatypes.Credential",
						"{\"key1\":{\"protocol\":\"protocol<br>1\",\"token\":\"token1\"},\"key2\":{\"protocol\":\"protocol2\",\"token\":\"token2\"}}",
						"{\"key1\":{\"protocol\":\"protocol1\",\"token\":\"token1\"},\"key2\":{\"protocol\":\"protocol2\",\"token\":\"token2\"}}",
						"tosca.datatypes.Credential",
						"{\"key1\":{\"protocol\":\"protocol<br>1\",\"token\":\"token1\"},\"key2\":{\"protocol\":\"protocol2\",\"token\":\"token2\"}}",
						"{\"key1\":{\"protocol\":\"protocol1\",\"token\":\"token1\"},\"key2\":{\"protocol\":\"protocol2\",\"token\":\"token2\"}}" },

		};
	}

	@DataProvider
	private static final Object[][] propertiesMapDefaultValueSuccessFlow() throws IOException, Exception {
		return new Object[][] {

				// entrySchemaType , propertyDefaultValues ,
				// expectedDefaultValue
				//
				// {"string",
				// "{\"vf_module_id\":{\"get_input\":\"vf_module_id\"},
				// \"vnf_idw\": 2}",
				// "{\"vf_module_id\":{\"get_input\":\"vf_module_id\"},
				// \"vnf_idw\": 2}"},

				// tosca.datatypes.Credential
				{ "tosca.datatypes.Credential",
						"{\"key1\":{\"protocol\":\"protocol<br>1\",\"token\":\"token1\"},\"key2\":{\"protocol\":\"protocol2\",\"token\":\"token2\"}}",
						"{\"key1\":{\"protocol\":\"protocol1\",\"token\":\"token1\"},\"key2\":{\"protocol\":\"protocol2\",\"token\":\"token2\"}}" },
				// integer
				{ "integer", "{\"key1\":1 , \"key2\":2}", "{\"key1\":1,\"key2\":2}" },
				{ "integer", "{\"key1\":1,\"key2\":2,\"key3\":1,\"key4\":2}",
						"{\"key1\":1,\"key2\":2,\"key3\":1,\"key4\":2}" },
				{ "integer", "{\"key1\":1,\"key2\":null,\"key3\":1,\"key4\":2}",
						"{\"key1\":1,\"key2\":null,\"key3\":1,\"key4\":2}" },
				{ "integer", "{\"key1\":null,\"key2\":1,\"key3\":1,\"key4\":2}",
						"{\"key1\":null,\"key2\":1,\"key3\":1,\"key4\":2}" },
				{ "integer", "{\"key1\":1,\"key2\":2,\"key3\":1,\"key4\":null}",
						"{\"key1\":1,\"key2\":2,\"key3\":1,\"key4\":null}" },
				{ "integer", "{\"key1\":1,\"key2\":2,\"key3\":1,\"key4\":NULL}",
						"{\"key1\":1,\"key2\":2,\"key3\":1,\"key4\":null}" },
				{ "integer", "{\"key1\":1,\"key2\":2,\"key3\":1,\"key4\":Null}",
						"{\"key1\":1,\"key2\":2,\"key3\":1,\"key4\":null}" },
				{ "integer", "{\"key1\":1,\"key2\":2,\"key3\":1,\"key4\":nuLL}",
						"{\"key1\":1,\"key2\":2,\"key3\":1,\"key4\":null}" },
				{ "integer", null, null }, // no default value
				// //BUG
				//// {"integer",
				// "{\"key1\":1000000000000000000000000000000000000000000000000000,\"key2\":2}"
				// ,"{\"key1\":1000000000000000000000000000000000000000000000000000,\"key2\":2}"},
				{ "boolean", "{\"key1\":true , \"key2\":false}", "{\"key1\":true,\"key2\":false}" },
				{ "boolean", "{\"key1\":true , \"key2\":false, \"key3\":false }",
						"{\"key1\":true,\"key2\":false,\"key3\":false}" },
				{ "boolean", "{\"key1\":null , \"key2\":true, \"key3\":false }",
						"{\"key1\":null,\"key2\":true,\"key3\":false}" },
				{ "boolean", "{\"key1\":true , \"key2\":Null, \"key3\":false }",
						"{\"key1\":true,\"key2\":null,\"key3\":false}" },
				{ "boolean", "{\"key1\":true , \"key2\":false, \"key3\":nULL }",
						"{\"key1\":true,\"key2\":false,\"key3\":null}" },
				{ "boolean", null, null },
				{ "boolean", "{\"key1\":on , \"key2\":off}", "{\"key1\":true,\"key2\":false}" },
				{ "boolean", "{\"key1\":ON , \"key2\":OFF}", "{\"key1\":true,\"key2\":false}" },
				{ "boolean", "{\"key1\":On , \"key2\":Off}", "{\"key1\":true,\"key2\":false}" },
				{ "boolean", "{\"key1\":yes , \"key2\":no}", "{\"key1\":true,\"key2\":false}" },
				{ "boolean", "{\"key1\":YES , \"key2\":NO}", "{\"key1\":true,\"key2\":false}" },
				{ "boolean", "{\"key1\":Yes , \"key2\":No}", "{\"key1\":true,\"key2\":false}" },
				{ "boolean", "{\"key1\":y , \"key2\":n}", "{\"key1\":true,\"key2\":false}" },
				{ "boolean", "{\"key1\":Y , \"key2\":N}", "{\"key1\":true,\"key2\":false}" },
				{ "boolean", "{null:false}", "{\"null\":false}" },
				// float
				{ "float", "{\"key1\":10.0 , \"key2\":0.0}", "{\"key1\":10.0,\"key2\":0.0}" },
				{ "float", "{\"key1\":10 , \"key2\":0}", "{\"key1\":10,\"key2\":0}" }, // contain
																						// integer
				{ "float", "{\"key1\":null , \"key2\":Null}", "{\"key1\":null,\"key2\":null}" }, // contain
																									// null
				{ "float", "{\"key1\":3.5 , \"key2\":nULL}", "{\"key1\":3.5,\"key2\":null}" },
				// BUG
				{ "float", "{\"key1\":3.5 , \"key2\":0.1111111111111111111111111111111111111111}",
						"{\"key1\":3.5,\"key2\":0.1111111111111111111111111111111111111111}" },
				{ "float", "{\"key1\":4.7f , \"key2\":-5.5f}", "{\"key1\":4.7,\"key2\":-5.5}" },
				{ "float", "{\"key1\":4.7f , \"key2\":-5.5f, \"key3\":-5.5f}",
						"{\"key1\":4.7,\"key2\":-5.5,\"key3\":-5.5}" },
				{ "boolean", null, null },
				{ "string", "{\"key1\":aaaa , \"key2\":AAAA}", "{\"key1\":\"aaaa\",\"key2\":\"AAAA\"}" },
				{ "string", "{\"key1\":off , \"key2\":true , \"key3\":1}",
						"{\"key1\":\"off\",\"key2\":\"true\",\"key3\":\"1\"}" },
				{ "string", "{\"key1\":aaaa , \"key2\":Aaaa , \"key3\":aaaa}",
						"{\"key1\":\"aaaa\",\"key2\":\"Aaaa\",\"key3\":\"aaaa\"}" },
				{ "string", "{\"key1\":aaaa , \"key2\":bbbb , \"key3\":null}",
						"{\"key1\":\"aaaa\",\"key2\":\"bbbb\",\"key3\":null}" },
				{ "string", "{\"key1\":NULL , \"key2\":bbbb , \"key3\":aaaa}",
						"{\"key1\":null,\"key2\":\"bbbb\",\"key3\":\"aaaa\"}" },
				{ "string", "{\"key1\":aaaa , \"key2\":Null , \"key3\":bbbb}",
						"{\"key1\":\"aaaa\",\"key2\":null,\"key3\":\"bbbb\"}" },
				{ "string", null, null }, // without default values - Property
											// will be without default parameter
				{ "string", "{\"key1\":\"<b>AAAA</b>\" }", "{\"key1\":\"AAAA\"}" },

		};
	}

	@DataProvider
	private static final Object[][] updatePropertiesMapDefaultValueFailureFlow() throws IOException, Exception {
		return new Object[][] {

				// integer
				{ "integer", "{\"key1\":1 , \"key2\":2}", "{\"key1\":1,\"key2\":2}", "integer",
						"{\"key1\":aaa , \"key2\":bbb}" },
				{ "integer", "{\"key1\":1 , \"key2\":2}", "{\"key1\":1,\"key2\":2}", "integer",
						"{\"key1\":true , \"key2\":false}" },
				{ "integer", "{\"key1\":1 , \"key2\":2}", "{\"key1\":1,\"key2\":2}", "integer",
						"{\"key1\":1.0 , \"key2\":100}" },
				{ "integer", "{\"key1\":1 , \"key2\":2}", "{\"key1\":1,\"key2\":2}", "integer",
						"{\"key1\":12@ , \"key2\":100}" },
				// float
				{ "float", "{\"key1\":0.11 , \"key2\":0.22}", "{\"key1\":0.11,\"key2\":0.22}", "float",
						"{\"key1\":aaa , \"key2\":bbb}" },
				{ "float", "{\"key1\":0.11 , \"key2\":0.22}", "{\"key1\":0.11,\"key2\":0.22}", "float",
						"{\"key1\":0.88 , \"key2\":false}" },
				{ "float", "{\"key1\":0.11 , \"key2\":0.22}", "{\"key1\":0.11,\"key2\":0.22}", "float",
						"{\"key1\":0.88g , \"key2\":0.3}" },
				{ "float", "{\"key1\":0.11 , \"key2\":0.22}", "{\"key1\":0.11,\"key2\":0.22}", "float",
						"{\"key1\":@0.88g , \"key2\":0.3}" },
				// boolean
				{ "boolean", "{\"key1\":true , \"key2\":false}", "{\"key1\":true,\"key2\":false}", "boolean",
						"{\"key1\":true , \"key2\":100}" },
				{ "boolean", "{\"key1\":true , \"key2\":false}", "{\"key1\":true,\"key2\":false}", "boolean",
						"{\"key1\":false , \"key2\":0.01}" },
				{ "boolean", "{\"key1\":true , \"key2\":false}", "{\"key1\":true,\"key2\":false}", "boolean",
						"{\"key1\":koko , \"key2\":true}" },
				{ "boolean", "{\"key1\":true , \"key2\":false}", "{\"key1\":true,\"key2\":false}", "boolean",
						"{\"key1\":@false , \"key2\":true}" },
				{ "boolean", "{\"key1\":true,\"key2\":false}", "{\"key1\":true,\"key2\":false}", "boolean",
						"{:false , \"key2\":true}" },
				{ "boolean", "{\"key1\":true,\"key2\":false}", "{\"key1\":true,\"key2\":false}", "boolean",
						"{\"key1\":true , , \"key2\":false}" },
				// tosca.datatypes.Credential
				{ "tosca.datatypes.Credential",
						"{\"key1\":{\"protocol\":\"protocol<br>1\",\"token\":\"token1\"},\"key2\":{\"protocol\":\"protocol2\",\"token\":\"token2\"}}",
						"{\"key1\":{\"protocol\":\"protocol1\",\"token\":\"token1\"},\"key2\":{\"protocol\":\"protocol2\",\"token\":\"token2\"}}",
						"tosca.datatypes.Credential",
						"{\"key1\":{\"protocol\":\"protocol<br>1\",\"token\":\"token1\"},\"key2\":{\"protocol\":\"protocol2\",\"token2\":\"token2\"}}" },

		};
	}

	// US594938 - UPDATE PROPERTY
	// DE199718
	@Test(dataProvider = "updatePropertiesListDefaultValueFailureFlow")
	public void updateDefaultValueOfResourcePropertyListFailureFlow(String entrySchemaType, String propertyDefaltValues,
			String expecteddefaultValues, String newEntrySchemaType, String newPropertyDefaltValues) throws Exception {
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		propertyDetails.setPropertyDefaultValue(propertyDefaltValues);
		propertyDetails.getSchema().getProperty().setType(entrySchemaType);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		ComponentInstanceProperty resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, true).left().value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		// verify properties return from response
		assertEquals("list", resourcePropertiesFromResponse.getType());
		assertEquals(expecteddefaultValues, resourcePropertiesFromResponse.getDefaultValue());
		assertEquals(propertyDetails.getSchema().getProperty().getType(),
				resourcePropertiesFromResponse.getSchema().getProperty().getType()); // string/integer/boolean/float
		verifyResourcePropertyList(basicVFC, propertyDetails, expecteddefaultValues);
		// Update resource property type = "list"
		propertyDetails.setPropertyDefaultValue(newPropertyDefaltValues);
		propertyDetails.getSchema().getProperty().setType(newEntrySchemaType);
		RestResponse updatePropertyResponse = AtomicOperationUtils
				.updatePropertyOfResource(propertyDetails, basicVFC, propertyUniqueId, UserRoleEnum.DESIGNER, false)
				.right().value();
		assertTrue(updatePropertyResponse.getErrorCode().equals(STATUS_CODE_INVALID_CONTENT));
		ArrayList<String> variables = new ArrayList<>();
		variables.add(propertyDetails.getName());
		variables.add(propertyDetails.getPropertyType());
		variables.add(propertyDetails.getSchema().getProperty().getType());
		variables.add(newPropertyDefaltValues);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_COMPLEX_DEFAULT_VALUE.name(), variables,
				updatePropertyResponse.getResponse());
	}

	@Test
	public void updatePropertyOfDerivedResource() throws Exception {
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty(PropertyTypeEnum.STRING_LIST);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		ComponentInstanceProperty resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, true).left().value();
		String derivedResourcePropertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		AtomicOperationUtils.changeComponentState(basicVFC, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		// second resource derived from basicVFC
		Resource vfc1FromBasicVFC = AtomicOperationUtils
				.createResourcesByCustomNormativeTypeAndCatregory(ResourceTypeEnum.VFC, basicVFC,
						ResourceCategoryEnum.APPLICATION_L4_BORDER, UserRoleEnum.DESIGNER, true)
				.left().value();
		// add property Type list to second resource
		PropertyReqDetails defaultListProperty = ElementFactory.getDefaultListProperty(PropertyTypeEnum.INTEGER_LIST);
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(defaultListProperty, vfc1FromBasicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Update property (list) of derived resource
		RestResponse updatePropertyResponse = AtomicOperationUtils.updatePropertyOfResource(propertyDetails, basicVFC,
				derivedResourcePropertyUniqueId, UserRoleEnum.DESIGNER, false).right().value();
		assertTrue(updatePropertyResponse.getErrorCode().equals(BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION));
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				updatePropertyResponse.getResponse());
		// Verify resource's priority list did not changed
		verifyResourcePropertyList(basicVFC, propertyDetails, "[\"a\",\"b\"]");
	}

	@Test
	public void updatePropertyOfNonDerivedResource() throws Exception {
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty(PropertyTypeEnum.STRING_LIST);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		ComponentInstanceProperty resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.changeComponentState(basicVFC, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		// second resource derived from basicVFC
		Resource vfc1FromBasicVFC = AtomicOperationUtils
				.createResourcesByCustomNormativeTypeAndCatregory(ResourceTypeEnum.VFC, basicVFC,
						ResourceCategoryEnum.APPLICATION_L4_BORDER, UserRoleEnum.DESIGNER, true)
				.left().value();
		// add property Type list to second resource
		PropertyReqDetails defaultListProperty = ElementFactory.getDefaultListProperty(PropertyTypeEnum.INTEGER_LIST);
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(defaultListProperty, vfc1FromBasicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		// Update property (list) of derived resource
		defaultListProperty.setPropertyDefaultValue("[1,2,3,4]");
		String expectedDefaultValue = "[1,2,3,4]";
		ComponentInstanceProperty resourcePropertyAfterUpdate = AtomicOperationUtils
				.updatePropertyOfResource(defaultListProperty, vfc1FromBasicVFC, propertyUniqueId,
						UserRoleEnum.DESIGNER, true)
				.left().value();
		assertEquals(resourcePropertyAfterUpdate.getType(), "list");
		assertEquals(resourcePropertyAfterUpdate.getDefaultValue(), expectedDefaultValue);
		assertEquals(resourcePropertyAfterUpdate.getSchema().getProperty().getType(),
				defaultListProperty.getSchema().getProperty().getType()); // string/integer/boolean/float
		// Get resource and verify updated default value
		RestResponse restResponse = ResourceRestUtils.getResource(vfc1FromBasicVFC.getUniqueId());
		String expectedDefaultValueFromDerivedResource = "[\"a\",\"b\"]";
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		resource.getProperties().get(0).getDefaultValue().equals(expectedDefaultValue);
		resource.getProperties().get(1).getDefaultValue().equals(expectedDefaultValueFromDerivedResource);
	}

	@Test
	public void updateListPropertyToNonCheckedOutResource() throws Exception {
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		String PropertyDefaultValue = "[2,3]";
		propertyDetails.setPropertyDefaultValue(PropertyDefaultValue);
		propertyDetails.getSchema().getProperty().setType("integer");
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		ComponentInstanceProperty resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, true).left().value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		AtomicOperationUtils.changeComponentState(basicVFC, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		// Update resource property type = "list"
		propertyDetails.setPropertyDefaultValue("[3,4]");
		propertyDetails.getSchema().getProperty().setType("integer");
		RestResponse updatePropertyResponse = AtomicOperationUtils
				.updatePropertyOfResource(propertyDetails, basicVFC, propertyUniqueId, UserRoleEnum.DESIGNER, false)
				.right().value();
		assertTrue(updatePropertyResponse.getErrorCode().equals(BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION));
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				updatePropertyResponse.getResponse());
		// Verify resource's priority list did not changed
		verifyResourcePropertyList(basicVFC, propertyDetails, "[2,3]");
	}

	@Test
	public void updateListPropertyResourceByNonResouceOwner() throws Exception {
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		String PropertyDefaultValue = "[2,3]";
		propertyDetails.setPropertyDefaultValue(PropertyDefaultValue);
		propertyDetails.getSchema().getProperty().setType("integer");
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		ComponentInstanceProperty resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, true).left().value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		// AtomicOperationUtils.changeComponentState(basicVFC,
		// UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		// Update resource property type = "list"
		propertyDetails.setPropertyDefaultValue("[3,4]");
		propertyDetails.getSchema().getProperty().setType("integer");
		RestResponse updatePropertyResponse = AtomicOperationUtils
				.updatePropertyOfResource(propertyDetails, basicVFC, propertyUniqueId, UserRoleEnum.DESIGNER2, false)
				.right().value();
		assertTrue(updatePropertyResponse.getErrorCode().equals(BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION));
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				updatePropertyResponse.getResponse());
		// Verify resource's priority list did not changed
		verifyResourcePropertyList(basicVFC, propertyDetails, "[2,3]");
	}

	@Test
	public void updateListPropertyResourceByTester() throws Exception {
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		String PropertyDefaultValue = "[2,3]";
		propertyDetails.setPropertyDefaultValue(PropertyDefaultValue);
		propertyDetails.getSchema().getProperty().setType("integer");
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		ComponentInstanceProperty resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, true).left().value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		// AtomicOperationUtils.changeComponentState(basicVFC,
		// UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		// Update resource property type = "list"
		propertyDetails.setPropertyDefaultValue("[3,4]");
		propertyDetails.getSchema().getProperty().setType("integer");
		RestResponse updatePropertyResponse = AtomicOperationUtils
				.updatePropertyOfResource(propertyDetails, basicVFC, propertyUniqueId, UserRoleEnum.TESTER, false)
				.right().value();
		assertTrue(updatePropertyResponse.getErrorCode().equals(BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION));
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				updatePropertyResponse.getResponse());
		// Verify resource's priority list did not changed
		verifyResourcePropertyList(basicVFC, propertyDetails, "[2,3]");
	}

	// DE199964
	@Test(enabled = false)
	public void updateListPropertyToNonExistingResource() throws Exception {
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		String PropertyDefaultValue = "[2,3]";
		propertyDetails.setPropertyDefaultValue(PropertyDefaultValue);
		propertyDetails.getSchema().getProperty().setType("integer");
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		ComponentInstanceProperty resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, true).left().value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		String resourceUniqueId = basicVFC.getUniqueId();
		basicVFC.setUniqueId("1111111");
		RestResponse updatePropertyResponse = AtomicOperationUtils
				.updatePropertyOfResource(propertyDetails, basicVFC, propertyUniqueId, UserRoleEnum.DESIGNER, false)
				.right().value();
		assertTrue(updatePropertyResponse.getErrorCode().equals(BaseRestUtils.STATUS_CODE_NOT_FOUND));
		ArrayList<String> variables = new ArrayList<>();
		variables.add(basicVFC.getUniqueId());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_NOT_FOUND.name(), variables,
				updatePropertyResponse.getResponse());
		// Verify resource's priority list did not changed
		basicVFC.setUniqueId(resourceUniqueId);
		verifyResourcePropertyList(basicVFC, propertyDetails, "[2,3]");
	}

	// DE199725
	@Test
	public void updateResourcePropertyListNonSupportedPropertyType() throws Exception { // Not
																						// "list"
																						// type
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		String PropertyDefaultValue = "[2,3]";
		propertyDetails.setPropertyDefaultValue(PropertyDefaultValue);
		propertyDetails.getSchema().getProperty().setType("integer");
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		ComponentInstanceProperty resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, true).left().value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		// update resource property
		String propertyType = "listttttttt";
		propertyDetails.setPropertyType(propertyType);
		RestResponse updatePropertyResponse = AtomicOperationUtils
				.updatePropertyOfResource(propertyDetails, basicVFC, propertyUniqueId, UserRoleEnum.DESIGNER, false)
				.right().value();
		ArrayList<String> variables = new ArrayList<>();
		variables.add(propertyDetails.getPropertyType()); // property data type
															// (koko instead
															// list)
		variables.add(propertyDetails.getName());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_PROPERTY_TYPE.name(), variables,
				updatePropertyResponse.getResponse());
		// Verify resource's priority list did not changed
		propertyDetails.setPropertyType("list");
		verifyResourcePropertyList(basicVFC, propertyDetails, "[2,3]");
	}

	@Test(enabled = false) // DE199732
	public void updateResourcePropertyListNonSupportedEntrySchemaType() throws Exception {
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		String PropertyDefaultValue = "[2,3]";
		propertyDetails.setPropertyDefaultValue(PropertyDefaultValue);
		propertyDetails.getSchema().getProperty().setType("integer");
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		ComponentInstanceProperty resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, true).left().value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		// update resource property
		String EntrySchemaType = "integerrrrrr";
		propertyDetails.getSchema().getProperty().setType(EntrySchemaType);
		RestResponse updatePropertyResponse = AtomicOperationUtils
				.updatePropertyOfResource(propertyDetails, basicVFC, propertyUniqueId, UserRoleEnum.DESIGNER, false)
				.right().value();
		assertTrue(updatePropertyResponse.getErrorCode().equals(STATUS_CODE_INVALID_CONTENT));
		ArrayList<String> variables = new ArrayList<>();
		variables.add(EntrySchemaType);
		variables.add(propertyDetails.getName());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_PROPERTY_INNER_TYPE.name(), variables,
				updatePropertyResponse.getResponse());
		propertyDetails.getSchema().getProperty().setType("integer");
		verifyResourcePropertyList(basicVFC, propertyDetails, "[2,3]");
	}

	@Test(dataProvider = "updatePropertiesListDefaultValueSuccessFlow")
	public void updateResourcePropertyListSuccessFlow(String entrySchemaType, String propertyDefaltValues,
			String expecteddefaultValues, String newEntrySchemaType, String newPropertyDefaltValues,
			String newExpecteddefaultValues) throws Exception {
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		propertyDetails.setPropertyDefaultValue(propertyDefaltValues);
		propertyDetails.getSchema().getProperty().setType(entrySchemaType);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		ComponentInstanceProperty resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, true).left().value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		// verify properties return from response
		assertEquals("list", resourcePropertiesFromResponse.getType());
		assertEquals(expecteddefaultValues, resourcePropertiesFromResponse.getDefaultValue());
		assertEquals(propertyDetails.getSchema().getProperty().getType(),
				resourcePropertiesFromResponse.getSchema().getProperty().getType()); // string/integer/boolean/float
		verifyResourcePropertyList(basicVFC, propertyDetails, expecteddefaultValues);
		// Update resource property type = "list"
		propertyDetails.setPropertyDefaultValue(newPropertyDefaltValues);
		propertyDetails.getSchema().getProperty().setType(newEntrySchemaType);
		ComponentInstanceProperty resourcePropertyAfterUpdate = AtomicOperationUtils
				.updatePropertyOfResource(propertyDetails, basicVFC, propertyUniqueId, UserRoleEnum.DESIGNER, true)
				.left().value();
		assertEquals("list", resourcePropertyAfterUpdate.getType());
		assertEquals(newExpecteddefaultValues, resourcePropertyAfterUpdate.getDefaultValue());
		assertEquals(propertyDetails.getSchema().getProperty().getType(),
				resourcePropertyAfterUpdate.getSchema().getProperty().getType()); // string/integer/boolean/float
		verifyResourcePropertyList(basicVFC, propertyDetails, newExpecteddefaultValues);
	}

	// Add property type list to resource
	// DE199718
	@Test(dataProvider = "invalidListProperties") // invalid default values
	public void addListPropertyToResourceFailureFlow(String entrySchemaType, String propertyDefaltValues)
			throws Exception {
		// String propertyType = "list";
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		propertyDetails.getSchema().getProperty().setType(entrySchemaType);
		propertyDetails.setPropertyDefaultValue(propertyDefaltValues);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		RestResponse addPropertyToResourceResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, false).right().value();
		assertTrue(addPropertyToResourceResponse.getErrorCode().equals(STATUS_CODE_INVALID_CONTENT));
		ArrayList<String> variables = new ArrayList<>();
		variables.add(propertyDetails.getName());
		variables.add(propertyDetails.getPropertyType());
		variables.add(propertyDetails.getSchema().getProperty().getType());
		variables.add(propertyDefaltValues);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_COMPLEX_DEFAULT_VALUE.name(), variables,
				addPropertyToResourceResponse.getResponse());

	}

	// DE199964
	@Test
	public void addListPropertyToNonExistingResource() throws Exception {
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		propertyDetails.getSchema().getProperty().setType("integer");
		propertyDetails.setPropertyDefaultValue("[1,2]");
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to non existing resource
		basicVFC.setUniqueId("1111111");
		RestResponse addPropertyToResourceResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, false).right().value();
		assertTrue(addPropertyToResourceResponse.getErrorCode().equals(BaseRestUtils.STATUS_CODE_NOT_FOUND));
		ArrayList<String> variables = new ArrayList<>();
		variables.add("");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_NOT_FOUND.name(), variables,
				addPropertyToResourceResponse.getResponse());
	}

	@Test
	public void addListPropertyToNonCheckedOutResource() throws Exception {
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		propertyDetails.getSchema().getProperty().setType("integer");
		propertyDetails.setPropertyDefaultValue("[1,2]");
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		AtomicOperationUtils.changeComponentState(basicVFC, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		// Add property type list to non Checked-Out resource
		RestResponse addPropertyToResourceResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, false).right().value();
		assertTrue(addPropertyToResourceResponse.getErrorCode().equals(BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION));
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				addPropertyToResourceResponse.getResponse());
	}

	@Test
	public void addListPropertyToResourceByNonResourceOwner() throws Exception {
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		propertyDetails.getSchema().getProperty().setType("integer");
		propertyDetails.setPropertyDefaultValue("[1,2]");
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to non Checked-Out resource
		RestResponse addPropertyToResourceResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER2, false).right().value();
		assertTrue(addPropertyToResourceResponse.getErrorCode().equals(BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION));
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				addPropertyToResourceResponse.getResponse());
	}

	@Test
	public void addListPropertyToResourcePropertyAlreadyExists01() throws Exception {
		String propertyType = "list";
		String propertySchemaType = "integer";
		String defaultValues = "[1,2]";
		String expecteddefaultValues = "[1,2]";
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		propertyDetails.getSchema().getProperty().setType(propertySchemaType);
		propertyDetails.setPropertyDefaultValue(defaultValues);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// check-in and check-out resource
		RestResponse changeComponentState = LifecycleRestUtils.changeComponentState(basicVFC,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), LifeCycleStatesEnum.CHECKIN);
		assertTrue(changeComponentState.getErrorCode().equals(BaseRestUtils.STATUS_CODE_SUCCESS));
		changeComponentState = LifecycleRestUtils.changeComponentState(basicVFC,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), LifeCycleStatesEnum.CHECKOUT);
		assertTrue(changeComponentState.getErrorCode().equals(BaseRestUtils.STATUS_CODE_SUCCESS));
		// Add property type list to resource
		ComponentInstanceProperty resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, true).left().value();
		// verify properties return from response
		assertEquals(resourcePropertiesFromResponse.getType(), propertyType);
		assertEquals(resourcePropertiesFromResponse.getDefaultValue(), expecteddefaultValues);
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(), propertySchemaType); // string/integer/boolean/float
		verifyResourcePropertyList(basicVFC, propertyDetails, expecteddefaultValues);
		// Add same property again to resource
		RestResponse addPropertyRestResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, false).right().value();
		assertTrue(addPropertyRestResponse.getErrorCode().equals(BaseRestUtils.STATUS_CODE_ALREADY_EXISTS));
		ArrayList<String> variables = new ArrayList<>();
		variables.add("");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.PROPERTY_ALREADY_EXIST.name(), variables,
				addPropertyRestResponse.getResponse());
		// verify property not deleted
		verifyResourcePropertyList(basicVFC, propertyDetails, expecteddefaultValues);
	}

	@Test
	public void addListPropertyToResourcePropertyAlreadyExists02() throws Exception {
		String propertyType = "list";
		String propertySchemaType = "integer";
		String defaultValues = "[1,2]";
		String expecteddefaultValues = "[1,2]";
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		propertyDetails.getSchema().getProperty().setType(propertySchemaType);
		propertyDetails.setPropertyDefaultValue(defaultValues);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		ComponentInstanceProperty resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, true).left().value();
		// verify properties return from response
		assertEquals(resourcePropertiesFromResponse.getType(), propertyType);
		assertEquals(resourcePropertiesFromResponse.getDefaultValue(), expecteddefaultValues);
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(), propertySchemaType); // string/integer/boolean/float
		verifyResourcePropertyList(basicVFC, propertyDetails, expecteddefaultValues);
		// check-in and check-out resource
		RestResponse changeComponentState = LifecycleRestUtils.changeComponentState(basicVFC,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), LifeCycleStatesEnum.CHECKIN);
		assertTrue(changeComponentState.getErrorCode().equals(BaseRestUtils.STATUS_CODE_SUCCESS));
		changeComponentState = LifecycleRestUtils.changeComponentState(basicVFC,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), LifeCycleStatesEnum.CHECKOUT);
		assertTrue(changeComponentState.getErrorCode().equals(BaseRestUtils.STATUS_CODE_SUCCESS));
		// Add same property again to resource
		RestResponse addPropertyRestResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, false).right().value();
		assertTrue(addPropertyRestResponse.getErrorCode().equals(BaseRestUtils.STATUS_CODE_ALREADY_EXISTS));
		ArrayList<String> variables = new ArrayList<>();
		variables.add("");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.PROPERTY_ALREADY_EXIST.name(), variables,
				addPropertyRestResponse.getResponse());
		// verify property not deleted
		verifyResourcePropertyList(basicVFC, propertyDetails, expecteddefaultValues);
	}

	@Test // DE199725
	public void addListPropertyToResourceNonSupportedPropertyType() throws Exception { // Not
																						// "list"
																						// type
		String propertyType = "listttttttt";
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		propertyDetails.setPropertyType(propertyType);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		RestResponse addPropertyRestResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, false).right().value();
		assertTrue(addPropertyRestResponse.getErrorCode().equals(STATUS_CODE_INVALID_CONTENT));
		ArrayList<String> variables = new ArrayList<>();
		variables.add(propertyDetails.getPropertyType()); // property data type
															// (koko instead
															// list)
		variables.add(propertyDetails.getName());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_PROPERTY_TYPE.name(), variables,
				addPropertyRestResponse.getResponse());
	}

	@Test // DE199732
	public void addListPropertyToResourceNonSupportedEntrySchemaType() throws Exception {
		String EntrySchemaType = "stringggg"; // instead "string"
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		propertyDetails.getSchema().getProperty().setType(EntrySchemaType);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		RestResponse addPropertyRestResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, false).right().value();
		assertTrue(addPropertyRestResponse.getErrorCode().equals(STATUS_CODE_INVALID_CONTENT));
		ArrayList<String> variables = new ArrayList<>();
		variables.add(EntrySchemaType);
		variables.add(propertyDetails.getName());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_PROPERTY_INNER_TYPE.name(), variables,
				addPropertyRestResponse.getResponse());
	}

	@Test
	public void addHundredPropertyListToResourceSuccessFlow() throws Exception {
		String propertyType = "list";
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		String propertyName = propertyDetails.getName();
		int numberOfPropertiesToAddToResource = 100;
		ComponentInstanceProperty resourcePropertiesFromResponse;
		for (int x = 0; x < numberOfPropertiesToAddToResource; x++) {
			propertyDetails.setName(propertyName + x);
			resourcePropertiesFromResponse = AtomicOperationUtils
					.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, true).left().value();
			// verify properties return from response
			assertEquals(resourcePropertiesFromResponse.getName(), propertyName + x);
			assertEquals(resourcePropertiesFromResponse.getType(), propertyType);
			assertEquals(resourcePropertiesFromResponse.getDefaultValue(), "[\"a\",\"b\"]");
			assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
					propertyDetails.getSchema().getProperty().getType()); // string/integer/boolean/float
		}
		// get resource and verify that 100 properties exist
		Resource resourceObject = AtomicOperationUtils.getResourceObject(basicVFC, UserRoleEnum.DESIGNER);
		assertEquals(numberOfPropertiesToAddToResource, resourceObject.getProperties().size());

	}

	@Test(dataProvider = "propertiesListDefaultValueSuccessFlow")
	public void addListPropertyToResourceSuccessFlow(String entrySchemaType, String propertyDefaltValues,
			String expecteddefaultValues) throws Exception {
		String propertyType = "list";
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		propertyDetails.getSchema().getProperty().setType(entrySchemaType);
		propertyDetails.setPropertyDefaultValue(propertyDefaltValues);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		ComponentInstanceProperty resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, true).left().value();
		// verify properties return from response
		assertEquals(propertyType, resourcePropertiesFromResponse.getType());
		assertEquals(expecteddefaultValues, resourcePropertiesFromResponse.getDefaultValue());
		assertEquals(entrySchemaType, resourcePropertiesFromResponse.getSchema().getProperty().getType()); // string/integer/boolean/float
		verifyResourcePropertyList(basicVFC, propertyDetails, expecteddefaultValues);

		assertEquals(resourcePropertiesFromResponse.getType(), propertyType);
		assertEquals(resourcePropertiesFromResponse.getDefaultValue(), expecteddefaultValues);
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(), entrySchemaType); // string/integer/boolean/float
		verifyResourcePropertyList(basicVFC, propertyDetails, expecteddefaultValues);
	}

	// Delete property type list
	@Test
	public void deleteOneOfTheListPropertiesFromResourceAndAddItAgain() throws Exception {
		ComponentInstanceProperty resourcePropertiesFromResponse;
		PropertyReqDetails propertyDetailsTypeString = ElementFactory
				.getDefaultListProperty(PropertyTypeEnum.STRING_LIST);
		PropertyReqDetails propertyDetailsInteger = ElementFactory
				.getDefaultListProperty(PropertyTypeEnum.INTEGER_LIST);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add 2 property type list to resource
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsTypeString, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsTypeString.getPropertyType());
		assertEquals(resourcePropertiesFromResponse.getDefaultValue(), "[\"a\",\"b\"]");
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsTypeString.getSchema().getProperty().getType()); // string/integer/boolean/float
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsInteger, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsInteger.getPropertyType());
		assertEquals(resourcePropertiesFromResponse.getDefaultValue(), "[1,2]");
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsInteger.getSchema().getProperty().getType());
		// Get resource and verify updated default value
		RestResponse restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(2, resource.getProperties().size());
		// Delete one resource
		RestResponse deletePropertyOfResource = AtomicOperationUtils.deletePropertyOfResource(basicVFC.getUniqueId(),
				propertyUniqueId, UserRoleEnum.DESIGNER);
		assertTrue(BaseRestUtils.STATUS_CODE_DELETE == deletePropertyOfResource.getErrorCode());
		// Get resource and verify updated default value
		restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(1, resource.getProperties().size());
		verifyResourcePropertyList(basicVFC, propertyDetailsTypeString, "[\"a\",\"b\"]");
		// Add deleted property again to resource
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsInteger, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsInteger.getPropertyType());
		assertEquals(resourcePropertiesFromResponse.getDefaultValue(), "[1,2]");
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsInteger.getSchema().getProperty().getType());
		// Get resource and verify updated default value
		restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(2, resource.getProperties().size());
	}

	@Test
	public void deletePropertyListTypeInteger() throws Exception {
		ComponentInstanceProperty resourcePropertiesFromResponse;
		PropertyReqDetails propertyDetailsTypeString = ElementFactory
				.getDefaultListProperty(PropertyTypeEnum.STRING_LIST);
		PropertyReqDetails propertyDetailsInteger = ElementFactory
				.getDefaultListProperty(PropertyTypeEnum.INTEGER_LIST);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add 2 property type list to resource
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsTypeString, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsTypeString.getPropertyType());
		assertEquals(resourcePropertiesFromResponse.getDefaultValue(), "[\"a\",\"b\"]");
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsTypeString.getSchema().getProperty().getType()); // string/integer/boolean/float
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsInteger, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsInteger.getPropertyType());
		assertEquals(resourcePropertiesFromResponse.getDefaultValue(), "[1,2]");
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsInteger.getSchema().getProperty().getType());
		// Get resource and verify updated default value
		RestResponse restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(2, resource.getProperties().size());
		// Delete one resource
		RestResponse deletePropertyOfResource = AtomicOperationUtils.deletePropertyOfResource(basicVFC.getUniqueId(),
				propertyUniqueId, UserRoleEnum.DESIGNER);
		assertTrue(BaseRestUtils.STATUS_CODE_DELETE == deletePropertyOfResource.getErrorCode());
		// Get resource and verify updated default value
		restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(1, resource.getProperties().size());
		verifyResourcePropertyList(basicVFC, propertyDetailsInteger, "[1,2]");
	}

	@Test
	public void deletePropertyListTypeBoolean() throws Exception {
		ComponentInstanceProperty resourcePropertiesFromResponse;
		PropertyReqDetails propertyDetailsTypeString = ElementFactory
				.getDefaultListProperty(PropertyTypeEnum.BOOLEAN_LIST);
		PropertyReqDetails propertyDetailsInteger = ElementFactory
				.getDefaultListProperty(PropertyTypeEnum.INTEGER_LIST);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add 2 property type list to resource
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsTypeString, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsTypeString.getPropertyType());
		assertEquals(resourcePropertiesFromResponse.getDefaultValue(), "[true,false]");
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsTypeString.getSchema().getProperty().getType()); // string/integer/boolean/float
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsInteger, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsInteger.getPropertyType());
		assertEquals(resourcePropertiesFromResponse.getDefaultValue(), "[1,2]");
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsInteger.getSchema().getProperty().getType());
		// Get resource and verify updated default value
		RestResponse restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(2, resource.getProperties().size());
		// Delete one property
		RestResponse deletePropertyOfResource = AtomicOperationUtils.deletePropertyOfResource(basicVFC.getUniqueId(),
				propertyUniqueId, UserRoleEnum.DESIGNER);
		assertTrue(BaseRestUtils.STATUS_CODE_DELETE == deletePropertyOfResource.getErrorCode());
		// Get resource and verify updated default value
		restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(1, resource.getProperties().size());
		verifyResourcePropertyList(basicVFC, propertyDetailsInteger, "[1,2]");
	}

	@Test
	public void deletePropertyListTypeFloat() throws Exception {
		ComponentInstanceProperty resourcePropertiesFromResponse;
		PropertyReqDetails propertyDetailsTypeString = ElementFactory
				.getDefaultListProperty(PropertyTypeEnum.FLOAT_LIST);
		PropertyReqDetails propertyDetailsInteger = ElementFactory
				.getDefaultListProperty(PropertyTypeEnum.INTEGER_LIST);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add 2 property type list to resource
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsTypeString, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsTypeString.getPropertyType());
		assertEquals(resourcePropertiesFromResponse.getDefaultValue(), "[1.0,2.0]");
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsTypeString.getSchema().getProperty().getType()); // string/integer/boolean/float
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsInteger, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsInteger.getPropertyType());
		assertEquals(resourcePropertiesFromResponse.getDefaultValue(), "[1,2]");
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsInteger.getSchema().getProperty().getType());
		// Get resource and verify updated default value
		RestResponse restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(2, resource.getProperties().size());
		// Delete one property
		RestResponse deletePropertyOfResource = AtomicOperationUtils.deletePropertyOfResource(basicVFC.getUniqueId(),
				propertyUniqueId, UserRoleEnum.DESIGNER);
		assertTrue(BaseRestUtils.STATUS_CODE_DELETE == deletePropertyOfResource.getErrorCode());
		// Get resource and verify updated default value
		restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(1, resource.getProperties().size());
		verifyResourcePropertyList(basicVFC, propertyDetailsInteger, "[1,2]");
	}

	@Test
	public void deletePropertyListAlreadyDeleted() throws Exception {
		ComponentInstanceProperty resourcePropertiesFromResponse;
		PropertyReqDetails propertyDetailsTypeString = ElementFactory
				.getDefaultListProperty(PropertyTypeEnum.FLOAT_LIST);
		PropertyReqDetails propertyDetailsInteger = ElementFactory
				.getDefaultListProperty(PropertyTypeEnum.INTEGER_LIST);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add 2 property type list to resource
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsTypeString, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsTypeString.getPropertyType());
		assertEquals(resourcePropertiesFromResponse.getDefaultValue(), "[1.0,2.0]");
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsTypeString.getSchema().getProperty().getType()); // string/integer/boolean/float
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsInteger, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsInteger.getPropertyType());
		assertEquals(resourcePropertiesFromResponse.getDefaultValue(), "[1,2]");
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsInteger.getSchema().getProperty().getType());
		// Get resource and verify updated default value
		RestResponse restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(2, resource.getProperties().size());
		// Delete one property
		RestResponse deletePropertyOfResource = AtomicOperationUtils.deletePropertyOfResource(basicVFC.getUniqueId(),
				propertyUniqueId, UserRoleEnum.DESIGNER);
		assertTrue(BaseRestUtils.STATUS_CODE_DELETE == deletePropertyOfResource.getErrorCode());
		// Get resource and verify updated default value
		restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(1, resource.getProperties().size());
		verifyResourcePropertyList(basicVFC, propertyDetailsInteger, "[1,2]");
		// delete again the same property
		deletePropertyOfResource = AtomicOperationUtils.deletePropertyOfResource(basicVFC.getUniqueId(),
				propertyUniqueId, UserRoleEnum.DESIGNER);
		assertTrue(BaseRestUtils.STATUS_CODE_NOT_FOUND == deletePropertyOfResource.getErrorCode());
		ArrayList<String> variables = new ArrayList<>();
		variables.add("");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.PROPERTY_NOT_FOUND.name(), variables,
				deletePropertyOfResource.getResponse());
	}

	@Test
	public void deletePropertyListResourceIsNotCheckedOutState() throws Exception {
		ComponentInstanceProperty resourcePropertiesFromResponse;
		PropertyReqDetails propertyDetailsTypeString = ElementFactory
				.getDefaultListProperty(PropertyTypeEnum.FLOAT_LIST);
		String expectedDefaultvalues = "[1.0,2.0]";
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsTypeString, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsTypeString.getPropertyType());
		assertEquals(resourcePropertiesFromResponse.getDefaultValue(), expectedDefaultvalues);
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsTypeString.getSchema().getProperty().getType()); // string/integer/boolean/float
		// Get resource and verify updated default value
		verifyResourcePropertyList(basicVFC, propertyDetailsTypeString, expectedDefaultvalues);
		// Check-in resource
		AtomicOperationUtils.changeComponentState(basicVFC, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		// Delete property
		RestResponse deletePropertyOfResource = AtomicOperationUtils.deletePropertyOfResource(basicVFC.getUniqueId(),
				propertyUniqueId, UserRoleEnum.DESIGNER);
		assertTrue(BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION == deletePropertyOfResource.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				deletePropertyOfResource.getResponse());
		// Get resource and verify property is not deleted
		verifyResourcePropertyList(basicVFC, propertyDetailsTypeString, expectedDefaultvalues);
	}

	@Test
	public void deletePropertyListResourceByNotIsNonResouceOwner() throws Exception {
		ComponentInstanceProperty resourcePropertiesFromResponse;
		PropertyReqDetails propertyDetailsTypeString = ElementFactory
				.getDefaultListProperty(PropertyTypeEnum.FLOAT_LIST);
		String expectedDefaultvalues = "[1.0,2.0]";
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsTypeString, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsTypeString.getPropertyType());
		assertEquals(resourcePropertiesFromResponse.getDefaultValue(), expectedDefaultvalues);
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsTypeString.getSchema().getProperty().getType()); // string/integer/boolean/float
		// Get resource and verify updated default value
		verifyResourcePropertyList(basicVFC, propertyDetailsTypeString, expectedDefaultvalues);
		// Delete property by non resource owner
		RestResponse deletePropertyOfResource = AtomicOperationUtils.deletePropertyOfResource(basicVFC.getUniqueId(),
				propertyUniqueId, UserRoleEnum.DESIGNER2);
		assertTrue(BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION == deletePropertyOfResource.getErrorCode());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				deletePropertyOfResource.getResponse());
		// Get resource and verify property is not deleted
		verifyResourcePropertyList(basicVFC, propertyDetailsTypeString, expectedDefaultvalues);
	}

	@Test
	public void deletePropertyListFromNonExistingResource() throws Exception {
		ComponentInstanceProperty resourcePropertiesFromResponse;
		PropertyReqDetails propertyDetailsTypeString = ElementFactory
				.getDefaultListProperty(PropertyTypeEnum.FLOAT_LIST);
		String expectedDefaultvalues = "[1.0,2.0]";
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		String actualResourceUniqueId = basicVFC.getUniqueId();
		// Add property type list to resource
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsTypeString, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsTypeString.getPropertyType());
		assertEquals(resourcePropertiesFromResponse.getDefaultValue(), expectedDefaultvalues);
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsTypeString.getSchema().getProperty().getType()); // string/integer/boolean/float
		// Get resource and verify updated default value
		verifyResourcePropertyList(basicVFC, propertyDetailsTypeString, expectedDefaultvalues);
		// Delete property from non existing resource
		basicVFC.setUniqueId("1111111");
		RestResponse deletePropertyOfResource = AtomicOperationUtils.deletePropertyOfResource(basicVFC.getUniqueId(),
				propertyUniqueId, UserRoleEnum.DESIGNER);
		assertTrue(deletePropertyOfResource.getErrorCode().equals(BaseRestUtils.STATUS_CODE_NOT_FOUND));
		ArrayList<String> variables = new ArrayList<>();
		variables.add("");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_NOT_FOUND.name(), variables,
				deletePropertyOfResource.getResponse());
		// Get resource and verify property is not deleted
		basicVFC.setUniqueId(actualResourceUniqueId);
		verifyResourcePropertyList(basicVFC, propertyDetailsTypeString, expectedDefaultvalues);
	}

	@Test
	public void deletePropertyOfDerivedResource() throws Exception {
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty(PropertyTypeEnum.STRING_LIST);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		ComponentInstanceProperty resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, true).left().value();
		String derivedResourcePropertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		AtomicOperationUtils.changeComponentState(basicVFC, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		// second resource derived from basicVFC
		Resource vfc1FromBasicVFC = AtomicOperationUtils
				.createResourcesByCustomNormativeTypeAndCatregory(ResourceTypeEnum.VFC, basicVFC,
						ResourceCategoryEnum.APPLICATION_L4_BORDER, UserRoleEnum.DESIGNER, true)
				.left().value();
		// Delete property (list) of derived resource
		RestResponse deletePropertyOfResource = AtomicOperationUtils.deletePropertyOfResource(
				vfc1FromBasicVFC.getUniqueId(), derivedResourcePropertyUniqueId, UserRoleEnum.DESIGNER);
		assertTrue(deletePropertyOfResource.getErrorCode().equals(BaseRestUtils.STATUS_CODE_NOT_FOUND));
		ArrayList<String> variables = new ArrayList<>();
		variables.add("");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.PROPERTY_NOT_FOUND.name(), variables,
				deletePropertyOfResource.getResponse());
		// Verify resource's priority list did not changed
		verifyResourcePropertyList(vfc1FromBasicVFC, propertyDetails, "[\"a\",\"b\"]");
	}

	@Test
	public void deletePropertyOfNonDerivedResource() throws Exception {
		PropertyReqDetails propertyListString = ElementFactory.getDefaultListProperty(PropertyTypeEnum.STRING_LIST);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		ComponentInstanceProperty resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyListString, basicVFC, UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.changeComponentState(basicVFC, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		// second resource derived from basicVFC
		Resource vfc1FromBasicVFC = AtomicOperationUtils
				.createResourcesByCustomNormativeTypeAndCatregory(ResourceTypeEnum.VFC, basicVFC,
						ResourceCategoryEnum.APPLICATION_L4_BORDER, UserRoleEnum.DESIGNER, true)
				.left().value();
		// add property Type list to second resource
		PropertyReqDetails propertyListInteger = ElementFactory.getDefaultListProperty(PropertyTypeEnum.INTEGER_LIST);
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyListInteger, vfc1FromBasicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		// Delete property (list) of derived resource
		RestResponse deletePropertyOfResource = AtomicOperationUtils
				.deletePropertyOfResource(vfc1FromBasicVFC.getUniqueId(), propertyUniqueId, UserRoleEnum.DESIGNER);
		assertTrue(BaseRestUtils.STATUS_CODE_DELETE == deletePropertyOfResource.getErrorCode());
		// Get resource and verify updated default value
		RestResponse restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(1, resource.getProperties().size());
		verifyResourcePropertyList(basicVFC, propertyListString, "[\"a\",\"b\"]");
	}

	private void verifyResourcePropertyList(Resource resource, PropertyReqDetails expectedProperty,
			String expecteddefaultValues) throws Exception {
		// get resource and verify property from type list
		Resource getResource = AtomicOperationUtils.getResourceObject(resource, UserRoleEnum.DESIGNER);
		List<PropertyDefinition> actualResourceProperties = getResource.getProperties();
		boolean isPropertyAppear = false;
		for (PropertyDefinition pro : actualResourceProperties) {
			if (expectedProperty.getName().equals(pro.getName())) {
				assertTrue("Check Property Type ", pro.getType().equals(expectedProperty.getPropertyType()));
				assertEquals("Check Property  default values ", expecteddefaultValues, pro.getDefaultValue());
				// assertTrue("Check Property default values ",
				// pro.getDefaultValue().equals(expecteddefaultValues));
				assertTrue("Check entrySchema Property Type ", pro.getSchema().getProperty().getType()
						.equals(expectedProperty.getSchema().getProperty().getType()));
				isPropertyAppear = true;
			}
		}
		assertTrue(isPropertyAppear);
	}

	// US656905
	// --------------------- Map Property
	// ----------------------------------------------------------------
	@Test(dataProvider = "updatePropertiesMapDefaultValueFailureFlow")
	public void updateDefaultValueOfResourcePropertyMapFailureFlow(String entrySchemaType, String propertyDefaultValues,
			String expectedDefaultValue, String newEntrySchemaType, String newPropertyDefaultValue) throws Exception {
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultMapProperty();
		propertyDetails.setPropertyDefaultValue(propertyDefaultValues);
		propertyDetails.getSchema().getProperty().setType(entrySchemaType);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		ComponentInstanceProperty resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, true).left().value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		// verify properties return from response
		assertEquals("map", resourcePropertiesFromResponse.getType());
		assertEquals(expectedDefaultValue, resourcePropertiesFromResponse.getDefaultValue());
		assertEquals(propertyDetails.getSchema().getProperty().getType(),
				resourcePropertiesFromResponse.getSchema().getProperty().getType()); // string/integer/boolean/float
		verifyResourcePropertyList(basicVFC, propertyDetails, expectedDefaultValue);
		// Update resource property type = "map"
		propertyDetails.setPropertyDefaultValue(newPropertyDefaultValue);
		propertyDetails.getSchema().getProperty().setType(newEntrySchemaType);
		RestResponse updatePropertyResponse = AtomicOperationUtils
				.updatePropertyOfResource(propertyDetails, basicVFC, propertyUniqueId, UserRoleEnum.DESIGNER, false)
				.right().value();
		assertTrue(updatePropertyResponse.getErrorCode().equals(STATUS_CODE_INVALID_CONTENT));
		ArrayList<String> variables = new ArrayList<>();
		variables.add(propertyDetails.getName());
		variables.add(propertyDetails.getPropertyType());
		variables.add(propertyDetails.getSchema().getProperty().getType());
		variables.add(newPropertyDefaultValue);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.INVALID_COMPLEX_DEFAULT_VALUE.name(), variables,
				updatePropertyResponse.getResponse());
	}

	@Test(dataProvider = "updatePropertiesMapDefaultValueSuccessFlow")
	public void updateResourcePropertyMapSuccessFlow(String entrySchemaType, String propertyDefaultValues,
			String expectedDefaultValue, String newEntrySchemaType, String newPropertyDefaultValue,
			String newExpectedDefaultValue) throws Exception {
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultMapProperty();
		propertyDetails.setPropertyDefaultValue(propertyDefaultValues);
		propertyDetails.getSchema().getProperty().setType(entrySchemaType);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		ComponentInstanceProperty resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, true).left().value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		// verify properties return from response
		assertEquals("map", resourcePropertiesFromResponse.getType());
		assertEquals(expectedDefaultValue, resourcePropertiesFromResponse.getDefaultValue());
		assertEquals(propertyDetails.getSchema().getProperty().getType(),
				resourcePropertiesFromResponse.getSchema().getProperty().getType()); // string/integer/boolean/float
		verifyResourcePropertyList(basicVFC, propertyDetails, expectedDefaultValue);
		// Update resource property type = "map"
		propertyDetails.setPropertyDefaultValue(newPropertyDefaultValue);
		propertyDetails.getSchema().getProperty().setType(newEntrySchemaType);
		ComponentInstanceProperty resourcePropertyAfterUpdate = AtomicOperationUtils
				.updatePropertyOfResource(propertyDetails, basicVFC, propertyUniqueId, UserRoleEnum.DESIGNER, true)
				.left().value();
		assertEquals("map", resourcePropertyAfterUpdate.getType());
		assertEquals(newExpectedDefaultValue, resourcePropertyAfterUpdate.getDefaultValue());
		assertEquals(propertyDetails.getSchema().getProperty().getType(),
				resourcePropertyAfterUpdate.getSchema().getProperty().getType()); // string/integer/boolean/float
		verifyResourcePropertyList(basicVFC, propertyDetails, newExpectedDefaultValue);
	}

	@Test
	public void deletePropertyMapTypeString() throws Exception {
		ComponentInstanceProperty resourcePropertiesFromResponse;
		PropertyReqDetails propertyDetailsTypeString = ElementFactory
				.getDefaultMapProperty(PropertyTypeEnum.STRING_MAP);
		PropertyReqDetails propertyDetailsInteger = ElementFactory.getDefaultMapProperty(PropertyTypeEnum.INTEGER_MAP);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add 2 property type list to resource
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsTypeString, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsTypeString.getPropertyType());
		assertEquals("{\"key1\":\"val1\",\"key2\":\"val2\"}", resourcePropertiesFromResponse.getDefaultValue());
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsTypeString.getSchema().getProperty().getType()); // string/integer/boolean/float
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsInteger, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsInteger.getPropertyType());
		assertEquals("{\"key1\":123,\"key2\":-456}", resourcePropertiesFromResponse.getDefaultValue());
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsInteger.getSchema().getProperty().getType());
		// Get resource and verify updated default value
		RestResponse restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(2, resource.getProperties().size());
		// Delete one resource
		RestResponse deletePropertyOfResource = AtomicOperationUtils.deletePropertyOfResource(basicVFC.getUniqueId(),
				propertyUniqueId, UserRoleEnum.DESIGNER);
		assertTrue(BaseRestUtils.STATUS_CODE_DELETE == deletePropertyOfResource.getErrorCode());
		// Get resource and verify updated default value
		restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(1, resource.getProperties().size());
		verifyResourcePropertyList(basicVFC, propertyDetailsInteger, "{\"key1\":123,\"key2\":-456}");
	}

	@Test
	public void deletePropertyMapTypeFloat() throws Exception {
		ComponentInstanceProperty resourcePropertiesFromResponse;
		PropertyReqDetails propertyDetailsTypeFloat = ElementFactory.getDefaultMapProperty(PropertyTypeEnum.FLOAT_MAP);
		PropertyReqDetails propertyDetailsInteger = ElementFactory.getDefaultMapProperty(PropertyTypeEnum.INTEGER_MAP);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add 2 property type list to resource
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsTypeFloat, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsTypeFloat.getPropertyType());
		assertEquals("{\"key1\":0.2123,\"key2\":43.545}", resourcePropertiesFromResponse.getDefaultValue());
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsTypeFloat.getSchema().getProperty().getType()); // string/integer/boolean/float
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsInteger, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsInteger.getPropertyType());
		assertEquals("{\"key1\":123,\"key2\":-456}", resourcePropertiesFromResponse.getDefaultValue());
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsInteger.getSchema().getProperty().getType());
		// Get resource and verify updated default value
		RestResponse restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(2, resource.getProperties().size());
		// Delete one resource
		RestResponse deletePropertyOfResource = AtomicOperationUtils.deletePropertyOfResource(basicVFC.getUniqueId(),
				propertyUniqueId, UserRoleEnum.DESIGNER);
		assertTrue(BaseRestUtils.STATUS_CODE_DELETE == deletePropertyOfResource.getErrorCode());
		// Get resource and verify updated default value
		restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(1, resource.getProperties().size());
		verifyResourcePropertyList(basicVFC, propertyDetailsInteger, "{\"key1\":123,\"key2\":-456}");
	}

	@Test
	public void deletePropertyMapTypeBoolean() throws Exception {
		ComponentInstanceProperty resourcePropertiesFromResponse;
		PropertyReqDetails propertyDetailsTypeBoolean = ElementFactory
				.getDefaultMapProperty(PropertyTypeEnum.BOOLEAN_MAP);
		PropertyReqDetails propertyDetailsInteger = ElementFactory.getDefaultMapProperty(PropertyTypeEnum.INTEGER_MAP);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add 2 property type list to resource
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsTypeBoolean, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsTypeBoolean.getPropertyType());
		assertEquals("{\"key1\":true,\"key2\":false}", resourcePropertiesFromResponse.getDefaultValue());
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsTypeBoolean.getSchema().getProperty().getType()); // string/integer/boolean/float
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsInteger, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsInteger.getPropertyType());
		assertEquals("{\"key1\":123,\"key2\":-456}", resourcePropertiesFromResponse.getDefaultValue());
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsInteger.getSchema().getProperty().getType());
		// Get resource and verify updated default value
		RestResponse restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(2, resource.getProperties().size());
		// Delete one resource
		RestResponse deletePropertyOfResource = AtomicOperationUtils.deletePropertyOfResource(basicVFC.getUniqueId(),
				propertyUniqueId, UserRoleEnum.DESIGNER);
		assertTrue(BaseRestUtils.STATUS_CODE_DELETE == deletePropertyOfResource.getErrorCode());
		// Get resource and verify updated default value
		restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(1, resource.getProperties().size());
		verifyResourcePropertyList(basicVFC, propertyDetailsInteger, "{\"key1\":123,\"key2\":-456}");
	}

	@Test
	public void deletePropertyMapTypeInteger() throws Exception {
		ComponentInstanceProperty resourcePropertiesFromResponse;
		PropertyReqDetails propertyDetailsTypeInteger = ElementFactory
				.getDefaultMapProperty(PropertyTypeEnum.INTEGER_MAP);
		PropertyReqDetails propertyDetailsBoolean = ElementFactory.getDefaultMapProperty(PropertyTypeEnum.BOOLEAN_MAP);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add 2 property type list to resource
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsTypeInteger, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		String propertyUniqueId = resourcePropertiesFromResponse.getUniqueId();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsTypeInteger.getPropertyType());
		assertEquals("{\"key1\":123,\"key2\":-456}", resourcePropertiesFromResponse.getDefaultValue());
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsTypeInteger.getSchema().getProperty().getType()); // string/integer/boolean/float
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsBoolean, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsBoolean.getPropertyType());
		assertEquals("{\"key1\":true,\"key2\":false}", resourcePropertiesFromResponse.getDefaultValue());
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsBoolean.getSchema().getProperty().getType());
		// Get resource and verify updated default value
		RestResponse restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(2, resource.getProperties().size());
		// Delete one resource
		RestResponse deletePropertyOfResource = AtomicOperationUtils.deletePropertyOfResource(basicVFC.getUniqueId(),
				propertyUniqueId, UserRoleEnum.DESIGNER);
		assertTrue(BaseRestUtils.STATUS_CODE_DELETE == deletePropertyOfResource.getErrorCode());
		// Get resource and verify updated default value
		restResponse = ResourceRestUtils.getResource(basicVFC.getUniqueId());
		resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		assertEquals(1, resource.getProperties().size());
		verifyResourcePropertyList(basicVFC, propertyDetailsBoolean, "{\"key1\":true,\"key2\":false}");
	}

	@Test(dataProvider = "propertiesMapDefaultValueSuccessFlow")
	public void addMapPropertyToResourceSuccessFlow(String entrySchemaType, String propertyDefaltValues,
			String expecteddefaultValues) throws Exception {
		String propertyType = "map";
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultMapProperty();
		propertyDetails.getSchema().getProperty().setType(entrySchemaType);
		propertyDetails.setPropertyDefaultValue(propertyDefaltValues);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to resource
		ComponentInstanceProperty resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, true).left().value();
		// verify properties return from response
		assertEquals(propertyType, resourcePropertiesFromResponse.getType());
		assertEquals(expecteddefaultValues, resourcePropertiesFromResponse.getDefaultValue());
		assertEquals(entrySchemaType, resourcePropertiesFromResponse.getSchema().getProperty().getType()); // string/integer/boolean/float
		verifyResourcePropertyList(basicVFC, propertyDetails, expecteddefaultValues);
	}

	@Test
	public void addMapPropertyToNonExistingResource() throws Exception {
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		propertyDetails.getSchema().getProperty().setType("integer");
		propertyDetails.setPropertyDefaultValue("{\"key1\":1 , \"key2\":2}");
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to non existing resource
		basicVFC.setUniqueId("1111111");
		RestResponse addPropertyToResourceResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER, false).right().value();
		assertTrue(addPropertyToResourceResponse.getErrorCode().equals(BaseRestUtils.STATUS_CODE_NOT_FOUND));
		ArrayList<String> variables = new ArrayList<>();
		variables.add("");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESOURCE_NOT_FOUND.name(), variables,
				addPropertyToResourceResponse.getResponse());
	}

	@Test
	public void addMaptPropertyToResourceByNonResourceOwner() throws Exception {
		PropertyReqDetails propertyDetails = ElementFactory.getDefaultListProperty();
		propertyDetails.getSchema().getProperty().setType("integer");
		propertyDetails.setPropertyDefaultValue("{\"key1\":1 , \"key2\":2}");
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add property type list to non Checked-Out resource
		RestResponse addPropertyToResourceResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetails, basicVFC, UserRoleEnum.DESIGNER2, false).right().value();
		assertTrue(addPropertyToResourceResponse.getErrorCode().equals(BaseRestUtils.STATUS_CODE_RESTRICTED_OPERATION));
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.RESTRICTED_OPERATION.name(), new ArrayList<String>(),
				addPropertyToResourceResponse.getResponse());
	}

	@Test
	public void addMapPropertyToResourcePropertyAlreadyExists() throws Exception {
		ComponentInstanceProperty resourcePropertiesFromResponse;
		PropertyReqDetails propertyDetailsTypeString = ElementFactory
				.getDefaultListProperty(PropertyTypeEnum.STRING_MAP);
		// create resource
		Resource basicVFC = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		// Add 2 property type list to resource
		resourcePropertiesFromResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsTypeString, basicVFC, UserRoleEnum.DESIGNER, true).left()
				.value();
		assertEquals(resourcePropertiesFromResponse.getType(), propertyDetailsTypeString.getPropertyType());
		assertEquals("{\"key1\":\"val1\",\"key2\":\"val2\"}", resourcePropertiesFromResponse.getDefaultValue());
		assertEquals(resourcePropertiesFromResponse.getSchema().getProperty().getType(),
				propertyDetailsTypeString.getSchema().getProperty().getType()); // string/integer/boolean/float
		// check-in and check-out resource
		RestResponse changeComponentState = LifecycleRestUtils.changeComponentState(basicVFC,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), LifeCycleStatesEnum.CHECKIN);
		assertTrue(changeComponentState.getErrorCode().equals(BaseRestUtils.STATUS_CODE_SUCCESS));
		changeComponentState = LifecycleRestUtils.changeComponentState(basicVFC,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), LifeCycleStatesEnum.CHECKOUT);
		assertTrue(changeComponentState.getErrorCode().equals(BaseRestUtils.STATUS_CODE_SUCCESS));
		// Add same property again to resource
		RestResponse addPropertyRestResponse = AtomicOperationUtils
				.addCustomPropertyToResource(propertyDetailsTypeString, basicVFC, UserRoleEnum.DESIGNER, false).right()
				.value();
		assertTrue(addPropertyRestResponse.getErrorCode().equals(BaseRestUtils.STATUS_CODE_ALREADY_EXISTS));
		ArrayList<String> variables = new ArrayList<>();
		variables.add("");
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.PROPERTY_ALREADY_EXIST.name(), variables,
				addPropertyRestResponse.getResponse());
		// verify property not deleted
		verifyResourcePropertyList(basicVFC, propertyDetailsTypeString, "{\"key1\":\"val1\",\"key2\":\"val2\"}");
	}

}
