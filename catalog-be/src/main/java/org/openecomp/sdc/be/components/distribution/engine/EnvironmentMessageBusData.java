package org.openecomp.sdc.be.components.distribution.engine;

import org.openecomp.sdc.be.resources.data.OperationalEnvironmentEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * a pojo which holds all the necessary data to communicate with the message bus
 * this class is a reflection ot the {@link OperationalEnvironmentEntry} class
 */
public class EnvironmentMessageBusData {

    private List<String> dmaaPuebEndpoints;

    private String uebPublicKey;

    private String uebPrivateKey;

    private String envId;

    private String tenant;

    public EnvironmentMessageBusData() {
    }

    public EnvironmentMessageBusData(OperationalEnvironmentEntry operationalEnvironment) {
        this.dmaaPuebEndpoints = new ArrayList<>(operationalEnvironment.getDmaapUebAddress());
        this.uebPublicKey = operationalEnvironment.getUebApikey();
        this.uebPrivateKey = operationalEnvironment.getUebSecretKey();
        this.envId = operationalEnvironment.getEnvironmentId();
        this.tenant = operationalEnvironment.getTenant();
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public List<String> getDmaaPuebEndpoints() {
        return dmaaPuebEndpoints;
    }

    public void setDmaaPuebEndpoints(List<String> dmaaPuebEndpoints) {
        this.dmaaPuebEndpoints = dmaaPuebEndpoints;
    }

    public String getUebPublicKey() {
        return uebPublicKey;
    }

    public void setUebPublicKey(String uebPublicKey) {
        this.uebPublicKey = uebPublicKey;
    }

    public String getUebPrivateKey() {
        return uebPrivateKey;
    }

    public void setUebPrivateKey(String uebPrivateKey) {
        this.uebPrivateKey = uebPrivateKey;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

}
