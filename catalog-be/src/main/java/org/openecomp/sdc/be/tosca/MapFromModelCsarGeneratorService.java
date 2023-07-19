/*
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2023 Nordix Foundation. All rights reserved.
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
package org.openecomp.sdc.be.tosca;

import fj.data.Either;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.plugins.CsarZipGenerator;
import org.openecomp.sdc.exception.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MapFromModelCsarGeneratorService {
    private final Map<String, CsarZipGenerator> servicesByModel;

    @Autowired
    public MapFromModelCsarGeneratorService(List<CsarZipGenerator> modelServices) {
        servicesByModel = modelServices.stream()
            .collect(Collectors.toMap(CsarZipGenerator::getModel, Function.identity()));
    }

    public Either<ZipOutputStream, ResponseFormat> generateCsarZip(
        final Component component,
        boolean getFromCS,
        ZipOutputStream zip,
        boolean isInCertificationRequest,
        String createdBy,
        String fileName,
        boolean isAsdPackage
    ) throws IOException {
        CsarZipGenerator generatorImpl = servicesByModel.get(component.getModel());

        if (null == generatorImpl) {
            generatorImpl = servicesByModel.get(null);
        }

        return generatorImpl.generateCsarZip(component, getFromCS, zip, isInCertificationRequest, createdBy,
            fileName, isAsdPackage);
    }
}
