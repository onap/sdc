/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.vendorlicense.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDao;
import org.openecomp.sdc.vendorlicense.dao.types.ChoiceOrOther;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseTerm;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


public class LicenseAgreementDaoZusammenImpl implements LicenseAgreementDao {

  private ZusammenAdaptor zusammenAdaptor;

  public LicenseAgreementDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
    //no need
  }

  @Override
  public void create(LicenseAgreementEntity licenseAgreement) {
    ZusammenElement licenseAgreementElement =
        buildLicenseAgreementElement(licenseAgreement, Action.CREATE);
    ZusammenElement licenseAgreementsElement =
        VlmZusammenUtil.buildStructuralElement(StructureElement.LicenseAgreements, null);
    licenseAgreementsElement.addSubElement(licenseAgreementElement);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(licenseAgreement.getVendorLicenseModelId());
    Optional<Element> savedElement = zusammenAdaptor.saveElement(context, new ElementContext(itemId,
            VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor)),
        licenseAgreementsElement, "Create license agreement");
    savedElement.ifPresent(element -> licenseAgreement
        .setId(element.getSubElements().iterator().next().getElementId().getValue()));
  }

  @Override
  public void update(LicenseAgreementEntity licenseAgreement) {
    ZusammenElement licenseAgreementElement =
        buildLicenseAgreementElement(licenseAgreement, Action.UPDATE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(licenseAgreement.getVendorLicenseModelId());
    zusammenAdaptor.saveElement(context, new ElementContext(itemId,
            VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor)),
        licenseAgreementElement,
        String.format("Update license agreement with id %s", licenseAgreement.getId()));
  }

  @Override
  public LicenseAgreementEntity get(LicenseAgreementEntity licenseAgreement) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(licenseAgreement.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VlmZusammenUtil.getVersionTag(licenseAgreement.getVersion()));

    return zusammenAdaptor.getElementInfo(context, elementContext, new Id(licenseAgreement.getId()))
        .map(elementInfo -> mapElementInfoToLicenseAgreement(
            licenseAgreement.getVendorLicenseModelId(), licenseAgreement.getVersion(), elementInfo))
        .orElse(null);
  }

  @Override
  public void delete(LicenseAgreementEntity licenseAgreement) {
    SessionContext context = ZusammenUtil.createSessionContext();
    ZusammenElement zusammenElement = new ZusammenElement();
    zusammenElement.setAction(Action.DELETE);
    zusammenElement.setElementId(new Id(licenseAgreement.getId()));

    Id itemId = new Id(licenseAgreement.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));
    zusammenAdaptor.saveElement(context, elementContext, zusammenElement,
        "delete license agreement. id:" + licenseAgreement.getId() + ".");
  }


  @Override
  public Collection<LicenseAgreementEntity> list(LicenseAgreementEntity licenseAgreement) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(licenseAgreement.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VlmZusammenUtil.getVersionTag(licenseAgreement.getVersion()));

    return zusammenAdaptor
        .listElementsByName(context, elementContext, null,
            StructureElement.LicenseAgreements.name())
        .stream().map(elementInfo -> mapElementInfoToLicenseAgreement(
            licenseAgreement.getVendorLicenseModelId(), licenseAgreement.getVersion(), elementInfo))
        .collect(Collectors.toList());
  }

  @Override
  public long count(LicenseAgreementEntity licenseAgreement) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(licenseAgreement.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VlmZusammenUtil.getVersionTag(licenseAgreement.getVersion()));

    return zusammenAdaptor.listElementsByName(context, elementContext, null,
        StructureElement.LicenseAgreements.name())
        .size();
  }

  @Override
  public void deleteAll(LicenseAgreementEntity entity) {
    //not supported
  }

  @Override
  public void removeFeatureGroup(LicenseAgreementEntity licenseAgreement, String featureGroupId) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(licenseAgreement.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    Optional<ElementInfo> elementInfo = zusammenAdaptor.getElementInfo(context,
        elementContext, new Id(licenseAgreement.getId()));
    if (elementInfo.isPresent()) {
      ZusammenElement zusammenElement = VlmZusammenUtil.getZusammenElement(elementInfo.get());
      zusammenElement.setAction(Action.UPDATE);
      zusammenElement.setRelations(elementInfo.get().getRelations().stream()
          .filter(relation -> !featureGroupId.equals(relation.getEdge2().getElementId().getValue()))
          .collect(Collectors.toList()));
      zusammenAdaptor.saveElement(context, elementContext, zusammenElement, "remove feature group");
    }
  }

  @Override
  public void updateColumnsAndDeltaFeatureGroupIds(LicenseAgreementEntity licenseAgreement,
                                                   Set<String> addedFeatureGroupIds,
                                                   Set<String> removedFeatureGroupIds) {
    ZusammenElement licenseAgreementElement =
        buildLicenseAgreementElement(licenseAgreement, Action.UPDATE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(licenseAgreement.getVendorLicenseModelId());
    ElementContext elementContext = new ElementContext(itemId,
        VlmZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    Optional<ElementInfo> elementInfo =
        zusammenAdaptor.getElementInfo(context, elementContext, new Id(licenseAgreement.getId()));
    if (elementInfo.isPresent()) {
      LicenseAgreementEntity currentLicenseAgreement =
          mapElementInfoToLicenseAgreement(licenseAgreement.getId(), licenseAgreement.getVersion(),
              elementInfo.get());

      currentLicenseAgreement.getFeatureGroupIds().removeAll(removedFeatureGroupIds);
      currentLicenseAgreement.getFeatureGroupIds().addAll(addedFeatureGroupIds);
      licenseAgreementElement.setRelations(currentLicenseAgreement.getFeatureGroupIds().stream()
          .map(relation -> VlmZusammenUtil
              .createRelation(RelationType.LicenseAgreementToFeatureGroup, relation))
          .collect(Collectors.toList()));
      zusammenAdaptor.saveElement(context, elementContext, licenseAgreementElement,
          "update license agreement");
    }
  }

  private LicenseAgreementEntity mapElementInfoToLicenseAgreement(String vlmId, Version version,
                                                                  ElementInfo elementInfo) {
    LicenseAgreementEntity licenseAgreement =
        new LicenseAgreementEntity(vlmId, version, elementInfo.getId().getValue());
    licenseAgreement.setName(elementInfo.getInfo().getName());
    licenseAgreement.setDescription(elementInfo.getInfo().getDescription());

    licenseAgreement
        .setLicenseTerm(getCoiceOrOther(elementInfo.getInfo().getProperty("licenseTerm")));
    licenseAgreement.setRequirementsAndConstrains(
        elementInfo.getInfo().getProperty("requirementsAndConstrains"));
    if (elementInfo.getRelations() != null && elementInfo.getRelations().size() > 0) {
      licenseAgreement.setFeatureGroupIds(elementInfo.getRelations().stream()
          .map(relation -> relation.getEdge2().getElementId().getValue())
          .collect(Collectors.toSet()));
    }
    return licenseAgreement;
  }

  private ChoiceOrOther<LicenseTerm> getCoiceOrOther(Map licenseTerm) {
    return new ChoiceOrOther(LicenseTerm.valueOf((String) licenseTerm.get("choice")),
        (String) licenseTerm.get("other"));
  }


  private ZusammenElement buildLicenseAgreementElement(LicenseAgreementEntity licenseAgreement,
                                                       Action action) {
    ZusammenElement licenseAgreementElement = new ZusammenElement();
    licenseAgreementElement.setAction(action);
    if (licenseAgreement.getId() != null) {
      licenseAgreementElement.setElementId(new Id(licenseAgreement.getId()));
    }
    Info info = new Info();
    info.setName(licenseAgreement.getName());
    info.setDescription(licenseAgreement.getDescription());
    info.addProperty("licenseTerm", licenseAgreement.getLicenseTerm());
    info.addProperty("requirementsAndConstrains", licenseAgreement.getRequirementsAndConstrains());
    licenseAgreementElement.setInfo(info);

    if (licenseAgreement.getFeatureGroupIds() != null &&
        licenseAgreement.getFeatureGroupIds().size() > 0) {
      licenseAgreementElement.setRelations(licenseAgreement.getFeatureGroupIds().stream()
          .map(rel -> VlmZusammenUtil
              .createRelation(RelationType.LicenseAgreementToFeatureGroup, rel))
          .collect(Collectors.toList()));
    }
    return licenseAgreementElement;
  }
}
