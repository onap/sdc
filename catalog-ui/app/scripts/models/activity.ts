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
 * Created by obarda on 19/11/2015.
 */
/// <reference path="../references"/>
module Sdc.Models {
    'use strict';

    /*this is in uppercase because of the server response*/
    export class Activity{
        TIMESTAMP: string;
        ACTION:string;
        MODIFIER:string;
        STATUS:string;
        DESC:string;
        COMMENT:string;
        //custom data
        public  dateFormat:string;

        constructor() {
        }
        public toJSON = ():any => {
            this.dateFormat = undefined;
            return this;
        };

    }
}


