package org.openecomp.sdc.be.config;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
}
