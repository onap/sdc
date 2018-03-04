import {PathElements} from './path-elements';
import {ForwardingPathLink} from "./forwarding-path-link";

export class ForwardingPath {
    public name:string;
    public destinationPortNumber:string;
    public protocol:string;
    public uniqueId:string;
    public ownerId: string;
    public pathElements: PathElements;

    addPathLink(fromNode:string, fromCP:string, toNode:string, toCP:string, fromCPOriginId: string, toCPOriginId: string) {
        if (!this.pathElements) {
            this.pathElements = new PathElements();
        }
        this.pathElements.listToscaDataDefinition[this.pathElements.listToscaDataDefinition.length] = new ForwardingPathLink(fromNode, fromCP, toNode, toCP, fromCPOriginId, toCPOriginId);
    }

    deserialize(response:any) {
        this.name = response.name;
        this.destinationPortNumber = response.destinationPortNumber;
        this.protocol = response.protocol;
        if (response.pathElements && response.pathElements.listToscaDataDefinition) {
            let list = response.pathElements.listToscaDataDefinition;
            for (let i = 0; i < list.length; i++) {
                this.addPathLink(list[i].fromNode, list[i].fromCP, list[i].toNode, list[i].toCP, list[i].fromCPOriginId, list[i].toCPOriginId);
            }
        }
    }
};
