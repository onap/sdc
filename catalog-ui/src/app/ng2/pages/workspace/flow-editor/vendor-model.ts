/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
import {ArtifactGroupModel, ComponentInstance} from 'app/models';

/**
 * A single actor/lane in the sequence-diagram flow editor. For network-call-flow the
 * participants are derived from the service's VF component instances; for management-workflow
 * they are a fixed set of plain {id, name} objects (see ManagementWorkflowTabComponent.PARTICIPANTS).
 *
 * Relocated from the deleted network-call-flow-view-model.ts (Phase 5) so the migrated Angular
 * flow-editor tabs can share it without the old circular view-model coupling.
 */
export class participant {
    name: string;
    id: string;

    constructor(instance: ComponentInstance) {
        this.name = instance.name;
        this.id = instance.uniqueId;
    }
}

/**
 * The data payload handed to the internal React "sequence-diagram" punch-out (FlowsListEditor):
 * the punch-out reads `serviceID` + `diagramType` to fetch the matching flow artifacts and renders
 * the editor read-only when `readonly` is true.
 *
 * Relocated from the deleted management-workflow-view-model.ts (Phase 5).
 */
export class VendorModel {
    artifacts: ArtifactGroupModel;
    serviceID: string;
    readonly: boolean;
    sessionID: string;
    requestID: string;
    diagramType: string;
    participants: Array<participant | {id: string, name: string}>;

    constructor(artifacts: ArtifactGroupModel, serviceID: string, readonly: boolean, sessionID: string,
                requestID: string, diagramType: string,
                participants: Array<participant | {id: string, name: string}>) {
        this.artifacts = artifacts;
        this.serviceID = serviceID;
        this.readonly = readonly;
        this.sessionID = sessionID;
        this.requestID = requestID;
        this.diagramType = diagramType;
        this.participants = participants;
    }
}
