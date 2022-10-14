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

package org.openecomp.sdcrests.vsp.rest.mapping;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireValidationResult;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ValidationResponseDto;

/**
 * This class was generated.
 */
public class MapValidationResponseToDtoTest {

    @Test()
    public void testConversion() {

        final ValidationResponse source = new ValidationResponse();

        final Collection<ErrorCode> vspErrors = Collections.emptyList();
        source.setVspErrors(vspErrors);

        final Collection<ErrorCode> licensingDataErrors = Collections.emptyList();
        source.setLicensingDataErrors(licensingDataErrors);

        final Map<String, List<ErrorMessage>> uploadDataErrors = Collections.emptyMap();
        source.setUploadDataErrors(uploadDataErrors);

        final QuestionnaireValidationResult questionnaireValidationResult = new QuestionnaireValidationResult(null);
        source.setQuestionnaireValidationResult(questionnaireValidationResult);

        final ValidationResponseDto target = new ValidationResponseDto();
        final MapValidationResponseToDto mapper = new MapValidationResponseToDto();
        mapper.doMapping(source, target);

        assertTrue(target.isValid());
        assertNull(target.getVspErrors());
        assertNull(target.getLicensingDataErrors());
        assertNull(target.getUploadDataErrors());
        assertNull(target.getQuestionnaireValidationResult());
    }
}
