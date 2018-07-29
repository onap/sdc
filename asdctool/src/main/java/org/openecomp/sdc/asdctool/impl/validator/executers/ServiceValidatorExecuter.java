package org.openecomp.sdc.asdctool.impl.validator.executers;

import org.openecomp.sdc.asdctool.impl.validator.tasks.ServiceValidationTask;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chaya on 7/4/2017.
 */
public class ServiceValidatorExecuter extends TopologyTemplateValidatorExecuter implements ValidatorExecuter {

    @Autowired(required = false)
    List<ServiceValidationTask> tasks = new ArrayList<>();

    public ServiceValidatorExecuter() {
        setName("SERVICE_VALIDATOR");
    }

    @Override
    public boolean executeValidations() {
        List<GraphVertex> vertices = getVerticesToValidate(ComponentTypeEnum.SERVICE);
        return validate(tasks, vertices);
    }

    @Override
    public String getName() {
        return super.getName();
    }
}
