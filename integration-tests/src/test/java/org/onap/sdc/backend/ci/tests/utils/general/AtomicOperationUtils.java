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

package org.onap.sdc.backend.ci.tests.utils.general;

import static org.junit.Assert.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import com.aventstack.extentreports.Status;
import com.google.gson.Gson;
import fj.data.Either;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.onap.sdc.backend.ci.tests.api.ComponentBaseTest;
import org.onap.sdc.backend.ci.tests.api.ExtentTestActions;
import org.onap.sdc.backend.ci.tests.api.Urls;
import org.onap.sdc.backend.ci.tests.config.Config;
import org.onap.sdc.backend.ci.tests.datatypes.ArtifactReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.DistributionMonitorObject;
import org.onap.sdc.backend.ci.tests.datatypes.ImportReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ProductReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.PropertyReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceDistributionStatus;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.PropertyTypeEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.HttpHeaderEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.HttpRequest;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.execute.lifecycle.LCSbaseTest;
import org.onap.sdc.backend.ci.tests.tosca.datatypes.ToscaDefinition;
import org.onap.sdc.backend.ci.tests.utils.CsarToscaTester;
import org.onap.sdc.backend.ci.tests.utils.DistributionUtils;
import org.onap.sdc.backend.ci.tests.utils.ToscaParserUtils;
import org.onap.sdc.backend.ci.tests.utils.Utils;
import org.onap.sdc.backend.ci.tests.utils.rest.ArtifactRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.AssetRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.BaseRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ConsumerRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.LifecycleRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ProductRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.PropertyRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ResourceRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ResponseParser;
import org.onap.sdc.backend.ci.tests.utils.rest.ServiceRestUtils;
import org.onap.sdc.tosca.parser.api.ISdcCsarHelper;
import org.onap.sdc.tosca.parser.impl.SdcToscaParserFactory;
import org.openecomp.sdc.be.datatypes.elements.ConsumerDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.AssetTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.GroupDefinition;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.testng.SkipException;

public final class AtomicOperationUtils {

    static final String basicAuthentication = "Basic Y2k6MTIzNDU2";

    private AtomicOperationUtils() {
        throw new UnsupportedOperationException();
    }

    // *********** RESOURCE ****************

