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

package org.openecomp.sdc.ci.tests.validation;

import com.aventstack.extentreports.Status;
import fj.data.Either;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.toscaparser.api.Group;
import org.onap.sdc.toscaparser.api.Property;
import org.onap.sdc.toscaparser.api.elements.Metadata;
import org.onap.sdc.toscaparser.api.parameters.Input;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaGroupPropertyDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaGroupsTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaInputsTopologyTemplateDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaServiceGroupsMetadataDefinition;
import org.openecomp.sdc.ci.tests.tosca.datatypes.VfModuleDefinition;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ToscaValidation {


    private ToscaValidation() {
    }

    /**
     * @param expectedToscaDefinition - expected toscaDefinition object
     * @param actualToscaDefinition   - actual toscaDefinition object
     * @return true if all validation success else return error map
     */
    public static Either<Boolean, Map<String, Object>> resourceToscaMetadataValidator(ToscaDefinition expectedToscaDefinition, ToscaDefinition actualToscaDefinition) {

        SetupCDTest.getExtendTest().log(Status.INFO, "Going to validate resource TOSCA metadata...");
        Map<String, String> expectedMetadata = expectedToscaDefinition.getMetadata();
        Map<String, String> actualMetadata = actualToscaDefinition.getMetadata();
        Either<Boolean, Map<String, Object>> resourceToscaMetadataValidator = compareStringMapData(expectedMetadata, actualMetadata);
        if (resourceToscaMetadataValidator.isLeft()) {
            SetupCDTest.getExtendTest().log(Status.INFO, "Resource TOSCA metadata verification success");
        } else {
            SetupCDTest.getExtendTest().log(Status.ERROR, "Resource TOSCA metadata verification failed" + resourceToscaMetadataValidator.right().value());
        }
        return resourceToscaMetadataValidator;
    }

    public static Boolean resourceToscaNodeTemplateMetadataValidator(Map<String, Map<String, String>> expectedMetadata, ToscaDefinition actualToscaDefinition) {
        boolean isTestFailed = true;
        for (String nodeTemplateName : expectedMetadata.keySet()) {
            Either<Boolean, Map<String, Object>> serviceToscaMetadataValidator = componentToscaNodeTemplateMetadataValidator(expectedMetadata.get(nodeTemplateName), actualToscaDefinition, nodeTemplateName, ComponentTypeEnum.RESOURCE, nodeTemplateName);
            if (!serviceToscaMetadataValidator.left().value()) {
                isTestFailed = false;
            }
        }
        return isTestFailed;
    }

    public static Either<Boolean, Map<String, Object>> vfModuleJsonFileValidator(Map<String, VfModuleDefinition> expectedVfModulesDefinitionObject, Map<String, VfModuleDefinition> actualVfModulesDefinitionObject) {
        SetupCDTest.getExtendTest().log(Status.INFO, "Going to validate vf module json file...");
        Either<Boolean, Map<String, Object>> vfModuleFileValidator = compareObjectMapData(expectedVfModulesDefinitionObject, actualVfModulesDefinitionObject);
        if (vfModuleFileValidator.isLeft()) {
            SetupCDTest.getExtendTest().log(Status.INFO, "Vf module json file verification success");
        } else {
            SetupCDTest.getExtendTest().log(Status.ERROR, "Vf module json file verification failed" + vfModuleFileValidator.right().value().toString());
        }
        return vfModuleFileValidator;
    }

    public static Either<Boolean, Map<String, Object>> serviceToscaMetadataValidator(Map<String, String> expectedMetadata, ToscaDefinition actualToscaDefinition) {

        SetupCDTest.getExtendTest().log(Status.INFO, "Going to validate service TOSCA metadata...");
        Map<String, String> actualMetadata = actualToscaDefinition.getMetadata();
        Either<Boolean, Map<String, Object>> serviceToscaMetadataValidator = compareStringMapData(expectedMetadata, actualMetadata);
        if (serviceToscaMetadataValidator.isLeft()) {
            SetupCDTest.getExtendTest().log(Status.INFO, "Service TOSCA metadata verification success");
        } else {
            SetupCDTest.getExtendTest().log(Status.ERROR, "Service TOSCA metadata verification failed" + serviceToscaMetadataValidator.right().value().toString());
        }
        return serviceToscaMetadataValidator;
    }

    public static Either<Boolean, Map<String, Object>> serviceToscaGroupMetadataValidator(Map<String, ToscaGroupsTopologyTemplateDefinition> expectedServiceGroup, ToscaDefinition actualToscaDefinition) {

        SetupCDTest.getExtendTest().log(Status.INFO, "Going to validate service TOSCA group metadata...");
        Map<String, ToscaGroupsTopologyTemplateDefinition> actualServiceGroups = actualToscaDefinition.getTopology_template().getGroups();
        Either<Boolean, Map<String, Object>> serviceToscaGroupMetadataValidator = compareServiceGroupMetadata(expectedServiceGroup, actualServiceGroups);
        if (serviceToscaGroupMetadataValidator.isLeft()) {
            SetupCDTest.getExtendTest().log(Status.INFO, "Service TOSCA group metadata verification success");
        } else {
            SetupCDTest.getExtendTest().log(Status.ERROR, "Service TOSCA group metadata verification failed" + serviceToscaGroupMetadataValidator.right().value().toString());
        }
        return serviceToscaGroupMetadataValidator;
    }

    private static Either<Boolean, Map<String, Object>> compareServiceGroupMetadata(Map<String, ToscaGroupsTopologyTemplateDefinition> expectedServiceGroup, Map<String, ToscaGroupsTopologyTemplateDefinition> actualServiceGroups) {

        Map<String, Object> errorMap = new HashMap<>();
        for (String groupName : expectedServiceGroup.keySet()) {
            if (actualServiceGroups.get(groupName) == null) {
                errorMap.put("group/module [" + groupName + "]", " does not exist in TOSCA main yaml");
            } else {
                compareServiceGroupData(expectedServiceGroup.get(groupName), actualServiceGroups.get(groupName), groupName, errorMap);
            }
        }
        if (!errorMap.isEmpty()) {
            return Either.right(errorMap);
        }
        return Either.left(true);
    }

    private static Either<Boolean, Map<String, Object>> compareServiceGroupData(ToscaServiceGroupsMetadataDefinition expectedServiceGroupMetadata, ToscaServiceGroupsMetadataDefinition actualServiceGroupMetadata, String groupName, Map<String, Object> errorMap) {

        Field[] declaredFields = expectedServiceGroupMetadata.getClass().getSuperclass().getFields();
        for (Field field : declaredFields) {
            if (!field.getName().equals("serialVersionUID")) {
                try {
                    String expectedValue = (String) field.get(expectedServiceGroupMetadata);
                    String actualValue = (String) field.get(actualServiceGroupMetadata);
                    comparingServiceGroupMetadata(groupName, errorMap, field, expectedValue, actualValue);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!errorMap.isEmpty()) {
            return Either.right(errorMap);
        }
        return Either.left(true);
    }


    //    ###########################UsingParser############################
    public static Either<Boolean, Map<String, Object>> serviceToscaGroupMetadataValidatorUsingParser(Map<String, ToscaGroupsTopologyTemplateDefinition> expectedServiceGroup, List<Group> actualServiceGroups) {

        SetupCDTest.getExtendTest().log(Status.INFO, "Going to validate service TOSCA group metadata...");
        Either<Boolean, Map<String, Object>> serviceToscaGroupMetadataValidator = compareServiceGroupMetadataUsingParser(expectedServiceGroup, actualServiceGroups);
        if (serviceToscaGroupMetadataValidator.isLeft()) {
            SetupCDTest.getExtendTest().log(Status.INFO, "Service TOSCA group metadata verification vs. tosca parser success");
        } else {
            SetupCDTest.getExtendTest().log(Status.ERROR, "Service TOSCA group metadata verification vs. tosca parser failed" + serviceToscaGroupMetadataValidator.right().value().toString());
        }
        return serviceToscaGroupMetadataValidator;
    }

    private static Either<Boolean, Map<String, Object>> compareServiceGroupMetadataUsingParser(Map<String, ToscaGroupsTopologyTemplateDefinition> expectedServiceGroup, List<Group> actualServiceGroups) {

        Map<String, Object> errorMap = new HashMap<>();
        for (String groupName : expectedServiceGroup.keySet()) {
            Group actualGroup = getServiceToscaParserGroupFromObject(groupName, actualServiceGroups);
            if (actualGroup == null) {
                errorMap.put("group/module [" + groupName + "]", " does not exist in TOSCA main yaml");
            } else {
                compareServiceGroupDataUsingParser(expectedServiceGroup.get(groupName), actualGroup.getMetadata(), groupName, errorMap);
            }
        }
        if (!errorMap.isEmpty()) {
            return Either.right(errorMap);
        }
        return Either.left(true);
    }

    private static Group getServiceToscaParserGroupFromObject(String groupName, List<Group> actualServiceGroups) {
        for (Group group : actualServiceGroups) {
            if (group.getName().equals(groupName)) {
                return group;
            }
        }
        return null;
    }

    private static Either<Boolean, Map<String, Object>> compareServiceGroupDataUsingParser(ToscaServiceGroupsMetadataDefinition expectedServiceGroupMetadata, Metadata actualServiceGroupMetadata, String groupName, Map<String, Object> errorMap) {

        Field[] declaredFields = expectedServiceGroupMetadata.getClass().getSuperclass().getFields();
        for (Field field : declaredFields) {
            if (!field.getName().equals("serialVersionUID")) {
                try {
                    String expectedValue = (String) field.get(expectedServiceGroupMetadata);
                    String actualValue = actualServiceGroupMetadata.getValue(field.getName());
                    comparingServiceGroupMetadata(groupName, errorMap, field, expectedValue, actualValue);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!errorMap.isEmpty()) {
            return Either.right(errorMap);
        }
        return Either.left(true);
    }

    private static void comparingServiceGroupMetadata(String groupName, Map<String, Object> errorMap, Field field, String expectedValue, String actualValue) {
        if (expectedValue != null && !expectedValue.trim().equals("")) {
            if (actualValue != null) {
                Boolean result = compareStringValue(expectedValue, actualValue);
                if (!result) {
                    errorMap.put("Data field [" + field.getName() + "] in group service metadata [" + groupName + "]", "expected: " + expectedValue + ", actual: " + actualValue);
                }
            } else {
                errorMap.put("Data field [" + field.getName() + "] in group service metadata [" + groupName + "]", " does not exist in actual object");
                System.out.println("Data field [" + field.getName() + "] in group service metadata [" + groupName + "] does not exist in actual object");
            }
        }
    }

//############################################################################################


    //    ###########################Property UsingParser############################
    public static Either<Boolean, Map<String, Object>> serviceToscaGroupPropertyValidatorUsingParser(Map<String, ToscaGroupsTopologyTemplateDefinition> expectedServiceGroup, List<Group> actualServiceGroups) {

        SetupCDTest.getExtendTest().log(Status.INFO, "Going to validate service TOSCA group property...");
        Either<Boolean, Map<String, Object>> serviceToscaGroupMetadataValidator = compareServiceGroupPropertyUsingParser(expectedServiceGroup, actualServiceGroups);
        if (serviceToscaGroupMetadataValidator.isLeft()) {
            SetupCDTest.getExtendTest().log(Status.INFO, "Service TOSCA group property verification vs. tosca parser success");
        } else {
            SetupCDTest.getExtendTest().log(Status.ERROR, "Service TOSCA group property verification vs. tosca parser failed" + serviceToscaGroupMetadataValidator.right().value().toString());
        }
        return serviceToscaGroupMetadataValidator;
    }

    private static Either<Boolean, Map<String, Object>> compareServiceGroupPropertyUsingParser(Map<String, ToscaGroupsTopologyTemplateDefinition> expectedServiceGroup, List<Group> actualServiceGroups) {

        Map<String, Object> errorMap = new HashMap<>();
        for (String groupName : expectedServiceGroup.keySet()) {
            Group actualGroup = getServiceToscaParserGroupFromObject(groupName, actualServiceGroups);
            if (actualGroup == null) {
                errorMap.put("group/module [" + groupName + "]", " does not exist in TOSCA main yaml");
            } else {
                compareServiceGroupPropertyUsingParser(expectedServiceGroup.get(groupName).getProperties(), actualGroup.getProperties(), groupName, errorMap);
            }
        }
        if (!errorMap.isEmpty()) {
            return Either.right(errorMap);
        }
        return Either.left(true);
    }

    private static Either<Boolean, Map<String, Object>> compareServiceGroupPropertyUsingParser(ToscaGroupPropertyDefinition expectedServiceGroupProperty, LinkedHashMap<String, Property> actualServiceGroupProperty, String groupName, Map<String, Object> errorMap) {

        Field[] declaredFields = expectedServiceGroupProperty.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (!field.getName().equals("serialVersionUID")) {
                try {
                    String expectedValue = null;
                    String actualValue = null;
                    if (field.get(expectedServiceGroupProperty) != null) {
                        expectedValue = field.get(expectedServiceGroupProperty).toString();
                    }
                    if (actualServiceGroupProperty.get(field.getName()) != null && actualServiceGroupProperty.get(field.getName()).getValue() != null) {
                        actualValue = actualServiceGroupProperty.get(field.getName()).getValue().toString();
                    }
                    comparingServiceGroupProperty(groupName, errorMap, field, expectedValue, actualValue);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!errorMap.isEmpty()) {
            return Either.right(errorMap);
        }
        return Either.left(true);
    }

//############################################################################################


    //?-------
    public static Either<Boolean, Map<String, Object>> serviceToscaGroupPropertyValidator(Map<String, ToscaGroupsTopologyTemplateDefinition> expectedServiceGroup, ToscaDefinition actualToscaDefinition) {

        SetupCDTest.getExtendTest().log(Status.INFO, "Going to validate service TOSCA group property...");
        Map<String, ToscaGroupsTopologyTemplateDefinition> actualServiceGroups = actualToscaDefinition.getTopology_template().getGroups();
        Either<Boolean, Map<String, Object>> serviceToscaGroupPropertyValidator = compareServiceGroupProperty(expectedServiceGroup, actualServiceGroups);
        if (serviceToscaGroupPropertyValidator.isLeft()) {
            SetupCDTest.getExtendTest().log(Status.INFO, "Service TOSCA group property verification success");
        } else {
            SetupCDTest.getExtendTest().log(Status.ERROR, "Service TOSCA group property verification failed" + serviceToscaGroupPropertyValidator.right().value().toString());
        }
        return serviceToscaGroupPropertyValidator;
    }

    private static Either<Boolean, Map<String, Object>> compareServiceGroupProperty(Map<String, ToscaGroupsTopologyTemplateDefinition> expectedServiceGroup, Map<String, ToscaGroupsTopologyTemplateDefinition> actualServiceGroups) {

        Map<String, Object> errorMap = new HashMap<>();
        for (String groupName : expectedServiceGroup.keySet()) {
            if (actualServiceGroups.get(groupName) == null) {
                errorMap.put("group/module [" + groupName + "]", " does not exist in TOSCA main yaml");
            } else {
                compareServiceGroupProperty(expectedServiceGroup.get(groupName).getProperties(), actualServiceGroups.get(groupName).getProperties(), groupName, errorMap);
            }
        }
        if (!errorMap.isEmpty()) {
            return Either.right(errorMap);
        }
        return Either.left(true);
    }

    private static Either<Boolean, Map<String, Object>> compareServiceGroupProperty(ToscaGroupPropertyDefinition expectedServiceGroupProperty, ToscaGroupPropertyDefinition actualServiceGroupProperty, String groupName, Map<String, Object> errorMap) {

        Field[] declaredFields = expectedServiceGroupProperty.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if (!field.getName().equals("serialVersionUID")) {
                try {
                    String expectedValue = null;
                    String actualValue = null;
                    if (field.get(expectedServiceGroupProperty) != null) {
                        expectedValue = field.get(expectedServiceGroupProperty).toString();
                    }
                    if (field.get(actualServiceGroupProperty) != null) {
                        actualValue = field.get(actualServiceGroupProperty).toString();
                    }
                    comparingServiceGroupProperty(groupName, errorMap, field, expectedValue, actualValue);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!errorMap.isEmpty()) {
            return Either.right(errorMap);
        }
        return Either.left(true);
    }

    private static void comparingServiceGroupProperty(String groupName, Map<String, Object> errorMap, Field field, String expectedValue, String actualValue) {
        if (expectedValue != null && !expectedValue.trim().equals("")) {
            if (actualValue != null) {
                Boolean result = compareStringValue(expectedValue, actualValue);
                if (!result) {
                    errorMap.put("Data field [" + field.getName() + "] in group service property [" + groupName + "]", "expected: " + expectedValue + ", actual: " + actualValue);
                }
            } else {
                errorMap.put("Data field [" + field.getName() + "] in group service property [" + groupName + "]", " does not exist in actual object");
                System.out.println("Data field [" + field.getName() + "] in group service property [" + groupName + "] does not exist in actual object");
            }
        }
    }


    //    ----------------------------------
    public static Either<Boolean, Map<String, Object>> componentToscaNodeTemplateMetadataValidator(Map<String, String> expectedMetadata, ToscaDefinition actualToscaDefinition, String nodeTemplateName, ComponentTypeEnum componentType, String componentName) {

        SetupCDTest.getExtendTest().log(Status.INFO, "Going to validate " + componentName + " " + componentType.getValue() + " node template TOSCA metadata...");
        Map<String, String> actualMetadata = actualToscaDefinition.getTopology_template().getNode_templates().get(nodeTemplateName).getMetadata();
        Either<Boolean, Map<String, Object>> componentToscaMetadataValidator = compareStringMapData(expectedMetadata, actualMetadata);
        if (componentToscaMetadataValidator.isLeft()) {
            SetupCDTest.getExtendTest().log(Status.INFO, " " + componentName + " " + componentType.getValue() + "TOSCA node template metadata verification success");
        } else {
            SetupCDTest.getExtendTest().log(Status.ERROR, " " + componentName + " " + componentType.getValue() + "TOSCA node template metadata verification failed" + componentToscaMetadataValidator.right().value().toString());
        }
        return componentToscaMetadataValidator;
    }

    public static Either<Boolean, Map<String, Object>> serviceToscaMetadataValidatorAgainstParser(Map<String, String> expectedMetadata, Metadata actualMetadata) {

        SetupCDTest.getExtendTest().log(Status.INFO, "Going to validate service TOSCA metadata...");

        Either<Boolean, Map<String, Object>> serviceToscaMetadataValidator = compareMetadataUsingToscaParser(expectedMetadata, actualMetadata);
        if (serviceToscaMetadataValidator.isLeft()) {
            SetupCDTest.getExtendTest().log(Status.INFO, "Service TOSCA metadata verification vs. tosca parser success");
        } else {
            SetupCDTest.getExtendTest().log(Status.ERROR, "Service TOSCA metadata verification vs. tosca parser failed" + serviceToscaMetadataValidator.right().value().toString());
        }
        return serviceToscaMetadataValidator;
    }


    private static Either<Boolean, Map<String, Object>> compareStringMapData(Map<String, String> expectedMetadata, Map<String, String> actualMetadata) {
        Either.left(false);
        Map<String, Object> errorMap = new HashMap<>();
        for (String key : expectedMetadata.keySet()) {
            boolean isError = compareStringValue(expectedMetadata.get(key), actualMetadata.get(key));
            if (!isError) {
                errorMap.put("Data key[" + key + "]", "expected: " + expectedMetadata.get(key) + ", actual: " + actualMetadata.get(key));
            }
        }
        if (!errorMap.isEmpty()) {
            return Either.right(errorMap);
        }
        return Either.left(true);
    }


    private static Either<Boolean, Map<String, Object>> compareObjectMapData(Map<String, VfModuleDefinition> expectedObject, Map<String, VfModuleDefinition> actualObject) {
        Map<String, Object> errorMap = new HashMap<>();
        for (String key : expectedObject.keySet()) {
            boolean isError = compareObjectValue(expectedObject.get(key), actualObject.get(key));
            if (!isError) {
                errorMap.put("Data key[" + key + "]", "expected: " + expectedObject.get(key) + ", actual: " + actualObject.get(key));
            }
        }
        if (!errorMap.isEmpty()) {
            return Either.right(errorMap);
        }
        return Either.left(true);
    }

    private static boolean compareObjectValue(VfModuleDefinition expected, VfModuleDefinition actual) {
        return expected.equals(actual);
    }

    private static Either<Boolean, Map<String, Object>> compareMetadataUsingToscaParser(Map<String, String> expectedMetadata, Metadata actualMetadata) {
        Map<String, Object> errorMap = new HashMap<>();
        for (String key : expectedMetadata.keySet()) {
            boolean isError = compareStringValue(expectedMetadata.get(key), actualMetadata.getValue(key));
            if (!isError) {
                errorMap.put("Data key[" + key + "]", "expected: " + expectedMetadata.get(key) + ", actual: " + actualMetadata.getValue(key));
            }
        }
        if (!errorMap.isEmpty()) {
            return Either.right(errorMap);
        }
        return Either.left(true);
    }

    private static boolean compareStringValue(String expected, String actual) {
        return expected.equals(actual);
    }

    public static Either<Boolean, Map<String, Object>> toscaInputsValidator(Map<String, ToscaInputsTopologyTemplateDefinition> expectedInputs, Map<String, ToscaInputsTopologyTemplateDefinition> actualInputs) {

        SetupCDTest.getExtendTest().log(Status.INFO, "Going to validate TOSCA inputs...");
        Either<Boolean, Map<String, Object>> toscaInputsValidator = compareInputs(expectedInputs, actualInputs);
        if (toscaInputsValidator.isLeft()) {
            SetupCDTest.getExtendTest().log(Status.INFO, "TOSCA inputs verification success");
        } else {
            SetupCDTest.getExtendTest().log(Status.ERROR, "TOSCA inputs verification failed" + toscaInputsValidator.right().value().toString());
        }

        if (toscaInputsValidator.right() != null && !toscaInputsValidator.right().equals("")) {
            return toscaInputsValidator;
        }
        return Either.left(true);
    }

    private static Either<Boolean, Map<String, Object>> compareInputs(Map<String, ToscaInputsTopologyTemplateDefinition> expectedInputs, Map<String, ToscaInputsTopologyTemplateDefinition> actualInputs) {

        Map<String, Object> errorMap = new HashMap<>();

        for (String inputName : expectedInputs.keySet()) {
            if (actualInputs.get(inputName) == null) {
                errorMap.put("input [" + inputName + "]", " does not exist in TOSCA main yaml");
            } else {
                compareInputData(expectedInputs.get(inputName), actualInputs.get(inputName), errorMap);
            }
        }
        if (!errorMap.isEmpty()) {
            return Either.right(errorMap);
        }
        return Either.left(true);
    }

    private static Either<Boolean, Map<String, Object>> compareInputData(ToscaInputsTopologyTemplateDefinition expectedInputDefinition, ToscaInputsTopologyTemplateDefinition actualInputDefinition, Map<String, Object> errorMap) {

        Field[] declaredFields = expectedInputDefinition.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            try {
                Object expectedValue = field.get(expectedInputDefinition);
                Object actualValue = field.get(actualInputDefinition);
//				verification exclude fields as (immutable, hidden, constraints, entry_schema) according Renana
                if (expectedValue != null && !expectedValue.toString().trim().equals("") && field.getName() != "name" && field.getName() != "immutable" && field.getName() != "hidden" && field.getName() != "constraints" && field.getName() != "entry_schema" && field.getName() != "required") {
                    if (actualValue != null) {
                        compareInputValue(expectedInputDefinition, errorMap, field, expectedValue, actualValue);
                    } else {
                        errorMap.put("Data field [" + field.getName() + "] in input [" + expectedInputDefinition.getName() + "]", " does not exist in actual object");
                        System.out.println("Data field [" + field.getName() + "] in input [" + expectedInputDefinition.getName() + "] does not exist in actual object");
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if (!errorMap.isEmpty()) {
            return Either.right(errorMap);
        }
        return Either.left(true);
    }

    private static void compareInputValue(ToscaInputsTopologyTemplateDefinition expectedInputDefinition, Map<String, Object> errorMap, Field field, Object expectedValue, Object actualValue) {
        if (field.getName().equals("value") || field.getName().equals("Default")) {
            switch (expectedInputDefinition.getType()) {
                case "string":
                    if (!expectedValue.toString().replace("\n", " ").replaceAll("( +)", " ").equals(actualValue.toString().replace("\n", " ").replaceAll("( +)", " "))) {
                        errorMap.put("Data field [" + field.getName() + "] in input [" + expectedInputDefinition.getName() + "]", "expected: " + expectedValue + ", actual: " + actualValue);
                        System.out.println("Data field [" + field.getName() + "] in input [" + expectedInputDefinition.getName() + "]: expected: " + expectedValue + ", actual: " + actualValue);
                    }
                    break;
                case "float":
                    float newExpectedValue = convertObjectToFloat(expectedValue);
                    float newActualValue = convertObjectToFloat(actualValue);
                    if (newExpectedValue != newActualValue) {
                        errorMap.put("Data field [" + field.getName() + "] in input [" + expectedInputDefinition.getName() + "]", "expected: " + newExpectedValue + ", actual: " + newActualValue);
                        System.out.println("Data field [" + field.getName() + "] in input [" + expectedInputDefinition.getName() + "]: expected: " + newExpectedValue + ", actual: " + newActualValue);
                    }
                    break;
                case "boolean":
                    if (!expectedValue.toString().equalsIgnoreCase(actualValue.toString())) {
                        errorMap.put("Data field [" + field.getName() + "] in input [" + expectedInputDefinition.getName() + "]", "expected: " + expectedValue + ", actual: " + actualValue);
                        System.out.println("Data field [" + field.getName() + "] in input [" + expectedInputDefinition.getName() + "]: expected: " + expectedValue + ", actual: " + actualValue);
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


        } else {
            if (!expectedValue.equals(actualValue)) {
                errorMap.put("Data field [" + field.getName() + "] in input [" + expectedInputDefinition.getName() + "]", "expected: " + expectedValue + ", actual: " + actualValue);
                System.out.println("Data field [" + field.getName() + "] in input [" + expectedInputDefinition.getName() + "]: expected: " + expectedValue + ", actual: " + actualValue);
            }
        }

    }


    private static float convertObjectToFloat(Object object) {

        float floatValue = 0;

        if (object instanceof Integer) {
            floatValue = ((Integer) object).floatValue();
        }
        if (object instanceof Double) {
            floatValue = ((Double) object).floatValue();
        }
        if (object instanceof Float) {
            floatValue = ((Float) object).floatValue();
        }
        if (object instanceof String) {
            floatValue = Float.parseFloat(object.toString());
        }

        if (object instanceof Long) {
            floatValue = ((Long) object).floatValue();
        }
        return floatValue;
    }

    public static Either<Boolean, Map<String, Object>> toscaInputsValidatorAgainstParser(Map<String, ToscaInputsTopologyTemplateDefinition> expectedInputsMap, ISdcCsarHelper fdntCsarHelper) {
        SetupCDTest.getExtendTest().log(Status.INFO, "Going to convert tosca parser inputs output to ToscaInputsTopologyTemplateDefinition object...");
        if (!fdntCsarHelper.getServiceInputs().isEmpty()) {
            if (expectedInputsMap != null && !expectedInputsMap.isEmpty()) {
                return Either.left(true);
            } else {
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
     * @return field.get(actualServiceGroupProperty)
     */
    private static Map<String, ToscaInputsTopologyTemplateDefinition> convertInputsParserOutputToMap(ISdcCsarHelper fdntCsarHelper) {
        Map<String, ToscaInputsTopologyTemplateDefinition> actualInputsMap = new HashMap<>();
        List<Input> serviceInputs = fdntCsarHelper.getServiceInputs();
        for (Input input : serviceInputs) {
            ToscaInputsTopologyTemplateDefinition actualInputDefinition = new ToscaInputsTopologyTemplateDefinition();
            actualInputDefinition.setDefault(input.getDefault());
            actualInputDefinition.setType(input.getType());
            actualInputDefinition.setDescription(input.getDescription());
            actualInputsMap.put(input.getName(), actualInputDefinition);
        }
        return actualInputsMap;
    }

}
