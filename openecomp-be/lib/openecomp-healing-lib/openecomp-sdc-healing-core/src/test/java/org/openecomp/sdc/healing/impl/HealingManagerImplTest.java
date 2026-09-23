/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
 */
package org.openecomp.sdc.healing.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openecomp.sdc.common.session.SessionContext;
import org.openecomp.sdc.common.session.SessionContextProvider;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.datatypes.model.ItemType;
import org.openecomp.sdc.healing.dao.HealingDao;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.healing.types.HealerType;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionState;

@ExtendWith(MockitoExtension.class)
class HealingManagerImplTest {

    private static final String ITEM_ID = "item-id";
    private static final String USER = "user";
    private static final String TENANT = "tenant";

    @Mock
    private VersioningManager versioningManager;
    @Mock
    private HealingDao healingDao;

    private final SessionContextProvider sessionContextProvider = SessionContextProviderFactory.getInstance().createInterface();
    private HealingManagerImpl healingManager;

    @BeforeEach
    void setUp() {
        sessionContextProvider.create(USER, TENANT);
        when(healingDao.getItemHealingFlag(anyString(), anyString(), anyString())).thenReturn(Optional.empty());
        healingManager = new HealingManagerImpl(versioningManager, healingDao) {
            @Override
            Map<String, Collection<String>> getItemHealers(ItemType itemType) {
                return Map.of(HealerType.structure.name(), List.of(StubHealer.class.getName()));
            }
        };
    }

    @AfterEach
    void tearDown() {
        sessionContextProvider.close();
    }

    @Test
    void restoresSessionWhenForceSyncFails() {
        Version version = new Version("version-id");
        doAnswer(invocation -> {
            assertSession(USER + "_healer");
            throw new IllegalStateException("forceSync failed");
        }).when(versioningManager).forceSync(ITEM_ID, version);

        assertThrows(IllegalStateException.class, () -> healingManager.healItemVersion(ITEM_ID, version, ItemType.vsp, true));

        assertSession(USER);
    }

    @Test
    void restoresSessionWhenPublishFails() {
        Version version = new Version("version-id");
        when(versioningManager.get(ITEM_ID, version)).thenReturn(dirtyVersion());
        doThrow(new IllegalStateException("publish failed")).when(versioningManager).publish(any(), any(), any());

        assertThrows(IllegalStateException.class, () -> healingManager.healItemVersion(ITEM_ID, version, ItemType.vsp, true));

        assertSession(USER);
    }

    @Test
    void restoresSessionAfterSuccessfulPublicHealing() {
        Version version = new Version("version-id");
        when(versioningManager.get(ITEM_ID, version)).thenReturn(dirtyVersion());

        healingManager.healItemVersion(ITEM_ID, version, ItemType.vsp, true);

        assertSession(USER);
    }

    private void assertSession(String expectedUser) {
        SessionContext sessionContext = sessionContextProvider.get();
        assertEquals(expectedUser, sessionContext.getUser().getUserId());
        assertEquals(TENANT, sessionContext.getTenant());
    }

    private static Version dirtyVersion() {
        VersionState state = new VersionState();
        state.setDirty(true);
        Version publicVersion = new Version("version-id");
        publicVersion.setState(state);
        return publicVersion;
    }

    public static class StubHealer implements Healer {

        @Override
        public boolean isHealingNeeded(String itemId, Version version) {
            return true;
        }

        @Override
        public void heal(String itemId, Version version) {
        }
    }
}