    /**
     * Import a vfc From tosca file
     *
     * @param filePath
     * @param fileName
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public static Either<Resource, RestResponse> importResource(String filePath, String fileName) {
        try {
            User designer = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
            ImportReqDetails importReqDetails = ElementFactory.getDefaultImportResource(ElementFactory.getResourcePrefix());
            importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, filePath, fileName);
            RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, designer, null);
            return buildResourceFromResponse(importResourceResponse);
        } catch (Exception e) {
            throw new AtomicOperationException(e);
        }
    }

    public static Either<Resource, RestResponse> importResource(ImportReqDetails importReqDetails, String filePath, String fileName, User userRole,
                                                                Boolean validateState) {
        try {
            importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, filePath, fileName);
            RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, userRole, null);

            if (validateState) {
                assertTrue("Import resource failed with error: " + importResourceResponse.getResponse(),
                    importResourceResponse.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED);
            }

            if (importResourceResponse.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
                Resource resourceResponseObject = ResponseParser.convertResourceResponseToJavaObject(importResourceResponse.getResponse());
                return Either.left(resourceResponseObject);
            }
            return Either.right(importResourceResponse);
        } catch (Exception e) {
            throw new AtomicOperationException(e);
        }
    }


    public static Either<Resource, RestResponse> createResourceByType(ResourceTypeEnum resourceType, UserRoleEnum userRole, Boolean validateState) {
        try {
            User defaultUser = ElementFactory.getDefaultUser(userRole);
            ResourceReqDetails defaultResource = ElementFactory.getDefaultResourceByType(resourceType, defaultUser);
            RestResponse resourceResp = ResourceRestUtils.createResource(defaultResource, defaultUser);

            if (validateState) {
                assertTrue("Create resource failed with error: " + resourceResp.getResponse(),
                    resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED);
            }

            if (resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
                Resource resourceResponseObject = ResponseParser.convertResourceResponseToJavaObject(resourceResp.getResponse());
                return Either.left(resourceResponseObject);
            }
            return Either.right(resourceResp);
        } catch (Exception e) {
            throw new AtomicOperationException(e);
        }
    }

    public static Either<Resource, RestResponse> createResourceByResourceDetails(final ResourceReqDetails resourceDetails,
                                                                                 final UserRoleEnum userRole,
                                                                                 final Boolean validateState) {
        try {
            User defaultUser = ElementFactory.getDefaultUser(userRole);
            RestResponse resourceResp = ResourceRestUtils.createResource(resourceDetails, defaultUser);

            if (validateState) {
                assertEquals("Create resource failed with error: " + resourceResp.getResponse(),
                    ResourceRestUtils.STATUS_CODE_CREATED, (int) resourceResp.getErrorCode());
            }

            if (resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
                Resource resourceResponseObject = ResponseParser.convertResourceResponseToJavaObject(resourceResp.getResponse());
                return Either.left(resourceResponseObject);
            }
            return Either.right(resourceResp);
        } catch (Exception e) {
            throw new AtomicOperationException(e);
        }
    }

    public static Either<Resource, RestResponse> createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum resourceType,
                                                                                           NormativeTypesEnum normativeTypes,
                                                                                           ResourceCategoryEnum resourceCategory,
                                                                                           UserRoleEnum userRole, Boolean validateState)
        throws Exception {
        User defaultUser = ElementFactory.getDefaultUser(userRole);
        ResourceReqDetails defaultResource = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(resourceType, normativeTypes,
            resourceCategory, defaultUser);
        RestResponse resourceResp = ResourceRestUtils.createResource(defaultResource, defaultUser);

        if (validateState) {
            assertTrue("Actual Response Code is: " + resourceResp.getErrorCode(),
                resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED);
        }

        if (resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
            // Resource resourceResponseObject = ResponseParser
            // .convertResourceResponseToJavaObject(resourceResp.getResponse());
            Resource resourceResponseObject = ResponseParser.parseToObjectUsingMapper(resourceResp.getResponse(), Resource.class);
            return Either.left(resourceResponseObject);
        }
        return Either.right(resourceResp);
    }

    public static Either<Resource, RestResponse> createResourcesByCustomNormativeTypeAndCatregory(ResourceTypeEnum resourceType,
                                                                                                  Resource resourceNormativeType,
                                                                                                  ResourceCategoryEnum resourceCategory,
                                                                                                  UserRoleEnum userRole, Boolean validateState)
        throws Exception {
        User defaultUser = ElementFactory.getDefaultUser(userRole);
        ResourceReqDetails defaultResource = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(resourceType, resourceNormativeType,
            resourceCategory, defaultUser);
        RestResponse resourceResp = ResourceRestUtils.createResource(defaultResource, defaultUser);

        if (validateState) {
            assertTrue("Create resource failed with error: " + resourceResp.getResponse(),
                resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED);
        }

        if (resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
            // Resource resourceResponseObject = ResponseParser
            // .convertResourceResponseToJavaObject(resourceResp.getResponse());
            Resource resourceResponseObject = ResponseParser.parseToObjectUsingMapper(resourceResp.getResponse(), Resource.class);
            return Either.left(resourceResponseObject);
        }
        return Either.right(resourceResp);
    }

    public static Either<Resource, RestResponse> updateResource(ResourceReqDetails resourceReqDetails, User defaultUser, Boolean validateState) {
        try {

            RestResponse resourceResp = ResourceRestUtils.updateResource(resourceReqDetails, defaultUser, resourceReqDetails.getUniqueId());

            if (validateState) {
                assertTrue("Update resource failed with error: " + resourceResp.getResponse(),
                    resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_SUCCESS);
            }

            if (resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_SUCCESS) {
                Resource resourceResponseObject = ResponseParser.convertResourceResponseToJavaObject(resourceResp.getResponse());
                return Either.left(resourceResponseObject);
            }
            return Either.right(resourceResp);
        } catch (Exception e) {
            throw new AtomicOperationException(e);
        }
    }

    // *********** SERVICE ****************

    public static Either<Service, RestResponse> createDefaultService(UserRoleEnum userRole, Boolean validateState) throws Exception {
        User defaultUser = ElementFactory.getDefaultUser(userRole);
        ServiceReqDetails serviceDetails = ElementFactory.getDefaultService(defaultUser);
        RestResponse createServiceResp = ServiceRestUtils.createService(serviceDetails, defaultUser);

        if (validateState) {
            assertTrue("Create service failed with error: " + createServiceResp.getResponse(),
                createServiceResp.getErrorCode() == ServiceRestUtils.STATUS_CODE_CREATED);
        }

        if (createServiceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
            Service serviceResponseObject = ResponseParser.convertServiceResponseToJavaObject(createServiceResp.getResponse());
            return Either.left(serviceResponseObject);
        }
        return Either.right(createServiceResp);
    }

    public static Either<Service, RestResponse> createServiceByCategory(ServiceCategoriesEnum category, UserRoleEnum userRole, Boolean validateState)
        throws Exception {
        User defaultUser = ElementFactory.getDefaultUser(userRole);
        ServiceReqDetails serviceDetails = ElementFactory.getDefaultService(category, defaultUser);
        RestResponse createServiceResp = ServiceRestUtils.createService(serviceDetails, defaultUser);

        if (validateState) {
            assertTrue("Create service failed with error: " + createServiceResp.getResponse(),
                createServiceResp.getErrorCode() == ServiceRestUtils.STATUS_CODE_CREATED);
        }

        if (createServiceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
            Service serviceResponseObject = ResponseParser.convertServiceResponseToJavaObject(createServiceResp.getResponse());
            return Either.left(serviceResponseObject);
        }
        return Either.right(createServiceResp);
    }

    public static Either<Service, RestResponse> createCustomService(ServiceReqDetails serviceDetails, UserRoleEnum userRole, Boolean validateState)
        throws Exception {
        User defaultUser = ElementFactory.getDefaultUser(userRole);
        RestResponse createServiceResp = ServiceRestUtils.createService(serviceDetails, defaultUser);

        if (validateState) {
            assertTrue("Create service failed with error: " + createServiceResp.getResponse(),
                createServiceResp.getErrorCode() == ServiceRestUtils.STATUS_CODE_CREATED);
        }

        if (createServiceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
            Service serviceResponseObject = ResponseParser.convertServiceResponseToJavaObject(createServiceResp.getResponse());
            return Either.left(serviceResponseObject);
        }
        return Either.right(createServiceResp);
    }
    // *********** PRODUCT ****************

    public static Either<Product, RestResponse> createDefaultProduct(UserRoleEnum userRole, Boolean validateState) throws Exception {
        User defaultUser = ElementFactory.getDefaultUser(userRole);
        ProductReqDetails defaultProduct = ElementFactory.getDefaultProduct();
        RestResponse createProductResp = ProductRestUtils.createProduct(defaultProduct, defaultUser);

        if (validateState) {
            assertTrue(createProductResp.getErrorCode() == ProductRestUtils.STATUS_CODE_CREATED);
        }

        if (createProductResp.getErrorCode() == ProductRestUtils.STATUS_CODE_CREATED) {
            Product productResponseJavaObject = ResponseParser.convertProductResponseToJavaObject(createProductResp.getResponse());
            return Either.left(productResponseJavaObject);
        }
        return Either.right(createProductResp);
    }

    // public static ComponentReqDetails
    // convertCompoentToComponentReqDetails(Component component){
    //
    // ComponentReqDetails componentReqDetails =
    // ElementFactory.getDefaultService();
    // componentReqDetails.setName(component.getName());
    // componentReqDetails.setDescription(component.getDescription());
    // componentReqDetails.setTags(component.getTags());
    // componentReqDetails.setContactId(component.getContactId());
    // componentReqDetails.setIcon(component.getIcon());
    // componentReqDetails.setUniqueId(component.getUniqueId());
    // componentReqDetails.setCreatorUserId(component.getCreatorUserId());
    // componentReqDetails.setCreatorFullName(component.getCreatorFullName());
    // componentReqDetails.setLastUpdaterUserId(component.getLastUpdaterUserId());
    // componentReqDetails.setLastUpdaterFullName(component.getLastUpdaterFullName());
    // componentReqDetails.setCreationDate(component.getCreationDate());
    // componentReqDetails.setLastUpdateDate(component.getLastUpdateDate());
    // componentReqDetails.setLifecycleState(component.getLifecycleState());
    // componentReqDetails.setVersion(component.getVersion());
    // componentReqDetails.setUuid(component.getUUID());
    // componentReqDetails.setCategories(component.getCategories());
    // componentReqDetails.setProjectCode(component.getProjectCode());
    //
    // return componentReqDetails;
    // }

    // *********** LIFECYCLE ***************

    public static Pair<Component, RestResponse> changeComponentState(Component component, UserRoleEnum userRole, LifeCycleStatesEnum targetState,
                                                                     Boolean validateState) throws Exception {

        Boolean isValidationFailed = false;
        RestResponse lifeCycleStatesResponse = null;
        User defaultUser;

        LifeCycleStatesEnum currentCompState = LifeCycleStatesEnum.findByCompState(component.getLifecycleState().toString());

        if (currentCompState == targetState) {
            component = getComponentObject(component, userRole);
            return Pair.of(component, null);
        }
        String componentType = component.getComponentType().getValue();
        ArrayList<String> lifeCycleStatesEnumList = new ArrayList<>();
        if (currentCompState.equals(LifeCycleStatesEnum.CHECKIN) && targetState.equals(LifeCycleStatesEnum.CHECKOUT)) {
            lifeCycleStatesEnumList.add(LifeCycleStatesEnum.CHECKIN.toString());
            lifeCycleStatesEnumList.add(LifeCycleStatesEnum.CHECKOUT.toString());
        } else {
            lifeCycleStatesEnumList.add(LifeCycleStatesEnum.CHECKOUT.toString());
            lifeCycleStatesEnumList.add(LifeCycleStatesEnum.CHECKIN.toString());
            lifeCycleStatesEnumList.add(LifeCycleStatesEnum.CERTIFY.toString());
        }
        for (int i = 0; i < lifeCycleStatesEnumList.size(); i++) {
            if (lifeCycleStatesEnumList.get(i).equals(currentCompState.name())) {
                int a;
                a = (i == lifeCycleStatesEnumList.size() - 1) ? 0 : i + 1;
                for (int n = a; n < lifeCycleStatesEnumList.size(); n++) {
                    defaultUser = ElementFactory.getDefaultUser(userRole);
                    lifeCycleStatesResponse = LifecycleRestUtils.changeComponentState(component, defaultUser,
                        LifeCycleStatesEnum.findByState(lifeCycleStatesEnumList.get(n)));
                    if (lifeCycleStatesResponse.getErrorCode() != LifecycleRestUtils.STATUS_CODE_SUCCESS) {
                        isValidationFailed = true;
                    }
                    if (lifeCycleStatesEnumList.get(n).equals(targetState.toString()) || isValidationFailed) {
                        break;
                    }
                }
            }
        }
        Component componentJavaObject = getComponentObject(component, userRole);

        if (validateState && isValidationFailed) {
            assertTrue("change state to [" + targetState.getState() + "] failed" + lifeCycleStatesResponse.getResponse(), false);
            return Pair.of(componentJavaObject, lifeCycleStatesResponse);
        }

        if (isValidationFailed) {
            return Pair.of(componentJavaObject, lifeCycleStatesResponse);
        }

        return Pair.of(componentJavaObject, lifeCycleStatesResponse);
    }

    public static RestResponse distributeService(Component component, Boolean validateState) throws Exception {

        Service service = (Service) component;

        User opsUser = ElementFactory.getDefaultUser(UserRoleEnum.OPS);
        User governotUser = ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR);

        ServiceReqDetails serviceDetails = new ServiceReqDetails(service);
        RestResponse distributionService = null;

        RestResponse approveDistribution = LifecycleRestUtils.changeDistributionStatus(serviceDetails, null, governotUser, "approveService",
            DistributionStatusEnum.DISTRIBUTED);
        if (approveDistribution.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS) {
            distributionService = LifecycleRestUtils.changeDistributionStatus(serviceDetails, null, opsUser, "approveService",
                DistributionStatusEnum.DISTRIBUTED);
        }

        if (validateState) {
            assertTrue("Distribution approve failed with error: " + approveDistribution.getResponse(),
                approveDistribution.getErrorCode() == ProductRestUtils.STATUS_CODE_SUCCESS);
            assertTrue("Distribute service failed with error: " + distributionService.getResponse(),
                distributionService.getErrorCode() == ProductRestUtils.STATUS_CODE_SUCCESS);
            return distributionService;
        }

        return distributionService;
    }

    public static void toscaValidation(Component component, String vnfFile) throws Exception {

        ISdcCsarHelper fdntCsarHelper;
        SdcToscaParserFactory factory = SdcToscaParserFactory.getInstance();
        File csarFile = AssetRestUtils.getToscaModelCsarFile(AssetTypeEnum.SERVICES, component.getUUID(), vnfFile);
        ExtentTestActions.log(Status.INFO, "Tosca parser is going to convert service csar file to ISdcCsarHelper object...");
        fdntCsarHelper = factory.getSdcCsarHelper(csarFile.getAbsolutePath());
        CsarToscaTester.processCsar(fdntCsarHelper);
        ExtentTestActions.log(Status.INFO, String.format("Tosca parser successfully parsed service CSAR"));

    }

    // *********** ARTIFACTS *****************

    public static Either<ArtifactDefinition, RestResponse> uploadArtifactByType(ArtifactTypeEnum artifactType, Component component,
                                                                                UserRoleEnum userRole, Boolean deploymentTrue, Boolean validateState)
        throws Exception {

        User defaultUser = ElementFactory.getDefaultUser(userRole);
        ArtifactReqDetails artifactDetails = ElementFactory.getArtifactByType(null, artifactType, deploymentTrue);
        if (!deploymentTrue) {
            artifactDetails.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL.getType());
        }
        RestResponse uploadArtifactResp = ArtifactRestUtils.uploadArtifact(artifactDetails, component, defaultUser);

        if (validateState) {
            assertTrue("artifact upload failed: " + artifactDetails.getArtifactName(),
                uploadArtifactResp.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
        }

        if (uploadArtifactResp.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS) {
            ArtifactDefinition artifactJavaObject = ResponseParser.convertArtifactDefinitionResponseToJavaObject(uploadArtifactResp.getResponse());
            return Either.left(artifactJavaObject);
        }
        return Either.right(uploadArtifactResp);
    }

    // *********** CONTAINERS *****************

    /**
     * Adds Component instance to Component
     *
     * @param compInstParent
     * @param compContainer
     * @return
     */
    public static Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer(Component compInstParent,
                                                                                                   Component compContainer) {
        return addComponentInstanceToComponentContainer(compInstParent, compContainer, UserRoleEnum.DESIGNER, false);
    }

