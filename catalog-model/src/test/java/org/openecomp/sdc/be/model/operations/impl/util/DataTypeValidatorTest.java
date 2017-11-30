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

package org.openecomp.sdc.be.model.operations.impl.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.be.model.tosca.converters.ListConverter;
import org.openecomp.sdc.be.model.tosca.converters.MapConverter;
import org.openecomp.sdc.be.model.tosca.validators.DataTypeValidatorConverter;
import org.openecomp.sdc.be.model.tosca.validators.ListValidator;
import org.openecomp.sdc.be.model.tosca.validators.MapValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import fj.data.Either;

public class DataTypeValidatorTest {
	private static Logger log = LoggerFactory.getLogger(DataTypeValidatorTest.class.getName());
	private static Gson gson = new Gson();

	DataTypeValidatorConverter dataTypeValidator = DataTypeValidatorConverter.getInstance();

	@Test
	public void testDerivedFromPrimitiveEmptyValue() {

		Map<String, DataTypeDefinition> allDataTypes = new HashMap<String, DataTypeDefinition>();
		allDataTypes.put("integer", getPrimitiveDataType("integer"));

		DataTypeDefinition fromIntegerType = buildDerivedFromIntegerType();
		ImmutablePair<JsonElement, Boolean> validate = dataTypeValidator.validateAndUpdate("", fromIntegerType,
				allDataTypes);

		assertTrue("check result is valid", validate.right.booleanValue());
		assertEquals("check value is the same as sent", null, validate.left);

		validate = dataTypeValidator.validateAndUpdate(null, fromIntegerType, allDataTypes);

		assertTrue("check result is valid", validate.right.booleanValue());
		assertEquals("check value is the same as sent", null, validate.left);

		validate = dataTypeValidator.validateAndUpdate("88", fromIntegerType, allDataTypes);

		assertTrue("check result is valid", validate.right.booleanValue());
		assertEquals("check value is the same as sent", "88", validate.left.toString());

	}

	@Test
	public void testCompositeWithParameterDerivedFromPrimitiveEmptyValue() {

		DataTypeDefinition derivedFromIntegerType = buildDerivedFromIntegerType();
		Map<String, DataTypeDefinition> allDataTypes = new HashMap<String, DataTypeDefinition>();
		allDataTypes.put("myinteger", derivedFromIntegerType);

		DataTypeDefinition personDataType = buildPersonDataType();

		Person person = new Person("my address", 32);
		String json = gson.toJson(person);
		log.debug(json);

		ImmutablePair<JsonElement, Boolean> validate = dataTypeValidator.validateAndUpdate(json, personDataType,
				allDataTypes);
		assertTrue("check valid value", validate.right.booleanValue());

		person = new Person("my address", 32);
		json = gson.toJson(person);
		json = json.replace("32", "32a");
		log.debug(json);

		validate = dataTypeValidator.validateAndUpdate(json, personDataType, allDataTypes);
		assertFalse("check valid value", validate.right.booleanValue());

	}

	@Test
	public void testCompositeWithEmptyListValue() {

		DataTypeDefinition dataTypeDefinition = buildCredentialDataType();

		String[] strArr = {};
		List<String> strList = Arrays.asList(strArr);

		// Check empty list
		Credential credential = new Credential("protcol<br>>", 5, "token_type", "token", null, "user", true, strList);
		City mycity = new City("myadd<br><<br>", 55);
		credential.setMycity(mycity);

		String json = gson.toJson(credential);
		log.debug(json);

		Map<String, DataTypeDefinition> allDataTypes = new HashMap<String, DataTypeDefinition>();
		DataTypeDefinition cityDataType = buildCityDataType();
		allDataTypes.put("city", cityDataType);

		ImmutablePair<JsonElement, Boolean> validate = dataTypeValidator.validateAndUpdate(json, dataTypeDefinition,
				allDataTypes);
		assertTrue("check valid value", validate.right.booleanValue());

		Credential credentialRes = gson.fromJson(validate.left.toString(), Credential.class);
		assertEquals("check empty list", 0, credentialRes.getMylist().size());

		log.debug("Result is = {}", validate.left.toString());

	}

