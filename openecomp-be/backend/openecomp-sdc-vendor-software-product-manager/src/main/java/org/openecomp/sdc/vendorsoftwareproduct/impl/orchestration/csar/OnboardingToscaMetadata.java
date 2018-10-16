package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.common.errors.CoreException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder.getErrorWithParameters;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.CSARConstants.*;

public class OnboardingToscaMetadata {

    private static final Logger LOGGER = LoggerFactory.getLogger(OnboardingManifest.class);
    private List<String> errors;
    private String entryDefinitionsPath;

    public OnboardingToscaMetadata(InputStream is) {

        parseToscaMetadataFile(is);

    }

    private void parseToscaMetadataFile(InputStream st) {

        try {
            ImmutableList<String> meta_lines = readAllLines(st);

            for (String line : meta_lines) {
                line = line.trim();
                if (line.startsWith(TOSCA_META_ENTRY_DEFINITIONS + SEPERATOR_MF_ATTRIBUTE)) {

                    entryDefinitionsPath = line.replaceAll(TOSCA_META_ENTRY_DEFINITIONS + SEPERATOR_MF_ATTRIBUTE, "")
                            .trim();
                    break;

                }
            }

        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RuntimeException("Invalid TOSCA Metadata file", e);

        }

    }

    private ImmutableList<String> readAllLines(InputStream is) throws IOException {
        ImmutableList.Builder<String> builder = ImmutableList.<String>builder();
        try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8.newDecoder());
             BufferedReader bufferedReader = new BufferedReader(reader);) {
            for (;;) {
                String line = bufferedReader.readLine();
                if (line == null)
                    break;
                builder.add(line);
            }
        }
        return builder.build();
    }

    public String getEntryDefinitionsPath() {
        return entryDefinitionsPath;
    }
}

