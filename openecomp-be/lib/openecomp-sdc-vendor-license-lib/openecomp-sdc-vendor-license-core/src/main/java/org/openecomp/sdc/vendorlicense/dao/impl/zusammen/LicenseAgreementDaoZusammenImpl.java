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
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorlicense.dao.LicenseAgreementDao;
import org.openecomp.sdc.vendorlicense.dao.impl.zusammen.convertor.ElementToLicenseAgreementConvertor;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.types.ElementPropertyName;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;


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
        buildStructuralElement(ElementType.LicenseAgreements, Action.IGNORE);
    licenseAgreementsElement.addSubElement(licenseAgreementElement);

    SessionContext context = createSessionContext();
    Element licenseAgreementsSavedElement = zusammenAdaptor.saveElement(context,
        new ElementContext(licenseAgreement.getVendorLicenseModelId(),
            licenseAgreement.getVersion().getId()), licenseAgreementsElement,
        "Create license agreement");
    licenseAgreement
        .setId(licenseAgreementsSavedElement.getSubElements().iterator().next().getElementId()
            .getValue());
  }

  @Override
  public void update(LicenseAgreementEntity licenseAgreement) {
    ZusammenElement licenseAgreementElement =
        buildLicenseAgreementElement(licenseAgreement, Action.UPDATE);

    SessionContext context = createSessionContext();
    zusammenAdaptor.saveElement(context,
        new ElementContext(licenseAgreement.getVendorLicenseModelId(),
            licenseAgreement.getVersion().getId()), licenseAgreementElement,
        String.format("Update license agreement with id %s", licenseAgreement.getId()));
  }

  @Override
  public LicenseAgreementEntity get(LicenseAgreementEntity licenseAgreement) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(licenseAgreement.getVendorLicenseModelId(),
        licenseAgreement.getVersion().getId());
    ElementToLicenseAgreementConvertor convertor = new ElementToLicenseAgreementConvertor();
    return zusammenAdaptor.getElementInfo(context, elementContext, new Id(licenseAgreement.getId()))
        .map(elementInfo -> {
          LicenseAgreementEntity entity = convertor.convert(elementInfo);
          entity.setVendorLicenseModelId(licenseAgreement.getVendorLicenseModelId());
          entity.setVersion(licenseAgreement.getVersion());
          return entity;
        })
        .orElse(null);
  }

  @Override
  public void delete(LicenseAgreementEntity licenseAgreement) {
    ZusammenElement zusammenElement = buildElement(new Id(licenseAgreement.getId()), Action.DELETE);

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(licenseAgreement.getVendorLicenseModelId(),
        licenseAgreement.getVersion().getId());
    zusammenAdaptor.saveElement(context, elementContext, zusammenElement,
        "delete license agreement. id:" + licenseAgreement.getId() + ".");
  }


  @Override
  public Collection<LicenseAgreementEntity> list(LicenseAgreementEntity licenseAgreement) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(licenseAgreement.getVendorLicenseModelId(),
        licenseAgreement.getVersion().getId());
    ElementToLicenseAgreementConvertor convertor = new ElementToLicenseAgreementConvertor();
    return zusammenAdaptor
        .listElementsByName(context, elementContext, null,
            ElementType.LicenseAgreements.name())
        .stream().map(elementInfo -> {
          LicenseAgreementEntity entity = convertor.convert(elementInfo);
          entity.setVendorLicenseModelId(licenseAgreement.getVendorLicenseModelId());
          entity.setVersion(licenseAgreement.getVersion());
          return entity;
        })
        .collect(Collectors.toList());
  }

  @Override
  public long count(LicenseAgreementEntity licenseAgreement) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(licenseAgreement.getVendorLicenseModelId(),
        licenseAgreement.getVersion().getId());

    return zusammenAdaptor.listElementsByName(context, elementContext, null,
        ElementType.LicenseAgreements.name())
        .size();
  }

  @Override
  public void deleteAll(LicenseAgreementEntity entity) {
    //not supported
  }

  @Override
  public void removeFeatureGroup(LicenseAgreementEntity licenseAgreement, String featureGroupId) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(licenseAgreement.getVendorLicenseModelId(),
        licenseAgreement.getVersion().getId());

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

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(licenseAgreement.getVendorLicenseModelId(),
        licenseAgreement.getVersion().getId());
    ElementToLicenseAgreementConvertor convertor = new ElementToLicenseAgreementConvertor();
    Optional<ElementInfo> elementInfo =
        zusammenAdaptor.getElementInfo(context, elementContext, new Id(licenseAgreement.getId()));
    if (elementInfo.isPresent()) {
      LicenseAgreementEntity currentLicenseAgreement =
          convertor.convert(elementInfo.get());
      currentLicenseAgreement.setVendorLicenseModelId(licenseAgreement.getVendorLicenseModelId());
      currentLicenseAgreement.setVersion(licenseAgreement.getVersion());
      if (!(removedFeatureGroupIds == null)) {
        currentLicenseAgreement.getFeatureGroupIds().removeAll(removedFeatureGroupIds);
      }

      if (!(addedFeatureGroupIds == null)) {
        currentLicenseAgreement.getFeatureGroupIds().addAll(addedFeatureGroupIds);
      }
      licenseAgreementElement.setRelations(currentLicenseAgreement.getFeatureGroupIds().stream()
          .map(relation -> VlmZusammenUtil
              .createRelation(RelationType.LicenseAgreementToFeatureGroup, relation))
          .collect(Collectors.toList()));
      zusammenAdaptor.saveElement(context, elementContext, licenseAgreementElement,
          "update license agreement");
    }
  }

  private ZusammenElement buildLicenseAgreementElement(LicenseAgreementEntity licenseAgreement,
                                                       Action action) {
    ZusammenElement licenseAgreementElement =
        buildElement(licenseAgreement.getId() == null ? null : new Id(licenseAgreement.getId()),
            action);
    Info info = new Info();
    info.setName(licenseAgreement.getName());
    info.setDescription(licenseAgreement.getDescription());
    info.addProperty(ElementPropertyName.elementType.name(), ElementType.LicenseAgreement);
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
