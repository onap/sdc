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

package org.openecomp.sdc.generator;

import org.openecomp.sdc.generator.aai.model.Widget;
import org.openecomp.sdc.generator.aai.tosca.ToscaTemplate;
import org.openecomp.sdc.generator.aai.xml.Model;
import org.openecomp.sdc.generator.data.*;
import org.openecomp.sdc.generator.impl.ArtifactGenerationServiceImpl;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static org.openecomp.sdc.generator.ArtifactGenerationServiceTest.*;
import static org.openecomp.sdc.generator.data.GeneratorConstants.*;

public class SampleJUnitTest extends TestCase {

    private static final String AAI_ARTIFACT_TYPE = ArtifactType.AAI.name();
    private static final String AAI_ARTIFACT_GROUP_TYPE = GroupType.DEPLOYMENT.name();
    private static final String GENERATOR_CONFIG = "{\"artifactTypes\": [\"OTHER\",\"AAI\"]}";
    private static final String ARTIFACT_GENERATOR_CONFIG = "artifactgenerator.config";
    private static final String CONFIG_PATH = "/qa-test-repo/jmeter3/apache-jmeter-3" +
        ".0/lib/junit/";
    private static final String GENERATOR_AAI_CONFIGLPROP_NOT_FOUND =
        "Cannot generate artifacts. Widget configuration not found for %s";
    private static final String GENERATOR_AAI_CONFIGFILE_NOT_FOUND =
        "Cannot generate artifacts. Artifact Generator Configuration file not found at %s";
    private static final String GENERATOR_AAI_CONFIGLOCATION_NOT_FOUND =
        "Cannot generate artifacts. artifactgenerator.config system property not configured";
    private static final String INVALID_VALUE_INVARIANT =
        "Invalid value for mandatory attribute <invariantUUID> in Artifact";
    private static final String INVALID_VALUE_UUID =
        "Invalid value for mandatory attribute <UUID> in Artifact:";
    static final Map<String, String> additionalParams = new HashMap<>();
    public static final String ARTIFACT_GENERATOR_PROPERTIES = "Artifact-Generator.properties";
    public static final String VF_VMME_TEMPLATE_NO_SYSTEM_PROP_CONFIGURED_YML = "vf_vmme_template_NoSystemPropConfigured.yml";
    public static final String SERVICE_VMME_TEMPLATE_NO_SYSTEM_PROP_CONFIGURED_YML = "service_vmme_template_NoSystemPropConfigured.yml";

    static{
        additionalParams.put(AdditionalParams.ServiceVersion.getName(),"1.0");
    }

    public SampleJUnitTest(String name) throws IOException {
        super(name);
        System.setProperty(ARTIFACT_GENERATOR_CONFIG,CONFIG_PATH + ARTIFACT_GENERATOR_PROPERTIES);
        loadConfig(ArtifactGenerationServiceTest.properties);
    }

    private void loadConfig(Properties properties) throws IOException {
        String configLocation = System.getProperty(ARTIFACT_GENERATOR_CONFIG);
        if (configLocation != null) {
            File file = new File(configLocation);
            if (file.exists()) {

                try (InputStream fis = new FileInputStream(file)) {
                    properties.load(fis);
                }
            }
        }
    }

    public SampleJUnitTest() {
        super();
        System.setProperty(ARTIFACT_GENERATOR_CONFIG,CONFIG_PATH + ARTIFACT_GENERATOR_PROPERTIES);
    }

