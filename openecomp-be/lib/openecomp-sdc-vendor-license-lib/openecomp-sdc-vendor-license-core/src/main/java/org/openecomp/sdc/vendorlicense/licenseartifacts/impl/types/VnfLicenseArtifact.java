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

package org.openecomp.sdc.vendorlicense.licenseartifacts.impl.types;

import com.ctc.wstx.api.WstxInputProperties;
import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupModel;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.xml.LimitXml;
import org.openecomp.sdc.vendorlicense.licenseartifacts.impl.types.mixins.MixinEntitlementPoolEntityForVnfArtifact;
import org.openecomp.sdc.vendorlicense.licenseartifacts.impl.types.mixins.MixinFeatureGroupModel;
import org.openecomp.sdc.vendorlicense.licenseartifacts.impl.types.mixins.MixinLicenseKeyGroupEntityForVnfArtifact;
import org.openecomp.sdc.vendorlicense.licenseartifacts.impl.types.mixins.MixinLimitArtifact;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

@JacksonXmlRootElement(localName = "vf-license-model",
    namespace = "http://xmlns.openecomp.org/asdc/license-model/1.0")
public class VnfLicenseArtifact extends XmlArtifact {
  @JsonProperty(value = "vendor-name")
  String vendorName;
  @JsonProperty(value = "vf-id")
  String vspId;
  List<FeatureGroupModel> featureGroups = new ArrayList<>();

  public String getVspId() {
    return vspId;
  }

  public void setVspId(String vspId) {
    this.vspId = vspId;
  }

  public String getVendorName() {
    return vendorName;
  }

  public void setVendorName(String vendorName) {
    this.vendorName = vendorName;
  }

  @JacksonXmlProperty(isAttribute = false, localName = "feature-group")
  @JacksonXmlElementWrapper(localName = "feature-group-list")
  public List<FeatureGroupModel> getFeatureGroups() {
    return featureGroups;
  }

  public void setFeatureGroups(List<FeatureGroupModel> featureGroups) {
    this.featureGroups = featureGroups;
  }

  void initMapper() {
    WstxOutputFactory wstxOutputFactory = new WstxOutputFactory() {
      @Override
      public XMLStreamWriter createXMLStreamWriter(Writer writer) throws XMLStreamException {
        mConfig.setProperty(WstxInputProperties.P_RETURN_NULL_FOR_DEFAULT_NAMESPACE, true);
        return super.createXMLStreamWriter(writer);
      }
    };
    XmlFactory factory = new XmlFactory(new WstxInputFactory(), wstxOutputFactory);

    xmlMapper = new XmlMapper(factory);


    xmlMapper.addMixIn(EntitlementPoolEntity.class, MixinEntitlementPoolEntityForVnfArtifact.class);
    xmlMapper.addMixIn(LicenseKeyGroupEntity.class, MixinLicenseKeyGroupEntityForVnfArtifact.class);
    xmlMapper.addMixIn(FeatureGroupModel.class, MixinFeatureGroupModel.class);
    xmlMapper.addMixIn(LimitXml.class, MixinLimitArtifact.class);
  }
}
