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

package org.openecomp.sdc.enrichment.impl.external.artifact;


import org.openecomp.core.enrichment.types.ArtifactCategory;
import org.openecomp.core.enrichment.types.InformationArtifactFolderNames;
import org.openecomp.core.model.dao.EnrichedServiceModelDao;
import org.openecomp.core.model.dao.EnrichedServiceModelDaoFactory;
import org.openecomp.core.model.types.ServiceArtifact;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.enrichment.EnrichmentInfo;
import org.openecomp.sdc.enrichment.inter.ExternalArtifactEnricherInterface;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductConstants;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.factory.InformationArtifactGeneratorFactory;
import org.openecomp.sdc.vendorsoftwareproduct.informationArtifact.InformationArtifactGenerator;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Talio on 11/24/2016
 */
public class VspInformationArtifactEnricher implements ExternalArtifactEnricherInterface {
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private static InformationArtifactGenerator informationArtifactGenerator =
      InformationArtifactGeneratorFactory.getInstance().createInterface();
  private EnrichedServiceModelDao enrichedServiceModelDao =
      EnrichedServiceModelDaoFactory.getInstance().createInterface();
  private VendorSoftwareProductInfoDao vspInfoDao = VendorSoftwareProductInfoDaoFactory
      .getInstance().createInterface();

  public VspInformationArtifactEnricher() {
  }

  public Map<String, List<ErrorMessage>> enrich(EnrichmentInfo enrichmentInfo)
      throws IOException {

    String vspId = enrichmentInfo.getKey();
    Version version = enrichmentInfo.getVersion();
    Map<String, List<ErrorMessage>> errors = enrichInformationArtifact(vspId, version);

    return errors;
  }

  private Map<String, List<ErrorMessage>> enrichInformationArtifact(String vspId, Version version)
      throws IOException {


    mdcDataDebugMessage.debugEntryMessage(null);

    Map<String, List<ErrorMessage>> errors = new HashMap<>();
    ByteBuffer infoArtifactByteBuffer = ByteBuffer.wrap(informationArtifactGenerator.generate(
        vspId, version).getBytes());

    if (Objects.isNull(infoArtifactByteBuffer)) {
      List<ErrorMessage> errorList = new ArrayList<>();
      errorList.add(new ErrorMessage(ErrorLevel.ERROR, String.format(
          "Cannot enrich information artifact for vendor software product with id %s and version %s",
          vspId, version.toString())));
      //TODO: add error to map (what is the key?)

      mdcDataDebugMessage.debugExitMessage(null);
      return errors;
    }

    enrichInformationArtifact(vspId, version, infoArtifactByteBuffer);

    mdcDataDebugMessage.debugExitMessage(null);
    return errors;
  }

  private void enrichInformationArtifact(String vspId, Version version,
                                         ByteBuffer infoArtifactByteBuffer) {
    ServiceArtifact infoArtifactServiceArtifact = new ServiceArtifact();

    String vspName = vspInfoDao.get(new VspDetails(vspId, version)).getName();

    infoArtifactServiceArtifact.setVspId(vspId);
    infoArtifactServiceArtifact.setVersion(version);
    infoArtifactServiceArtifact
        .setName(ArtifactCategory.INFORMATIONAL.getDisplayName() + File.separator
            + InformationArtifactFolderNames.Guide + File.separator + String.format(
            VendorSoftwareProductConstants
                .INFORMATION_ARTIFACT_NAME,
            vspName));
    infoArtifactServiceArtifact.setContentData(infoArtifactByteBuffer.array());

    enrichedServiceModelDao.storeExternalArtifact(infoArtifactServiceArtifact);

  }

}
