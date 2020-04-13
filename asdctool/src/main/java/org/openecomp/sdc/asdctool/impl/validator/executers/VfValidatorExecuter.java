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

package org.openecomp.sdc.asdctool.impl.validator.executers;

import org.openecomp.sdc.asdctool.impl.validator.tasks.VfValidationTask;
import org.openecomp.sdc.asdctool.impl.validator.utils.VertexResult;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chaya on 7/3/2017.
 */
public class VfValidatorExecuter extends TopologyTemplateValidatorExecuter implements ValidatorExecuter {

    private List<VfValidationTask> tasks;

    @Autowired(required = false)
    public VfValidatorExecuter(List<VfValidationTask> tasks, JanusGraphDao janusGraphDao) {
        super(janusGraphDao);
        this.tasks = tasks;
        setName("BASIC_VF_VALIDATOR");
    }

    @Override
    public boolean executeValidations(Map<String, Set<String>> failedVerticesPerTask, Map<String, Map<String, VertexResult>> resultsPerVertex, String txtReportFilePath) {
        List<GraphVertex> vertices = getVerticesToValidate(ComponentTypeEnum.RESOURCE);
        return validate(failedVerticesPerTask, resultsPerVertex, tasks, vertices, txtReportFilePath);
    }

    @Override
    public String getName() {
        return super.getName();
    }
}
