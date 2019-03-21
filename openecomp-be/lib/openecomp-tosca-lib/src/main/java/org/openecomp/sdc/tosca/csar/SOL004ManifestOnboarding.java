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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.openecomp.sdc.tosca.csar.CSARConstants.ALGORITHM_MF_ATTRIBUTE;
import static org.openecomp.sdc.tosca.csar.CSARConstants.CMD_END;
import static org.openecomp.sdc.tosca.csar.CSARConstants.CMS_BEGIN;
import static org.openecomp.sdc.tosca.csar.CSARConstants.HASH_MF_ATTRIBUTE;
import static org.openecomp.sdc.tosca.csar.CSARConstants.NON_MANO_MF_ATTRIBUTE;
import static org.openecomp.sdc.tosca.csar.CSARConstants.SEPERATOR_MF_ATTRIBUTE;
import static org.openecomp.sdc.tosca.csar.CSARConstants.SOURCE_MF_ATTRIBUTE;

public class SOL004ManifestOnboarding extends AbstractOnboardingManifest {

    @Override
    protected void processMetadata(Iterator<String> iterator) {
        if(!iterator.hasNext()){
            return;
        }
        String line = iterator.next();
        if(isEmptyLine(iterator, line)){
            return;
        }
        String[] metaSplit = line.split(SEPERATOR_MF_ATTRIBUTE);
        if (isInvalidLine(line, metaSplit)) {
            return;
        }
        if (!metaSplit[0].equals(SOURCE_MF_ATTRIBUTE) && !metaSplit[0].equals(NON_MANO_MF_ATTRIBUTE)){
            String value = line.substring((metaSplit[0] + SEPERATOR_MF_ATTRIBUTE).length()).trim();
            metadata.put(metaSplit[0].trim(),value.trim());
            processMetadata(iterator);
        } else {
            processSourcesAndNonManoSources(iterator, line);
        }
    }

    private void processSourcesAndNonManoSources(Iterator<String> iterator, String prevLine) {
        if(prevLine.isEmpty()){
            if(iterator.hasNext()){
                processSourcesAndNonManoSources(iterator, iterator.next());
            }
        } else if(prevLine.startsWith(SOURCE_MF_ATTRIBUTE+SEPERATOR_MF_ATTRIBUTE)){
            processSource(iterator, prevLine);
        }
        else if(prevLine.startsWith(ALGORITHM_MF_ATTRIBUTE + SEPERATOR_MF_ATTRIBUTE) ||
                prevLine.startsWith(HASH_MF_ATTRIBUTE + SEPERATOR_MF_ATTRIBUTE)){
            processSourcesAndNonManoSources(iterator, iterator.next());
        }else if(prevLine.startsWith(CMS_BEGIN)){
            String line = iterator.next();
            while(iterator.hasNext() && !line.contains(CMD_END)){
               line = iterator.next();
            }
            processSourcesAndNonManoSources(iterator, iterator.next());
        }
        else if(prevLine.startsWith(NON_MANO_MF_ATTRIBUTE+SEPERATOR_MF_ATTRIBUTE)){
            //non mano should be the last bit in manifest file,
            // all sources after non mano will be placed to the last non mano
            // key, if any other structure met error reported
            processNonManoInputs(iterator, iterator.next());
        }else{
            reportError(prevLine);
        }
    }

    private void processSource(Iterator<String> iterator, String prevLine) {
        String value = prevLine.substring((SOURCE_MF_ATTRIBUTE + SEPERATOR_MF_ATTRIBUTE).length()).trim();
        sources.add(value);
        if(iterator.hasNext()) {
            processSourcesAndNonManoSources(iterator, iterator.next());
        }
    }

    private void processNonManoInputs(Iterator<String> iterator, String prevLine) {
        if(prevLine.trim().equals(SOURCE_MF_ATTRIBUTE + SEPERATOR_MF_ATTRIBUTE)){
            reportError(prevLine);
            return;
        }
        if(!prevLine.contains(SEPERATOR_MF_ATTRIBUTE)){
            reportError(prevLine);
            return;
        }

        String[] metaSplit = prevLine.trim().split(SEPERATOR_MF_ATTRIBUTE);
        if (metaSplit.length > 1){
            reportError(prevLine);
            return;
        }
        int index = prevLine.indexOf(':');
        if(index > 0){
            prevLine = prevLine.substring(0, index);
        }
        processNonManoSource(iterator, prevLine, new ArrayList<>());

    }

    private void processNonManoSource(Iterator<String> iterator, String key, List<String> sources) {
        if(!iterator.hasNext()){
            return;
        }
        String line = iterator.next();
        if(line.isEmpty()){
            processNonManoSource(iterator, key, sources);
        }else if(line.trim().startsWith(SOURCE_MF_ATTRIBUTE + SEPERATOR_MF_ATTRIBUTE)){
            String value = line.replace(SOURCE_MF_ATTRIBUTE + SEPERATOR_MF_ATTRIBUTE, "").trim();
            sources.add(value);
            processNonManoSource(iterator, key, sources);
        }else {
            processNonManoInputs(iterator, line);
        }
        nonManoSources.put(key.trim(), sources);
    }
}