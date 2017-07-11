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
 * Created by obarda on 6/30/2016.
 */
/**
 * Created by obarda on 2/4/2016.
 */
'use strict';
import {PropertyModel} from "../properties";
import {ArtifactModel} from "../artifacts";
import {CommonUtils} from "../../utils/common-utils";
export class Module {

    public name:string;
    public groupUUID:string;
    public invariantUUID:string;
    public propertyValueCounter:number;
    public type:string;
    public typeUid:string;
    public uniqueId:string;
    public version:string;
    public artifacts:Array<string> | Array<ArtifactModel>;
    public artifactsUuid:Array<string>;
    public properties:Array<PropertyModel>;
    public members:Array<string>;
    public customizationUUID:string;
    public groupInstanceUniqueId:string; // This will only have a value if this is a group instance

    constructor(module?:Module) {
        if (module) {
            this.name = module.name;
            this.groupUUID = module.groupUUID;
            this.invariantUUID = module.invariantUUID;
            this.propertyValueCounter = module.propertyValueCounter;
            this.type = module.type;
            this.typeUid = module.typeUid;
            this.uniqueId = module.uniqueId;
            this.version = module.version;
            this.artifacts = module.artifacts;
            this.artifactsUuid = module.artifactsUuid;
            this.properties = CommonUtils.initProperties(module.properties);
            this.members = module.members;
            this.customizationUUID = module.customizationUUID;
            this.groupInstanceUniqueId = module.groupInstanceUniqueId;
            this.name = this.name.replace(/:/g, '..');

        }
    }
}

export class DisplayModule extends Module {

    isBase:string;
    artifacts:Array<ArtifactModel>;

    //custom properties
    public vfInstanceName:string;
    public heatName:string;
    public moduleName:string;
    public customizationUUID:string;


    constructor(displayModule?:DisplayModule) {
        super(displayModule);

        this.isBase = displayModule.isBase;
        this.customizationUUID = displayModule.customizationUUID;
        this.initArtifactsForDisplay(displayModule.artifacts);

        //splitting module name for display and edit
        let splitName:Array<string> = this.name.split('..');
        this.vfInstanceName = splitName[0];
        this.heatName = splitName[1];
        this.moduleName = splitName[2];
    }

    private initArtifactsForDisplay = (artifacts:Array<ArtifactModel>):void => {
        this.artifacts = new Array<ArtifactModel>();
        _.forEach(artifacts, (artifact:ArtifactModel) => {
            this.artifacts.push(new ArtifactModel(artifact));
        });
    };

    public updateName = ():void => {
        this.name = this.vfInstanceName + '..' + this.heatName + '..' + this.moduleName;
    };

    public toJSON = ():any => {
        this.vfInstanceName = undefined;
        this.heatName = undefined;
        this.moduleName = undefined;
        this.isBase = undefined;
        this.artifacts = undefined;
        return this;
    };
}