	@Test
	public void testCompositeWithListNullValue() {
		DataTypeDefinition dataTypeDefinition = buildCredentialDataType();

		Map<String, DataTypeDefinition> allDataTypes = new HashMap<String, DataTypeDefinition>();
		DataTypeDefinition cityDataType = buildCityDataType();
		allDataTypes.put("city", cityDataType);

		// Check list is NULL
		Credential credential = new Credential("protcol<br>>", 5, "token_type", "token", null, "user", true, null);
		City mycity = new City("myadd<br><<br>", 55);
		credential.setMycity(mycity);

		String json = gson.toJson(credential);

		ImmutablePair<JsonElement, Boolean> validate = dataTypeValidator.validateAndUpdate(json, dataTypeDefinition,
				allDataTypes);
		assertTrue("check valid value", validate.right.booleanValue());

		Credential credentialRes = gson.fromJson(validate.left.toString(), Credential.class);
		assertNull("check list is null", credentialRes.getMylist());
		log.debug("Result is = {}", validate.left.toString());

	}

	@Test
	public void testCompositeWithUserNullValue() {
		DataTypeDefinition dataTypeDefinition = buildCredentialDataType();

		Map<String, DataTypeDefinition> allDataTypes = new HashMap<String, DataTypeDefinition>();
		DataTypeDefinition cityDataType = buildCityDataType();
		allDataTypes.put("city", cityDataType);

		// Check user is null
		Credential credential = new Credential("protcol<br>>", 5, "token_type", "token", null, null, true, null);
		City mycity = new City("myadd<br><<br>", 55);
		credential.setMycity(mycity);

		String json = gson.toJson(credential);

		ImmutablePair<JsonElement, Boolean> validate = dataTypeValidator.validateAndUpdate(json, dataTypeDefinition,
				allDataTypes);
		assertTrue("check valid value", validate.right.booleanValue());

		Credential credentialRes = gson.fromJson(validate.left.toString(), Credential.class);
		assertNull("check list is null", credentialRes.getUser());
		log.debug("Result is = {}", validate.left.toString());
	}

	@Test
	public void testCompositeWithEmptyUserValue() {

		DataTypeDefinition dataTypeDefinition = buildCredentialDataType();

		Map<String, DataTypeDefinition> allDataTypes = new HashMap<String, DataTypeDefinition>();
		DataTypeDefinition cityDataType = buildCityDataType();
		allDataTypes.put("city", cityDataType);
		// Check user is empty
		Credential credential = new Credential("protcol<br>>", 5, "token_type", "token", null, "", true, null);
		City mycity = new City("myadd<br><<br>", 55);
		credential.setMycity(mycity);

		String json = gson.toJson(credential);
		log.debug(json);

		ImmutablePair<JsonElement, Boolean> validate = dataTypeValidator.validateAndUpdate(json, dataTypeDefinition,
				allDataTypes);
		assertTrue("check valid value", validate.right.booleanValue());

		Credential credentialRes = gson.fromJson(validate.left.toString(), Credential.class);
		assertNotNull("check list is not null", credentialRes.getUser());
		assertEquals("check user is empty", "", credentialRes.getUser());
		log.debug("Result is = {}", validate.left.toString());

	}

	@Test
	public void testCompositeWithSumNullValue() {
		DataTypeDefinition dataTypeDefinition = buildCredentialDataType();

		Map<String, DataTypeDefinition> allDataTypes = new HashMap<String, DataTypeDefinition>();
		DataTypeDefinition cityDataType = buildCityDataType();
		allDataTypes.put("city", cityDataType);

		// Check user is null
		Credential credential = new Credential("protcol<br>>", null, "token_type", "token", null, null, true, null);
		City mycity = new City("myadd<br><<br>", 55);
		credential.setMycity(mycity);

		String json = gson.toJson(credential);

		ImmutablePair<JsonElement, Boolean> validate = dataTypeValidator.validateAndUpdate(json, dataTypeDefinition,
				allDataTypes);
		assertTrue("check valid value", validate.right.booleanValue());

		Credential credentialRes = gson.fromJson(validate.left.toString(), Credential.class);
		assertNull("check list is null", credentialRes.getSum());
		log.debug("Result is = {}", validate.left.toString());
	}

