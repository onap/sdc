package org.openecomp.sdc.be.tosca;

import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.tosca.model.ToscaGroupTemplate;

import java.util.Map;

public interface GroupExportParser {

	Map<String, ToscaGroupTemplate> getGroups(Component component);
	
	ToscaGroupTemplate getToscaGroupTemplate(GroupInstance groupInstance, String invariantName) ;
	
}
