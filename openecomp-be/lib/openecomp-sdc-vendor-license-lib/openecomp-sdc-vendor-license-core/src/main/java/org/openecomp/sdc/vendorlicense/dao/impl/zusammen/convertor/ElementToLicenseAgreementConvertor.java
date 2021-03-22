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
import java.util.Map;
import java.util.stream.Collectors;
import org.openecomp.convertor.ElementConvertor;
import org.openecomp.sdc.vendorlicense.dao.types.ChoiceOrOther;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseTerm;

public class ElementToLicenseAgreementConvertor extends ElementConvertor {

    @Override
    public LicenseAgreementEntity convert(Element element) {
        if (element == null) {
            return null;
        }
        return mapElementToLicenseAgreementEntity(element);
    }

    @Override
    public LicenseAgreementEntity convert(ElementInfo elementInfo) {
        if (elementInfo == null) {
            return null;
        }
        return mapElementInfoToLicenseAgreementEntity(elementInfo);
    }

    private LicenseAgreementEntity mapElementToLicenseAgreementEntity(Element element) {
        LicenseAgreementEntity licenseAgreement = new LicenseAgreementEntity();
        licenseAgreement.setId(element.getElementId().getValue());
        mapInfoToLicenseAgreementEntity(licenseAgreement, element.getInfo());
        mapRelationsToLicenseAgreementEntity(licenseAgreement, element.getRelations());
        return licenseAgreement;
    }

    private LicenseAgreementEntity mapElementInfoToLicenseAgreementEntity(ElementInfo elementInfo) {
        LicenseAgreementEntity licenseAgreement = new LicenseAgreementEntity();
        licenseAgreement.setId(elementInfo.getId().getValue());
        mapInfoToLicenseAgreementEntity(licenseAgreement, elementInfo.getInfo());
        mapRelationsToLicenseAgreementEntity(licenseAgreement, elementInfo.getRelations());
        return licenseAgreement;
    }

    private void mapRelationsToLicenseAgreementEntity(LicenseAgreementEntity licenseAgreementEntity, Collection<Relation> relations) {
        if (relations != null && relations.size() > 0) {
            licenseAgreementEntity
                .setFeatureGroupIds(relations.stream().map(relation -> relation.getEdge2().getElementId().getValue()).collect(Collectors.toSet()));
        }
    }

    private void mapInfoToLicenseAgreementEntity(LicenseAgreementEntity licenseAgreement, Info info) {
        licenseAgreement.setName(info.getName());
        licenseAgreement.setDescription(info.getDescription());
        licenseAgreement.setLicenseTerm(getCoiceOrOther(info.getProperty("licenseTerm")));
        licenseAgreement.setRequirementsAndConstrains(info.getProperty("requirementsAndConstrains"));
    }

    private ChoiceOrOther<LicenseTerm> getCoiceOrOther(Map licenseTerm) {
        return new ChoiceOrOther<>(LicenseTerm.valueOf((String) licenseTerm.get("choice")), (String) licenseTerm.get("other"));
    }
}
