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

package org.onap.sdc.backend.externalApis;

import com.aventstack.extentreports.Status;
import fj.data.Either;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.onap.sdc.backend.ci.tests.api.ComponentBaseTest;
import org.onap.sdc.backend.ci.tests.config.Config;
import org.onap.sdc.backend.ci.tests.datatypes.ArtifactReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.enums.*;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.utils.Decoder;
import org.onap.sdc.backend.ci.tests.utils.general.AtomicOperationUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.rest.ArtifactRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ResponseParser;
import org.onap.sdc.backend.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.common.util.ValidationUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static java.util.Arrays.asList;

public class CRUDExternalAPI extends ComponentBaseTest {

    /*private static Logger log = LoggerFactory.getLogger(CRUDExternalAPI.class.getName());
    protected static final String UPLOAD_ARTIFACT_PAYLOAD = "UHVUVFktVXNlci1LZXktRmlsZS0yOiBzc2gtcnNhDQpFbmNyeXB0aW9uOiBhZXMyNTYtY2JjDQpDb21tZW5wOA0K";
    protected static final String UPLOAD_ARTIFACT_NAME = "TLV_prv.ppk";*/

    protected Config config = Config.instance();

    protected String serviceVersion;
    protected ResourceReqDetails resourceDetails;
    protected User sdncUserDetails;
    protected ServiceReqDetails serviceDetails;

    @Rule
    public static TestName name = new TestName();

