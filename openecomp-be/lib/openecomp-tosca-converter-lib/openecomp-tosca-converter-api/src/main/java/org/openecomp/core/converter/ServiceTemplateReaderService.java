package org.openecomp.core.converter;

import java.util.Map;

public interface ServiceTemplateReaderService {

  Map<String, Object> readServiceTemplate(byte[] serivceTemplateContent);

  Object getMetadata();

  Object getToscaVersion();

  Map<String, Object> getNodeTypes();

  Object getTopologyTemplate();

  Map<String, Object> getNodeTemplates();

  Map<String, Object> getInputs();

  Map<String, Object> getOutputs();

  Map<String, Object> getSubstitutionMappings();
}
