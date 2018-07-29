export class PolicyMetadata {
    public uniqueId: string;
    public name:string;
    public icon:string;
    public type: string;
    public version: string;
    public description: string;
    public creationTime: number;
    public modificationTime: number;
    public highestVersion: boolean;
    public empty: boolean;

    deserialize (response): PolicyMetadata {
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

export interface PolicyTpes {
    policyTypes: Array<PolicyMetadata>;
    excludeMapping: ExcludedPolicyTypes;
}

export interface ExcludedPolicyTypes {
    componentType: string;
    excludedPolicyTypes: Array<string>;
}