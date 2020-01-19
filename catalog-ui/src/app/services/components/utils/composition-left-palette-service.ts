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
import IHttpPromiseCallbackArg = angular.IHttpPromiseCallbackArg;

export class LeftPaletteLoaderService {

    static '$inject' = [
        'Restangular',
        '$http',
        'sdcConfig',
        '$q',
        'ComponentFactory',
        'EventListenerService'

    ];

    constructor(protected restangular:restangular.IElement,
                protected $http:ng.IHttpService,
                protected sdcConfig:IAppConfigurtaion,
                protected $q:ng.IQService,
                protected ComponentFactory:ComponentFactory,
                protected EventListenerService:EventListenerService) {

        this.restangular.setBaseUrl(sdcConfig.api.root + sdcConfig.api.component_api_root);

    }

    leftPanelComponents:Array<LeftPaletteComponent>;

    public loadLeftPanel = (component:Component):void => {
        this.leftPanelComponents = [];
        this.updateLeftPaletteForTopologyTemplate(component);
    }

    private updateLeftPalette = (componentInternalType:string):void => {

        /* add components */
        const leftPaletteUrl = this.sdcConfig.api.uicache_root + this.sdcConfig.api.GET_uicache_left_palette;
        this.$http.get(leftPaletteUrl, {params: {'internalComponentType': componentInternalType}}).then((res) => res.data).then((leftPaletteComponentMetadata:Array<ComponentMetadata>) => {
        // this.restangular.one("resources").one('/latestversion/notabstract/metadata').get({'internalComponentType': componentInternalType}).then((leftPaletteComponentMetadata:Array<ComponentMetadata>) => {
            _.forEach(leftPaletteComponentMetadata, (componentMetadata:ComponentMetadata) => {
                this.leftPanelComponents.push(new LeftPaletteComponent(LeftPaletteMetadataTypes.Component, componentMetadata));
            });
            
            /* add groups */
            this.restangular.one('/groupTypes').get({'internalComponentType': componentInternalType}).then((leftPaletteGroupTypes:GroupTpes) => {
                _.forEach(leftPaletteGroupTypes, (groupMetadata: GroupMetadata) => {
                    this.leftPanelComponents.push(new LeftPaletteComponent(LeftPaletteMetadataTypes.Group, groupMetadata));
                }); 
                this.EventListenerService.notifyObservers(EVENTS.LEFT_PALETTE_UPDATE_EVENT);
            });

            /* add policies */
            this.restangular.one('/policyTypes').get({'internalComponentType': componentInternalType}).then((leftPalettePolicyTypes:PolicyTpes) => {
                _.forEach(leftPalettePolicyTypes, (policyMetadata: PolicyMetadata) => {
                    this.leftPanelComponents.push(new LeftPaletteComponent(LeftPaletteMetadataTypes.Policy, policyMetadata));
                }); 
                this.EventListenerService.notifyObservers(EVENTS.LEFT_PALETTE_UPDATE_EVENT);
            });
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
