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
 * Created by rc2122 on 5/23/2017.
 */
import { Injectable } from '@angular/core';
import {WorkspaceMode, ComponentState, Role} from "../../../utils/constants";
import { Component as ComponentData } from "app/models";
import { CacheService } from "app/services/cache-service"

@Injectable()

export class ComponentModeService {

    constructor(private cacheService:CacheService) {
    }

    public getComponentMode = (component:ComponentData):WorkspaceMode => {//return if is edit or view for resource or service
        let mode = WorkspaceMode.VIEW;

        let user = this.cacheService.get("user");
        if (component.lifecycleState === ComponentState.NOT_CERTIFIED_CHECKOUT &&
            component.lastUpdaterUserId === user.userId) {
            if ((component.isService() || component.isResource()) && user.role == Role.DESIGNER) {
                mode = WorkspaceMode.EDIT;
            }
        }
        return mode;
    }
}

