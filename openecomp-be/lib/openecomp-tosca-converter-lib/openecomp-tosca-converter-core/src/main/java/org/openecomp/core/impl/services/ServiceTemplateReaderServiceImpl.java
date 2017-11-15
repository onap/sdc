package org.openecomp.core.impl.services;

import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.sdc.tosca.services.YamlUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.openecomp.core.converter.datatypes.Constants.*;

public class ServiceTemplateReaderServiceImpl implements ServiceTemplateReaderService {

  private Map<String, Object> readServiceTemplate = new HashMap<>();

  public ServiceTemplateReaderServiceImpl(byte[] serviceTemplateContent){
    this.readServiceTemplate = readServiceTemplate(serviceTemplateContent);
  }

  @Override
  public Map<String, Object> readServiceTemplate(byte[] serviceTemplateContent) {

    Map<String, Object> readSt =
        new YamlUtil().yamlToObject(new String(serviceTemplateContent), Map.class);

    return readSt;
  }

  @Override
  public Object getMetadata(){
    return this.readServiceTemplate.get(metadata);
  }

  @Override
  public Object getToscaVersion(){
    return this.readServiceTemplate.get(definitionVersion);
  }

  @Override
  public Object getNodeTypes(){
    return this.readServiceTemplate.get(nodeTypes);
  }

  @Override
  public Object getTopologyTemplate(){
    return this.readServiceTemplate.get(topologyTemplate);
  }

  @Override
  public Map<String, Object> getNodeTemplates(){
    return Objects.isNull(this.getTopologyTemplate()) ? new HashMap<>()
        : (Map<String, Object>) ((Map<String, Object>)this.getTopologyTemplate()).get(nodeTemplates);
  }

  @Override
  public Map<String, Object> getInputs(){
    return Objects.isNull(this.getTopologyTemplate()) ? new HashMap<>()
        : (Map<String, Object>) ((Map<String, Object>)this.getTopologyTemplate()).get(inputs);
  }

  @Override
  public Map<String, Object> getOutputs(){
    return Objects.isNull(this.getTopologyTemplate()) ? new HashMap<>()
        : (Map<String, Object>) ((Map<String, Object>)this.getTopologyTemplate()).get(outputs);
  }

  @Override
  public Map<String, Object> getSubstitutionMappings(){
    return Objects.isNull(this.getTopologyTemplate()) ? new HashMap<>()
        : (Map<String, Object>) ((Map<String, Object>)this.getTopologyTemplate()).get(substitutionMappings);
  }
}
