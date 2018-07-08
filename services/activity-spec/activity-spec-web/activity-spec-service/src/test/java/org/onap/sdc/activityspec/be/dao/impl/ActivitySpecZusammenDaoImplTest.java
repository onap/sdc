/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.sdc.activityspec.be.dao.impl;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementConflict;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ItemVersionConflict;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.commons.health.data.HealthInfo;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Item;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.item.ItemVersionData;
import com.amdocs.zusammen.datatypes.item.ItemVersionStatus;
import com.amdocs.zusammen.datatypes.item.Resolution;
import com.amdocs.zusammen.datatypes.itemversion.ItemVersionRevisions;
import com.amdocs.zusammen.datatypes.itemversion.Tag;

import java.io.InputStream;

import org.onap.sdc.activityspec.be.dao.impl.ActivitySpecDaoZusammenImpl.InfoPropertyName;
import org.onap.sdc.activityspec.be.dao.types.ActivitySpecEntity;
import org.onap.sdc.activityspec.be.datatypes.ActivitySpecData;
import org.onap.sdc.activityspec.be.datatypes.ActivitySpecParameter;
import org.onap.sdc.activityspec.be.datatypes.ElementType;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.types.ElementPropertyName;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

public class ActivitySpecZusammenDaoImplTest {

    private static final Version version = new Version();
    private static final String versionId = "1234";
    private static final String itemId = "5678";
    private static final String tenant = "dox";

    private ZusammenAdaptorMock zusammenAdaptor;
    private ActivitySpecDaoZusammenImpl daoImpl;
    private ActivitySpecEntity entity;


    @BeforeMethod
    public void setUp() {
        SessionContextProviderFactory.getInstance().createInterface().create("test", tenant);
        zusammenAdaptor = new ZusammenAdaptorMock();
        daoImpl = new ActivitySpecDaoZusammenImpl(zusammenAdaptor);
        entity = new ActivitySpecEntity();
        entity = new ActivitySpecEntity();

        entity.setId(itemId);
        version.setId(versionId);
        entity.setVersion(version);
        entity.setName("activitySpec");
        List<String> categoryList = new ArrayList<>();
        categoryList.add("category1");
        entity.setCategoryList(categoryList);
        ActivitySpecParameter inputParams = new ActivitySpecParameter("dbhost", "String");
        inputParams.setValue("localhost");
        List<ActivitySpecParameter> inputs = new ArrayList<>();
        inputs.add(inputParams);
        entity.setInputs(inputs);
    }

    @AfterMethod
    public void tearDown() {
        SessionContextProviderFactory.getInstance().createInterface().close();
    }

    @Test
    public void testCreate() {
        ItemVersion itemVersionmock = new ItemVersion();
        itemVersionmock.setId(new Id());

        daoImpl.create(entity);
        SessionContext context = createSessionContext();
        ElementContext elementContext = new ElementContext(entity.getId(), entity.getVersion().getId());
        Optional<ElementInfo> testElementInfo =
                zusammenAdaptor.getElementInfoByName(context, elementContext, Id.ZERO, ElementType.ACTIVITYSPEC.name());
        Assert.assertTrue(testElementInfo.isPresent());
        Assert.assertEquals(testElementInfo.get().getInfo().getName(), ElementType.ACTIVITYSPEC.name());
        Assert.assertEquals(testElementInfo.get().getInfo().getProperty(
                ActivitySpecDaoZusammenImpl.InfoPropertyName.DESCRIPTION.getValue()), entity.getDescription());
        Assert.assertEquals(testElementInfo.get().getInfo().getProperty(InfoPropertyName.CATEGORY.getValue()),
                entity.getCategoryList());
        Assert.assertEquals(testElementInfo.get().getInfo()
                                           .getProperty(ActivitySpecDaoZusammenImpl.InfoPropertyName.NAME.getValue()),
                entity.getName());

        final Optional<Element> testElement =
                zusammenAdaptor.getElement(context, elementContext, zusammenAdaptor.elementId);
        final InputStream data = testElement.get().getData();
        final ActivitySpecData activitySpecData = JsonUtil.json2Object(data, ActivitySpecData.class);
        Assert.assertEquals(activitySpecData.getInputs().get(0).getName(), entity.getInputs().get(0).getName());
    }

    @Test
    public void testGet() {
        final ActivitySpecEntity retrieved = daoImpl.get(entity);
        Assert.assertEquals(retrieved.getName(), entity.getName());
        Assert.assertEquals(retrieved.getDescription(), entity.getDescription());
        Assert.assertEquals(retrieved.getCategoryList(), entity.getCategoryList());
    }

    @Test
    public void testUpdate() {
        entity.setDescription("Update AS version1");
        daoImpl.update(entity);
        final ActivitySpecEntity retrieved = daoImpl.get(entity);
        Assert.assertEquals(retrieved.getName(), entity.getName());
        Assert.assertEquals(retrieved.getDescription(), entity.getDescription());
        Assert.assertEquals(retrieved.getCategoryList(), entity.getCategoryList());
    }

    private class ZusammenAdaptorMock implements ZusammenAdaptor {

        private ItemVersion itemVersion;
        private final Map<String, Element> elementMap = new HashMap<>();
        String elementId;

        @Override
        public Optional<ItemVersion> getFirstVersion(SessionContext context, Id itemId) {

            return Optional.ofNullable(itemVersion);
        }

        @Override
        public Collection<ItemVersion> listPublicVersions(SessionContext context, Id itemId) {
            return null;
        }

