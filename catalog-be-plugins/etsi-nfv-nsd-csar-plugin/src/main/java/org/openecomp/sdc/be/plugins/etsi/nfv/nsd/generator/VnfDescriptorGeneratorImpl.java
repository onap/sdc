/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.be.csar.storage.StorageFactory;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.builder.NsdToscaMetadataBuilder;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.exception.VnfDescriptorException;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model.VnfDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

/**
 * Implementation of a VNF Descriptor Generator
 */
@Component("vnfPackageGenerator")
public class VnfDescriptorGeneratorImpl implements VnfDescriptorGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(VnfDescriptorGeneratorImpl.class);
    private static final String SPACE_REGEX = "\\s+";
    private static final String CSAR = "csar";
    private static final String COLON = ":";
    private static final String EMPTY_STRING = "";
    private static final String SLASH = "/";
    private static final String DEFINITIONS_DIRECTORY = "Definitions";
    private static final String TOSCA_META_PATH = "TOSCA-Metadata/TOSCA.meta";

    private static boolean isACsarArtifact(final ArtifactDefinition definition) {
        return definition.getPayloadData() != null && definition.getArtifactName() != null
                && CSAR.equalsIgnoreCase(FilenameUtils.getExtension(definition.getArtifactName()));
    }

    public Optional<VnfDescriptor> generate(final String name, final ArtifactDefinition onboardedPackageArtifact) throws VnfDescriptorException {
        if (!isACsarArtifact(onboardedPackageArtifact)) {
            return Optional.empty();
        }
        final FileContentHandler fileContentHandler;
        try {
            final var artifactStorageManager = new StorageFactory().createArtifactStorageManager();
            final byte[] payloadData = onboardedPackageArtifact.getPayloadData();
            if (artifactStorageManager.isEnabled()) {
                final var inputStream =
                        artifactStorageManager.get(getFromPayload(payloadData, "bucket"), getFromPayload(payloadData, "object") + ".reduced");
                fileContentHandler = FileUtils.getFileContentMapFromZip(inputStream);
            } else {
                fileContentHandler = FileUtils.getFileContentMapFromZip(new ByteArrayInputStream(payloadData));
            }
        } catch (final IOException e) {
            final String errorMsg = String.format("Could not unzip artifact '%s' content", onboardedPackageArtifact.getArtifactName());
            throw new VnfDescriptorException(errorMsg, e);
        }
        if (MapUtils.isEmpty(fileContentHandler.getFiles())) {
            return Optional.empty();
        }
        final String mainDefinitionFile;
        try {
            mainDefinitionFile = getMainFilePathFromMetaFile(fileContentHandler).orElse(null);
        } catch (final IOException e) {
            final String errorMsg = String.format("Could not read main definition file of artifact '%s'", onboardedPackageArtifact.getArtifactName());
            throw new VnfDescriptorException(errorMsg, e);
        }
        LOGGER.debug("found main file: {}", mainDefinitionFile);
        if (mainDefinitionFile == null) {
            return Optional.empty();
        }
        final var vnfDescriptor = new VnfDescriptor();
        vnfDescriptor.setName(name);
        final String vnfdFileName = FilenameUtils.getName(mainDefinitionFile);
        vnfDescriptor.setVnfdFileName(vnfdFileName);
        vnfDescriptor.setDefinitionFiles(getFiles(fileContentHandler, mainDefinitionFile));
        vnfDescriptor.setNodeType(getNodeType(getFileContent(fileContentHandler, mainDefinitionFile)));
        return Optional.of(vnfDescriptor);
    }

    private String getFromPayload(final byte[] payload, final String name) {
        final String[] strings = new String(payload).split("\n");
        for (final String str : strings) {
            if (str.contains(name)) {
                return str.split(": ")[1];
            }
        }
        return "";
    }

    private Map<String, byte[]> getFiles(final FileContentHandler fileContentHandler, final String filePath) {
        final Map<String, byte[]> files = new HashMap<>();
        final byte[] fileContent = fileContentHandler.getFileContent(filePath);
        if (fileContent != null) {
            final String mainYmlFile = new String(fileContent);
            LOGGER.debug("file content: {}", mainYmlFile);
            files.put(appendDefinitionDirPath(filePath.substring(filePath.lastIndexOf(SLASH) + 1)), getVnfdAmendedForInclusionInNsd(fileContent));
            final List<Object> imports = getImportFilesPath(mainYmlFile);
            LOGGER.info("found imports {}", imports);
            for (final Object importObject : imports) {
                if (importObject != null) {
                    final String importFilename = importObject.toString();
                    final String importFileFullPath = appendDefinitionDirPath(importFilename);
                    final byte[] importFileContent = fileContentHandler.getFileContent(importFileFullPath);
                    files.put(appendDefinitionDirPath(importFilename), importFileContent);
                }
            }
        }
        return files;
    }

    private String getFileContent(final FileContentHandler fileContentHandler, final String filePath) {
        final byte[] fileContent = fileContentHandler.getFileContent(filePath);
        if (fileContent != null) {
            return new String(fileContent);
        }
        return null;
    }

    private Optional<String> getMainFilePathFromMetaFile(final FileContentHandler fileContentHandler) throws IOException {
        final Map<String, String> metaFileContent = getMetaFileContent(fileContentHandler);
        final String mainFile = metaFileContent.get(NsdToscaMetadataBuilder.ENTRY_DEFINITIONS);
        if (mainFile != null) {
            return Optional.of(mainFile.replaceAll(SPACE_REGEX, EMPTY_STRING));
        }
        LOGGER.error("{} entry not found in {}", NsdToscaMetadataBuilder.ENTRY_DEFINITIONS, TOSCA_META_PATH);
        return Optional.empty();
    }

    private Map<String, String> getMetaFileContent(final FileContentHandler fileContentHandler) throws IOException {
        final InputStream inputStream = fileContentHandler.getFileContentAsStream(TOSCA_META_PATH);
        if (inputStream == null) {
            throw new FileNotFoundException("Unable find " + TOSCA_META_PATH + " file");
        }
        final List<String> lines = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
        return lines.stream().map(str -> str.split(COLON)).collect(Collectors.toMap(str -> str[0], str -> str.length > 1 ? str[1] : EMPTY_STRING));
    }

    private String appendDefinitionDirPath(final String filename) {
        return DEFINITIONS_DIRECTORY + SLASH + filename;
    }

    private List<Object> getImportFilesPath(final String mainYmlFile) {
        final Map<Object, Object> fileContentMap = new YamlUtil().yamlToObject(mainYmlFile, Map.class);
        final Object importsObject = fileContentMap.get("imports");
        if (importsObject instanceof List) {
            return (List<Object>) importsObject;
        }
        return Collections.emptyList();
    }

    private byte[] getVnfdAmendedForInclusionInNsd(final byte[] vnfdFileContent) {
        final Yaml yaml = new Yaml();
        final Map<String, Object> toscaFileContent = (Map<String, Object>) yaml.load(new String(vnfdFileContent));
        toscaFileContent.remove("topology_template");
        removeInterfacesFromNodeTypes(toscaFileContent);
        return yaml.dumpAsMap(toscaFileContent).getBytes();
    }

    private void removeInterfacesFromNodeTypes(final Map<String, Object> toscaFileContent) {
        final Map<String, Object> nodeTypes = (Map<String, Object>) toscaFileContent.get("node_types");
        if (nodeTypes != null) {
            for (Entry<String, Object> nodeType : nodeTypes.entrySet()) {
                if (nodeType.getValue() != null && nodeType.getValue() instanceof Map) {
                    ((Map<String, Object>) nodeType.getValue()).remove("interfaces");
                }
            }
        }
    }

    private String getNodeType(final String mainYmlFile) {
        final Map<Object, Object> fileContentMap = new YamlUtil().yamlToObject(mainYmlFile, Map.class);
        final Object nodeTypesObject = fileContentMap.get("node_types");
        if (nodeTypesObject instanceof Map && ((Map<String, Object>) nodeTypesObject).size() == 1) {
            return ((Map<String, Object>) nodeTypesObject).keySet().iterator().next();
        }
        return null;
    }
}