	@Test
	public void testInvalidJson() {
		DataTypeDefinition dataTypeDefinition = buildCredentialDataType();

		Map<String, DataTypeDefinition> allDataTypes = new HashMap<String, DataTypeDefinition>();
		DataTypeDefinition cityDataType = buildCityDataType();
		allDataTypes.put("city", cityDataType);

		// Check user is null
		Credential credential = new Credential("protcol<br>>", null, "token_type", "token", null, null, true, null);
		City mycity = new City("myadd<br><<br>", 55);
		credential.setMycity(mycity);

		String json = gson.toJson(credential);

		json += "fdfd";

		ImmutablePair<JsonElement, Boolean> validate = dataTypeValidator.validateAndUpdate(json, dataTypeDefinition,
				allDataTypes);
		assertFalse("check valid value", validate.right.booleanValue());

	}

	@Test
	public void testInvalidInnerValue() {

		DataTypeDefinition dataTypeDefinition = buildCredentialDataType();

		Map<String, DataTypeDefinition> allDataTypes = new HashMap<String, DataTypeDefinition>();
		DataTypeDefinition cityDataType = buildCityDataType();
		allDataTypes.put("city", cityDataType);

		// Check user is null
		Credential credential = new Credential("protcol<br>>", null, "token_type", "token", null, null, true, null);
		City mycity = new City("myadd<br><<br>", 55);
		credential.setMycity(mycity);

		String json = gson.toJson(credential);

		json = json.replace("55", "a55b");

		ImmutablePair<JsonElement, Boolean> validate = dataTypeValidator.validateAndUpdate(json, dataTypeDefinition,
				allDataTypes);
		assertFalse("check valid value", validate.right.booleanValue());

	}

	@Test
	public void testInvalidInnerJson() {

		DataTypeDefinition dataTypeDefinition = buildCredentialDataType();

		Map<String, DataTypeDefinition> allDataTypes = new HashMap<String, DataTypeDefinition>();
		DataTypeDefinition cityDataType = buildCityDataType();
		allDataTypes.put("city", cityDataType);

		// Check user is null
		Credential credential = new Credential("protcol<br>>", null, "token_type", "token", null, null, true, null);
		City mycity = new City("", null);

		credential.setMycity(mycity);

		String json = gson.toJson(credential);

		json = json.replace("{\"address\":\"\"}", "scalar");

		ImmutablePair<JsonElement, Boolean> validate = dataTypeValidator.validateAndUpdate(json, dataTypeDefinition,
				allDataTypes);
		assertFalse("check valid value", validate.right.booleanValue());

	}

	@Test
	public void testInvalidPropertyJson() {

		DataTypeDefinition dataTypeDefinition = buildCredentialDataType();

		Map<String, DataTypeDefinition> allDataTypes = new HashMap<String, DataTypeDefinition>();
		DataTypeDefinition cityDataType = buildCityDataType();
		allDataTypes.put("city", cityDataType);

		// Check user is null
		Credential credential = new Credential("protcol<br>>", null, "token_type", "token", null, null, true, null);
		City mycity = new City("myadd<br><<br>", 55);
		credential.setMycity(mycity);

		String json = gson.toJson(credential);

		json = json.replace("55", "a55b");

		ImmutablePair<JsonElement, Boolean> validate = dataTypeValidator.validateAndUpdate(json, dataTypeDefinition,
				allDataTypes);
		assertFalse("check valid value", validate.right.booleanValue());

	}

