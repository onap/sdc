/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.ci.tests.capability;

import fj.data.Either;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.elements.SchemaDefinition;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.CapabilityDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.CapabilityRestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils.getResourceObject;
import static org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils.getServiceObject;
import static org.testng.AssertJUnit.fail;

public class CapabilitiesTest extends ComponentBaseTest {
    @Rule
    public static TestName name = new TestName();

    private static User user = null;
    private static Service service;
    private static Resource resource;
    private static Resource pnfResource;

    public CapabilitiesTest() {
        super(name, CapabilitiesTest.class.getName());
    }

    @BeforeTest
    public void init() throws Exception {
        user = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

        // Create default service
        Either<Service, RestResponse> createDefaultServiceEither =
                AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
        if (createDefaultServiceEither.isRight()) {
            fail("Error creating default service");
        }
        service = createDefaultServiceEither.left().value();

        // Create default resource
        Either<Resource, RestResponse> createDefaultResourceEither =
                AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true);
        if (createDefaultResourceEither.isRight()) {
            fail("Error creating default resource");
        }
        resource = createDefaultResourceEither.left().value();

        // Create default PNF resource
        Either<Resource, RestResponse> createDefaultPNFResourceEither =
                AtomicOperationUtils.createResourceByType(ResourceTypeEnum.PNF, UserRoleEnum.DESIGNER, true);
        if (createDefaultPNFResourceEither.isRight()) {
            fail("Error creating default pnf resource");
        }
        pnfResource = createDefaultPNFResourceEither.left().value();
    }

    @Test
    public void createCapabilityOnServiceTest() throws Exception {

        CapabilityDetails capability = createCapability();
        RestResponse restResponse = CapabilityRestUtils.createCapability(service, Collections.singletonList(capability),
                user);
        logger.info("createCapability On Service Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "createCapabilityOnServiceTest")
    public void updateCapabilityOnServiceTest() throws Exception {

        CapabilityDetails capability = createCapability();
        capability.setMaxOccurrences("10");
        capability.setMinOccurrences("4");
        RestResponse restResponse = CapabilityRestUtils.updateCapability(service, Collections.singletonList(capability),
                user);
        logger.info("updateCapability On Service Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "updateCapabilityOnServiceTest")
    public void getCapabilityFromServiceTest() throws Exception {
        Service serviceObject = getServiceObject(service.getUniqueId());

        List<org.openecomp.sdc.be.model.CapabilityDefinition> capabilityDefinitionList = serviceObject.getCapabilities()
                .values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        RestResponse restResponse = CapabilityRestUtils.getCapability(service,
                capabilityDefinitionList.get(0).getUniqueId(), user);
        logger.info("getCapabilityTest from Service Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "getCapabilityFromServiceTest")
    public void deleteCapabilityFromServiceTest() throws Exception {
        Service serviceObject = getServiceObject(service.getUniqueId());

        List<org.openecomp.sdc.be.model.CapabilityDefinition> capabilityDefinitionList = serviceObject.getCapabilities()
                .values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        RestResponse restResponse = CapabilityRestUtils.deleteCapability(service,
                capabilityDefinitionList.get(0).getUniqueId(), user);
        logger.info("deleteCapabilityTest from Service Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test
    public void createCapabilityWithPropertiesOnServiceTest() throws Exception {

        CapabilityDetails capability = createCapability();
        List<ComponentInstanceProperty> properties = new ArrayList<>();
        ComponentInstanceProperty instanceProperty = new ComponentInstanceProperty();
        instanceProperty.setType("prop_type");
        instanceProperty.setName("prop_name");
        instanceProperty.setDescription("prop_description");
        instanceProperty.setSchema(new SchemaDefinition());
        properties.add(instanceProperty);
        capability.setProperties(properties);
        RestResponse restResponse = CapabilityRestUtils.createCapability(service, Collections.singletonList(capability),
                user);
        logger.info("createCapability On Service Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "createCapabilityWithPropertiesOnServiceTest")
    public void updateCapabilityWithPropertiesOnServiceTest() throws Exception {

        CapabilityDetails capability = createCapability();
        capability.setMaxOccurrences("10");
        capability.setMinOccurrences("4");

        List<ComponentInstanceProperty> properties = new ArrayList<>();
        ComponentInstanceProperty instanceProperty = new ComponentInstanceProperty();
        instanceProperty.setType("prop_type_updated");
        instanceProperty.setName("prop_name_updated");
        instanceProperty.setDescription("prop_description_prop_desc");
        instanceProperty.setSchema(new SchemaDefinition());
        properties.add(instanceProperty);
        capability.setProperties(properties);
        RestResponse restResponse = CapabilityRestUtils.updateCapability(service, Collections.singletonList(capability),
                user);
        logger.info("updateCapability On Service Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test
    public void createCapabilityOnVfTest() throws Exception {

        CapabilityDetails capability = createCapability();
        RestResponse restResponse = CapabilityRestUtils.createCapability(resource, Collections.singletonList(capability),
                user);
        logger.info("createCapability On Vf Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "createCapabilityOnVfTest")
    public void updateCapabilityOnVfTest() throws Exception {

        CapabilityDetails capability = createCapability();
        capability.setMaxOccurrences("10");
        capability.setMinOccurrences("4");
        RestResponse restResponse = CapabilityRestUtils.updateCapability(resource, Collections.singletonList(capability),
                user);
        logger.info("updateCapability On Vf Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "updateCapabilityOnVfTest")
    public void getCapabilityFromVfTest() throws Exception {
        Resource resourceObject = getResourceObject(resource.getUniqueId());

        List<org.openecomp.sdc.be.model.CapabilityDefinition> capabilityDefinitionList = resourceObject.getCapabilities()
                .values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        RestResponse restResponse = CapabilityRestUtils.getCapability(resource,
                capabilityDefinitionList.get(0).getUniqueId(), user);
        logger.info("getCapabilityTest from Vf Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "getCapabilityFromVfTest")
    public void deleteCapabilityFromVfTest() throws Exception {
        Resource resourceObject = getResourceObject(resource.getUniqueId());

        List<org.openecomp.sdc.be.model.CapabilityDefinition> capabilityDefinitionList = resourceObject.getCapabilities()
                .values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        RestResponse restResponse = CapabilityRestUtils.deleteCapability(resource,
                capabilityDefinitionList.get(0).getUniqueId(), user);
        logger.info("deleteCapabilityTest from Vf Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test
    public void createCapabilityWithPropertiesOnVfTest() throws Exception {

        CapabilityDetails capability = createCapability();
        List<ComponentInstanceProperty> properties = new ArrayList<>();
        ComponentInstanceProperty instanceProperty = new ComponentInstanceProperty();
        instanceProperty.setType("prop_type");
        instanceProperty.setName("prop_name");
        instanceProperty.setDescription("prop_description");
        instanceProperty.setSchema(new SchemaDefinition());
        properties.add(instanceProperty);
        capability.setProperties(properties);
        RestResponse restResponse = CapabilityRestUtils.createCapability(resource, Collections.singletonList(capability),
                user);
        logger.info("createCapability On Vf Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "createCapabilityWithPropertiesOnVfTest")
    public void updateCapabilityWithPropertiesOnVfTest() throws Exception {

        CapabilityDetails capability = createCapability();
        capability.setMaxOccurrences("10");
        capability.setMinOccurrences("4");

        List<ComponentInstanceProperty> properties = new ArrayList<>();
        ComponentInstanceProperty instanceProperty = new ComponentInstanceProperty();
        instanceProperty.setType("prop_type_updated");
        instanceProperty.setName("prop_name_updated");
        instanceProperty.setDescription("prop_description_prop_desc");
        instanceProperty.setSchema(new SchemaDefinition());
        properties.add(instanceProperty);
        capability.setProperties(properties);
        RestResponse restResponse = CapabilityRestUtils.updateCapability(resource, Collections.singletonList(capability),
                user);
        logger.info("updateCapability On Vf Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }


    @Test
    public void createCapabilityOnPnfTest() throws Exception {

        CapabilityDetails capability = createCapability();
        RestResponse restResponse = CapabilityRestUtils.createCapability(pnfResource, Collections.singletonList(capability),
                user);
        logger.info("createCapability On Pnf Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "createCapabilityOnPnfTest")
    public void updateCapabilityOnPnfTest() throws Exception {

        CapabilityDetails capability = createCapability();
        capability.setMaxOccurrences("10");
        capability.setMinOccurrences("4");
        RestResponse restResponse = CapabilityRestUtils.updateCapability(pnfResource, Collections.singletonList(capability),
                user);
        logger.info("updateCapability On Pnf Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "updateCapabilityOnPnfTest")
    public void getCapabilityFromPnfTest() throws Exception {
        Resource pnfResourceObject = getResourceObject(pnfResource.getUniqueId());

        List<org.openecomp.sdc.be.model.CapabilityDefinition> capabilityDefinitionList = pnfResourceObject.getCapabilities()
                .values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        RestResponse restResponse = CapabilityRestUtils.getCapability(pnfResource,
                capabilityDefinitionList.get(0).getUniqueId(), user);
        logger.info("getCapabilityTest from Pnf Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "getCapabilityFromPnfTest")
    public void deleteCapabilityFromPnfTest() throws Exception {
        Resource pnfResourceObject = getResourceObject(pnfResource.getUniqueId());

        List<org.openecomp.sdc.be.model.CapabilityDefinition> capabilityDefinitionList = pnfResourceObject.getCapabilities()
                .values().stream().flatMap(Collection::stream).collect(Collectors.toList());

        RestResponse restResponse = CapabilityRestUtils.deleteCapability(pnfResource,
                capabilityDefinitionList.get(0).getUniqueId(), user);
        logger.info("deleteCapabilityTest from Pnf Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test
    public void createCapabilityWithPropertiesOnPnfTest() throws Exception {

        CapabilityDetails capability = createCapability();
        List<ComponentInstanceProperty> properties = new ArrayList<>();
        ComponentInstanceProperty instanceProperty = new ComponentInstanceProperty();
        instanceProperty.setType("prop_type");
        instanceProperty.setName("prop_name");
        instanceProperty.setDescription("prop_description");
        instanceProperty.setSchema(new SchemaDefinition());
        properties.add(instanceProperty);
        capability.setProperties(properties);
        RestResponse restResponse = CapabilityRestUtils.createCapability(pnfResource, Collections.singletonList(capability),
                user);
        logger.info("createCapability On Pnf Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "createCapabilityWithPropertiesOnPnfTest")
    public void updateCapabilityWithPropertiesOnPnfTest() throws Exception {

        CapabilityDetails capability = createCapability();
        capability.setMaxOccurrences("10");
        capability.setMinOccurrences("4");

        List<ComponentInstanceProperty> properties = new ArrayList<>();
        ComponentInstanceProperty instanceProperty = new ComponentInstanceProperty();
        instanceProperty.setType("prop_type_updated");
        instanceProperty.setName("prop_name_updated");
        instanceProperty.setDescription("prop_description_prop_desc");
        instanceProperty.setSchema(new SchemaDefinition());
        properties.add(instanceProperty);
        capability.setProperties(properties);
        RestResponse restResponse = CapabilityRestUtils.updateCapability(pnfResource,
                Collections.singletonList(capability), user);
        logger.info("updateCapability On Pnf Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }


    private CapabilityDetails createCapability() {
        CapabilityDetails capabilityDetails = new CapabilityDetails();
        capabilityDetails.setName("cap" + Math.random());
        capabilityDetails.setType("tosca.capabilities.network.Bindable");

        return capabilityDetails;
    }
}
