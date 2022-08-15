package org.openecomp.sdc.be.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class NodeTypeDefinition {

    @Setter
    private Map.Entry<String, Object> mappedNodeType;
    private NodeTypeMetadata nodeTypeMetadata;
}
