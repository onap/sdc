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

    public static final String aaiArtifactType = ArtifactType.AAI.name();
    public static final String aaiArtifactGroupType = GroupType.DEPLOYMENT.name();
    public static final String generatorConfig = "{\"artifactTypes\": [\"OTHER\",\"AAI\"]}";
    public static final String ARTIFACTGENERATOR_CONFIG = "artifactgenerator.config";
    public static final String CONFIG_PATH = "/qa-test-repo/jmeter3/apache-jmeter-3" +
        ".0/lib/junit/";
    //public static final String CONFIG_PATH ="C:\\Jmeter-Copy\\jmeter3\\apache-jmeter-3" +
        //".0\\lib\\junit\\";
    public static final String GENERATOR_AAI_CONFIGLPROP_NOT_FOUND =
        "Cannot generate artifacts. Widget configuration not found for %s";
    public static final String GENERATOR_AAI_CONFIGFILE_NOT_FOUND =
        "Cannot generate artifacts. Artifact Generator Configuration file not found at %s";
    public static final String GENERATOR_AAI_CONFIGLOCATION_NOT_FOUND =
        "Cannot generate artifacts. artifactgenerator.config system property not configured";
    public static final String INVALID_VALUE_INVARIANT =
        "Invalid value for mandatory attribute <invariantUUID> in Artifact";
    public static final String INVALID_VALUE_UUID =
        "Invalid value for mandatory attribute <UUID> in Artifact:";
    public static final Map<String, String> additionalParams = new HashMap<>();

    static{
        additionalParams.put(AdditionalParams.ServiceVersion.getName(),"1.0");
    }

    private void loadConfig(Properties properties) throws IOException {
        String configLocation = System.getProperty(ARTIFACTGENERATOR_CONFIG);
        if (configLocation != null) {
            File file = new File(configLocation);
            if (file.exists()) {
                properties.load(new FileInputStream(file));
            }
        }
    }
    public SampleJUnitTest(String name) throws Exception {
        super(name);
        System.setProperty(ARTIFACTGENERATOR_CONFIG,CONFIG_PATH+"Artifact-Generator.properties");
        loadConfig(ArtifactGenerationServiceTest.properties);
    }

    public SampleJUnitTest() {
        super();
        System.setProperty(ARTIFACTGENERATOR_CONFIG,CONFIG_PATH+"Artifact-Generator.properties");
    }

    @Test
    public void testArtifactGenerationSingleVFSingleVFModule() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_SingleVFVFMod.yml");
            readPayload(inputArtifacts, fis1, "vf_vmme_template_SingleVFVFMod.yml");

            fis1.close();
            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_SingleVFVFMod.yml");
            readPayload(inputArtifacts, fis2, "service_vmme_template_SingleVFVFMod.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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
            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_MissingVFInServiceTOSCA.yml");
            readPayload(inputArtifacts, fis2, "service_vmme_template_MissingVFInServiceTOSCA.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_SameWidgets1.yml");
            readPayload(inputArtifacts, fis1, "vf_vmme_template_SameWidgets1.yml");

            fis1.close();
            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_SameWidget1.yml");
            readPayload(inputArtifacts, fis2, "service_vmme_template_SameWidget1.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap, resultData);

            removeMockArtifact(data.getResultData().iterator());


            List<Artifact> inputArtifacts2 = new ArrayList();
            InputStream fis3 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_SameWidgets2.yml");
            readPayload(inputArtifacts2, fis3, "vf_vmme_template_SameWidgets2.yml");

            InputStream fis4 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_SameWidget2.yml");
            readPayload(inputArtifacts2, fis4, "service_vmme_template_SameWidget2.yml");
            ArtifactGenerationServiceImpl obj2 = new ArtifactGenerationServiceImpl();

            GenerationData data2 = obj2.generateArtifact(inputArtifacts2, generatorConfig,additionalParams);
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
            for(String name : map.keySet()){
                Assert.assertEquals(map.get(name),map2.get(name));
            }

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationMulVFModule() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            String[] resourceFileList = {};
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_MulVFVFMod.yml");
            readPayload(inputArtifacts,fis1, "vf_vmme_template_MulVFVFMod.yml");

            fis1.close();
            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_MulVFVFMod.yml");
            readPayload(inputArtifacts,fis2, "service_vmme_template_MulVFVFMod.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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

            InputStream fis3 = SampleJUnitTest.class.getResourceAsStream("/CMAUI_VF.yaml");
            readPayload(inputArtifacts,fis3, "CMAUI_VF.yaml");

            InputStream fis4 = SampleJUnitTest.class.getResourceAsStream("/ECA_OAM_VF.yaml");
            readPayload(inputArtifacts,fis4, "ECA_OAM_VF.yaml");

            InputStream fis5 = SampleJUnitTest.class.getResourceAsStream("/MMSC_Sevice_07_25_16.yaml");
            readPayload(inputArtifacts,fis5, "MMSC_Sevice_07_25_16.yaml");

            InputStream fis6 = SampleJUnitTest.class.getResourceAsStream("/MMSC_VF.yaml");
            readPayload(inputArtifacts,fis6, "MMSC_VF.yaml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_DupVFUUID.yml");
            readPayload(inputArtifacts,fis1, "vf_vmme_template_DupVFUUID.yml");

            fis1.close();
            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_DupVFUUID.yml");
            readPayload(inputArtifacts,fis2, "service_vmme_template_DupVFUUID.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_DupVFModUUID.yml");
            readPayload(inputArtifacts,fis1, "vf_vmme_template_DupVFModUUID.yml");

            fis1.close();
            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_DupVFModUUID.yml");
            readPayload(inputArtifacts,fis2, "service_vmme_template_DupVFModUUID.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_VerifyVFModWithoutVNFC.yml");
            readPayload(inputArtifacts,fis1, "vf_vmme_template_VerifyVFModWithoutVNFC.yml");

            fis1.close();
            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_VerifyVFModWithoutVNFC.yml");
            readPayload(inputArtifacts,fis2, "service_vmme_template_VerifyVFModWithoutVNFC.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_VerifyVFModWithInvalidNo.yml");
            readPayload(inputArtifacts,fis1, "vf_vmme_template_VerifyVFModWithInvalidNo.yml");

            fis1.close();
            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_VerifyVFModWithInvalidNo.yml");
            readPayload(inputArtifacts,fis2, "service_vmme_template_VerifyVFModWithInvalidNo.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_NullFields.yml");
            readPayload(inputArtifacts,fis1, "vf_vmme_template_NullFields.yml");

            fis1.close();
            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_NullFields.yml");
            readPayload(inputArtifacts,fis2, "service_vmme_template_NullFields.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
            Assert.assertEquals(false,data.getErrorData().isEmpty());

            Assert.assertEquals("Invalid Service/Resource definition mandatory attribute <UUID> missing in Artifact: <"+inputArtifacts.get(0).getName()+">",data.getErrorData().get("AAI").get(0));

            Assert.assertEquals(2,data.getResultData().size());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationInCorrectYmlFormat() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            InputStream fis3 = SampleJUnitTest.class.getResourceAsStream("/CMAUI_VFInvalidFormat.yaml");
            readPayload(inputArtifacts,fis3, "CMAUI_VFInvalidFormat.yaml");

            InputStream fis4 = SampleJUnitTest.class.getResourceAsStream("/ECA_OAM_VFInvalidFormat.yaml");
            readPayload(inputArtifacts,fis4, "ECA_OAM_VFInvalidFormat.yaml");

            InputStream fis5 = SampleJUnitTest.class.getResourceAsStream("/MMSC_Sevice_07_25_16InvalidFormat.yaml");
            readPayload(inputArtifacts,fis5, "MMSC_Sevice_07_25_16InvalidFormat.yaml");

            InputStream fis6 = SampleJUnitTest.class.getResourceAsStream("/MMSC_VFInvalidFormat.yaml");
            readPayload(inputArtifacts,fis6, "MMSC_VFInvalidFormat.yaml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
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
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_MulComp.yml");
            readPayload(inputArtifacts,fis1, "vf_vmme_template_MulComp.yml");

            fis1.close();
            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_MulComp.yml");
            readPayload(inputArtifacts,fis2, "service_vmme_template_MulComp.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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

            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_Orphan.yml");
            readPayload(inputArtifacts,fis1, "vf_vmme_template_Orphan.yml");

            fis1.close();
            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_Orphan.yml");
            readPayload(inputArtifacts,fis2, "service_vmme_template_Orphan.yml");

            InputStream fis4 = SampleJUnitTest.class.getResourceAsStream("/ECA_OAM_VFOrphan.yaml");
            readPayload(inputArtifacts,fis4, "ECA_OAM_VFOrphan.yaml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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

            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_MissingVFTemplate.yml");
            readPayload(inputArtifacts,fis1, "service_vmme_template_MissingVFTemplate.yml");

            fis1.close();

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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

            InputStream fis3 = SampleJUnitTest.class.getResourceAsStream("/CMAUI_VFMissingVFModule.yaml");
            readPayload(inputArtifacts,fis3, "CMAUI_VFMissingVFModule.yaml");

            InputStream fis4 = SampleJUnitTest.class.getResourceAsStream("/ECA_OAM_VFMissingVFModule.yaml");
            readPayload(inputArtifacts,fis4, "ECA_OAM_VFMissingVFModule.yaml");

            InputStream fis5 = SampleJUnitTest.class.getResourceAsStream("/MMSC_Sevice_07_25_16MissingVFModule.yaml");
            readPayload(inputArtifacts,fis5, "MMSC_Sevice_07_25_16MissingVFModule.yaml");

            InputStream fis6 = SampleJUnitTest.class.getResourceAsStream("/MMSC_VFMissingVFModule.yaml");
            readPayload(inputArtifacts,fis6, "MMSC_VFMissingVFModule.yaml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationEmptyArtifact() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
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

            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_SingleVFVFMod.yml");
            readPayload(inputArtifacts,fis1, "vf_vmme_template_SingleVFVFMod.yml");

            fis1.close();
            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_SingleVFVFMod.yml");
            readPayload(inputArtifacts,fis2, "service_vmme_template_SingleVFVFMod.yml");
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

            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/ServiceWithNodetemplate.yml");
            readPayload(inputArtifacts,fis1, "ServiceWithNodetemplate.yml");

            fis1.close();
            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/Resource0-template.yml");
            readPayload(inputArtifacts,fis2, "Resource0-template.yml");

            InputStream fis3 = SampleJUnitTest.class.getResourceAsStream("/Resource1-template.yml");
            readPayload(inputArtifacts,fis3, "Resource1-template.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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

            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/Service0-template.yml");
            readPayload(inputArtifacts,fis1, "Service0-template.yml");

            fis1.close();

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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

            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/Service0-templateMoreThan256.yml");
            readPayload(inputArtifacts,fis1, "Service0-templateMoreThan256.yml");

            fis1.close();

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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

            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_DiffVerOfSameVF.yml");
            readPayload(inputArtifacts,fis1, "service_vmme_template_DiffVerOfSameVF.yml");
            fis1.close();

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_DiffVerOfSameVF_1.yml");
            readPayload(inputArtifacts,fis2, "vf_vmme_template_DiffVerOfSameVF_1.yml");
            fis2.close();

            InputStream fis3 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_DiffVerOfSameVF_2.yml");
            readPayload(inputArtifacts,fis3, "vf_vmme_template_DiffVerOfSameVF_2.yml");
            fis3.close();

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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

            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_DiffVerOfSameVFModWithSameInvId.yml");
            readPayload(inputArtifacts,fis1, "service_vmme_template_DiffVerOfSameVFModWithSameInvId.yml");
            fis1.close();

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_DiffVerOfSameVFModWithSameInvId.yml");
            readPayload(inputArtifacts,fis2, "vf_vmme_template_DiffVerOfSameVFModWithSameInvId.yml");
            fis2.close();

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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

            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_WithL3Network.yml");
            readPayload(inputArtifacts,fis1, "service_vmme_template_WithL3Network.yml");
            fis1.close();

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_WithL3Network.yml");
            readPayload(inputArtifacts,fis2, "vf_vmme_template_WithL3Network.yml");
            fis2.close();

            InputStream fis3 = SampleJUnitTest.class.getResourceAsStream("/resource-AllottedResource-template_WithL3Network.yml");
            readPayload(inputArtifacts,fis3, "resource-AllottedResource-template_WithL3Network.yml");
            fis3.close();

            InputStream fis4 = SampleJUnitTest.class.getResourceAsStream
                ("/resource-Extvl-template_WithL3Network.yml");
            readPayload(inputArtifacts,fis4, "resource-Extvl-template_WithL3Network.yml");
            fis4.close();

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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

            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_WithDupL3Network.yml");
            readPayload(inputArtifacts,fis1, "service_vmme_template_WithDupL3Network.yml");
            fis1.close();

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_WithDupL3Network.yml");
            readPayload(inputArtifacts,fis2, "vf_vmme_template_WithDupL3Network.yml");
            fis2.close();

            InputStream fis3 = SampleJUnitTest.class.getResourceAsStream("/resource-AllottedResource-template_WithDupL3Network.yml");
            readPayload(inputArtifacts,fis3, "resource-AllottedResource-template_WithDupL3Network.yml");
            fis3.close();

            InputStream fis4 = SampleJUnitTest.class.getResourceAsStream
                ("/resource-Extvl-template_WithDupL3Network.yml");
            readPayload(inputArtifacts,fis4, "resource-Extvl-template_WithDupL3Network.yml");
            fis4.close();

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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

            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_WithL3NetworkInVFMod.yml");
            readPayload(inputArtifacts,fis1, "service_vmme_template_WithL3NetworkInVFMod.yml");
            fis1.close();

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_WithL3NetworkInVFMod.yml");
            readPayload(inputArtifacts,fis2, "vf_vmme_template_WithL3NetworkInVFMod.yml");
            fis2.close();

            InputStream fis3 = SampleJUnitTest.class.getResourceAsStream("/resource-AllottedResource-template_WithL3NetworkInVFMod.yml");
            readPayload(inputArtifacts,fis3, "resource-AllottedResource-template_WithL3NetworkInVFMod.yml");
            fis3.close();

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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

            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_WithDiffVersionOfSameL3Network.yml");
            readPayload(inputArtifacts,fis1, "service_vmme_template_WithDiffVersionOfSameL3Network.yml");
            fis1.close();

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_WithDiffVersionOfSameL3Network.yml");
            readPayload(inputArtifacts,fis2, "vf_vmme_template_WithDiffVersionOfSameL3Network.yml");
            fis2.close();

            InputStream fis3 = SampleJUnitTest.class.getResourceAsStream("/resource-AllottedResource-template_WithDiffVersionOfSameL3Network.yml");
            readPayload(inputArtifacts,fis3, "resource-AllottedResource-template_WithDiffVersionOfSameL3Network.yml");
            fis3.close();

            InputStream fis4 = SampleJUnitTest.class.getResourceAsStream
                ("/resource-Extvl-template_WithDiffVersionOfSameL3Network.yml");
            readPayload(inputArtifacts,fis4, "resource-Extvl-template_WithDiffVersionOfSameL3Network.yml");
            fis4.close();

            InputStream fis5 = SampleJUnitTest.class.getResourceAsStream
                ("/resource-Extvl-template_1_WithDiffVersionOfSameL3Network.yml");
            readPayload(inputArtifacts,fis5,
                "resource-Extvl-template_1_WithDiffVersionOfSameL3Network.yml");
            fis5.close();

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_WithInvIdGreaterThanSpecifiedLimit.yml");
            readPayload(inputArtifacts,fis1, "vf_vmme_template_WithInvIdGreaterThanSpecifiedLimit" +
                ".yml");
            fis1.close();

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_WithInvIdGreaterThanSpecifiedLimit.yml");
            readPayload(inputArtifacts,fis2, "service_vmme_template_WithInvIdGreaterThanSpecifiedLimit.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            fis2.close();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
            Assert.assertEquals(false,data.getErrorData().isEmpty());

            Assert.assertEquals(INVALID_VALUE_INVARIANT + ": <" +inputArtifacts.get(1).getName()+">",data.getErrorData().get("AAI").get(0));

            Assert.assertEquals(2,data.getResultData().size());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testArtifactGenerationWithInvIdLesserThanSpecifiedLimit() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_WithInvIdLesserThanSpecifiedLimit.yml");
            readPayload(inputArtifacts,fis1, "vf_vmme_template_WithInvIdLesserThanSpecifiedLimit" +
                ".yml");
            fis1.close();

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_WithInvIdLesserThanSpecifiedLimit.yml");
            readPayload(inputArtifacts,fis2, "service_vmme_template_WithInvIdLesserThanSpecifiedLimit.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            fis2.close();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
            Assert.assertEquals(false,data.getErrorData().isEmpty());

            Assert.assertEquals(INVALID_VALUE_UUID + " <"
                +inputArtifacts.get(1).getName()+">",data.getErrorData().get("AAI").get(0));

            Assert.assertEquals(2,data.getResultData().size());

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }


    @Test
    public void testErrorWhenNoSystemPropConfigured() throws Exception  {
        String configLoc = System.getProperty(ARTIFACTGENERATOR_CONFIG);
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_NoSystemPropConfigured.yml");
            readPayload(inputArtifacts,fis1, "vf_vmme_template_NoSystemPropConfigured" +
                ".yml");
            fis1.close();

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_NoSystemPropConfigured.yml");
            readPayload(inputArtifacts,fis2, "service_vmme_template_NoSystemPropConfigured.yml");
            fis2.close();

            System.clearProperty(ARTIFACTGENERATOR_CONFIG);

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
            Assert.assertEquals(false,data.getErrorData().isEmpty());
            Assert.assertEquals(data.getErrorData().
                get("AAI").get(0), GENERATOR_AAI_CONFIGLOCATION_NOT_FOUND);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        finally{
            System.setProperty(ARTIFACTGENERATOR_CONFIG,configLoc);
        }
    }

    @Test
    public void testErrorWhenNoFileAtConfigLocation() throws Exception  {
        String configLoc = System.getProperty(ARTIFACTGENERATOR_CONFIG);
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_NoSystemPropConfigured.yml");
            readPayload(inputArtifacts,fis1, "vf_vmme_template_NoSystemPropConfigured" +
                ".yml");
            fis1.close();

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_NoSystemPropConfigured.yml");
            readPayload(inputArtifacts,fis2, "service_vmme_template_NoSystemPropConfigured.yml");
            fis2.close();

            System.setProperty(ARTIFACTGENERATOR_CONFIG,configLoc + File.separator + "testErrorWhenNoFileAtConfigLocation");
            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
            Assert.assertEquals(data.getErrorData().isEmpty(),false);
            Assert.assertEquals(data.getErrorData().get("AAI").get(0),String.format(
                GENERATOR_AAI_CONFIGFILE_NOT_FOUND,System.getProperty
                (ARTIFACTGENERATOR_CONFIG)));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        finally{
            System.setProperty(ARTIFACTGENERATOR_CONFIG,configLoc);
        }
    }

    @Test
    public void testErrorWhenNoWidgetInConfig() throws Exception  {
        System.setProperty(ARTIFACTGENERATOR_CONFIG,CONFIG_PATH+"Artifact-Generator1.properties");
        loadConfig(ArtifactGenerationServiceTest.properties);
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_NoSystemPropConfigured.yml");
            readPayload(inputArtifacts,fis1, "vf_vmme_template_NoSystemPropConfigured" +
                ".yml");
            fis1.close();

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_NoSystemPropConfigured.yml");
            readPayload(inputArtifacts,fis2, "service_vmme_template_NoSystemPropConfigured.yml");
            fis2.close();

            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

            String assertMsg = ArtifactType.AAI.name() + ".model-version-id." + Widget.getWidget
                (Widget.Type.SERVICE).getName();

            Assert.assertEquals(false,data.getErrorData().isEmpty());
            String errMsg = String.format(GENERATOR_AAI_CONFIGLPROP_NOT_FOUND,assertMsg);
            Assert.assertEquals(data.getErrorData().get("AAI").get(0),errMsg);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            System.setProperty(ARTIFACTGENERATOR_CONFIG, CONFIG_PATH+"Artifact-Generator.properties");
            loadConfig(ArtifactGenerationServiceTest.properties);
        }
    }

    @Test
    public void testArtifactGenerationWithUpdatedUUIDInConfig() throws Exception  {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_WithUpdatedUUIDInConfig.yml");
            readPayload(inputArtifacts,fis1, "vf_vmme_template_WithUpdatedUUIDInConfig" +
                ".yml");
            fis1.close();

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_WithUpdatedUUIDInConfig.yml");
            readPayload(inputArtifacts,fis2, "service_vmme_template_WithUpdatedUUIDInConfig.yml");
            fis2.close();

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
            List<ToscaTemplate> toscas = new LinkedList();
            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            List<Artifact> resultData = data.getResultData();
            Map<String, Model> outputArtifactMap = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap, resultData);
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap, toscas);
            testResourceTosca(toscas.iterator(), outputArtifactMap);

            System.setProperty(ARTIFACTGENERATOR_CONFIG,CONFIG_PATH+"Artifact-Generator2.properties");
            loadConfig(ArtifactGenerationServiceTest.properties);

            List<ToscaTemplate> toscas2 = new LinkedList();
            for (Artifact inputArtifact : inputArtifacts) {
                toscas2.add(getToscaModel(inputArtifact));
            }
            GenerationData data2 = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
            Map<String, Model> outputArtifactMap2 = new HashMap<>();
            ArtifactGenerationServiceTest.populateAAIGeneratedModelStore(outputArtifactMap2,
                data2.getResultData());
            ArtifactGenerationServiceTest.testServiceTosca(outputArtifactMap2, toscas2);
            testResourceTosca(toscas2.iterator(), outputArtifactMap2);

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            System.setProperty(ARTIFACTGENERATOR_CONFIG,CONFIG_PATH+"Artifact-Generator.properties");
            loadConfig(ArtifactGenerationServiceTest.properties);
        }
    }

    @Test
    public void testArtifactGenerationVerifyMandatoryParameterServiceVersion() {
        try {
            List<Artifact> inputArtifacts = new ArrayList();
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_VerifyMandatoryParameterServiceVersion.yml");
            readPayload(inputArtifacts, fis1, "vf_vmme_template_VerifyMandatoryParameterServiceVersion.yml");

            fis1.close();
            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_VerifyMandatoryParameterServiceVersion.yml");
            readPayload(inputArtifacts, fis2, "service_vmme_template_VerifyMandatoryParameterServiceVersion.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, new HashMap<String, String>());
            List<Artifact> resultData = data.getResultData();

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
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_VerifyServiceVersionFormat.yml");
            readPayload(inputArtifacts, fis1, "vf_vmme_template_VerifyServiceVersionFormat.yml");

            fis1.close();
            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_VerifyServiceVersionFormat.yml");
            readPayload(inputArtifacts, fis2, "service_vmme_template_VerifyServiceVersionFormat.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();

            additionalParams.put(AdditionalParams.ServiceVersion.getName(),"1");
            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
            List<Artifact> resultData = data.getResultData();
            Assert.assertEquals(data.getErrorData().isEmpty(),false);
            Assert.assertEquals(data.getErrorData().get("AAI").get(0),GENERATOR_AAI_INVALID_SERVICE_VERSION);

            additionalParams.put(AdditionalParams.ServiceVersion.getName(),"0.1");
            GenerationData data2 = obj.generateArtifact(inputArtifacts, generatorConfig,
                additionalParams);
            List<Artifact> resultData2 = data.getResultData();
            Assert.assertEquals(data2.getErrorData().isEmpty(),false);
            Assert.assertEquals(data2.getErrorData().get("AAI").get(0),
                GENERATOR_AAI_INVALID_SERVICE_VERSION);

            additionalParams.put(AdditionalParams.ServiceVersion.getName(),"0.0");
            GenerationData data3 = obj.generateArtifact(inputArtifacts, generatorConfig,
                additionalParams);
            List<Artifact> resultData3 = data.getResultData();
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
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_VerifyServiceVersion.yml");
            readPayload(inputArtifacts, fis1, "vf_vmme_template_VerifyServiceVersion.yml");

            fis1.close();
            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_VerifyServiceVersion.yml");
            readPayload(inputArtifacts, fis2, "service_vmme_template_VerifyServiceVersion.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            additionalParams.put(AdditionalParams.ServiceVersion.getName(),"9.0");
            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_VerifyResourceVersionFormat.yml");
            readPayload(inputArtifacts, fis1, "vf_vmme_template_VerifyResourceVersionFormat.yml");
            fis1.close();

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_VerifyResourceVersionFormat1.yml");
            readPayload(inputArtifacts, fis2, "service_vmme_template_VerifyResourceVersionFormat1.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            fis2.close();

            List<ToscaTemplate> toscas = new LinkedList();
            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
            List<Artifact> resultData = data.getResultData();
            Assert.assertEquals(data.getErrorData().isEmpty(),false);
            Assert.assertEquals(data.getErrorData().get("AAI").get(0),
                String.format(GENERATOR_AAI_ERROR_INVALID_RESOURCE_VERSION_IN_SERVICE_TOSCA,
                    toscas.get(0).getMetadata().get("UUID")));

            inputArtifacts.remove(1);

            InputStream fis3 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_VerifyResourceVersionFormat2.yml");
            readPayload(inputArtifacts, fis3, "service_vmme_template_VerifyResourceVersionFormat2.yml");
            fis3.close();
            GenerationData data2 = obj.generateArtifact(inputArtifacts, generatorConfig,
                additionalParams);
            List<Artifact> resultData2 = data2.getResultData();
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
            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/vf_vmme_template_VerifyMandatoryParameterResourceVersion.yml");
            readPayload(inputArtifacts, fis1, "vf_vmme_template_VerifyMandatoryParameterResourceVersion.yml");

            fis1.close();
            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service_vmme_template_VerifyMandatoryParameterResourceVersion.yml");
            readPayload(inputArtifacts, fis2, "service_vmme_template_VerifyMandatoryParameterResourceVersion.yml");
            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();
            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
            List<Artifact> resultData = data.getResultData();
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

            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/service-ServiceWithAllottedResourceIpmux-template.yml");
            readPayload(inputArtifacts, fis1, "service-ServiceWithAllottedResourceIpmux-template.yml");
            fis1.close();

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/resource-AllottedResource-template_IpMux.yml");
            readPayload(inputArtifacts, fis2, "resource-AllottedResource-template_IpMux.yml");
            fis2.close();

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }
            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

            List<Artifact> resultData = data.getResultData();
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

            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream
                ("/service-ServiceWithAllottedResourceIpmux-template_WithGroups.yml");
            readPayload(inputArtifacts, fis1, "service-ServiceWithAllottedResourceIpmux-template_WithGroups.yml");
            fis1.close();

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/resource-AllottedResource-template_IpMux_WithGroups.yml");
            readPayload(inputArtifacts, fis2, "resource-AllottedResource-template_IpMux_WithGroups.yml");
            fis2.close();

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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

            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/service-ServiceWithAllottedResourcesIpMuxSameInvariant-template.yml");
            readPayload(inputArtifacts, fis1, "service-ServiceWithAllottedResourcesIpMuxSameInvariant-template.yml");
            fis1.close();

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/resource-AllottedResource1SameInvariant-IpMux-template.yml");
            readPayload(inputArtifacts, fis2, "resource-AllottedResource1SameInvariant-IpMux-template.yml");
            fis2.close();

            InputStream fis3 = SampleJUnitTest.class.getResourceAsStream("/resource-AllottedResource2SameInvariant-IpMux-template.yml");
            readPayload(inputArtifacts, fis3, "resource-AllottedResource2SameInvariant-IpMux-template.yml");
            fis3.close();

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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

            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/service-ServiceWithAllottedResourcesIpMuxSameInvariantSameVers-template.yml");
            readPayload(inputArtifacts, fis1, "service-ServiceWithAllottedResourcesIpMuxSameInvariantSameVers-template.yml");
            fis1.close();

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/resource-AllottedResourceSameInvariantSameVers-IpMux-template.yml");
            readPayload(inputArtifacts, fis2, "resource-AllottedResourceSameInvariantSameVers-IpMux-template.yml");
            fis2.close();

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service-Allottedipmux-template.yml");
            readPayload(inputArtifacts, fis2, "service-Allottedipmux-template.yml");

            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/resource-IpMuxDemux-template.yml");
            readPayload(inputArtifacts, fis1, "resource-IpMuxDemux-template.yml");

            InputStream fis3 = SampleJUnitTest.class.getResourceAsStream("/resource-TunnelXconn-template.yml");
            readPayload(inputArtifacts, fis3, "resource-TunnelXconn-template.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);

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

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service-SdWan-template_WithOutDepSerId.yml");
            readPayload(inputArtifacts, fis2, "service-SdWan-template_WithOutDepSerId.yml");

            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/resource-VhnfNonHeat-template_WithOutDepSerId.yml");
            readPayload(inputArtifacts, fis1, "resource-VhnfNonHeat-template_WithOutDepSerId.yml");

            InputStream fis3 = SampleJUnitTest.class.getResourceAsStream("/resource-TunnelXconn-template_WithOutDepSerId.yml");
            readPayload(inputArtifacts, fis3, "resource-TunnelXconn-template_WithOutDepSerId.yml");

            InputStream fis4 = SampleJUnitTest.class.getResourceAsStream
                ("/resource-ServiceAdmin-template_WithOutDepSerId.yml");
            readPayload(inputArtifacts, fis4, "resource-ServiceAdmin-template_WithOutDepSerId.yml");

            InputStream fis5 = SampleJUnitTest.class.getResourceAsStream
                ("/resource-IpMuxDemux-template_WithOutDepSerId.yml");
            readPayload(inputArtifacts, fis5, "resource-IpMuxDemux-template_WithOutDepSerId.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();
            for (Artifact inputArtifact : inputArtifacts) {
                toscas.add(getToscaModel(inputArtifact));
            }

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
            List<Artifact> resultData = data.getResultData();
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

            InputStream fis2 = SampleJUnitTest.class.getResourceAsStream("/service-SdWan-template_AllRes_VF.yml");
            readPayload(inputArtifacts, fis2, "service-SdWan-template_AllRes_VF.yml");

            InputStream fis1 = SampleJUnitTest.class.getResourceAsStream("/resource-VhnfNonHeat-template_AllRes_VF.yml");
            readPayload(inputArtifacts, fis1, "resource-VhnfNonHeat-template_AllRes_VF.yml");

            InputStream fis3 = SampleJUnitTest.class.getResourceAsStream("/resource-TunnelXconn-template_AllRes_VF.yml");
            readPayload(inputArtifacts, fis3, "resource-TunnelXconn-template_AllRes_VF.yml");

            InputStream fis4 = SampleJUnitTest.class.getResourceAsStream
                ("/resource-ServiceAdmin-template_AllRes_VF.yml");
            readPayload(inputArtifacts, fis4, "resource-ServiceAdmin-template_AllRes_VF.yml");

            InputStream fis5 = SampleJUnitTest.class.getResourceAsStream
                ("/resource-IpMuxDemux-template_AllRes_VF.yml");
            readPayload(inputArtifacts, fis5, "resource-IpMuxDemux-template_AllRes_VF.yml");

            ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
            List<ToscaTemplate> toscas = new LinkedList();

            GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
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
        Artifact artifact = new Artifact(aaiArtifactType, aaiArtifactGroupType, checksum, encodedPayload);
        artifact.setName(fileName);
        artifact.setLabel(fileName);
        artifact.setDescription(fileName);
        //artifact.setVersion("1.0");
        System.out.println(artifact.getName());
        inputArtifacts.add(artifact);
    }


    /**
     * Get the tosca java model from the tosca input artifact
     *
     * @param input Input tosca file and its metadata information as {@link Artifact} object
     * @return Translated {@link ToscaTemplate tosca} object
     * @throws SecurityException
     */
    public static ToscaTemplate getToscaModel(Artifact input) throws SecurityException {
        byte[] decodedInput = GeneratorUtil.decoder(input.getPayload());
        String checksum = GeneratorUtil.checkSum(decodedInput);
        if (checksum.equals(input.getChecksum())) {
            try {
                return GeneratorUtil.translateTosca(new String(decodedInput), ToscaTemplate.class);
            } catch (Exception e) {
                e.printStackTrace();
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

    /*public static void testResourceTosca(Iterator<ToscaTemplate> itr, Map<String, Model> outputArtifactMap) {
        while(itr.hasNext()){
            ToscaTemplate toscaTemplate = itr.next();
            if("VF".equals(toscaTemplate.getMetadata().get("type"))){
                ArtifactGenerationServiceTest.testResourceTosca(outputArtifactMap, toscaTemplate);
            }
        }
    }*/

}
