/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import {IMainCategory} from "./category";
/**
 * Created by obarda on 4/18/2017.
 */
export class ComponentMetadata {

    public abstract:string;
    public uniqueId:string;
    public uuid:string;
    public invariantUUID:string;
    public name:string;
    public version:string;
    public creationDate:number;
    public lastUpdateDate:number;
    public description:string;
    public lifecycleState:string;
    public tags:Array<string>;
    public icon:string;
    public contactId:string;
    public allVersions:any;
    public creatorUserId:string;
    public creatorFullName:string;
    public lastUpdaterUserId:string;
    public lastUpdaterFullName:string;
    public componentType:string;
    public categories:Array<IMainCategory>;
    public highestVersion:boolean;
    public normalizedName:string;
    public systemName:string;
    public archived:boolean;
    public vspArchived: boolean;
    public toscaResourceName: string;

    //Resource only
    public resourceType: string;
    public csarUUID:string;
    public csarVersion:string;
    public derivedList: string[];
    public vendorName:string;
    public vendorRelease:string;
    public derivedFrom: Array<string>;
    public resourceVendorModelNumber:string;

    //Service only
    public projectCode:string;
    public distributionStatus:string;
    public ecompGeneratedNaming: boolean;
    public namingPolicy: string;
    public serviceType:string;
    public serviceRole:string;
    public environmentContext:string;
    public instantiationType:string;



        //backend lifecycleState
    public state:string;

    deserialize (response): ComponentMetadata {
        this.abstract = response.abstract;
        this.uniqueId = response.uniqueId;
        this.uuid = response.uuid;
        this.invariantUUID = response.invariantUUID;
        this.contactId = response.contactId;
        this.categories = response.categories;
        this.creatorUserId = response.creatorUserId;
        this.creationDate = response.creationDate;
        this.creatorFullName = response.creatorFullName;
        this.description = response.description;
        this.icon = response.icon;
        this.lastUpdateDate = response.lastUpdateDate;
        this.lastUpdaterUserId = response.lastUpdaterUserId;
        this.lastUpdaterFullName = response.lastUpdaterFullName;
        this.lifecycleState = response.lifecycleState;
        this.name = response.name;
        this.version = response.version;
        this.tags = angular.copy(response.tags, this.tags);
        this.allVersions = response.allVersions;
        this.componentType = response.componentType;
        this.distributionStatus = response.distributionStatus;
        this.highestVersion = response.highestVersion;
        this.vendorName = response.vendorName;
        this.vendorRelease = response.vendorRelease;
        this.derivedList = response.derivedList;
        this.normalizedName = response.normalizedName;
        this.systemName = response.systemName;
        this.projectCode = response.projectCode;
        this.resourceType = response.resourceType;
        this.csarUUID = response.csarUUID;
        this.csarVersion = response.csarVersion;
        this.state = response.state;
        this.ecompGeneratedNaming = response.ecompGeneratedNaming;
        this.namingPolicy = response.namingPolicy;
        this.derivedFrom = response.derivedFrom;
        this.resourceVendorModelNumber = response.resourceVendorModelNumber;
        this.serviceType = response.serviceType;
        this.serviceRole = response.serviceRole;
        this.environmentContext = response.environmentContext;
        this.archived = response.archived;
        this.instantiationType = response.instantiationType;
        this.vspArchived = response.vspArchived;
        this.toscaResourceName = response.toscaResourceName;
        return this;
    }

}
