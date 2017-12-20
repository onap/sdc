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

import org.openecomp.sdc.generator.aai.model.*;
import org.openecomp.sdc.generator.aai.model.Service;
import org.openecomp.sdc.generator.aai.tosca.GroupDefinition;
import org.openecomp.sdc.generator.aai.tosca.NodeTemplate;
import org.openecomp.sdc.generator.aai.tosca.ToscaTemplate;
import org.openecomp.sdc.generator.aai.xml.*;
import org.openecomp.sdc.generator.aai.xml.Model;
import org.openecomp.sdc.generator.data.*;
import org.junit.Assert;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.openecomp.sdc.generator.SampleJUnitTest.additionalParams;

@SuppressWarnings("Duplicates")
public class ArtifactGenerationServiceTest {

    public static final String RESOURCE_MAPPING_NOT_FOUND = "Resource mapping not found for ";
    public static final String INV_UID = "-INV_UID";
    public static final String VF_MODULE_NAMESPACE = "org.openecomp.groups.VfModule";
    public static final String VF_MODULE_DESCRIPTION = "vf_module_description";
    public static final String CATEGORY = "category";
    static Map<String, String> resourcesVersion = new HashMap<>();
    public static Properties properties = new Properties();

    @SuppressWarnings("Since15")
    public static void validateName(List<Artifact> artifactList) throws JAXBException {
        for(Artifact artifact : artifactList){
            String xml = new String(Base64.getDecoder().decode(artifact.getPayload()));
            Model model = getUnmarshalledArtifactModel(xml);
            String xmlName = "AAI-"+ model.getModelVers().getModelVer().get(0).getModelName()
                +"-"+model
                .getModelType
                ()+"-"+model.getModelVers().getModelVer().get(0)
                .getModelVersion()+".xml";

            Assert.assertEquals(true,artifact.getName().equals(xmlName));

        }
    }

    public static void checkArtifactName(String name){
        Assert.assertEquals(true,name.length()<=255);
    }

    public static void checkArtifactLabel(String label){
        Pattern pattern = Pattern.compile("[a-zA-Z0-9-+\\s]+");
        Matcher matcher = pattern.matcher(label);
        Assert.assertEquals(true,matcher.matches());
    }

    public static void checkArtifactDescription(String description){
        Pattern pattern = Pattern.compile("[a-zA-Z\\s\\t\\n]+");
        Matcher matcher = pattern.matcher(description);
        Assert.assertEquals(true,matcher.matches());
        Assert.assertEquals(true,description.length()<=256);

    }

    public static void testResourceTosca(Iterator<ToscaTemplate> itr, Map<String, Model>
        outputArtifactMap) {
        while(itr.hasNext()){
            ToscaTemplate toscaTemplate = itr.next();
            String resourceVersion= resourcesVersion.get(toscaTemplate.getMetadata().get("UUID"));
            toscaTemplate.getMetadata().put("version", resourceVersion);
            if("VF".equals(toscaTemplate.getMetadata().get("type")) && !("Allotted Resource".equals
                (toscaTemplate.getMetadata().get(CATEGORY))) ){
                testVfTosca(outputArtifactMap, toscaTemplate);
            } else if("VF".equals(toscaTemplate.getMetadata().get("type")) && ("Allotted Resource".equals
                (toscaTemplate.getMetadata().get(CATEGORY))) ){
                testAllottedResourceTosca(outputArtifactMap, toscaTemplate);
            } else if("VL".equals(toscaTemplate.getMetadata().get("type"))){
                testL3NetworkResourceTosca(outputArtifactMap, toscaTemplate);
            }
        }
    }

