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
import * as _ from "lodash";
import {LeftPaletteComponent, LeftPaletteMetadataTypes} from "app/models/components/displayComponent";
import {Component} from "app/models/components/component";
import {EventListenerService} from "../../event-listener-service";
import {ComponentFactory} from "../../../utils/component-factory";
import {IAppConfigurtaion} from "app/models/app-config";
import {ResourceType, ComponentType, EVENTS} from "../../../utils/constants";
import {ComponentMetadata} from "app/models/component-metadata";
import {GroupMetadata, GroupTpes} from "app/models/group-metadata";
import {PolicyMetadata, PolicyTpes} from "app/models/policy-metadata";
import {Resource} from "app/models/components/resource";

// export class LeftPaletteDataObject {
//     displayLeftPanelComponents:Array<LeftPaletteComponent>;
//     onFinishLoadingEvent:string;

//     constructor(onFinishEventListener:string) {

//         this.displayLeftPanelComponents = new Array<LeftPaletteComponent>();
//         this.onFinishLoadingEvent = onFinishEventListener;
//     }
// }

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

    // private serviceLeftPaletteData:LeftPaletteDataObject;
    // private resourceLeftPaletteData:LeftPaletteDataObject;
    // private resourcePNFLeftPaletteData:LeftPaletteDataObject;
    // private vlData:LeftPaletteDataObject;
    leftPanelComponents:Array<LeftPaletteComponent>;

    public loadLeftPanel = (component:Component):void => {
        // this.serviceLeftPaletteData = new LeftPaletteDataObject(EVENTS.SERVICE_LEFT_PALETTE_UPDATE_EVENT);
        // this.resourceLeftPaletteData = new LeftPaletteDataObject(EVENTS.RESOURCE_LEFT_PALETTE_UPDATE_EVENT);
        // this.resourcePNFLeftPaletteData = new LeftPaletteDataObject(EVENTS.RESOURCE_PNF_LEFT_PALETTE_UPDATE_EVENT);
        this.leftPanelComponents = [];
        this.updateLeftPaletteForTopologyTemplate(component);
    }

    // private getResourceLeftPaletteDataByResourceType = (resourceType:string):LeftPaletteDataObject => {
    //     if(resourceType == ResourceType.PNF) {
    //         return this.resourcePNFLeftPaletteData;
    //     }
    //     return this.resourceLeftPaletteData;
    // }

    private updateLeftPalette = (componentInternalType:string):void => {

        /* add components */
        this.restangular.one("resources").one('/latestversion/notabstract/metadata').get({'internalComponentType': componentInternalType}).then((leftPaletteComponentMetadata:Array<ComponentMetadata>) => {
            _.forEach(leftPaletteComponentMetadata, (componentMetadata:ComponentMetadata) => {
                this.leftPanelComponents.push(new LeftPaletteComponent(LeftPaletteMetadataTypes.Component, componentMetadata));
            });
            this.EventListenerService.notifyObservers(EVENTS.LEFT_PALETTE_UPDATE_EVENT);
        });

        /* add groups */
        //TODO: In backend implement like this:
        //this.restangular.one("groups").one('/latestversion/notabstract/metadata').get({'internalComponentType': componentInternalType}).then((leftPaletteComponentMetadata:Array<ComponentMetadata>) => {
        this.restangular.one('/groupTypes').get().then((leftPaletteGroupTypes:GroupTpes) => {
            _.forEach(leftPaletteGroupTypes.groupTypes, (groupMetadata: GroupMetadata) => {
                this.leftPanelComponents.push(new LeftPaletteComponent(LeftPaletteMetadataTypes.Group, groupMetadata));
            });
            this.EventListenerService.notifyObservers(EVENTS.LEFT_PALETTE_UPDATE_EVENT);
        });

        /* add policies */
        //TODO: In backend implement like this:
        //this.restangular.one("policies").one('/latestversion/notabstract/metadata').get({'internalComponentType': componentInternalType}).then((leftPaletteComponentMetadata:Array<ComponentMetadata>) => {
         this.restangular.one('/policyTypes').get().then((leftPalettePolicyTypes:PolicyTpes) => {
            _.forEach(leftPalettePolicyTypes.policyTypes, (policyMetadata: PolicyMetadata) => {
                this.leftPanelComponents.push(new LeftPaletteComponent(LeftPaletteMetadataTypes.Policy, policyMetadata));
            });
            this.EventListenerService.notifyObservers(EVENTS.LEFT_PALETTE_UPDATE_EVENT);
        });
    }

    public getLeftPanelComponentsForDisplay = (component:Component):Array<LeftPaletteComponent> => {
        return this.leftPanelComponents;
    };

    /**
     * Update left palete items according to current topology templates we are in.
     */
    public updateLeftPaletteForTopologyTemplate = (component:Component):void => {
        switch (component.componentType) {
            case ComponentType.SERVICE:
                this.updateLeftPalette(ComponentType.SERVICE);
                break;
            case ComponentType.RESOURCE:
                this.updateLeftPalette((<Resource>component).resourceType);
                break;
            default:
                console.log('ERROR: Component type '+ component.componentType + ' is not exists');
        }
    };
}