    public static Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer(Component compInstParent,
                                                                                                   Component compContainer,
                                                                                                   UserRoleEnum userRole,
                                                                                                   Boolean validateState) {
        try {
            User defaultUser = ElementFactory.getDefaultUser(userRole);
            ComponentInstanceReqDetails componentInstanceDetails = ElementFactory.getComponentInstance(compInstParent);
            if (componentInstanceDetails.getOriginType() == null) {
                componentInstanceDetails.setOriginType(((Resource) compInstParent).getResourceType().toString());
            }
            RestResponse createComponentInstance = ComponentInstanceRestUtils.createComponentInstance(componentInstanceDetails,
                defaultUser, compContainer);

            if (validateState) {
                if (createComponentInstance.getErrorCode() == ServiceRestUtils.STATUS_CODE_NOT_FOUND) {
                    throw new SkipException("Open bug DE262001");
                } else {
                    assertTrue("error - " + createComponentInstance.getErrorCode() + "instead - " +
                            ServiceRestUtils.STATUS_CODE_CREATED,
                        createComponentInstance.getErrorCode() == ServiceRestUtils.STATUS_CODE_CREATED);
                }
            }

            if (createComponentInstance.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
                ComponentInstance componentInstance = ResponseParser
                    .convertComponentInstanceResponseToJavaObject(createComponentInstance.getResponse());
                return Either.left(componentInstance);
            }
            return Either.right(createComponentInstance);
        } catch (Exception e) {
            throw new AtomicOperationException(e);
        }
    }

