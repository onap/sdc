package org.openecomp.sdc.asdctool.impl.validator;

import java.util.List;

import org.openecomp.sdc.asdctool.impl.validator.executers.ArtifactValidatorExecuter;
import org.openecomp.sdc.asdctool.impl.validator.executers.IArtifactValidatorExecuter;
import org.openecomp.sdc.asdctool.impl.validator.utils.ReportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ArtifactToolBL {
	
	 private static Logger log = LoggerFactory.getLogger(ValidationToolBL.class.getName());

	    @Autowired
	    protected List<IArtifactValidatorExecuter> validators;

	    @Autowired
	    protected ReportManager reportManager;

	    private boolean allValid = true;


	    public boolean validateAll() {
	        for (IArtifactValidatorExecuter validatorExec: validators) {
	            System.out.println("ValidatorExecuter "+validatorExec.getName()+" started");
	            if (!validatorExec.executeValidations()) {
	                allValid = false;
	                System.out.println("ValidatorExecuter "+validatorExec.getName()+" finished with warnings");
	            }
	            else {
	                System.out.println("ValidatorExecuter "+validatorExec.getName()+" finished successfully");
	            }
	        }
	        return allValid;
	    }

}
