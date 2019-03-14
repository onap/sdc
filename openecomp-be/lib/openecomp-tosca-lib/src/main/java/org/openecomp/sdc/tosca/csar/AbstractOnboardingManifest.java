/*
 * Copyright © 2016-2017 European Support Limited
 * Modification Copyright (C) 2019 Nordix Foundation.
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

package org.openecomp.sdc.tosca.csar;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder.getErrorWithParameters;
import static org.openecomp.sdc.tosca.csar.CSARConstants.METADATA_MF_ATTRIBUTE;
import static org.openecomp.sdc.tosca.csar.CSARConstants.SEPERATOR_MF_ATTRIBUTE;

 abstract class AbstractOnboardingManifest implements Manifest{

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractOnboardingManifest.class);
    protected Map<String, String> metadata;
    protected List<String> sources;
    protected List<String> errors;
    protected Map<String, List<String>> nonManoSources;

    protected AbstractOnboardingManifest() {
        errors = new ArrayList<>();
        sources = new ArrayList<>();
        metadata = new HashMap<>();
        nonManoSources = new HashMap<>();
    }

    @Override
    public void parse(InputStream is) {
        try {
            ImmutableList<String> lines = readAllLines(is);
            processManifest(lines);
        } catch (IOException e){
            LOGGER.error(e.getMessage(),e);
            errors.add(Messages.MANIFEST_PARSER_INTERNAL.getErrorMessage());
        }
    }

    protected void processManifest(ImmutableList<String> lines) {
        if (isEmptyManifest(lines)){
            return;
        }
        Iterator<String> iterator = lines.iterator();
        //SOL004 #4.3.2: The manifest file shall start with the package metadata
        String line = iterator.next();
        if (!isMetadata(line)) {
            return;
        }
        //handle metadata
        processMetadata(iterator);
        if (errors.isEmpty() && metadata.isEmpty()) {
                errors.add(Messages.MANIFEST_NO_METADATA.getErrorMessage());
        }
    }

    protected abstract void processMetadata(Iterator<String> iterator);

     protected boolean isEmptyLine(Iterator<String> iterator, String line) {
         if(line.isEmpty()){
             processMetadata(iterator);
             return true;
         }
         return false;
     }

     protected boolean isInvalidLine(String line, String[] metaSplit) {
         if (metaSplit.length < 2){
             reportError(line);
             return true;
         }
         return false;
     }

     protected boolean isMetadata(String line) {
         if(line.trim().equals(METADATA_MF_ATTRIBUTE + SEPERATOR_MF_ATTRIBUTE)){
             return true;
         }
         reportError(line);
         return false;
     }

     protected boolean isEmptyManifest(ImmutableList<String> lines) {
         if(lines == null || lines.isEmpty()){
             errors.add(Messages.MANIFEST_EMPTY.getErrorMessage());
             return true;
         }
         return false;
     }

    protected void reportError(String line) {
        errors.add(getErrorWithParameters(Messages.MANIFEST_INVALID_LINE.getErrorMessage(), line));
    }

    protected ImmutableList<String> readAllLines(InputStream is) throws IOException {
        if(is == null){
            throw new IOException("Input Stream cannot be null!");
        }
        ImmutableList.Builder<String> builder = ImmutableList.<String> builder();
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8.newDecoder()))) {
            bufferedReader.lines().forEach(builder::add);
        }
        return builder.build();
    }

    public Map<String, String> getMetadata() {
        if (!isValid()){
            return Collections.emptyMap();
        }
        return ImmutableMap.copyOf(metadata);
    }

    public List<String> getSources() {
        if (!isValid()){
            return Collections.emptyList();
        }
        return ImmutableList.copyOf(sources);
    }

    public List<String> getErrors() {
        return  ImmutableList.copyOf(errors);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public Map<String, List<String>> getNonManoSources() {
        if (!isValid()){
            return Collections.emptyMap();
        }
        return ImmutableMap.copyOf(nonManoSources);
    }
}
