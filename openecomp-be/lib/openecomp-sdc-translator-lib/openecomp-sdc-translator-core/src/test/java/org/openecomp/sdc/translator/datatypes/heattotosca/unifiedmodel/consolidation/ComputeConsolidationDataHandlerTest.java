/*
 * Copyright © 2016-2018 European Support Limited
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

package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation;

import org.junit.Assert;
import org.junit.Test;

public class ComputeConsolidationDataHandlerTest {

    private static final String MAIN_SERVICE_TEMPLATE = "MainServiceTemplate.yaml";
    private static final String SERVER_OAM = "server_oam";

    @Test
    public void isNumberOfComputeTypesLegalPositive() {
        ComputeTemplateConsolidationData computeTemplateConsolidationData = new ComputeTemplateConsolidationData();
        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();
        typeComputeConsolidationData.setComputeTemplateConsolidationData(SERVER_OAM + "_1", computeTemplateConsolidationData);

        FileComputeConsolidationData fileComputeConsolidationData = new FileComputeConsolidationData();
        fileComputeConsolidationData.setTypeComputeConsolidationData(SERVER_OAM, typeComputeConsolidationData);

        ComputeConsolidationData computeConsolidationData = new ComputeConsolidationData();
        computeConsolidationData.setFileComputeConsolidationData(MAIN_SERVICE_TEMPLATE, fileComputeConsolidationData);

        ComputeConsolidationDataHandler computeConsolidationDataHandler =
                new ComputeConsolidationDataHandler(computeConsolidationData);

        Assert.assertTrue(computeConsolidationDataHandler.isNumberOfComputeTypesLegal(MAIN_SERVICE_TEMPLATE));
    }

    @Test
    public void isNumberOfComputeTypesLegalNegative() {
        TypeComputeConsolidationData typeComputeConsolidationData = new TypeComputeConsolidationData();
        typeComputeConsolidationData.setComputeTemplateConsolidationData(SERVER_OAM + "_1", new ComputeTemplateConsolidationData());
        typeComputeConsolidationData.setComputeTemplateConsolidationData(SERVER_OAM + "_2", new ComputeTemplateConsolidationData());


        FileComputeConsolidationData fileComputeConsolidationData = new FileComputeConsolidationData();
        fileComputeConsolidationData.setTypeComputeConsolidationData(SERVER_OAM, typeComputeConsolidationData);

        ComputeConsolidationData computeConsolidationData = new ComputeConsolidationData();
        computeConsolidationData.setFileComputeConsolidationData(MAIN_SERVICE_TEMPLATE, fileComputeConsolidationData);

        ComputeConsolidationDataHandler computeConsolidationDataHandler =
                new ComputeConsolidationDataHandler(computeConsolidationData);

        Assert.assertFalse(computeConsolidationDataHandler.isNumberOfComputeTypesLegal(MAIN_SERVICE_TEMPLATE));
    }
}