	@Test
	public void testCompositeDataTypeWithInternalComposite() {

		DataTypeDefinition dataTypeDefinition = buildCredentialDataType();

		String[] strArr = { "aaa", "bbb", "c<br>dcc" };
		List<String> strList = Arrays.asList(strArr);

		Credential credential = new Credential("protcol<br>>", 5, "token_type", "token", null, "user", true, strList);
		City mycity = new City("myadd<br><<br>", 55);
		credential.setMycity(mycity);

		String json = gson.toJson(credential);

		Map<String, DataTypeDefinition> allDataTypes = new HashMap<String, DataTypeDefinition>();
		DataTypeDefinition cityDataType = buildCityDataType();
		allDataTypes.put("city", cityDataType);

		ImmutablePair<JsonElement, Boolean> validate = dataTypeValidator.validateAndUpdate(json, dataTypeDefinition,
				allDataTypes);
		assertTrue("check valid value", validate.right.booleanValue());

		log.debug("Result is = {}", validate.left.toString());

	}

	@Test
	public void testMapValidator() {

		MapValidator validator = new MapValidator();
		Gson gson = new Gson();
		// Happy Scenarios
		// 1 - Map<String,Integer> check OK
		Map<String, Integer> map_1 = new HashMap<>();
		map_1.put("key1", 2);
		map_1.put("key2", 3);
		String value = gson.toJson(map_1);
		String innerType = "integer";
		assertTrue("Test Map validation with inner integer type", validator.isValid(value, innerType, null));

		// 2 - Map<String,Boolean> check OK
		Map<String, Boolean> map_2 = new HashMap<>();
		map_2.put("key1", true);
		map_2.put("key2", false);
		value = gson.toJson(map_2);
		innerType = "boolean";
		assertTrue("Test Map validation with inner boolean type", validator.isValid(value, innerType, null));

		// 3 - give integer with quotes
		innerType = "integer";
		value = "{\"key1\":\"5\",\"key2\":\"7\"}";
		assertTrue("Test Map validation with inner integer type, but qouted values",
				validator.isValid(value, innerType, null));

		// 4 - empty default value
		innerType = "float";
		value = "";
		assertTrue("Test Map validation with inner float type", validator.isValid(value, innerType, null));

		// Faulty Scenarios
		// 5 - mismatch in data type
		value = gson.toJson(map_1);
		innerType = "boolean";
		assertFalse("Test Map faulty validation with inner boolean type", validator.isValid(value, innerType, null));
		// 6 - mismatch in data type
		value = gson.toJson(map_2);
		innerType = "integer";
		assertFalse("Test Map faulty validation with inner integer type", validator.isValid(value, innerType, null));

	}

	@Test
	public void testMapConverter() {

		MapConverter converter = new MapConverter();
		Gson gson = new Gson();
		// Happy Scenarios
		Map<String, String> map_1 = new HashMap<>();
		Map<String, String> resMap_1 = new HashMap<>();

		// 1 - check Spaces eliminated + html square brackets eliminated
		map_1.put("key1", "<b>test</b>");
		map_1.put("key2", "        test");
		resMap_1.put("key1", "test");
		resMap_1.put("key2", " test");
		String value = gson.toJson(map_1);
		String expectedVal = gson.toJson(resMap_1);
		String innerType = "string";
		assertEquals("Test Map validation with inner string type", expectedVal,
				converter.convert(value, innerType, null));

		// 2 - float converter
		innerType = "float";
		value = "{\"key1\":0.4545,\"key2\":0.2f}";
		expectedVal = "{\"key1\":0.4545,\"key2\":0.2}";
		assertEquals("Test Map validation with inner float type", expectedVal,
				converter.convert(value, innerType, null));

		// 3 - check default empty value converter
		innerType = "float";
		value = "";
		expectedVal = "";
		assertEquals("Test Map validation with inner float type", expectedVal,
				converter.convert(value, innerType, null));

		// 4 - invalid json
		// 3 - check default empty value converter
		innerType = "float";
		value = "{1345234556@#(";
		expectedVal = null;
		assertEquals("Test Map validation with inner float type", expectedVal,
				converter.convert(value, innerType, null));

	}

