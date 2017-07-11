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

'use strict';
import {SEVERITY} from "app/utils";

export interface IMessageModalModel {
    title:string;
    message:string;
    severity:SEVERITY;
}

export interface IMessageModalViewModelScope extends ng.IScope {
    footerButtons:Array<any>;
    messageModalModel:IMessageModalModel;
    modalInstanceError:ng.ui.bootstrap.IModalServiceInstance;
    ok():void;
}

export class MessageModalViewModel {

    constructor(private $baseScope:IMessageModalViewModelScope,
                private $baseModalInstance:ng.ui.bootstrap.IModalServiceInstance,
                private baseMessageModalModel:IMessageModalModel) {

        this.initScope(baseMessageModalModel);
    }

    private initScope = (messageModalViewModel:IMessageModalModel):void => {

        this.$baseScope.messageModalModel = messageModalViewModel;
        this.$baseScope.modalInstanceError = this.$baseModalInstance;

        this.$baseScope.ok = ():void => {
            this.$baseModalInstance.close();
        };

        this.$baseScope.footerButtons = [
            {
                'name': 'OK',
                'css': 'grey',
                'callback': this.$baseScope.ok
            }
        ];
    }
}
