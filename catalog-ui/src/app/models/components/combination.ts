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
 * Created by obarda on 2/4/2016.
 */
'use strict';
import * as _ from "lodash";
import {ICombinationService} from "../../services/components/combination-service";
import {Component} from "../../models";
import {ComponentMetadata} from "../component-metadata";


export class Combination extends Component {
    
    public componentService:ICombinationService;  

    constructor(componentService:ICombinationService, $q:ng.IQService, component?:Combination) {
        super(componentService, $q, component);
        if (component) {            
            this.filterTerm = this.name + ' ';
        }
        this.componentService = componentService;
        this.iconSprite = "sprite-resource-icons";
        this.categoryNormalizedName = "combination";
        this.lifecycleState ="CERTIFIED";
        this.icon = "combination";
        this.componentType = "Combination";  
        this.uuid = this.uniqueId;
    }

    getTypeUrl():string {
        return 'combinations/';
    }


    public setComponentMetadata(componentMetadata: ComponentMetadata) {
        super.setComponentMetadata(componentMetadata);
        this.setComponentDisplayData();        
    }

    setComponentDisplayData():void {
        this.filterTerm = this.name + ' ';
        this.iconSprite = "sprite-resource-icons";
        this.categoryNormalizedName = "combination";
        this.lifecycleState ="CERTIFIED";
        this.icon = "combination";
    }

    public toJSON = ():any => {
        let temp = angular.copy(this);
        temp.componentService = undefined;
        temp.filterTerm = undefined;
        temp.iconSprite = undefined;
        temp.mainCategory = undefined;
        temp.subCategory = undefined;
        temp.selectedInstance = undefined;
        temp.showMenu = undefined;
        temp.$q = undefined;
        temp.selectedCategory = undefined;
        temp.modules = undefined;
        temp.groupInstances = undefined;
        return temp;
    };
}

