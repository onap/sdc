package org.openecomp.sdc.be.tosca;

import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.tosca.model.ToscaPolicyTemplate;

import java.util.Map;

public interface PolicyExportParser {

	Map<String, ToscaPolicyTemplate> getPolicies(Component component);
}