	@Test
	public void testCompositeDataTypeWithMapComposite() {

		DataTypeDefinition fileDataTypeDefinition = buildFileDataType();
		Map<String, DataTypeDefinition> allDataTypes = new HashMap<String, DataTypeDefinition>();
		DataTypeDefinition cityDataType = buildCityDataType();
		allDataTypes.put("city", cityDataType);

		MyFile myFile = new MyFile();
		myFile.setAge(88);
		Map<String, City> attributes = new HashMap<>();
		attributes.put("key1", new City("address1<br>", 11));
		attributes.put("key2", new City("address2<br>", 22));
		myFile.setAttributes(attributes);

		String str = gson.toJson(myFile);
		log.debug(str);

		ImmutablePair<JsonElement, Boolean> convert = dataTypeValidator.validateAndUpdate(str, fileDataTypeDefinition,
				allDataTypes);

		assertTrue("check map converter succeed", convert.right);

		JsonElement convertedValue = convert.left;

		log.debug("{}", convertedValue);
		MyFile fromJson = gson.fromJson(convertedValue, MyFile.class);

		assertEquals("check age", 88, fromJson.getAge().intValue());
		assertEquals("check address 1", "address1", fromJson.getAttributes().get("key1").getAddress());
		assertEquals("check address 2", "address2", fromJson.getAttributes().get("key2").getAddress());

	}

	@Test
	public void testMapConverterWithComplexInnerType() {

		Map<String, DataTypeDefinition> allDataTypes = new HashMap<String, DataTypeDefinition>();
		DataTypeDefinition credentialDataTypeDefinition = buildCredentialDataType();
		DataTypeDefinition cityDataType = buildCityDataType();
		allDataTypes.put("city", cityDataType);
		allDataTypes.put("credential", credentialDataTypeDefinition);

		Gson gson = new Gson();
		// Happy Scenarios
		Map<String, Object> map_1 = new HashMap<>();

		// 1 - check Spaces eliminated + html square brackets eliminated

		String[] strArr = { "aaa", "bbb", "c<br>dcc" };
		List<String> strList = Arrays.asList(strArr);
		Credential credential1 = new Credential("protocol;:,.\"<br>>", 5, "token_type", "token", null, "user", true,
				strList);
		City mycity1 = new City("myadd<br><<br>", 55);
		credential1.setMycity(mycity1);

		Credential credential2 = new Credential("protocol;:,.\"<br>>", 5, "token_type", "token", null, "user", true,
				strList);
		City mycity2 = new City("myadd<br><<br>", 66);
		credential2.setMycity(mycity2);

		map_1.put("key1", credential1);
		map_1.put("key2", credential2);

		String str = gson.toJson(map_1);
		log.debug(str);

		MapConverter mapConverter = new MapConverter();
		Either<String, Boolean> convert = mapConverter.convertWithErrorResult(str, "credential", allDataTypes);

		assertTrue("check map converter succeed", convert.isLeft());

		String convertedValue = convert.left().value();

		Type type = new TypeToken<Map<String, Credential>>() {
		}.getType();

		Map<String, Credential> fromJson = gson.fromJson(convertedValue, type);

		Credential actualCredential1 = fromJson.get("key1");
		assertEquals("check sum", 5, actualCredential1.getSum().intValue());
		assertEquals("check protocol", "protocol;:,.\">", actualCredential1.getProtocol());
		String[] convertedStrArr = { "aaa", "bbb", "cdcc" };
		List<String> convertedStrList = Arrays.asList(convertedStrArr);
		assertEquals("check list", convertedStrList, actualCredential1.getMylist());

		assertEquals("check city address", "myadd<", actualCredential1.getMycity().getAddress());
		assertEquals("check city address", 55, actualCredential1.getMycity().getAge().intValue());

		Credential actualCredential2 = fromJson.get("key2");
		assertEquals("check city address", 66, actualCredential2.getMycity().getAge().intValue());

	}

	@Test
	public void testListConverterWithComplexInnerType() {

		Map<String, DataTypeDefinition> allDataTypes = new HashMap<String, DataTypeDefinition>();
		DataTypeDefinition credentialDataTypeDefinition = buildCredentialDataType();
		DataTypeDefinition cityDataType = buildCityDataType();
		allDataTypes.put("city", cityDataType);
		allDataTypes.put("credential", credentialDataTypeDefinition);

		Gson gson = new Gson();

		List<Object> list = buildListOf2CredentialObjects();

		String str = gson.toJson(list);
		log.debug(str);

		ListConverter listConverter = new ListConverter();

		Either<String, Boolean> convert = listConverter.convertWithErrorResult(str, "credential", allDataTypes);

		assertTrue("check map converter succeed", convert.isLeft());

		String convertedValue = convert.left().value();

		validateListOfCredential(gson, convertedValue);

		list.add(null);

		str = gson.toJson(list);
		log.debug(str);

		convert = listConverter.convertWithErrorResult(str, "credential", allDataTypes);

		assertTrue("check map converter succeed", convert.isLeft());

		validateListOfCredential(gson, convertedValue);
	}