    public static Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer(Component compInstParent, Component compContainer,
                                                                                                   UserRoleEnum userRole, Boolean validateState,
                                                                                                   String positionX, String positionY) {
        try {
            User defaultUser = ElementFactory.getDefaultUser(userRole);
            ComponentInstanceReqDetails componentInstanceDetails = ElementFactory.getComponentInstance(compInstParent);
            componentInstanceDetails.setPosX(positionX);
            componentInstanceDetails.setPosY(positionY);
            if (componentInstanceDetails.getOriginType() == null) {
                componentInstanceDetails.setOriginType(((Resource) compInstParent).getResourceType().toString());
            }
            RestResponse createComponentInstance = ComponentInstanceRestUtils.createComponentInstance(componentInstanceDetails, defaultUser,
                compContainer);

            if (validateState) {
                if (createComponentInstance.getErrorCode() == ServiceRestUtils.STATUS_CODE_NOT_FOUND) {
                    throw new SkipException("Open bug DE262001");
                } else {
                    assertTrue("error - " + createComponentInstance.getErrorCode() + "instead - " + ServiceRestUtils.STATUS_CODE_CREATED,
                        createComponentInstance.getErrorCode() == ServiceRestUtils.STATUS_CODE_CREATED);
                }
            }

            if (createComponentInstance.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
                ComponentInstance componentInstance = ResponseParser.convertComponentInstanceResponseToJavaObject(
                    createComponentInstance.getResponse());
                return Either.left(componentInstance);
            }
            return Either.right(createComponentInstance);
        } catch (Exception e) {
            throw new AtomicOperationException(e);
        }
    }

    public static Resource getResourceObject(Component containerDetails, UserRoleEnum userRole) throws Exception {
        // User defaultUser = ElementFactory.getDefaultUser(userRole);
        RestResponse restResponse = ResourceRestUtils.getResource(containerDetails.getUniqueId());
        return ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
    }

    public static Resource getResourceObject(String uniqueId) throws Exception {
        RestResponse restResponse = ResourceRestUtils.getResource(uniqueId);
        return ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
    }

    public static Resource getResourceObjectByNameAndVersion(UserRoleEnum sdncModifierDetails, String resourceName, String resourceVersion)
        throws Exception {
        User defaultUser = ElementFactory.getDefaultUser(sdncModifierDetails);
        RestResponse resourceResponse = ResourceRestUtils.getResourceByNameAndVersion(defaultUser.getUserId(), resourceName, resourceVersion);
        return ResponseParser.convertResourceResponseToJavaObject(resourceResponse.getResponse());
    }

    public static Service getServiceObject(Component containerDetails, UserRoleEnum userRole) throws Exception {
        User defaultUser = ElementFactory.getDefaultUser(userRole);
        RestResponse serviceResponse = ServiceRestUtils.getService(containerDetails.getUniqueId(), defaultUser);
        return ResponseParser.convertServiceResponseToJavaObject(serviceResponse.getResponse());
    }

    public static Service getServiceObjectByNameAndVersion(UserRoleEnum sdncModifierDetails, String serviceName, String serviceVersion)
        throws Exception {
        User defaultUser = ElementFactory.getDefaultUser(sdncModifierDetails);
        RestResponse serviceResponse = ServiceRestUtils.getServiceByNameAndVersion(defaultUser, serviceName, serviceVersion);
        return ResponseParser.convertServiceResponseToJavaObject(serviceResponse.getResponse());
    }

    public static Service getServiceObject(String uniqueId) throws Exception {
        RestResponse serviceResponse = ServiceRestUtils.getService(uniqueId);
        return ResponseParser.convertServiceResponseToJavaObject(serviceResponse.getResponse());
    }

    public static Product getProductObject(Component containerDetails, UserRoleEnum userRole) throws Exception {
        User defaultUser = ElementFactory.getDefaultUser(userRole);
        RestResponse productRest = ProductRestUtils.getProduct(containerDetails.getUniqueId(), defaultUser.getUserId());
        return ResponseParser.convertProductResponseToJavaObject(productRest.getResponse());
    }

    public static Component getComponentObject(Component containerDetails, UserRoleEnum userRole) throws Exception {
        User defaultUser = ElementFactory.getDefaultUser(userRole);

        switch (containerDetails.getComponentType()) {
            case RESOURCE:
                RestResponse restResponse = ResourceRestUtils.getResource(containerDetails.getUniqueId());
                containerDetails = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
                break;
            case SERVICE:
                RestResponse serviceResponse = ServiceRestUtils.getService(containerDetails.getUniqueId(), defaultUser);
                containerDetails = ResponseParser.convertServiceResponseToJavaObject(serviceResponse.getResponse());
                break;
            case PRODUCT:
                RestResponse productRest = ProductRestUtils.getProduct(containerDetails.getUniqueId(), defaultUser.getUserId());
                containerDetails = ResponseParser.convertProductResponseToJavaObject(productRest.getResponse());
                break;
            default:
                break;
        }
        return containerDetails;
    }

