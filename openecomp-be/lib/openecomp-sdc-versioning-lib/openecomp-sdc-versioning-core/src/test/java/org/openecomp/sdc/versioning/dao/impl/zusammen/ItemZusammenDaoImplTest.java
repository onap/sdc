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

package org.openecomp.sdc.versioning.dao.impl.zusammen;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.openecomp.sdc.versioning.dao.impl.zusammen.ItemZusammenDaoImpl.ItemInfoProperty.ITEM_TYPE;
import static org.openecomp.sdc.versioning.dao.impl.zusammen.ItemZusammenDaoImpl.ItemInfoProperty.ITEM_VERSIONS_STATUSES;
import static org.openecomp.sdc.versioning.dao.impl.zusammen.TestUtil.createZusammenContext;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.item.Info;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.ItemStatus;

public class ItemZusammenDaoImplTest {

  private static final String USER = "user1";
  private static final String APP_PROP_1 = "app_prop1";
  private static final String APP_PROP_2 = "app_prop2";
  private static final String tenant = "dox";

  @Mock
  private ZusammenAdaptor zusammenAdaptorMock;
  @InjectMocks
  private ItemZusammenDaoImpl itemDao;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
    SessionContextProviderFactory.getInstance().createInterface().create(USER, tenant);
  }

  @Test
  public void testListWhenNone() throws Exception {
    doReturn(new ArrayList<>()).when(zusammenAdaptorMock)
        .listItems(createZusammenContext(USER));

    Collection<Item> items = itemDao.list();

    Assert.assertTrue(items.isEmpty());
  }

  @Test
  public void testList() throws Exception {
    Map<String, Number> vlm1versionStatuses = new HashMap<>();
    vlm1versionStatuses.put(VersionStatus.Draft.name(), 1);

    Map<String, Number> vsp2versionStatuses = new HashMap<>();
    vsp2versionStatuses.put(VersionStatus.Draft.name(), 3);
    vsp2versionStatuses.put(VersionStatus.Certified.name(), 2);


    List<com.amdocs.zusammen.datatypes.item.Item> returnedItems = Stream.of(
        createItem("1", "vsp1", "vsp 1", "vsp", new Date(), new Date(), new HashMap<>()),
        createItem("2", "vlm1", "vlm 1", "vlm", new Date(), new Date(), vlm1versionStatuses),
        createItem("3", "vsp2", "vsp 2", "vsp", new Date(), new Date(), vsp2versionStatuses))
        .collect(Collectors.toList());
    doReturn(returnedItems).when(zusammenAdaptorMock).listItems(createZusammenContext(USER));

    Collection<Item> items = itemDao.list();
    assertEquals(3, items.size());

    Iterator<Item> itemIterator = items.iterator();
    assertItemEquals(itemIterator.next(), returnedItems.get(0));
    assertItemEquals(itemIterator.next(), returnedItems.get(1));
    assertItemEquals(itemIterator.next(), returnedItems.get(2));
  }

  @Test
  public void testGetNonExisting() throws Exception {
    Item requestedItem = new Item();
    requestedItem.setId("1");

    Item item = itemDao.get(requestedItem);

    Assert.assertNull(item);
  }

  @Test
  public void testGet() throws Exception {
    Item inputItem = new Item();
    inputItem.setId("1");

    Map<String, Number> versionStatuses = new HashMap<>();
    versionStatuses.put(VersionStatus.Draft.name(), 3);
    versionStatuses.put(VersionStatus.Certified.name(), 2);

    com.amdocs.zusammen.datatypes.item.Item toBeReturned =
        createItem("1", "vsp1", "vsp 1", "vsp", new Date(System.currentTimeMillis() - 100),
            new Date(), versionStatuses);
    doReturn(toBeReturned).when(zusammenAdaptorMock)
        .getItem(createZusammenContext(USER), new Id(inputItem.getId()));

    Item item = itemDao.get(inputItem);

    Assert.assertNotNull(item);
    assertItemEquals(item, toBeReturned);
    assertEquals(ItemStatus.ACTIVE, item.getStatus());

  }

  @Test
  public void testCreate() throws Exception {
    Item inputItem = new Item();
    inputItem.setName("vsp1");
    inputItem.setDescription("VSP 1");
    inputItem.setType("vsp");

    ArgumentCaptor<Info> capturedZusammenInfo = ArgumentCaptor.forClass(Info.class);

    String itemId = "1";
    doReturn(new Id(itemId)).when(zusammenAdaptorMock)
        .createItem(eq(createZusammenContext(USER)), capturedZusammenInfo.capture());

    Item item = itemDao.create(inputItem);

    Info capturedInfo = capturedZusammenInfo.getValue();
    assertEquals(capturedInfo.getName(), inputItem.getName());
    assertEquals(capturedInfo.getDescription(), inputItem.getDescription());
    assertEquals(capturedInfo.getProperty(ITEM_TYPE.getName()), inputItem.getType());
    assertEquals(capturedInfo.getProperty(ITEM_VERSIONS_STATUSES.getName()),
        inputItem.getVersionStatusCounters());

    assertEquals(item.getId(), itemId);
    assertEquals(item.getName(), inputItem.getName());
    assertEquals(item.getDescription(), inputItem.getDescription());
    assertEquals(item.getType(), inputItem.getType());
    assertEquals(item.getVersionStatusCounters(), inputItem.getVersionStatusCounters());
  }

  @Test
  public void testUpdate() throws Exception {
    Item item = new Item();
    item.setId("1");
    item.setName("vsp1");
    item.setDescription("VSP 1");
    item.setType("vsp");
    item.addVersionStatus(VersionStatus.Draft);
    item.addVersionStatus(VersionStatus.Draft);
    item.addVersionStatus(VersionStatus.Certified);

    ArgumentCaptor<Info> capturedZusammenInfo = ArgumentCaptor.forClass(Info.class);

    itemDao.update(item);

    verify(zusammenAdaptorMock)
        .updateItem(eq(createZusammenContext(USER)), eq(new Id(item.getId())),
            capturedZusammenInfo.capture());

    Info capturedInfo = capturedZusammenInfo.getValue();
    assertEquals(capturedInfo.getName(), item.getName());
    assertEquals(capturedInfo.getDescription(), item.getDescription());
    assertEquals(capturedInfo.getProperty(ITEM_TYPE.getName()), item.getType());
    assertEquals(capturedInfo.getProperty(ITEM_VERSIONS_STATUSES.getName()),
        item.getVersionStatusCounters());
  }

  private com.amdocs.zusammen.datatypes.item.Item createItem(String id, String name,
                                                             String description, String type,
                                                             Date creationTime,
                                                             Date modificationTime,
                                                             Map<String, Number> versionStatusCounters) {
    com.amdocs.zusammen.datatypes.item.Item item = new com.amdocs.zusammen.datatypes.item.Item();
    item.setId(new Id(id));
    Info info = new Info();
    info.setName(name);
    info.setDescription(description);
    info.addProperty(ITEM_TYPE.getName(), type);
    info.addProperty(ITEM_VERSIONS_STATUSES.getName(), versionStatusCounters);
    info.addProperty(APP_PROP_1, "app_prop1_value");
    info.addProperty(APP_PROP_2, 8);
    item.setInfo(info);
    item.setCreationTime(creationTime);
    item.setModificationTime(modificationTime);
    return item;
  }

  private void assertItemEquals(Item item, com.amdocs.zusammen.datatypes.item.Item zusammenItem) {
    assertEquals(item.getId(), zusammenItem.getId().getValue());
    assertEquals(item.getName(), zusammenItem.getInfo().getName());
    assertEquals(item.getDescription(), zusammenItem.getInfo().getDescription());
    assertEquals(item.getType(), zusammenItem.getInfo().getProperty(ITEM_TYPE.getName()));
    assertEquals(item.getProperties().get(APP_PROP_1),
        zusammenItem.getInfo().getProperty(APP_PROP_1));
    assertEquals(item.getProperties().get(APP_PROP_2),
        zusammenItem.getInfo().getProperty(APP_PROP_2));

    Map<String, Number> zusammenStatusesMap =
        zusammenItem.getInfo().getProperty(ITEM_VERSIONS_STATUSES.getName());
    Map<VersionStatus, Integer> statusesMap = item.getVersionStatusCounters();

    zusammenStatusesMap.entrySet()
            .forEach(entry -> assertEquals(statusesMap.get(VersionStatus.valueOf(entry.getKey())), entry.getValue()));

    assertEquals(item.getCreationTime(), zusammenItem.getCreationTime());
    assertEquals(item.getModificationTime(), zusammenItem.getModificationTime());
  }

}