	@Test
	public void testListValidatorWithComplexInnerType() {

		Map<String, DataTypeDefinition> allDataTypes = new HashMap<String, DataTypeDefinition>();
		DataTypeDefinition credentialDataTypeDefinition = buildCredentialDataType();
		DataTypeDefinition cityDataType = buildCityDataType();
		allDataTypes.put("city", cityDataType);
		allDataTypes.put("credential", credentialDataTypeDefinition);

		Gson gson = new Gson();
		// Happy Scenarios
		List<Object> list = buildListOf2CredentialObjects();

		String str = gson.toJson(list);
		log.debug(str);

		ListValidator listValidator = new ListValidator();

		boolean isValid = listValidator.isValid(str, "credential", allDataTypes);

		assertTrue("check valid value", isValid);

		String badStr = str.replace("protocol", "protocol1");

		isValid = listValidator.isValid(badStr, "credential", allDataTypes);

		assertFalse("check valid value", isValid);

		badStr = str.replace("55", "\"aa\"");

		isValid = listValidator.isValid(badStr, "credential", allDataTypes);

		assertFalse("check valid value", isValid);

	}

	private List<Object> buildListOf2CredentialObjects() {
		List<Object> list = new ArrayList<>();

		String[] strArr = { "aaa", "bbb", "c<br>dcc" };
		List<String> strList = Arrays.asList(strArr);
		Credential credential1 = new Credential("protocol.,\":;<br>>", 5, "token_type", "token", null, "user", true,
				strList);
		City mycity1 = new City("myadd<br><<br>", 55);
		credential1.setMycity(mycity1);

		Credential credential2 = new Credential("protocol.,\":;<br>>", 5, "token_type", "token", null, "user", true,
				strList);
		City mycity2 = new City("myadd<br><<br>", 66);
		credential2.setMycity(mycity2);

		list.add(credential1);
		list.add(credential2);
		return list;
	}

	private void validateListOfCredential(Gson gson, String convertedValue) {

		log.debug(convertedValue);
		Type type = new TypeToken<List<Credential>>() {
		}.getType();

		List<Credential> fromJson = gson.fromJson(convertedValue, type);

		assertEquals("check list size", 2, fromJson.size());

		// Credential actualCredential1 = gson.fromJson(list.get(0).toString(),
		// Credential.class);
		Credential actualCredential1 = fromJson.get(0);
		assertEquals("check sum", 5, actualCredential1.getSum().intValue());
		assertEquals("check protocol", "protocol.,\":;>", actualCredential1.getProtocol());
		String[] convertedStrArr = { "aaa", "bbb", "cdcc" };
		List<String> convertedStrList = Arrays.asList(convertedStrArr);
		assertEquals("check list", convertedStrList, actualCredential1.getMylist());

		assertEquals("check city address", "myadd<", actualCredential1.getMycity().getAddress());
		assertEquals("check city address", 55, actualCredential1.getMycity().getAge().intValue());

		// Credential actualCredential2 = gson.fromJson(list.get(1).toString(),
		// Credential.class);
		Credential actualCredential2 = fromJson.get(1);
		assertEquals("check city address", 66, actualCredential2.getMycity().getAge().intValue());
	}

