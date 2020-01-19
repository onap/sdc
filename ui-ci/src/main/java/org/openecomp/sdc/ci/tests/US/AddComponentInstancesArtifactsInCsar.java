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

package org.openecomp.sdc.ci.tests.US;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.HttpStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.HeatMetaFirstLevelDefinition;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.VendorLicenseModel;
import org.openecomp.sdc.ci.tests.datatypes.VendorSoftwareProductObject;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.devCI.ArtifactFromCsar;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.HomePage;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.ToscaArtifactsPage;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.OnboardingUiUtils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.VendorLicenseModelRestUtils;
import org.openecomp.sdc.ci.tests.utils.general.VendorSoftwareProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AddComponentInstancesArtifactsInCsar extends SetupCDTest {

    public static final String DEPLOYMENT = "Deployment";
    public static final String INFORMATIONAL = "Informational";
    private String filePath;

    @BeforeClass
    public void beforeClass() {
        filePath = System.getProperty("filePath");
        if (filePath == null && System.getProperty("os.name").contains("Windows")) {
            filePath = FileHandling.getResourcesFilesPath() + "AddComponentInstancesArtifactsInCsar" + File.separator;
        } else if (filePath.isEmpty() && !System.getProperty("os.name").contains("Windows")) {
            filePath = FileHandling.getBasePath() + File.separator + "Files" + File.separator + "AddComponentInstancesArtifactsInCsar" + File.separator;
        }
    }

    // US847439 - Story [BE] - Add Component Instance's artifacts in CSAR
    // TC1521795 - VF CSAR - The Flow
    @Test
    public void vfAndServiceCsarTheFlow() throws Exception {
        ResourceReqDetails resourceMetaData = ElementFactory.getDefaultResourceByType(ResourceTypeEnum.VF, getUser());

        String vnfFile = "FDNT.zip";
        String snmpFile = "Fault-alarms-ASDC-vprobes-vLB.zip";

        VendorLicenseModel vendorLicenseModel = VendorLicenseModelRestUtils.createVendorLicense(getUser());
        ResourceReqDetails resourceReqDetails = ElementFactory.getDefaultResource();//getResourceReqDetails(ComponentConfigurationTypeEnum.DEFAULT);
        VendorSoftwareProductObject createVSP = VendorSoftwareProductRestUtils.createVSP(resourceReqDetails, vnfFile, filePath, getUser(),
            vendorLicenseModel);
        String vspName = createVSP.getName();
        resourceMetaData.setName(vspName);
        VendorSoftwareProductRestUtils.addVFCArtifacts(filePath, snmpFile, null, createVSP, getUser());
        VendorSoftwareProductRestUtils.prepareVspForUse(getUser(), createVSP, true);

        HomePage.showVspRepository();
        OnboardingUiUtils.importVSP(createVSP);
        resourceMetaData.setVersion("0.1");
        Resource vfResource = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, resourceMetaData.getName(), resourceMetaData.getVersion());

        Map<String, Object> artifacts = getArtifactsOfComponentAndComponentsInstance(vfResource);

        List<ImmutablePair<ComponentInstance, ArtifactDefinition>> artifactsUploadedToComponentInstance = new LinkedList<>();
        Random random = new Random();
        final int randomIntForLoop = random.nextInt(10) + 10;
        for (int i = 0; i < randomIntForLoop; i++) {
            ImmutablePair<ComponentInstance, ArtifactDefinition> uploadArtifactOnRandomVfc = uploadArtifactOnRandomRI(vfResource);

            if (uploadArtifactOnRandomVfc.getRight().getArtifactName() != null) {
                artifactsUploadedToComponentInstance.add(uploadArtifactOnRandomVfc);
            }
        }

        if (!artifactsUploadedToComponentInstance.isEmpty()) {
            Map<String, Object> artifactsOfResourceInstance = getArtifactsOfResourceInstance(artifactsUploadedToComponentInstance);
            artifacts.put("Resources", artifactsOfResourceInstance);
        }

        ResourceGeneralPage.getLeftMenu().moveToToscaArtifactsScreen();
        ToscaArtifactsPage.downloadCsar();
        File latestFilefromDir = FileHandling.getLastModifiedFileNameFromDir();
        Map<String, Object> combineHeatArtifacstWithFolderArtifacsToMap = ArtifactFromCsar.getVFCArtifacts(latestFilefromDir.getAbsolutePath());

        compareArtifactFromFileStructureToArtifactsFromJavaObject(artifacts, combineHeatArtifacstWithFolderArtifacsToMap);

    }

    public void compareArtifactFromFileStructureToArtifactsFromJavaObject(Map<String, Object> artifactFromJavaObject, Map<String, Object> artifactsFromFileStructure) {
        for (String key : artifactFromJavaObject.keySet()) {
            if ((!key.equals(DEPLOYMENT)) && (!key.equals(INFORMATIONAL))) {
                Map<String, Object> newArtifactFromJavaObject = (Map<String, Object>) artifactFromJavaObject.get(key);
                Map<String, Object> newArtifactsFromFileStructure = (Map<String, Object>) artifactsFromFileStructure.get(key);
                compareArtifactFromFileStructureToArtifactsFromJavaObject(newArtifactFromJavaObject, newArtifactsFromFileStructure);
            } else {
                compareArtifacts(artifactFromJavaObject.get(key), artifactsFromFileStructure.get(key));
            }
        }
    }


    private void compareArtifacts(Object artifactFromJavaObject, Object artifactsFromFileStructure) {
        Map<String, List<String>> artifactsMap = (Map<String, List<String>>) artifactFromJavaObject;
        List<HeatMetaFirstLevelDefinition> artifactsList = (List<HeatMetaFirstLevelDefinition>) artifactsFromFileStructure;

        for (HeatMetaFirstLevelDefinition heatMetaFirstLevelDefinition : artifactsList) {
            Assert.assertTrue(artifactsMap.get(heatMetaFirstLevelDefinition.getType()).contains(heatMetaFirstLevelDefinition.getFileName()),
                    "Expected that artifacts will be the same. Not exists: " + heatMetaFirstLevelDefinition.getFileName() + " of type: " + heatMetaFirstLevelDefinition.getType());
        }

        for (String key : artifactsMap.keySet()) {
            List<String> artifacts = artifactsMap.get(key);

            for (HeatMetaFirstLevelDefinition heatMetaFirstLevelDefinition : artifactsList) {
                if (heatMetaFirstLevelDefinition.getType().equals(key)) {
                    if (artifacts.contains(heatMetaFirstLevelDefinition.getFileName())) {
                        artifacts.remove(heatMetaFirstLevelDefinition.getFileName());
                    }
                }
            }

            Assert.assertEquals(artifacts.size(), 0, "Expected that all artifacts equal. There is artifacts which not equal: " + artifacts.toString());
        }
    }


    public Map<String, Object> getArtifactsOfResourceInstance(List<ImmutablePair<ComponentInstance, ArtifactDefinition>> riList) {
        Map<String, Object> artifacts = new HashMap<>();

        for (ImmutablePair<ComponentInstance, ArtifactDefinition> ri : riList) {
            ArtifactDefinition artifactDefinition = ri.getRight();
            ComponentInstance componentInstance = ri.getLeft();
            if (artifacts.containsKey(componentInstance.getNormalizedName())) {
                if (((Map<String, ArrayList<String>>) ((Map<String, Object>) artifacts.get(componentInstance.getNormalizedName())).get(DEPLOYMENT)).containsKey(artifactDefinition.getArtifactType())) {

                    ((Map<String, ArrayList<String>>) ((Map<String, Object>) artifacts.get(componentInstance.getNormalizedName())).get(DEPLOYMENT)).get(artifactDefinition.getArtifactType()).add(artifactDefinition.getArtifactName());

                } else {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(artifactDefinition.getArtifactName());
                    ((Map<String, ArrayList<String>>) ((Map<String, Object>) artifacts.get(componentInstance.getNormalizedName())).get(DEPLOYMENT)).put(artifactDefinition.getArtifactType(), list);
                }

            } else {
                try {


                    ArrayList<String> list = new ArrayList<>();
                    list.add(artifactDefinition.getArtifactName());

                    Map<String, ArrayList<String>> map = new HashMap<>();
                    map.put(artifactDefinition.getArtifactType(), list);

                    Map<String, Map<String, ArrayList<String>>> addMap = new HashMap<>();
                    addMap.put(DEPLOYMENT, map);

                    artifacts.put(componentInstance.getNormalizedName(), addMap);
                } catch (Exception e) {
                    Assert.fail("Artifact name is null for componentInstance: " + componentInstance.getNormalizedName());
                }
            }
        }
        return artifacts;
    }

    public Map<String, Object> getArtifactsOfComponentAndComponentsInstance(Component component) {
        Map<String, Object> artifacts = getArtifactsOfComponent(component);

        for (ComponentInstance componentInstance : component.getComponentInstances()) {
            Map<String, Object> artifactsOfComponentInstance = getArtifactsOfComponentInstance(componentInstance);
            if (!artifactsOfComponentInstance.isEmpty()) {
                artifacts.put(componentInstance.getToscaComponentName() + "." + componentInstance.getComponentVersion(), artifactsOfComponentInstance);
            }
        }

        return artifacts;
    }

    public Map<String, Object> getArtifactsOfComponentInstance(ComponentInstance componentInstance) {
        Map<String, Object> map = new HashMap<>();

        if (componentInstance.getArtifacts() != null) {
            Map<String, Object> informationalArtifacts = getArtifacts(componentInstance.getArtifacts());
            if (!informationalArtifacts.isEmpty()) {
                map.put(INFORMATIONAL, informationalArtifacts);
            }
        }

        if (componentInstance.getDeploymentArtifacts() != null) {
            Map<String, Object> deploymentArtifacts = getArtifacts(componentInstance.getDeploymentArtifacts());
            if (!deploymentArtifacts.isEmpty()) {
                map.put(DEPLOYMENT, deploymentArtifacts);
            }
        }

        return map;
    }

    public Map<String, Object> getArtifactsOfComponent(Component component) {
        Map<String, Object> map = new HashMap<>();

        if (component.getArtifacts() != null) {
            Map<String, Object> informationalArtifacts = getArtifacts(component.getArtifacts());
            if (!informationalArtifacts.isEmpty()) {
                map.put(INFORMATIONAL, informationalArtifacts);
            }
        }

        if (component.getDeploymentArtifacts() != null) {
            Map<String, Object> deploymentArtifacts = getArtifacts(component.getDeploymentArtifacts());
            if (!deploymentArtifacts.isEmpty()) {
                map.put(DEPLOYMENT, deploymentArtifacts);
            }
        }

        return map;
    }

    public Map<String, Object> getArtifacts(Map<String, ArtifactDefinition> artifacts) {
        Map<String, Object> map = new HashMap<>();

        for (String artifact : artifacts.keySet()) {
            ArtifactDefinition artifactDefinition = artifacts.get(artifact);
            if ((artifactDefinition.getEsId() != null) && (!artifactDefinition.getEsId().equals("")) && (!artifactDefinition.getArtifactType().equals("HEAT_ENV"))) {
                if (map.containsKey(artifactDefinition.getArtifactType())) {
                    ((List<String>) map.get(artifactDefinition.getArtifactType())).add(artifactDefinition.getArtifactName());
                } else {
                    ArrayList<String> list = new ArrayList<>();
                    list.add(artifactDefinition.getArtifactName());
                    map.put(artifactDefinition.getArtifactType(), list);
                }
            }
        }

        return map;
    }

    public ImmutablePair<ComponentInstance, ArtifactDefinition> uploadArtifactOnRandomRI(Component component) throws Exception {
        ArtifactReqDetails artifactReqDetails = getRandomArtifact();
        Random random = new Random();
        int randInt = random.nextInt(component.getComponentInstances().size());
        User defaultUser = ElementFactory.getDefaultUser(getRole());
        ComponentInstance componentInstance = component.getComponentInstances().get(randInt);

        RestResponse uploadArtifactRestResponse = ArtifactRestUtils.externalAPIUploadArtifactOfComponentInstanceOnAsset(component, defaultUser, artifactReqDetails, componentInstance);

        // Check response of external API
        Integer responseCode = uploadArtifactRestResponse.getErrorCode();
        Assert.assertEquals(responseCode, (Integer) HttpStatus.SC_OK, "Response code is not correct.");

        ImmutablePair<ComponentInstance, ArtifactDefinition> pair = ImmutablePair.of(componentInstance, ResponseParser.convertArtifactDefinitionResponseToJavaObject(uploadArtifactRestResponse.getResponse()));

        return pair;
    }

    public ImmutablePair<ComponentInstance, ArtifactDefinition> uploadArtifactOnRandomRI(Resource resource) throws Exception {
        ArtifactReqDetails artifactReqDetails = getRandomVfcArtifact();
        Random random = new Random();
        int randInt = random.nextInt(resource.getComponentInstances().size());
        User defaultUser = ElementFactory.getDefaultUser(getRole());
        ComponentInstance componentInstance = resource.getComponentInstances().get(randInt);

        RestResponse uploadArtifactRestResponse = ArtifactRestUtils.externalAPIUploadArtifactOfComponentInstanceOnAsset(resource, defaultUser, artifactReqDetails, componentInstance);
        // Check response of external API
        Integer responseCode = uploadArtifactRestResponse.getErrorCode();
        Assert.assertEquals(responseCode, (Integer) HttpStatus.SC_OK, "Response code is not correct.");
        ImmutablePair<ComponentInstance, ArtifactDefinition> pair = ImmutablePair.of(componentInstance, ResponseParser.convertArtifactDefinitionResponseToJavaObject(uploadArtifactRestResponse.getResponse()));
        return pair;
    }

    public ArtifactReqDetails getRandomArtifact() throws Exception {
        List<String> artifactsTypeList = Arrays.asList("Other");
        return getRandomArtifact(artifactsTypeList);
    }

    public ArtifactReqDetails getRandomVfcArtifact() throws Exception {
        List<String> vfcArtifactsTypeList = Arrays.asList(
                ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(),
                ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(),
                ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(),
                ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(),
                ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(),
                ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(),
                ArtifactTypeEnum.SNMP_POLL.getType(),
                ArtifactTypeEnum.SNMP_TRAP.getType());
        return getRandomArtifact(vfcArtifactsTypeList);
    }

    public ArtifactReqDetails getRandomArtifact(List<String> artifactType) throws Exception {
        Random random = new Random();
        return ElementFactory.getArtifactByType("ci", artifactType.get(random.nextInt(artifactType.size())), true, false);
    }

    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }

}
