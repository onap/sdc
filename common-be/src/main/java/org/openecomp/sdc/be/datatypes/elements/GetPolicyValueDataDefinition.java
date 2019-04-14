package org.openecomp.sdc.be.datatypes.elements;

import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@EqualsAndHashCode
public class GetPolicyValueDataDefinition {
    private String policyId;
    private String propertyName;
    private String origPropertyValue;

}
