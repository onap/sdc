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
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ItemManagerImplTest {

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
    doReturn(Stream.of(
        createItem("item1", "A"),
        createItem("item2", "B"),
        createItem("item3", "B"),
        createItem("item4", "A"))
        .collect(Collectors.toList())).when(itemDao).list();

    Collection<Item> items = itemManager.list(item -> "B".equals(item.getType()));
    Assert.assertEquals(items.size(), 2);
    Assert.assertTrue(items.stream().anyMatch(item -> "item2".equals(item.getName())));
    Assert.assertTrue(items.stream().anyMatch(item -> "item3".equals(item.getName())));
  }

  @Test
  public void testGetNotExisting() throws Exception {
    Item item = itemManager.get("item1");
    Assert.assertNull(item);
  }

  @Test
  public void testGet() throws Exception {
    Item toBeReturned = new Item();
    toBeReturned.setId("itemId");
    doReturn(toBeReturned).when(itemDao).get(any(Item.class));

    Item item = itemManager.get("itemId");
    Assert.assertEquals(item.getId(), "itemId");
  }

  @Test
  public void testCreate() throws Exception {
    SessionContextProviderFactory.getInstance().createInterface().create("user1");

    Item toBeReturned = new Item();
    toBeReturned.setId("itemId");
    doReturn(toBeReturned).when(itemDao).create(any(Item.class));

    Item item = itemManager.create(createItem("item1", "A"));
    Assert.assertEquals(item.getId(), "itemId");
  }

  @Test
  public void testUpdateNotExistingVersionStatus() throws Exception {
    itemManager.updateVersionStatus("itemId", VersionStatus.Certified, VersionStatus.Draft);
    verify(itemDao, never()).update(any(Item.class));
  }

  @Test
  public void testUpdateVersionStatusWhenNone() throws Exception {
    Item item = new Item();
    item.setId("itemId");
    doReturn(item).when(itemDao).get(any(Item.class));

    itemManager.updateVersionStatus("itemId", VersionStatus.Certified, VersionStatus.Draft);
    verify(itemDao).update(item);
    Assert.assertEquals(item.getVersionStatusCounters().get(VersionStatus.Certified).intValue(), 1);
    Assert.assertNull(item.getVersionStatusCounters().get(VersionStatus.Draft));
  }

  @Test
  public void testUpdateVersionStatus() throws Exception {
    Item item = new Item();
    item.setId("itemId");
    item.getVersionStatusCounters().put(VersionStatus.Certified, 2);
    item.getVersionStatusCounters().put(VersionStatus.Draft, 5);
    doReturn(item).when(itemDao).get(any(Item.class));

    itemManager.updateVersionStatus("itemId", VersionStatus.Certified, VersionStatus.Draft);
    verify(itemDao).update(item);
    Assert.assertEquals(item.getVersionStatusCounters().get(VersionStatus.Certified).intValue(), 3);
    Assert.assertEquals(item.getVersionStatusCounters().get(VersionStatus.Draft).intValue(), 4);
  }

  private Item createItem(String name, String type) {
    Item item = new Item();
    item.setId(name);
    item.setName(name);
    item.setType(type);
    return item;
  }

}