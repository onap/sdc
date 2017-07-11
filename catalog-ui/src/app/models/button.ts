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
 * Created by rc2122 on 5/10/2017.
 */
export class ButtonModel {
    text: string;
    cssClass: string;
    callback: Function;
    getDisabled:Function;
    constructor(text?:string, cssClass?:string, callback?:Function, getDisabled?:Function){
        this.text = text;
        this.cssClass = cssClass;
        this.callback = callback;
        this.getDisabled = getDisabled;

    }
}

export class ButtonsModelMap {
    [buttonName: string]: ButtonModel;
}
