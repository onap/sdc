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
    private static final Logger logger = LoggerFactory.getLogger(OnboardingManifest.class);
    private Map<String, String> metadata;
    private List<String> sources;
    private List<String> errors;
    private State state;
    private enum State {
        Start, ProcessMetadata, ProcessSources, Error
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
            state = State.Start;

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
            logger.error(e.getMessage(),e);
            errors.add(Messages.MANIFEST_PARSER_INTERNAL.getErrorMessage());
        }
    }

    private State processLine(State state, String line) {
        switch (state) {
            case Start:
                if (line.trim().equals(METADATA_MF_ATTRIBUTE + SEPERATOR_MF_ATTRIBUTE)) {
                    state = State.ProcessMetadata;
                } else {
                    reportError(line);
                }
                break;
            case ProcessMetadata:
                String[] metaSplit = line.split(SEPERATOR_MF_ATTRIBUTE);
                if (metaSplit.length < 2){
                    reportError(line);
                    break;
                }
                if (!metaSplit[0].equals(SOURCE_MF_ATTRIBUTE)){
                    String value = line.replace(metaSplit[0] + SEPERATOR_MF_ATTRIBUTE, "").trim();
                    metadata.put(metaSplit[0],value);
                } else {
                    state = State.ProcessSources;
                    processSourceLine(line);
                }
                break;
            case ProcessSources:
                processSourceLine(line);

                break;
            case Error:
                break;

        } return state;
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
        state = State.Error;
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
            return Collections.EMPTY_MAP;
        }
        return ImmutableMap.copyOf(metadata);
    }

    public List<String> getSources() {
        if (!isValid()){
            return Collections.EMPTY_LIST;
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
