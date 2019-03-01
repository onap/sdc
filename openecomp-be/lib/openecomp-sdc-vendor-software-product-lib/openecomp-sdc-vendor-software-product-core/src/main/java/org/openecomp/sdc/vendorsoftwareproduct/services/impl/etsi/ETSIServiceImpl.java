/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019, Nordix Foundation. All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct.services.impl.etsi;

import org.apache.commons.io.IOUtils;
import org.onap.sdc.tosca.parser.utils.YamlToObjectConverter;
import org.openecomp.core.utilities.file.FileContentHandler;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.openecomp.sdc.tosca.csar.Manifest;

import static org.openecomp.sdc.tosca.csar.CSARConstants.*;

public class ETSIServiceImpl implements ETSIService {

    private Configuration configuration;

    public ETSIServiceImpl() throws IOException {
        InputStream io = getClass().getClassLoader().getResourceAsStream("nonManoConfig.yaml");
        if(io == null){
            throw new IOException("Non Mano configuration not found");
        }
        String data = IOUtils.toString(io, StandardCharsets.UTF_8);
        YamlToObjectConverter yamlToObjectConverter = new YamlToObjectConverter();
        configuration = yamlToObjectConverter.convertFromString(data, Configuration.class);
    }

    public ETSIServiceImpl(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public boolean isSol004WithToscaMetaDirectory(FileContentHandler handler) {
        Map<String, byte[]> templates = handler.getFiles();
        return isMetaFilePresent(templates) && hasMetaMandatoryEntries(templates);
    }

    @Override
    public void moveNonManoFileToArtifactFolder(FileContentHandler handler, Manifest manifest) {
        for (Map.Entry<String, List<String>> entry : manifest.getNonManoSources().entrySet()) {
            String e = entry.getKey();
            List<String> k = entry.getValue();
            updateNonManoLocation(handler, e, k);
        }
    }

    private void updateNonManoLocation(FileContentHandler handler, String nonManoKey, List<String> sources) {
        Map<String, byte[]> files = handler.getFiles();
        for (String key : sources) {
            if (files.containsKey(key)) {
                updateLocation(key, nonManoKey, files);
            }
        }
    }

    private void updateLocation(String key, String nonManoKey, Map<String, byte[]> files){
        if (nonManoKey == null || nonManoKey.isEmpty()) {
            return;
        }
        Map<String, NonManoType> map = configuration.getNonManoKeyFolderMapping();
        if (map.containsKey(nonManoKey)) {
            NonManoType nonManoPair = map.get(nonManoKey);
            String newLocation = nonManoPair.getType() + "/" +
                    nonManoPair.getLocation() + "/" + getFileName(key);
            if (!files.containsKey(newLocation)) {
                files.put(newLocation, files.remove(key));
            }
        }
    }


    private String getFileName(String key) {
        return key.substring(key.lastIndexOf('/') + 1);
    }

    private boolean hasMetaMandatoryEntries(Map<String, byte[]> templates) {
        Optional<byte[]> meta = templates.entrySet().stream().filter(e -> e.getKey().equals(TOSCA_META_PATH_FILE_NAME)
                || e.getKey().equals(TOSCA_META_ORIG_PATH_FILE_NAME)).findFirst().map(Map.Entry::getValue);
        if (!meta.isPresent()) {
            return false;
        }
        String metaContent = new String(meta.get(), StandardCharsets.UTF_8);
        return metaContent.contains(TOSCA_META_ENTRY_DEFINITIONS) && metaContent.contains(TOSCA_META_ENTRY_MANIFEST)
                && metaContent.contains(TOSCA_META_ENTRY_CHANGE_LOG);
    }

    private boolean isMetaFilePresent(Map<String, byte[]> handler) {
        return handler.containsKey(TOSCA_META_PATH_FILE_NAME) || handler.containsKey(TOSCA_META_ORIG_PATH_FILE_NAME);
    }
}
