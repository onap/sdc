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
import static org.mockito.Mockito.verify;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.itempermissions.PermissionsServices;
import org.openecomp.sdc.notification.services.SubscriptionService;
import org.openecomp.sdc.versioning.dao.ItemDao;
import org.openecomp.sdc.versioning.types.Item;
import org.openecomp.sdc.versioning.types.ItemStatus;

public class ItemManagerImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    @After
    public void tearDown() {
        itemManager = null;
    }

    @Test
    public void archiveTest() {

        Item item = createItem(ITEM_ID, ITEM_NAME, ITEM_TYPE_A);
        itemManager.archive(item);

        verify(itemDao).update(item);
        assertEquals(item.getStatus(), ItemStatus.ARCHIVED);
    }

    @Test
    public void archiveTestNegative() {

        expectedException.expect(CoreException.class);
        expectedException.expectMessage(new RegexMatcher("Archive item failed, item .* is already Archived"));

        Item item = createItem(ITEM_ID, ITEM_NAME, ITEM_TYPE_B);
        item.setStatus(ItemStatus.ARCHIVED);
        itemManager.archive(item);

    }

    @Test
    public void restoreTest() {

        Item item = createItem(ITEM_ID, ITEM_NAME, ITEM_TYPE_A);
        item.setStatus(ItemStatus.ARCHIVED);
        itemManager.restore(item);

        verify(itemDao).update(item);
        assertEquals(item.getStatus(), ItemStatus.ACTIVE);
    }

    @Test
    public void restoreTestNegative() {

        expectedException.expect(CoreException.class);
        expectedException.expectMessage(new RegexMatcher("Restore item failed, item .* is already Active"));

        Item item = createItem(ITEM_ID, ITEM_NAME, ITEM_TYPE_B);
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

    private static class RegexMatcher extends TypeSafeMatcher<String> {

        private final String regex;

        private RegexMatcher(String regex) {
            this.regex = regex;
        }

        @Override
        protected boolean matchesSafely(String s) {
            return s.matches(regex);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Regular expression: " + regex);
        }
    }
}
