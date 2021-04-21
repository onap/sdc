package org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.validation;

import java.util.ArrayList;
import java.util.List;

public class CnfValidatorResult {

    private final List<String> errorMessages;
    private final List<String> warningMessages;
    private boolean isDeployable;

    public CnfValidatorResult() {
        this.errorMessages = new ArrayList<>();
        this.warningMessages = new ArrayList<>();
        this.isDeployable = true;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public boolean isValid() {
        return errorMessages.isEmpty() && isDeployable;
    }

    public List<String> getWarningMessages() {
        return warningMessages;
    }

    public void addWarning(String helmWarning) {
        warningMessages.add(helmWarning);
    }

    public void addErrorMessages(List<String> errorMessages) {
        this.errorMessages.addAll(errorMessages);
    }

    public void addWarningMessages(List<String> warningMessages) {
        this.warningMessages.addAll(warningMessages);
    }

    void setDeployable(boolean deployable) {
        isDeployable = deployable;
    }
}
