package org.openecomp.sdc.ci.tests.verificator;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaInputsTopologyTemplateDefinition;
import org.openecomp.sdc.tosca.parser.api.ISdcCsarHelper;
import org.openecomp.sdc.toscaparser.api.elements.Metadata;
import org.openecomp.sdc.toscaparser.api.parameters.Input;

import com.aventstack.extentreports.Status;

import fj.data.Either;

public class ToscaValidation {

	
	/**
	 * @param expectedToscaDefinition - expected toscaDefinition object
	 * @param actualToscaDefinition - actual toscaDefinition object
	 * @return true if all validation success else return error map
	 */
	public static Either<Boolean, Map<String, Object>> resourceToscaMetadataValidator(ToscaDefinition expectedToscaDefinition, ToscaDefinition actualToscaDefinition){
		
		SetupCDTest.getExtendTest().log(Status.INFO, "Going to validate resource TOSCA metadata...");
		Map<String, String> expectedMetadata = expectedToscaDefinition.getMetadata();
		Map<String, String> actualMetadata = actualToscaDefinition.getMetadata();
		Either<Boolean, Map<String, Object>> resourceToscaMetadataValidator = compareStringMapData(expectedMetadata, actualMetadata);
		if(resourceToscaMetadataValidator.isLeft()){
			SetupCDTest.getExtendTest().log(Status.INFO, "Resource TOSCA metadata verification success");
		}else{
			SetupCDTest.getExtendTest().log(Status.ERROR, "Resource TOSCA metadata verification failed" + resourceToscaMetadataValidator.right().value());
		}
		return resourceToscaMetadataValidator;
	}

	public static Boolean resourceToscaNodeTemplateMetadataValidator(Map<String, Map<String, String>> expectedMetadata, ToscaDefinition actualToscaDefinition){
		boolean isTestFailed = true;
		for(String nodeTemplateName : expectedMetadata.keySet()){
			Either<Boolean,Map<String,Object>> serviceToscaMetadataValidator = componentToscaNodeTemplateMetadataValidator(expectedMetadata.get(nodeTemplateName), actualToscaDefinition, nodeTemplateName, ComponentTypeEnum.RESOURCE, nodeTemplateName);
			if(serviceToscaMetadataValidator.left().value() == false){
				isTestFailed = false;
			}
		}
		return isTestFailed;
	}
	
	public static Either<Boolean, Map<String, Object>> serviceToscaMetadataValidator(Map<String, String> expectedMetadata, ToscaDefinition actualToscaDefinition){
		
		SetupCDTest.getExtendTest().log(Status.INFO, "Going to validate service TOSCA metadata...");
		Map<String, String> actualMetadata = actualToscaDefinition.getMetadata();
		Either<Boolean,Map<String,Object>> serviceToscaMetadataValidator = compareStringMapData(expectedMetadata, actualMetadata);
		if(serviceToscaMetadataValidator.isLeft()){
			SetupCDTest.getExtendTest().log(Status.INFO, "Service TOSCA metadata verification success");
		}else{
			SetupCDTest.getExtendTest().log(Status.ERROR, "Service TOSCA metadata verification failed" + serviceToscaMetadataValidator.right().value().toString());
		}
		return serviceToscaMetadataValidator;
	}

	public static Either<Boolean, Map<String, Object>> componentToscaNodeTemplateMetadataValidator(Map<String, String> expectedMetadata, ToscaDefinition actualToscaDefinition, String nodeTemplateName, ComponentTypeEnum componentType, String componentName){
		
		SetupCDTest.getExtendTest().log(Status.INFO, "Going to validate "+ componentName + " " + componentType.getValue() + " node template TOSCA metadata...");
		Map<String, String> actualMetadata = actualToscaDefinition.getTopology_template().getNode_templates().get(nodeTemplateName).getMetadata();
		Either<Boolean,Map<String,Object>> componentToscaMetadataValidator = compareStringMapData(expectedMetadata, actualMetadata);
		if(componentToscaMetadataValidator.isLeft()){
			SetupCDTest.getExtendTest().log(Status.INFO, " " + componentName + " " + componentType.getValue() + "TOSCA node template metadata verification success");
		}else{
			SetupCDTest.getExtendTest().log(Status.ERROR, " " +componentName + " " + componentType.getValue() + "TOSCA node template metadata verification failed" + componentToscaMetadataValidator.right().value().toString());
		}
		return componentToscaMetadataValidator;
	}
	
