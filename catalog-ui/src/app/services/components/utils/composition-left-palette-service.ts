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
 * Created by obarda on 3/13/2016.
 */
'use strict';
import {LeftPaletteComponent} from "../../../models/components/displayComponent";
import {Component} from "../../../models/components/component";
import {EventListenerService} from "../../event-listener-service";
import {ComponentFactory} from "../../../utils/component-factory";
import {IAppConfigurtaion} from "../../../models/app-config";
import {ResourceType, ComponentType, EVENTS} from "../../../utils/constants";
import {ComponentMetadata} from "../../../models/component-metadata";
import {Resource} from "app/models/components/resource";

export class LeftPaletteDataObject {
    displayLeftPanelComponents:Array<LeftPaletteComponent>;
    onFinishLoadingEvent:string;

    constructor(onFinishEventListener:string) {

        this.displayLeftPanelComponents = new Array<LeftPaletteComponent>();
        this.onFinishLoadingEvent = onFinishEventListener;
    }
}

export class LeftPaletteLoaderService {

    static '$inject' = [
        'Restangular',
        'sdcConfig',
        '$q',
        'ComponentFactory',
        'EventListenerService'

    ];

    constructor(protected restangular:restangular.IElement,
                protected sdcConfig:IAppConfigurtaion,
                protected $q:ng.IQService,
                protected ComponentFactory:ComponentFactory,
                protected EventListenerService:EventListenerService) {

        this.restangular.setBaseUrl(sdcConfig.api.root + sdcConfig.api.component_api_root);

    }

    private serviceLeftPaletteData:LeftPaletteDataObject;
    private resourceLeftPaletteData:LeftPaletteDataObject;
    private resourcePNFLeftPaletteData:LeftPaletteDataObject;
    private vlData:LeftPaletteDataObject;

    public loadLeftPanel = (component:Component):void => {
        this.serviceLeftPaletteData = new LeftPaletteDataObject(EVENTS.SERVICE_LEFT_PALETTE_UPDATE_EVENT);
        this.resourceLeftPaletteData = new LeftPaletteDataObject(EVENTS.RESOURCE_LEFT_PALETTE_UPDATE_EVENT);
        this.resourcePNFLeftPaletteData = new LeftPaletteDataObject(EVENTS.RESOURCE_PNF_LEFT_PALETTE_UPDATE_EVENT);
        this.updateComponentLeftPalette(component);
    }

    private getResourceLeftPaletteDataByResourceType = (resourceType:string):LeftPaletteDataObject => {
        if(resourceType == ResourceType.PNF) {
            return this.resourcePNFLeftPaletteData;
        }
        return this.resourceLeftPaletteData;
    }

    private onFinishLoading = (componentType:string, leftPaletteData:LeftPaletteDataObject):void => {
        this.EventListenerService.notifyObservers(leftPaletteData.onFinishLoadingEvent);
    };

    private updateLeftPalette = (componentType, componentInternalType:string, leftPaletteData:LeftPaletteDataObject):void => {

        this.restangular.one("resources").one('/latestversion/notabstract/metadata').get({'internalComponentType': componentInternalType}).then((leftPaletteComponentMetadata:Array<ComponentMetadata>) => {
            _.forEach(leftPaletteComponentMetadata, (componentMetadata:ComponentMetadata) => {
                leftPaletteData.displayLeftPanelComponents.push(new LeftPaletteComponent(componentMetadata));
            });
            this.onFinishLoading(componentType, leftPaletteData);
        });
    };

    public getLeftPanelComponentsForDisplay = (component:Component):Array<LeftPaletteComponent> => {
        switch (component.componentType) {
            case ComponentType.SERVICE:
                return this.serviceLeftPaletteData.displayLeftPanelComponents;
            default://resource
                return this.getResourceLeftPaletteDataByResourceType((<Resource>component).resourceType).displayLeftPanelComponents;
        }
    };

    public updateComponentLeftPalette = (component:Component):void => {
        switch (component.componentType) {
            case ComponentType.SERVICE:
                this.updateLeftPalette(ComponentType.SERVICE, ComponentType.SERVICE, this.serviceLeftPaletteData);
                break;
            case ComponentType.RESOURCE:
                this.updateLeftPalette(ComponentType.RESOURCE, (<Resource>component).resourceType, this.getResourceLeftPaletteDataByResourceType((<Resource>component).resourceType));
                break;
            default:
                console.log('ERROR: Component type '+ component.componentType + ' is not exists');
        }
    };
}
