/**
 * Created by ob0695
 */
import {ArtifactModel} from "../../../models/artifacts";

export class GetInstanceArtifactsByTypeAction {
    static readonly type = '[INSTANCE_ARTIFACTS] GetInstanceArtifactsByTypeAction';

    constructor(public payload: { componentType: string, componentId: string, artifactType: string, instanceId: string }) {
    }
}

export class CreateInstanceArtifactAction {
    static readonly type = '[INSTANCE_ARTIFACTS] CreateInstanceArtifactAction';

    constructor(public payload: { componentType: string, componentId: string, instanceId: string, artifact: ArtifactModel }) {
    }
}

export class UpdateInstanceArtifactAction {
    static readonly type = '[INSTANCE_ARTIFACTS] UpdateInstanceArtifactAction';

    constructor(public payload: { componentType: string, componentId: string, instanceId: string, artifact: ArtifactModel }) {
    }
}

export class DeleteInstanceArtifactAction {
    static readonly type = '[INSTANCE_ARTIFACTS] DeleteInstanceArtifactAction';

    constructor(public payload: { componentType: string, componentId: string, instanceId: string, artifact: ArtifactModel }) {
    }
}