    public static Component convertReposnseToComponentObject(Component containerDetails, RestResponse restresponse) {

        switch (containerDetails.getComponentType()) {
            case RESOURCE:
                containerDetails = ResponseParser.convertResourceResponseToJavaObject(restresponse.getResponse());
                break;
            case SERVICE:
                containerDetails = ResponseParser.convertServiceResponseToJavaObject(restresponse.getResponse());
                break;
            case PRODUCT:
                containerDetails = ResponseParser.convertProductResponseToJavaObject(restresponse.getResponse());
                break;
            default:
                break;
        }
        return containerDetails;
    }

    public static Either<Component, RestResponse> associate2ResourceInstances(Component containerDetails, ComponentInstance fromNode,
                                                                              ComponentInstance toNode, String assocType, UserRoleEnum userRole,
                                                                              Boolean validateState) throws Exception {

        User defaultUser = ElementFactory.getDefaultUser(userRole);
        RestResponse associate2ResourceInstancesResponse = ResourceRestUtils.associate2ResourceInstances(containerDetails, fromNode, toNode,
            assocType, defaultUser);

        if (validateState) {
            assertTrue(associate2ResourceInstancesResponse.getErrorCode() == ServiceRestUtils.STATUS_CODE_SUCCESS);
        }

        if (associate2ResourceInstancesResponse.getErrorCode() == ResourceRestUtils.STATUS_CODE_SUCCESS) {

            switch (containerDetails.getComponentType()) {
                case RESOURCE:
                    containerDetails = ResponseParser.convertResourceResponseToJavaObject(associate2ResourceInstancesResponse.getResponse());
                    break;
                case SERVICE:
                    containerDetails = ResponseParser.convertServiceResponseToJavaObject(associate2ResourceInstancesResponse.getResponse());
                    break;
                case PRODUCT:
                    containerDetails = ResponseParser.convertProductResponseToJavaObject(associate2ResourceInstancesResponse.getResponse());
                    break;
                default:
                    break;
            }

            return Either.left(containerDetails);
        }
        return Either.right(associate2ResourceInstancesResponse);

    }

