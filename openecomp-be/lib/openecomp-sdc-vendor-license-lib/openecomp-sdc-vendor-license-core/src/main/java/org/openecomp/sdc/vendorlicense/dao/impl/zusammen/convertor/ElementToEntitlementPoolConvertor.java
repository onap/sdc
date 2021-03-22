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
package org.openecomp.sdc.vendorlicense.dao.impl.zusammen.convertor;

import static org.openecomp.sdc.vendorlicense.dao.impl.zusammen.VlmZusammenUtil.toInteger;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Relation;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolType;
import org.openecomp.sdc.vendorlicense.dao.types.MultiChoiceOrOther;
import org.openecomp.sdc.vendorlicense.dao.types.OperationalScope;
import org.openecomp.sdc.vendorlicense.dao.types.ThresholdUnit;

public class ElementToEntitlementPoolConvertor extends ElementConvertor {

    @Override
    public EntitlementPoolEntity convert(Element element) {
        if (element == null) {
            return null;
        }
        return mapElementToEntitlementPoolEntity(element);
    }

    @Override
    public EntitlementPoolEntity convert(ElementInfo elementInfo) {
        if (elementInfo == null) {
            return null;
        }
        return mapElementInfoToEntitlementPoolEntity(elementInfo);
    }

    private EntitlementPoolEntity mapElementToEntitlementPoolEntity(Element element) {
        EntitlementPoolEntity entitlementPool = new EntitlementPoolEntity();
        entitlementPool.setId(element.getElementId().getValue());
        mapInfoToEntitlementPoolEntity(entitlementPool, element.getInfo());
        mapRelationsToEntitlementPoolEntity(entitlementPool, element.getRelations());
        return entitlementPool;
    }

    private EntitlementPoolEntity mapElementInfoToEntitlementPoolEntity(ElementInfo elementInfo) {
        EntitlementPoolEntity entitlementPool = new EntitlementPoolEntity();
        entitlementPool.setId(elementInfo.getId().getValue());
        mapInfoToEntitlementPoolEntity(entitlementPool, elementInfo.getInfo());
        mapRelationsToEntitlementPoolEntity(entitlementPool, elementInfo.getRelations());
        return entitlementPool;
    }

    private void mapInfoToEntitlementPoolEntity(EntitlementPoolEntity entitlementPool, Info info) {
        entitlementPool.setName(info.getName());
        entitlementPool.setDescription(info.getDescription());
        entitlementPool.setVersionUuId(info.getProperty("version_uuid"));
        entitlementPool.setType(EntitlementPoolType.permissiveValueOf(info.getProperty("EntitlementPoolType")));
        entitlementPool.setThresholdValue(toInteger(info.getProperty("thresholdValue")));
        String thresholdUnit = info.getProperty("threshold_unit");
        entitlementPool.setThresholdUnit(thresholdUnit == null ? null : ThresholdUnit.valueOf(thresholdUnit));
        entitlementPool.setIncrements(info.getProperty("increments"));
        entitlementPool.setOperationalScope(getOperationalScopeMultiChoiceOrOther(info.getProperty("operational_scope")));
        entitlementPool.setStartDate(info.getProperty("startDate"));
        entitlementPool.setExpiryDate(info.getProperty("expiryDate"));
        entitlementPool.setManufacturerReferenceNumber(info.getProperty("manufacturerReferenceNumber"));
    }

    private void mapRelationsToEntitlementPoolEntity(EntitlementPoolEntity entitlementPool, Collection<Relation> relations) {
        if (relations != null && relations.size() > 0) {
            entitlementPool.setReferencingFeatureGroups(
                relations.stream().map(relation -> relation.getEdge2().getElementId().getValue()).collect(Collectors.toSet()));
        }
    }

    private MultiChoiceOrOther<OperationalScope> getOperationalScopeMultiChoiceOrOther(Map<String, Object> operationalScope) {
        if (operationalScope == null || operationalScope.isEmpty()) {
            return null;
        }
        Set<OperationalScope> choices = new HashSet<>();
        ((List<String>) operationalScope.get("choices")).forEach(choice -> choices.add(OperationalScope.valueOf(choice)));
        Object other = operationalScope.get("other");
        return new MultiChoiceOrOther<>(choices, other == null ? null : (String) other);
    }
}
