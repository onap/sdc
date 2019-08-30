/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.tosca.csar;

import com.google.common.collect.ImmutableList;
import java.util.Optional;
import org.openecomp.sdc.common.errors.Messages;

public class ONAPManifestOnboarding extends AbstractOnboardingManifest {

    @Override
    protected void processManifest(ImmutableList<String> lines) {
        super.processManifest(lines);
        if (errors.isEmpty() && sources.isEmpty()) {
            errors.add(Messages.MANIFEST_NO_SOURCES.getErrorMessage());
        }
    }

    @Override
    protected void processMetadata() {
        Optional<String> currentLine = getCurrentLine();
        if (!currentLine.isPresent() || !isMetadata(currentLine.get())) {
            reportError(Messages.MANIFEST_START_METADATA);
            continueToProcess = false;
            return;
        }
        currentLine = readNextNonEmptyLine();

        while (currentLine.isPresent() && continueToProcess) {
            final String line = currentLine.get();
            final String entry = readEntryName(line).orElse(null);
            if (entry == null) {
                reportInvalidLine();
            }
            final String value = readEntryValue(line).orElse(null);
            if (value == null) {
                reportInvalidLine();
            }

            final ManifestTokenType tokenType = ManifestTokenType.parse(entry).orElse(null);
            if (tokenType == ManifestTokenType.SOURCE) {
                sources.add(value);
            } else {
                addToMetadata(entry, value);
                continueToProcess = isValid();
            }
            currentLine = readNextNonEmptyLine();
        }
    }

    @Override
    protected void processBody() {
        //no implementation
    }

}
