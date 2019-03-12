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
import org.openecomp.sdc.common.errors.Messages;
import java.util.Iterator;

import static org.openecomp.sdc.tosca.csar.CSARConstants.SEPERATOR_MF_ATTRIBUTE;
import static org.openecomp.sdc.tosca.csar.CSARConstants.SOURCE_MF_ATTRIBUTE;

public class ONAPManifestOnboarding extends AbstractOnboardingManifest implements Manifest {

    @Override
    protected void processManifest(ImmutableList<String> lines) {
        super.processManifest(lines);
        if (errors.isEmpty() && sources.isEmpty()) {
                errors.add(Messages.MANIFEST_NO_SOURCES.getErrorMessage());
        }
    }

    @Override
    protected void processMetadata(Iterator<String> iterator) {
        if(!iterator.hasNext()){
            return;
        }
        String line = iterator.next();
        if(isEmptyLine(iterator, line)) return;
        String[] metaSplit = line.split(SEPERATOR_MF_ATTRIBUTE);
        if (isInvalidLine(line, metaSplit)) return;
        if (!metaSplit[0].equals(SOURCE_MF_ATTRIBUTE)){
            String value = line.substring((metaSplit[0] + SEPERATOR_MF_ATTRIBUTE).length()).trim();
            metadata.put(metaSplit[0].trim(),value.trim());
            processMetadata(iterator);
        }if(metaSplit[0].startsWith(SOURCE_MF_ATTRIBUTE)){
            String value = line.substring((metaSplit[0] + SEPERATOR_MF_ATTRIBUTE).length()).trim();
            sources.add(value);
            processMetadata(iterator);
        }
    }

}
