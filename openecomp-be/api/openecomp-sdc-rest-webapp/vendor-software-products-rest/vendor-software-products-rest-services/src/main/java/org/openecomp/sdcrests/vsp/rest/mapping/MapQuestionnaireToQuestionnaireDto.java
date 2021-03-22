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
package org.openecomp.sdcrests.vsp.rest.mapping;

import java.util.HashMap;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.CompositionEntity;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.QuestionnaireDto;

/**
 * Created by ayalaben on 9/26/2017
 */
public class MapQuestionnaireToQuestionnaireDto extends MappingBase<CompositionEntity, QuestionnaireDto> {

    @Override
    public void doMapping(CompositionEntity source, QuestionnaireDto target) {
        target.setId(source.getId());
        target.setQuestionareData(JsonUtil.json2Object(source.getQuestionnaireData(), HashMap.class));
    }
}
