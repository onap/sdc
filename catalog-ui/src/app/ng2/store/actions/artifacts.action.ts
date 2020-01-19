/**
 * Created by ob0695
 */
import {ArtifactModel} from "../../../models/artifacts";

export class GetArtifactsByTypeAction {
    static readonly type = '[ARTIFACTS] GetArtifactsByType';

    constructor(public payload: {componentType:string, componentId:string, artifactType: string}) {
    }
}

export class CreateOrUpdateArtifactAction {
    static readonly type = '[ARTIFACTS] CreateOrUpdateArtifactAction';

    constructor(public payload: {componentType:string, componentId:string, artifact:ArtifactModel}) {
    }
}

export class DeleteArtifactAction {
    static readonly type = '[ARTIFACTS] DeleteArtifactAction';

    constructor(public payload: {componentType:string, componentId:string, artifact: ArtifactModel}) {
    }
}
