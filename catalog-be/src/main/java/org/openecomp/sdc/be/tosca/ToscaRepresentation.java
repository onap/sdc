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
package org.openecomp.sdc.be.tosca;

import io.vavr.control.Option;
import java.util.List;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Triple;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.tosca.model.ToscaTemplate;

public class ToscaRepresentation {

    @Getter
    private final byte[] mainYaml;
    @Getter
    private final Option<List<Triple<String, String, Component>>> dependencies;

    private ToscaRepresentation(byte[] mainYaml, Option<List<Triple<String, String, Component>>> dependencies) {
        this.mainYaml = mainYaml;
        this.dependencies = dependencies;
    }

    public static ToscaRepresentation make(byte[] mainYaml) {
        return new ToscaRepresentation(mainYaml, Option.none());
    }

    public static ToscaRepresentation make(byte[] mainYaml, List<Triple<String, String, Component>> dependencies) {
        return new ToscaRepresentation(mainYaml, Option.of(dependencies));
    }

    public static ToscaRepresentation make(byte[] mainYaml, ToscaTemplate tt) {
        return new ToscaRepresentation(mainYaml, Option.of(tt.getDependencies()));
    }
}
