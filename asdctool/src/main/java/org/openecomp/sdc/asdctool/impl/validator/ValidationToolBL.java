package org.openecomp.sdc.asdctool.impl.validator;

import org.openecomp.sdc.asdctool.impl.validator.executers.ValidatorExecuter;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by chaya on 7/3/2017.
 */
@Component
public class ValidationToolBL {

    private static Logger log = Logger.getLogger(ValidationToolBL.class.getName());

    @Autowired
    protected List<ValidatorExecuter> validators;

    private boolean allValid = true;

    public boolean validateAll() {
        for (ValidatorExecuter validatorExec: validators) {
            log.debug("ValidatorExecuter "+validatorExec.getName()+" started");
            if (!validatorExec.executeValidations()) {
                allValid = false;
                log.debug("ValidatorExecuter "+validatorExec.getName()+" finished with warnings");
            }
            else {
                log.debug("ValidatorExecuter "+validatorExec.getName()+" finished successfully");
            }
        }
        return allValid;
    }

}
