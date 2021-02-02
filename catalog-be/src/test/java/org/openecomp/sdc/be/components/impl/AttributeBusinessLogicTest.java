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
 * Modifications copyright (c) 2019 Nokia
 * Modifications Copyright (C) 2020 Nordix Foundation
 * ================================================================================
 */

package org.openecomp.sdc.be.components.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import fj.data.Either;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.sdc.be.components.validation.UserValidations;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.impl.ComponentsUtils;
import org.openecomp.sdc.be.model.AttributeDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.LifecycleStateEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.operations.api.IGraphLockOperation;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.AttributeOperation;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.openecomp.sdc.exception.ResponseFormat;

public class AttributeBusinessLogicTest extends BaseBusinessLogicMock {

    private AttributeBusinessLogic createTestSubject() {
        return new AttributeBusinessLogic(elementDao, groupOperation, groupInstanceOperation, groupTypeOperation,
            interfaceOperation, interfaceLifecycleTypeOperation, artifactToscaOperation);
    }

    private UserValidations userValidations = Mockito.mock(UserValidations.class);
    private ComponentsUtils componentsUtils = Mockito.mock(ComponentsUtils.class);
    private JanusGraphDao janusGraphDao = Mockito.mock(JanusGraphDao.class);
    private ToscaOperationFacade toscaOperationFacade = Mockito.mock(ToscaOperationFacade.class);
    private ApplicationDataTypeCache applicationDataTypeCache = Mockito.mock(ApplicationDataTypeCache.class);
    private AttributeOperation attributeOperation = Mockito.mock(AttributeOperation.class);
    private Field baseBusinessLogic;
    private AttributeBusinessLogic attributeBusinessLogic = createTestSubject();
    private IGraphLockOperation igraphLockOperation = Mockito.mock(IGraphLockOperation.class);

    @Before
    public void setup() throws Exception {
        baseBusinessLogic = attributeBusinessLogic.getClass().getSuperclass().getDeclaredField("graphLockOperation");
        baseBusinessLogic.setAccessible(true);
        baseBusinessLogic.set(attributeBusinessLogic, igraphLockOperation);

        final Field baseBusinessLogic1 = attributeBusinessLogic.getClass().getSuperclass()
            .getDeclaredField("userValidations");
        baseBusinessLogic1.setAccessible(true);
        baseBusinessLogic1.set(attributeBusinessLogic, userValidations);

        final Field baseBusinessLogic2 = attributeBusinessLogic.getClass().getSuperclass()
            .getDeclaredField("componentsUtils");
        baseBusinessLogic2.setAccessible(true);
        baseBusinessLogic2.set(attributeBusinessLogic, componentsUtils);

        final Field baseBusinessLogic3 = attributeBusinessLogic.getClass().getSuperclass()
            .getDeclaredField("janusGraphDao");
        baseBusinessLogic3.setAccessible(true);
        baseBusinessLogic3.set(attributeBusinessLogic, janusGraphDao);

        baseBusinessLogic = attributeBusinessLogic.getClass().getSuperclass().getDeclaredField("toscaOperationFacade");
        baseBusinessLogic.setAccessible(true);
        baseBusinessLogic.set(attributeBusinessLogic, toscaOperationFacade);

        baseBusinessLogic = attributeBusinessLogic.getClass().getSuperclass()
            .getDeclaredField("applicationDataTypeCache");
        baseBusinessLogic.setAccessible(true);
        baseBusinessLogic.set(attributeBusinessLogic, applicationDataTypeCache);

        baseBusinessLogic = attributeBusinessLogic.getClass().getSuperclass().getDeclaredField("attributeOperation");
        baseBusinessLogic.setAccessible(true);
        baseBusinessLogic.set(attributeBusinessLogic, attributeOperation);
    }

    @Test
    public void createAttribute_lockfail() {
        Either<AttributeDefinition, ResponseFormat> response;
        response = attributeBusinessLogic.createAttribute("RES01", new AttributeDefinition(), "USR01");
        assertTrue(response.isRight());
    }

    @Test
    public void createAttribute_Success() {
        Component resource = new Resource();
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resource.setIsDeleted(false);
        resource.setLastUpdaterUserId("USR01");

        AttributeDefinition attrib = new AttributeDefinition();
        attrib.setType(ToscaPropertyType.STRING.getType());

        when(igraphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);

        Either<Component, StorageOperationStatus> toscastatus = Either.left(resource);
        when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);
        AttributeDefinition attributeDataDefinition = new AttributeDefinition();
        Either<AttributeDefinition, StorageOperationStatus> either = Either.left(attributeDataDefinition);
        when(toscaOperationFacade.addAttributeOfResource(any(), any())).thenReturn(either);

