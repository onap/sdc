/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.translator.services.heattotosca.unifiedmodel.commands;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.onap.sdc.tosca.datatypes.model.TopologyTemplate;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.commands.UnifiedSubstitutionNodeTemplateIdGenerator;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.commands.impl.ComputeNewNodeTemplateIdGenerator;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.to.UnifiedCompositionTo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.openecomp.sdc.translator.TestUtils.createInitServiceTemplate;

public class ComputeNewNodeTemplateIdGeneratorTest  {

  private static final String SERVER_PCM = "server_pcm";
  private static UnifiedSubstitutionNodeTemplateIdGenerator unifiedSubstitutionNodeTemplateIdGenerator;

  @BeforeClass
  public static void setItUp(){
    unifiedSubstitutionNodeTemplateIdGenerator = new ComputeNewNodeTemplateIdGenerator();
  }

  @Test
  public void testGenerateNewComputeNodeTemplateId() {
    UnifiedCompositionTo unifiedCompositionTo = new UnifiedCompositionTo(getTestComputeServiceTemplate(), null, null,
        null, null);
    Optional<String>
        nodeTemplateId = unifiedSubstitutionNodeTemplateIdGenerator.generate(unifiedCompositionTo, SERVER_PCM);
    if (nodeTemplateId.isPresent()) {
      Assert.assertEquals(nodeTemplateId.get(), "pcm_server");
    } else {
      Assert.fail();
    }
  }

  @Test
  public void testGenerateInvalidOriginalNodeTemplateId() {
    //node template with id is not present in the service template
    UnifiedCompositionTo unifiedCompositionTo = new UnifiedCompositionTo(getTestComputeServiceTemplate(), null, null,
        null, null);
    Optional<String>
        nodeTemplateId = unifiedSubstitutionNodeTemplateIdGenerator.generate(unifiedCompositionTo, SERVER_PCM +
        "_invalid");
    Assert.assertEquals(nodeTemplateId.isPresent(), false);
  }

  @Test
  public void testGenerateNullOriginalNodeTemplateId() {
    UnifiedCompositionTo unifiedCompositionTo = new UnifiedCompositionTo(getTestComputeServiceTemplate(), null, null,
        null, null);
    Optional<String>
        nodeTemplateId = unifiedSubstitutionNodeTemplateIdGenerator.generate(unifiedCompositionTo, null);
    Assert.assertEquals(nodeTemplateId.isPresent(), false);
  }

  private static ServiceTemplate getTestComputeServiceTemplate() {
    ServiceTemplate serviceTemplate = createInitServiceTemplate();
    TopologyTemplate topologyTemplate = serviceTemplate.getTopology_template();
    NodeTemplate nodeTemplate = new NodeTemplate();
    nodeTemplate.setType("org.openecomp.resource.vfc.nodes.heat.pcm_server");
    Map<String, NodeTemplate> nodeTemplateMap = new HashMap<>();
    nodeTemplateMap.put(SERVER_PCM, nodeTemplate);
    topologyTemplate.setNode_templates(nodeTemplateMap);
    return serviceTemplate;
  }

}
