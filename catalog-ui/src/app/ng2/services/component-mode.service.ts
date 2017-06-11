/**
 * Created by rc2122 on 5/23/2017.
 */
import { Injectable } from '@angular/core';
import {WorkspaceMode, ComponentState, Role} from "../../utils/constants";
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