        when(attributeOperation.isAttributeTypeValid(any())).thenReturn(true);

        Map<String, DataTypeDefinition> data = new HashMap<>();
        data.put("ONE", new DataTypeDefinition());
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = Either.left(data);
        when(applicationDataTypeCache.getAll()).thenReturn(allDataTypes);

        when(attributeOperation.isAttributeDefaultValueValid(any(), any())).thenReturn(true);
        Either<AttributeDefinition, ResponseFormat> response;

        response = attributeBusinessLogic.createAttribute("RES01", attrib, "USR01");

        assertTrue(response.isLeft());
    }

    @Test
    public void createAttribute_failtogettoscaelement() throws NoSuchFieldException, IllegalAccessException {
        Component resource = new Resource();
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resource.setIsDeleted(false);
        resource.setLastUpdaterUserId("USR01");

        when(igraphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);

        Either<AttributeDefinition, ResponseFormat> response;
        AttributeDefinition prop = new AttributeDefinition();

        baseBusinessLogic = attributeBusinessLogic.getClass().getSuperclass().getDeclaredField("toscaOperationFacade");
        baseBusinessLogic.setAccessible(true);
        baseBusinessLogic.set(attributeBusinessLogic, toscaOperationFacade);
        Either<Component, StorageOperationStatus> toscastatus = Either.right(StorageOperationStatus.GENERAL_ERROR);
        when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

        response = attributeBusinessLogic.createAttribute("RES01", prop, "USR01");

        assertTrue(response.isRight());
    }

    @Test
    public void createAttribute_componentvalidationfails() {
        Component resource = new Resource();
        resource.setLifecycleState(LifecycleStateEnum.CERTIFIED);
        resource.setIsDeleted(false);
        resource.setLastUpdaterUserId("USR02");

        when(igraphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);

        Either<AttributeDefinition, ResponseFormat> response;
        AttributeDefinition prop = new AttributeDefinition();

        Either<Component, StorageOperationStatus> toscastatus = Either.left(resource);
        when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

        response = attributeBusinessLogic.createAttribute("RES01", prop, "USR01");

        assertTrue(response.isRight());
    }

    @Test
    public void createAttribute_componentalreadyexist_fails() {
        Either<AttributeDefinition, ResponseFormat> response;
        AttributeDefinition attrib = new AttributeDefinition();
        attrib.setName("RES01");
        attrib.setOwnerId("RES01");

        List<AttributeDefinition> attributes = new ArrayList<>();
        attributes.add(attrib);

        final Resource resource = new Resource();
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resource.setIsDeleted(false);
        resource.setLastUpdaterUserId("USR01");
        resource.setAttributes(attributes);

        when(igraphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);

        Either<Component, StorageOperationStatus> toscastatus = Either.left(resource);
        when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

        response = attributeBusinessLogic.createAttribute("RES01", attrib, "USR01");

        assertTrue(response.isRight());
    }

    @Test
    public void createAttribute_addresourcetostoragefails() {

        Component resource = new Resource();
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resource.setIsDeleted(false);
        resource.setLastUpdaterUserId("USR01");

        IGraphLockOperation igraphLockOperation = Mockito.mock(IGraphLockOperation.class);
        when(igraphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);

        Either<Component, StorageOperationStatus> toscastatus = Either.left(resource);
        when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);
        AttributeDefinition attributeDataDefinition = new AttributeDefinition();
        Either<AttributeDefinition, StorageOperationStatus> either = Either
            .right(StorageOperationStatus.CONNECTION_FAILURE);
        when(toscaOperationFacade.addAttributeOfResource(any(), any())).thenReturn(either);

        when(attributeOperation.isAttributeTypeValid(any())).thenReturn(true);

        Map<String, DataTypeDefinition> data = new HashMap<>();
        data.put("ONE", new DataTypeDefinition());
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> allDataTypes = Either.left(data);
        when(applicationDataTypeCache.getAll()).thenReturn(allDataTypes);

        when(attributeOperation.isAttributeDefaultValueValid(any(), any())).thenReturn(true);
        Either<AttributeDefinition, ResponseFormat> response;

        AttributeDefinition attrib = new AttributeDefinition();
        response = attributeBusinessLogic.createAttribute("RES01", attrib, "USR01");

        assertTrue(response.isRight());

    }

    @Test
    public void testgetAttribute_ATTRIBUTE_NOT_FOUND() {
        Either<AttributeDefinition, ResponseFormat> result;

        Component resource = new Resource();
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resource.setIsDeleted(false);
        resource.setLastUpdaterUserId("USR01");
        Either<Component, StorageOperationStatus> toscastatus = Either.left(resource);
        when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

        result = attributeBusinessLogic.getAttribute("RES01", "ATTR01", "USR01");
        assertTrue(result.isRight());
    }

    @Test
    public void testgetAttribute_success() {
        Either<AttributeDefinition, ResponseFormat> result;

        final Resource resource = new Resource();
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resource.setIsDeleted(false);
        resource.setLastUpdaterUserId("USR01");

        AttributeDefinition attrib = new AttributeDefinition();
        attrib.setUniqueId("ATTR01");
        attrib.setOwnerId("RES01");

        List<AttributeDefinition> attr = new ArrayList<>();
        attr.add(attrib);

        resource.setAttributes(attr);
        Either<Component, StorageOperationStatus> toscastatus = Either.left(resource);
        when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

        result = attributeBusinessLogic.getAttribute("RES01", "ATTR01", "USR01");
        assertTrue(result.isLeft());
    }

    @Test
    public void testgetAttribute_RESOURCE_NOT_FOUND() {
        Either<AttributeDefinition, ResponseFormat> result;

        Either<Component, StorageOperationStatus> toscastatus = Either
            .right(StorageOperationStatus.PARENT_RESOURCE_NOT_FOUND);
        when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

        result = attributeBusinessLogic.getAttribute("RES01", "ATTR01", "USR01");
        assertTrue(result.isRight());
    }

    @Test
    public void testdeleteAttribute_FAILED_TO_LOCK_COMPONENT() {
        Either<AttributeDefinition, ResponseFormat> result;

        result = attributeBusinessLogic.deleteAttribute("RES01", "ATTR01", "USR01");
        assertTrue(result.isRight());
    }

    @Test
    public void testdeleteAttribute_get_RESOURCE_from_DB_failed() {
        Either<AttributeDefinition, ResponseFormat> result;

        Either<Component, StorageOperationStatus> toscastatus = Either.right(StorageOperationStatus.CONNECTION_FAILURE);
        when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

        result = attributeBusinessLogic.deleteAttribute("RES01", "ATTR01", "USR01");
        assertTrue(result.isRight());
    }

    @Test
    public void testdeleteAttribute_get_RESOURCE_verification_failed() {
        Either<AttributeDefinition, ResponseFormat> result;
        Component resource = new Resource();
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resource.setIsDeleted(true);
        resource.setLastUpdaterUserId("USR01");
        when(igraphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);
        Either<Component, StorageOperationStatus> toscastatus = Either.left(resource);
        when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);
        result = attributeBusinessLogic.deleteAttribute("RES01", "ATTR01", "USR01");
        assertTrue(result.isRight());
    }

    @Test
    public void testdeleteAttribute_nonexistingresource() {
        Either<AttributeDefinition, ResponseFormat> result;

        Component resource = new Resource();
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resource.setIsDeleted(false);
        resource.setLastUpdaterUserId("USR01");

        when(igraphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);

        Either<Component, StorageOperationStatus> toscastatus = Either.left(resource);
        when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);

        result = attributeBusinessLogic.deleteAttribute("RES01", "ATTR01", "USR01");
        assertTrue(result.isRight());
    }

    @Test
    public void testdeleteAttribute_success() {
        Either<AttributeDefinition, ResponseFormat> result;
        final Resource resource = new Resource();
        resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKOUT);
        resource.setIsDeleted(false);
        resource.setLastUpdaterUserId("USR01");

        when(igraphLockOperation.lockComponent(any(), any())).thenReturn(StorageOperationStatus.OK);

        Either<Component, StorageOperationStatus> toscastatus = Either.left(resource);
        when(toscaOperationFacade.getToscaElement("RES01")).thenReturn(toscastatus);
        when(toscaOperationFacade.deleteAttributeOfResource(any(), any())).thenReturn(StorageOperationStatus.OK);

        AttributeDefinition attrib = new AttributeDefinition();
        attrib.setUniqueId("ATTR01");
        attrib.setOwnerId("RES01");
        List<AttributeDefinition> attributes = new ArrayList<>();
        attributes.add(attrib);
        resource.setAttributes(attributes);

        result = attributeBusinessLogic.deleteAttribute("RES01", "ATTR01", "USR01");
        assertTrue(result.isLeft());
    }

}
