package org.openecomp.sdc.be.datamodel.utils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import fj.data.Either;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.be.components.impl.ResponseFormatManager;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.impl.PropertyOperation;
import org.openecomp.sdc.exception.ResponseFormat;

public class PropertyValueConstraintValidationUtilTest {

	@Mock
	ApplicationDataTypeCache applicationDataTypeCache;

	@Mock
	ToscaOperationFacade toscaOperationFacade;

	@Spy
	@InjectMocks
	PropertyValueConstraintValidationUtil propertyValueConstraintValidationUtil;

	private Map<String, DataTypeDefinition> dataTypeDefinitionMap;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		ResponseFormatManager responseFormatManagerMock = Mockito.mock(ResponseFormatManager.class);
		when(responseFormatManagerMock.getResponseFormat(any())).thenReturn(new ResponseFormat());
		when(responseFormatManagerMock.getResponseFormat(any(), any())).thenReturn(new ResponseFormat());
		when(responseFormatManagerMock.getResponseFormat(any(), any(), any())).thenReturn(new ResponseFormat());
		when(propertyValueConstraintValidationUtil.getResponseFormatManager()).thenReturn(responseFormatManagerMock);

		createDataTypeMap();
	}

	@Test
	public void primitiveValueSuccessTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		Mockito.when(applicationDataTypeCache.getAll()).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("integer");
		propertyDefinition.setValue("10");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache);

		Assert.assertTrue(responseEither.isLeft());
	}

	@Test
	public void primitiveValueFailTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		Mockito.when(applicationDataTypeCache.getAll()).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("integer");
		propertyDefinition.setValue("abcd");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache);

		Assert.assertTrue(responseEither.isRight());
	}

	@Test
	public void complexWithValidValueSuccessTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		Mockito.when(applicationDataTypeCache.getAll()).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		propertyDefinition.setValue("{\"prefixlen\":\"4\"}");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache);

		Assert.assertTrue(responseEither.isLeft());
	}

	@Test
	public void complexWithValidValueFailTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		Mockito.when(applicationDataTypeCache.getAll()).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		propertyDefinition.setValue("{\"prefixlen\":\"5\"}");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache);

		Assert.assertTrue(responseEither.isRight());
	}

	@Test
	public void complexWithListWithPrimitiveValueSuccessTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		Mockito.when(applicationDataTypeCache.getAll()).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		propertyDefinition.setValue("{\"allocation_pools\":[\"slaac\"]}");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache);

		Assert.assertTrue(responseEither.isLeft());
	}

	@Test
	public void complexWithListWithPrimitiveValueFailTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		Mockito.when(applicationDataTypeCache.getAll()).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		propertyDefinition.setValue("{\"allocation_pools\":[\"value\"]}");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache);

		Assert.assertTrue(responseEither.isRight());
	}

	@Test
	public void complexWithMapWithPrimitiveValueSuccessTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		Mockito.when(applicationDataTypeCache.getAll()).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		propertyDefinition.setValue("{\"value_specs\":{\"key\":\"slaac\"}}");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache);

		Assert.assertTrue(responseEither.isLeft());
	}

	@Test
	public void complexWithMapWithPrimitiveValueFailTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		Mockito.when(applicationDataTypeCache.getAll()).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		propertyDefinition.setValue("{\"value_specs\":{\"key\":\"value\"}}");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache);

		Assert.assertTrue(responseEither.isRight());
	}

	@Test
	public void inputValidValueSuccessTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		Mockito.when(applicationDataTypeCache.getAll()).thenReturn(either);

		InputDefinition inputDefinition = new InputDefinition();
		inputDefinition.setInputPath("propetyName#ipv6_ra_mode");
		inputDefinition.setDefaultValue("slaac");
		inputDefinition.setType("string");
		ComponentInstanceProperty propertyDefinition = new ComponentInstanceProperty();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		inputDefinition.setProperties(Collections.singletonList(propertyDefinition));

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(inputDefinition), applicationDataTypeCache);

		Assert.assertTrue(responseEither.isLeft());
	}

	@Test
	public void inputValidValueFailTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		Mockito.when(applicationDataTypeCache.getAll()).thenReturn(either);

		InputDefinition inputDefinition = new InputDefinition();
		inputDefinition.setInputPath("propetyName#ipv6_ra_mode");
		inputDefinition.setDefaultValue("incorrectValue");
		ComponentInstanceProperty propertyDefinition = new ComponentInstanceProperty();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		inputDefinition.setProperties(Collections.singletonList(propertyDefinition));

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(inputDefinition), applicationDataTypeCache);

		Assert.assertTrue(responseEither.isRight());
	}

	@Test
	public void serviceConsumptionValidValueSuccessTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		Mockito.when(applicationDataTypeCache.getAll()).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		propertyDefinition.setValue("{\"ipv6_ra_mode\":\"slaac\"}");
		propertyDefinition.setName("ipv6_ra_mode");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache);

		Assert.assertTrue(responseEither.isLeft());
	}
	@Test
	public void serviceConsumptionValidValueFailTest() {
		Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> either = Either.left(dataTypeDefinitionMap);
		Mockito.when(applicationDataTypeCache.getAll()).thenReturn(either);

		PropertyDefinition propertyDefinition = new PropertyDefinition();
		propertyDefinition.setType("org.openecomp.datatypes.heat.network.neutron.Subnet");
		propertyDefinition.setValue("{\"ipv6_ra_mode\":\"incorrectValue\"}");
		propertyDefinition.setName("ipv6_ra_mode");

		Either<Boolean, ResponseFormat> responseEither =
				propertyValueConstraintValidationUtil.validatePropertyConstraints(
						Collections.singletonList(propertyDefinition), applicationDataTypeCache);

		Assert.assertTrue(responseEither.isRight());
	}

	private void createDataTypeMap() {
		Type constraintType = new TypeToken<PropertyConstraint>() {}.getType();
		Type typeOfHashMap = new TypeToken<Map<String, DataTypeDefinition>>() { }.getType();
		Gson gson = new GsonBuilder().registerTypeAdapter(constraintType,
				new PropertyOperation.PropertyConstraintDeserialiser()).create();

		dataTypeDefinitionMap = gson.fromJson(readFile(), typeOfHashMap);

		DataTypeDefinition dataTypeDefinition = dataTypeDefinitionMap.get("org.openecomp.datatypes.heat.network"
				+ ".neutron.Subnet");

		PropertyDefinition mapProperty = null;
		PropertyDefinition listProperty = null;
		List<PropertyConstraint> constraints = null;
		for (PropertyDefinition propertyDefinition : dataTypeDefinition.getProperties()) {
			if ("value_specs".equals(propertyDefinition.getName())) {
				mapProperty = propertyDefinition;
			} else if ("allocation_pools".equals(propertyDefinition.getName())) {
				listProperty = propertyDefinition;
			} else if ("ipv6_ra_mode".equals(propertyDefinition.getName())) {
				constraints = propertyDefinition.getConstraints();
			}
		}

		PropertyDefinition definition = new PropertyDefinition(mapProperty.getSchema().getProperty());
		definition.setConstraints(constraints);
		mapProperty.getSchema().setProperty(definition);

		definition = new PropertyDefinition(listProperty.getSchema().getProperty());
		definition.setConstraints(constraints);
		listProperty.getSchema().setProperty(definition);
	}

	private static String readFile() {
		StringBuilder stringBuilder = new StringBuilder();
		File file = new File(Objects.requireNonNull(
				PropertyValueConstraintValidationUtilTest.class.getClassLoader().getResource("types/datatypes"
						+ "/constraintTest.json")).getFile());
		Path logFile = Paths.get(file.getAbsolutePath());
		try (BufferedReader reader = Files.newBufferedReader(logFile, StandardCharsets.UTF_8)) {
			reader.lines().forEach(stringBuilder::append);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
			e.printStackTrace();
		}
		return stringBuilder.toString();
	}

}