	public static Either<Boolean, Map<String, Object>> serviceToscaMetadataValidatorAgainstParser(Map<String, String> expectedMetadata, Metadata actualMetadata){
		
		SetupCDTest.getExtendTest().log(Status.INFO, "Going to validate service TOSCA metadata...");

		Either<Boolean,Map<String,Object>> serviceToscaMetadataValidator = compareMetadataUsingToscaParser(expectedMetadata, actualMetadata);
		if(serviceToscaMetadataValidator.isLeft()){
			SetupCDTest.getExtendTest().log(Status.INFO, "Service TOSCA metadata verification vs. tosca parser success");
		}else{
			SetupCDTest.getExtendTest().log(Status.ERROR, "Service TOSCA metadata verification vs. tosca parser failed" + serviceToscaMetadataValidator.right().value().toString());
		}
		return serviceToscaMetadataValidator;
	}

	
	public static Either<Boolean, Map<String, Object>> compareStringMapData(Map<String, String> expectedMetadata, Map<String, String> actualMetadata) {
		Either.left(false);
		Map<String, Object> errorMap = new HashMap<>();
		for(String key : expectedMetadata.keySet()){
			boolean isError = compareValue(expectedMetadata.get(key), actualMetadata.get(key));
			if(!isError){
				errorMap.put("Data key["+key+"]", "expected: " + expectedMetadata.get(key) + ", actual: " + actualMetadata.get(key));
			}
		}
		if(!errorMap.isEmpty()){
			return Either.right(errorMap);
		}
		return Either.left(true);
	}
	
	public static Either<Boolean, Map<String, Object>> compareMetadataUsingToscaParser(Map<String, String> expectedMetadata, Metadata actualMetadata) {
		Map<String, Object> errorMap = new HashMap<>();
		for(String key : expectedMetadata.keySet()){
			boolean isError = compareValue(expectedMetadata.get(key), actualMetadata.getValue(key));
			if(!isError){
				errorMap.put("Data key["+key+"]", "expected: " + expectedMetadata.get(key) + ", actual: " + actualMetadata.getValue(key));
			}
		}
		if(!errorMap.isEmpty()){
			return Either.right(errorMap);
		}
		return Either.left(true);
	}
	
	private static boolean compareValue(String expected, String actual) {
		
		return expected.equals(actual);
		
	}
	
	public static Either<Boolean, Map<String, Object>> toscaInputsValidator(Map<String, ToscaInputsTopologyTemplateDefinition> expectedInputs, Map<String, ToscaInputsTopologyTemplateDefinition> actualInputs){
		
		SetupCDTest.getExtendTest().log(Status.INFO, "Going to validate TOSCA inputs...");
		Either<Boolean,Map<String,Object>> toscaInputsValidator = compareInputs(expectedInputs, actualInputs);
		if(toscaInputsValidator.isLeft()){
			SetupCDTest.getExtendTest().log(Status.INFO, "TOSCA inputs verification success");
		}else{
			SetupCDTest.getExtendTest().log(Status.ERROR, "TOSCA inputs verification failed" + toscaInputsValidator.right().value().toString());
		}
		
		if(toscaInputsValidator.right() != null && ! toscaInputsValidator.right().equals("")){
			return toscaInputsValidator;
		}
		return Either.left(true);
	}
	
	public static Either<Boolean,Map<String,Object>> compareInputs(Map<String, ToscaInputsTopologyTemplateDefinition> expectedInputs, Map<String, ToscaInputsTopologyTemplateDefinition> actualInputs) {
		
		Map<String, Object> errorMap = new HashMap<>();
		
		for (String inputName : expectedInputs.keySet()){
			if (actualInputs.get(inputName) == null ){
				errorMap.put("input [" + inputName + "]", " does not exist in TOSCA main yaml");
			}else{
				compareInputData(expectedInputs.get(inputName), actualInputs.get(inputName), errorMap);
			}
		}
		if(errorMap != null && !errorMap.isEmpty()){
			return Either.right(errorMap);
		}
		return Either.left(true);
	}