    @DataProvider(name = "uploadArtifactOnVfcVlCpViaExternalAPI", parallel = true)
    public static Object[][] dataProviderUploadArtifactOnVfcVlCpViaExternalAPI() {
        return new Object[][]{
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VNF_CATALOG.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VF_LICENSE.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VFC},

                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.YANG_XML.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VNF_CATALOG.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VF_LICENSE.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VL},

                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.YANG_XML.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VNF_CATALOG.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VF_LICENSE.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.CP},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.YANG_XML.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VNF_CATALOG.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VF_LICENSE.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VFC},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.YANG_XML.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VNF_CATALOG.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VF_LICENSE.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VL},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.YANG_XML.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VNF_CATALOG.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VF_LICENSE.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.CP},
        };
    }

    // External API
    // Upload artifact on VFC, VL, CP via external API - happy flow
    @Test(dataProvider = "uploadArtifactOnVfcVlCpViaExternalAPI")
    public void uploadArtifactOnVfcVlCpViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
        getExtendTest().log(Status.INFO, String.format("chosenLifeCycleState: %s, artifactType: %s, resourceTypeEnum: %s", chosenLifeCycleState, artifactType, resourceTypeEnum));
        uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE, chosenLifeCycleState, artifactType, resourceTypeEnum);
    }

    @DataProvider(name = "uploadArtifactOnVFViaExternalAPI", parallel = true)
    public static Object[][] dataProviderUploadArtifactOnVFViaExternalAPI() {
        return new Object[][]{
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_JSON.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_POLICY.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_EVENT.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.APPC_CONFIG.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_DOC.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_TOSCA.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.YANG_XML.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VNF_CATALOG.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VF_LICENSE.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VENDOR_LICENSE.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_JSON.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_POLICY.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_EVENT.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.APPC_CONFIG.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_DOC.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_TOSCA.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.YANG_XML.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VNF_CATALOG.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VF_LICENSE.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VENDOR_LICENSE.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
        };
    }

    // External API
    // Upload artifact on VF via external API - happy flow
    @Test(dataProvider = "uploadArtifactOnVFViaExternalAPI")
    public void uploadArtifactOnVFViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState, String artifactType) throws Exception {
        getExtendTest().log(Status.INFO, String.format("chosenLifeCycleState: %s, artifactType: %s", chosenLifeCycleState, artifactType));
        uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE, chosenLifeCycleState, artifactType, null);
    }


    @DataProvider(name = "uploadArtifactOnServiceViaExternalAPI", parallel = true)
    public static Object[][] dataProviderUploadArtifactOnServiceViaExternalAPI() {
        return new Object[][]{
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.YANG_XML.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VNF_CATALOG.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.YANG_XML.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VNF_CATALOG.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ArtifactTypeEnum.YANG_XML.getType()},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ArtifactTypeEnum.VNF_CATALOG.getType()},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType()},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType()},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ArtifactTypeEnum.OTHER.getType()}
        };
    }


    @Test(dataProvider = "uploadArtifactOnServiceViaExternalAPI")
    public void uploadArtifactOnServiceViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState, String artifactType) throws Exception {
        getExtendTest().log(Status.INFO, String.format("chosenLifeCycleState: %s, artifactType: %s", chosenLifeCycleState, artifactType));
        uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.SERVICE, chosenLifeCycleState, artifactType, null);
    }

    // Happy flow - get chosen life cycle state, artifact type and asset type
    // Create asset, upload artifact via external API + check audit & response code
    // Download artifact via external API + check audit & response code
    protected Component uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum componentTypeEnum, LifeCycleStatesEnum chosenLifeCycleState, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
        Component component = null;
        RestResponse restResponse;
        int numberOfArtifact = 0;

        // get artifact data
        ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", artifactType, true, false);

        // create component/s & upload artifact via external api
        if (ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum) {
            component = getComponentWithResourceInstanceInTargetLifeCycleState(chosenLifeCycleState, resourceTypeEnum);
            restResponse = uploadArtifactOfRIIncludingValiditionOfAuditAndResponseCode(component, component.getComponentInstances().get(0), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, 200);
            component = getNewerVersionOfComponent(component, chosenLifeCycleState);
            numberOfArtifact = (component.getComponentInstances().get(0).getDeploymentArtifacts() == null ? 0 : component.getComponentInstances().get(0).getDeploymentArtifacts().size());
        } else {
            component = getComponentInTargetLifeCycleState(componentTypeEnum.toString(), UserRoleEnum.DESIGNER, chosenLifeCycleState, resourceTypeEnum);
            restResponse = uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, 200);
            component = updateComponentDetailsByLifeCycleState(chosenLifeCycleState, component);
            numberOfArtifact = component.getDeploymentArtifacts().size();
        }

        ArtifactDefinition responseArtifact = ResponseParser.convertArtifactDefinitionResponseToJavaObject(restResponse.getResponse());
        // Get list of deployment artifact + download them via external API
        Map<String, ArtifactDefinition> deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);
        Assert.assertEquals(numberOfArtifact, deploymentArtifacts.keySet().size(), "Expected that number of deployment artifacts will be increase by one.");

        // Download the uploaded artifact via external API
        downloadResourceDeploymentArtifactExternalAPI(component, deploymentArtifacts.get(responseArtifact.getArtifactLabel()), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentTypeEnum);
        return component;
    }

    /**
     * according lifecycle state of component get updated component details
     *
     * @param chosenLifeCycleState
     * @param component
     * @return
     * @throws Exception
     */
    public Component updateComponentDetailsByLifeCycleState(LifeCycleStatesEnum chosenLifeCycleState, Component component) throws Exception {
        if (LifeCycleStatesEnum.CHECKOUT.equals(chosenLifeCycleState)) {
            component = AtomicOperationUtils.getComponentObject(component, UserRoleEnum.DESIGNER);
        } else {
            component = getNewerVersionOfComponent(component, chosenLifeCycleState);
        }
        return component;
    }

    // Upload artifact via external API + Check auditing for upload operation + Check response of external API
    public RestResponse uploadArtifactOfRIIncludingValiditionOfAuditAndResponseCode(Component component, ComponentInstance componentInstance, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails, Integer expectedResponseCode) throws Exception {
        RestResponse restResponse = ArtifactRestUtils.externalAPIUploadArtifactOfComponentInstanceOnAsset(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, component.getComponentInstances().get(0));

        // Check response of external API
        Integer responseCode = restResponse.getErrorCode();
        Assert.assertEquals(responseCode, expectedResponseCode, "Response code is not correct.");
        return restResponse;
    }


    protected Component getComponentWithResourceInstanceInTargetLifeCycleState(LifeCycleStatesEnum lifeCycleStatesEnum, ResourceTypeEnum resourceTypeEnum) throws Exception {
        Component component;
        if (resourceTypeEnum == ResourceTypeEnum.VF) {
            component = getComponentInTargetLifeCycleState(ComponentTypeEnum.SERVICE.toString(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, null);

            Component resourceInstanceDetails = getComponentInTargetLifeCycleState(ComponentTypeEnum.RESOURCE.getValue(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, null);
            AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceInstanceDetails, component, UserRoleEnum.DESIGNER, true).left().value();

            // Add artifact to service if asked for certification request - must be at least one artifact for the flow
//			if((LifeCycleStatesEnum.CERTIFICATIONREQUEST == lifeCycleStatesEnum) || (LifeCycleStatesEnum.STARTCERTIFICATION == lifeCycleStatesEnum)) {
//			}
            AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.OTHER, component, UserRoleEnum.DESIGNER, true, true).left().value();
            component = AtomicOperationUtils.changeComponentState(component, UserRoleEnum.DESIGNER, lifeCycleStatesEnum, true).getLeft();
        } else {
            component = getComponentInTargetLifeCycleState(ComponentTypeEnum.RESOURCE.toString(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, null);
            Component resourceInstanceDetails = getComponentInTargetLifeCycleState(ComponentTypeEnum.RESOURCE.getValue(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, resourceTypeEnum);
            AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceInstanceDetails, component, UserRoleEnum.DESIGNER, true).left().value();
            component = AtomicOperationUtils.changeComponentState(component, UserRoleEnum.DESIGNER, lifeCycleStatesEnum, true).getLeft();
        }
        return component;
    }

    // Upload artifact via external API + Check auditing for upload operation + Check response of external API
    protected RestResponse uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(Component component, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails, Integer expectedResponseCode) throws Exception {
        RestResponse restResponse = ArtifactRestUtils.externalAPIUploadArtifactOfTheAsset(component, sdncModifierDetails, artifactReqDetails);

        // Check response of external API
        Integer responseCode = restResponse.getErrorCode();
        Assert.assertEquals(responseCode, expectedResponseCode, "Response code is not correct.");
        return restResponse;
    }

    // Check Component version (increase by one if not in checkout)
    // Input: component, componentLifeCycleState
    // for any LifeCycleState != checkout
    // find component of version +0.1
    // check that this version different for input version
    // check that this component uniqueID different from input uniqueID
    // Return: that version
    protected synchronized Component getNewerVersionOfComponent(Component component, LifeCycleStatesEnum lifeCycleStatesEnum) throws Exception {
        Component resourceDetails = null;

        if ((!lifeCycleStatesEnum.equals(LifeCycleStatesEnum.CHECKOUT)) && (!lifeCycleStatesEnum.equals(LifeCycleStatesEnum.STARTCERTIFICATION))) {


            String resourceVersion = component.getVersion();
            String resourceUniqueID = component.getUniqueId();

            if (component.getComponentType().equals(ComponentTypeEnum.SERVICE)) {
                resourceDetails = AtomicOperationUtils.getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, component.getName(), String.format("%.1f", Double.parseDouble(component.getVersion()) + 0.1));
            } else {
                resourceDetails = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, component.getName(), String.format("%.1f", Double.parseDouble(component.getVersion()) + 0.1));
            }

            String resourceNewVersion = resourceDetails.getVersion();
            String resourceNewUniqueID = resourceDetails.getUniqueId();

            System.out.println(resourceNewVersion);
            System.out.println("Service UUID: " + resourceDetails.getUUID());
            System.out.println("Service UniqueID: " + resourceDetails.getUniqueId());

            // Checking that new version exist + different from old one by unique id
            Assert.assertNotEquals(resourceVersion, resourceNewVersion, "Expected for different resource version.");
            Assert.assertNotEquals(resourceUniqueID, resourceNewUniqueID, "Expected that resource will have new unique ID.");
        } else {
            if (component.getComponentType().equals(ComponentTypeEnum.SERVICE)) {
                resourceDetails = AtomicOperationUtils.getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, component.getName(), component.getVersion());
            } else {
                resourceDetails = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, component.getName(), component.getVersion());
            }
        }
        return resourceDetails;
    }


    // download deployment via external api + check response code for success (200) + get artifactReqDetails and verify payload + verify audit
    protected RestResponse downloadResourceDeploymentArtifactExternalAPI(Component component, ArtifactDefinition artifactDefinition, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails, ComponentTypeEnum componentTypeEnum) throws Exception {
        RestResponse restResponse;

        if (componentTypeEnum == ComponentTypeEnum.RESOURCE_INSTANCE) {
            restResponse = ArtifactRestUtils.getComponentInstanceDeploymentArtifactExternalAPI(component.getUUID(), component.getComponentInstances().get(0).getNormalizedName(), artifactDefinition.getArtifactUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), component.getComponentType().toString());
        } else {
            restResponse = ArtifactRestUtils.getResourceDeploymentArtifactExternalAPI(component.getUUID(), artifactDefinition.getArtifactUUID(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), component.getComponentType().toString());
        }

        Integer responseCode = restResponse.getErrorCode();
        Integer expectedCode = 200;
        Assert.assertEquals(responseCode, expectedCode, "Response code is not correct.");

        // For known artifact/payload - verify payload of downloaded artifact
        if (artifactReqDetails != null) {
            String response = restResponse.getResponse();
            String payloadData = artifactReqDetails.getPayload();
            String decodedPaypload = Decoder.decode(payloadData);
            Assert.assertEquals(response, decodedPaypload, "Response artifact payload not correct.");
        }
        return restResponse;
    }

    // download deployment via external api + check response code for success (200) + verify audit
    protected void downloadResourceDeploymentArtifactExternalAPI(Component component, ArtifactDefinition artifactDefinition, User sdncModifierDetails) throws Exception {
        downloadResourceDeploymentArtifactExternalAPI(component, artifactDefinition, sdncModifierDetails, null, component.getComponentType());
    }


    @DataProvider(name = "uploadArtifactOnRIViaExternalAPI", parallel = true)
    public static Object[][] dataProviderUploadArtifactOnRIViaExternalAPI() {
        return new Object[][]{
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_POLL.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType()},


                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_POLL.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_TRAP.getType()},
        };
    }


    @Test(dataProvider = "uploadArtifactOnRIViaExternalAPI")
    public void uploadArtifactOnRIViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState, String artifactType) throws Exception {
        getExtendTest().log(Status.INFO, String.format("chosenLifeCycleState: %s, artifactType: %s", chosenLifeCycleState, artifactType));
        uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE_INSTANCE, chosenLifeCycleState, artifactType, null);
    }


    @DataProvider(name = "uploadArtifactOnVfcVlCpRIViaExternalAPI", parallel = true)
    public static Object[][] dataProviderUploadArtifactOnVfcVlCpRIViaExternalAPI() {
        return new Object[][]{
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VFC},


                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VL},

                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.CP},


                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VFC},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VL},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.CP},
        };
    }


    @Test(dataProvider = "uploadArtifactOnVfcVlCpRIViaExternalAPI")
    public void uploadArtifactOnVfcVlCpRIViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
        getExtendTest().log(Status.INFO, String.format("chosenLifeCycleState: %s, artifactType: %s", chosenLifeCycleState, artifactType));
        uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE_INSTANCE, chosenLifeCycleState, artifactType, resourceTypeEnum);
    }


    @DataProvider(name = "uploadInvalidArtifactTypeExtensionLabelDescriptionCheckSumDuplicateLabelViaExternalAPI", parallel = false)
    public static Object[][] dataProviderUploadInvalidArtifactTypeExtensionLabelDescriptionCheckSumDuplicateLabelViaExternalAPI() {
        return new Object[][]{

                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidTypeToLong"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidTypeToLong"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidTypeToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidTypeToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidTypeToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidTypeToLong"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidTypeToLong"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidTypeToLong"},

                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidTypeEmpty"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidTypeEmpty"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidTypeEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidTypeEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidTypeEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidTypeEmpty"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidTypeEmpty"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidTypeEmpty"},

                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidNameToLong"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidNameToLong"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidNameToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidNameToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidNameToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidNameToLong"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidNameToLong"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidNameToLong"},

                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidNameEmpty"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidNameEmpty"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidNameEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidNameEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidNameEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidNameEmpty"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidNameEmpty"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidNameEmpty"},

                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidLabelToLong"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidLabelToLong"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidLabelToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidLabelToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidLabelToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidLabelToLong"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidLabelToLong"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidLabelToLong"},

                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidLabelEmpty"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidLabelEmpty"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidLabelEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidLabelEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidLabelEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidLabelEmpty"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidLabelEmpty"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidLabelEmpty"},

                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidDescriptionToLong"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidDescriptionToLong"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidDescriptionToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidDescriptionToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidDescriptionToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidDescriptionToLong"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidDescriptionToLong"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidDescriptionToLong"},

                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidDescriptionEmpty"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidDescriptionEmpty"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidDescriptionEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidDescriptionEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidDescriptionEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidDescriptionEmpty"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidDescriptionEmpty"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidDescriptionEmpty"},

                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithSameLabel"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithSameLabel"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithSameLabel"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithSameLabel"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithSameLabel"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithSameLabel"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithSameLabel"},

                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidCheckSum"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidCheckSum"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidCheckSum"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "uploadArtifactWithInvalidCheckSum"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidCheckSum"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidCheckSum"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "uploadArtifactWithInvalidCheckSum"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "uploadArtifactWithInvalidCheckSum"},
        };
    }


    // InvalidArtifact + check audit & response code function
    @Test(dataProvider = "uploadInvalidArtifactTypeExtensionLabelDescriptionCheckSumDuplicateLabelViaExternalAPI")
    public void uploadInvalidArtifactTypeExtensionLabelDescriptionCheckSumDuplicateLabelViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState,
                                                                                                       ComponentTypeEnum componentTypeEnum, String uploadArtifactTestType) throws Exception {
        getExtendTest().log(Status.INFO, String.format("chosenLifeCycleState: %s, componentTypeEnum: %s, uploadArtifactTestType: %s", chosenLifeCycleState, componentTypeEnum, uploadArtifactTestType));
        Component resourceDetails;
        ComponentInstance componentResourceInstanceDetails = null;
        ArtifactReqDetails artifactReqDetails;

        if (ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum) {
            artifactReqDetails = ElementFactory.getArtifactByType("Abcd", ArtifactTypeEnum.DCAE_INVENTORY_DOC.toString(), true, false);

            resourceDetails = getComponentInTargetLifeCycleState(ComponentTypeEnum.SERVICE.toString(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, null);
            resourceDetails = getComponentWithResourceInstanceInTargetLifeCycleState(chosenLifeCycleState, null);
            componentResourceInstanceDetails = resourceDetails.getComponentInstances().get(0);
        } else {
            artifactReqDetails = ElementFactory.getArtifactByType("Abcd", ArtifactTypeEnum.OTHER.toString(), true, false);
            resourceDetails = getComponentInTargetLifeCycleState(componentTypeEnum.toString(), UserRoleEnum.DESIGNER, chosenLifeCycleState, null);
        }


        switch (uploadArtifactTestType) {
            case "uploadArtifactWithInvalidTypeToLong":
                uploadArtifactWithInvalidTypeToLong(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
                break;
            case "uploadArtifactWithInvalidTypeEmpty":
                uploadArtifactWithInvalidTypeEmpty(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
                break;
            case "uploadArtifactWithInvalidCheckSum":
                uploadArtifactWithInvalidCheckSum(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
                break;
            case "uploadArtifactWithInvalidNameToLong":
                uploadArtifactWithInvalidNameToLong(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
                break;
            case "uploadArtifactWithInvalidNameEmpty":
                uploadArtifactWithInvalidNameEmpty(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
                break;
            case "uploadArtifactWithInvalidLabelToLong":
                uploadArtifactWithInvalidLabelToLong(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
                break;
            case "uploadArtifactWithInvalidLabelEmpty":
                uploadArtifactWithInvalidLabelEmpty(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
                break;
            case "uploadArtifactWithInvalidDescriptionToLong":
                uploadArtifactWithInvalidDescriptionToLong(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
                break;
            case "uploadArtifactWithInvalidDescriptionEmpty":
                uploadArtifactWithInvalidDescriptionEmpty(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
                break;
            case "uploadArtifactWithSameLabel":
            default:
                uploadArtifactWithSameLabel(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
                break;
        }
    }

    // Upload artifact with invalid type via external API - to long type
    protected void uploadArtifactWithInvalidTypeToLong(Component component, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
                                                       ComponentInstance componentResourceInstanceDetails) throws Exception {
        artifactReqDetails.setArtifactType("dsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfdsdsfdsfds");
        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED.name());
        List<String> variables = asList(artifactReqDetails.getArtifactType());

        uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                artifactReqDetails, componentResourceInstanceDetails, errorInfo, variables, null, false);
    }

    // Upload artifact with invalid type via external API - empty type
    protected void uploadArtifactWithInvalidTypeEmpty(Component component, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
                                                      ComponentInstance componentResourceInstanceDetails) throws Exception {
        artifactReqDetails.setArtifactType("");
        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_TYPE_NOT_SUPPORTED.name());
        List<String> variables = asList(artifactReqDetails.getArtifactType());

        uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                artifactReqDetails, componentResourceInstanceDetails, errorInfo, variables, null, false);
    }

    // Upload artifact with invalid checksum via external API
    protected void uploadArtifactWithInvalidCheckSum(Component component, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
                                                     ComponentInstance componentResourceInstanceDetails) throws Exception {
        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_INVALID_MD5.name());
        List<String> variables = asList();
        uploadArtifactWithInvalidCheckSumOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                artifactReqDetails, componentResourceInstanceDetails, errorInfo, variables);
    }

    // Upload artifact with valid type & invalid name via external API - name to long
    protected void uploadArtifactWithInvalidNameToLong(Component component, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
                                                       ComponentInstance componentResourceInstanceDetails) throws Exception {
        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.EXCEEDS_LIMIT.name());
        List<String> variables = asList("artifact name", "255");
        artifactReqDetails.setArtifactName("invalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidType");
        uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                artifactReqDetails, componentResourceInstanceDetails, errorInfo, variables, null, false);
    }

    // Upload artifact with valid type & invalid name via external API - name is empty
    protected void uploadArtifactWithInvalidNameEmpty(Component component, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
                                                      ComponentInstance componentResourceInstanceDetails) throws Exception {
        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_ARTIFACT_NAME.name());
        List<String> variables = asList();

        artifactReqDetails.setArtifactName("");
        uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                artifactReqDetails, componentResourceInstanceDetails, errorInfo, variables, null, false);
    }


    // Upload artifact with valid type & invalid label via external API - label to long
    protected void uploadArtifactWithInvalidLabelToLong(Component component, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
                                                        ComponentInstance componentResourceInstanceDetails) throws Exception {

        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.EXCEEDS_LIMIT.name());
        List<String> variables = asList("artifact label", "255");
        artifactReqDetails.setArtifactLabel("invalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidType");
        uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                artifactReqDetails, componentResourceInstanceDetails, errorInfo, variables, null, false);
    }

    // Upload artifact with valid type & invalid label via external API - label is empty
    protected void uploadArtifactWithInvalidLabelEmpty(Component component, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
                                                       ComponentInstance componentResourceInstanceDetails) throws Exception {

        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_DATA.name());
        List<String> variables = asList("artifact label");
        artifactReqDetails.setArtifactLabel("");
        uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                artifactReqDetails, componentResourceInstanceDetails, errorInfo, variables, null, false);
    }

    // Upload artifact with invalid description via external API - to long description
    protected void uploadArtifactWithInvalidDescriptionToLong(Component component, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
                                                              ComponentInstance componentResourceInstanceDetails) throws Exception {

        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.EXCEEDS_LIMIT.name());
        List<String> variables = asList("artifact description", "256");
        artifactReqDetails.setDescription("invalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidType");
        uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                artifactReqDetails, componentResourceInstanceDetails, errorInfo, variables, null, false);
    }


    // Upload artifact with invalid description via external API - empty description
    protected void uploadArtifactWithInvalidDescriptionEmpty(Component component, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
                                                             ComponentInstance componentResourceInstanceDetails) throws Exception {

        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_DATA.name());
        List<String> variables = asList("artifact description");
        artifactReqDetails.setDescription("");
        uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                artifactReqDetails, componentResourceInstanceDetails, errorInfo, variables, null, false);
    }

    // Upload artifact with same label via external API
    protected void uploadArtifactWithSameLabel(Component component, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
                                               ComponentInstance componentResourceInstanceDetails) throws Exception {

        RestResponse restResponse = null;
        if (componentResourceInstanceDetails != null) {
            restResponse = ArtifactRestUtils.externalAPIUploadArtifactOfComponentInstanceOnAsset(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
        } else {
            restResponse = ArtifactRestUtils.externalAPIUploadArtifactOfTheAsset(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails);

        }

        ArtifactDefinition artifactDefinition = ResponseParser.convertArtifactDefinitionResponseToJavaObject(restResponse.getResponse());
        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_EXIST.name());

        List<String> variables = asList(artifactDefinition.getArtifactDisplayName());
        ArtifactReqDetails artifactReqDetailsSameLabel = ElementFactory.getArtifactByType("Abcd", ArtifactTypeEnum.DCAE_INVENTORY_EVENT.toString(), true, false);
        artifactReqDetailsSameLabel.setArtifactLabel(artifactReqDetails.getArtifactLabel());
        uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                artifactReqDetailsSameLabel, componentResourceInstanceDetails, errorInfo, variables, null, false);
    }

    protected RestResponse uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(Component component, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
                                                                                          ComponentInstance componentResourceInstanceDetails, ErrorInfo errorInfo, List<String> variables, LifeCycleStatesEnum lifeCycleStatesEnum, Boolean includeResourceNameInAudit) throws Exception {
        RestResponse restResponse;
        if (componentResourceInstanceDetails != null) {
            restResponse = ArtifactRestUtils.externalAPIUploadArtifactOfComponentInstanceOnAsset(component, sdncModifierDetails, artifactReqDetails, componentResourceInstanceDetails);
        } else {
            restResponse = ArtifactRestUtils.externalAPIUploadArtifactOfTheAsset(component, sdncModifierDetails, artifactReqDetails);

        }
        // validate response code
        Integer responseCode = restResponse.getErrorCode();
        Assert.assertEquals(responseCode, errorInfo.getCode(), "Response code is not correct.");
        return restResponse;
    }


    protected RestResponse uploadArtifactWithInvalidCheckSumOfAssetIncludingValiditionOfAuditAndResponseCode(Component component, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails,
                                                                                                             ComponentInstance componentResourceInstanceDetails, ErrorInfo errorInfo, List<String> variables) throws Exception {
        RestResponse restResponse;
        if (componentResourceInstanceDetails != null) {
            restResponse = ArtifactRestUtils.externalAPIUploadArtifactWithInvalidCheckSumOfComponentInstanceOnAsset(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
        } else {
            restResponse = ArtifactRestUtils.externalAPIUploadArtifactWithInvalidCheckSumOfTheAsset(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails);

        }
        // validate response code
        Integer responseCode = restResponse.getErrorCode();
        Assert.assertEquals(responseCode, errorInfo.getCode(), "Response code is not correct.");
        return restResponse;

    }

    @DataProvider(name = "uploadArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset", parallel = true)
    public static Object[][] dataProviderUploadArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset() {
        return new Object[][]{
                {ComponentTypeEnum.RESOURCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT},
                {ComponentTypeEnum.RESOURCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT},

                {ComponentTypeEnum.RESOURCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKIN},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKIN},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKIN},
                {ComponentTypeEnum.RESOURCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKOUT},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKOUT},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKOUT},

                {ComponentTypeEnum.RESOURCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKIN},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKIN},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKIN},
                {ComponentTypeEnum.RESOURCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKOUT},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKOUT},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKOUT},

                {ComponentTypeEnum.RESOURCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKIN},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKIN},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKIN},
                {ComponentTypeEnum.RESOURCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKOUT},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKOUT},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKOUT},
        };
    }

    // External API
    // Upload artifact by diffrent user then creator of asset - Fail
    @Test(dataProvider = "uploadArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset")
    public void uploadArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset(ComponentTypeEnum componentTypeEnum, UserRoleEnum userRoleEnum, LifeCycleStatesEnum lifeCycleStatesEnum) throws Exception {
        getExtendTest().log(Status.INFO, String.format("componentTypeEnum: %s, userRoleEnum: %s, lifeCycleStatesEnum: %s", componentTypeEnum, userRoleEnum, lifeCycleStatesEnum));
        Component resourceDetails;
        ComponentInstance componentResourceInstanceDetails = null;
        ArtifactReqDetails artifactReqDetails;

        if (ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum) {
            artifactReqDetails = ElementFactory.getArtifactByType("Abcd", ArtifactTypeEnum.DCAE_INVENTORY_DOC.toString(), true, false);

            resourceDetails = getComponentWithResourceInstanceInTargetLifeCycleState(lifeCycleStatesEnum, null);
            componentResourceInstanceDetails = resourceDetails.getComponentInstances().get(0);
        } else {
            artifactReqDetails = ElementFactory.getArtifactByType("Abcd", ArtifactTypeEnum.OTHER.toString(), true, false);

            resourceDetails = getComponentInTargetLifeCycleState(componentTypeEnum.toString(), UserRoleEnum.DESIGNER, lifeCycleStatesEnum, null);
        }

        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
        List<String> variables = asList();

        uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(userRoleEnum),
                artifactReqDetails, componentResourceInstanceDetails, errorInfo, variables, lifeCycleStatesEnum, true);

    }

    @DataProvider(name = "uploadArtifactOnAssetWhichNotExist", parallel = true)
    public static Object[][] dataProviderUploadArtifactOnAssetWhichNotExist() {
        return new Object[][]{
                {ComponentTypeEnum.SERVICE},
                {ComponentTypeEnum.RESOURCE},
                {ComponentTypeEnum.RESOURCE_INSTANCE},
        };
    }

    // External API
    // Upload artifact on VF via external API - happy flow
    @Test(dataProvider = "uploadArtifactOnAssetWhichNotExist")
    public void uploadArtifactOnAssetWhichNotExist(ComponentTypeEnum componentTypeEnum) throws Exception {
        getExtendTest().log(Status.INFO, String.format("componentTypeEnum: %s", componentTypeEnum));
        Component resourceDetails;
        ComponentInstance componentResourceInstanceDetails = null;
        ArtifactReqDetails artifactReqDetails;

        if (ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum) {
            artifactReqDetails = ElementFactory.getArtifactByType("Abcd", ArtifactTypeEnum.DCAE_INVENTORY_DOC.toString(), true, false);

            resourceDetails = getComponentWithResourceInstanceInTargetLifeCycleState(LifeCycleStatesEnum.CHECKIN, null);
            componentResourceInstanceDetails = resourceDetails.getComponentInstances().get(0);

            resourceDetails.setUUID("12345");
            componentResourceInstanceDetails.setNormalizedName("12345");
        } else {
            artifactReqDetails = ElementFactory.getArtifactByType("Abcd", ArtifactTypeEnum.OTHER.getType(), true, false);

            resourceDetails = getComponentInTargetLifeCycleState(componentTypeEnum.toString(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, null);

            resourceDetails.setUUID("12345");
        }

        String componentTypeError = ActionStatus.RESOURCE_NOT_FOUND.name();
        if (ComponentTypeEnum.SERVICE == componentTypeEnum) {
            componentTypeError = ActionStatus.SERVICE_NOT_FOUND.name();
        }
        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(componentTypeError);
        List<String> variables = asList(resourceDetails.getUUID());

        uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                artifactReqDetails, componentResourceInstanceDetails, errorInfo, variables, LifeCycleStatesEnum.CHECKIN, false);
    }

    @DataProvider(name = "uploadArtifactOnAssetWhichInInvalidStateForUploading", parallel = true)
    public static Object[][] dataProviderUploadArtifactOnAssetWhichInInvalidStateForUploading() {
        return new Object[][]{
                {ComponentTypeEnum.SERVICE},
        };
    }

    @Test(dataProvider = "uploadArtifactOnAssetWhichInInvalidStateForUploading")
    public void uploadArtifactOnAssetWhichInInvalidStateForUploading(ComponentTypeEnum componentTypeEnum) throws Exception {
        getExtendTest().log(Status.INFO, String.format("componentTypeEnum: %s", componentTypeEnum));
        ComponentInstance componentResourceInstanceDetails = null;

        ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("Abcd", ArtifactTypeEnum.OTHER.toString(), true, false);
        Component resourceDetails = getComponentInTargetLifeCycleState(componentTypeEnum.toString(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.STARTCERTIFICATION, null);

        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE.name());
        List<String> variables = asList(resourceDetails.getName(), resourceDetails.getComponentType().toString().toLowerCase(), resourceDetails.getLastUpdaterFullName().split(" ")[0],
                resourceDetails.getLastUpdaterFullName().split(" ")[1], resourceDetails.getLastUpdaterUserId());

        uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                artifactReqDetails, componentResourceInstanceDetails, errorInfo, variables, LifeCycleStatesEnum.STARTCERTIFICATION, true);
    }

    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    //					Update External API											  //
    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////

    @DataProvider(name = "updateArtifactForServiceViaExternalAPI", parallel = true)
    public static Object[][] dataProviderUpdateArtifactForServiceViaExternalAPI() {
        return new Object[][]{
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.YANG_XML.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VNF_CATALOG.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.YANG_XML.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VNF_CATALOG.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ArtifactTypeEnum.YANG_XML.getType()},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ArtifactTypeEnum.VNF_CATALOG.getType()},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType()},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType()},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ArtifactTypeEnum.OTHER.getType()},
                {LifeCycleStatesEnum.CERTIFY, ArtifactTypeEnum.YANG_XML.getType()},
                {LifeCycleStatesEnum.CERTIFY, ArtifactTypeEnum.VNF_CATALOG.getType()},
                {LifeCycleStatesEnum.CERTIFY, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType()},
                {LifeCycleStatesEnum.CERTIFY, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType()},
                {LifeCycleStatesEnum.CERTIFY, ArtifactTypeEnum.OTHER.getType()}
        };
    }

    // Update artifact for Service - Success
    @Test(dataProvider = "updateArtifactForServiceViaExternalAPI")
    public void updateArtifactForServiceViaExternalAPI(LifeCycleStatesEnum lifeCycleStatesEnum, String artifactType) throws Exception {
        getExtendTest().log(Status.INFO, String.format("lifeCycleStatesEnum: %s, artifactType: %s", lifeCycleStatesEnum, artifactType));
        Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.SERVICE, LifeCycleStatesEnum.CHECKOUT, artifactType, null);
        updateArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.SERVICE, lifeCycleStatesEnum, artifactType);

        // for certify version check that previous version exist, and that it artifact can be download + checksum
        if (lifeCycleStatesEnum.equals(LifeCycleStatesEnum.CERTIFY)) {
            // Download the uploaded artifact via external API
            downloadResourceDeploymentArtifactExternalAPIAndComparePayLoadOfArtifactType(component, artifactType, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ComponentTypeEnum.SERVICE);
        }
    }

    @DataProvider(name = "updateArtifactForVFViaExternalAPI", parallel = true)
    public static Object[][] dataProviderUpdateArtifactForVFViaExternalAPI() {
        return new Object[][]{
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_JSON.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_POLICY.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_EVENT.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.APPC_CONFIG.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_DOC.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_TOSCA.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.YANG_XML.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VNF_CATALOG.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VF_LICENSE.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VENDOR_LICENSE.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_JSON.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_POLICY.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_EVENT.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.APPC_CONFIG.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_DOC.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_TOSCA.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.YANG_XML.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VNF_CATALOG.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VF_LICENSE.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VENDOR_LICENSE.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
        };
    }

    // Update artifact for VF - Success
    @Test(dataProvider = "updateArtifactForVFViaExternalAPI")
    public void updateArtifactForVFViaExternalAPI(LifeCycleStatesEnum lifeCycleStatesEnum, String artifactType) throws Exception {
        getExtendTest().log(Status.INFO, String.format("lifeCycleStatesEnum: %s, artifactType: %s", lifeCycleStatesEnum, artifactType));
        Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE, LifeCycleStatesEnum.CHECKOUT, artifactType, null);
        updateArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.RESOURCE, lifeCycleStatesEnum, artifactType);

        // for certify version check that previous version exist, and that it artifact can be download + checksum
        if (lifeCycleStatesEnum.equals(LifeCycleStatesEnum.CERTIFY)) {
            // Download the uploaded artifact via external API
            downloadResourceDeploymentArtifactExternalAPIAndComparePayLoadOfArtifactType(component, artifactType, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ComponentTypeEnum.RESOURCE);
        }
    }

    @DataProvider(name = "updateArtifactForVfcVlCpViaExternalAPI", parallel = true)
    public static Object[][] dataProviderUpdateArtifactForVfcVlCpViaExternalAPI() {
        return new Object[][]{
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.YANG_XML.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VNF_CATALOG.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VF_LICENSE.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VFC},

                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.YANG_XML.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VNF_CATALOG.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VF_LICENSE.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VL},

                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.YANG_XML.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VNF_CATALOG.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VF_LICENSE.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.CP},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.YANG_XML.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VNF_CATALOG.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VF_LICENSE.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VFC},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.YANG_XML.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VNF_CATALOG.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VF_LICENSE.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VL},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.YANG_XML.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VNF_CATALOG.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VF_LICENSE.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.CP},
        };
    }

    // Update artifact for VFC/VL/CP - Success
    @Test(dataProvider = "updateArtifactForVfcVlCpViaExternalAPI")
    public void updateArtifactForVfcVlCpViaExternalAPI(LifeCycleStatesEnum lifeCycleStatesEnum, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
        getExtendTest().log(Status.INFO, String.format("lifeCycleStatesEnum: %s, artifactType: %s, resourceTypeEnum: %s", lifeCycleStatesEnum, artifactType, resourceTypeEnum));
        Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE, LifeCycleStatesEnum.CHECKOUT, artifactType, resourceTypeEnum);
        updateArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.RESOURCE, lifeCycleStatesEnum, artifactType);

        // for certify version check that previous version exist, and that it artifact can be download + checksum
        if (lifeCycleStatesEnum.equals(LifeCycleStatesEnum.CERTIFY)) {
            // Download the uploaded artifact via external API
            downloadResourceDeploymentArtifactExternalAPIAndComparePayLoadOfArtifactType(component, artifactType, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ComponentTypeEnum.RESOURCE);
        }
    }

    @DataProvider(name = "updateArtifactOfVfcVlCpForVfciVliCpiViaExternalAPI", parallel = true)
    public static Object[][] dataProviderUpdateArtifactOfVfcVlCpForVfciVliCpiViaExternalAPI() {
        return new Object[][]{
                {ResourceTypeEnum.VFC},
                {ResourceTypeEnum.VL},
                {ResourceTypeEnum.CP}
        };
    }

    // Verify that it cannot update VFC/VL/CP artifact on VFCi/VLi/CPi - Failure flow
    @Test(dataProvider = "updateArtifactOfVfcVlCpForVfciVliCpiViaExternalAPI")
    public void updateArtifactOfVfcVlCpForVfciVliCpiViaExternalAPI(ResourceTypeEnum resourceTypeEnum) throws Exception {
        getExtendTest().log(Status.INFO, String.format("resourceTypeEnum: %s", resourceTypeEnum));

        Component resourceInstanceDetails = getComponentInTargetLifeCycleState(ComponentTypeEnum.RESOURCE.getValue(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, resourceTypeEnum);
        ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", ArtifactTypeEnum.SNMP_TRAP.getType(), true, false);
        uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceInstanceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, 200);
        resourceInstanceDetails = AtomicOperationUtils.changeComponentState(resourceInstanceDetails, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
        Component component = getComponentInTargetLifeCycleState(ComponentTypeEnum.RESOURCE.toString(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, null);
        AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceInstanceDetails, component, UserRoleEnum.DESIGNER, true).left().value();
        component = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, component.getName(), component.getVersion());

        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.OK.name());
        Map<String, ArtifactDefinition> deploymentArtifacts;
        deploymentArtifacts = getDeploymentArtifactsOfAsset(component, ComponentTypeEnum.RESOURCE_INSTANCE);
        String artifactUUID = null;
        for (String key : deploymentArtifacts.keySet()) {
            if (key.startsWith("ci") && StringUtils.isNotEmpty(deploymentArtifacts.get(key).getArtifactUUID())) {
                artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
                break;
            }
        }
        List<String> variables = asList(artifactUUID);
        updateArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.RESOURCE_INSTANCE, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType(), errorInfo, variables, UserRoleEnum.DESIGNER);

    }

    @DataProvider(name = "updateArtifactOnRIViaExternalAPI", parallel = true)
    public static Object[][] dataProviderUpdateArtifactOnRIViaExternalAPI() {
        return new Object[][]{
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), null},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), null},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), null},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), null},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), null},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), null},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), null},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), null},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), null},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), null},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), null},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), null},

        };
    }

    @Test(dataProvider = "updateArtifactOnRIViaExternalAPI")
    public void updateArtifactOnRIViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
        getExtendTest().log(Status.INFO, String.format("chosenLifeCycleState: %s, artifactType: %s", chosenLifeCycleState, artifactType));
        Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE_INSTANCE, LifeCycleStatesEnum.CHECKOUT, artifactType, resourceTypeEnum);
        updateArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.RESOURCE_INSTANCE, chosenLifeCycleState, artifactType);

        // for certify version check that previous version exist, and that it artifact can be download + checksum
        if (chosenLifeCycleState.equals(LifeCycleStatesEnum.CERTIFY)) {
            // Download the uploaded artifact via external API
            downloadResourceDeploymentArtifactExternalAPIAndComparePayLoadOfArtifactType(component, artifactType, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ComponentTypeEnum.RESOURCE_INSTANCE);
        }
    }

    @DataProvider(name = "updateArtifactOnVfcVlCpRIViaExternalAPI", parallel = true)
    public static Object[][] dataProviderUpdateArtifactOnVfcVlCpRIViaExternalAPI() {
        return new Object[][]{
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VFC},


                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VL},

                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.CP},


                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VFC},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VL},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.CP},
        };
    }

    @Test(dataProvider = "updateArtifactOnVfcVlCpRIViaExternalAPI")
    public void updateArtifactOnVfcVlCpRIViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
        getExtendTest().log(Status.INFO, String.format("chosenLifeCycleState: %s, artifactType: %s", chosenLifeCycleState, artifactType));
        Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE_INSTANCE, LifeCycleStatesEnum.CHECKOUT, artifactType, resourceTypeEnum);
        updateArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.RESOURCE_INSTANCE, chosenLifeCycleState, artifactType);


        // for certify version check that previous version exist, and that it artifact can be download + checksum
        if (chosenLifeCycleState.equals(LifeCycleStatesEnum.CERTIFY)) {
            // Download the uploaded artifact via external API
            downloadResourceDeploymentArtifactExternalAPIAndComparePayLoadOfArtifactType(component, artifactType, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), ComponentTypeEnum.RESOURCE_INSTANCE);
        }
    }

    @DataProvider(name = "updateArtifactOnVFViaExternalAPIByDifferentUserThenCreatorOfAsset", parallel = true)
    public static Object[][] dataProviderUpdateArtifactOnVFViaExternalAPIByDifferentUserThenCreatorOfAsset() {
        return new Object[][]{
                {ComponentTypeEnum.RESOURCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()},

                {ComponentTypeEnum.RESOURCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()},

                {ComponentTypeEnum.RESOURCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()},
                {ComponentTypeEnum.RESOURCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()},

                {ComponentTypeEnum.RESOURCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()},
                {ComponentTypeEnum.RESOURCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()},

                {ComponentTypeEnum.RESOURCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()},
                {ComponentTypeEnum.RESOURCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()},
        };
    }

    // External API
    // Update artifact by different user then creator of asset - Fail
    @Test(dataProvider = "updateArtifactOnVFViaExternalAPIByDifferentUserThenCreatorOfAsset")
    public void updateArtifactOnVFViaExternalAPIByDifferentUserThenCreatorOfAsset(ComponentTypeEnum componentTypeEnum, UserRoleEnum userRoleEnum, LifeCycleStatesEnum lifeCycleStatesEnum, String artifactType) throws Exception {
        getExtendTest().log(Status.INFO, String.format("componentTypeEnum: %s, userRoleEnum: %s, lifeCycleStatesEnum: %s, artifactType: %s", componentTypeEnum, userRoleEnum, lifeCycleStatesEnum, artifactType));
        Component component = uploadArtifactOnAssetViaExternalAPI(componentTypeEnum, LifeCycleStatesEnum.CHECKIN, artifactType, null);
        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
        List<String> variables = asList();
        updateArtifactOnAssetViaExternalAPI(component, componentTypeEnum, lifeCycleStatesEnum, artifactType, errorInfo, variables, userRoleEnum);
    }

    @DataProvider(name = "updateArtifactOnAssetWhichNotExist", parallel = true)
    public static Object[][] dataProviderUpdateArtifactOnAssetWhichNotExist() {
        return new Object[][]{
                {ComponentTypeEnum.SERVICE, ArtifactTypeEnum.OTHER.getType(), null},
                {ComponentTypeEnum.RESOURCE, ArtifactTypeEnum.OTHER.getType(), null},
                {ComponentTypeEnum.RESOURCE_INSTANCE, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.VF},
        };
    }

    // External API
    // Upload artifact on VF via external API - happy flow
    @Test(dataProvider = "updateArtifactOnAssetWhichNotExist")
    public void updateArtifactOnAssetWhichNotExist(ComponentTypeEnum componentTypeEnum, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
        getExtendTest().log(Status.INFO, String.format("componentTypeEnum: %s, artifactType: %s", componentTypeEnum, artifactType));
        Component component = uploadArtifactOnAssetViaExternalAPI(componentTypeEnum, LifeCycleStatesEnum.CHECKIN, artifactType, resourceTypeEnum);

        // get updated artifact data
        Map<String, ArtifactDefinition> deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);
        ArtifactReqDetails artifactReqDetails = getUpdatedArtifact(deploymentArtifacts, artifactType);
        String artifactUUID = deploymentArtifacts.get(artifactReqDetails.getArtifactLabel()).getArtifactUUID();

        // Invalid artifactUUID
        String invalidArtifactUUID = "12341234-1234-1234-1234-123412341234";
        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_NOT_FOUND.name());
        List<String> variables = asList(invalidArtifactUUID);

        if (componentTypeEnum.equals(ComponentTypeEnum.RESOURCE_INSTANCE)) {
            updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    component.getComponentInstances().get(0), artifactReqDetails, invalidArtifactUUID, errorInfo, variables, null, true);
        } else {
            updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    null, artifactReqDetails, invalidArtifactUUID, errorInfo, variables, null, true);

        }

        // Invalid componentUUID
