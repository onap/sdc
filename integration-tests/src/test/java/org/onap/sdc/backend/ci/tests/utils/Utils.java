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

package org.onap.sdc.backend.ci.tests.utils;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.onap.sdc.backend.ci.tests.config.Config;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.onap.sdc.backend.ci.tests.tosca.model.ToscaMetadataFieldsPresentationEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.common.api.ToscaNodeTypeInfo;
import org.openecomp.sdc.common.api.YamlConstants;
import org.yaml.snakeyaml.Yaml;

public final class Utils {

    @SuppressWarnings("unchecked")
    public ToscaNodeTypeInfo parseToscaNodeYaml(String fileContent) {

        ToscaNodeTypeInfo result = new ToscaNodeTypeInfo();

        if (fileContent != null) {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlObject = (Map<String, Object>) yaml.load(fileContent);

            final Object templateVersion = yamlObject.get(YamlConstants.TEMPLATE_VERSION);
            if (templateVersion != null) {
                result.setTemplateVersion(templateVersion.toString());
            }

            final Object templateName = yamlObject.get(YamlConstants.TEMPLATE_NAME);
            if (templateName != null) {
                result.setTemplateName(templateName.toString());
            }

            Object nodeTypes = yamlObject.get(YamlConstants.NODE_TYPES);
            if (nodeTypes != null) {
                Map<String, Object> nodeTypesMap = (Map<String, Object>) nodeTypes;
                for (Entry<String, Object> entry : nodeTypesMap.entrySet()) {

                    String nodeName = entry.getKey();
                    if (nodeName != null) {
                        result.setNodeName(nodeName);
                    }
                    break;
                }
            }
        }

        return result;
    }

    public static String getJsonObjectValueByKey(String metadata, String key) {
        JsonElement jelement = new JsonParser().parse(metadata);

        JsonObject jobject = jelement.getAsJsonObject();
        Object obj = jobject.get(key);
        if (obj == null) {
            return null;
        } else {
            return (String) jobject.get(key).getAsString();
        }
    }

    public static Config getConfig() throws FileNotFoundException {
        return Config.instance();
    }

    public static Config getConfigHandleException() {
        Config config = null;
        try {
            config = Config.instance();
        } catch (Exception e) {
            System.out.println("Configuration file not found. " + e);
        }
        return config;
    }

    public static void compareArrayLists(List<String> actualArraylList, List<String> expectedArrayList, String message) {

        ArrayList<String> actual = new ArrayList<String>(actualArraylList);
        ArrayList<String> expected = new ArrayList<String>(expectedArrayList);
        expected.removeAll(actual);
        assertEquals(message + " content got by rest API not match to " + message + " actual content", 0, expected.size());
    }

    public static Object parseYamlConfig(String pattern) throws FileNotFoundException {

        Yaml yaml = new Yaml();
        Config config = getConfig();
        String configurationFile = config.getConfigurationFile();
        File file = new File(configurationFile);
        InputStream inputStream = new FileInputStream(file);
        Map<?, ?> map = (Map<?, ?>) yaml.load(inputStream);
        return (Object) map.get(pattern);
    }

    public static String getDepArtLabelFromConfig(ArtifactTypeEnum artifactTypeEnum) throws FileNotFoundException {

        @SuppressWarnings("unchecked")
        Map<String, Object> mapOfDepResArtTypesObjects = (Map<String, Object>) parseYamlConfig(
            "deploymentResourceArtifacts");
        for (Map.Entry<String, Object> iter : mapOfDepResArtTypesObjects.entrySet()) {
            if (iter.getValue().toString().contains(artifactTypeEnum.getType())) {
                return iter.getKey().toLowerCase();
            }
        }

        return "defaultLabelName";
    }


    public static String multipleChar(String ch, int repeat) {
        return StringUtils.repeat(ch, repeat);
    }

    public static List<String> getListOfDepResArtLabels(Boolean isLowerCase) throws FileNotFoundException {

        List<String> listOfResDepArtTypesFromConfig = new ArrayList<String>();
        @SuppressWarnings("unchecked")
        Map<String, Object> resourceDeploymentArtifacts = (Map<String, Object>) parseYamlConfig(
            "deploymentResourceArtifacts");
        if (resourceDeploymentArtifacts != null) {

            if (isLowerCase) {
                for (Map.Entry<String, Object> iter : resourceDeploymentArtifacts.entrySet()) {
                    listOfResDepArtTypesFromConfig.add(iter.getKey().toLowerCase());
                }
            } else {

                for (Map.Entry<String, Object> iter : resourceDeploymentArtifacts.entrySet()) {
                    listOfResDepArtTypesFromConfig.add(iter.getKey());
                }
            }
        }
        return listOfResDepArtTypesFromConfig;
    }

