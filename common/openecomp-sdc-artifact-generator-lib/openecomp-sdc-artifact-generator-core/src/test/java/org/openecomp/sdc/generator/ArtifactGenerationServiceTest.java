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

import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_CONFIGFILE_NOT_FOUND;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_CONFIGLOCATION_NOT_FOUND;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_CONFIGLPROP_NOT_FOUND;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_PROVIDING_SERVICE_METADATA_MISSING;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_PROVIDING_SERVICE_MISSING;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_ERROR_INVALID_ID;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_ERROR_MISSING_SERVICE_VERSION;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_INVALID_SERVICE_VERSION;

import org.apache.commons.io.FileUtils;
import org.openecomp.sdc.generator.aai.model.Resource;
import org.openecomp.sdc.generator.aai.model.Service;
import org.openecomp.sdc.generator.aai.model.Widget;
import org.openecomp.sdc.generator.aai.tosca.GroupDefinition;
import org.openecomp.sdc.generator.aai.tosca.NodeTemplate;
import org.openecomp.sdc.generator.aai.tosca.ToscaTemplate;
import org.openecomp.sdc.generator.data.AdditionalParams;
import org.openecomp.sdc.generator.aai.xml.Model;
import org.openecomp.sdc.generator.aai.xml.ModelElement;
import org.openecomp.sdc.generator.aai.xml.ModelElements;
import org.openecomp.sdc.generator.aai.xml.ModelVer;
import org.openecomp.sdc.generator.aai.xml.Relationship;
import org.openecomp.sdc.generator.aai.xml.RelationshipData;
import org.openecomp.sdc.generator.data.Artifact;
import org.openecomp.sdc.generator.data.ArtifactType;
import org.openecomp.sdc.generator.data.GenerationData;
import org.openecomp.sdc.generator.data.GeneratorConstants;
import org.openecomp.sdc.generator.data.GeneratorUtil;
import org.openecomp.sdc.generator.data.GroupType;
import org.openecomp.sdc.generator.impl.ArtifactGenerationServiceImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

@SuppressWarnings("Duplicates")
public class ArtifactGenerationServiceTest {

  private static final String aaiArtifactType = ArtifactType.AAI.name();
  private static final String aaiArtifactGroupType = GroupType.DEPLOYMENT.name();
  private static final String generatorConfig = "{\"artifactTypes\": [\"OTHER\",\"AAI\"]}";
  private final Properties properties = new Properties();
  private final Map<String, String> additionalParams = new HashMap<>();
  private final Map<String, String> resourcesVersion = new HashMap<>();

  @BeforeSuite
  public void loadProperties() throws Exception{
    loadConfigFromClasspath(properties);
    additionalParams.put(AdditionalParams.ServiceVersion.getName(), "1.0");
  }

