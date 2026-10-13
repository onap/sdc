import * as _ from 'lodash';
import { ArtifactGroupModel } from '../../models/artifacts';
import { ForwardingPath } from '../../models/forwarding-path';
import { Serializable } from '../utils/serializable';
import { ComponentGenericResponse } from './component-generic-response';

export class ServiceGenericResponse extends ComponentGenericResponse implements Serializable<ServiceGenericResponse>  {
    public forwardingPaths: { [key: string]: ForwardingPath } = {};
    public serviceApiArtifacts: ArtifactGroupModel;

    deserialize(response): ServiceGenericResponse {
        super.deserialize(response);
        this.serviceApiArtifacts = new ArtifactGroupModel(response.serviceApiArtifacts);
        if (response.forwardingPaths) {
            _.forEach(response.forwardingPaths, (pathResponse, id) => {
                const pathId = id;
                const path: ForwardingPath = new ForwardingPath();
                path.deserialize(pathResponse);
                path.uniqueId = pathId;
                this.forwardingPaths[pathId] = path;
            });
        }
        return this;
    }
}
