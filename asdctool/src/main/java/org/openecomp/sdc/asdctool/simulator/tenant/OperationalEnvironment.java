/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.asdctool.simulator.tenant;

import com.opencsv.bean.CsvBindByPosition;
import org.openecomp.sdc.be.datatypes.enums.EnvironmentStatusEnum;

/**
 * Represents line in CSV file should be imported into "operationalenvironment" table.
 *
 * @author dr2032
 */
public class OperationalEnvironment {

    @CsvBindByPosition(position = 0)
    private String environmentId;
    @CsvBindByPosition(position = 1)
    private String dmaapUebAddress;
    @CsvBindByPosition(position = 2)
    private String ecompWorkloadContext;
    @CsvBindByPosition(position = 3)
    private Boolean isProduction;
    @CsvBindByPosition(position = 4)
    private String lastModified;
    @CsvBindByPosition(position = 5)
    private String status;
    @CsvBindByPosition(position = 6)
    private String tenant;
    @CsvBindByPosition(position = 7)
    private String uebApikey;
    @CsvBindByPosition(position = 8)
    private String uebSecretKey;

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getEnvironmentId() {
        return environmentId;
    }

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
        this.status = status;
    }

    public void setStatus(EnvironmentStatusEnum status) {
        this.status = status.getName();
    }

    public String getDmaapUebAddress() {
        return dmaapUebAddress;
    }

    public void setDmaapUebAddress(String dmaapUebAddress) {
        this.dmaapUebAddress = dmaapUebAddress;
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
}
