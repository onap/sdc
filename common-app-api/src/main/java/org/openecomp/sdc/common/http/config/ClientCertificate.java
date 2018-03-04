package org.openecomp.sdc.common.http.config;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdc.security.SecurityUtil;

import fj.data.Either;

public class ClientCertificate {
    private String keyStore;
    private String keyStorePassword;

    public ClientCertificate() {
    }

    public ClientCertificate(ClientCertificate clientCertificate) {
        setKeyStore(clientCertificate.getKeyStore());
        setKeyStorePassword(clientCertificate.getKeyStorePassword(), false);
    }
    
    public void setKeyStore(String keyStore) {
        validate(keyStore);
        this.keyStore = keyStore;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        setKeyStorePassword(keyStorePassword, true);
    }

    private void setKeyStorePassword(String keyStorePassword, boolean isEncoded) {
        validate(keyStorePassword);
        if(isEncoded) {
            Either<String, String> passkey = SecurityUtil.INSTANCE.decrypt(keyStorePassword);
            if (passkey.isLeft()) {
                this.keyStorePassword = passkey.left().value();
            }
            else {
                throw new IllegalArgumentException(passkey.right().value());
            }
        }
        else {
            this.keyStorePassword = keyStorePassword;
        }
    }

    public String getKeyStore() {
        return keyStore;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((keyStore == null) ? 0 : keyStore.hashCode());
        result = prime * result + ((keyStorePassword == null) ? 0 : keyStorePassword.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClientCertificate other = (ClientCertificate) obj;
        if (keyStore == null) {
            if (other.keyStore != null)
                return false;
        }
        else if (!keyStore.equals(other.keyStore))
            return false;
        if (keyStorePassword == null) {
            if (other.keyStorePassword != null)
                return false;
        }
        else if (!keyStorePassword.equals(other.keyStorePassword))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ClientCertificate [keyStore=");
        builder.append(keyStore);
        builder.append("]");
        return builder.toString();
    }
    
    private void validate(String str) {
        if(StringUtils.isEmpty(str)) {
            throw new IllegalArgumentException("ClientCertificate keystore and/or kestorePassword cannot be empty");
        }
    }
}