    public static void testVfTosca(Map<String, Model> outputArtifactMap , ToscaTemplate resourceTosca) {

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
                        org.testng.Assert.fail("Missing VFModule in VF model.xml");
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
                System.out.println(RESOURCE_MAPPING_NOT_FOUND + resourceNameVersionId);
            }
        }
    }

    public static void testAllottedResourceTosca(Map<String, Model> outputArtifactMap , ToscaTemplate
        resourceTosca) {

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

                validateWidgetIds(matchedVFBaseWidgetElements, Widget.getWidget(Widget.Type.ALLOTTED_RESOURCE).getName(),
                    Widget.getWidget(Widget.Type.ALLOTTED_RESOURCE).getWidgetId());

                Map<String, Object> providingServiceDetails = getProvidingServiceDetails(resourceTosca);

                ModelElements containedModelElements = modelVersion.getModelElements().getModelElement().
                    get(0).getModelElements();

                org.testng.Assert.assertEquals(containedModelElements.getModelElement().get(0).getRelationshipList()
                     .getRelationship().get(0).getRelationshipData().get(0).getRelationshipValue(),
                        providingServiceDetails.get("providing_service_uuid"));

                org.testng.Assert.assertEquals(containedModelElements.getModelElement().get(0).getRelationshipList()
                    .getRelationship().get(0).getRelationshipData().get(1).getRelationshipValue(),
                        providingServiceDetails.get("providing_service_invariant_uuid"));


                if("Allotted Resource".equals(resourceTosca.getMetadata().get(CATEGORY)) &&
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
                System.out.println(RESOURCE_MAPPING_NOT_FOUND + resourceNameVersionId);
            }
        }
    }

    public static Map<String, Object> getProvidingServiceDetails(ToscaTemplate resourceTemplate) {
        Set<String> keys = resourceTemplate.getTopology_template().getNode_templates().keySet();

        Map<String, Object> nodeProperties =null;
        for(String key : keys) {
            NodeTemplate node = resourceTemplate.getTopology_template().getNode_templates().get(key);
            if(node.getType().contains("org.openecomp.resource.vfc") &&
                node.getMetadata().get(CATEGORY).equals("Allotted Resource")) {
                nodeProperties = node.getProperties();
            }
        }

        return nodeProperties;
    }

    public static void testL3NetworkResourceTosca(Map<String, Model> outputArtifactMap , ToscaTemplate
        resourceTosca) {

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
                System.out.println(RESOURCE_MAPPING_NOT_FOUND + resourceNameVersionId);
            }
        }
    }

    public static void testServiceTosca(Map<String, Model> outputArtifactMap,List<ToscaTemplate>
        toscas) {

        ToscaTemplate serviceTosca = getServiceTosca(toscas);
        if (serviceTosca == null) {
            org.testng.Assert.fail("Service Tosca not found");
        }
        serviceTosca.getMetadata().put("version", additionalParams.get(AdditionalParams
            .ServiceVersion.getName()));
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
            for (Map.Entry<String, String> entry : nodeTemplateIdTypeStore.entrySet()) {
                if (entry.getValue().contains("org.openecomp.resource.vf")) {
                    List<ModelElement> matchedResourceElements =
                        getModelElementbyRelationshipValue(baseServiceWidgetModelElements, entry.getKey());
                    if (entry.getValue().contains("org.openecomp.resource.vf.allottedResource")){
                        validateMatchedModelElementsInService(matchedResourceElements,
                            Widget.getWidget(Widget.Type.ALLOTTED_RESOURCE).getName());
                    }else {
                        validateMatchedModelElementsInService(matchedResourceElements,
                            Widget.getWidget(Widget.Type.VF).getName());
                    }

                    //Validate uuid and invariantuuid are populated in model-ver.model-version-id and model.model-invariant-id
                    org.testng.Assert.assertEquals(matchedResourceElements.get(0).getRelationshipList()
                        .getRelationship().get(0)
                        .getRelationshipData().get(0).getRelationshipValue(),entry.getKey());

                    org.testng.Assert.assertEquals(matchedResourceElements.get(0).getRelationshipList().getRelationship().get(0)
                        .getRelationshipData().get(1).getRelationshipValue(), nodeTemplateIdTypeStore
                        .get(entry.getKey()+ INV_UID));
                } else if(entry.getValue().contains("org.openecomp.resource.vl")){
                    //validate l3-network in service tosca
                    List<ModelElement> matchedResourceElements =
                        getModelElementbyRelationshipValue(baseServiceWidgetModelElements, entry.getKey());
                    validateMatchedModelElementsInService(matchedResourceElements,
                        Widget.getWidget(Widget.Type.L3_NET).getName());
                    //Validate uuid and invariantuuid are populated in model-ver.model-version-id and model.model-invariant-id
                    org.testng.Assert.assertEquals(matchedResourceElements.get(0).getRelationshipList()
                        .getRelationship().get(0)
                        .getRelationshipData().get(0).getRelationshipValue(),entry.getKey());

                    org.testng.Assert.assertEquals(matchedResourceElements.get(0).getRelationshipList().getRelationship().get(0)
                        .getRelationshipData().get(1).getRelationshipValue(), nodeTemplateIdTypeStore
                        .get(entry.getKey() + INV_UID));
                }
            }
        }
    }

    private static void validateWidgetIds(List<ModelElement> matchedServiceBaseWidgetElements,
                                          String widgetName, String widgetInvUuId) {
        org.testng.Assert.assertEquals(matchedServiceBaseWidgetElements.get(0).getRelationshipList().getRelationship().get(0)
            .getRelationshipData().get(0).getRelationshipValue(), properties.getProperty(ArtifactType.AAI.name()
            + ".model-version-id."+ widgetName));

        org.testng.Assert.assertEquals(matchedServiceBaseWidgetElements.get(0).getRelationshipList().getRelationship().get(0)
            .getRelationshipData().get(1).getRelationshipValue(), widgetInvUuId);
    }



    private static void validateMatchedModelElementsInService(List<ModelElement> matchedModelElements, String modelType) {
        if (matchedModelElements.isEmpty()) {
            Assert.fail(modelType + " not present ");
        }
        if (matchedModelElements.size() > 1) {
            Assert.fail("More than one " + modelType + " present ");
        }
    }

    private static Map<String, String> getNodeTemplateTypeStore(ToscaTemplate toscaTemplate) {
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
                        .getMetadata().get(CATEGORY).equals("Allotted Resource")))
                    {
                        e.getValue().setType("org.openecomp.resource.vf.allottedResource");
                    }
                    nodeTemplateIdTypeStore.put(uuid, e.getValue().getType());
                    resourcesVersion.put(uuid,e.getValue().getMetadata().get
                        ("version"));
                    //Populate invraintUuId for V9
                    String invUuId = e.getValue().getMetadata().get("invariantUUID");
                    nodeTemplateIdTypeStore.put(uuid + INV_UID , invUuId);
                }
            }
            return nodeTemplateIdTypeStore;
        } else {
            return null;
        }
    }


    private static Map<String, String> getGroupsTypeStore(ToscaTemplate toscaTemplate) {
        if(toscaTemplate.getTopology_template() !=null) {
            Map<String, GroupDefinition> groupDefinitionMap = toscaTemplate.getTopology_template().getGroups();
            Map<String, String> groupDefinitionIdTypeStore = new LinkedHashMap<>();
            if (groupDefinitionMap != null) {
                for (Map.Entry<String, GroupDefinition> e : groupDefinitionMap.entrySet()) {
                    if (e.getValue().getType().contains(VF_MODULE_NAMESPACE)) {
                        String uuid = e.getValue().getMetadata().get("vfModuleModelUUID");
                        if (GeneratorUtil.isEmpty(uuid)) {
                            uuid = e.getValue().getMetadata().get("UUID");
                            if (GeneratorUtil.isEmpty(uuid))
                                Assert.fail("UUID Not found");
                        }
                        groupDefinitionIdTypeStore.put(uuid, e.getValue().getType());
                    }
                }
            }
            return groupDefinitionIdTypeStore;
        }
        else {
            return null;
        }
    }

    private static void validateServiceModelMetadata(Service serviceToscaModel, Model generatedAAIModel) {
        Assert.assertEquals(serviceToscaModel.getModelNameVersionId(), generatedAAIModel
            .getModelVers().getModelVer().get(0).getModelVersionId());
        Assert.assertEquals(serviceToscaModel.getModelId(), generatedAAIModel.getModelInvariantId());
        Assert.assertEquals(serviceToscaModel.getModelName(), generatedAAIModel.getModelVers()
            .getModelVer().get(0).getModelName());
        Assert.assertEquals(serviceToscaModel.getModelVersion(), generatedAAIModel.getModelVers()
            .getModelVer().get(0).getModelVersion());
        Assert.assertEquals(serviceToscaModel.getModelDescription(), generatedAAIModel.getModelVers()
            .getModelVer().get(0).getModelDescription());
    }

    private static void validateResourceModelMetadata(Resource resouerceToscaModel, Model generatedAAIModel) {
        Assert.assertEquals(resouerceToscaModel.getModelNameVersionId(), generatedAAIModel
            .getModelVers().getModelVer().get(0).getModelVersionId());
        Assert.assertEquals(resouerceToscaModel.getModelId(), generatedAAIModel.getModelInvariantId());
        Assert.assertEquals(resouerceToscaModel.getModelName(), generatedAAIModel.getModelVers()
            .getModelVer().get(0).getModelName());
        Assert.assertEquals(resouerceToscaModel.getModelVersion(), generatedAAIModel.getModelVers()
            .getModelVer().get(0).getModelVersion());
        Assert.assertEquals(resouerceToscaModel.getModelDescription(), generatedAAIModel.getModelVers()
            .getModelVer().get(0).getModelDescription());
    }

    private static void validateVFModelMetadata(Map<String, String> vfModuleModelMetadata, Model generatedAAIModel) {
        Assert.assertEquals(vfModuleModelMetadata.get("vfModuleModelUUID"),  generatedAAIModel
            .getModelVers().getModelVer().get(0).getModelVersionId());
        Assert.assertEquals(vfModuleModelMetadata.get("vfModuleModelInvariantUUID"), generatedAAIModel.getModelInvariantId());
        Assert.assertEquals(vfModuleModelMetadata.get("vfModuleModelName"), generatedAAIModel.getModelVers()
            .getModelVer().get(0).getModelName());
        Assert.assertEquals(vfModuleModelMetadata.get("vfModuleModelVersion"), generatedAAIModel.getModelVers()
            .getModelVer().get(0).getModelVersion());
        Assert.assertEquals(vfModuleModelMetadata.get(VF_MODULE_DESCRIPTION), generatedAAIModel.getModelVers()
            .getModelVer().get(0).getModelDescription());

    }

    private static Model getAAIModelByNameVersionId(String nameVersionId,
                                                    Map<String, Model> outputArtifactMap) {
        return outputArtifactMap.get(nameVersionId);
    }

    private static List<ModelElement> getModelElementbyRelationshipValue(ModelElements modelElements,
                                                                         String relationshipValue) {
        List<ModelElement> matchedModelElements = new ArrayList<>();
        if (modelElements != null) {
            List<ModelElement> modelElementList = modelElements.getModelElement();
            for (ModelElement element : modelElementList) {
                List<Relationship> relationshipList = element.getRelationshipList().getRelationship();
                for (Relationship r : relationshipList) {
                    List<RelationshipData> relationshipDataList = r.getRelationshipData();
                    for (RelationshipData relationshipData : relationshipDataList) {
                        if (relationshipData.getRelationshipValue().equals(relationshipValue))
                            matchedModelElements.add(element);
                    }
                }
            }
        }
        return matchedModelElements;
    }

    public static void populateAAIGeneratedModelStore(Map<String, Model> outputArtifactMap,List<Artifact> resultData) throws JAXBException {
        for (Artifact outputArtifact : resultData) {
            if (outputArtifact.getType().equals(ArtifactType.MODEL_INVENTORY_PROFILE.name())) {
                byte[] decodedPayload = GeneratorUtil.decoder(outputArtifact.getPayload());
                Model aaiModel = getUnmarshalledArtifactModel(new String(decodedPayload));
                outputArtifactMap.put(aaiModel.getModelVers().getModelVer().get(0).getModelVersionId(), aaiModel);
            }
        }
    }

    private static Model getUnmarshalledArtifactModel(String aaiModel) throws JAXBException {
        JAXBContext jaxbContext;

        jaxbContext = JAXBContext.newInstance(Model.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        try (InputStream aaiModelStream = new ByteArrayInputStream(aaiModel.getBytes())) {
            return (Model) unmarshaller.unmarshal(aaiModelStream);
        } catch (IOException ignored) { /* ignore */ }
        throw new RuntimeException("could not resolve artifact model");
    }

    /**
     * Identify the service tosca artifact from the list of translated tosca inputs
     *
     * @param input List of translated {@link ToscaTemplate tosca} object models
     * @return Identified service {@link ToscaTemplate tosca}
     */
    private static ToscaTemplate getServiceTosca(List<ToscaTemplate> input) {
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


    private static Map<String, String> getVFModuleMetadataTosca(ToscaTemplate toscaTemplate, String vfModuleModelUUID) {
        Map<String, GroupDefinition> groupDefinitionMap = toscaTemplate.getTopology_template().getGroups();
        Map<String, String> vfModuleModelMetadata = new LinkedHashMap<>();
        for (Map.Entry<String, GroupDefinition> e : groupDefinitionMap.entrySet()) {
            if (e.getValue().getType().contains(VF_MODULE_NAMESPACE)) {
                String uuid = e.getValue().getMetadata().get("vfModuleModelUUID");
                if (uuid == vfModuleModelUUID) {
                    vfModuleModelMetadata = e.getValue().getMetadata();
                    vfModuleModelMetadata.put(VF_MODULE_DESCRIPTION, (String) e.getValue().getProperties().get(VF_MODULE_DESCRIPTION));
                }
            }
        }
        return vfModuleModelMetadata;
    }

    private static Map<String, Object> getVFModuleMembersTosca(ToscaTemplate toscaTemplate, String vfModuleModelUUID) {
        Map<String, GroupDefinition> groupDefinitionMap = toscaTemplate.getTopology_template().getGroups();
        Map<String, NodeTemplate> nodeTemplateMaps = toscaTemplate.getTopology_template().getNode_templates();
        Map<String, Object> vfModuleMembers = new LinkedHashMap<>();
        List<String> vfModuleModelMetadata;
        for (Map.Entry<String, GroupDefinition> e : groupDefinitionMap.entrySet()) {
            if (e.getValue().getType().contains(VF_MODULE_NAMESPACE)) {
                String uuid = e.getValue().getMetadata().get("vfModuleModelUUID");
                if (uuid == vfModuleModelUUID) {
                    vfModuleModelMetadata = e.getValue().getMembers();
                    Iterator itr = vfModuleModelMetadata.iterator();
                    while (itr.hasNext()) {
                        Object obj= itr.next();
                        NodeTemplate nodeTemplate = nodeTemplateMaps.get(obj);
                        String nodetype = null;
                        if (nodeTemplate != null)
                            nodetype = nodeTemplate.getType();
                        if (nodetype != null) {
                            String widgetType = membersType(nodetype);
                            if (widgetType != null)
                                vfModuleMembers.put(widgetType, obj);
                        }
                    }

                }
            }
        }

        return vfModuleMembers;
    }


    private static String membersType(String toscaType) {
        String modelToBeReturned = null;
        while (toscaType != null && toscaType.lastIndexOf('.') != -1 && modelToBeReturned == null) {

            switch (toscaType) {

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
                case VF_MODULE_NAMESPACE:
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
}