//		errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESOURCE_NOT_FOUND.name());
//		variables = asList("null");

        if (componentTypeEnum.equals(ComponentTypeEnum.RESOURCE_INSTANCE)) {
            component.getComponentInstances().get(0).setNormalizedName("invalidNormalizedName");

            errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER.name());

            variables = asList("invalidNormalizedName", ComponentTypeEnum.RESOURCE_INSTANCE.getValue().toLowerCase(), ComponentTypeEnum.SERVICE.getValue(), component.getName());
            updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    component.getComponentInstances().get(0), artifactReqDetails, artifactUUID, errorInfo, variables, LifeCycleStatesEnum.CHECKIN, true);
        } else {
            component.setUUID("invalidComponentUUID");

            errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESOURCE_NOT_FOUND.name());
            variables = asList("null");

            updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    null, artifactReqDetails, artifactUUID, errorInfo, variables, LifeCycleStatesEnum.CHECKIN, false);
        }
        performClean();
    }

    @DataProvider(name = "updateArtifactOnAssetWhichInInvalidStateForUploading", parallel = true)
    public static Object[][] dataProviderUpdateProviderDeleteArtifactOnAssetWhichInInvalidStateForUploading() {
        return new Object[][]{
                {ComponentTypeEnum.SERVICE, ArtifactTypeEnum.OTHER.getType()},
        };
    }

    @Test(dataProvider = "updateArtifactOnAssetWhichInInvalidStateForUploading")
    public void updateArtifactOnAssetWhichInInvalidStateForUploading(ComponentTypeEnum componentTypeEnum, String artifactType) throws Exception {
        getExtendTest().log(Status.INFO, String.format("componentTypeEnum: %s, artifactType: %s", componentTypeEnum, artifactType));
        Component component = uploadArtifactOnAssetViaExternalAPI(componentTypeEnum, LifeCycleStatesEnum.CHECKIN, artifactType, null);
        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE.name());
        List<String> variables = asList(component.getName(), component.getComponentType().toString().toLowerCase(), ElementFactory.getDefaultUser(UserRoleEnum.TESTER).getFirstName(),
                ElementFactory.getDefaultUser(UserRoleEnum.TESTER).getLastName(), ElementFactory.getDefaultUser(UserRoleEnum.TESTER).getUserId());
        updateArtifactOnAssetViaExternalAPI(component, componentTypeEnum, LifeCycleStatesEnum.STARTCERTIFICATION, artifactType, errorInfo, variables, UserRoleEnum.DESIGNER);

    }

    @DataProvider(name = "updateInvalidArtifactTypeExtensionLabelDescriptionCheckSumDuplicateLabelViaExternalAPI", parallel = true)
    public static Object[][] dataProviderUpdateInvalidArtifactTypeExtensionLabelDescriptionCheckSumDuplicateLabelViaExternalAPI() {
        return new Object[][]{
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidCheckSum"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidCheckSum"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidCheckSum"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidCheckSum"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidCheckSum"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidCheckSum"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidCheckSum"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidCheckSum"},

                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidNameToLong"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidNameToLong"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidNameToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidNameToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidNameToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidNameToLong"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidNameToLong"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidNameToLong"},

                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidNameEmpty"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidNameEmpty"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidNameEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidNameEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidNameEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidNameEmpty"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidNameEmpty"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidNameEmpty"},

                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidLabelToLong"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidLabelToLong"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidLabelToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidLabelToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidLabelToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidLabelToLong"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidLabelToLong"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidLabelToLong"},

                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidLabelEmpty"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidLabelEmpty"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidLabelEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidLabelEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidLabelEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidLabelEmpty"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidLabelEmpty"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidLabelEmpty"},

                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidDescriptionToLong"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidDescriptionToLong"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidDescriptionToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidDescriptionToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidDescriptionToLong"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidDescriptionToLong"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidDescriptionToLong"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidDescriptionToLong"},

                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidDescriptionEmpty"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidDescriptionEmpty"},
                {LifeCycleStatesEnum.CHECKOUT, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidDescriptionEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE, "updateArtifactWithInvalidDescriptionEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidDescriptionEmpty"},
                {LifeCycleStatesEnum.CHECKIN, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidDescriptionEmpty"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.SERVICE, "updateArtifactWithInvalidDescriptionEmpty"},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ComponentTypeEnum.RESOURCE_INSTANCE, "updateArtifactWithInvalidDescriptionEmpty"},
        };
    }

    // InvalidArtifact + check audit & response code function
    @Test(dataProvider = "updateInvalidArtifactTypeExtensionLabelDescriptionCheckSumDuplicateLabelViaExternalAPI")
    public void updateInvalidArtifactTypeExtensionLabelDescriptionCheckSumDuplicateLabelViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState,
                                                                                                       ComponentTypeEnum componentTypeEnum, String uploadArtifactTestType) throws Exception {
        getExtendTest().log(Status.INFO, String.format("chosenLifeCycleState: %s, componentTypeEnum: %s, uploadArtifactTestType: %s", chosenLifeCycleState, componentTypeEnum, uploadArtifactTestType));
        Component component;
        ComponentInstance componentInstance = null;
        String artifactType;

        if (ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum) {
            artifactType = ArtifactTypeEnum.DCAE_INVENTORY_DOC.toString();
            component = uploadArtifactOnAssetViaExternalAPI(componentTypeEnum, LifeCycleStatesEnum.CHECKIN, artifactType, null);
            componentInstance = component.getComponentInstances().get(0);
        } else {
            artifactType = ArtifactTypeEnum.OTHER.toString();
            component = uploadArtifactOnAssetViaExternalAPI(componentTypeEnum, LifeCycleStatesEnum.CHECKIN, artifactType, null);
        }

        component = AtomicOperationUtils.changeComponentState(component, UserRoleEnum.DESIGNER, chosenLifeCycleState, true).getLeft();

        switch (uploadArtifactTestType) {
            case "updateArtifactWithInvalidCheckSum":
                updateArtifactWithInvalidCheckSum(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactType, componentInstance);
                break;
            case "updateArtifactWithInvalidNameToLong":
                updateArtifactWithInvalidNameToLong(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactType, componentInstance);
                break;
            case "updateArtifactWithInvalidNameEmpty":
                updateArtifactWithInvalidNameEmpty(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactType, componentInstance);
                break;
            case "updateArtifactWithInvalidLabelToLong":
                updateArtifactWithInvalidLabelToLong(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactType, componentInstance);
                break;
            case "updateArtifactWithInvalidLabelEmpty":
                updateArtifactWithInvalidLabelEmpty(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactType, componentInstance);
                break;
            case "updateArtifactWithInvalidDescriptionToLong":
                updateArtifactWithInvalidDescriptionToLong(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactType, componentInstance);
                break;
            case "updateArtifactWithInvalidDescriptionEmpty":
            default:
                updateArtifactWithInvalidDescriptionEmpty(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactType, componentInstance);
                break;
        }

///////////////////////////////////////////////////////////////////////////////		
//		// TODO: there is defect when checking invalid type
////		// Upload artifact with invalid type via external API
////		// invalid type
////		String artifactType = artifactReqDetails.getArtifactType();
////		artifactReqDetails.setArtifactType("invalidType");
////		restResponse = uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
////		// empty type
////		artifactReqDetails.setArtifactType("");
////		restResponse = uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentResourceInstanceDetails);
////		artifactReqDetails.setArtifactType(artifactType);
///////////////////////////////////////////////////////////////////////////////			
    }

    // TODO
    // Update artifact with invalid checksum via external API
    protected void updateArtifactWithInvalidCheckSum(Component component, User sdncModifierDetails, String artifactType, ComponentInstance componentInstance) throws Exception {
        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_INVALID_MD5.name());
        List<String> variables = asList();
    }


    // Update artifact with valid type & invalid name via external API - name to long
    protected void updateArtifactWithInvalidNameToLong(Component component, User sdncModifierDetails, String artifactType,
                                                       ComponentInstance componentInstance) throws Exception {

        ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", artifactType, true, true);
        String artifactUUID = getFirstArtifactUuidFromComponent(component);

        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.EXCEEDS_LIMIT.name());
        List<String> variables = asList("artifact name", "255");
        artifactReqDetails.setArtifactName("invalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidType");

        if (componentInstance != null) {
            updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    component.getComponentInstances().get(0), artifactReqDetails, artifactUUID, errorInfo, variables, null, true);
        } else {
            updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    null, artifactReqDetails, artifactUUID, errorInfo, variables, null, true);

        }
    }

    // Update artifact with valid type & invalid name via external API - name is empty
    protected void updateArtifactWithInvalidNameEmpty(Component component, User sdncModifierDetails, String artifactType,
                                                      ComponentInstance componentInstance) throws Exception {

        ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", artifactType, true, true);
        String artifactUUID = getFirstArtifactUuidFromComponent(component);

        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_ARTIFACT_NAME.name());
        List<String> variables = asList();
        artifactReqDetails.setArtifactName("");

        if (componentInstance != null) {
            updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    component.getComponentInstances().get(0), artifactReqDetails, artifactUUID, errorInfo, variables, null, true);
        } else {
            updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    null, artifactReqDetails, artifactUUID, errorInfo, variables, null, true);

        }
    }

    // Update artifact with valid type & invalid label via external API - label to long -
