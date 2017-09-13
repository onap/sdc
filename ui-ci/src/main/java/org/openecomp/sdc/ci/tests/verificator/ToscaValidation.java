package org.openecomp.sdc.ci.tests.verificator;

import java.util.HashMap;
import java.util.Map;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaDefinition;
import org.openecomp.sdc.toscaparser.api.elements.Metadata;

import com.aventstack.extentreports.Status;

import fj.data.Either;

public class ToscaValidation {

	
	/**
	 * @param expectedToscaDefinition
	 * @param actualToscaDefinition
	 * @return true if all validation success else return error map
	 */
	public static Either<Boolean, Map<String, Object>> resourceToscaMetadataValidator(ToscaDefinition expectedToscaDefinition, ToscaDefinition actualToscaDefinition){
		
		SetupCDTest.getExtendTest().log(Status.INFO, "Going to validate resource TOSCA metadata...");
		Map<String, String> expectedMetadata = expectedToscaDefinition.getMetadata();
		Map<String, String> actualMetadata = actualToscaDefinition.getMetadata();
		Either<Boolean, Map<String, Object>> resourceToscaMetadataValidator = compareMapData(expectedMetadata, actualMetadata);
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
			Either<Boolean,Map<String,Object>> serviceToscaMetadataValidator = componentToscaNodeTemplateMetadataValidator(expectedMetadata.get(nodeTemplateName), actualToscaDefinition, nodeTemplateName, ComponentTypeEnum.RESOURCE);
			if(serviceToscaMetadataValidator.left().value() == false){
				isTestFailed = false;
			}
		}
		return isTestFailed;
	}
	
	public static Either<Boolean, Map<String, Object>> serviceToscaMetadataValidator(Map<String, String> expectedMetadata, ToscaDefinition actualToscaDefinition){
		
		SetupCDTest.getExtendTest().log(Status.INFO, "Going to validate service TOSCA metadata...");
		Map<String, String> actualMetadata = actualToscaDefinition.getMetadata();
		Either<Boolean,Map<String,Object>> serviceToscaMetadataValidator = compareMapData(expectedMetadata, actualMetadata);
		if(serviceToscaMetadataValidator.isLeft()){
			SetupCDTest.getExtendTest().log(Status.INFO, "Service TOSCA metadata verification success");
		}else{
			SetupCDTest.getExtendTest().log(Status.ERROR, "Service TOSCA metadata verification failed" + serviceToscaMetadataValidator.right().value().toString());
		}
		return serviceToscaMetadataValidator;
	}

	public static Either<Boolean, Map<String, Object>> componentToscaNodeTemplateMetadataValidator(Map<String, String> expectedMetadata, ToscaDefinition actualToscaDefinition, String nodeTemplateName, ComponentTypeEnum componentType){
		
		SetupCDTest.getExtendTest().log(Status.INFO, "Going to validate "+ componentType.getValue() + " node template TOSCA metadata...");
		Map<String, String> actualMetadata = actualToscaDefinition.getTopology_template().getNode_templates().get(nodeTemplateName).getMetadata();
		Either<Boolean,Map<String,Object>> componentToscaMetadataValidator = compareMapData(expectedMetadata, actualMetadata);
		if(componentToscaMetadataValidator.isLeft()){
			SetupCDTest.getExtendTest().log(Status.INFO, componentType.getValue() + "  TOSCA node template metadata verification success");
		}else{
			SetupCDTest.getExtendTest().log(Status.ERROR, componentType.getValue() + "  TOSCA node template metadata verification failed" + componentToscaMetadataValidator.right().value().toString());
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

	
	public static Either<Boolean, Map<String, Object>> compareMapData(Map<String, String> expectedMetadata, Map<String, String> actualMetadata) {
		Either.left(false);
		Map<String, Object> errorMap = new HashMap<>();
		for(String key : expectedMetadata.keySet()){
			boolean isError = compaireValue(expectedMetadata.get(key), actualMetadata.get(key));
			if(!isError){
				errorMap.put("Data key["+key+"]", "expected: " + expectedMetadata.get(key) + ", actual: " + actualMetadata.get(key));
			}
		}
		if(errorMap != null && !errorMap.isEmpty()){
			return Either.right(errorMap);
		}
		return Either.left(true);
	}
	
	public static Either<Boolean, Map<String, Object>> compareMetadataUsingToscaParser(Map<String, String> expectedMetadata, Metadata actualMetadata) {
		Map<String, Object> errorMap = new HashMap<>();
		for(String key : expectedMetadata.keySet()){
			boolean isError = compaireValue(expectedMetadata.get(key), actualMetadata.getValue(key));
			if(!isError){
				errorMap.put("Data key["+key+"]", "expected: " + expectedMetadata.get(key) + ", actual: " + actualMetadata.getValue(key));
			}
		}
		if(errorMap != null && !errorMap.isEmpty()){
			return Either.right(errorMap);
		}
		return Either.left(true);
	}
	
	private static boolean compaireValue(String expected, String actual) {
		
		return expected.equals(actual) ? true : false;
		
	}
	
	
	
}
