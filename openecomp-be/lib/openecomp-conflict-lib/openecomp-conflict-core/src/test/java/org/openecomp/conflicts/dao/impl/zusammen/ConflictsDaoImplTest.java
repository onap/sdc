/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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
package org.openecomp.conflicts.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementConflict;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementConflictInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ItemVersionConflict;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.ItemVersionDataConflict;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.openecomp.conflicts.types.Conflict;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.types.ElementPropertyName;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ConflictsDaoImplTest {

    private static final String ITEM_ID = "itemId";
    private static final String CONFLICT_ID = "conflictId";
    private static final Object PROPERTY = "itemVersion";

    @Mock
    private ZusammenAdaptor zusammenAdaptor;
    @Mock
    private Version version;
    @Mock
    private Supplier<SessionContext> sessionContextSupplier;
    @Mock
    private SessionContext sessionContext;
    @Mock
    private ItemVersionConflict itemVersionConflict;
    @Mock
    private ItemVersionDataConflict versionDataConflict;
    @Mock
    private Collection<ElementConflictInfo> conflictsInfos;
    @Mock
    private Element element;
    @Mock
    private Info info;


    @Test
    public void shouldItemBeConflicted() {
        ConflictsDaoImpl conflictsDao = new ConflictsDaoImpl(zusammenAdaptor, sessionContextSupplier);
        Mockito.when(sessionContextSupplier.get()).thenReturn(sessionContext);
        Mockito.when(zusammenAdaptor
            .getVersionConflict(Mockito.any(SessionContext.class), Mockito.any(Id.class), Mockito.any(Id.class)))
            .thenReturn(itemVersionConflict);
        Mockito.when(itemVersionConflict.getVersionDataConflict()).thenReturn(versionDataConflict);
        Mockito.when(itemVersionConflict.getElementConflictInfos()).thenReturn(conflictsInfos);

        boolean conflicted = conflictsDao.isConflicted(ITEM_ID, version);
        Assert.assertTrue(conflicted);
    }

    @Test
    public void shouldItemBeNotConflicted() {
        ConflictsDaoImpl conflictsDao = new ConflictsDaoImpl(zusammenAdaptor, sessionContextSupplier);
        Mockito.when(sessionContextSupplier.get()).thenReturn(sessionContext);
        Mockito.when(zusammenAdaptor
            .getVersionConflict(Mockito.any(SessionContext.class), Mockito.any(Id.class), Mockito.any(Id.class)))
            .thenReturn(itemVersionConflict);
        Mockito.when(itemVersionConflict.getVersionDataConflict()).thenReturn(null);

        boolean conflicted = conflictsDao.isConflicted(ITEM_ID, version);
        Assert.assertFalse(conflicted);
    }

    @Test
    public void shouldGetItemVersionConflict() {
        ConflictsDaoImpl conflictsDao = new ConflictsDaoImpl(zusammenAdaptor, sessionContextSupplier);
        Mockito.when(sessionContextSupplier.get()).thenReturn(sessionContext);
        Mockito.when(zusammenAdaptor
            .getVersionConflict(Mockito.any(SessionContext.class), Mockito.any(Id.class), Mockito.any(Id.class)))
            .thenReturn(itemVersionConflict);
        Mockito.when(itemVersionConflict.getVersionDataConflict()).thenReturn(versionDataConflict);
        Mockito.when(itemVersionConflict.getElementConflictInfos()).thenReturn(conflictsInfos);
        org.openecomp.conflicts.types.ItemVersionConflict conflict = conflictsDao.getConflict(ITEM_ID, version);
        Assert.assertNotNull(conflict);
    }

    @Test
    public void shouldGetConflict() {
        ConflictsDaoImpl conflictsDao = new ConflictsDaoImpl(zusammenAdaptor, sessionContextSupplier);
        Mockito.when(sessionContextSupplier.get()).thenReturn(sessionContext);
        Optional<ElementConflict> elementConflict = Optional.ofNullable(getElementConflict());
        Mockito.when(zusammenAdaptor
            .getElementConflict(Mockito.any(SessionContext.class), Mockito.any(ElementContext.class),
                Mockito.any(Id.class))).thenReturn(elementConflict);
        Mockito.when(itemVersionConflict.getVersionDataConflict()).thenReturn(versionDataConflict);
        Mockito.when(itemVersionConflict.getElementConflictInfos()).thenReturn(conflictsInfos);
        Conflict conflict = conflictsDao.getConflict(ITEM_ID, version, CONFLICT_ID);
        Assert.assertNotNull(conflict);
    }

    private ElementConflict getElementConflict() {
        Mockito.when(element.getInfo()).thenReturn(info);
        Mockito.when(info.getProperty(ElementPropertyName.elementType.name())).thenReturn(PROPERTY);

        ElementConflict elementConflict = new ElementConflict();
        elementConflict.setLocalElement(element);
        return elementConflict;
    }
}