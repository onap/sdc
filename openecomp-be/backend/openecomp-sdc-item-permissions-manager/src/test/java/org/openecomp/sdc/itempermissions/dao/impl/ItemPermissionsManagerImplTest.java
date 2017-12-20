package org.openecomp.sdc.itempermissions.dao.impl;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.itempermissions.PermissionsServices;
import org.openecomp.sdc.notification.services.NotificationPropagationManager;
import org.openecomp.sdc.notification.services.SubscriptionService;
import org.openecomp.sdc.versioning.ItemManager;
import org.openecomp.sdc.versioning.types.Item;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/**
 * Created by ayalaben on 7/6/2017
 */
public class ItemPermissionsManagerImplTest {

  private static final String ITEM1_ID = "1";
  private static final String PERMISSION = "Contributor";
  private static final String ACTION = "Change_Item_Permissions";
  private static final String USER = "user";
  private static final String AFFECTED_USER1 = "affected_user1";
  private static final String AFFECTED_USER2 = "affected_user2";
  private static final String AFFECTED_USER3 = "affected_user3";

  @Mock
  private PermissionsServices permissionsServicesMock;
  @Mock
  private ItemManager itemManagerMock;
  @Mock
  private SubscriptionService subscriptionServiceMock;
  @Mock
  private NotificationPropagationManager notifierMock;
  @InjectMocks
  private ItemPermissionsManagerImpl permissionsManager;

  @BeforeMethod
  public void setUp() throws Exception {
    SessionContextProviderFactory.getInstance().createInterface().create(USER);
    MockitoAnnotations.initMocks(this);
  }

  @Test(expectedExceptions = CoreException.class, expectedExceptionsMessageRegExp = "Permissions " +
      "Error. The user does not have permission to perform this action.")
  public void testUpdateItemPermissionsWhenNotAllowed() {
    doReturn(false).when(permissionsServicesMock).isAllowed(ITEM1_ID, USER, ACTION);

    permissionsManager
        .updateItemPermissions(ITEM1_ID, PERMISSION, Collections.singleton(AFFECTED_USER1),
            new HashSet<>());
  }

  @Test
  public void testUpdateItemPermissions() {
    doReturn(true).when(permissionsServicesMock).isAllowed(ITEM1_ID, USER, ACTION);
    Item item = new Item();
    item.setName("Item 1 Name");
    doReturn(item).when(itemManagerMock).get(ITEM1_ID);

    Set<String> addedUsersIds =
        Stream.of(AFFECTED_USER1, AFFECTED_USER2).collect(Collectors.toSet());
    Set<String> removedUsersIds = Collections.singleton(AFFECTED_USER3);
    permissionsManager
        .updateItemPermissions(ITEM1_ID, PERMISSION, addedUsersIds, removedUsersIds);

    verify(permissionsServicesMock)
        .updateItemPermissions(ITEM1_ID, PERMISSION, addedUsersIds, removedUsersIds);
    verify(subscriptionServiceMock).subscribe(AFFECTED_USER1, ITEM1_ID);
    verify(subscriptionServiceMock).subscribe(AFFECTED_USER2, ITEM1_ID);
    verify(subscriptionServiceMock).unsubscribe(AFFECTED_USER3, ITEM1_ID);

    // TODO: 12/18/2017 verify notification
  }
}