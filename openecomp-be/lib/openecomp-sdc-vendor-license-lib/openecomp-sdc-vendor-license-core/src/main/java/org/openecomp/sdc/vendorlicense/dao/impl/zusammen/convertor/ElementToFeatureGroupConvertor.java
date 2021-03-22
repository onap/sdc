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

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Info;
import com.amdocs.zusammen.datatypes.item.Relation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.RelationType;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;

public class ElementToFeatureGroupConvertor extends ElementConvertor {

    @Override
    public FeatureGroupEntity convert(Element element) {
        if (element == null) {
            return null;
        }
        return mapElementToFeatureGroupEntity(element);
    }

    @Override
    public FeatureGroupEntity convert(ElementInfo elementInfo) {
        if (elementInfo == null) {
            return null;
        }
        return mapElementInfoToFeatureGroupEntity(elementInfo);
    }

    private FeatureGroupEntity mapElementToFeatureGroupEntity(Element element) {
        FeatureGroupEntity featureGroup = new FeatureGroupEntity();
        featureGroup.setId(element.getElementId().getValue());
        mapInfoToFeatureGroup(featureGroup, element.getInfo());
        mapRelationsToFeatureGroup(featureGroup, element.getRelations());
        return featureGroup;
    }

    private FeatureGroupEntity mapElementInfoToFeatureGroupEntity(ElementInfo elementInfo) {
        FeatureGroupEntity featureGroup = new FeatureGroupEntity();
        featureGroup.setId(elementInfo.getId().getValue());
        mapInfoToFeatureGroup(featureGroup, elementInfo.getInfo());
        mapRelationsToFeatureGroup(featureGroup, elementInfo.getRelations());
        return featureGroup;
    }

    private void mapInfoToFeatureGroup(FeatureGroupEntity featureGroup, Info info) {
        featureGroup.setName(info.getName());
        featureGroup.setDescription(info.getDescription());
        featureGroup.setPartNumber(info.getProperty("partNumber"));
        featureGroup.setManufacturerReferenceNumber(info.getProperty("manufacturerReferenceNumber"));
    }

    private void mapRelationsToFeatureGroup(FeatureGroupEntity featureGroup, Collection<Relation> relations) {
        Set<String> entitlementPoolIds = new HashSet<>();
        Set<String> licenseAgreements = new HashSet<>();
        Set<String> licenseKeyGroupIds = new HashSet<>();
        if (relations != null) {
            for (Relation relation : relations) {
                if (RelationType.FeatureGroupToEntitlmentPool.name().equals(relation.getType())) {
                    entitlementPoolIds.add(relation.getEdge2().getElementId().getValue());
                } else if (RelationType.FeatureGroupToLicenseKeyGroup.name().equals(relation.getType())) {
                    licenseKeyGroupIds.add(relation.getEdge2().getElementId().getValue());
                } else if (RelationType.FeatureGroupToReferencingLicenseAgreement.name().equals(relation.getType())) {
                    licenseAgreements.add(relation.getEdge2().getElementId().getValue());
                }
            }
        }
        featureGroup.setEntitlementPoolIds(entitlementPoolIds);
        featureGroup.setLicenseKeyGroupIds(licenseKeyGroupIds);
        featureGroup.setReferencingLicenseAgreements(licenseAgreements);
    }
}
