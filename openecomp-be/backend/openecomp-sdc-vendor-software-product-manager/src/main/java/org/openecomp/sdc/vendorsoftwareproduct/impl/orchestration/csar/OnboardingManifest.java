/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder.getErrorWithParameters;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.CSARConstants.METADATA_MF_ATTRIBUTE;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.CSARConstants.SEPERATOR_MF_ATTRIBUTE;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.CSARConstants.SOURCE_MF_ATTRIBUTE;

public class OnboardingManifest {
    private static final Logger LOGGER = LoggerFactory.getLogger(OnboardingManifest.class);
    private Map<String, String> metadata;
    private List<String> sources;
    private List<String> errors;
    private State state;
    private enum State {
        START, PROCESS_METADATA, PROCESS_SOURCES, ERROR
    }

    public OnboardingManifest(InputStream is) {
        errors = new ArrayList<>();
        sources = new ArrayList<>();
        metadata = new HashMap<>();
        parseManifest(is);
    }

    private void parseManifest(InputStream is) {
        try {
            ImmutableList<String> lines = readAllLines(is);
            state = State.START;

            for (String line : lines) {
                line = line.trim();
                if (!StringUtils.isEmpty(line.trim())) {
                    state = processLine(state, line);
                }
            }
            if (errors.isEmpty()) {
                if (metadata.isEmpty()) {
                    errors.add(Messages.MANIFEST_NO_METADATA.getErrorMessage());
                }
                if (sources.isEmpty()) {
                    errors.add(Messages.MANIFEST_NO_SOURCES.getErrorMessage());
                }
            }
        } catch (IOException e){
            LOGGER.error(e.getMessage(),e);
            errors.add(Messages.MANIFEST_PARSER_INTERNAL.getErrorMessage());
        }
    }

    private State processLine(State state, String line) {
        State newState = state;
        switch (state) {
            case START:
                newState = startProcessLine(state, line);
                break;
            case PROCESS_METADATA:
                newState = processMetaData(state, line);
                break;
            case PROCESS_SOURCES:
                processSourceLine(line);

                break;
            case ERROR:
                break;
            default:
        }
        return newState;
    }

    private State startProcessLine(State state, String line) {
        State newState = state;
        if (line.trim().equals(METADATA_MF_ATTRIBUTE + SEPERATOR_MF_ATTRIBUTE)) {
            newState = State.PROCESS_METADATA;
        } else {
            reportError(line);
        }
        return newState;
    }

    private State processMetaData(State state, String line) {
        State newState = state;
        String[] metaSplit = line.split(SEPERATOR_MF_ATTRIBUTE);
        if (metaSplit.length < 2){
            reportError(line);
            return state;
        }
        if (!metaSplit[0].equals(SOURCE_MF_ATTRIBUTE)){
            String value = line.replace(metaSplit[0] + SEPERATOR_MF_ATTRIBUTE, "").trim();
            metadata.put(metaSplit[0],value);
        } else {
            newState = State.PROCESS_SOURCES;
            processSourceLine(line);
        }
        return newState;
    }

    private void processSourceLine(String line) {
        if (line.startsWith(SOURCE_MF_ATTRIBUTE+SEPERATOR_MF_ATTRIBUTE)){
            String value = line.replaceAll(SOURCE_MF_ATTRIBUTE + SEPERATOR_MF_ATTRIBUTE, "").trim();
            sources.add(value);
        }else {
            reportError(line);
        }
    }

    private void reportError(String line) {
        errors.add(getErrorWithParameters(Messages.MANIFEST_INVALID_LINE.getErrorMessage(), line));
        state = State.ERROR;
    }

    private ImmutableList<String> readAllLines(InputStream is) throws IOException {
        ImmutableList.Builder<String> builder = ImmutableList.<String> builder();
        try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8.newDecoder());
             BufferedReader bufferedReader = new BufferedReader(reader);) {
            for (; ; ) {
                String line = bufferedReader.readLine();
                if (line == null)
                    break;
                builder.add(line);
            }
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
}