//	according to the newest AID doc artifactLabel, artifactType, artifactGroupType parameters should be ignored 
    protected void updateArtifactWithInvalidLabelToLong(Component component, User sdncModifierDetails, String artifactType,
                                                        ComponentInstance componentInstance) throws Exception {

        ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", artifactType, true, true);
        String artifactUUID = getFirstArtifactUuidFromComponent(component);

        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.OK.name());
        artifactReqDetails.setArtifactLabel("invalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidType");

        if (componentInstance != null) {
            updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    component.getComponentInstances().get(0), artifactReqDetails, artifactUUID, errorInfo, null, null, true);
        } else {
            updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    null, artifactReqDetails, artifactUUID, errorInfo, null, null, true);

        }
    }

    // Update artifact with valid type & invalid label via external API - label is empty
//		according to the newest AID doc artifactLabel, artifactType, artifactGroupType parameters should be ignored 
    protected void updateArtifactWithInvalidLabelEmpty(Component component, User sdncModifierDetails, String artifactType,
                                                       ComponentInstance componentInstance) throws Exception {

        ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", artifactType, true, true);
        String artifactUUID = getFirstArtifactUuidFromComponent(component);

        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.OK.name());
        artifactReqDetails.setArtifactLabel("");

        if (componentInstance != null) {
            updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    component.getComponentInstances().get(0), artifactReqDetails, artifactUUID, errorInfo, null, null, true);
        } else {
            updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    null, artifactReqDetails, artifactUUID, errorInfo, null, null, true);

        }
    }

    // Update artifact with invalid description via external API - to long description
    protected void updateArtifactWithInvalidDescriptionToLong(Component component, User sdncModifierDetails, String artifactType,
                                                              ComponentInstance componentInstance) throws Exception {

        ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", artifactType, true, true);
        String artifactUUID = getFirstArtifactUuidFromComponent(component);

        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.EXCEEDS_LIMIT.name());
        List<String> variables = asList("artifact description", ValidationUtils.ARTIFACT_DESCRIPTION_MAX_LENGTH.toString());
        artifactReqDetails.setDescription("invalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeinvalGGfdsiofhdsouhfoidshfoidshoifhsdoifhdsouihfdsofhiufdsghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidTypeghiufghodhfioudsgafodsgaiofudsghifudsiugfhiufawsouipfhgawseiupfsadiughdfsoiuhgfaighfpasdghfdsaqgfdsgdfgidType");

        if (componentInstance != null) {
            updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    component.getComponentInstances().get(0), artifactReqDetails, artifactUUID, errorInfo, variables, null, true);
        } else {
            updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    null, artifactReqDetails, artifactUUID, errorInfo, variables, null, true);

        }
    }

    public String getFirstArtifactUuidFromComponent(Component component) {
        String artifactUUID = null;
        Map<String, ArtifactDefinition> deploymentArtifacts;
        if (component.getComponentInstances() != null) {
            deploymentArtifacts = component.getComponentInstances().get(0).getDeploymentArtifacts();
        } else {
            deploymentArtifacts = component.getDeploymentArtifacts();
        }

        for (String key : deploymentArtifacts.keySet()) {
            if (key.startsWith("ci")) {
                artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
                break;
            }
        }
        return artifactUUID;
    }

    // Update artifact with invalid description via external API - empty description
    protected void updateArtifactWithInvalidDescriptionEmpty(Component component, User sdncModifierDetails, String artifactType,
                                                             ComponentInstance componentInstance) throws Exception {

        ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", artifactType, true, true);
        String artifactUUID = getFirstArtifactUuidFromComponent(component);

        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.MISSING_DATA.name());
        List<String> variables = asList("artifact description");
        artifactReqDetails.setDescription("");

        if (componentInstance != null) {
            updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    component.getComponentInstances().get(0), artifactReqDetails, artifactUUID, errorInfo, variables, null, true);
        } else {
            updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    null, artifactReqDetails, artifactUUID, errorInfo, variables, null, true);
        }
    }

    // Unhappy flow - get chosen life cycle state, artifact type and asset type
    // update artifact via external API + check audit & response code
    // Download artifact via external API + check audit & response code
    // Check artifact version, uuid & checksusm
    protected Component updateArtifactOnAssetViaExternalAPI(Component component, ComponentTypeEnum componentTypeEnum, LifeCycleStatesEnum chosenLifeCycleState, String artifactType, ErrorInfo errorInfo, List<String> variables, UserRoleEnum userRoleEnum) throws Exception {
        String componentVersionBeforeUpdate = null;

        // get updated artifact data
        component = AtomicOperationUtils.changeComponentState(component, UserRoleEnum.DESIGNER, chosenLifeCycleState, true).getLeft();
        componentVersionBeforeUpdate = component.getVersion();

        Map<String, ArtifactDefinition> deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);
        ArtifactReqDetails artifactReqDetails = getUpdatedArtifact(deploymentArtifacts, artifactType);

        String artifactName = artifactReqDetails.getArtifactLabel();
        String artifactUUID = deploymentArtifacts.get(artifactName).getArtifactUUID();
        String artifactVersionBeforeUpdate = deploymentArtifacts.get(artifactName).getArtifactVersion();
        int numberOfArtifact = deploymentArtifacts.size();

        ArtifactReqDetails updateArtifactReqDetails = ElementFactory.getArtifactByType(ArtifactTypeEnum.SNMP_POLL.name(), ArtifactTypeEnum.SNMP_POLL.name(), true, true);
        updateArtifactReqDetails.setPayload("dGVzdA==");

        // create component/s & upload artifact via external api
        if (ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum) {
            updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(userRoleEnum),
                    component.getComponentInstances().get(0), updateArtifactReqDetails, artifactUUID, errorInfo, variables, chosenLifeCycleState, true);
        } else {
            updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(userRoleEnum),
                    null, updateArtifactReqDetails, artifactUUID, errorInfo, variables, chosenLifeCycleState, true);
        }

        if (component.getComponentType().equals(ComponentTypeEnum.SERVICE)) {
            component = AtomicOperationUtils.getServiceObjectByNameAndVersion(UserRoleEnum.DESIGNER, component.getName(), component.getVersion());
        } else {
            component = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, component.getName(), component.getVersion());
        }

        // Get list of deployment artifact + download them via external API
        if (ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum) {
            deploymentArtifacts = component.getComponentInstances().get(0).getDeploymentArtifacts();
        } else {
            deploymentArtifacts = component.getDeploymentArtifacts();
        }
        Assert.assertEquals(numberOfArtifact, deploymentArtifacts.keySet().size(), "Expected that number of deployment artifact will be same as before.");
        Assert.assertEquals(componentVersionBeforeUpdate, component.getVersion(), "Expected that check-out component will not change version number.");
        if (errorInfo.getCode().equals(200)) {
            Assert.assertNotEquals(String.valueOf((Integer.parseInt(artifactVersionBeforeUpdate))), deploymentArtifacts.get(artifactName).getArtifactVersion(), "Artifact version should be changed.");
            Assert.assertNotEquals(artifactUUID, deploymentArtifacts.get(artifactName).getArtifactUUID(), "Artifact UUID should be changed.");
        } else {
            Assert.assertEquals(String.valueOf((Integer.parseInt(artifactVersionBeforeUpdate))), deploymentArtifacts.get(artifactName).getArtifactVersion(), "Artifact version should not be changed.");
            Assert.assertEquals(artifactUUID, deploymentArtifacts.get(artifactName).getArtifactUUID(), "Artifact UUID should not be changed.");
        }
        return component;
    }

    protected RestResponse updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(Component component, User sdncModifierDetails,
                                                                                          ComponentInstance componentInstance, ArtifactReqDetails artifactReqDetails, String artifactUUID, ErrorInfo errorInfo, List<String> variables, LifeCycleStatesEnum lifeCycleStatesEnum, Boolean resourceNameInAudit) throws Exception {
        RestResponse restResponse;

        if (componentInstance != null) {
            restResponse = ArtifactRestUtils.externalAPIUpdateArtifactOfComponentInstanceOnAsset(component, sdncModifierDetails, artifactReqDetails, componentInstance, artifactUUID);
        } else {
            restResponse = ArtifactRestUtils.externalAPIUpdateArtifactOfTheAsset(component, sdncModifierDetails, artifactReqDetails, artifactUUID);

        }
        // validate response code
        Integer responseCode = restResponse.getErrorCode();
        Assert.assertEquals(responseCode, errorInfo.getCode(), "Response code is not correct.");
        component = AtomicOperationUtils.getComponentObject(component, UserRoleEnum.DESIGNER);
        return restResponse;
    }


    // This function get component, user & if updatedPayload or not
    // It will create default payload / updated payload of artifact
    // And download artifact of component which starts with ci
    protected RestResponse downloadResourceDeploymentArtifactExternalAPIAndComparePayLoadOfArtifactType(Component component, String artifactType, User sdncModifierDetails, ComponentTypeEnum componentTypeEnum) throws IOException, Exception {
        // Download the uploaded artifact via external API
        ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("abcd", artifactType, true, false);
        String artifactName = null;
        for (String key : component.getDeploymentArtifacts().keySet()) {
            if (key.startsWith("ci")) {
                artifactName = key;
                break;
            }
        }
        return downloadResourceDeploymentArtifactExternalAPI(component, component.getDeploymentArtifacts().get(artifactName), sdncModifierDetails, artifactReqDetails, componentTypeEnum);
    }

    // Get deployment artifact of RI
    protected Map<String, ArtifactDefinition> getDeploymentArtifactsOfAsset(Component component, ComponentTypeEnum componentTypeEnum) {
        Map<String, ArtifactDefinition> deploymentArtifacts = null;
        if (ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum) {
            for (ComponentInstance componentInstance : component.getComponentInstances()) {
                if (componentInstance.getNormalizedName().startsWith("ci")) {
                    deploymentArtifacts = componentInstance.getDeploymentArtifacts();
                    break;
                }
            }
        } else {
            deploymentArtifacts = component.getDeploymentArtifacts();
        }
        return deploymentArtifacts;
    }

    // get deploymentArtifact of asset and artifactType -> generate new artifact that can be updated on the asset
    protected ArtifactReqDetails getUpdatedArtifact(Map<String, ArtifactDefinition> deploymentArtifacts, String artifactType) throws IOException, Exception {
        ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", artifactType, true, true);

        for (String key : deploymentArtifacts.keySet()) {
            if (key.startsWith("ci")) {
                artifactReqDetails.setArtifactDisplayName(deploymentArtifacts.get(key).getArtifactDisplayName());
                artifactReqDetails.setArtifactName(deploymentArtifacts.get(key).getArtifactName());
                artifactReqDetails.setArtifactLabel(deploymentArtifacts.get(key).getArtifactLabel());
                break;
            }
        }
        return artifactReqDetails;
    }

    // Happy flow - get chosen life cycle state, artifact type and asset type
    // update artifact via external API + check audit & response code
    // Download artifact via external API + check audit & response code
    // Check artifact version, uuid & checksusm
    protected Component updateArtifactOnAssetViaExternalAPI(Component component, ComponentTypeEnum componentTypeEnum, LifeCycleStatesEnum chosenLifeCycleState, String artifactType) throws Exception {
        RestResponse restResponse = null;
        int numberOfArtifact = 0;
        String artifactVersionBeforeUpdate = null;
        String artifactName = null;
        String componentVersionBeforeUpdate = null;

        // get updated artifact data
        ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", artifactType, true, true);
        String artifactUUID = null;
        Map<String, ArtifactDefinition> deploymentArtifacts;
        deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);

        for (String key : deploymentArtifacts.keySet()) {
            if (key.startsWith("ci")) {
                artifactName = key;
                artifactVersionBeforeUpdate = deploymentArtifacts.get(key).getArtifactVersion();
                artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
                artifactReqDetails.setArtifactDisplayName(deploymentArtifacts.get(key).getArtifactDisplayName());
                artifactReqDetails.setArtifactName(deploymentArtifacts.get(key).getArtifactName());
                artifactReqDetails.setArtifactLabel(deploymentArtifacts.get(key).getArtifactLabel());
                break;
            }
        }

        component = AtomicOperationUtils.changeComponentState(component, UserRoleEnum.DESIGNER, chosenLifeCycleState, true).getLeft();
        componentVersionBeforeUpdate = component.getVersion();
        deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);
        numberOfArtifact = deploymentArtifacts.size();

        // create component/s & upload artifact via external api
        if (ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum) {
            restResponse = updateArtifactOfRIIncludingValiditionOfAuditAndResponseCode(component, component.getComponentInstances().get(0), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, artifactUUID, 200);
        } else {
            restResponse = updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, artifactUUID, 200);
        }

        ArtifactDefinition responseArtifact = ResponseParser.convertArtifactDefinitionResponseToJavaObject(restResponse.getResponse());
        component = getNewerVersionOfComponent(component, chosenLifeCycleState);

        // Get list of deployment artifact + download them via external API
        deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);
        Assert.assertEquals(numberOfArtifact, deploymentArtifacts.keySet().size(), "Expected that number of deployment artifact will be same as before.");
        Assert.assertEquals(String.valueOf((Integer.parseInt(artifactVersionBeforeUpdate) + 1)), deploymentArtifacts.get(artifactName).getArtifactVersion(), "Expected that aftifact version will increase by one.");

        if (chosenLifeCycleState == LifeCycleStatesEnum.CHECKOUT) {
            Assert.assertEquals(componentVersionBeforeUpdate, component.getVersion(), "Expected that check-out component will not change version number.");
        } else {
            Assert.assertEquals(String.format("%.1f", (Double.parseDouble(componentVersionBeforeUpdate) + 0.1)), component.getVersion(), "Expected that non check-out component version will increase by 0.1.");
        }

        // Download the uploaded artifact via external API
        downloadResourceDeploymentArtifactExternalAPI(component, deploymentArtifacts.get(responseArtifact.getArtifactLabel()), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, componentTypeEnum);

        return component;
    }


    // Update artifact via external API + Check auditing for upload operation + Check response of external API
    protected RestResponse updateArtifactOfRIIncludingValiditionOfAuditAndResponseCode(Component component, ComponentInstance componentInstance, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails, String artifactUUID, Integer expectedResponseCode) throws Exception {
        RestResponse restResponse = ArtifactRestUtils.externalAPIUpdateArtifactOfComponentInstanceOnAsset(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, component.getComponentInstances().get(0), artifactUUID);

        // Check response of external API
        Integer responseCode = restResponse.getErrorCode();
        Assert.assertEquals(responseCode, expectedResponseCode, "Response code is not correct.");
        return restResponse;
    }


    // Update artifact via external API + Check auditing for upload operation + Check response of external API
    protected RestResponse updateArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(Component component, User sdncModifierDetails, ArtifactReqDetails artifactReqDetails, String artifactUUID, Integer expectedResponseCode) throws Exception {
        RestResponse restResponse = ArtifactRestUtils.externalAPIUpdateArtifactOfTheAsset(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, artifactUUID);

        // Check response of external API
        Integer responseCode = restResponse.getErrorCode();
        Assert.assertEquals(responseCode, expectedResponseCode, "Response code is not correct.");
        return restResponse;
    }


    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    //					Delete External API											  //
    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////
    @DataProvider(name = "deleteArtifactForServiceViaExternalAPI", parallel = true)
    public static Object[][] dataProviderDeleteArtifactForServiceViaExternalAPI() {
        return new Object[][]{
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.YANG_XML.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VNF_CATALOG.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.YANG_XML.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VNF_CATALOG.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ArtifactTypeEnum.YANG_XML.getType()},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ArtifactTypeEnum.VNF_CATALOG.getType()},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType()},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType()},
                {LifeCycleStatesEnum.CERTIFICATIONREQUEST, ArtifactTypeEnum.OTHER.getType()},
                {LifeCycleStatesEnum.CERTIFY, ArtifactTypeEnum.YANG_XML.getType()},
                {LifeCycleStatesEnum.CERTIFY, ArtifactTypeEnum.VNF_CATALOG.getType()},
                {LifeCycleStatesEnum.CERTIFY, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType()},
                {LifeCycleStatesEnum.CERTIFY, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType()},
                {LifeCycleStatesEnum.CERTIFY, ArtifactTypeEnum.OTHER.getType()}
        };
    }

    // Delete artifact for Service - Success
    @Test(dataProvider = "deleteArtifactForServiceViaExternalAPI")
    public void deleteArtifactForServiceViaExternalAPI(LifeCycleStatesEnum lifeCycleStatesEnum, String artifactType) throws Exception {
        getExtendTest().log(Status.INFO, String.format("lifeCycleStatesEnum: %s, artifactType: %s", lifeCycleStatesEnum, artifactType));
        Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.SERVICE, LifeCycleStatesEnum.CHECKOUT, artifactType, null);
        deleteArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.SERVICE, lifeCycleStatesEnum);
    }

    @DataProvider(name = "deleteArtifactForVFViaExternalAPI", parallel = true)
    public static Object[][] dataProviderDeleteArtifactForVFViaExternalAPI() {
        return new Object[][]{
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_JSON.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_POLICY.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_EVENT.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.APPC_CONFIG.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_DOC.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_TOSCA.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.YANG_XML.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VNF_CATALOG.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VF_LICENSE.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VENDOR_LICENSE.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType()},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_JSON.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_POLICY.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_EVENT.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.APPC_CONFIG.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_DOC.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_TOSCA.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.YANG_XML.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VNF_CATALOG.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VF_LICENSE.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VENDOR_LICENSE.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType()},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
        };
    }

    // Delete artifact for VF - Success
    @Test(dataProvider = "deleteArtifactForVFViaExternalAPI")
    public void deleteArtifactForVFViaExternalAPI(LifeCycleStatesEnum lifeCycleStatesEnum, String artifactType) throws Exception {
        getExtendTest().log(Status.INFO, String.format("lifeCycleStatesEnum: %s, artifactType: %s", lifeCycleStatesEnum, artifactType));
        Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE, LifeCycleStatesEnum.CHECKOUT, artifactType, null);
        deleteArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.RESOURCE, lifeCycleStatesEnum);
    }

    @DataProvider(name = "deleteArtifactForVfcVlCpViaExternalAPI", parallel = true)
    public static Object[][] dataProviderDeleteArtifactForVfcVlCpViaExternalAPI() {
        return new Object[][]{
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.YANG_XML.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VNF_CATALOG.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VF_LICENSE.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VFC},

                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.YANG_XML.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VNF_CATALOG.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VF_LICENSE.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VL},

                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.YANG_XML.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VNF_CATALOG.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VF_LICENSE.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.CP},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.YANG_XML.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VNF_CATALOG.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VF_LICENSE.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VFC},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.YANG_XML.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VNF_CATALOG.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VF_LICENSE.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VL},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.YANG_XML.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VNF_CATALOG.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VF_LICENSE.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.VENDOR_LICENSE.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_INVENTORY_PROFILE.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.MODEL_QUERY_SPEC.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.CP},
        };
    }

    // Delete artifact for VFC, VL, CP - Success
    @Test(dataProvider = "deleteArtifactForVfcVlCpViaExternalAPI")
    public void deleteArtifactForVfcVlCpViaExternalAPI(LifeCycleStatesEnum lifeCycleStatesEnum, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
        getExtendTest().log(Status.INFO, String.format("lifeCycleStatesEnum: %s, artifactType: %s, resourceTypeEnum: %s", lifeCycleStatesEnum, artifactType, resourceTypeEnum));
        Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE, LifeCycleStatesEnum.CHECKOUT, artifactType, resourceTypeEnum);
        deleteArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.RESOURCE, lifeCycleStatesEnum);
    }

    @DataProvider(name = "deleteArtifactOnRIViaExternalAPI", parallel = true)
    public static Object[][] dataProviderDeleteArtifactOnRIViaExternalAPI() {
        return new Object[][]{
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), null},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), null},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), null},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), null},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), null},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), null},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), null},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), null},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), null},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), null},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), null},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), null},

        };
    }


    @Test(dataProvider = "deleteArtifactOnRIViaExternalAPI")
    public void deleteArtifactOnRIViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
        getExtendTest().log(Status.INFO, String.format("chosenLifeCycleState: %s, artifactType: %s", chosenLifeCycleState, artifactType));
        Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE_INSTANCE, LifeCycleStatesEnum.CHECKOUT, artifactType, resourceTypeEnum);
        deleteArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.RESOURCE_INSTANCE, chosenLifeCycleState);
    }

    @DataProvider(name = "deleteArtifactOnVfcVlCpRIViaExternalAPI", parallel = true)
    public static Object[][] dataProviderDeleteArtifactOnVfcVlCpRIViaExternalAPI() {
        return new Object[][]{
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VFC},


                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VL},

                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), ResourceTypeEnum.CP,},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.CP},


                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VFC},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VFC},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.VL},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.VL},

                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_JSON.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_POLICY.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_DOC.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_BLUEPRINT.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_EVENT.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_POLL.getType(), ResourceTypeEnum.CP},
                {LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.SNMP_TRAP.getType(), ResourceTypeEnum.CP},
        };
    }

    @Test(dataProvider = "deleteArtifactOnVfcVlCpRIViaExternalAPI")
    public void deleteArtifactOnVfcVlCpRIViaExternalAPI(LifeCycleStatesEnum chosenLifeCycleState, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
        getExtendTest().log(Status.INFO, String.format("chosenLifeCycleState: %s, artifactType: %s", chosenLifeCycleState, artifactType));
        Component component = uploadArtifactOnAssetViaExternalAPI(ComponentTypeEnum.RESOURCE_INSTANCE, LifeCycleStatesEnum.CHECKOUT, artifactType, resourceTypeEnum);
        deleteArtifactOnAssetViaExternalAPI(component, ComponentTypeEnum.RESOURCE_INSTANCE, chosenLifeCycleState);
    }

    @DataProvider(name = "deleteArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset", parallel = true)
    public static Object[][] dataProviderDeleteArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset() {
        return new Object[][]{
                {ComponentTypeEnum.RESOURCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()},

                {ComponentTypeEnum.RESOURCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()},
                {ComponentTypeEnum.RESOURCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.TESTER, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()},

                {ComponentTypeEnum.RESOURCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()},
                {ComponentTypeEnum.RESOURCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.ADMIN, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()},

                {ComponentTypeEnum.RESOURCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()},
                {ComponentTypeEnum.RESOURCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.OPS, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()},

                {ComponentTypeEnum.RESOURCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKIN, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()},
                {ComponentTypeEnum.RESOURCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.SERVICE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.OTHER.getType()},
                {ComponentTypeEnum.RESOURCE_INSTANCE, UserRoleEnum.GOVERNOR, LifeCycleStatesEnum.CHECKOUT, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType()},
        };
    }

    // External API
    // Delete artifact by different user then creator of asset - Fail
    @Test(dataProvider = "deleteArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset")
    public void deleteArtifactOnVFViaExternalAPIByDiffrentUserThenCreatorOfAsset(ComponentTypeEnum componentTypeEnum, UserRoleEnum userRoleEnum, LifeCycleStatesEnum lifeCycleStatesEnum, String artifactType) throws Exception {
        getExtendTest().log(Status.INFO, String.format("componentTypeEnum: %s, userRoleEnum %s, lifeCycleStatesEnum %s, artifactType: %s", componentTypeEnum, userRoleEnum, lifeCycleStatesEnum, artifactType));
        Component component = uploadArtifactOnAssetViaExternalAPI(componentTypeEnum, lifeCycleStatesEnum, artifactType, null);
        Map<String, ArtifactDefinition> deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);

        String artifactUUID = null;
        for (String key : deploymentArtifacts.keySet()) {
            if (key.startsWith("ci")) {
                artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
                break;
            }
        }

        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESTRICTED_OPERATION.name());
        List<String> variables = asList();

        if (componentTypeEnum.equals(ComponentTypeEnum.RESOURCE_INSTANCE)) {
            deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(userRoleEnum),
                    component.getComponentInstances().get(0), artifactUUID, errorInfo, variables, lifeCycleStatesEnum, true);
        } else {
            deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(userRoleEnum),
                    null, artifactUUID, errorInfo, variables, lifeCycleStatesEnum, true);
        }

        //TODO
