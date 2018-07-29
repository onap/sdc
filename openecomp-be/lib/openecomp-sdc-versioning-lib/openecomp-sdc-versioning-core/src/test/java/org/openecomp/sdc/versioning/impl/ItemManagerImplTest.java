package org.openecomp.sdc.versioning.impl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.itempermissions.PermissionsServices;
import org.openecomp.sdc.notification.services.SubscriptionService;
import org.openecomp.sdc.versioning.dao.ItemDao;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.ItemStatus;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

public class ItemManagerImplTest {

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
    private ItemManagerImpl itemManager;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @AfterMethod
    public void tearDown(){
        itemManager = null;
    }

    @Test
    public void ArchiveTest(){

        Item item  = createItem(ITEM_ID,ITEM_NAME,ITEM_TYPE_A);
        itemManager.archive(item);

        verify(itemDao).update(item);
        assertEquals(item.getStatus(), ItemStatus.ARCHIVED);
    }

    @Test(expectedExceptions = CoreException.class,expectedExceptionsMessageRegExp =
            "Archive item failed, item .* is already Archived")
    public void ArchiveTestNegative(){

        Item item  = createItem(ITEM_ID,ITEM_NAME,ITEM_TYPE_B);
        item.setStatus(ItemStatus.ARCHIVED);
        itemManager.archive(item);

    }

    @Test
    public void RestoreTest(){

        Item item  = createItem(ITEM_ID,ITEM_NAME,ITEM_TYPE_A);
        item.setStatus(ItemStatus.ARCHIVED);
        itemManager.restore(item);

        verify(itemDao).update(item);
        assertEquals(item.getStatus(), ItemStatus.ACTIVE);
    }

    @Test(expectedExceptions = CoreException.class,expectedExceptionsMessageRegExp =
            "Restore item failed, item .* is already Active")
    public void RestoreTestNegative(){

        Item item  = createItem(ITEM_ID,ITEM_NAME,ITEM_TYPE_B);
        item.setStatus(ItemStatus.ACTIVE);
        itemManager.restore(item);

    }


    private Item createItem(String id, String name, String type) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setType(type);
        return item;
    }
}
