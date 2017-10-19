package org.openecomp.sdc.asdctool.impl.validator.executers;

import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdc.asdctool.impl.validator.tasks.ServiceValidationTask;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by chaya on 7/4/2017.
 */
public class ServiceValidatorExecuter extends TopologyTemplateValidatorExecuter implements ValidatorExecuter {

    @Autowired(required = false)
    List<ServiceValidationTask> tasks = new ArrayList<>();

    private static Logger log = LoggerFactory.getLogger(VfValidatorExecuter.class.getName());

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
