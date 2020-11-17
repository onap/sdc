package org.openecomp.sdc.be.datatypes.elements;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.onap.sdc.tosca.datatypes.model.Constraint;
import org.openecomp.sdc.be.datatypes.tosca.ToscaDataDefinition;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EntrySchemaDataDefinition extends ToscaDataDefinition {

    private String description;
    private String type;
    private List<Constraint> constraints;

}
