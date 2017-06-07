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
    private productLeftPaletteData:LeftPaletteDataObject;
    private vlData:LeftPaletteDataObject;

    public loadLeftPanel = (componentType: string):void => {
        this.serviceLeftPaletteData = new LeftPaletteDataObject(EVENTS.SERVICE_LEFT_PALETTE_UPDATE_EVENT);
        this.resourceLeftPaletteData = new LeftPaletteDataObject(EVENTS.RESOURCE_LEFT_PALETTE_UPDATE_EVENT);
        this.updateComponentLeftPalette(componentType);
    }


    private getTypeUrl = (componentType:string):string => {
        return ComponentType.PRODUCT === componentType ? "services" : "resources";
    };

    private onFinishLoading = (componentType:string, leftPaletteData:LeftPaletteDataObject):void => {
        this.EventListenerService.notifyObservers(leftPaletteData.onFinishLoadingEvent);
    };

    private updateLeftPalette = (componentType, componentInternalType:string, leftPaletteData:LeftPaletteDataObject):void => {

        this.restangular.one(this.getTypeUrl(componentType)).one('/latestversion/notabstract/metadata').get({'internalComponentType': componentInternalType}).then((leftPaletteComponentMetadata:Array<ComponentMetadata>) => {
            _.forEach(leftPaletteComponentMetadata, (componentMetadata:ComponentMetadata) => {
                leftPaletteData.displayLeftPanelComponents.push(new LeftPaletteComponent(componentMetadata));
            });
            this.onFinishLoading(componentType, leftPaletteData);
        });
    };

    public getLeftPanelComponentsForDisplay = (componentType:string):Array<LeftPaletteComponent> => {
        switch (componentType) {
            case ComponentType.SERVICE:
                return this.serviceLeftPaletteData.displayLeftPanelComponents;
            case ComponentType.PRODUCT:
                return this.productLeftPaletteData.displayLeftPanelComponents;
            default:
                return this.resourceLeftPaletteData.displayLeftPanelComponents;
        }
    };

    public updateComponentLeftPalette = (componentType):void => {
        switch (componentType) {
            case ResourceType.VL:
                this.updateLeftPalette(ComponentType.RESOURCE, ResourceType.VL, this.vlData);
                break;
            case ComponentType.SERVICE:
                this.updateLeftPalette(ComponentType.SERVICE, ComponentType.SERVICE, this.serviceLeftPaletteData);
                break;
            case ComponentType.PRODUCT:
                this.updateLeftPalette(ComponentType.PRODUCT, ComponentType.SERVICE, this.productLeftPaletteData);
                break;
            default:
                this.updateLeftPalette(ComponentType.RESOURCE, ResourceType.VF, this.resourceLeftPaletteData);
        }
    };
}