	private DataTypeDefinition buildCredentialDataType() {
		DataTypeDefinition dataTypeDefinition = new DataTypeDefinition();
		dataTypeDefinition.setName("datatype.1");
		List<PropertyDefinition> properties = new ArrayList<>();
		PropertyDefinition propertyDefinition1 = new PropertyDefinition();
		propertyDefinition1.setName("sum");
		propertyDefinition1.setType(ToscaPropertyType.INTEGER.getType());
		PropertyDefinition propertyDefinition2 = new PropertyDefinition();
		propertyDefinition2.setName("protocol");
		propertyDefinition2.setType(ToscaPropertyType.STRING.getType());
		PropertyDefinition propertyDefinition3 = new PropertyDefinition();
		propertyDefinition3.setName("token_type");
		propertyDefinition3.setType(ToscaPropertyType.STRING.getType());
		PropertyDefinition propertyDefinition4 = new PropertyDefinition();
		propertyDefinition4.setName("token");
		propertyDefinition4.setType(ToscaPropertyType.STRING.getType());
		PropertyDefinition propertyDefinition5 = new PropertyDefinition();
		propertyDefinition5.setName("keys");
		propertyDefinition5.setType(ToscaPropertyType.MAP.getType());
		PropertyDefinition propertyDefinition6 = new PropertyDefinition();
		propertyDefinition6.setName("mylist");
		propertyDefinition6.setType(ToscaPropertyType.LIST.getType());
		SchemaDefinition entrySchema = new SchemaDefinition();
		PropertyDataDefinition property = new PropertyDataDefinition();
		property.setType("string");
		entrySchema.setProperty(property);
		propertyDefinition6.setSchema(entrySchema);
		PropertyDefinition propertyDefinition7 = new PropertyDefinition();
		propertyDefinition7.setName("user");
		propertyDefinition7.setType(ToscaPropertyType.STRING.getType());
		PropertyDefinition propertyDefinition8 = new PropertyDefinition();
		propertyDefinition8.setName("isMandatory");
		propertyDefinition8.setType(ToscaPropertyType.BOOLEAN.getType());

		PropertyDefinition propertyDefinition9 = new PropertyDefinition();
		propertyDefinition9.setName("mycity");
		propertyDefinition9.setType("city");

		properties.add(propertyDefinition1);
		properties.add(propertyDefinition2);
		properties.add(propertyDefinition3);
		properties.add(propertyDefinition4);
		properties.add(propertyDefinition5);
		properties.add(propertyDefinition6);
		properties.add(propertyDefinition7);
		properties.add(propertyDefinition8);
		properties.add(propertyDefinition9);

		dataTypeDefinition.setProperties(properties);
		return dataTypeDefinition;
	}

	private static DataTypeDefinition buildCityDataType() {
		DataTypeDefinition cityDataType = new DataTypeDefinition();
		cityDataType.setName("city");
		List<PropertyDefinition> cityProperties = new ArrayList<>();
		PropertyDefinition cityPropertyDefinition1 = new PropertyDefinition();
		cityPropertyDefinition1.setName("age");
		cityPropertyDefinition1.setType(ToscaPropertyType.INTEGER.getType());
		PropertyDefinition cityPropertyDefinition2 = new PropertyDefinition();
		cityPropertyDefinition2.setName("address");
		cityPropertyDefinition2.setType(ToscaPropertyType.STRING.getType());

		cityProperties.add(cityPropertyDefinition1);
		cityProperties.add(cityPropertyDefinition2);

		cityDataType.setProperties(cityProperties);
		return cityDataType;
	}

	private static DataTypeDefinition buildPersonDataType() {
		DataTypeDefinition personDataType = new DataTypeDefinition();
		personDataType.setName("person");
		List<PropertyDefinition> personProperties = new ArrayList<>();
		PropertyDefinition personPropertyDefinition1 = new PropertyDefinition();
		personPropertyDefinition1.setName("age");
		personPropertyDefinition1.setType("myinteger");
		PropertyDefinition personPropertyDefinition2 = new PropertyDefinition();
		personPropertyDefinition2.setName("address");
		personPropertyDefinition2.setType(ToscaPropertyType.STRING.getType());

		personProperties.add(personPropertyDefinition1);
		personProperties.add(personPropertyDefinition2);

		personDataType.setProperties(personProperties);
		return personDataType;
	}

