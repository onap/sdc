package org.openecomp.sdc.be.model.operations.api;

import org.openecomp.sdc.be.dao.graph.datatype.GraphEdge;
import org.openecomp.sdc.be.model.ComponentInstance;

import java.util.List;

public interface ToscaDefinitionPathCalculator {

    List<String> calculateToscaDefinitionPath(ComponentInstance componentInstance, GraphEdge edge);

}
