/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireResponseDto;

/**
 * This class was generated.
 */
public class MapQuestionnaireResponseToQuestionnaireResponseDtoTest {

    @Test()
    public void testConversion() {

        final QuestionnaireResponse source = new QuestionnaireResponse();

        final String schema = "20f7944e-ae84-4604-b597-f4c14ee413cc";
        source.setSchema(schema);

        final String data = "8fac7a9d-b801-47d4-a482-e21ee6558873";
        source.setData(data);

        final ErrorMessage errorMessage = new ErrorMessage(ErrorLevel.WARNING, "15fc54f6-e719-4da0-9a57-da9926994566");
        source.setErrorMessage(errorMessage);

        final QuestionnaireResponseDto target = new QuestionnaireResponseDto();
        final MapQuestionnaireResponseToQuestionnaireResponseDto mapper = new MapQuestionnaireResponseToQuestionnaireResponseDto();
        mapper.doMapping(source, target);

        assertEquals(schema, target.getSchema());
        assertEquals(data, target.getData());
        assertSame(errorMessage, target.getErrorMessage());
    }
}
