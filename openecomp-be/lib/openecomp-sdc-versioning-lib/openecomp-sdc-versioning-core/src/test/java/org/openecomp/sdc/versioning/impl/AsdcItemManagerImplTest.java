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

package org.openecomp.sdc.versioning.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.itempermissions.PermissionsServices;
import org.openecomp.sdc.notification.services.SubscriptionService;
import org.openecomp.sdc.versioning.dao.ItemDao;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;

public class AsdcItemManagerImplTest {

  private static final String USER = "user1";
  private static final String ITEM_ID = "item1";
  private static final String ITEM_NAME = "item 1 name";
  private static final String ITEM_TYPE_A = "A";
  private static final String ITEM_TYPE_B = "B";
  private static final String tenant = "dox";
  @Mock
  private ItemDao itemDao;
  @Mock
  private PermissionsServices permissionsServices;
  @Mock
  private SubscriptionService subscriptionService;
  @InjectMocks
  private AsdcItemManagerImpl itemManager;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testList() throws Exception {
    List<Item> returnedItems = Stream.of(createItem(ITEM_ID, ITEM_NAME, ITEM_TYPE_A),
        createItem("item2", "item2 name", ITEM_TYPE_B),
        createItem("item3", "item3 name", ITEM_TYPE_B),
        createItem("item4", "item4 name", ITEM_TYPE_A)).collect(Collectors.toList());
    doReturn(returnedItems).when(itemDao).list();

    Collection<Item> items = itemManager.list(item -> ITEM_TYPE_B.equals(item.getType()));

    assertEquals(items.size(), 2);
    assertTrue(
        items.stream().anyMatch(item -> returnedItems.get(1).getName().equals(item.getName())));
    assertTrue(
        items.stream().anyMatch(item -> returnedItems.get(2).getName().equals(item.getName())));
  }

  @Test
  public void testGetNotExisting() throws Exception {
    Item item = itemManager.get(ITEM_ID);
    assertNull(item);
  }

  @Test
  public void testGet() throws Exception {
    Item toBeReturned = createItem(ITEM_ID, ITEM_NAME, ITEM_TYPE_A);
    doReturn(toBeReturned).when(itemDao).get(any(Item.class));

    Item item = itemManager.get(ITEM_ID);

    assertItemEquals(item, toBeReturned);
  }

  @Test
  public void testCreate() throws Exception {
    SessionContextProviderFactory.getInstance().createInterface().create(USER, tenant);

    Item returnedItem = createItem(ITEM_ID, ITEM_NAME, ITEM_TYPE_A);
    doReturn(returnedItem).when(itemDao).create(any(Item.class));

    Item inputItem = createItem(null, returnedItem.getName(), returnedItem.getType());
    Item item = itemManager.create(inputItem);

    assertItemEquals(item, returnedItem);

  }

  @Test
  public void testUpdateNotExistingVersionStatus() throws Exception {
    itemManager.updateVersionStatus(ITEM_ID, VersionStatus.Certified, VersionStatus.Draft);

    verify(itemDao, never()).update(any(Item.class));
  }

  @Test
  public void testUpdateVersionStatusWhenNone() throws Exception {
    Item item = new Item();
    item.setId(ITEM_ID);
    doReturn(item).when(itemDao).get(any(Item.class));

    itemManager.updateVersionStatus(ITEM_ID, VersionStatus.Certified, VersionStatus.Draft);

    verify(itemDao).update(item);
    assertEquals(item.getVersionStatusCounters().get(VersionStatus.Certified).intValue(), 1);
    assertNull(item.getVersionStatusCounters().get(VersionStatus.Draft));
  }

  @Test
  public void testUpdateVersionStatusAddFirst() throws Exception {
    Item item = new Item();
    item.setId(ITEM_ID);
    doReturn(item).when(itemDao).get(any(Item.class));

    itemManager.updateVersionStatus(ITEM_ID, VersionStatus.Draft, null);

    verify(itemDao).update(item);
    assertEquals(item.getVersionStatusCounters().size(), 1);
    assertEquals(item.getVersionStatusCounters().get(VersionStatus.Draft).intValue(), 1);
  }

  @Test
  public void testUpdateVersionStatus() throws Exception {
    Item item = new Item();
    item.setId(ITEM_ID);
    item.getVersionStatusCounters().put(VersionStatus.Certified, 2);
    item.getVersionStatusCounters().put(VersionStatus.Draft, 3);
    doReturn(item).when(itemDao).get(any(Item.class));

    itemManager.updateVersionStatus(ITEM_ID, VersionStatus.Certified, VersionStatus.Draft);

    verify(itemDao).update(item);
    assertEquals(item.getVersionStatusCounters().size(), 2);
    assertEquals(item.getVersionStatusCounters().get(VersionStatus.Certified).intValue(), 3);
    assertEquals(item.getVersionStatusCounters().get(VersionStatus.Draft).intValue(), 2);
  }

  @Test
  public void testUpdateVersionStatusRemoveLast() throws Exception {
    Item item = new Item();
    item.setId(ITEM_ID);
    item.getVersionStatusCounters().put(VersionStatus.Certified, 2);
    item.getVersionStatusCounters().put(VersionStatus.Draft, 1);
    doReturn(item).when(itemDao).get(any(Item.class));

    itemManager.updateVersionStatus(ITEM_ID, VersionStatus.Certified, VersionStatus.Draft);

    verify(itemDao).update(item);
    assertEquals(item.getVersionStatusCounters().size(), 1);
    assertEquals(item.getVersionStatusCounters().get(VersionStatus.Certified).intValue(), 3);
    assertNull(item.getVersionStatusCounters().get(VersionStatus.Draft));
  }

  private Item createItem(String id, String name, String type) {
    Item item = new Item();
    item.setId(id);
    item.setName(name);
    item.setType(type);
    return item;
  }

  private void assertItemEquals(Item actual, Item expected) {
    assertEquals(actual.getId(), expected.getId());
    assertEquals(actual.getName(), expected.getName());
    assertEquals(actual.getType(), expected.getType());
  }
}