	private static DataTypeDefinition buildFileDataType() {
		DataTypeDefinition fileDataType = new DataTypeDefinition();
		fileDataType.setName("file");
		List<PropertyDefinition> fileProperties = new ArrayList<>();
		PropertyDefinition filePropertyDefinition1 = new PropertyDefinition();
		filePropertyDefinition1.setName("age");
		filePropertyDefinition1.setType("integer");

		PropertyDefinition filePropertyDefinition2 = new PropertyDefinition();
		filePropertyDefinition2.setName("attributes");
		filePropertyDefinition2.setType(ToscaPropertyType.MAP.getType());

		fileProperties.add(filePropertyDefinition1);
		fileProperties.add(filePropertyDefinition2);

		SchemaDefinition entrySchema = new SchemaDefinition();
		PropertyDataDefinition property = new PropertyDataDefinition();
		property.setType("city");
		entrySchema.setProperty(property);
		filePropertyDefinition2.setSchema(entrySchema);

		fileDataType.setProperties(fileProperties);
		return fileDataType;
	}

	private static DataTypeDefinition getPrimitiveDataType(String type) {

		DataTypeDefinition derivedFrom = new DataTypeDefinition();
		derivedFrom.setName(type);

		return derivedFrom;

	}

	private static DataTypeDefinition buildDerivedFromIntegerType() {

		DataTypeDefinition derivedFrom = getPrimitiveDataType("integer");

		DataTypeDefinition myIntegerDataType = new DataTypeDefinition();
		myIntegerDataType.setDerivedFrom(derivedFrom);

		myIntegerDataType.setName("myinteger");

		return myIntegerDataType;
	}

	public static class MyFile {

		Integer age;

		Map<String, City> attributes;

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

		public Map<String, City> getAttributes() {
			return attributes;
		}

		public void setAttributes(Map<String, City> attributes) {
			this.attributes = attributes;
		}

	}

	public static class City {

		String address;
		Integer age;

		public City(String address, Integer age) {
			super();
			this.address = address;
			this.age = age;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

	}

	public static class Person {

		String address;
		Integer age;

		public Person(String address, Integer age) {
			super();
			this.address = address;
			this.age = age;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public Integer getAge() {
			return age;
		}

		public void setAge(Integer age) {
			this.age = age;
		}

		@Override
		public String toString() {
			return "Person [address=" + address + ", age=" + age + "]";
		}

	}

	public static class Credential {

		String protocol;
		Integer sum;
		String token_type;
		String token;
		Map<String, String> keys;
		String user;
		Boolean isMandatory;
		List<String> mylist;
		City mycity;

		public Credential(String protocol, Integer sum, String token_type, String token, Map<String, String> keys,
				String user, Boolean isMandatory, List<String> mylist) {
			super();
			this.protocol = protocol;
			this.sum = sum;
			this.token_type = token_type;
			this.token = token;
			this.keys = keys;
			this.user = user;
			this.isMandatory = isMandatory;
			this.mylist = mylist;
		}

		public String getProtocol() {
			return protocol;
		}

		public void setProtocol(String protocol) {
			this.protocol = protocol;
		}

		public String getToken_type() {
			return token_type;
		}

		public void setToken_type(String token_type) {
			this.token_type = token_type;
		}

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public Map<String, String> getKeys() {
			return keys;
		}

		public void setKeys(Map<String, String> keys) {
			this.keys = keys;
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public Boolean getIsMandatory() {
			return isMandatory;
		}

		public void setIsMandatory(Boolean isMandatory) {
			this.isMandatory = isMandatory;
		}

		public Integer getSum() {
			return sum;
		}

		public void setSum(Integer sum) {
			this.sum = sum;
		}

		public List<String> getMylist() {
			return mylist;
		}

		public void setMylist(List<String> mylist) {
			this.mylist = mylist;
		}

		public City getMycity() {
			return mycity;
		}

		public void setMycity(City mycity) {
			this.mycity = mycity;
		}

		@Override
		public String toString() {
			return "Credential [protocol=" + protocol + ", token_type=" + token_type + ", token=" + token + ", keys="
					+ keys + ", user=" + user + ", isMandatory=" + isMandatory + "]";
		}

	}

}