//		downloadResourceDeploymentArtifactExternalAPI(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactUUID, componentTypeEnum);
    }


    @DataProvider(name = "deleteArtifactOnAssetWhichNotExist", parallel = true)
    public static Object[][] dataProviderDeleteArtifactOnAssetWhichNotExist() {
        return new Object[][]{
                {ComponentTypeEnum.SERVICE, ArtifactTypeEnum.OTHER.getType(), null},
                {ComponentTypeEnum.RESOURCE, ArtifactTypeEnum.OTHER.getType(), null},
                {ComponentTypeEnum.RESOURCE_INSTANCE, ArtifactTypeEnum.DCAE_INVENTORY_TOSCA.getType(), ResourceTypeEnum.VF},
        };
    }

    // External API
    // Upload artifact on VF via external API - happy flow
    @Test(dataProvider = "deleteArtifactOnAssetWhichNotExist")
    public void deleteArtifactOnAssetWhichNotExist(ComponentTypeEnum componentTypeEnum, String artifactType, ResourceTypeEnum resourceTypeEnum) throws Exception {
        getExtendTest().log(Status.INFO, String.format("componentTypeEnum: %s, artifactType: %s", componentTypeEnum, artifactType));
        Component component = uploadArtifactOnAssetViaExternalAPI(componentTypeEnum, LifeCycleStatesEnum.CHECKIN, artifactType, resourceTypeEnum);

        Map<String, ArtifactDefinition> deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);

        String artifactUUID = null;
        for (String key : deploymentArtifacts.keySet()) {
            if (key.startsWith("ci")) {
                artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
                break;
            }
        }

        // Invalid artifactUUID
        String invalidArtifactUUID = "12341234-1234-1234-1234-123412341234";
        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.ARTIFACT_NOT_FOUND.name());
        List<String> variables = asList(invalidArtifactUUID);

        if (componentTypeEnum.equals(ComponentTypeEnum.RESOURCE_INSTANCE)) {
            deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    component.getComponentInstances().get(0), invalidArtifactUUID, errorInfo, variables, null, true);
        } else {
            deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    null, invalidArtifactUUID, errorInfo, variables, null, true);

        }
        // Invalid componentUUID
        if (componentTypeEnum.equals(ComponentTypeEnum.RESOURCE_INSTANCE)) {
            component.getComponentInstances().get(0).setNormalizedName("invalidNormalizedName");
            errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_INSTANCE_NOT_FOUND_ON_CONTAINER.name());
            variables = asList("invalidNormalizedName", ComponentTypeEnum.RESOURCE_INSTANCE.getValue().toLowerCase(), ComponentTypeEnum.SERVICE.getValue(), component.getName());
            deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    component.getComponentInstances().get(0), artifactUUID, errorInfo, variables, LifeCycleStatesEnum.CHECKIN, true);
        } else {
            component.setUUID("invalidComponentUUID");
            if (componentTypeEnum.equals(ComponentTypeEnum.RESOURCE)) {
                errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.RESOURCE_NOT_FOUND.name());
            } else {
                errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.SERVICE_NOT_FOUND.name());
            }
            variables = asList("invalidComponentUUID");
            deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    null, artifactUUID, errorInfo, variables, LifeCycleStatesEnum.CHECKIN, false);
        }

    }

    @DataProvider(name = "deleteArtifactOnAssetWhichInInvalidStateForUploading", parallel = true)
    public static Object[][] dataProviderDeleteArtifactOnAssetWhichInInvalidStateForUploading() {
        return new Object[][]{
                {ComponentTypeEnum.SERVICE, ArtifactTypeEnum.OTHER.getType()},
        };
    }


    @Test(dataProvider = "deleteArtifactOnAssetWhichInInvalidStateForUploading")
    public void deleteArtifactOnAssetWhichInInvalidStateForUploading(ComponentTypeEnum componentTypeEnum, String artifactType) throws Exception {
        getExtendTest().log(Status.INFO, String.format("componentTypeEnum: %s, artifactType: %s", componentTypeEnum, artifactType));
        Component component = uploadArtifactOnAssetViaExternalAPI(componentTypeEnum, LifeCycleStatesEnum.CHECKOUT, artifactType, null);
        component = AtomicOperationUtils.changeComponentState(component, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.STARTCERTIFICATION, true).getLeft();

        Map<String, ArtifactDefinition> deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);
        String artifactUUID = null;
        for (String key : deploymentArtifacts.keySet()) {
            if (key.startsWith("ci")) {
                artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
                break;
            }
        }
        // Invalid artifactUUID
        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.COMPONENT_IN_CERT_IN_PROGRESS_STATE.name());
        List<String> variables = asList(component.getName(), component.getComponentType().toString().toLowerCase(), ElementFactory.getDefaultUser(UserRoleEnum.TESTER).getFirstName(),
                ElementFactory.getDefaultUser(UserRoleEnum.TESTER).getLastName(), ElementFactory.getDefaultUser(UserRoleEnum.TESTER).getUserId());

        if (componentTypeEnum.equals(ComponentTypeEnum.RESOURCE_INSTANCE)) {
            deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    component.getComponentInstances().get(0), artifactUUID, errorInfo, variables, null, true);
        } else {
            deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                    null, artifactUUID, errorInfo, variables, null, true);

        }

    }

    @DataProvider(name = "deleteArtifactOfVfcVlCpForVfciVliCpiViaExternalAPI", parallel = true)
    public static Object[][] dataProviderDeleteArtifactOfVfcVlCpForVfciVliCpiViaExternalAPI() {
        return new Object[][]{
                {ResourceTypeEnum.VFC},
                {ResourceTypeEnum.VL},
                {ResourceTypeEnum.CP}
        };
    }

    // Verify that it cannot delete VFC/VL/CP artifact on VFCi/VLi/CPi - Failure flow
    @Test(dataProvider = "deleteArtifactOfVfcVlCpForVfciVliCpiViaExternalAPI")
    public void deleteArtifactOfVfcVlCpForVfciVliCpiViaExternalAPI(ResourceTypeEnum resourceTypeEnum) throws Exception {

        getExtendTest().log(Status.INFO, String.format("resourceTypeEnum: %s", resourceTypeEnum));

        Component resourceInstanceDetails = getComponentInTargetLifeCycleState(ComponentTypeEnum.RESOURCE.getValue(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, resourceTypeEnum);
        ArtifactReqDetails artifactReqDetails = ElementFactory.getArtifactByType("ci", ArtifactTypeEnum.SNMP_TRAP.getType(), true, false);
        uploadArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(resourceInstanceDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactReqDetails, 200);
        resourceInstanceDetails = AtomicOperationUtils.changeComponentState(resourceInstanceDetails, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
        Component component = getComponentInTargetLifeCycleState(ComponentTypeEnum.RESOURCE.toString(), UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, null);
        AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceInstanceDetails, component, UserRoleEnum.DESIGNER, true).left().value();
        component = AtomicOperationUtils.getResourceObjectByNameAndVersion(UserRoleEnum.DESIGNER, component.getName(), component.getVersion());

        ErrorInfo errorInfo = ErrorValidationUtils.parseErrorConfigYaml(ActionStatus.OK.name());
        Map<String, ArtifactDefinition> deploymentArtifacts;
        deploymentArtifacts = getDeploymentArtifactsOfAsset(component, ComponentTypeEnum.RESOURCE_INSTANCE);
        String artifactUUID = null;
        for (String key : deploymentArtifacts.keySet()) {
            if (key.startsWith("ci")) {
                artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();
                break;
            }
        }
        List<String> variables = asList(artifactUUID);
        deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                component.getComponentInstances().get(0), artifactUUID, errorInfo, variables, null, true);
    }

    protected RestResponse deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(Component component, User sdncModifierDetails,
                                                                                          ComponentInstance componentInstance, String artifactUUID, ErrorInfo errorInfo, List<String> variables, LifeCycleStatesEnum lifeCycleStatesEnum, Boolean resourceNameInAudit) throws Exception {
        RestResponse restResponse;

        if (componentInstance != null) {
            restResponse = ArtifactRestUtils.externalAPIDeleteArtifactOfComponentInstanceOnAsset(component, sdncModifierDetails, componentInstance, artifactUUID);
        } else {
            restResponse = ArtifactRestUtils.externalAPIDeleteArtifactOfTheAsset(component, sdncModifierDetails, artifactUUID);

        }

        // validate response code
        Integer responseCode = restResponse.getErrorCode();
        Assert.assertEquals(responseCode, errorInfo.getCode(), "Response code is not correct.");
        return restResponse;

    }


    // Happy flow - get chosen life cycle state, artifact type and asset type
    // delete artifact via external API + check audit & response code
    protected Component deleteArtifactOnAssetViaExternalAPI(Component component, ComponentTypeEnum componentTypeEnum, LifeCycleStatesEnum chosenLifeCycleState) throws Exception {
        String artifactName = null;
        component = AtomicOperationUtils.changeComponentState(component, UserRoleEnum.DESIGNER, chosenLifeCycleState, true).getLeft();
        if (!LifeCycleStatesEnum.CHECKOUT.equals(chosenLifeCycleState)) {
            component = AtomicOperationUtils.getComponentObject(component, UserRoleEnum.DESIGNER);
        } else {
            component = getNewerVersionOfComponent(component, chosenLifeCycleState);
        }
        // get updated artifact data
        String artifactUUID = null;
        int moduleTypeArtifact = 0;
        Map<String, ArtifactDefinition> deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);

        for (String key : deploymentArtifacts.keySet()) {
            if (key.startsWith("ci") && StringUtils.isNotEmpty(deploymentArtifacts.get(key).getArtifactUUID())) {
                artifactName = key;
                artifactUUID = deploymentArtifacts.get(key).getArtifactUUID();

                if (deploymentArtifacts.get(key).getArtifactType().equals(ArtifactTypeEnum.VF_MODULES_METADATA)) {
                    moduleTypeArtifact = 1;
                }
                break;
            }
        }

        String componentVersionBeforeDelete = component.getVersion();
        int numberOfArtifact = deploymentArtifacts.size();

        // create component/s & upload artifact via external api
        if (ComponentTypeEnum.RESOURCE_INSTANCE == componentTypeEnum) {
            deleteArtifactOfRIIncludingValiditionOfAuditAndResponseCode(component, component.getComponentInstances().get(0), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactUUID, 200);
        } else {
            deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactUUID, 200);
        }

        component = updateComponentDetailsByLifeCycleState(chosenLifeCycleState, component);

        // Get list of deployment artifact + download them via external API
        deploymentArtifacts = getDeploymentArtifactsOfAsset(component, componentTypeEnum);
        if (deploymentArtifacts.get(artifactName) != null) {
            Assert.assertTrue(false, "Expected that deleted artifact will not appear in deployment artifact list.");
        }
        if ((LifeCycleStatesEnum.CERTIFICATIONREQUEST.equals(chosenLifeCycleState)) && (ComponentTypeEnum.RESOURCE_INSTANCE.equals(componentTypeEnum)) && (!component.getComponentType().toString().equals(ComponentTypeEnum.RESOURCE.toString()))) {
            Assert.assertEquals(numberOfArtifact - 1 - moduleTypeArtifact, deploymentArtifacts.keySet().size(), "Expected that number of deployment artifact (one deleted and one vfmodule) will decrease by two.");
        } else {
            Assert.assertEquals(numberOfArtifact - 1, deploymentArtifacts.keySet().size(), "Expected that number of deployment artifact will decrease by one.");
        }

        if (chosenLifeCycleState == LifeCycleStatesEnum.CHECKOUT) {
            Assert.assertEquals(componentVersionBeforeDelete, component.getVersion(), "Expected that check-out component will not change version number.");
        } else {
            Assert.assertEquals(String.format("%.1f", (Double.parseDouble(componentVersionBeforeDelete) + 0.1)), component.getVersion(), "Expected that non check-out component version will increase by 0.1.");
        }

        downloadResourceDeploymentArtifactExternalAPI(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactUUID, componentTypeEnum);

        return component;
    }

    // Delete artifact via external API + Check auditing for upload operation + Check response of external API
    protected RestResponse deleteArtifactOfRIIncludingValiditionOfAuditAndResponseCode(Component component, ComponentInstance componentInstance, User sdncModifierDetails, String artifactUUID, Integer expectedResponseCode) throws Exception {
        RestResponse restResponse = ArtifactRestUtils.externalAPIDeleteArtifactOfComponentInstanceOnAsset(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), component.getComponentInstances().get(0), artifactUUID);

        // Check response of external API
        Integer responseCode = restResponse.getErrorCode();
        Assert.assertEquals(responseCode, expectedResponseCode, "Response code is not correct.");
