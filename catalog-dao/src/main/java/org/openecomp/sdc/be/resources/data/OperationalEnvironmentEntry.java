package org.openecomp.sdc.be.resources.data;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Table(keyspace = "sdcrepository", name = "operationalEnvironment")
public class OperationalEnvironmentEntry {

    @PartitionKey(0)
    @Column(name = "environment_id")
    private String environmentId;

    @Column(name = "tenant")
    private String tenant;

    @Column(name = "is_production")
    private Boolean isProduction;

    @Column(name = "ecomp_workload_context")
    private String ecompWorkloadContext;

    @Column(name = "dmaap_ueb_address")
    private Set<String> dmaapUebAddress;

    @Column(name = "ueb_api_key")
    private String uebApikey;

    @Column(name = "ueb_secret_key")
    private String uebSecretKey;

    @Column(name = "status")
    private String status;

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    @Column(name = "last_modified")
    private Date lastModified;


    public String getEnvironmentId() {
        return environmentId;
    }

    //must be unique, add any validation if neccessary
    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public Boolean getIsProduction() {
        return isProduction;
    }

    public void setIsProduction(Boolean production) {
        isProduction = production;
    }

    public String getEcompWorkloadContext() {
        return ecompWorkloadContext;
    }

    public void setEcompWorkloadContext(String ecompWorkloadContext) {
        this.ecompWorkloadContext = ecompWorkloadContext;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        //log if status doesn't exists in EnvironmentStatusEnum
        this.status = status;
    }

    public void setStatus(EnvironmentStatusEnum status) {
        this.status = status.getName();
    }

    public Set<String> getDmaapUebAddress() {
        return dmaapUebAddress;
    }

    public void setDmaapUebAddress(Set<String> dmaapUebAddress) {
        this.dmaapUebAddress = dmaapUebAddress;
    }

    public void addDmaapUebAddress(String address) {
        if ( this.dmaapUebAddress == null )
            this.dmaapUebAddress = new HashSet<>();
        dmaapUebAddress.add(address);
    }

    public String getUebApikey() {
        return uebApikey;
    }

    public void setUebApikey(String uebApikey) {
        this.uebApikey = uebApikey;
    }

    public String getUebSecretKey() {
        return uebSecretKey;
    }

    public void setUebSecretKey(String uebSecretKey) {
        this.uebSecretKey = uebSecretKey;
    }
    
    @Override
	public String toString() {
		return "OperationalEnvironmentEntry [environmentId=" + environmentId + ", tenant=" + tenant + ", isProduction="
				+ isProduction + ", ecompWorkloadContext=" + ecompWorkloadContext + ", dmaapUebAddress="
				+ dmaapUebAddress + ", uebApikey=" + uebApikey + ", status=" + status
				+ ", lastModified=" + lastModified + "]";
	}


}