    public static List<String> getListOfToscaArtLabels(Boolean isLowerCase) throws FileNotFoundException {

        List<String> listOfToscaArtTypesFromConfig = new ArrayList<String>();
        @SuppressWarnings("unchecked")
        Map<String, Object> toscaArtifacts = (Map<String, Object>) parseYamlConfig("toscaArtifacts");
        if (toscaArtifacts != null) {

            if (isLowerCase) {
                for (Map.Entry<String, Object> iter : toscaArtifacts.entrySet()) {
                    listOfToscaArtTypesFromConfig.add(iter.getKey().toLowerCase());
                }
            } else {
                for (Map.Entry<String, Object> iter : toscaArtifacts.entrySet()) {
                    listOfToscaArtTypesFromConfig.add(iter.getKey());
                }
            }
        }
        return listOfToscaArtTypesFromConfig;
    }

    public static List<String> getListOfResPlaceHoldersDepArtTypes() throws FileNotFoundException {
        List<String> listResDepArtTypesFromConfig = new ArrayList<String>();
        List<String> listOfResDepArtLabelsFromConfig = getListOfDepResArtLabels(false);
        assertNotNull("deployment artifact types list is null", listOfResDepArtLabelsFromConfig);
        Object parseYamlConfig = Utils.parseYamlConfig("deploymentResourceArtifacts");
        Map<String, Object> mapOfDepResArtTypesObjects = (Map<String, Object>) Utils
            .parseYamlConfig("deploymentResourceArtifacts");

        if (listOfResDepArtLabelsFromConfig != null) {
            for (String resDepArtType : listOfResDepArtLabelsFromConfig) {
                Object object = mapOfDepResArtTypesObjects.get(resDepArtType);
                if (object instanceof Map<?, ?>) {
                    Map<String, Object> map = (Map<String, Object>) object;
                    listResDepArtTypesFromConfig.add((String) map.get("type"));
                } else {
                    assertTrue("return object does not instance of map", false);
                }
            }
        }
        return listResDepArtTypesFromConfig;
    }

    public static Long getEpochTimeFromUTC(String time) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzz");
        java.util.Date date = df.parse(time);
        return date.getTime();
    }

    public static Long getActionDuration(Runnable func) throws Exception {
        long startTime = System.nanoTime();
        func.run();
        long estimateTime = System.nanoTime();
        return TimeUnit.NANOSECONDS.toSeconds(estimateTime - startTime);
    }

    public static Long getAndValidateActionDuration(Runnable action, int regularTestRunTime) {
        Long actualTestRunTime = null;
        try {
            actualTestRunTime = Utils.getActionDuration(() -> {
                try {
                    action.run();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        double factor = 1.5;

        assertTrue("Expected test run time should be less than " + regularTestRunTime * factor + ", " +
            "actual time is " + actualTestRunTime, regularTestRunTime * factor > actualTestRunTime);
        return actualTestRunTime;
    }


    public static Map<String, String> generateServiceMetadataToExpectedObject(ServiceReqDetails serviceReqDetails, Component component) {

        Map<String, String> metadata = new HashMap<>();

        metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.CATEGORY.value, serviceReqDetails.getCategories().get(0).getName());
        metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.DESCRIPTION.value, serviceReqDetails.getDescription());
        metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.INVARIANT_UUID.value, component.getInvariantUUID());
        metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.TYPE.value, "Service");
        metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.UUID.value, component.getUUID());
        metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.NAME.value, component.getName());
        metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.INSTANTIATION_TYPE.value, serviceReqDetails.getInstantiationType());
        metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.SERVICE_TYPE.value, serviceReqDetails.getServiceType());
        metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.SERVICE_ROLE.value, serviceReqDetails.getServiceRole());
        metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.NAMING_POLICY.value, serviceReqDetails.getNamingPolicy());
        metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.ECOMP_GENERATED_NAMING.value,
            serviceReqDetails.getEcompGeneratedNaming().toString());
        metadata.put(ToscaMetadataFieldsPresentationEnum.ToscaMetadataFieldsEnum.SERVICE_ECOMP_NAMING.value,
            serviceReqDetails.getEcompGeneratedNaming().toString());//equals to ECOMP_GENERATED_NAMING

        return metadata;
    }
}
