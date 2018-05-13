package org.openecomp.sdc.be.components.utils;

import java.util.Set;

import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;
import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;

public class OperationalEnvironmentBuilder {

    private OperationalEnvironmentEntry operationalEnvironmentEntry;

    public OperationalEnvironmentBuilder() {
        operationalEnvironmentEntry = new OperationalEnvironmentEntry();
    }

    public OperationalEnvironmentBuilder setEnvId(String envId) {
        operationalEnvironmentEntry.setEnvironmentId(envId);
        return this;
    }

    public OperationalEnvironmentBuilder setDmaapUebAddress(Set<String> addresses) {
        operationalEnvironmentEntry.setDmaapUebAddress(addresses);
        return this;
    }

    public OperationalEnvironmentBuilder setStatus(EnvironmentStatusEnum status) {
        operationalEnvironmentEntry.setStatus(status);
        return this;
    }

    public OperationalEnvironmentEntry build() {
        return operationalEnvironmentEntry;
    }

}
