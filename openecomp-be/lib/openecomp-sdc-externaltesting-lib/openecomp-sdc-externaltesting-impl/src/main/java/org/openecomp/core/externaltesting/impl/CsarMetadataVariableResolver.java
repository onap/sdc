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

import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.core.externaltesting.api.VtpTestExecutionRequest;
import org.openecomp.core.externaltesting.errors.ExternalTestingException;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.VspManagerFactory;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.MultiValueMap;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The CSAR Metadata variable resolver is responsible for processing of
 * variables in the test request.  It looks for variables with the "csar:" prefix
 * and extracts the contents of the uploaded CSAR file for a VSP.
 */
public class CsarMetadataVariableResolver implements VariableResolver {

  private Logger logger = LoggerFactory.getLogger(CsarMetadataVariableResolver.class);

  static final String VSP_ID = "vspId";
  static final String VSP_VERSION = "vspVersion";
  static final String CSAR_PREFIX = "csar:";

  private VersioningManager versioningManager;
  private VendorSoftwareProductManager vendorSoftwareProductManager;
  private OrchestrationTemplateCandidateManager candidateManager;

  CsarMetadataVariableResolver(VersioningManager versioningManager,
                                      VendorSoftwareProductManager vendorSoftwareProductManager,
                                      OrchestrationTemplateCandidateManager candidateManager) {
    this();
    this.versioningManager = versioningManager;
    this.vendorSoftwareProductManager = vendorSoftwareProductManager;
    this.candidateManager = candidateManager;
  }

  CsarMetadataVariableResolver() {

  }

  @PostConstruct
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
  public boolean resolvesVariablesForRequest(VtpTestExecutionRequest requestItem) {
    Map<String,String> params = requestItem.getParameters();

    // no params, quickly return.
    if (params == null) {
      return false;
    }

    // no match, quickly return
    if (!params.containsKey(VSP_ID) || !params.containsKey(VSP_VERSION)) {
      return false;
    }

    return (params.keySet().stream().anyMatch(s -> StringUtils.startsWith(s, CSAR_PREFIX)));
  }

  @Override
  public void resolve(VtpTestExecutionRequest requestItem, MultiValueMap<String, Object> body) {
    logger.debug("run {} variable resolver...", this.getClass().getSimpleName());
    Map<String,String> params = requestItem.getParameters();
    String vspId = params.get(VSP_ID);
    String version = params.get(VSP_VERSION);

    try {
      extractMetadata(requestItem, body, vspId, version);
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
  protected void extractMetadata(VtpTestExecutionRequest requestItem, MultiValueMap<String, Object> body, String vspId, String version) throws IOException {

    Version ver = new Version(version);
    logger.debug("attempt to retrieve archive for VSP {} version {}", vspId, ver.getId());

    Optional<Pair<String, byte[]>> ozip = candidateManager.get(vspId, new Version(version));
    if (!ozip.isPresent()) {
      ozip = vendorSoftwareProductManager.get(vspId, ver);
    }

    if (!ozip.isPresent()) {
      List<Version> versions = versioningManager.list(vspId);
      String knownVersions = versions
          .stream()
          .map(v -> String.format("%d.%d: %s (%s)", v.getMajor(), v.getMinor(), v.getStatus(), v.getId()))
          .collect(Collectors.joining("\n"));

      String detail = String.format("Unable to find archive for VSP ID %s and Version %s.  Known versions are:\n%s",
        vspId, version, knownVersions);

      throw new ExternalTestingException("Archive Processing Failed", 500, detail);
    }

    // safe here to do get.
    Pair<String, byte[]> zip = ozip.get();
    processArchive(requestItem, body, zip.getRight());
  }

  @EqualsAndHashCode(callSuper = false)
  private class NamedByteArrayResource extends ByteArrayResource {
    private String filename;
    private NamedByteArrayResource(byte[] bytes, String filename) {
      super(bytes, filename);
      this.filename = filename;
    }
    @Override
    public String getFilename() {
      return this.filename;
    }

  }

  @SuppressWarnings("WeakerAccess")
  protected void processArchive(VtpTestExecutionRequest requestItem, MultiValueMap<String, Object> body, byte[] zip) {
    try {
      ZipInputStream zipStream = new ZipInputStream(new ByteArrayInputStream(zip));
      ZipEntry entry;
      while ((entry = zipStream.getNextEntry()) != null) {
        String entryName = entry.getName();
        logger.debug("csar contains entry {}", entryName);
        Map<String,String> params = requestItem.getParameters();
          params.forEach((key,val) -> {
            if (key.startsWith(CSAR_PREFIX)) {
              addToBody(requestItem, body, zipStream, entryName, key);
            }
        });
      }
    } catch (IOException ex) {
      logger.error("IO Exception parsing zip", ex);
    }
  }

  private void addToBody(VtpTestExecutionRequest requestItem, MultiValueMap<String, Object> body, ZipInputStream zipStream, String entryName, String key) {
    String filename = key.substring(CSAR_PREFIX.length());
    logger.debug("match {} with {}", entryName, filename);
    if (StringUtils.equals(entryName, filename)) {
      try {
        NamedByteArrayResource res = new NamedByteArrayResource(IOUtils.toByteArray(zipStream), filename);
        body.add("file", res);

        // we've added the file to the body.   need to replace the value in the request for this
        // parameter to match the VTP requirement that it start with a file URL protocol handler.
        requestItem.getParameters().put(key, "file://" + entryName);

      } catch (IOException ex) {
        logger.error("failed to read zip entry content for {}", entryName, ex);
      }
    }
  }
}
