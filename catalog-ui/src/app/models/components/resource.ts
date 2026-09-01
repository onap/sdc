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

/**
 * Created by obarda on 2/3/2016.
 */
'use strict';
import * as _ from "lodash";
import {ResourceType} from "../../utils/constants";
import {Component} from "./component";
import {FileUploadModel} from "../file-upload-model";
import {IResourceService} from "../../services/components/resource-service";
import {ComponentMetadata} from "../component-metadata";

export class Resource extends Component {

    public interfaces:any;
    public derivedFrom:Array<string>;
    public componentService:IResourceService;
    public resourceType:string;
    public payloadData:string;
    public payloadName:string;
    public importedFile:FileUploadModel;
    public resourceVendorModelNumber:string;

    // Onboarding parameters
    public csarUUID:string;
    public csarVersion:string;
    public csarVersionId: string;
    public csarPackageType:string;
    public packageId:string;

    constructor(componentService:IResourceService, component?:Resource) {
        super(componentService, component);
        if (component) {

            this.interfaces = component.interfaces;
            this.derivedFrom = component.derivedFrom;
            this.payloadData = component.payloadData ? component.payloadData : undefined;
            this.payloadName = component.payloadName ? component.payloadName : undefined;
            this.resourceType = component.resourceType;
            this.csarUUID = component.csarUUID;
            this.csarVersion = component.csarVersion;
            this.csarVersionId = component.csarVersionId;
            this.resourceVendorModelNumber = component.resourceVendorModelNumber;
            this.filterTerm = this.name + ' ' + this.description + ' ' + (this.tags ? this.tags.toString() : '') + ' ' + this.version + ' ' + this.resourceType;
            if (component.categories && component.categories[0] && component.categories[0].subcategories && component.categories[0].subcategories[0]) {
                component.mainCategory = component.categories[0].name;
                component.subCategory = component.categories[0].subcategories[0].name;
                this.selectedCategory = component.mainCategory + "_#_" + component.subCategory;
                this.importedFile = component.importedFile;
            }
        } else {
            this.resourceType = ResourceType.VF;
        }

        this.componentService = componentService;
        this.iconSprite = "sprite-resource-icons";
    }

    public setComponentMetadata(componentMetadata: ComponentMetadata) {
        super.setComponentMetadata(componentMetadata);
        this.resourceType = componentMetadata.resourceType;
        this.csarUUID = componentMetadata.csarUUID;
        this.csarVersion = componentMetadata.csarVersion;
        this.csarVersionId = componentMetadata.csarVersionId;
        this.derivedFrom = componentMetadata.derivedFrom;
        this.resourceVendorModelNumber = componentMetadata.resourceVendorModelNumber;
        this.setComponentDisplayData();
    };

    public isResource = ():boolean => true;

    public getComponentSubType = ():string => {
        return this.resourceType;
    };

    public isComplex = ():boolean => {
        return this.resourceType === ResourceType.VF || this.resourceType === ResourceType.PNF || this.resourceType === ResourceType.CVFC || this.resourceType === ResourceType.CR;
    };

    public isVl = ():boolean => {
        return ResourceType.VL == this.resourceType;
    };

    public isCsarComponent = ():boolean => {
        return !!this.csarUUID;
    };

    public createComponentOnServer = ():Promise<Component> => {
        return new Promise<Component>((resolve, reject) => {
            this.handleTags();
            if (this.importedFile) {
                this.payloadData = this.importedFile.base64;
                this.payloadName = this.importedFile.filename;
            }
            this.componentService.createComponent(this).then(
                (component:Resource):void => {
                    this.payloadData = undefined;
                    this.payloadName = undefined;
                    resolve(component);
                },
                (error:any):void => {
                    reject(error);
                });
        });
    };

    getTypeUrl():string {
        return 'resources/';
    }


    setComponentDisplayData():void {
        this.filterTerm = this.name + ' ' + this.description + ' ' + (this.tags ? this.tags.toString() : '') + ' ' + this.version + ' ' + this.resourceType;
        if (this.categories && this.categories[0] && this.categories[0].subcategories && this.categories[0].subcategories[0]) {
            this.mainCategory = this.categories[0].name;
            this.subCategory = this.categories[0].subcategories[0].name;
            this.selectedCategory = this.mainCategory + "_#_" + this.subCategory;
            this.iconSprite = "sprite-resource-icons";
        }
    };

    public toJSON = ():any => {
        let temp = _.cloneDeep(this);
        temp.componentService = undefined;
        temp.filterTerm = undefined;
        temp.iconSprite = undefined;
        temp.mainCategory = undefined;
        temp.subCategory = undefined;
        temp.selectedInstance = undefined;
        temp.showMenu = undefined;
        temp.selectedCategory = undefined;
        temp.importedFile = undefined;
        temp.modules = undefined;
        temp.groupInstances = undefined;
        temp.policies = undefined;
        return temp;
    };
}