    @Test
    public void testArtifactGenerationSingleVFSingleVFModule() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, "vf_vmme_template_SingleVFVFMod.yml");

            readPayloadFromResource(inputArtifacts, "service_vmme_template_SingleVFVFMod.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap, resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap, toscas);

            testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(5, data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());


        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationMissingVFInServiceTOSCA() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, "service_vmme_template_MissingVFInServiceTOSCA.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap, resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap, toscas);

            Assert.assertEquals(3, data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());


        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationVerifySameStaticWidgetsForAllServices() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, "vf_vmme_template_SameWidgets1.yml");

            readPayloadFromResource(inputArtifacts, "service_vmme_template_SameWidget1.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap, resultData);

            removeMockArtifact(data.getResultData().iterator());


            List<Artifact> inputArtifacts2 = new ArrayList();
            readPayloadFromResource(inputArtifacts2, "vf_vmme_template_SameWidgets2.yml");

            readPayloadFromResource(inputArtifacts2, "service_vmme_template_SameWidget2.yml");
            ArtifactGenerationServiceImpl obj2 = new ArtifactGenerationServiceImpl();

            GenerationData data2 = obj2.generateArtifact(inputArtifacts2, GENERATOR_CONFIG,additionalParams);
            List<Artifact> resultData2 = data2.getResultData();

            List<ToscaTemplate> toscas2 = new LinkedList();

            for (Artifact inputArtifact : inputArtifacts2) {
                toscas2.add(getToscaModel(inputArtifact));
            }

            Map<String, Model> outputArtifactMap2 = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap2, resultData2);
            removeMockArtifact(data2.getResultData().iterator());

            Map<String,String> map = new HashMap<>();
            Iterator<Artifact> itr =  data.getResultData().iterator();
                    while(itr.hasNext()){
                        Artifact artifact=itr.next();
                        if(artifact.getLabel().contains("AAI-widget")){
                            map.put(artifact.getName(),artifact.getChecksum());
                        }
                    }
            Map<String,String> map2 = new HashMap<>();
            Iterator<Artifact> itr2 =  data2.getResultData().iterator();
            while(itr2.hasNext()){
                Artifact artifact=itr2.next();
                if(artifact.getLabel().contains("AAI-widget")){
                    map2.put(artifact.getName(),artifact.getChecksum());
                }
            }
            Assert.assertEquals(map.size(),map2.size());
            for(Map.Entry<String, String> entry : map.entrySet()){
                Assert.assertEquals(entry.getValue(), map2.get(entry.getKey()));
            }

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationMulVFModule() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, "vf_vmme_template_MulVFVFMod.yml");

            readPayloadFromResource(inputArtifacts, "service_vmme_template_MulVFVFMod.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

                for (Artifact inputArtifact : inputArtifacts) {
                    toscas.add(getToscaModel(inputArtifact));
                }
                List<Artifact> resultData = data.getResultData();

                Map<String, Model> outputArtifactMap = new HashMap<>();
                ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap,resultData);
                ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap,toscas);
                testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(3,data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationMulVFs() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "CMAUI_VF.yaml");

            readPayloadFromResource(inputArtifacts, "ECA_OAM_VF.yaml");

            readPayloadFromResource(inputArtifacts, "MMSC_Sevice_07_25_16.yaml");

            readPayloadFromResource(inputArtifacts, "MMSC_VF.yaml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap,resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap,toscas);
            testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(8,data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationDupVFUUID() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, "vf_vmme_template_DupVFUUID.yml");

            readPayloadFromResource(inputArtifacts, "service_vmme_template_DupVFUUID.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

                for (Artifact inputArtifact : inputArtifacts) {
                    toscas.add(getToscaModel(inputArtifact));
                }
                List<Artifact> resultData = data.getResultData();

                Map<String, Model> outputArtifactMap = new HashMap<>();
                ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap,resultData);
                ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap,toscas);
                testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(5,data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationDupVFModUUID() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, "vf_vmme_template_DupVFModUUID.yml");

            readPayloadFromResource(inputArtifacts, "service_vmme_template_DupVFModUUID.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

                for (Artifact inputArtifact : inputArtifacts) {
                    toscas.add(getToscaModel(inputArtifact));
                }
                List<Artifact> resultData = data.getResultData();

                Map<String, Model> outputArtifactMap = new HashMap<>();
                ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap,resultData);
                ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap,toscas);
                testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(5,data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationVerifyVFModWithoutVNFC() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, "vf_vmme_template_VerifyVFModWithoutVNFC.yml");

            readPayloadFromResource(inputArtifacts, "service_vmme_template_VerifyVFModWithoutVNFC.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

                for (Artifact inputArtifact : inputArtifacts) {
                    toscas.add(getToscaModel(inputArtifact));
                }
                List<Artifact> resultData = data.getResultData();

                Map<String, Model> outputArtifactMap = new HashMap<>();
                ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap,resultData);
                ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap,toscas);
                testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(5,data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationVerifyVFModWithInvalidMember() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, "vf_vmme_template_VerifyVFModWithInvalidNo.yml");

            readPayloadFromResource(inputArtifacts, "service_vmme_template_VerifyVFModWithInvalidNo.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

                for (Artifact inputArtifact : inputArtifacts) {
                    toscas.add(getToscaModel(inputArtifact));
                }
                List<Artifact> resultData = data.getResultData();

                Map<String, Model> outputArtifactMap = new HashMap<>();
                ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap,resultData);
                ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap,toscas);
                testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(5,data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationNullFields() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, "vf_vmme_template_NullFields.yml");

            readPayloadFromResource(inputArtifacts, "service_vmme_template_NullFields.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);
            Assert.assertEquals(false,data.getErrorData().isEmpty());

            Assert.assertEquals("Invalid Service/Resource definition mandatory attribute <UUID> missing in Artifact: <"+inputArtifacts.get(0).getName()+">",data.getErrorData().get("AAI").get(0));

            Assert.assertEquals(2,data.getResultData().size());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationInCorrectYmlFormat() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "CMAUI_VFInvalidFormat.yaml");

            readPayloadFromResource(inputArtifacts, "ECA_OAM_VFInvalidFormat.yaml");

            readPayloadFromResource(inputArtifacts, "MMSC_Sevice_07_25_16InvalidFormat.yaml");

            readPayloadFromResource(inputArtifacts, "MMSC_VFInvalidFormat.yaml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);
            Assert.assertEquals(false,data.getErrorData().isEmpty());

            Assert.assertEquals("Invalid format for Tosca YML  : "+inputArtifacts.get(1).getName(),data.getErrorData().get("AAI").get(0));

            Assert.assertEquals(2,data.getResultData().size());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationMulComp() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, "vf_vmme_template_MulComp.yml");

            readPayloadFromResource(inputArtifacts, "service_vmme_template_MulComp.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

                for (Artifact inputArtifact : inputArtifacts) {
                    toscas.add(getToscaModel(inputArtifact));
                }
                List<Artifact> resultData = data.getResultData();

                Map<String, Model> outputArtifactMap = new HashMap<>();
                ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap,resultData);
                ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap,toscas);
                testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(5,data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationOrphan() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "vf_vmme_template_Orphan.yml");

            readPayloadFromResource(inputArtifacts, "service_vmme_template_Orphan.yml");

            readPayloadFromResource(inputArtifacts, "ECA_OAM_VFOrphan.yaml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

                for (Artifact inputArtifact : inputArtifacts) {
                    toscas.add(getToscaModel(inputArtifact));
                }
                List<Artifact> resultData = data.getResultData();

                Map<String, Model> outputArtifactMap = new HashMap<>();
                ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap,resultData);
                ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap,toscas);
                testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(5,data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationMissingVFTemplate() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "service_vmme_template_MissingVFTemplate.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

                for (Artifact inputArtifact : inputArtifacts) {
                    toscas.add(getToscaModel(inputArtifact));
                }
                List<Artifact> resultData = data.getResultData();

                Map<String, Model> outputArtifactMap = new HashMap<>();
                ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap,resultData);
                ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap,toscas);


            Assert.assertEquals(3,data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationMissingVFModule() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "CMAUI_VFMissingVFModule.yaml");

            readPayloadFromResource(inputArtifacts, "ECA_OAM_VFMissingVFModule.yaml");

            readPayloadFromResource(inputArtifacts, "MMSC_Sevice_07_25_16MissingVFModule.yaml");

            readPayloadFromResource(inputArtifacts, "MMSC_VFMissingVFModule.yaml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

                for (Artifact inputArtifact : inputArtifacts) {
                    toscas.add(getToscaModel(inputArtifact));
                }
                List<Artifact> resultData = data.getResultData();

                Map<String, Model> outputArtifactMap = new HashMap<>();
                ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap,resultData);
                ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap,toscas);
                testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(8,data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationEmptyArtifact() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);
            Assert.assertEquals(false,data.getErrorData().isEmpty());
            Assert.assertEquals("Service tosca missing from list of input artifacts",data.getErrorData().get("AAI").get(0));

            Assert.assertEquals(2,data.getResultData().size());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationMissingConfigFile() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "vf_vmme_template_SingleVFVFMod.yml");

            readPayloadFromResource(inputArtifacts, "service_vmme_template_SingleVFVFMod.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();

            GenerationData data = obj.generateArtifact(inputArtifacts, "",additionalParams);
            Assert.assertEquals(false,data.getErrorData().isEmpty());
            Assert.assertEquals("Invalid Client Configuration",data.getErrorData().get("ARTIFACT_GENERATOR_INVOCATION_ERROR").get(0));

            Assert.assertEquals(0,data.getResultData().size());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }


    @Test
    public void testArtifactGenerationWithNodeTemplates() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "ServiceWithNodetemplate.yml");

            readPayloadFromResource(inputArtifacts, "Resource0-template.yml");

            readPayloadFromResource(inputArtifacts, "Resource1-template.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap,resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap,toscas);
            testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(5,data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());


        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationWithoutNodeTemplates() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "Service0-template.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap,resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap,toscas);


            Assert.assertEquals(3,data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());


        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationWithArtifactNameAndDescMoreThan256() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "Service0-templateMoreThan256.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap,resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap,toscas);


            Assert.assertEquals(3,data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            for(Artifact artifact : data.getResultData()){
                checkArtifactName(artifact.getName());
                checkArtifactLabel(artifact.getLabel());
                checkArtifactDescription(artifact.getDescription());

            }

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationWithDifferentVersionOfSameVF() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "service_vmme_template_DiffVerOfSameVF.yml");

            readPayloadFromResource(inputArtifacts, "vf_vmme_template_DiffVerOfSameVF_1.yml");

            readPayloadFromResource(inputArtifacts, "vf_vmme_template_DiffVerOfSameVF_2.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap,resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap,toscas);
            testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(6,data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());


        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationWithDifferentVersionOfSameVFModWithSameInvId() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "service_vmme_template_DiffVerOfSameVFModWithSameInvId.yml");

            readPayloadFromResource(inputArtifacts, "vf_vmme_template_DiffVerOfSameVFModWithSameInvId.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap,resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap,toscas);
            testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(6,data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());


        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationWithServiceContainingL3Network() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "service_vmme_template_WithL3Network.yml");

            readPayloadFromResource(inputArtifacts, "vf_vmme_template_WithL3Network.yml");

            readPayloadFromResource(inputArtifacts, "resource-AllottedResource-template_WithL3Network.yml");

            readPayloadFromResource(inputArtifacts, "resource-Extvl-template_WithL3Network.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap,resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap,toscas);
            testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(7,data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());


        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationWithServiceContainingDupL3Network() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "service_vmme_template_WithDupL3Network.yml");

            readPayloadFromResource(inputArtifacts, "vf_vmme_template_WithDupL3Network.yml");

            readPayloadFromResource(inputArtifacts, "resource-AllottedResource-template_WithDupL3Network.yml");

            readPayloadFromResource(inputArtifacts, "resource-Extvl-template_WithDupL3Network.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap,resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap,toscas);
            testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(7,data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());


        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationWithL3NetworkInVFMod() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "service_vmme_template_WithL3NetworkInVFMod.yml");

            readPayloadFromResource(inputArtifacts, "vf_vmme_template_WithL3NetworkInVFMod.yml");

            readPayloadFromResource(inputArtifacts, "resource-AllottedResource-template_WithL3NetworkInVFMod.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap,resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap,toscas);
            testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(6,data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());


        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationWithDiffVersionOfSameL3Network() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "service_vmme_template_WithDiffVersionOfSameL3Network.yml");

            readPayloadFromResource(inputArtifacts, "vf_vmme_template_WithDiffVersionOfSameL3Network.yml");

            readPayloadFromResource(inputArtifacts, "resource-AllottedResource-template_WithDiffVersionOfSameL3Network.yml");

            readPayloadFromResource(inputArtifacts, "resource-Extvl-template_WithDiffVersionOfSameL3Network.yml");

            readPayloadFromResource(inputArtifacts, "resource-Extvl-template_1_WithDiffVersionOfSameL3Network.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap,resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap,toscas);
            testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(8,data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());


        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationWithInvIdGreaterThanSpecifiedLimit() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, "vf_vmme_template_WithInvIdGreaterThanSpecifiedLimit.yml");

            readPayloadFromResource(inputArtifacts, "service_vmme_template_WithInvIdGreaterThanSpecifiedLimit.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);
            Assert.assertEquals(false,data.getErrorData().isEmpty());

            Assert.assertEquals(INVALID_VALUE_INVARIANT + ": <" +inputArtifacts.get(1).getName()+">",data.getErrorData().get("AAI").get(0));

            Assert.assertEquals(2,data.getResultData().size());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationWithInvIdLesserThanSpecifiedLimit() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, "vf_vmme_template_WithInvIdLesserThanSpecifiedLimit.yml");

            readPayloadFromResource(inputArtifacts, "service_vmme_template_WithInvIdLesserThanSpecifiedLimit.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);
            Assert.assertEquals(false,data.getErrorData().isEmpty());

            Assert.assertEquals(INVALID_VALUE_UUID + " <"
                +inputArtifacts.get(1).getName()+">",data.getErrorData().get("AAI").get(0));

            Assert.assertEquals(2,data.getResultData().size());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }


    @Test
    public void testErrorWhenNoSystemPropConfigured() {
        String configLoc = System.getProperty(ARTIFACT_GENERATOR_CONFIG);
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, VF_VMME_TEMPLATE_NO_SYSTEM_PROP_CONFIGURED_YML);

            readPayloadFromResource(inputArtifacts, SERVICE_VMME_TEMPLATE_NO_SYSTEM_PROP_CONFIGURED_YML);

            System.clearProperty(ARTIFACT_GENERATOR_CONFIG);

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);
            Assert.assertEquals(false,data.getErrorData().isEmpty());
            Assert.assertEquals(data.getErrorData().
                get("AAI").get(0), GENERATOR_AAI_CONFIGLOCATION_NOT_FOUND);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        finally{
            System.setProperty(ARTIFACT_GENERATOR_CONFIG,configLoc);
        }
    }

    @Test
    public void testErrorWhenNoFileAtConfigLocation() {
        String configLoc = System.getProperty(ARTIFACT_GENERATOR_CONFIG);
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, VF_VMME_TEMPLATE_NO_SYSTEM_PROP_CONFIGURED_YML);

            readPayloadFromResource(inputArtifacts, SERVICE_VMME_TEMPLATE_NO_SYSTEM_PROP_CONFIGURED_YML);

            System.setProperty(ARTIFACT_GENERATOR_CONFIG,configLoc + File.separator + "testErrorWhenNoFileAtConfigLocation");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);
            Assert.assertEquals(data.getErrorData().isEmpty(),false);
            Assert.assertEquals(data.getErrorData().get("AAI").get(0),String.format(
                GENERATOR_AAI_CONFIGFILE_NOT_FOUND,System.getProperty
                (ARTIFACT_GENERATOR_CONFIG)));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        finally{
            System.setProperty(ARTIFACT_GENERATOR_CONFIG,configLoc);
        }
    }

    @Test
    public void testErrorWhenNoWidgetInConfig() throws IOException {
        System.setProperty(ARTIFACT_GENERATOR_CONFIG,CONFIG_PATH+"Artifact-Generator1.properties");
        loadConfig(ArtifactGenerationServiceTest.properties);
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, VF_VMME_TEMPLATE_NO_SYSTEM_PROP_CONFIGURED_YML);

            readPayloadFromResource(inputArtifacts, SERVICE_VMME_TEMPLATE_NO_SYSTEM_PROP_CONFIGURED_YML);

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            String assertMsg = ArtifactType.AAI.name() + ".model-version-id." + Widget.getWidget
                (Widget.Type.SERVICE).getName();

            Assert.assertEquals(false,data.getErrorData().isEmpty());
            String errMsg = String.format(GENERATOR_AAI_CONFIGLPROP_NOT_FOUND,assertMsg);
            Assert.assertEquals(data.getErrorData().get("AAI").get(0),errMsg);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            System.setProperty(ARTIFACT_GENERATOR_CONFIG, CONFIG_PATH + ARTIFACT_GENERATOR_PROPERTIES);
            loadConfig(ArtifactGenerationServiceTest.properties);
        }
    }

    @Test
    public void testArtifactGenerationWithUpdatedUUIDInConfig() throws IOException {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, "vf_vmme_template_WithUpdatedUUIDInConfig.yml");

            readPayloadFromResource(inputArtifacts, "service_vmme_template_WithUpdatedUUIDInConfig.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);
            List<ToscaTemplate> toscas = new LinkedList();
            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();
            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap, resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap, toscas);
            testResourceTosca(toscas.iterator(), outputArtifactMap);

            System.setProperty(ARTIFACT_GENERATOR_CONFIG,CONFIG_PATH+"Artifact-Generator2.properties");
            loadConfig(ArtifactGenerationServiceTest.properties);

            List<ToscaTemplate> toscas2 = new LinkedList();
            for (Artifact inputArtifact : inputArtifacts) {
                toscas2.add(getToscaModel(inputArtifact));
            }
            GenerationData data2 = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);
            Map<String, Model> outputArtifactMap2 = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap2,
                data2.getResultData());
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap2, toscas2);
            testResourceTosca(toscas2.iterator(), outputArtifactMap2);

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            System.setProperty(ARTIFACT_GENERATOR_CONFIG,CONFIG_PATH + ARTIFACT_GENERATOR_PROPERTIES);
            loadConfig(ArtifactGenerationServiceTest.properties);
        }
    }

    @Test
    public void testArtifactGenerationVerifyMandatoryParameterServiceVersion() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, "vf_vmme_template_VerifyMandatoryParameterServiceVersion.yml");

            readPayloadFromResource(inputArtifacts, "service_vmme_template_VerifyMandatoryParameterServiceVersion.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, new HashMap<String, String>());

            Assert.assertEquals(data.getErrorData().isEmpty(),false);
            Assert.assertEquals(data.getErrorData().get("AAI").get(0),GENERATOR_AAI_ERROR_MISSING_SERVICE_VERSION);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationVerifyServiceVersionFormat() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, "vf_vmme_template_VerifyServiceVersionFormat.yml");

            readPayloadFromResource(inputArtifacts, "service_vmme_template_VerifyServiceVersionFormat.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();

            additionalParams.put(AdditionalParams.ServiceVersion.getName(),"1");
            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);
            Assert.assertEquals(data.getErrorData().isEmpty(),false);
            Assert.assertEquals(data.getErrorData().get("AAI").get(0),GENERATOR_AAI_INVALID_SERVICE_VERSION);

            additionalParams.put(AdditionalParams.ServiceVersion.getName(),"0.1");
            GenerationData data2 = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG,
                additionalParams);
            Assert.assertEquals(data2.getErrorData().isEmpty(),false);
            Assert.assertEquals(data2.getErrorData().get("AAI").get(0),
                GENERATOR_AAI_INVALID_SERVICE_VERSION);

            additionalParams.put(AdditionalParams.ServiceVersion.getName(),"0.0");
            GenerationData data3 = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG,
                additionalParams);
            Assert.assertEquals(data3.getErrorData().isEmpty(),false);
            Assert.assertEquals(data3.getErrorData().get("AAI").get(0),
                GENERATOR_AAI_INVALID_SERVICE_VERSION);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally{
            additionalParams.put(AdditionalParams.ServiceVersion.getName(),"1.0");
        }
    }

    @Test
    public void testArtifactGenerationVerifyServiceVersion() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, "vf_vmme_template_VerifyServiceVersion.yml");

            readPayloadFromResource(inputArtifacts, "service_vmme_template_VerifyServiceVersion.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            additionalParams.put(AdditionalParams.ServiceVersion.getName(),"9.0");
            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap, resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap, toscas);

            testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(5, data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());


        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally{
            additionalParams.put(AdditionalParams.ServiceVersion.getName(),"1.0");
        }
    }


    @Test
    public void testArtifactGenerationVerifyResourceVersionFormat() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, "vf_vmme_template_VerifyResourceVersionFormat.yml");

            readPayloadFromResource(inputArtifacts, "service_vmme_template_VerifyResourceVersionFormat1.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();

            List<ToscaTemplate> toscas = new LinkedList();
            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);
            Assert.assertEquals(data.getErrorData().isEmpty(),false);
            Assert.assertEquals(data.getErrorData().get("AAI").get(0),
                String.format(GENERATOR_AAI_ERROR_INVALID_RESOURCE_VERSION_IN_SERVICE_TOSCA,
                    toscas.get(0).getMetadata().get("UUID")));

            inputArtifacts.remove(1);

            readPayloadFromResource(inputArtifacts, "service_vmme_template_VerifyResourceVersionFormat2.yml");
            GenerationData data2 = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG,
                additionalParams);
            Assert.assertEquals(data2.getErrorData().isEmpty(),false);
            Assert.assertEquals(data2.getErrorData().get("AAI").get(0),
                String.format(GENERATOR_AAI_ERROR_INVALID_RESOURCE_VERSION_IN_SERVICE_TOSCA,
                    toscas.get(0).getMetadata().get("UUID")));

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationVerifyMandatoryParameterResourceVersion() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            readPayloadFromResource(inputArtifacts, "vf_vmme_template_VerifyMandatoryParameterResourceVersion.yml");

            readPayloadFromResource(inputArtifacts, "service_vmme_template_VerifyMandatoryParameterResourceVersion.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();
            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);
            Assert.assertEquals(data.getErrorData().isEmpty(),false);
            Assert.assertEquals(data.getErrorData().get("AAI").get(0),String.format(GENERATOR_AAI_ERROR_NULL_RESOURCE_VERSION_IN_SERVICE_TOSCA,toscas.get(0).getMetadata().get("UUID")));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationWithoutAllottedResource() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "service-ServiceWithAllottedResourceIpmux-template.yml");

            readPayloadFromResource(inputArtifacts, "resource-AllottedResource-template_IpMux.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            Assert.assertEquals(data.getErrorData().isEmpty(),false);
            Assert.assertEquals(data.getErrorData().get("AAI").get(0),String.format
                (GENERATOR_AAI_PROVIDING_SERVICE_MISSING, toscas.get(1).getModelId()));

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationAllottedResourceIpmuxWithGroups() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "service-ServiceWithAllottedResourceIpmux-template_WithGroups.yml");

            readPayloadFromResource(inputArtifacts, "resource-AllottedResource-template_IpMux_WithGroups.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap, resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap, toscas);
            testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(5, data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationAllottedResourceIpmuxSameInvariantDiffVersion() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "service-ServiceWithAllottedResourcesIpMuxSameInvariant-template.yml");

            readPayloadFromResource(inputArtifacts, "resource-AllottedResource1SameInvariant-IpMux-template.yml");

            readPayloadFromResource(inputArtifacts, "resource-AllottedResource2SameInvariant-IpMux-template.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap, resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap, toscas);
            testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(5, data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());


        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationAllottedResourceIpmuxSameInvariantSameVersion() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "service-ServiceWithAllottedResourcesIpMuxSameInvariantSameVers-template.yml");

            readPayloadFromResource(inputArtifacts, "resource-AllottedResourceSameInvariantSameVers-IpMux-template.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap, resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap, toscas);
            testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(4, data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());


        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationAllottedResourceWithIpMuxAndTunnelXConn() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "service-Allottedipmux-template.yml");

            readPayloadFromResource(inputArtifacts, "resource-IpMuxDemux-template.yml");

            readPayloadFromResource(inputArtifacts, "resource-TunnelXconn-template.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap, resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap, toscas);
            testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(5, data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationAllottedResourceWithOutProvidingServiceId() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "service-SdWan-template_WithOutDepSerId.yml");

            readPayloadFromResource(inputArtifacts, "resource-VhnfNonHeat-template_WithOutDepSerId.yml");

            readPayloadFromResource(inputArtifacts, "resource-TunnelXconn-template_WithOutDepSerId.yml");

            readPayloadFromResource(inputArtifacts, "resource-ServiceAdmin-template_WithOutDepSerId.yml");

            readPayloadFromResource(inputArtifacts, "resource-IpMuxDemux-template_WithOutDepSerId.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();
            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);
            Assert.assertEquals(data.getErrorData().isEmpty(),false);
            Assert.assertEquals(data.getErrorData().get("AAI").get(0),String.format
                (GENERATOR_AAI_PROVIDING_SERVICE_METADATA_MISSING, toscas.get(2).getModelId()));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationAllottedResourceWithVF() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            readPayloadFromResource(inputArtifacts, "service-SdWan-template_AllRes_VF.yml");

            readPayloadFromResource(inputArtifacts, "resource-VhnfNonHeat-template_AllRes_VF.yml");

            readPayloadFromResource(inputArtifacts, "resource-TunnelXconn-template_AllRes_VF.yml");

            readPayloadFromResource(inputArtifacts, "resource-ServiceAdmin-template_AllRes_VF.yml");

            readPayloadFromResource(inputArtifacts, "resource-IpMuxDemux-template_AllRes_VF.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, GENERATOR_CONFIG, additionalParams);
            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap, resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap, toscas);
            testResourceTosca(toscas.iterator(), outputArtifactMap);

            Assert.assertEquals(7, data.getResultData().size());

            removeMockArtifact(data.getResultData().iterator());

            ArtifactGenerationServiceTest.validateName(data.getResultData());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    public static void readPayload(List<Artifact> inputArtifacts,InputStream fis, String fileName) throws IOException {
        byte[] payload = new byte[fis.available()];
        fis.read(payload);
        String checksum = GeneratorUtil.checkSum(payload);
        byte[] encodedPayload = GeneratorUtil.encode(payload);
        Artifact artifact = new Artifact(AAI_ARTIFACT_TYPE, AAI_ARTIFACT_GROUP_TYPE, checksum, encodedPayload);
        artifact.setName(fileName);
        artifact.setLabel(fileName);
        artifact.setDescription(fileName);
        inputArtifacts.add(artifact);
    }


    /**
     * Get the tosca java model from the tosca input artifact
     *
     * @param input Input tosca file and its metadata information as {@link Artifact} object
     * @return Translated {@link ToscaTemplate tosca} object
     * @throws SecurityException
     */
    public static ToscaTemplate getToscaModel(Artifact input) {
        byte[] decodedInput = GeneratorUtil.decoder(input.getPayload());
        String checksum = GeneratorUtil.checkSum(decodedInput);
        if (checksum.equals(input.getChecksum())) {
            try {
                return GeneratorUtil.translateTosca(new String(decodedInput), ToscaTemplate.class);
            } catch (Exception e) {
                throw new IllegalArgumentException(String.format(GENERATOR_AAI_ERROR_INVALID_TOSCA, input.getName()));
            }
        } else {
            throw new SecurityException(String.format(GENERATOR_AAI_ERROR_CHECKSUM_MISMATCH, input.getName()));
        }
    }

    public static void removeMockArtifact(Iterator<Artifact> itr) {
        while (itr.hasNext()){
            if(itr.next().getType().equals("OTHER")){
                itr.remove();
            }
        }
    }

    private void readPayloadFromResource(List<Artifact> inputArtifacts, String fileName) throws IOException {

        try (InputStream fis = SampleJUnitTest.class.getResourceAsStream("/" + fileName)) {
            readPayload(inputArtifacts, fis, fileName);
        }
    }
}
