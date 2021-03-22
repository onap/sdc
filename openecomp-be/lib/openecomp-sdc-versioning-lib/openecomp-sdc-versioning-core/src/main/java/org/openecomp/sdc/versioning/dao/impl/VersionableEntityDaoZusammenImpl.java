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
package org.openecomp.sdc.versioning.dao.impl;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import com.amdocs.zusammen.datatypes.itemversion.Tag;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.versioning.dao.VersionableEntityDao;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionableEntityMetadata;

public class VersionableEntityDaoZusammenImpl implements VersionableEntityDao {

    private ZusammenAdaptor zusammenAdaptor;

    public VersionableEntityDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
        this.zusammenAdaptor = zusammenAdaptor;
    }

    private static Comparator<ItemVersion> getVersionModificationTimeDescComparator() {
        return (o1, o2) -> Integer.compare(o2.getId().getValue().length(), o1.getId().getValue().length());
    }

    @Override
    public void initVersion(VersionableEntityMetadata versionableTableMetadata, String entityId, Version baseVersion, Version newVersion) {
        // redundant in zusammen impl.
    }

    @Override
    public void deleteVersion(VersionableEntityMetadata versionableTableMetadata, String entityId, Version versionToDelete, Version backToVersion) {
        SessionContext context = ZusammenUtil.createSessionContext();
        Id itemId = new Id(entityId);
        Id versionId = getItemVersionId(itemId, context);
        zusammenAdaptor.resetVersionHistory(context, itemId, versionId, backToVersion.toString());
    }

    @Override
    public void closeVersion(VersionableEntityMetadata versionableTableMetadata, String entityId, Version versionToClose) {
        SessionContext context = ZusammenUtil.createSessionContext();
        Id itemId = new Id(entityId);
        Id versionId = getItemVersionId(itemId, context);
        zusammenAdaptor.tagVersion(context, itemId, versionId, new Tag(versionToClose.toString(), null));
    }

    // TODO: 3/19/2017 move to a common util
    private Id getItemVersionId(Id itemId, SessionContext context) {
        Optional<ItemVersion> itemVersionOptional = getFirstVersion(context, itemId);
        ItemVersion itemVersion = itemVersionOptional
            .orElseThrow(() -> new RuntimeException(String.format("No version was found for item %s.", itemId)));
        return itemVersion.getId();
    }

    private Optional<ItemVersion> getFirstVersion(SessionContext context, Id itemId) {
        Collection<ItemVersion> versions = zusammenAdaptor.listPublicVersions(context, itemId);
        return CollectionUtils.isEmpty(versions) ? Optional.empty() : versions.stream().min(getVersionModificationTimeDescComparator());
    }
}