	public static Either<Boolean, Map<String, Object>> compareInputData(ToscaInputsTopologyTemplateDefinition expectedInputDefinition, ToscaInputsTopologyTemplateDefinition actualInputDefinition, Map<String, Object> errorMap) {

		Field[] declaredFields = expectedInputDefinition.getClass().getDeclaredFields();
		for (Field field : declaredFields){
			try {
				Object expectedValue = field.get(expectedInputDefinition);
				Object actualValue = field.get(actualInputDefinition);
//				verification exclude fields as (immutable, hidden, constraints, entry_schema) according Renana
				if(expectedValue != null && expectedValue.toString().trim()!= "" && field.getName() != "name" && field.getName() != "immutable" && field.getName() != "hidden" && field.getName() != "constraints" && field.getName() != "entry_schema" && field.getName() != "required") {
					if (actualValue != null) {
						compareInputValue(expectedInputDefinition, errorMap, field, expectedValue, actualValue);
					} else {
						errorMap.put("Data field [" + field.getName() + "] in input [" + expectedInputDefinition.getName() + "]", " does not exist in actual object");
						System.out.println("Data field [" + field.getName() + "] in input [" + expectedInputDefinition.getName() + "] does not exist in actual object");
					}
				}
			}catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		if(errorMap != null && !errorMap.isEmpty()){
 			return Either.right(errorMap);
		}
		return Either.left(true);
	}

	public static void compareInputValue(ToscaInputsTopologyTemplateDefinition expectedInputDefinition, Map<String, Object> errorMap, Field field, Object expectedValue, Object actualValue) {
		if(field.getName() == "value" || field.getName() == "Default"){
			switch (expectedInputDefinition.getType()) {
			case "string":
				if(! expectedValue.toString().replace("\n"," ").replaceAll("( +)", " ").equals(actualValue.toString().replace("\n"," ").replaceAll("( +)", " "))){
					errorMap.put("Data field [" + field.getName()+"] in input [" + expectedInputDefinition.getName() + "]",  "expected: " + expectedValue + ", actual: " + actualValue);
					System.out.println("Data field [" + field.getName()+"] in input [" + expectedInputDefinition.getName() + "]: expected: " + expectedValue + ", actual: " + actualValue);
				}
				break;
			case "float":
					float newExpectedValue = convertObjectToFloat(expectedValue);
					float newActualValue = convertObjectToFloat(actualValue);
					if(newExpectedValue != newActualValue){
						errorMap.put("Data field [" + field.getName()+"] in input [" + expectedInputDefinition.getName() + "]",  "expected: " + newExpectedValue + ", actual: " + newActualValue);
						System.out.println("Data field [" + field.getName()+"] in input [" + expectedInputDefinition.getName() + "]: expected: " + newExpectedValue + ", actual: " + newActualValue);
					}
				break;
			case "boolean":
				if(! expectedValue.toString().toLowerCase().equals(actualValue.toString().toLowerCase())){
					errorMap.put("Data field [" + field.getName()+"] in input [" + expectedInputDefinition.getName() + "]",  "expected: " + expectedValue + ", actual: " + actualValue);
					System.out.println("Data field [" + field.getName()+"] in input [" + expectedInputDefinition.getName() + "]: expected: " + expectedValue + ", actual: " + actualValue);
				}
				break;
			case "list":
				expectedInputDefinition.getEntry_schema().get("type");
				break;
			case "map":
				
				break;			
			default:
				break;
			}
			
			
		}else{
			if(! expectedValue.equals(actualValue)){
				errorMap.put("Data field [" + field.getName()+"] in input [" + expectedInputDefinition.getName() + "]",  "expected: " + expectedValue + ", actual: " + actualValue);
				System.out.println("Data field [" + field.getName()+"] in input [" + expectedInputDefinition.getName() + "]: expected: " + expectedValue + ", actual: " + actualValue);
			}
		}
		
	}
	
	
	public static float convertObjectToFloat(Object object){
		
		float floatValue = 0;
		
		if(object instanceof Integer){
			floatValue = ((Integer)object).floatValue();
		}
		if(object instanceof Double){
			floatValue = ((Double)object).floatValue();
		}
		if(object instanceof Float){
			floatValue = ((Float) object).floatValue();
		}
		if(object instanceof String){
			floatValue = Float.parseFloat(object.toString());
		}
		
		if(object instanceof Long){
			floatValue = ((Long) object).floatValue();
		}
		return floatValue;
	}

	public static Either<Boolean, Map<String, Object>> toscaInputsValidatorAgainstParser(Map<String, ToscaInputsTopologyTemplateDefinition> expectedInputsMap, ISdcCsarHelper fdntCsarHelper) {
		SetupCDTest.getExtendTest().log(Status.INFO, "Going to convert tosca parser inputs output to ToscaInputsTopologyTemplateDefinition object...");
		if(fdntCsarHelper.getServiceInputs().size() == 0){
			if(expectedInputsMap != null && ! expectedInputsMap.isEmpty()){
				return Either.left(true);
			}else{
				Map<String, Object> errorMap = new HashMap<>();
				errorMap.put("Inputs", " do not exist on actual service");
				SetupCDTest.getExtendTest().log(Status.INFO, "Inputs do not exist on actual service csar");
				return Either.right(errorMap);
			}
		}
		Map<String, ToscaInputsTopologyTemplateDefinition> actualInputsMap = convertInputsParserOutputToMap(fdntCsarHelper);
		return toscaInputsValidator(expectedInputsMap, actualInputsMap);
	}

	/**
	 * @param fdntCsarHelper convert list of inputs return from tosca parser to map of ToscaInputsTopologyTemplateDefinition
	 * @return 
	 */
	public static Map<String, ToscaInputsTopologyTemplateDefinition> convertInputsParserOutputToMap(ISdcCsarHelper fdntCsarHelper) {
		Map<String, ToscaInputsTopologyTemplateDefinition> actualInputsMap = new HashMap<>();
		List<Input> serviceInputs = fdntCsarHelper.getServiceInputs();
		for (Input input : serviceInputs){
			ToscaInputsTopologyTemplateDefinition actualInputDefinition = new ToscaInputsTopologyTemplateDefinition();
			actualInputDefinition.setDefault(input.getDefault());
			actualInputDefinition.setType(input.getType());
			actualInputDefinition.setDescription(input.getDescription());
			actualInputsMap.put(input.getName(), actualInputDefinition);
		}
		return actualInputsMap;
	}
	
	
}
