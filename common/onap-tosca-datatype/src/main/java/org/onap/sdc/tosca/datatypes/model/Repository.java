package org.onap.sdc.tosca.datatypes.model;

import lombok.Data;

@Data
public class Repository {

    private String description;
    private String url;
    private Credential credential;
}