//		component = AtomicOperationUtils.getComponentObject(component, UserRoleEnum.DESIGNER);
        return restResponse;
    }


    // Delete artifact via external API + Check auditing for upload operation + Check response of external API
    protected RestResponse deleteArtifactOfAssetIncludingValiditionOfAuditAndResponseCode(Component component, User sdncModifierDetails, String artifactUUID, Integer expectedResponseCode) throws Exception {
        RestResponse restResponse = ArtifactRestUtils.externalAPIDeleteArtifactOfTheAsset(component, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), artifactUUID);

        // Check response of external API
        Integer responseCode = restResponse.getErrorCode();
        Assert.assertEquals(responseCode, expectedResponseCode, "Response code is not correct.");
//		component = AtomicOperationUtils.getComponentObject(component, UserRoleEnum.DESIGNER);
        return restResponse;
    }


    // download deployment via external api + check response code for success (200) + get artifactReqDetails and verify payload + verify audit
    protected RestResponse downloadResourceDeploymentArtifactExternalAPI(Component component, User sdncModifierDetails, String artifactUUID, ComponentTypeEnum componentTypeEnum) throws Exception {
        RestResponse restResponse;

        if (componentTypeEnum == ComponentTypeEnum.RESOURCE_INSTANCE) {
            restResponse = ArtifactRestUtils.getComponentInstanceDeploymentArtifactExternalAPI(component.getUUID(), component.getComponentInstances().get(0).getNormalizedName(), artifactUUID, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), component.getComponentType().toString());
        } else {
            restResponse = ArtifactRestUtils.getResourceDeploymentArtifactExternalAPI(component.getUUID(), artifactUUID, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), component.getComponentType().toString());
        }

        Integer responseCode = restResponse.getErrorCode();
        Integer expectedCode = 404;
        Assert.assertEquals(responseCode, expectedCode, "Response code is not correct.");

        return restResponse;

    }


    public Component getComponentInTargetLifeCycleState(String componentType, UserRoleEnum creatorUser, LifeCycleStatesEnum targetLifeCycleState, ResourceTypeEnum resourceTypeEnum) throws Exception {
        Component resourceDetails = null;

        if ((componentType.toLowerCase().equals("vf")) || (componentType.toLowerCase().equals("resource"))) {
            if (resourceTypeEnum == null) {
                resourceTypeEnum = ResourceTypeEnum.VF;
            }
            Either<Resource, RestResponse> createdResource = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(resourceTypeEnum, NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, creatorUser, true);
            resourceDetails = createdResource.left().value();
            resourceDetails = AtomicOperationUtils.changeComponentState(resourceDetails, creatorUser, targetLifeCycleState, true).getLeft();
        } else {
            Either<Service, RestResponse> createdResource = AtomicOperationUtils.createDefaultService(creatorUser, true);
            resourceDetails = createdResource.left().value();
            // Add artifact to service if asked for certifcationrequest - must be at least one artifact for the flow
            if ((LifeCycleStatesEnum.CERTIFICATIONREQUEST == targetLifeCycleState) || (LifeCycleStatesEnum.STARTCERTIFICATION == targetLifeCycleState)) {
                AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.OTHER, resourceDetails, UserRoleEnum.DESIGNER, true, true).left().value();
            }
            resourceDetails = AtomicOperationUtils.changeComponentState(resourceDetails, creatorUser, targetLifeCycleState, true).getLeft();
        }
        return resourceDetails;
    }

}
