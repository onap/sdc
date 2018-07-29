export class GroupMetadata {
    public name:string;
    public icon:string;
    public uniqueId: string;
    public type: string;
    public version: string;
    public description: string;
    public creationTime: number;
    public modificationTime: number;
    public highestVersion: boolean;
    public empty: boolean;

    deserialize (response): GroupMetadata {
        this.uniqueId = response.uniqueId;
        this.type = response.type;
        this.name = response.name;
        this.icon = response.icon;
        this.version = response.version;
        this.description = response.description;
        this.creationTime = response.creationTime;
        this.modificationTime = response.modificationTime;
        this.highestVersion = response.highestVersion;
        this.empty = response.empty;

        return this;
    }
}

export interface GroupTpes {
    groupTypes: Array<GroupMetadata>;
    excludeMapping: ExcludedGroupTypes;
}

export interface ExcludedGroupTypes {
    componentType: string;
    excludedGroupTypes: Array<string>;
}