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
package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import static org.openecomp.sdc.tosca.csar.CSARConstants.ETSI_VERSION_2_7_1;
import static org.openecomp.sdc.tosca.csar.ToscaMetadataFileInfo.TOSCA_META_PATH_FILE_NAME;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.stream.Collectors;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.tosca.csar.OnboardingToscaMetadata;
import org.openecomp.sdc.tosca.csar.ToscaMetadata;
import org.openecomp.sdc.vendorsoftwareproduct.services.impl.etsi.ETSIService;
import org.openecomp.sdc.vendorsoftwareproduct.services.impl.etsi.ETSIServiceImpl;

public class ValidatorFactory {

    private final ServiceLoader<Validator> validatorLoader;

    public ValidatorFactory() {
        this.validatorLoader = ServiceLoader.load(Validator.class);
    }

    /**
     * Returns a validator based on the contents of the csar package.
     *
     * @param fileContentHandler the csar package
     * @return Validator based on the contents of the csar package provided
     * @throws IOException when metafile is invalid
     */
    public Validator getValidator(final FileContentHandler fileContentHandler) throws IOException {
        final ETSIService etsiService = new ETSIServiceImpl(null);
        if (!etsiService.isSol004WithToscaMetaDirectory(fileContentHandler)) {
            final Map<String, byte[]> templates = fileContentHandler.getFiles();
            if (templates.containsKey(TOSCA_META_PATH_FILE_NAME)) {
                final ToscaMetadata metadata =
                    OnboardingToscaMetadata.parseToscaMetadataFile(fileContentHandler.getFileContentAsStream(TOSCA_META_PATH_FILE_NAME));
                return etsiService.isOnapPackage(metadata) ? new ONAPCsarValidator() : new EtsiSol004Version251Validator();
            }
            return new ONAPCsarValidator();
        }
        if (!etsiService.getHighestCompatibleSpecificationVersion(fileContentHandler).isLowerThan(ETSI_VERSION_2_7_1)) {
            if (etsiService.hasCnfEnhancements(fileContentHandler)) {
                return new SOL004Version4MetaDirectoryValidator();
            }
            return new SOL004Version3MetaDirectoryValidator();
        }
        return new SOL004MetaDirectoryValidator();
    }

    /**
     * Get validators based on the given model.
     *
     * @param model the model
     * @return a list containing all validators for the given model, empty otherwise.
     */
    public List<Validator> getValidators(final String model) {
        return validatorLoader.stream()
            .map(Provider::get)
            .filter(validator -> validator.appliesTo(model))
            .sorted(Comparator.comparingInt(Validator::getOrder))
            .collect(Collectors.toList());
    }
}
