/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.generator;

import org.openecomp.sdc.generator.data.Artifact;
import org.openecomp.sdc.generator.data.ArtifactType;
import org.openecomp.sdc.generator.data.GenerationData;
import org.openecomp.sdc.generator.data.GeneratorUtil;
import org.openecomp.sdc.generator.data.GroupType;
import org.openecomp.sdc.generator.intf.ArtifactGenerator;
import org.openecomp.sdc.generator.intf.Generator;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Generator(artifactType = ArtifactType.OTHER)
public class MockArtifactGenerator implements ArtifactGenerator {

  @Override
  public GenerationData generateArtifact(List<Artifact> input,
                                         Map<String, String> additionalParams) {
    final GenerationData data = new GenerationData();

    String staticArtifactName = "MOCK_Generator-Static-Artifact.xml";
    String staticArtifactLabel = "MOCK-Generator-Static-Artifact";
    final String dynamicArtifactName = "MOCK_Generator-Dynamic-Artifact.xml";
    final String dynamicArtifactLabel = "MOCK-Generator-Dynamic-Artifact";
    String staticArtifact = getStaticArtifact();
    String dynamicArtifact = getDynamicArtifact();

    Artifact staticArtifactModel = new Artifact(ArtifactType.OTHER.name(), GroupType.OTHER.name(),
        GeneratorUtil.checkSum(staticArtifact.getBytes()),
        GeneratorUtil.encode(staticArtifact.getBytes()));
    staticArtifactModel.setName(staticArtifactName);
    staticArtifactModel.setLabel(staticArtifactLabel);
    staticArtifactModel.setDescription("Mock Generator");

    Artifact dynamicArtifactModel = new Artifact(ArtifactType.OTHER.name(), GroupType.OTHER.name(),
        GeneratorUtil.checkSum(dynamicArtifact.getBytes()),
        GeneratorUtil.encode(dynamicArtifact.getBytes()));
    dynamicArtifactModel.setName(dynamicArtifactName);
    dynamicArtifactModel.setLabel(dynamicArtifactLabel);
    dynamicArtifactModel.setDescription("Mock Generator");

    data.add(staticArtifactModel);
    data.add(dynamicArtifactModel);

    return data;

  }

  private String getStaticArtifact() {
    return "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><theObj><name>Hi I'm Static</name></theObj>";
  }

  private String getDynamicArtifact() {
    return
        "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><theObj><name>Hi I'm Static</name><timestamp>"
            + new Date() + "</timestamp></theObj>";
  }

}
