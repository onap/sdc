package org.openecomp.sdc.be.tosca.model;

import lombok.Data;

@Data
public class ToscaFilter {

    private String name;
    private String constraint;
    private Object value; //ToscaFunction or String or float, etc I guess
}