    public static Either<Pair<Component, ComponentInstance>, RestResponse> updateComponentInstance(
        ComponentInstanceReqDetails componentInstanceReqDetails, User sdncModifierDetails, Component container, boolean validateState)
        throws Exception {

        RestResponse updateComponentInstance = ComponentInstanceRestUtils.updateComponentInstance(componentInstanceReqDetails, sdncModifierDetails,
            container.getUniqueId(), container.getComponentType());
        if (validateState) {
            assertTrue("Update ComponentInstance failed: " + updateComponentInstance.getResponseMessage(),
                updateComponentInstance.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
        }
        if (updateComponentInstance.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS) {
            String componentType = container.getComponentType().getValue();
            Component componentObject;
            if (componentType.equals("Resource")) {
                componentObject = getResourceObject(container.getUniqueId());
            } else {
                componentObject = getServiceObject(container.getUniqueId());
            }
            ComponentInstance componentInstanceJavaObject = ResponseParser.convertComponentInstanceResponseToJavaObject(
                updateComponentInstance.getResponse());
            return Either.left(Pair.of(componentObject, componentInstanceJavaObject));
        }
        return Either.right(updateComponentInstance);
    }

    public static Either<Pair<Component, ComponentInstance>, RestResponse> changeComponentInstanceVersion(Component containerDetails,
                                                                                                          ComponentInstance componentInstanceToReplace,
                                                                                                          Component newInstance,
                                                                                                          UserRoleEnum userRole,
                                                                                                          Boolean validateState)
        throws Exception {
        User defaultUser = ElementFactory.getDefaultUser(userRole);

        RestResponse changeComponentInstanceVersionResp = ComponentInstanceRestUtils.changeComponentInstanceVersion(containerDetails,
            componentInstanceToReplace, newInstance, defaultUser);
        if (validateState) {
            assertTrue("change ComponentInstance version failed: " + changeComponentInstanceVersionResp.getResponseMessage(),
                changeComponentInstanceVersionResp.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
        }

        if (changeComponentInstanceVersionResp.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS) {

            Component compoenntObject = AtomicOperationUtils.getComponentObject(containerDetails, userRole);
            ComponentInstance componentInstanceJavaObject = ResponseParser.convertComponentInstanceResponseToJavaObject(
                changeComponentInstanceVersionResp.getResponse());

            return Either.left(Pair.of(compoenntObject, componentInstanceJavaObject));
        }

        return Either.right(changeComponentInstanceVersionResp);
    }

    public static ComponentInstance getComponentInstanceByName(Component component, String name) {
        ComponentInstance componentInstance = component.getComponentInstances()
            .stream()
            .filter(ci -> ci.getName().equals(name))
            .findFirst()
            .orElse(null);
        if (componentInstance == null) {
            List<String> componentInstancesNameList = component.getComponentInstances().stream().map(ComponentInstance::getName)
                .collect(Collectors.toList());
            assertFalse("Instance name " + name + " not found in container " + component.getComponentType() + " named [" + component.getName()
                + "]. Component instances available are: " + componentInstancesNameList.toString(), true);
        }
        return componentInstance;
    }

    // *********** PROPERTIES *****************

    public static Either<ComponentInstanceProperty, RestResponse> addCustomPropertyToResource(PropertyReqDetails propDetails,
                                                                                              Resource resourceDetails, UserRoleEnum userRole,
                                                                                              Boolean validateState) throws Exception {

        User defaultUser = ElementFactory.getDefaultUser(userRole);
        Map<String, PropertyReqDetails> propertyToSend = new HashMap<>();
        propertyToSend.put(propDetails.getName(), propDetails);
        Gson gson = new Gson();
        RestResponse addPropertyResponse = PropertyRestUtils.createProperty(resourceDetails.getUniqueId(), gson.toJson(propertyToSend), defaultUser);

        if (validateState) {
            assertTrue("add property to resource failed: " + addPropertyResponse.getErrorCode(),
                addPropertyResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_CREATED);
        }

        if (addPropertyResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_CREATED) {
            ComponentInstanceProperty compInstProp = null;
            String property = ResponseParser.getJsonObjectValueByKey(addPropertyResponse.getResponse(), propDetails.getName());
            compInstProp = (ResponseParser.convertPropertyResponseToJavaObject(property));
            return Either.left(compInstProp);
        }
        return Either.right(addPropertyResponse);
    }

    // Benny
    public static Either<ComponentInstanceProperty, RestResponse> updatePropertyOfResource(PropertyReqDetails propDetails, Resource resourceDetails,
                                                                                           String propertyUniqueId, UserRoleEnum userRole,
                                                                                           Boolean validateState) throws Exception {

        User defaultUser = ElementFactory.getDefaultUser(userRole);
        Map<String, PropertyReqDetails> propertyToSend = new HashMap<>();
        propertyToSend.put(propDetails.getName(), propDetails);
        Gson gson = new Gson();
        RestResponse addPropertyResponse = PropertyRestUtils.updateProperty(resourceDetails.getUniqueId(), propertyUniqueId,
            gson.toJson(propertyToSend), defaultUser);

        if (validateState) {
            assertTrue("add property to resource failed: " + addPropertyResponse.getResponseMessage(),
                addPropertyResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
        }

        if (addPropertyResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS) {
            ComponentInstanceProperty compInstProp = null;
            String property = ResponseParser.getJsonObjectValueByKey(addPropertyResponse.getResponse(), propDetails.getName());
            compInstProp = (ResponseParser.convertPropertyResponseToJavaObject(property));
            return Either.left(compInstProp);
        }
        return Either.right(addPropertyResponse);
    }

    public static RestResponse deletePropertyOfResource(String resourceId, String propertyId, UserRoleEnum userRole) throws Exception {
        User defaultUser = ElementFactory.getDefaultUser(userRole);
        return PropertyRestUtils.deleteProperty(resourceId, propertyId, defaultUser);
    }

    public static Either<ComponentInstanceProperty, RestResponse> addDefaultPropertyToResource(PropertyTypeEnum propertyType,
                                                                                               Resource resourceDetails, UserRoleEnum userRole,
                                                                                               Boolean validateState) throws Exception {

        User defaultUser = ElementFactory.getDefaultUser(userRole);
        PropertyReqDetails propDetails = ElementFactory.getPropertyDetails(propertyType);
        Map<String, PropertyReqDetails> propertyToSend = new HashMap<>();
        propertyToSend.put(propDetails.getName(), propDetails);
        Gson gson = new Gson();
        RestResponse addPropertyResponse = PropertyRestUtils.createProperty(resourceDetails.getUniqueId(), gson.toJson(propertyToSend), defaultUser);

        if (validateState) {
            assertTrue("add property to resource failed: " + addPropertyResponse.getResponseMessage(),
                addPropertyResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_CREATED);
        }

        if (addPropertyResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_CREATED) {
            ComponentInstanceProperty compInstProp = null;
            String property = ResponseParser.getJsonObjectValueByKey(addPropertyResponse.getResponse(), propDetails.getName());
            compInstProp = (ResponseParser.convertPropertyResponseToJavaObject(property));

            return Either.left(compInstProp);
        }
        return Either.right(addPropertyResponse);
    }

    public static Either<GroupDefinition, RestResponse> updateGroupPropertyOnResource(String maxVFModuleInstacesValue, Resource resource,
                                                                                      String groupId, User user, Boolean validateState)
        throws Exception {

        // Json group property object
        String propertyObjectJson =
            "[{\"defaultValue\":null,\"description\":\"The maximum instances of this VF-Module\",\"name\":\"max_vf_module_instances\",\"parentUniqueId\":\"org.openecomp.groups.VfModule.1.0.groupType.max_vf_module_instances\",\"password\":false,\"required\":false,\"schema\":{\"property\":{}},\"type\":\"integer\",\"uniqueId\":\"org.openecomp.groups.VfModule.1.0.groupType.max_vf_module_instances.property.3\",\"value\":\""
                + maxVFModuleInstacesValue
                + "\",\"definition\":false,\"getInputValues\":null,\"constraints\":null,\"valueUniqueUid\":null,\"ownerId\":\"org.openecomp.groups.VfModule.1.0.groupType.max_vf_module_instances\"}]";
        RestResponse updateGroupPropertyResponse = PropertyRestUtils.updateGroupProperty(resource, groupId, propertyObjectJson, user);

        if (validateState) {
            assertTrue("update group property to resource failed: " + updateGroupPropertyResponse.getResponseMessage(),
                updateGroupPropertyResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
        }

        if (updateGroupPropertyResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS) {
            GroupDefinition responseGroupDefinition = ResponseParser.convertPropertyResponseToObject(updateGroupPropertyResponse.getResponse());
            return Either.left(responseGroupDefinition);
        }
        return Either.right(updateGroupPropertyResponse);
    }


    public static RestResponse createDefaultConsumer(Boolean validateState) {
        try {
            ConsumerDataDefinition defaultConsumerDefinition = ElementFactory.getDefaultConsumerDetails();
            RestResponse createResponse = ConsumerRestUtils.createConsumer(defaultConsumerDefinition,
                ElementFactory.getDefaultUser(UserRoleEnum.ADMIN));
            BaseRestUtils.checkCreateResponse(createResponse);

            if (validateState) {
                assertTrue(createResponse.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED);
            }
            return createResponse;
        } catch (Exception e) {
            throw new AtomicOperationException(e);
        }
    }

    /**
     * Builds Resource From rest response
     *
     * @param resourceResp
     * @return
     */
    public static Either<Resource, RestResponse> buildResourceFromResponse(RestResponse resourceResp) {
        Either<Resource, RestResponse> result;
        if (resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
            Resource resourceResponseObject = ResponseParser.convertResourceResponseToJavaObject(resourceResp.getResponse());
            result = Either.left(resourceResponseObject);
        } else {
            result = Either.right(resourceResp);
        }
        return result;
    }

    private static class AtomicOperationException extends RuntimeException {

        private AtomicOperationException(Exception e) {
            super(e);
        }

        private static final long serialVersionUID = 1L;
    }

    /**
     * Import resource from CSAR
     *
     * @param resourceType
     * @param userRole
     * @param fileName
     * @param filePath
     * @return Resource
     * @throws Exception
     */
    public static Resource importResourceFromCsar(ResourceTypeEnum resourceType, UserRoleEnum userRole, String fileName, String... filePath)
        throws Exception {
        // Get the CSARs path
        String realFilePath =
            System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "CI"
                + File.separator + "csars";
        if (filePath != null && filePath.length > 0) {
            StringBuilder result = new StringBuilder();
            for (String currStr : filePath) {
                result.append(currStr);
            }
//			realFilePath = Arrays.toString(filePath);
            realFilePath = result.toString();
        }

        // Create default import resource & user
        return importResourceFromCsarFile(resourceType, userRole, fileName, realFilePath);
    }

    public static Resource importResourceFromCsarFile(ResourceTypeEnum resourceType, UserRoleEnum userRole, String csarFileName, String csarFilePath)
        throws Exception {
        RestResponse createResource = getCreateResourceRestResponse(resourceType, userRole, csarFileName, csarFilePath);
        BaseRestUtils.checkCreateResponse(createResource);
        return ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
    }

    public static Resource importCertifiedResourceFromCsar(ResourceTypeEnum resourceType, UserRoleEnum userRole, String csarFileName,
                                                           String csarFilePath) throws Exception {
        RestResponse createResource = getCreateCertifiedResourceRestResponse(resourceType, userRole, csarFileName, csarFilePath);
        BaseRestUtils.checkSuccess(createResource);
        return ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
    }

    public static RestResponse getCreateResourceRestResponse(ResourceTypeEnum resourceType, UserRoleEnum userRole,
                                                             String csarFileName, String csarFilePath) throws IOException, Exception {

        ImportReqDetails resourceDetails = buildImportReqDetails(resourceType, csarFileName, csarFilePath);
        User sdncModifierDetails = ElementFactory.getDefaultUser(userRole);
        RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
        return createResource;
    }

    public static RestResponse getCreateCertifiedResourceRestResponse(ResourceTypeEnum resourceType, UserRoleEnum userRole,
                                                                      String csarFileName, String csarFilePath) throws IOException, Exception {

        ImportReqDetails resourceDetails = buildImportReqDetails(resourceType, csarFileName, csarFilePath);
        User sdncModifierDetails = ElementFactory.getDefaultUser(userRole);
        RestResponse response = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
        BaseRestUtils.checkCreateResponse(response);
        return LCSbaseTest.certifyResource(resourceDetails, sdncModifierDetails);
    }

    private static ImportReqDetails buildImportReqDetails(ResourceTypeEnum resourceType, String csarFileName, String csarFilePath)
        throws IOException {
        ImportReqDetails resourceDetails = ElementFactory.getDefaultImportResource();
        Path path = Paths.get(csarFilePath + File.separator + csarFileName);
        byte[] data = Files.readAllBytes(path);
        String payloadName = csarFileName;
        String payloadData = Base64.encodeBase64String(data);
        resourceDetails.setPayloadData(payloadData);
        resourceDetails.setCsarUUID(payloadName);
        resourceDetails.setPayloadName(payloadName);
        resourceDetails.setResourceType(resourceType.name());
        return resourceDetails;
    }

    public static Resource updateResourceFromCsar(Resource resource, UserRoleEnum userRole, String csarFileName, String csarFilePath)
        throws Exception {
        User sdncModifierDetails = ElementFactory.getDefaultUser(userRole);

        byte[] data = null;
        Path path = Paths.get(csarFilePath + File.separator + csarFileName);
        data = Files.readAllBytes(path);
        String payloadName = csarFileName;
        String payloadData = Base64.encodeBase64String(data);
        ImportReqDetails resourceDetails = new ImportReqDetails(resource, payloadName, payloadData);
        resourceDetails.setPayloadData(payloadData);
        resourceDetails.setCsarUUID(payloadName);
        resourceDetails.setPayloadName(payloadName);

        String userId = sdncModifierDetails.getUserId();
        Config config = Utils.getConfig();
        String url = String.format(Urls.UPDATE_RESOURCE, config.getCatalogBeHost(), config.getCatalogBePort(), resource.getUniqueId());

        Map<String, String> headersMap = ResourceRestUtils.prepareHeadersMap(userId);

        Gson gson = new Gson();
        String userBodyJson = gson.toJson(resourceDetails);
        String calculateMD5 = GeneralUtility.calculateMD5Base64EncodedByString(userBodyJson);
        headersMap.put(HttpHeaderEnum.Content_MD5.getValue(), calculateMD5);
        HttpRequest http = new HttpRequest();
        RestResponse updateResourceResponse = http.httpSendPut(url, userBodyJson, headersMap);
        BaseRestUtils.checkSuccess(updateResourceResponse);
        return ResponseParser.parseToObjectUsingMapper(updateResourceResponse.getResponse(), Resource.class);
    }

    public static Either<Resource, RestResponse> importResourceByFileName(ResourceTypeEnum resourceType, UserRoleEnum userRole, String fileName,
                                                                          Boolean validateState, String... filePath) throws IOException {

        String realFilePath =
            System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "CI"
                + File.separator + "csars";
        if (filePath != null && filePath.length > 0) {
            realFilePath = filePath.toString();
        }

        try {
            User defaultUser = ElementFactory.getDefaultUser(userRole);
            ResourceReqDetails defaultResource = ElementFactory.getDefaultResource(defaultUser);
            ImportReqDetails defaultImportResource = ElementFactory.getDefaultImportResource(defaultResource);
            ImportUtils.getImportResourceDetailsByPathAndName(defaultImportResource, realFilePath, fileName);
            RestResponse resourceResp = ResourceRestUtils.createResource(defaultImportResource, defaultUser);

            if (validateState) {
                assertTrue(resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED);
            }

            if (resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
                Resource resourceResponseObject = ResponseParser.convertResourceResponseToJavaObject(resourceResp.getResponse());
                return Either.left(resourceResponseObject);
            }
            return Either.right(resourceResp);
        } catch (Exception e) {
            throw new AtomicOperationException(e);
        }
    }

    public static Either<String, RestResponse> getComponenetArtifactPayload(Component component, String artifactType) throws Exception {

        String url;
        Config config = Utils.getConfig();
        if (component.getComponentType().toString().toUpperCase().equals(ComponentTypeEnum.SERVICE.getValue().toUpperCase())) {
            url = String.format(Urls.UI_DOWNLOAD_SERVICE_ARTIFACT, config.getCatalogBeHost(), config.getCatalogBePort(), component.getUniqueId(),
                component.getToscaArtifacts().get(artifactType).getUniqueId());
        } else {
            url = String.format(Urls.UI_DOWNLOAD_RESOURCE_ARTIFACT, config.getCatalogBeHost(), config.getCatalogBePort(), component.getUniqueId(),
                component.getToscaArtifacts().get(artifactType).getUniqueId());
        }
        String userId = component.getLastUpdaterUserId();
        Map<String, String> headersMap = new HashMap<>();
        headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), BaseRestUtils.contentTypeHeaderData);
        headersMap.put(HttpHeaderEnum.CACHE_CONTROL.getValue(), BaseRestUtils.cacheControlHeader);
        headersMap.put(HttpHeaderEnum.AUTHORIZATION.getValue(), basicAuthentication);
        headersMap.put(HttpHeaderEnum.X_ECOMP_INSTANCE_ID.getValue(), BaseRestUtils.xEcompInstanceId);
        if (userId != null) {
            headersMap.put(HttpHeaderEnum.USER_ID.getValue(), userId);
        }
        HttpRequest http = new HttpRequest();
        RestResponse response = http.httpSendGet(url, headersMap);
        if (response.getErrorCode() != BaseRestUtils.STATUS_CODE_SUCCESS && response.getResponse().getBytes() == null
            && response.getResponse().getBytes().length == 0) {
            return Either.right(response);
        }
        return Either.left(response.getResponse());

    }

    public static RestResponse getDistributionStatusByDistributionId(String distributionId, Boolean validateState) {

        try {
            User defaultUser = ElementFactory.getDefaultUser(UserRoleEnum.OPS);
            RestResponse response = DistributionUtils.getDistributionStatus(defaultUser, distributionId);

            if (validateState) {
                assertTrue(response.getErrorCode() == ResourceRestUtils.STATUS_CODE_SUCCESS);
            }
            return response;

        } catch (Exception e) {
            throw new AtomicOperationException(e);
        }
    }

    public static Either<RestResponse, Map<String, List<DistributionMonitorObject>>> getSortedDistributionStatusMap(Service service,
                                                                                                                    Boolean validateState) {

        try {
            ServiceDistributionStatus serviceDistributionObject = DistributionUtils.getLatestServiceDistributionObject(service);
            RestResponse response = getDistributionStatusByDistributionId(serviceDistributionObject.getDistributionID(), true);
            if (validateState) {
                assertTrue(response.getErrorCode() == ResourceRestUtils.STATUS_CODE_SUCCESS);
            }
            if (response.getErrorCode() == ResourceRestUtils.STATUS_CODE_SUCCESS) {
                Map<String, List<DistributionMonitorObject>> parsedDistributionStatus = DistributionUtils.getSortedDistributionStatus(response);
                return Either.right(parsedDistributionStatus);
            }
            return Either.left(response);
        } catch (Exception e) {
            throw new AtomicOperationException(e);
        }

    }


    /**
     * @param service
     * @param pollingCount
     * @param pollingInterval Recommended values for service distribution for pollingCount is 4 and for pollingInterval is 15000ms
     * @throws Exception
     */
    public static Boolean distributeAndValidateService(Service service, int pollingCount, int pollingInterval) throws Exception {
        int firstPollingInterval = 30000; //this value define first be polling topic time, should change if DC configuration changed
        Boolean statusFlag = true;
        AtomicOperationUtils.distributeService(service, true);
        TimeUnit.MILLISECONDS.sleep(firstPollingInterval);
        int timeOut = pollingCount * pollingInterval;
        com.clearspring.analytics.util.Pair<Boolean, Map<String, List<String>>> verifyDistributionStatus = null;

        while (timeOut > 0) {
            Map<String, List<DistributionMonitorObject>> sortedDistributionStatusMap = AtomicOperationUtils.getSortedDistributionStatusMap(service,
                true).right().value();
            verifyDistributionStatus = DistributionUtils.verifyDistributionStatus(sortedDistributionStatusMap);
            if (verifyDistributionStatus.left.equals(false)) {
                TimeUnit.MILLISECONDS.sleep(pollingInterval);
                timeOut -= pollingInterval;
            } else {
                timeOut = 0;
            }
        }

        if ((verifyDistributionStatus.right != null && !verifyDistributionStatus.right.isEmpty())) {
            for (Entry<String, List<String>> entry : verifyDistributionStatus.right.entrySet()) {
                if (ComponentBaseTest.getExtendTest() != null) {
                    ComponentBaseTest.getExtendTest().log(Status.INFO, "Consumer: " + entry.getKey() + " failed on following: " + entry.getValue());
                } else {
                    System.out.println("Consumer: [" + entry.getKey() + "] failed on following: " + entry.getValue());
                }
            }
            statusFlag = false;
        }
        return statusFlag;
    }

    public static Boolean distributeAndValidateService(Service service) throws Exception {
        return distributeAndValidateService(service, 10, 10000);
    }

    /**
     * @param resource to download csar file via API
     * @return Tosca definition object from main yaml file
     */
    public static ToscaDefinition downloadAndGetToscaMainYamlObjectApi(Resource resource, File filesFolder) throws Exception {
        File vfCsarFileName = new File(File.separator + "VfCsar_" + ElementFactory.generateUUIDforSufix() + ".csar");
        OnboardingUtillViaApis.downloadToscaCsarToDirectory(resource, new File(filesFolder.getPath() + vfCsarFileName));
        return ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(new File(filesFolder.getPath() + vfCsarFileName));
    }


    public static ComponentInstance getServiceComponentInstanceByName(Service service, String name, Boolean validateState) {
        List<ComponentInstance> compInstances = service.getComponentInstances();
        for (ComponentInstance instance : compInstances) {
            String compName = instance.getName();
            if (compName.equals(name)) {
                return instance;
            }
        }
        if (validateState) {
            assertEquals("Component instance name " + name + " not found", name, null);
        }
        return null;
    }

    public static Pair<Component, ComponentInstance> updateComponentInstanceName(String newName, Component component, String canvasElementName,
                                                                                 User user, Boolean validateState) throws Exception {
        ComponentInstanceReqDetails componentInstanceReqDetails = ElementFactory.getDefaultComponentInstance();
        ComponentInstance componentInstanceByName = AtomicOperationUtils.getComponentInstanceByName(component, canvasElementName);
        componentInstanceReqDetails.setName(newName);
        componentInstanceReqDetails.setComponentUid(componentInstanceByName.getComponentUid());
        componentInstanceReqDetails.setUniqueId(componentInstanceByName.getUniqueId());
        return AtomicOperationUtils.updateComponentInstance(componentInstanceReqDetails, user, component, validateState).left().value();
    }

}
