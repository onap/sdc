package org.openecomp.sdc.asdctool.impl.validator.executers;

import org.openecomp.sdc.asdctool.impl.validator.tasks.VfValidationTask;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Created by chaya on 7/3/2017.
 */
public class VfValidatorExecuter extends TopologyTemplateValidatorExecuter implements ValidatorExecuter {

    @Autowired(required = false)
    private List<VfValidationTask> tasks = new ArrayList<>();

    public VfValidatorExecuter() {
        setName("BASIC_VF_VALIDATOR");
    }

    @Override
    public boolean executeValidations() {
        List<GraphVertex> vertices = getVerticesToValidate(ComponentTypeEnum.RESOURCE);
        return validate(tasks, vertices);
    }

    @Override
    public String getName() {
        return super.getName();
    }
}
