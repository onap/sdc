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
 * Created by obarda on 7/31/2016.
 */
'use strict';

export class Tab {

    public templateUrl:string;
    public controller:string;
    public data:any;
    public icon:string;
    public name:string;
    public isViewMode:boolean;

    constructor(templateUrl:string, controller:string, name:string, isViewMode:boolean, data?:any, icon?:string) {

        this.templateUrl = templateUrl;
        this.controller = controller;
        this.icon = icon;
        this.data = data;
        this.name = name;
        this.isViewMode = isViewMode;
    }
}



