import * as _ from "lodash";
import {Serializable} from "../utils/serializable";
import {ComponentGenericResponse} from "./component-generic-response";
import {ForwardingPath} from "../../../models/forwarding-path";
import {ArtifactGroupModel} from "../../../models/artifacts";

export class ServiceGenericResponse extends ComponentGenericResponse implements Serializable<ServiceGenericResponse>  {
    public forwardingPaths: { [key:string]:ForwardingPath } = {};
    public serviceApiArtifacts: ArtifactGroupModel;

    deserialize (response): ServiceGenericResponse {
        super.deserialize(response);
        this.serviceApiArtifacts = new ArtifactGroupModel(response.serviceApiArtifacts);
        if(response.forwardingPaths) {
            _.forEach(response.forwardingPaths, (pathResponse, id) => {
                let pathId = id;
                let path:ForwardingPath = new ForwardingPath();
                path.deserialize(pathResponse);
                path.uniqueId = pathId;
                this.forwardingPaths[pathId] = path;
            });
        }
        return this;
    }
}