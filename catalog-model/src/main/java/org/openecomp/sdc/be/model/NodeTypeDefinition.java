package org.openecomp.sdc.be.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class NodeTypeDefinition {

    private Map.Entry<String, Object> mappedNodeType;
    private NodeTypeMetadata NodeTypeMetadata;
}
