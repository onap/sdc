export class SimpleFlatProperty {
    uniqueId: string;
    path: string;
    name: string;
    parentName: string;
    instanceName: string;

    constructor(uniqueId?: string, path?: string, name?: string, parentName?: string, instanceName?: string) {
        this.uniqueId = uniqueId;
        this.path = path;
        this.name = name;
        this.parentName = parentName;
        this.instanceName = instanceName;
    }
}