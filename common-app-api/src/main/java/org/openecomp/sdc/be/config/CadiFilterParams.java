package org.openecomp.sdc.be.config;

public class CadiFilterParams {

    private String hostname;
    private String csp_domain;
    private String cadi_keyfile;
    private String cadi_loglevel;
    private String cadi_truststore;
    private String cadi_truststore_password;

    private String aaf_id;
    private String aaf_password;
    private String aaf_env;
    private String aafLocateUrl;
    private String aaf_url;
    private String AFT_LATITUDE;
    private String AFT_LONGITUDE;
    private String AFT_ENVIRONMENT;
    private String cadiX509Issuers;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getCsp_domain() {
        return csp_domain;
    }

    public void setCsp_domain(String csp_domain) {
        this.csp_domain = csp_domain;
    }

    public String getCadi_keyfile() {
        return cadi_keyfile;
    }

    public void setCadi_keyfile(String cadi_keyfile) {
        this.cadi_keyfile = cadi_keyfile;
    }

    public String getCadi_loglevel() {
        return cadi_loglevel;
    }

    public void setCadi_loglevel(String cadi_loglevel) {
        this.cadi_loglevel = cadi_loglevel;
    }

    public String getCadi_truststore() {
        return cadi_truststore;
    }

    public void setCadi_truststore(String cadi_truststore) {
        this.cadi_truststore = cadi_truststore;
    }

    public String getCadi_truststore_password() {
        return cadi_truststore_password;
    }

    public void setCadi_truststore_password(String cadi_truststore_password) {
        this.cadi_truststore_password = cadi_truststore_password;
    }

    public String getAaf_id() {
        return aaf_id;
    }

    public void setAaf_id(String aaf_id) {
        this.aaf_id = aaf_id;
    }

    public String getAaf_password() {
        return aaf_password;
    }

    public void setAaf_password(String aaf_password) {
        this.aaf_password = aaf_password;
    }

    public String getAaf_env() {
        return aaf_env;
    }

    public void setAaf_env(String aaf_env) {
        this.aaf_env = aaf_env;
    }

    public String getAafLocateUrl() {
        return aafLocateUrl;
    }

    public void setAafLocateUrl(String aafLocateUrl) {
        this.aafLocateUrl = aafLocateUrl;
    }

    public String getAaf_url() {
        return aaf_url;
    }

    public void setAaf_url(String aaf_url) {
        this.aaf_url = aaf_url;
    }

    public String getAFT_LATITUDE() {
        return AFT_LATITUDE;
    }

    public void setAFT_LATITUDE(String aFT_LATITUDE) {
        AFT_LATITUDE = aFT_LATITUDE;
    }

    public String getAFT_LONGITUDE() {
        return AFT_LONGITUDE;
    }

    public void setAFT_LONGITUDE(String aFT_LONGITUDE) {
        AFT_LONGITUDE = aFT_LONGITUDE;
    }

    public String getAFT_ENVIRONMENT() {
        return AFT_ENVIRONMENT;
    }

    public void setAFT_ENVIRONMENT(String aFT_ENVIRONMENT) {
        AFT_ENVIRONMENT = aFT_ENVIRONMENT;
    }

    public String getCadiX509Issuers() {
        return cadiX509Issuers;
    }

    public void setCadiX509Issuers(String cadiX509Issuers) {
        this.cadiX509Issuers = cadiX509Issuers;
    }

}
