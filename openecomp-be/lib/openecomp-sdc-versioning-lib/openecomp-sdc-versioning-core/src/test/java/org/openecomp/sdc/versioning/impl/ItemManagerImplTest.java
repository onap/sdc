package org.openecomp.sdc.versioning.impl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.itempermissions.PermissionsServices;
import org.openecomp.sdc.notification.services.SubscriptionService;
import org.openecomp.sdc.versioning.dao.ItemDao;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class ItemManagerImplTest {

  private static final String USER = "user1";
  private static final String ITEM_ID = "item1";
  private static final String ITEM_NAME = "item 1 name";
  private static final String ITEM_TYPE_A = "A";
  private static final String ITEM_TYPE_B = "B";
  @Mock
  private ItemDao itemDao;
  @Mock
  private PermissionsServices permissionsServices;
  @Mock
  private SubscriptionService subscriptionService;
  @InjectMocks
  private ItemManagerImpl itemManager;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
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
    SessionContextProviderFactory.getInstance().createInterface().create(USER);

    Item returnedItem = createItem(ITEM_ID, ITEM_NAME, ITEM_TYPE_A);
    doReturn(returnedItem).when(itemDao).create(any(Item.class));

    Item inputItem = createItem(null, returnedItem.getName(), returnedItem.getType());
    Item item = itemManager.create(inputItem);

    assertItemEquals(item, returnedItem);
    verify(permissionsServices).execute(ITEM_ID, USER, "Create_Item");
    verify(subscriptionService).subscribe(USER, ITEM_ID);
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