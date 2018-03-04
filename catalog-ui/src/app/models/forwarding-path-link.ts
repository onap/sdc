export class ForwardingPathLink {
    public ownerId: string;
    public fromNode:string;
    public fromCP:string;
    public toNode:string;
    public toCP:string;
    public toCPOriginId:string;
    public fromCPOriginId:string;


    constructor(fromNode:string, fromCP:string, toNode:string, toCP:string, fromCPOriginId:string, toCPOriginId:string) {
        this.fromCP = fromCP;
        this.fromNode = fromNode;
        this.toCP = toCP;
        this.toNode = toNode;
        this.fromCPOriginId = fromCPOriginId;
        this.toCPOriginId = toCPOriginId;
    }

}