  @Test
  public void testArtifactGeneration() {
    // Sunny day scenario service with VF anf vfmodule
    try {

      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      List<ToscaTemplate> toscas = new LinkedList<>();
      String aaiResourceBasePaths = "aai/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      if (data.getErrorData().isEmpty()) {
        for (Artifact inputArtifact : inputArtifacts) {
          toscas.add(getToscaModel(inputArtifact));
        }
        List<Artifact> resultData = data.getResultData();

        /*for( int i = 0 ; i < resultData.size() ; i++) {
          Artifact artifact = resultData.get(i);
          String fileName = artifact.getName();
          while(fileName.contains(":")){
            fileName = fileName.replace(":","");
          }
          File targetFile =new File("src/test/resources/"+fileName);
          OutputStream outStream = new FileOutputStream(targetFile);
          outStream.write(Base64.getDecoder().decode(artifact.getPayload()));
        }*/

        Assert.assertEquals(resultData.size(),5);  //  1-service,1-VF-resource,1-vfmodule and 2
        // others
        Map<String, Model> outputArtifactMap = populateAAIGeneratedModelStore(resultData);
        testServiceTosca(toscas, outputArtifactMap);
        testResourceTosca(toscas.iterator(), outputArtifactMap);
      } else {
        Assert.fail("error encountered : " + data.getErrorData().get("AAI"));
      }

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testWidgetinServiceTosca() {
    // Sunny day scenario service with VF and extra widget like CP anf vf has vfmodule without
    // member
    try {
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      List<ToscaTemplate> toscas = new LinkedList<>();
      String aaiResourceBasePaths = "testWidgetinServiceTosca/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      if (data.getErrorData().isEmpty()) {
        for (Artifact inputArtifact : inputArtifacts) {
          toscas.add(getToscaModel(inputArtifact));
        }
        List<Artifact> resultData = data.getResultData();
        Assert.assertEquals(resultData.size(),5);  //  1-service,1-VF-resource,1-vfmodule and 2
        // others
        Map<String, Model> outputArtifactMap = populateAAIGeneratedModelStore(resultData);
        testServiceTosca(toscas, outputArtifactMap);
        testResourceTosca(toscas.iterator(), outputArtifactMap);
      } else {
        Assert.fail("error encountered : " + data.getErrorData().get("AAI"));
      }

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testSameVLdifferentVersion() {
    // Sunny day scenario service with VF and extra widget like CP anf vf has vfmodule without
    // member
    try {
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      List<ToscaTemplate> toscas = new LinkedList<>();
      String aaiResourceBasePaths = "testSameVLdifferentVersion/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      if (data.getErrorData().isEmpty()) {
        for (Artifact inputArtifact : inputArtifacts) {
          toscas.add(getToscaModel(inputArtifact));
        }
        List<Artifact> resultData = data.getResultData();
        Assert.assertEquals(resultData.size(),8);
        // others
        Map<String, Model> outputArtifactMap = populateAAIGeneratedModelStore(resultData);
        testServiceTosca(toscas, outputArtifactMap);
        testResourceTosca(toscas.iterator(), outputArtifactMap);
      } else {
        Assert.fail("error encountered : " + data.getErrorData().get("AAI"));
      }

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testArtifactGenerationAllottedResourceAndL3Network() {
    // Sunny day scenario service with allotted resource and L3-network
    try {
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      List<ToscaTemplate> toscas = new LinkedList<>();
      String aaiResourceBasePaths = "aai2/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      if (data.getErrorData().isEmpty()) {
        for (Artifact inputArtifact : inputArtifacts) {
          toscas.add(getToscaModel(inputArtifact));
        }
        List<Artifact> resultData = data.getResultData();

        Assert.assertEquals(resultData.size(),5);
        // and 2
        // others
        Map<String, Model> outputArtifactMap = populateAAIGeneratedModelStore(resultData);
        testServiceTosca(toscas, outputArtifactMap);
        testResourceTosca(toscas.iterator(), outputArtifactMap);
      } else {
        Assert.fail("error encountered : " + data.getErrorData().get("AAI"));
      }

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test(dependsOnMethods = {"testArtifactGeneration"})
  public void testWhenMissingTosca() {
    try {
      //Missing Service tosca test case
      String aaiResourceBasePaths = "testArtifactGeneration2/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(data.getErrorData().get("AAI").get(0),"Service tosca missing from list of input artifacts");
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test(dependsOnMethods = {"testWhenMissingTosca"})
  public void testWhenInvaildConfig() {
    try {
      //Invalid config test case
      String generatorConfig1 = "{\"artifactTypes\": [\"ABC\"]}";
      String aaiResourceBasePaths = "testArtifactGeneration2/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig1, additionalParams);
      Assert.assertEquals(data.getErrorData().get("ARTIFACT_GENERATOR_INVOCATION_ERROR").get(0),"Invalid Client Configuration");
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test(dependsOnMethods = {"testArtifactGeneration"})
  public void testWhenOnlyServToscaNoResTosca() {
    try {
      //Testing only service tosca no resource Tosca
      String aaiResourceBasePaths = "testArtifactGeneration4/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      List<ToscaTemplate> toscas = new LinkedList<>();
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      List<Artifact> resultData = data.getResultData();
      if (data.getErrorData().isEmpty()) {
        for (Artifact inputArtifact : inputArtifacts) {
          toscas.add(getToscaModel(inputArtifact));
        }
      }
      Assert.assertEquals(resultData.size(),3);  //  1-service and 2-Others
      Map<String, Model> outputArtifactMap = populateAAIGeneratedModelStore(resultData);
      testServiceTosca(toscas, outputArtifactMap);
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test(dependsOnMethods = {"testArtifactGeneration"})
  public void testWhenInvaildYaml() {
    try {
      //Invalid Yaml file test case
      String aaiResourceBasePaths = "testArtifactGeneration5/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(data.getErrorData().get("AAI").get(0),"Invalid format for Tosca YML  : " + inputArtifacts.get(0).getName());
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test(dependsOnMethods = {"testArtifactGeneration"})
  public void testWhenExtraResToscaNotPartOfServ() {
    try {
      // Valid scenario with extra resource tosca which is not part of Service
      String aaiResourceBasePaths = "testArtifactGeneration6/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      List<ToscaTemplate> toscas = new LinkedList<>();
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      List<Artifact> resultData = data.getResultData();
      if (data.getErrorData().isEmpty()) {
        for (Artifact inputArtifact : inputArtifacts) {
          toscas.add(getToscaModel(inputArtifact));
        }
      }
      Assert.assertEquals(resultData.size(),3);  //  1-service and 2 Others
      Map<String, Model> outputArtifactMap = populateAAIGeneratedModelStore(resultData);
      testServiceTosca(toscas, outputArtifactMap);
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }


  @Test
  public void testWhenInvUuIdAttrMissing() {
    try {
      // mandatory attribute <invariantUUID> missing
      String aaiResourceBasePaths = "testArtifactGeneration8/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(
          data.getErrorData().get("AAI").get(0),"Invalid Service/Resource definition mandatory attribute <invariantUUID> missing in Artifact: <" +
              inputArtifacts.get(0).getName() + ">");

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testErrorWhenInvalidInvId() {
    try {
      //Invariant Id in service tosca of length not 36
      String aaiResourceBasePaths = "testErrorWhenInvalidInvId/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(
          data.getErrorData().get("AAI").get(0),String.format(GENERATOR_AAI_ERROR_INVALID_ID,
              "invariantUUID",inputArtifacts.get(0).getName()));

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testWhenUuIdAttrMissing() {
    try {
      //mandatory attribute <UUID> missing
      String aaiResourceBasePaths = "testArtifactGeneration9/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(
          data.getErrorData().get("AAI").get(0),"Invalid Service/Resource definition mandatory attribute <UUID> missing in Artifact: <" +
              inputArtifacts.get(0).getName() + ">");

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testErrorWhenInvalidUuId() {
    try {
      //UUID Id in service tosca of length not 36
      String aaiResourceBasePaths = "testErrorWhenInvalidUuId/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(
          data.getErrorData().get("AAI").get(0),String.format(GENERATOR_AAI_ERROR_INVALID_ID,
              "UUID",inputArtifacts.get(0).getName()));

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testWhenNameAttrMissing() {
    try {
      //mandatory attribute <name> missing
      String aaiResourceBasePaths = "testArtifactGeneration10/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(
          data.getErrorData().get("AAI").get(0),"Invalid Service/Resource definition mandatory attribute <name> missing in Artifact: <" +
              inputArtifacts.get(0).getName() + ">");

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test //(dependsOnMethods = {"testArtifactGeneration"})
  public void testWhenVfModInvUuIdAttrMissing() {
    try {
      //mandatory attribute <vfModuleModelInvariantUUID> missing
      List<Artifact> inputArtifacts = new ArrayList<>();
      readPayloadFromResource(inputArtifacts, "service_vmme_template_ModInvUUID.yml");
      readPayloadFromResource(inputArtifacts, "vf_vmme_template_ModInvUUID.yml");
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(
          data.getErrorData().get("AAI").get(0),"Invalid Service/Resource definition mandatory attribute <vfModuleModelInvariantUUID> missing in Artifact: <" +
              inputArtifacts.get(1).getName() + ">");

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  public static void readPayload(List<Artifact> inputArtifacts, InputStream fis, String fileName) throws
      IOException {
    byte[] payload = new byte[fis.available()];
    fis.read(payload);
    String checksum = GeneratorUtil.checkSum(payload);
    byte[] encodedPayload = GeneratorUtil.encode(payload);
    Artifact artifact = new Artifact(aaiArtifactType, aaiArtifactGroupType, checksum, encodedPayload);
    artifact.setName(fileName);
    artifact.setLabel(fileName);
    artifact.setDescription(fileName);
    artifact.setVersion("1.0");
    System.out.println(artifact.getName());
    inputArtifacts.add(artifact);
  }

  @Test
  public void testWhenInvalidVfModInvUuIdAttr() {
    try {
      //invalid id since not of length 36 for  <vfModuleModelInvariantUUID>
      List<Artifact> inputArtifacts = new ArrayList<>();
      readPayloadFromResource(inputArtifacts, "service_vmme_template_InvalidVfModInvUuIdAttr.yml");

      readPayloadFromResource(inputArtifacts, "vf_vmme_template_InvalidVfModInvUuIdAttr.yml");
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(
          data.getErrorData().get("AAI").get(0),String.format(GENERATOR_AAI_ERROR_INVALID_ID,
              "vfModuleModelInvariantUUID", inputArtifacts.get(1).getName() ));

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test //(dependsOnMethods = {"testArtifactGeneration"})
  public void testWhenVfModNameAttrMissing() {
    try {
      //mandatory attribute <vfModuleModelName> missing
      List<Artifact> inputArtifacts = new ArrayList<>();
      readPayloadFromResource(inputArtifacts, "service_vmme_template_ModelName.yml");

      readPayloadFromResource(inputArtifacts, "vf_vmme_template_ModelName.yml");
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(
          data.getErrorData().get("AAI").get(0),"Invalid Service/Resource definition mandatory attribute <vfModuleModelName> missing in Artifact: <" +
              inputArtifacts.get(1).getName() + ">");

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  /*public static void readPayload(List<Artifact> inputArtifacts,InputStream fis, String fileName)
      throws
      IOException {
    byte[] payload = new byte[fis.available()];
    fis.read(payload);
    String checksum = GeneratorUtil.checkSum(payload);
    byte[] encodedPayload = GeneratorUtil.encode(payload);
    Artifact artifact = new Artifact(aaiArtifactType, aaiArtifactGroupType, checksum, encodedPayload);
    artifact.setName(fileName);
    artifact.setLabel(fileName);
    artifact.setDescription(fileName);
    artifact.setVersion("1.0");
    System.out.println(artifact.getName());
    inputArtifacts.add(artifact);
  }*/

  @Test //(dependsOnMethods = {"testArtifactGeneration"})
  public void testWhenVfModUuIdAttrMissing() {
    try {
      //mandatory attribute <vfModuleModelUUID> missing in Artifact
      List<Artifact> inputArtifacts = new ArrayList<>();
      readPayloadFromResource(inputArtifacts, "service_vmme_template_ModelUUID.yml");

      readPayloadFromResource(inputArtifacts, "vf_vmme_template_ModelUUID.yml");
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(
          data.getErrorData().get("AAI").get(0),"Invalid Service/Resource definition mandatory " +
              "attribute <vfModuleModelUUID> missing in Artifact: <" +
              inputArtifacts.get(1).getName() + ">");

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testWhenInvalidVfModUuIdAttr() {
    try {
      //invalid id since not of length 36 for  <vfModuleModelUUID>
      List<Artifact> inputArtifacts = new ArrayList<>();
      readPayloadFromResource(inputArtifacts, "service_vmme_template_InvalidVfModUuIdAttr.yml");

      readPayloadFromResource(inputArtifacts, "vf_vmme_template_InvalidVfModUuIdAttr.yml");
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(
          data.getErrorData().get("AAI").get(0),String.format(GENERATOR_AAI_ERROR_INVALID_ID,
              "vfModuleModelUUID", inputArtifacts.get(1).getName() ));

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test //(dependsOnMethods = {"testArtifactGeneration"})
  public void testWhenVfModVersionAttrMissing() {
    try {
      //mandatory attribute <vfModuleModelVersion> missing
      List<Artifact> inputArtifacts = new ArrayList<>();
      readPayloadFromResource(inputArtifacts, "service_vmme_template_ModelVersion.yml");

      readPayloadFromResource(inputArtifacts, "vf_vmme_template_ModelVersion.yml");
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(
          data.getErrorData().get("AAI").get(0),"Invalid Service/Resource definition mandatory attribute <vfModuleModelVersion> missing in Artifact: <" +
              inputArtifacts.get(1).getName() + ">");

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testErrorWhenNoSystemPropConfigured() throws Exception  {
    String configLoc = System.getProperty("artifactgenerator.config");
    try {
      System.clearProperty("artifactgenerator.config");
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      String aaiResourceBasePaths = "aai/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(false,data.getErrorData().isEmpty());
      Assert.assertEquals(data.getErrorData().
          get("AAI").get(0),GENERATOR_AAI_CONFIGLOCATION_NOT_FOUND);
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    finally{
      System.setProperty("artifactgenerator.config",configLoc);
    }
  }

  @Test
  public void testErrorWhenNoWidgetInConfig() throws Exception  {
    String configLoc = System.getProperty("artifactgenerator.config");
    final File configFile = new File(configLoc);
    String configDir = configLoc.substring(0, configLoc.lastIndexOf(File.separator));
    final File tempFile = new File(configDir + File.separator + "temp.properties");
    try {
      //copy orignal Artifact-Generator.properties to temp.properties for backup
      FileUtils.copyFile(configFile, tempFile);

      String serviceWidgetName = ArtifactType.AAI.name()+".model-version-id."+Widget.getWidget
          (Widget.Type.SERVICE)
          .getName();
      String assertMsg = ArtifactType.AAI.name() + ".model-version-id." +Widget.getWidget
          (Widget.Type.SERVICE).getName();

      //Remove property from Artifact-Generator.properties
      properties.remove(serviceWidgetName);
      try (OutputStream fos = new FileOutputStream(new File(configLoc))) {
        properties.store(fos,null);
      }

      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      String aaiResourceBasePaths = "aai/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(false,data.getErrorData().isEmpty());
      String errMsg = String.format(GENERATOR_AAI_CONFIGLPROP_NOT_FOUND,assertMsg);
      Assert.assertEquals(data.getErrorData().get("AAI").get(0),errMsg);
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    finally{
      if(tempFile.exists()){
        //Revert the changes
        FileUtils.copyFile(tempFile, configFile);
        loadConfigFromClasspath(properties);
        tempFile.delete();
      }
    }
  }

  @Test
  public void testErrorWhenNoFileAtConfigLocation() throws Exception  {
    String configLoc = System.getProperty("artifactgenerator.config");
    try {
      System.setProperty("artifactgenerator.config",configLoc + File.separator + "testErrorWhenNoFileAtConfigLocation");
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      String aaiResourceBasePaths = "aai/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(data.getErrorData().isEmpty(),false);
      Assert.assertEquals(data.getErrorData().get("AAI").get(0),String.format(GENERATOR_AAI_CONFIGFILE_NOT_FOUND,System.getProperty
          ("artifactgenerator.config")));
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    finally{
      System.setProperty("artifactgenerator.config",configLoc);
    }
  }

  @Test
  public void testErrorWhenNoServiceVersion() {
    //  scenario service with VF anf vfmodule but no service version in additional parameter
    try {
      additionalParams.clear();
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      String aaiResourceBasePaths = "aai/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(data.getErrorData().isEmpty(),false);
      Assert.assertEquals(data.getErrorData().get("AAI").get(0),
          GENERATOR_AAI_ERROR_MISSING_SERVICE_VERSION);
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    finally{
      additionalParams.put(AdditionalParams.ServiceVersion.getName(),"1.0");
    }
  }

  @Test
  public void testArtifactGenerationWithServiceVersion() {
    // Sunny day scenario service with VF anf vfmodule and service version as adiitional parameter
    try {
      additionalParams.put(AdditionalParams.ServiceVersion.getName(),"9.0");
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      List<ToscaTemplate> toscas = new LinkedList<>();
      String aaiResourceBasePaths = "aai/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      if (data.getErrorData().isEmpty()) {
        for (Artifact inputArtifact : inputArtifacts) {
          toscas.add(getToscaModel(inputArtifact));
        }
        List<Artifact> resultData = data.getResultData();

        /*for( int i = 0 ; i < resultData.size() ; i++) {
          Artifact artifact = resultData.get(i);
          String fileName = artifact.getName();
          while(fileName.contains(":")){
            fileName = fileName.replace(":","");
          }
          File targetFile =new File("src/test/resources/"+fileName);
          OutputStream outStream = new FileOutputStream(targetFile);
          outStream.write(Base64.getDecoder().decode(artifact.getPayload()));
        }*/

        Assert.assertEquals(resultData.size(),5);  //  1-service,1-VF-resource,1-vfmodule and 2
        // others
        Map<String, Model> outputArtifactMap = populateAAIGeneratedModelStore(resultData);
        testServiceTosca(toscas, outputArtifactMap);
        testResourceTosca(toscas.iterator(), outputArtifactMap);
      } else {
        Assert.fail("error encountered : " + data.getErrorData().get("AAI"));
      }

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    finally{
      additionalParams.put(AdditionalParams.ServiceVersion.getName(),"1.0");
    }
  }

  @Test
  public void testErrorWhenInvalidServiceVersion() {
    //  scenario service with VF anf vfmodule but invalid service version in additional parameter
    try {
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      String aaiResourceBasePaths = "aai/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);

      additionalParams.put(AdditionalParams.ServiceVersion.getName(),"1");
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(data.getErrorData().isEmpty(),false);
      Assert.assertEquals(data.getErrorData().get("AAI").get(0),
          GENERATOR_AAI_INVALID_SERVICE_VERSION);

      additionalParams.put(AdditionalParams.ServiceVersion.getName(),"0.1");
      data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(data.getErrorData().isEmpty(),false);
      Assert.assertEquals(data.getErrorData().get("AAI").get(0),
          GENERATOR_AAI_INVALID_SERVICE_VERSION);

      additionalParams.put(AdditionalParams.ServiceVersion.getName(),"0.0");
      data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(data.getErrorData().isEmpty(),false);
      Assert.assertEquals(data.getErrorData().get("AAI").get(0),
          GENERATOR_AAI_INVALID_SERVICE_VERSION);

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
    finally{
      additionalParams.put(AdditionalParams.ServiceVersion.getName(),"1.0");
    }
  }

  @Test
  public void testMissingResourceTosca() {
    try {
      //Service with resource but seperate resource tosca not coming as input.
      String aaiResourceBasePaths = "missingResourceTosca/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(data.getErrorData().get("AAI").get(0),"Cannot generate artifacts. Resource Tosca missing for resource with UUID: <b020ed1e-4bc7-4fc0-ba7e-cc7af6da7ffc>");
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }
  @Test
  public void testMissingVLTosca() {
    try {
      //Service with VL but seperate VL tosca not coming as input artifact.
      String aaiResourceBasePaths = "missingVLTosca/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(data.getErrorData().get("AAI").get(0),"Cannot generate artifacts. Resource Tosca missing for resource with UUID: <3f8fa4d2-2b86-4b36-bbc8-ffb8f9f57468>");
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testErrorWhenNoResourceVersion() {
    //  scenario service with VF but missing resource version in service tosca
    try {
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      String aaiResourceBasePaths = "testErrorWhenNoResourceVersion/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(data.getErrorData().isEmpty(),false);
      Assert.assertEquals(data.getErrorData().get("AAI").get(0),
          "Invalid Service definition mandatory attribute version missing for resource with UUID: <b020ed1e-4bc7-4fc0-ba7e-cc7af6da7ffc>");
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testErrorWhenInvalidResourceVersion1() {
    //  scenario service with VF but invalid resource version 0.0 in service tosca
    try {
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      String aaiResourceBasePaths = "testErrorWhenInvalidResourceVersion1/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(data.getErrorData().isEmpty(),false);
      Assert.assertEquals(data.getErrorData().get("AAI").get(0),
          "Cannot generate artifacts. Invalid Resource version in Service tosca for resource with UUID: <b020ed1e-4bc7-4fc0-ba7e-cc7af6da7ffc>");
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testErrorWhenInvalidResourceVersion2() {
    //  scenario service with VF but invalid resource version 1 in service tosca
    try {
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      String aaiResourceBasePaths = "testErrorWhenInvalidResourceVersion2/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(data.getErrorData().isEmpty(),false);
      Assert.assertEquals(data.getErrorData().get("AAI").get(0),
          "Cannot generate artifacts. Invalid Resource version in Service tosca for resource with UUID: <b020ed1e-4bc7-4fc0-ba7e-cc7af6da7ffc>");
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testArtifactGenerationAllottedResourceWithIpMuxAndTunnelXConn() {
    try {
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      List<ToscaTemplate> toscas = new LinkedList<>();
      String aaiResourceBasePaths = "testArtifactGeneration15/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      if (data.getErrorData().isEmpty()) {
        for (Artifact inputArtifact : inputArtifacts) {
          toscas.add(getToscaModel(inputArtifact));
        }
        List<Artifact> resultData = data.getResultData();

        Assert.assertEquals(resultData.size(),5);
        Map<String, Model> outputArtifactMap = populateAAIGeneratedModelStore(resultData);
        testServiceTosca(toscas, outputArtifactMap);
        testResourceTosca(toscas.iterator(), outputArtifactMap);
      } else {
        Assert.fail("error encountered : " + data.getErrorData().get("AAI"));
      }

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

//  @Test
  public void testErrorWhenAllottedResourceWithOutProvidingServiceId() {
    try {
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      String aaiResourceBasePaths = "testErrorWhenAllottedResourceWithOutDependingServiceId/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(data.getErrorData().isEmpty(),false);
      Assert.assertEquals(data.getErrorData().get("AAI").get(0),String.format
              (GENERATOR_AAI_PROVIDING_SERVICE_METADATA_MISSING, "707b2850-e830-4b00-9902-879f44ac05a4"));
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testArtifactGenerationWithoutAllottedResource() {
    try {
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      String aaiResourceBasePaths = "testArtifactGeneration16/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      Assert.assertEquals(data.getErrorData().isEmpty(),false);
      Assert.assertEquals(data.getErrorData().get("AAI").get(0),String.format
          (GENERATOR_AAI_PROVIDING_SERVICE_MISSING, "a54a5235-b69d-4f8a-838b-d011e6783fa5"));
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testArtifactGenerationAllottedResourceIpmuxSameInvariantDiffVersion() {
    try {
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      List<ToscaTemplate> toscas = new LinkedList<>();
      String aaiResourceBasePaths = "testArtifactGeneration17/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      if (data.getErrorData().isEmpty()) {
        for (Artifact inputArtifact : inputArtifacts) {
          toscas.add(getToscaModel(inputArtifact));
        }
        List<Artifact> resultData = data.getResultData();

        Assert.assertEquals(resultData.size(),5);
        Map<String, Model> outputArtifactMap = populateAAIGeneratedModelStore(resultData);
        testServiceTosca(toscas, outputArtifactMap);
        testResourceTosca(toscas.iterator(), outputArtifactMap);
      } else {
        Assert.fail("error encountered : " + data.getErrorData().get("AAI"));
      }

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testArtifactGenerationAllottedResourceIpmuxSameInvariantSameVersion() {
    try {
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      List<ToscaTemplate> toscas = new LinkedList<>();
      String aaiResourceBasePaths = "testArtifactGeneration18/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      if (data.getErrorData().isEmpty()) {
        for (Artifact inputArtifact : inputArtifacts) {
          toscas.add(getToscaModel(inputArtifact));
        }
        List<Artifact> resultData = data.getResultData();


        Assert.assertEquals(resultData.size(),4);
        Map<String, Model> outputArtifactMap = populateAAIGeneratedModelStore(resultData);
        testServiceTosca(toscas, outputArtifactMap);
        testResourceTosca(toscas.iterator(), outputArtifactMap);
      } else {
        Assert.fail("error encountered : " + data.getErrorData().get("AAI"));
      }

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testArtifactGenerationAllottedResourceIpmuxWithGroups() {
    try {
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      List<ToscaTemplate> toscas = new LinkedList<>();
      String aaiResourceBasePaths = "testAllotedResourceWithDependingSerWithGroups/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      if (data.getErrorData().isEmpty()) {
        for (Artifact inputArtifact : inputArtifacts) {
          toscas.add(getToscaModel(inputArtifact));
        }
        List<Artifact> resultData = data.getResultData();

        Assert.assertEquals(resultData.size(),5);
        Map<String, Model> outputArtifactMap = populateAAIGeneratedModelStore(resultData);
        testServiceTosca(toscas, outputArtifactMap);
        testResourceTosca(toscas.iterator(), outputArtifactMap);
      } else {
        Assert.fail("error encountered : " + data.getErrorData().get("AAI"));
      }

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testArtifactGenerationAllottedResourceWithVF() {
    try {
      ArtifactGenerationServiceImpl obj = new ArtifactGenerationServiceImpl();
      List<ToscaTemplate> toscas = new LinkedList<>();
      String aaiResourceBasePaths = "testArtifactGenerationAllottedResourceWithVF/";
      List<Artifact> inputArtifacts = init(aaiResourceBasePaths);
      GenerationData data = obj.generateArtifact(inputArtifacts, generatorConfig, additionalParams);
      if (data.getErrorData().isEmpty()) {
        for (Artifact inputArtifact : inputArtifacts) {
          toscas.add(getToscaModel(inputArtifact));
        }
        List<Artifact> resultData = data.getResultData();

        Assert.assertEquals(resultData.size(),7);
        Map<String, Model> outputArtifactMap = populateAAIGeneratedModelStore(resultData);
        testServiceTosca(toscas, outputArtifactMap);
        testResourceTosca(toscas.iterator(), outputArtifactMap);
      } else {
        Assert.fail("error encountered : " + data.getErrorData().get("AAI"));
      }

    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }


  //@Test
  public void testServiceTosca(List<ToscaTemplate> toscas, Map<String, Model> outputArtifactMap) {
    try {
      ToscaTemplate serviceTosca = getServiceTosca(toscas);
      if (serviceTosca == null) {
        Assert.fail("Service Tosca not found");
      }
      serviceTosca.getMetadata().put("version", "1.0");
      Service service = new Service();
      service.populateModelIdentificationInformation(serviceTosca.getMetadata());
      String serviceNameVersionId = service.getModelNameVersionId();
      Model serviceAAIModel = getAAIModelByNameVersionId(serviceNameVersionId, outputArtifactMap);
      validateServiceModelMetadata(service, serviceAAIModel);
      //Validate Service instance base widget
      ModelVer modelVersion =  serviceAAIModel.getModelVers().getModelVer().get(0);

      List<ModelElement> matchedServiceBaseWidgetElements =
          getModelElementbyRelationshipValue( modelVersion.getModelElements(),
              Widget.getWidget(Widget.Type.SERVICE).getId());
      validateMatchedModelElementsInService(matchedServiceBaseWidgetElements,
          Widget.getWidget(Widget.Type.SERVICE).getName());

      validateWidgetIds(matchedServiceBaseWidgetElements, Widget.getWidget(Widget.Type.SERVICE).getName(),
          Widget.getWidget(Widget.Type.SERVICE).getWidgetId());

      ModelElements baseServiceWidgetModelElements =
          matchedServiceBaseWidgetElements.get(0).getModelElements();


      Map<String, String> nodeTemplateIdTypeStore = getNodeTemplateTypeStore(serviceTosca);
      if (nodeTemplateIdTypeStore != null) {
        for (String key : nodeTemplateIdTypeStore.keySet()) {
          if (nodeTemplateIdTypeStore.get(key).contains("org.openecomp.resource.vf")) {
            List<ModelElement> matchedResourceElements =
                getModelElementbyRelationshipValue(baseServiceWidgetModelElements, key);
            if (nodeTemplateIdTypeStore.get(key).contains("org.openecomp.resource.vf.allottedResource")){
              validateMatchedModelElementsInService(matchedResourceElements,
                  Widget.getWidget(Widget.Type.ALLOTTED_RESOURCE).getName());
            }else {
              validateMatchedModelElementsInService(matchedResourceElements,
                  Widget.getWidget(Widget.Type.VF).getName());
            }

            //Validate uuid and invariantuuid are populated in model-ver.model-version-id and model.model-invariant-id
            Assert.assertEquals(matchedResourceElements.get(0).getRelationshipList()
                .getRelationship().get(0)
                .getRelationshipData().get(0).getRelationshipValue(),key);

            Assert.assertEquals(matchedResourceElements.get(0).getRelationshipList().getRelationship().get(0)
                .getRelationshipData().get(1).getRelationshipValue(), nodeTemplateIdTypeStore
                .get(key+"-INV_UID"));
          } else if(nodeTemplateIdTypeStore.get(key).contains("org.openecomp.resource.vl")){
            //validate l3-network in service tosca
            List<ModelElement> matchedResourceElements =
                getModelElementbyRelationshipValue(baseServiceWidgetModelElements, key);
            validateMatchedModelElementsInService(matchedResourceElements,
                Widget.getWidget(Widget.Type.L3_NET).getName());
            //Validate uuid and invariantuuid are populated in model-ver.model-version-id and model.model-invariant-id
            Assert.assertEquals(matchedResourceElements.get(0).getRelationshipList()
                .getRelationship().get(0)
                .getRelationshipData().get(0).getRelationshipValue(),key);

            Assert.assertEquals(matchedResourceElements.get(0).getRelationshipList().getRelationship().get(0)
                .getRelationshipData().get(1).getRelationshipValue(), nodeTemplateIdTypeStore
                .get(key+"-INV_UID"));
          }
        }


      System.out.println();

      }
    } catch (IllegalArgumentException e) {
      Assert.fail(e.getMessage());    //Can come while populating metadata
    }
  }

  private void validateWidgetIds(List<ModelElement> matchedServiceBaseWidgetElements,
                                 String widgetName, String widgetInvUuId) {
    Assert.assertEquals(matchedServiceBaseWidgetElements.get(0).getRelationshipList().getRelationship().get(0)
        .getRelationshipData().get(0).getRelationshipValue(), properties.getProperty(ArtifactType.AAI.name()
        + ".model-version-id."+ widgetName));

    Assert.assertEquals(matchedServiceBaseWidgetElements.get(0).getRelationshipList().getRelationship().get(0)
        .getRelationshipData().get(1).getRelationshipValue(), widgetInvUuId);
  }


  public void testL3NetworkResourceTosca(Map<String, Model> outputArtifactMap , ToscaTemplate
      resourceTosca) {
    try {
      if (resourceTosca != null) {
        Resource resource = new Resource();
        resource.populateModelIdentificationInformation(resourceTosca.getMetadata());
        String resourceNameVersionId = resource.getModelNameVersionId();
        Model resourceAAIModel =
            getAAIModelByNameVersionId(resourceNameVersionId, outputArtifactMap);
        if (resourceAAIModel != null) {
          validateResourceModelMetadata(resource, resourceAAIModel);
          //Validate Resource instance base widget

          ModelVer modelVersion = resourceAAIModel.getModelVers().getModelVer().get(0);

          List<ModelElement> matchedVFBaseWidgetElements =
              getModelElementbyRelationshipValue(modelVersion.getModelElements(),
                  Widget.getWidget(Widget.Type.L3_NET).getId());
          validateMatchedModelElementsInService(matchedVFBaseWidgetElements,
              Widget.getWidget(Widget.Type.L3_NET).getName());

          validateWidgetIds(matchedVFBaseWidgetElements, Widget.getWidget(Widget.Type.L3_NET).getName(),
              Widget.getWidget(Widget.Type.L3_NET).getWidgetId());

        }else {
          System.out.println("Resource mapping not found for " + resourceNameVersionId);
        }
      }

    }catch (IllegalArgumentException e) {
      Assert.fail(e.getMessage());    //Can come while populating metadata
    }

  }

  public void testAllottedResourceTosca(Map<String, Model> outputArtifactMap , ToscaTemplate
      resourceTosca) {
    try {
      if (resourceTosca != null) {
        Resource resource = new Resource();
        resource.populateModelIdentificationInformation(resourceTosca.getMetadata());
        String resourceNameVersionId = resource.getModelNameVersionId();
        Model resourceAAIModel =
            getAAIModelByNameVersionId(resourceNameVersionId, outputArtifactMap);
        if (resourceAAIModel != null) {
          validateResourceModelMetadata(resource, resourceAAIModel);
          //Validate Resource instance base widget

          ModelVer modelVersion = resourceAAIModel.getModelVers().getModelVer().get(0);

          List<ModelElement> matchedVFBaseWidgetElements =
              getModelElementbyRelationshipValue(modelVersion.getModelElements(),
                  Widget.getWidget(Widget.Type.ALLOTTED_RESOURCE).getId());
          validateMatchedModelElementsInService(matchedVFBaseWidgetElements,
              Widget.getWidget(Widget.Type.ALLOTTED_RESOURCE).getName());

          validateWidgetIds(matchedVFBaseWidgetElements, Widget.getWidget(Widget.Type.ALLOTTED_RESOURCE).getName(),
              Widget.getWidget(Widget.Type.ALLOTTED_RESOURCE).getWidgetId());

          Map<String, Object> dependingServiceDetails = getProvidingServiceDetails(resourceTosca);

          ModelElements containedModelElements = modelVersion.getModelElements().getModelElement().
                  get(0).getModelElements();
          Assert.assertEquals( containedModelElements.getModelElement().get(0).getRelationshipList()
                .getRelationship().get(0).getRelationshipData().get(0).getRelationshipValue(),
                dependingServiceDetails.get("providing_service_uuid"));

          Assert.assertEquals(containedModelElements.getModelElement().get(0).getRelationshipList()
                .getRelationship().get(0).getRelationshipData().get(1).getRelationshipValue(),
                dependingServiceDetails.get("providing_service_invariant_uuid"));

          if("Allotted Resource".equals(resourceTosca.getMetadata().get("category")) &&
                  "Tunnel XConnect".equals(resourceTosca.getMetadata().get("subcategory"))) {

            List<ModelElement> matchedTunnelXConnectWidgetElements =
                    getModelElementbyRelationshipValue(containedModelElements,
                            Widget.getWidget(Widget.Type.TUNNEL_XCONNECT).getId());
            validateMatchedModelElementsInService(matchedTunnelXConnectWidgetElements,
                    Widget.getWidget(Widget.Type.TUNNEL_XCONNECT).getName());

            validateWidgetIds(matchedTunnelXConnectWidgetElements, Widget.getWidget(Widget.Type.TUNNEL_XCONNECT).getName(),
                    Widget.getWidget(Widget.Type.TUNNEL_XCONNECT).getWidgetId());
          }

        }else {
          System.out.println("Resource mapping not found for " + resourceNameVersionId);
        }
      }

    }catch (IllegalArgumentException e) {
      Assert.fail(e.getMessage());    //Can come while populating metadata
    }

  }

  public Map<String, Object> getProvidingServiceDetails(ToscaTemplate resourceTemplate) {
Set<String> keys = resourceTemplate.getTopology_template().getNode_templates().keySet();

Map<String, Object> nodeProperties =null;
for(String key : keys) {
NodeTemplate node = resourceTemplate.getTopology_template().getNode_templates().get(key);
if(node.getType().equals("org.openecomp.resource.vfc.AllottedResource")) {
nodeProperties = node.getProperties();
  }
}

  return nodeProperties;
  }

  public void testVfTosca(Map<String, Model> outputArtifactMap , ToscaTemplate resourceTosca) {
    try {
      //ToscaTemplate resourceTosca = getResourceTosca(toscas);
      //resourceTosca.getTopology_template().getGroups().
      if (resourceTosca != null) {
        Resource resource = new Resource();
        resource.populateModelIdentificationInformation(resourceTosca.getMetadata());
        String resourceNameVersionId = resource.getModelNameVersionId();
        Model resourceAAIModel =
            getAAIModelByNameVersionId(resourceNameVersionId, outputArtifactMap);
        if (resourceAAIModel != null) {
          validateResourceModelMetadata(resource, resourceAAIModel);
          //Validate Resource instance base widget

          ModelVer modelVersion = resourceAAIModel.getModelVers().getModelVer().get(0);

          List<ModelElement> matchedVFBaseWidgetElements =
              getModelElementbyRelationshipValue(modelVersion.getModelElements(),
                  Widget.getWidget(Widget.Type.VF).getId());
          validateMatchedModelElementsInService(matchedVFBaseWidgetElements,
              Widget.getWidget(Widget.Type.VF).getName());

          validateWidgetIds(matchedVFBaseWidgetElements, Widget.getWidget(Widget.Type.VF).getName(),
                Widget.getWidget(Widget.Type.VF).getWidgetId());

          ModelElements baseResourceWidgetModelElements =
              matchedVFBaseWidgetElements.get(0).getModelElements();
          if (resourceTosca.getTopology_template() != null) {
            Map<String, String> groupIdTypeStore = getGroupsTypeStore(resourceTosca);

            if (baseResourceWidgetModelElements.getModelElement().size() !=
                groupIdTypeStore.size()) {
              Assert.fail("Missing VFModule in VF model.xml");
            }

            for (String key : groupIdTypeStore.keySet()) {

              List<ModelElement> matchedResourceElements =
                  getModelElementbyRelationshipValue(baseResourceWidgetModelElements, key);
              validateMatchedModelElementsInService(matchedResourceElements,
                  Widget.getWidget(Widget.Type.VFMODULE).getName());
              Model resourceAAIVFModel = getAAIModelByNameVersionId(key, outputArtifactMap);
              Map<String, String> vfModuleModelMetadata =
                  getVFModuleMetadataTosca(resourceTosca, key);
              Map<String, Object> vfModuleMembers = getVFModuleMembersTosca(resourceTosca, key);

              validateVFModelMetadata(vfModuleModelMetadata, resourceAAIVFModel);


              ModelVer modelVfVersion = resourceAAIVFModel.getModelVers().getModelVer().get(0);

              List<ModelElement> matchedVFModuleBaseWidgetElements =
                  getModelElementbyRelationshipValue(modelVfVersion.getModelElements(),
                      Widget.getWidget(Widget.Type.VFMODULE).getId());
              validateMatchedModelElementsInService(matchedVFModuleBaseWidgetElements,
                  Widget.getWidget(Widget.Type.VFMODULE).getName());
              validateWidgetIds(matchedVFModuleBaseWidgetElements, Widget.getWidget(Widget.Type.VFMODULE)
                  .getName(), Widget.getWidget(Widget.Type.VFMODULE).getWidgetId());

              ModelElements baseResourceVFModuleWidgetModelElements =
                  matchedVFModuleBaseWidgetElements.get(0).getModelElements();
             if (vfModuleMembers.containsKey("l3-network")) {
                //Validate l3
                List<ModelElement> matchedL3NetworkElements =
                    getModelElementbyRelationshipValue(baseResourceVFModuleWidgetModelElements,
                        Widget.getWidget(Widget.Type.L3_NET).getId());
                validateMatchedModelElementsInService(matchedL3NetworkElements,
                    Widget.getWidget(Widget.Type.L3_NET).getName());
                validateWidgetIds(matchedL3NetworkElements, Widget.getWidget(Widget.Type.L3_NET)
                    .getName(), Widget.getWidget(Widget.Type.L3_NET).getWidgetId());
              }
              if (vfModuleMembers.containsKey("vserver")) {
                //Validate vserver
                List<ModelElement> matchedVserverElements =
                    getModelElementbyRelationshipValue(baseResourceVFModuleWidgetModelElements,
                        Widget.getWidget(Widget.Type.VSERVER).getId());
                validateMatchedModelElementsInService(matchedVserverElements,
                    Widget.getWidget(Widget.Type.VSERVER).getName());
                ModelElements vserverWidgetModelElements =
                    matchedVserverElements.get(0).getModelElements();

                validateWidgetIds(matchedVserverElements, Widget.getWidget(Widget.Type.VSERVER)
                    .getName(), Widget.getWidget(Widget.Type.VSERVER).getWidgetId());


                //Validate vserver->vfc
                List<ModelElement> matchedVfcElements =
                    getModelElementbyRelationshipValue(vserverWidgetModelElements,
                        Widget.getWidget(Widget.Type.VFC).getId());
                validateMatchedModelElementsInService(matchedVfcElements,
                    Widget.getWidget(Widget.Type.VFC).getName());
                validateWidgetIds(matchedVfcElements, Widget.getWidget(Widget.Type.VFC).getName(),
                    Widget.getWidget(Widget.Type.VFC).getWidgetId());

                //Validate vserver->Image
                List<ModelElement> matchedImageElements =
                    getModelElementbyRelationshipValue(vserverWidgetModelElements,
                        Widget.getWidget(Widget.Type.IMAGE).getId());
                validateMatchedModelElementsInService(matchedImageElements,
                    Widget.getWidget(Widget.Type.IMAGE).getName());
                validateWidgetIds(matchedImageElements, Widget.getWidget(Widget.Type.IMAGE)
                    .getName(), Widget.getWidget(Widget.Type.IMAGE).getWidgetId());


                //Validate vserver->Flavor
                List<ModelElement> matchedFlavorElements =
                    getModelElementbyRelationshipValue(vserverWidgetModelElements,
                        Widget.getWidget(Widget.Type.FLAVOR).getId());
                validateMatchedModelElementsInService(matchedFlavorElements,
                    Widget.getWidget(Widget.Type.FLAVOR).getName());
                validateWidgetIds(matchedFlavorElements, Widget.getWidget(Widget.Type.FLAVOR).getName(),
                    Widget.getWidget(Widget.Type.FLAVOR).getWidgetId());

                //Validate vserver->Tenant
                List<ModelElement> matchedTenantElements =
                    getModelElementbyRelationshipValue(vserverWidgetModelElements,
                        Widget.getWidget(Widget.Type.TENANT).getId());
                validateMatchedModelElementsInService(matchedTenantElements,
                    Widget.getWidget(Widget.Type.TENANT).getName());
                validateWidgetIds(matchedTenantElements, Widget.getWidget(Widget.Type.TENANT).getName(),
                    Widget.getWidget(Widget.Type.TENANT).getWidgetId());

                //Validate vserver->l-interface
                if (vfModuleMembers.containsKey("l-interface")) {
                  List<ModelElement> matchedLinterfaceElements =
                      getModelElementbyRelationshipValue(vserverWidgetModelElements,
                          Widget.getWidget(Widget.Type.LINT).getId());
                  validateMatchedModelElementsInService(matchedLinterfaceElements,
                      Widget.getWidget(Widget.Type.LINT).getName());
                  validateWidgetIds(matchedLinterfaceElements, Widget.getWidget(Widget.Type.LINT).getName(),
                      Widget.getWidget(Widget.Type.LINT).getWidgetId());
                }
                //Validate vserver->volume
                if (vfModuleMembers.containsKey("volume")) {
                  List<ModelElement> matchedVolumeElements =
                      getModelElementbyRelationshipValue(vserverWidgetModelElements,
                          Widget.getWidget(Widget.Type.VOLUME).getId());
                  validateMatchedModelElementsInService(matchedVolumeElements,
                      Widget.getWidget(Widget.Type.VOLUME).getName());
                  validateWidgetIds(matchedVolumeElements, Widget.getWidget(Widget.Type.VOLUME).getName(),
                      Widget.getWidget(Widget.Type.VOLUME).getWidgetId());
                }
              }
            }
          }
        } else {
          System.out.println("Resource mapping not found for " + resourceNameVersionId);
        }
      }

    } catch (IllegalArgumentException e) {
      Assert.fail(e.getMessage());    //Can come while populating metadata
    }

  }

  private void validateMatchedModelElementsInService(List<ModelElement> matchedModelElements,
                                                     String modelType) {
    if (matchedModelElements.isEmpty()) {
      Assert.fail(modelType + " not present ");
    }
    if (matchedModelElements.size() > 1) {
      Assert.fail("More than one " + modelType + " present ");
    }
  }

  private Map<String, String> getNodeTemplateTypeStore(ToscaTemplate toscaTemplate) {
    if (toscaTemplate.getTopology_template() != null) {
      Map<String, NodeTemplate> nodeTemplateMap =
          toscaTemplate.getTopology_template().getNode_templates();
      Map<String, String> nodeTemplateIdTypeStore = new LinkedHashMap<>();
      if (nodeTemplateMap != null) {
        for (Map.Entry<String, NodeTemplate> e : nodeTemplateMap.entrySet()) {
          String uuid = e.getValue().getMetadata().get("resourceUUID");
          if (GeneratorUtil.isEmpty(uuid)) {
            uuid = e.getValue().getMetadata().get("UUID");
            if (GeneratorUtil.isEmpty(uuid)) {
              Assert.fail("UUID Not found");
            }
          }
          if(e.getValue().getType().contains("org.openecomp.resource.vf.")&& (e.getValue()
              .getMetadata().get("category").equals("Allotted Resource")))
          {
            e.getValue().setType("org.openecomp.resource.vf.allottedResource");
          }
          nodeTemplateIdTypeStore.put(uuid, e.getValue().getType());
          resourcesVersion.put(uuid,e.getValue().getMetadata().get
              ("version"));
          //Populate invraintUuId for V9
          String invUuId = e.getValue().getMetadata().get("invariantUUID");
          nodeTemplateIdTypeStore.put(uuid+"-INV_UID" , invUuId);
        }
      }
      return nodeTemplateIdTypeStore;
    } else {
      return null;
    }
  }

  private Map<String, String> getGroupsTypeStore(ToscaTemplate toscaTemplate) {
    if (toscaTemplate.getTopology_template() != null) {
      Map<String, GroupDefinition> groupDefinitionMap =
          toscaTemplate.getTopology_template().getGroups();
      Map<String, String> groupDefinitionIdTypeStore = new LinkedHashMap<>();
      if (groupDefinitionMap != null) {
        for (Map.Entry<String, GroupDefinition> e : groupDefinitionMap.entrySet()) {
          if (e.getValue().getType().contains("org.openecomp.groups.VfModule")) {
            String uuid = e.getValue().getMetadata().get("vfModuleModelUUID");
            if (GeneratorUtil.isEmpty(uuid)) {
              uuid = e.getValue().getMetadata().get("UUID");
              if (GeneratorUtil.isEmpty(uuid)) {
                Assert.fail("UUID Not found");
              }
            }
            groupDefinitionIdTypeStore.put(uuid, e.getValue().getType());
          }
        }
      }
      return groupDefinitionIdTypeStore;
    } else {
      return null;
    }

  }

  private void validateServiceModelMetadata(Service serviceToscaModel, Model generatedAAIModel) {
    ModelVer modelVersion =  generatedAAIModel.getModelVers().getModelVer().get(0);
    Assert.assertEquals(serviceToscaModel.getModelNameVersionId(),
        modelVersion.getModelVersionId());
    Assert.assertEquals(serviceToscaModel.getModelId(), generatedAAIModel.getModelInvariantId());
    Assert.assertEquals(serviceToscaModel.getModelName(), modelVersion.getModelName());
    Assert.assertEquals(additionalParams.get(AdditionalParams.ServiceVersion.getName()), modelVersion
        .getModelVersion());
    Assert.assertEquals(serviceToscaModel.getModelDescription(),
        modelVersion.getModelDescription());

  }

  private void validateResourceModelMetadata(Resource resouerceToscaModel,
                                             Model generatedAAIModel) {
    ModelVer modelVersion =  generatedAAIModel.getModelVers().getModelVer().get(0);
    Assert.assertEquals(resouerceToscaModel.getModelNameVersionId(),
        modelVersion.getModelVersionId());
    Assert.assertEquals(resouerceToscaModel.getModelId(), generatedAAIModel.getModelInvariantId());
    Assert.assertEquals(resouerceToscaModel.getModelName(), modelVersion.getModelName());
    Assert
        .assertEquals(resouerceToscaModel.getModelVersion(), modelVersion.getModelVersion());
    Assert.assertEquals(resouerceToscaModel.getModelDescription(),
        modelVersion.getModelDescription());

  }

  private void validateVFModelMetadata(Map<String, String> vfModuleModelMetadata,
                                       Model generatedAAIModel) {
    ModelVer modelVersion =  generatedAAIModel.getModelVers().getModelVer().get(0);
    Assert.assertEquals(vfModuleModelMetadata.get("vfModuleModelUUID"),
        modelVersion.getModelVersionId());
    Assert.assertEquals(vfModuleModelMetadata.get("vfModuleModelInvariantUUID"),
        generatedAAIModel.getModelInvariantId());
    Assert.assertEquals(vfModuleModelMetadata.get("vfModuleModelName"),
        modelVersion.getModelName());
    Assert.assertEquals(vfModuleModelMetadata.get("vfModuleModelVersion"),
        modelVersion.getModelVersion());
    Assert.assertEquals(vfModuleModelMetadata.get("vf_module_description"),
        modelVersion.getModelDescription());
  }

  private Model getAAIModelByNameVersionId(String nameVersionId,
                                           Map<String, Model> outputArtifactMap) {
    return outputArtifactMap.get(nameVersionId);
  }

  private List<ModelElement> getModelElementbyRelationshipValue(ModelElements modelElements,
                                                                String relationshipValue) {
    List<ModelElement> matchedModelElements = new ArrayList<>();
    if (modelElements != null) {
      List<ModelElement> modelElementList = modelElements.getModelElement();
      for (ModelElement element : modelElementList) {
        List<Relationship> relationshipList = element.getRelationshipList().getRelationship();
        for (Relationship r : relationshipList) {
          List<RelationshipData> relationshipDataList = r.getRelationshipData();
          for (RelationshipData relationshipData : relationshipDataList) {
            if (relationshipData.getRelationshipValue().equals(relationshipValue)) {
              matchedModelElements.add(element);
            }
          }
        }
      }
    }
    return matchedModelElements;
  }

  private Map<String, Model> populateAAIGeneratedModelStore(List<Artifact> resultData) throws IOException {
    Map<String, Model> outputArtifactMap = new HashMap<>();
    for (Artifact outputArtifact : resultData) {
      if (outputArtifact.getType().equals(ArtifactType.MODEL_INVENTORY_PROFILE.name())) {
        byte[] decodedPayload = GeneratorUtil.decoder(outputArtifact.getPayload());
        Model aaiModel = getUnmarshalledArtifactModel(new String(decodedPayload));
        List<ModelVer> modelVersions =  aaiModel.getModelVers().getModelVer();
        outputArtifactMap.put(modelVersions.get(0).getModelVersionId(), aaiModel);
      }
    }
    return outputArtifactMap;
  }

  private Model getUnmarshalledArtifactModel(String aaiModel) throws IOException {

    try {

      JAXBContext jaxbContext = JAXBContext.newInstance(Model.class);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

      try (InputStream aaiModelStream = new ByteArrayInputStream(aaiModel.getBytes())) {
        return (Model) unmarshaller.unmarshal(aaiModelStream);
      }

    } catch (JAXBException e) {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Get the tosca java model from the tosca input artifact
   *
   * @param input Input tosca file and its metadata information as {@link Artifact} object
   * @return Translated {@link ToscaTemplate tosca} object
   */
  private ToscaTemplate getToscaModel(Artifact input) throws SecurityException {
    byte[] decodedInput = GeneratorUtil.decoder(input.getPayload());
    String checksum = GeneratorUtil.checkSum(decodedInput);
    if (checksum.equals(input.getChecksum())) {
      try {
        return GeneratorUtil.translateTosca(new String(decodedInput), ToscaTemplate.class);
      } catch (Exception e) {
        e.printStackTrace();
        throw new IllegalArgumentException(
            String.format(GeneratorConstants.GENERATOR_AAI_ERROR_INVALID_TOSCA, input.getName()));
      }
    } else {
      throw new SecurityException(
          String.format(GeneratorConstants.GENERATOR_AAI_ERROR_CHECKSUM_MISMATCH, input.getName()));
    }
  }

  /**
   * Identify the service tosca artifact from the list of translated tosca inputs
   *
   * @param input List of translated {@link ToscaTemplate tosca} object models
   * @return Identified service {@link ToscaTemplate tosca}
   */
  private ToscaTemplate getServiceTosca(List<ToscaTemplate> input) {
    Iterator<ToscaTemplate> iter = input.iterator();
    while (iter.hasNext()) {
      ToscaTemplate tosca = iter.next();
      if (tosca.isService()) {
        iter.remove();
        return tosca;
      }
    }
    return null;
  }

  private Map<String, String> getVFModuleMetadataTosca(ToscaTemplate toscaTemplate,
                                                       String vfModuleModelUUID) {
    Map<String, GroupDefinition> groupDefinitionMap =
        toscaTemplate.getTopology_template().getGroups();
    Map<String, String> vfModuleModelMetadata = new LinkedHashMap<>();
    for (Map.Entry<String, GroupDefinition> e : groupDefinitionMap.entrySet()) {
      if (e.getValue().getType().contains("org.openecomp.groups.VfModule")) {
        String uuid = e.getValue().getMetadata().get("vfModuleModelUUID");
        if (Objects.equals(uuid, vfModuleModelUUID)) {
          vfModuleModelMetadata = e.getValue().getMetadata();
          vfModuleModelMetadata.put("vf_module_description",
              (String) e.getValue().getProperties().get("vf_module_description"));
        }
      }
    }
    return vfModuleModelMetadata;
  }

  private Map<String, Object> getVFModuleMembersTosca(ToscaTemplate toscaTemplate,
                                                      String vfModuleModelUUID) {
    Map<String, GroupDefinition> groupDefinitionMap =
        toscaTemplate.getTopology_template().getGroups();
    Map<String, NodeTemplate> nodeTemplateMaps =
        toscaTemplate.getTopology_template().getNode_templates();
    Map<String, Object> vfModuleMembers = new LinkedHashMap<>();
    List<String> vfModuleModelMetadata;
    for (Map.Entry<String, GroupDefinition> e : groupDefinitionMap.entrySet()) {
      if (e.getValue().getType().contains("org.openecomp.groups.VfModule")) {
        String uuid = e.getValue().getMetadata().get("vfModuleModelUUID");
        if (Objects.equals(uuid, vfModuleModelUUID)) {
          vfModuleModelMetadata = e.getValue().getMembers();
          if (vfModuleModelMetadata !=null) {
            for (Object key : vfModuleModelMetadata) {
              NodeTemplate nodeTemplate = nodeTemplateMaps.get(key);
              String nodetype = null;
              if (nodeTemplate != null) {
                nodetype = nodeTemplate.getType();
              }
              if (nodetype != null) {
                String widgetType = membersType(nodetype);
                if (widgetType != null) {
                  vfModuleMembers.put(widgetType, key);
                }
              }
            }
          }
        }
      }
    }

    return vfModuleMembers;
  }


  private String membersType(String toscaType) {
    String modelToBeReturned = null;
    while (toscaType != null && toscaType.lastIndexOf(".") != -1 && modelToBeReturned == null) {

      switch (toscaType) {
        case "org.openecomp.resource.vf.allottedResource":
          modelToBeReturned = "allotted-resource";
          break;
        case "org.openecomp.resource.vfc":
          modelToBeReturned = "vserver";
          break;
        case "org.openecomp.resource.cp":
        case "org.openecomp.cp":
          modelToBeReturned = "l-interface";
          break;
        case "org.openecomp.resource.vl":
          modelToBeReturned = "l3-network";
          break;
        case "org.openecomp.resource.vf":
          modelToBeReturned = "generic-vnf";
          break;
        case "org.openecomp.groups.VfModule":
          modelToBeReturned = "vf-module";
          break;
        case "org.openecomp.resource.vfc.nodes.heat.cinder":
          modelToBeReturned = "volume";
          break;
        default:
          modelToBeReturned = null;
          break;
      }

      toscaType = toscaType.substring(0, toscaType.lastIndexOf("."));
    }
    return modelToBeReturned;
  }

  private List<Artifact> init(String aaiResourceBasePaths) {
    List<Artifact> inputArtifacts1 = new ArrayList<>();
    try {

      String[] resourceFileList = {};
      URL resourceDirUrl = this.getClass().getClassLoader().getResource(aaiResourceBasePaths);
      if (resourceDirUrl != null && resourceDirUrl.getProtocol().equals("file")) {
        resourceFileList = new File(resourceDirUrl.toURI()).list();
      } else {
        Assert.fail("Invalid resource directory");
      }

      for (String aResourceFileList : resourceFileList) {
        File resourceFile = new File(
                this.getClass().getClassLoader().getResource(aaiResourceBasePaths + aResourceFileList)
                        .getPath());

        //convert service tosca file into array of bytes
        byte[] payload = new byte[(int) resourceFile.length()];
        try (FileInputStream fileInputStream = new FileInputStream(resourceFile)) {
          fileInputStream.read(payload);
        }

        String checksum = GeneratorUtil.checkSum(payload);
        byte[] encodedPayload = GeneratorUtil.encode(payload);
        Artifact artifact =
                new Artifact(aaiArtifactType, aaiArtifactGroupType, checksum, encodedPayload);
        artifact.setName(aResourceFileList);
        artifact.setLabel(aResourceFileList);
        artifact.setDescription(aResourceFileList);
        artifact.setVersion("1.0");
        inputArtifacts1.add(artifact);

      }
    } catch (Exception e) {
      //e.printStackTrace();
      Assert.fail(e.getMessage());
    }
    return inputArtifacts1;
  }

  private void loadConfigFromClasspath(Properties properties) throws IOException {
    String configLocation = System.getProperty("artifactgenerator.config");
    if (configLocation != null) {
      File file = new File(configLocation);
      if (file.exists()) {
        properties.load(new FileInputStream(file));
      }
    }
  }

  public  void testResourceTosca(Iterator<ToscaTemplate> itr, Map<String, Model>
      outputArtifactMap) {
    while(itr.hasNext()){
      ToscaTemplate toscaTemplate = itr.next();
      String resourceVersion=resourcesVersion.get(toscaTemplate.getMetadata().get("UUID"));
      toscaTemplate.getMetadata().put("version", resourceVersion);
      if("VF".equals(toscaTemplate.getMetadata().get("type")) && !("Allotted Resource".equals
          (toscaTemplate.getMetadata().get("category"))) ){
        testVfTosca(outputArtifactMap, toscaTemplate);
      } else if("VF".equals(toscaTemplate.getMetadata().get("type")) && ("Allotted Resource".equals
          (toscaTemplate.getMetadata().get("category"))) ){
        testAllottedResourceTosca(outputArtifactMap, toscaTemplate);
      } else if("VL".equals(toscaTemplate.getMetadata().get("type"))){
        testL3NetworkResourceTosca(outputArtifactMap, toscaTemplate);
      }
    }
  }

  private void readPayloadFromResource(List<Artifact> inputArtifacts, String file) throws IOException {
    try (InputStream stream = ArtifactGenerationServiceTest.class.getResourceAsStream("/" + file)) {
      readPayload(inputArtifacts, stream, file);
    }
  }
}
