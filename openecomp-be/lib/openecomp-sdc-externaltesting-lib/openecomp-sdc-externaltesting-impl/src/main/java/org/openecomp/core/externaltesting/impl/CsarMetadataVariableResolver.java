/*
 * Copyright © 2019 iconectiv
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

package org.openecomp.core.externaltesting.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.core.externaltesting.api.CsarMetadataContentItem;
import org.openecomp.core.externaltesting.api.TestExecutionRequestItem;
import org.openecomp.core.externaltesting.api.TestParameterValue;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.VspManagerFactory;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The CSAR Metadata variable resolver is resposible for processing of
 * variables in the test request.  It looks for variables with the "csar:" prefix
 * and extracts the contents of the uploaded CSAR file for a VSP.
 */
public class CsarMetadataVariableResolver implements VariableResolver {

  private Logger logger = LoggerFactory.getLogger(CsarMetadataVariableResolver.class);

  public static final String VSP_ID = "vspId";
  public static final String VSP_VERSION = "vspVersion";
  public static final String CSAR_PREFIX = "csar:";

  private VersioningManager versioningManager;
  private VendorSoftwareProductManager vendorSoftwareProductManager;
  private OrchestrationTemplateCandidateManager candidateManager;

  public CsarMetadataVariableResolver(VersioningManager versioningManager,
                                      VendorSoftwareProductManager vendorSoftwareProductManager,
                                      OrchestrationTemplateCandidateManager candidateManager) {
    this();
    this.versioningManager = versioningManager;
    this.vendorSoftwareProductManager = vendorSoftwareProductManager;
    this.candidateManager = candidateManager;
  }

  public CsarMetadataVariableResolver() {

  }

  @Override
  public void init() {
    if (versioningManager == null) {
      versioningManager = VersioningManagerFactory.getInstance().createInterface();
    }
    if (vendorSoftwareProductManager == null) {
      vendorSoftwareProductManager =
          VspManagerFactory.getInstance().createInterface();
    }
    if (candidateManager == null) {
      candidateManager =
          OrchestrationTemplateCandidateManagerFactory.getInstance().createInterface();
    }
  }

  @Override
  public boolean resolvesVariablesForRequest(TestExecutionRequestItem requestItem) {
    boolean hasVspId = false;
    boolean hasVersion = false;
    boolean hasCsarFileReference = false;

    if (requestItem.getParameterValues() != null) {
      for (TestParameterValue pv : requestItem.getParameterValues()) {
        if (VSP_ID.equals(pv.getId())) {
          hasVspId = true;
        } else if (VSP_VERSION.equals(pv.getId())) {
          hasVersion = true;
        }
        else if (StringUtils.startsWith(pv.getId(), CSAR_PREFIX)) {
          hasCsarFileReference = true;
        }
      }
    }
    return hasVersion && hasVspId && hasCsarFileReference;
  }

  @Override
  public void resolve(TestExecutionRequestItem requestItem) {
    logger.debug("run variable resolver...");
    String vspId = null;
    String version = null;
    if (requestItem.getParameterValues() != null) {
      for (TestParameterValue pv : requestItem.getParameterValues()) {
        if (VSP_ID.equals(pv.getId())) {
          vspId = pv.getValue();
        } else if (VSP_VERSION.equals(pv.getId())) {
          version = pv.getValue();
        }
      }
    }

    try {
      extractMetadata(requestItem, vspId, version);
    }
    catch (IOException ex) {
      logger.error("metadata extraction failed", ex);
    }
  }

  /**
   * Extract the metadata from the VSP CSAR file.
   * @param requestItem item to add metadata to for processing
   * @param vspId VSP identifier
   * @param version VSP version
   */
  @SuppressWarnings("WeakerAccess")
  protected void extractMetadata(TestExecutionRequestItem requestItem, String vspId, String version) throws IOException {

    List<Version> versions = versioningManager.list(vspId);
    if (logger.isDebugEnabled()) {
      logger.debug("known versions for {} are {}", vspId,
          versions.stream().map(v -> v.getId() + " " + v.getMajor() + "." + v.getMinor()).collect(Collectors.joining(",")));
    }

    Optional<Pair<String, byte[]>> ozip = candidateManager.get(vspId, new Version(version));
    if (!ozip.isPresent()) {
      ozip = vendorSoftwareProductManager.get(vspId, new Version((version)));
    }

    if (!ozip.isPresent()) {
      logger.warn("no zip found matching {} {}", vspId, version);
      return;
    }

    // safe here to do get.
    Pair<String, byte[]> zip = ozip.get();
    processArchive(requestItem, zip.getRight());
  }

  @SuppressWarnings("WeakerAccess")
  protected void processArchive(TestExecutionRequestItem requestItem, byte[] zip) {
    try {
      ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(zip));
      ZipEntry entry;
      while ((entry = zipStream.getNextEntry()) != null) {
        String entryName = entry.getName();
        logger.debug("csar contains entry {}", entryName);
        requestItem.getParameterValues().forEach(p -> {
          if (p.getId().equals(CSAR_PREFIX + entryName)) {
            try {
              if (requestItem.getContentItems() == null) {
                requestItem.setContentItems(new ArrayList<>());
              }
              CsarMetadataContentItem ci = new CsarMetadataContentItem();
              ci.setFilename(p.getId().substring(5));
              ci.setContent(IOUtils.toByteArray(zipStream));
              requestItem.getContentItems().add(ci);
            } catch (IOException ex) {
              logger.error("failed to read zip entry content for {}", entryName, ex);
            }
          }
        });
      }
      zipStream.close();
    } catch (IOException ex) {
      logger.error("IO Exception parsing zip", ex);
    }
  }
}
