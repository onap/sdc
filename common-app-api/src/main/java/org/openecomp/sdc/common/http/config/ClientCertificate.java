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
package org.openecomp.sdc.common.http.config;

import fj.data.Either;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;
import org.onap.sdc.security.SecurityUtil;

@EqualsAndHashCode
@Getter
@Setter
public class ClientCertificate {

    private String keyStore;
    private String keyStorePassword;
    private String trustStore;
    private String trustStorePassword;
    
    public ClientCertificate() {
    }

    public ClientCertificate(ClientCertificate clientCertificate) {
        setKeyStore(clientCertificate.getKeyStore());
        setKeyStorePassword(clientCertificate.getKeyStorePassword(), false);
        setTrustStore(clientCertificate.getTrustStore());
        setTrustStorePassword(clientCertificate.getTrustStorePassword());
    }

    public void setKeyStorePassword(String keyStorePassword, boolean isEncoded) {
        validate(keyStorePassword);
        if (isEncoded) {
            Either<String, String> passkey = SecurityUtil.decrypt(keyStorePassword);
            if (passkey.isLeft()) {
                this.keyStorePassword = passkey.left().value();
            } else {
                throw new IllegalArgumentException(passkey.right().value());
            }
        } else {
            this.keyStorePassword = keyStorePassword;
        }
    }

    public void setKeyStore(String keyStore) {
        validate(keyStore);
        this.keyStore = keyStore;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        setKeyStorePassword(keyStorePassword, true);
    }

    private void validate(String str) {
        if (StringUtils.isEmpty(str)) {
            throw new IllegalArgumentException("ClientCertificate keystore and/or kestorePassword cannot be empty");
        }
    }
}
