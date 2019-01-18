import { ComponentMetadata } from "./component-metadata";

export class CombinationMetadata {
    deserialize (response): ComponentMetadata {
        let compMetaData:ComponentMetadata = new ComponentMetadata();
        compMetaData.abstract="";
        compMetaData.uniqueId = response.uniqueId;
        compMetaData.uuid = response.uniqueId;
        compMetaData.invariantUUID = response.uniqueId;
        compMetaData.name = response.name; 
        compMetaData.version = "0.1";
        compMetaData.creationDate;
        compMetaData.lastUpdateDate;
        compMetaData.description = response.description;
        compMetaData.lifecycleState ="CERTIFIED";
        compMetaData.tags=[response.name];
        compMetaData.icon="combination";
        compMetaData.contactId="";
        compMetaData.allVersions=[];
        compMetaData.creatorUserId="";
        compMetaData.creatorFullName="";
        compMetaData.lastUpdaterUserId="";
        compMetaData.lastUpdaterFullName="";
        compMetaData.componentType
        compMetaData.categories=[];
        compMetaData.highestVersion=true;
        compMetaData.normalizedName
        compMetaData.systemName
        compMetaData.archived=false;
        compMetaData.vspArchived=false;
        compMetaData.resourceType="null";
        compMetaData.csarUUID;
        compMetaData.csarVersion;
        compMetaData.derivedList=[];
        compMetaData.vendorName="";
        compMetaData.vendorRelease="";
        compMetaData.derivedFrom = [],
        compMetaData.resourceVendorModelNumber="";        
        compMetaData.projectCode="";
        compMetaData.distributionStatus="";
        compMetaData.ecompGeneratedNaming=false;
        compMetaData.namingPolicy="";
        compMetaData.serviceType=null;
        compMetaData.serviceRole=null;
        compMetaData.environmentContext="";
        compMetaData.instantiationType="";     
        compMetaData.state="CERTIFIED";
        return compMetaData;
    }
}

export interface Combinations {
    Combinations: Array<CombinationMetadata>;   
}