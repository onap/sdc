/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.versioning.dao.impl.zusammen.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Item;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.sdc.versioning.dao.impl.zusammen.VersionZusammenDaoImpl;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;

public class ItemVersionToVersionConvertor extends ElementConvertor {

    @Override
    public Object convert(Element element) {
        return null;
    }

    @Override
    public Object convert(Item item) {
        return null;
    }

    @Override
    public Object convert(ElementInfo elementInfo) {
        return null;
    }

    @Override
    public Version convert(ItemVersion itemVersion) {
        if (itemVersion == null) {
            return null;
        }
        Version version = Version.valueOf(itemVersion.getData().getInfo().getProperty(VersionZusammenDaoImpl.ZusammenProperty.LABEL));
        version.setStatus(VersionStatus.valueOf(itemVersion.getData().getInfo().getProperty(VersionZusammenDaoImpl.ZusammenProperty.STATUS)));
        version.setName(itemVersion.getData().getInfo().getName());
        version.setDescription(itemVersion.getData().getInfo().getDescription());
        version.setId(itemVersion.getId().getValue());
        if (itemVersion.getBaseId() != null) {
            version.setBaseId(itemVersion.getBaseId().getValue());
        }
        version.setCreationTime(itemVersion.getCreationTime());
        version.setModificationTime(itemVersion.getModificationTime());
        return version;
    }
}