        @Override
        public ItemVersion getPublicVersion(SessionContext context, Id itemId, Id versionId) {
            return null;
        }

        @Override
        public Optional<Element> getElement(SessionContext context, ElementContext elementContext, String elementId) {
            return Optional.of(elementMap.get(elementId));
        }

        @Override
        public Optional<Element> getElementByName(SessionContext context, ElementContext elementContext,
                                                         Id parentElementId, String elementName) {
            //return Optional.empty();
            ZusammenElement element = new ZusammenElement();
            Info info = new Info();
            element.setElementId(Id.ZERO);
            info.addProperty("name", entity.getName());
            info.addProperty("description", entity.getDescription());
            info.addProperty("category", entity.getCategoryList());
            element.setInfo(info);
            return Optional.ofNullable(element);
        }

        @Override
        public Collection<ElementInfo> listElements(SessionContext context, ElementContext elementContext,
                                                           Id parentElementId) {
            return null;
        }

        @Override
        public Collection<Element> listElementData(SessionContext context, ElementContext elementContext,
                                                          Id parentElementId) {
            return elementMap.values();
        }

        @Override
        public Collection<ElementInfo> listElementsByName(SessionContext context, ElementContext elementContext,
                                                                 Id parentElementId, String elementName) {

            return elementMap.values().stream()
                                                       .filter(element -> elementName.equals(element.getInfo()
                                                                                                   .getProperty(
                                                                                        ElementPropertyName.elementType
                                                                                        .name())))
                             .map(element -> {
                                 ElementInfo elementInfo = new ElementInfo();
                                 elementInfo.setId(element.getElementId());
                                 elementInfo.setInfo(element.getInfo());
                                 return elementInfo;
                             }).collect(Collectors.toList());

        }

        @Override
        public Optional<ElementInfo> getElementInfoByName(SessionContext context, ElementContext elementContext,
                                                                 Id parentElementId, String elementName) {


            return elementMap.values().stream()
                                                       .filter(element -> elementName.equals(element.getInfo()
                                                                                      .getProperty(
                                                                                        ElementPropertyName.elementType
                                                                                                           .name())))
                             .map(element -> {
                                 ElementInfo elementInfo = new ElementInfo();
                                 elementInfo.setId(element.getElementId());
                                 elementInfo.setInfo(element.getInfo());
                                 return elementInfo;
                             }).findAny();


        }

        @Override
        public Element saveElement(SessionContext context, ElementContext elementContext, ZusammenElement element,
                                          String message) {
            if (element.getAction().equals(Action.CREATE) || element.getAction().equals(Action.UPDATE)) {
                element.setElementId(new Id(UUID.randomUUID().toString()));
            }
            elementMap.put(element.getElementId().getValue(), element);
            elementId = element.getElementId().getValue();
            return element;
        }

        @Override
        public void resolveElementConflict(SessionContext context, ElementContext elementContext,
                                                  ZusammenElement element, Resolution resolution) {

        }

        @Override
        public Collection<HealthInfo> checkHealth(SessionContext context) {
            return null;
        }

        @Override
        public Id createItem(SessionContext context, Info info) {
            return null;
        }

        @Override
        public void updateItem(SessionContext context, Id itemId, Info info) {

        }

        @Override
        public Id createVersion(SessionContext context, Id itemId, Id baseVersionId, ItemVersionData itemVersionData) {
            return null;
        }

        @Override
        public void updateVersion(SessionContext context, Id itemId, Id versionId, ItemVersionData itemVersionData) {

        }

        @Override
        public ItemVersion getVersion(SessionContext context, Id itemId, Id versionId) {
            return null;
        }

        @Override
        public String getVersion(SessionContext sessionContext) {
            return null;
        }

        @Override
        public ItemVersionStatus getVersionStatus(SessionContext context, Id itemId, Id versionId) {
            return null;
        }

        @Override
        public ItemVersionConflict getVersionConflict(SessionContext context, Id itemId, Id versionId) {
            return null;
        }

        @Override
        public void tagVersion(SessionContext context, Id itemId, Id versionId, Tag tag) {

        }

        @Override
        public void resetVersionHistory(SessionContext context, Id itemId, Id versionId, String changeRef) {

        }


        @Override
        public void publishVersion(SessionContext context, Id itemId, Id versionId, String message) {

        }

        @Override
        public void syncVersion(SessionContext sessionContext, Id itemId, Id versionId) {

        }

        @Override
        public void forceSyncVersion(SessionContext context, Id itemId, Id versionId) {

        }

        @Override
        public void cleanVersion(SessionContext sessionContext, Id itemId, Id versionId) {

        }

        @Override
        public Optional<ElementInfo> getElementInfo(SessionContext context, ElementContext elementContext,
                                                           Id elementId) {
            return Optional.empty();
        }

        @Override
        public void revert(SessionContext sessionContext, Id itemId, Id versionId, Id revisionId) {

        }

        @Override
        public ItemVersionRevisions listRevisions(SessionContext sessionContext, Id itemId, Id versionId) {
            return null;
        }

        @Override
        public Optional<ElementConflict> getElementConflict(SessionContext context, ElementContext elementContext,
                                                                   Id id) {
            return Optional.empty();
        }

        @Override
        public Collection<Item> listItems(SessionContext context) {
            return null;
        }

        @Override
        public Item getItem(SessionContext context, Id itemId) {
            return null;
        }

        @Override
        public void deleteItem(SessionContext context, Id itemId) {

        }

    }

}
