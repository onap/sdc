/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 Nokia Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.validation.impl.validators;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.FileData.Type;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.heat.services.manifest.ManifestUtil;
import org.openecomp.sdc.validation.util.ValidationUtil;

class GlobalContextUtil {

    private GlobalContextUtil() {
    }

    static Set<String> findFilesByType(GlobalValidationContext globalContext, Type type) {
        if (isManifestMissing(globalContext)) {
            return Set.of();
        }
        Map<String, FileData.Type> filesWithTypes = readAllFilesWithTypes(globalContext);
        return filterFilesByType(filesWithTypes, entry -> entry.getValue().equals(type));
    }

    private static boolean isManifestMissing(GlobalValidationContext globalContext) {
        return globalContext.getFileContent("MANIFEST.json").isEmpty();
    }

    private static Set<String> filterFilesByType(Map<String, FileData.Type> filesWithTypes,
        Predicate<Map.Entry<String, FileData.Type>> typePredicate) {
        return filesWithTypes.entrySet().stream().filter(typePredicate).map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    private static Map<String, FileData.Type> readAllFilesWithTypes(GlobalValidationContext globalContext) {
        ManifestContent manifestContent = ValidationUtil.validateManifest(globalContext);
        return ManifestUtil.getFileTypeMap(manifestContent);
    }

}
