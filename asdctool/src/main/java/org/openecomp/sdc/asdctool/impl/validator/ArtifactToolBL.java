package org.openecomp.sdc.asdctool.impl.validator;

import org.openecomp.sdc.asdctool.impl.validator.executers.IArtifactValidatorExecuter;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ArtifactToolBL {
	
	 private static Logger log = Logger.getLogger(ValidationToolBL.class.getName());

	    protected List<IArtifactValidatorExecuter> validators;

	    private boolean allValid = true;

	@Autowired
	public ArtifactToolBL(
		List<IArtifactValidatorExecuter> validators) {
		this.validators = validators;
	}

	public boolean validateAll() {
	        for (IArtifactValidatorExecuter validatorExec: validators) {
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
