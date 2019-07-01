package org.openecomp.sdc.asdctool.impl.validator.executers;

import org.openecomp.sdc.asdctool.impl.validator.tasks.ServiceValidationTask;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.JanusGraphDao;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Created by chaya on 7/4/2017.
 */
@Component
public class ServiceValidatorExecuter extends TopologyTemplateValidatorExecuter implements ValidatorExecuter {

    List<ServiceValidationTask> tasks = new ArrayList<>();

    @Autowired(required = false)
    public ServiceValidatorExecuter(JanusGraphDao janusGraphDao) {
        super(janusGraphDao);
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
