package org.openecomp.sdc.versioning.dao.impl.zusammen;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.item.Info;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.Item;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.openecomp.sdc.versioning.dao.impl.zusammen.TestUtil.createZusammenContext;

public class ItemZusammenDaoImplTest {

  private static final String USER = "user1";
  private static final String ITEM_TYPE = "item_type";
  private static final String ITEM_VERSIONS_STATUSES = "item_versions_statuses";
  private static final String APP_PROP_1 = "app_prop1";
  private static final String APP_PROP_2 = "app_prop2";

  @Mock
  private ZusammenAdaptor zusammenAdaptorMock;
  @InjectMocks
  private ItemZusammenDaoImpl itemDao;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    SessionContextProviderFactory.getInstance().createInterface().create(USER);
  }

  @Test
  public void testListWhenNone() throws Exception {
    doReturn(new ArrayList<>()).when(zusammenAdaptorMock)
        .listItems(eq(createZusammenContext(USER)));

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
    doReturn(returnedItems).when(zusammenAdaptorMock).listItems(eq(createZusammenContext(USER)));

    Collection<Item> items = itemDao.list();
    Assert.assertEquals(items.size(), 3);

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
        .getItem(eq(createZusammenContext(USER)), eq(new Id(inputItem.getId())));

    Item item = itemDao.get(inputItem);

    Assert.assertNotNull(item);
    assertItemEquals(item, toBeReturned);
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
    Assert.assertEquals(capturedInfo.getName(), inputItem.getName());
    Assert.assertEquals(capturedInfo.getDescription(), inputItem.getDescription());
    Assert.assertEquals(capturedInfo.getProperty(ITEM_TYPE), inputItem.getType());
    Assert.assertEquals(capturedInfo.getProperty(ITEM_VERSIONS_STATUSES),
        inputItem.getVersionStatusCounters());

    Assert.assertEquals(item.getId(), itemId);
    Assert.assertEquals(item.getName(), inputItem.getName());
    Assert.assertEquals(item.getDescription(), inputItem.getDescription());
    Assert.assertEquals(item.getType(), inputItem.getType());
    Assert.assertEquals(item.getVersionStatusCounters(), inputItem.getVersionStatusCounters());
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
    Assert.assertEquals(capturedInfo.getName(), item.getName());
    Assert.assertEquals(capturedInfo.getDescription(), item.getDescription());
    Assert.assertEquals(capturedInfo.getProperty(ITEM_TYPE), item.getType());
    Assert.assertEquals(capturedInfo.getProperty(ITEM_VERSIONS_STATUSES),
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
    info.addProperty(ITEM_TYPE, type);
    info.addProperty(ITEM_VERSIONS_STATUSES, versionStatusCounters);
    info.addProperty(APP_PROP_1, "app_prop1_value");
    info.addProperty(APP_PROP_2, 8);
    item.setInfo(info);
    item.setCreationTime(creationTime);
    item.setModificationTime(modificationTime);
    return item;
  }

  private void assertItemEquals(Item item, com.amdocs.zusammen.datatypes.item.Item zusammenItem) {
    Assert.assertEquals(item.getId(), zusammenItem.getId().getValue());
    Assert.assertEquals(item.getName(), zusammenItem.getInfo().getName());
    Assert.assertEquals(item.getDescription(), zusammenItem.getInfo().getDescription());
    Assert.assertEquals(item.getType(), zusammenItem.getInfo().getProperty(ITEM_TYPE));
    Assert.assertEquals(item.getProperties().get(APP_PROP_1),
        zusammenItem.getInfo().getProperty(APP_PROP_1));
    Assert.assertEquals(item.getProperties().get(APP_PROP_2),
        zusammenItem.getInfo().getProperty(APP_PROP_2));

    Map<String, Number> zusammenStatusesMap =
        zusammenItem.getInfo().getProperty(ITEM_VERSIONS_STATUSES);
    Map<VersionStatus, Integer> statusesMap = item.getVersionStatusCounters();

    zusammenStatusesMap.entrySet().forEach(entry -> Assert
        .assertEquals(statusesMap.get(VersionStatus.valueOf(entry.getKey())), entry.getValue()));

    Assert.assertEquals(item.getCreationTime(), zusammenItem.getCreationTime());
    Assert.assertEquals(item.getModificationTime(), zusammenItem.getModificationTime());
  }

}