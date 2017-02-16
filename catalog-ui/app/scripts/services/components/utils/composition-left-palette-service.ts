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
/// <reference path="../../../references"/>
module Sdc.Services.Components {

    'use strict';

    export class LeftPanelLatestVersion {
        uid: string;
        version: string;
    }

    export class LeftPaletteDataObject {
        currentUpdatingIdsList: Array<string>;
        latestVersionAndIdsList: Array<LeftPanelLatestVersion>;
        fullDataLeftPaletteComponents: Array<Models.Components.Component>;
        displayLeftPanelComponents: Array<Models.DisplayComponent>;
        onFinishLoadingEvent: string;

        constructor(onFinishEventListener: string) {

            this.fullDataLeftPaletteComponents = new Array<Models.Components.Component>();
            this.displayLeftPanelComponents = new Array<Models.DisplayComponent>();
            this.currentUpdatingIdsList = new Array<string>();
            this.latestVersionAndIdsList = new Array<LeftPanelLatestVersion>();
            this.onFinishLoadingEvent = onFinishEventListener;
        }
    }

    export class LeftPaletteLoaderService {

        static '$inject' = [
            'Restangular',
            'sdcConfig',
            '$q',
            '$base64',
            'ComponentFactory',
            'EventListenerService'

        ];

        constructor(protected restangular: restangular.IElement,
                    protected sdcConfig: Models.IAppConfigurtaion,
                    protected $q: ng.IQService,
                    protected $base64: any,
                    protected ComponentFactory: Utils.ComponentFactory,
                    protected EventListenerService: Services.EventListenerService) {

            this.restangular.setBaseUrl(sdcConfig.api.root + sdcConfig.api.component_api_root);
        }

        private serviceLeftPaletteData: LeftPaletteDataObject;
        private resourceLeftPaletteData: LeftPaletteDataObject;
        private productLeftPaletteData: LeftPaletteDataObject;
        private vlData: LeftPaletteDataObject;

        public loadLeftPanel = (): void => {

            this.serviceLeftPaletteData = new LeftPaletteDataObject(Utils.Constants.EVENTS.SERVICE_LEFT_PALETTE_UPDATE_EVENT);
            this.resourceLeftPaletteData = new LeftPaletteDataObject(Utils.Constants.EVENTS.RESOURCE_LEFT_PALETTE_UPDATE_EVENT);
            this.productLeftPaletteData = new LeftPaletteDataObject(Utils.Constants.EVENTS.PRODUCT_LEFT_PALETTE_UPDATE_EVENT);
            this.vlData = new LeftPaletteDataObject(Utils.Constants.EVENTS.VL_LEFT_PALETTE_UPDATE_EVENT);

            //initiating service palette
            this.updateComponentLeftPalette(Utils.Constants.ComponentType.SERVICE);

            //initiating resource palette
            this.updateComponentLeftPalette(Utils.Constants.ComponentType.RESOURCE);

            //initiating product palette
            this.updateComponentLeftPalette(Utils.Constants.ComponentType.PRODUCT);

            //initiating vl
            this.updateComponentLeftPalette(Utils.Constants.ResourceType.VL);
        };

        private updateData = (latestVersionComponents: Array<Models.Components.Component>, leftPaletteDataObj: LeftPaletteDataObject) => {

            let fullDataComponentsArray: Array<Models.Components.Component> = new Array<Models.Components.Component>();
            let displayComponentsArray: Array<Models.DisplayComponent> = new Array<Models.DisplayComponent>();

            _.forEach(latestVersionComponents, (componentObj: any) => {
                let component: Models.Components.Component = this.ComponentFactory.createComponent(componentObj);
                fullDataComponentsArray.push(component);
                displayComponentsArray.push(new Models.DisplayComponent(component));
            });

            leftPaletteDataObj.fullDataLeftPaletteComponents = leftPaletteDataObj.fullDataLeftPaletteComponents.concat(fullDataComponentsArray);
            leftPaletteDataObj.displayLeftPanelComponents = leftPaletteDataObj.displayLeftPanelComponents.concat(displayComponentsArray);
        };

        private getTypeUrl = (componentType: string): string => {
            return Utils.Constants.ComponentType.PRODUCT === componentType ? "services" : "resources";
        };

        private onFinishLoading = (componentType: string, leftPaletteData: LeftPaletteDataObject): void => {
            leftPaletteData.currentUpdatingIdsList = [];
            this.EventListenerService.notifyObservers(leftPaletteData.onFinishLoadingEvent);
        };

        private getPartialLastVersionFullComponents = (componentType: string, componentInternalType: string, leftPaletteData: LeftPaletteDataObject): void => {
            this.restangular.one(this.getTypeUrl(componentType)).one('/latestversion/notabstract').customPOST(leftPaletteData.currentUpdatingIdsList, '', {'internalComponentType': componentInternalType}).then((componentsArray: any) => {
                this.updateData(componentsArray, leftPaletteData);
                this.onFinishLoading(componentType, leftPaletteData); //when finish loading update view
            });
        };

        private removeNotUpdatedComponents = (leftPaletteObject: LeftPaletteDataObject) => {

            leftPaletteObject.fullDataLeftPaletteComponents = _.filter(leftPaletteObject.fullDataLeftPaletteComponents, (component)=> {
                return leftPaletteObject.currentUpdatingIdsList.indexOf(component.uniqueId) != -1;
            });
            leftPaletteObject.displayLeftPanelComponents = _.filter(leftPaletteObject.displayLeftPanelComponents, (component)=> {
                return leftPaletteObject.currentUpdatingIdsList.indexOf(component.uniqueId) != -1;
            });
        };

        private findIdsToUpdate = (leftPaletteObj: LeftPaletteDataObject): Array<string> => {
            let idsToUpdate = <string[]>_.difference(leftPaletteObj.currentUpdatingIdsList, _.map(leftPaletteObj.fullDataLeftPaletteComponents, 'uniqueId'));
            let neededUpdate = _.filter(leftPaletteObj.fullDataLeftPaletteComponents, (component) => {
                let updated = _.find(leftPaletteObj.latestVersionAndIdsList, (versionAndId: LeftPanelLatestVersion) => {
                    return versionAndId.uid === component.uniqueId && versionAndId.version != component.version
                });
                return updated != undefined;
            });
            if (neededUpdate && neededUpdate.length > 0) {
                let neededUpdateIds = <string[]>_.map(neededUpdate, 'uid');
                idsToUpdate.concat(neededUpdateIds);
            }
            return idsToUpdate;
        };

        private updateCurrentIdsList = (componentType: string, leftPaletteObj: LeftPaletteDataObject): void=> {
            this.removeNotUpdatedComponents(leftPaletteObj);
            leftPaletteObj.currentUpdatingIdsList = this.findIdsToUpdate(leftPaletteObj);
            //remove all components that needed update from current lists
            if (leftPaletteObj.currentUpdatingIdsList.length > 0) {
                leftPaletteObj.displayLeftPanelComponents = _.filter(leftPaletteObj.displayLeftPanelComponents, (component)=> {
                    return leftPaletteObj.currentUpdatingIdsList.indexOf(component.uniqueId) === -1;
                });
                leftPaletteObj.fullDataLeftPaletteComponents = _.filter(leftPaletteObj.fullDataLeftPaletteComponents, (component)=> {
                    return leftPaletteObj.currentUpdatingIdsList.indexOf(component.uniqueId) === -1;
                });
            }
        };

        private updateLeftPalette = (componentType, componentInternalType: string, leftPaletteData: LeftPaletteDataObject): void => {
            if (leftPaletteData.currentUpdatingIdsList.length > 0) return; //this means the service is still performing update
            this.restangular.one(this.getTypeUrl(componentType)).one('/latestversion/notabstract/uidonly').get({'internalComponentType': componentInternalType}).then((latestVersionUniqueIds: Array<LeftPanelLatestVersion>) => {
                leftPaletteData.latestVersionAndIdsList = latestVersionUniqueIds;
                leftPaletteData.currentUpdatingIdsList = <string[]>_.map(latestVersionUniqueIds, 'uid')

                if (leftPaletteData.fullDataLeftPaletteComponents.length === 0) { //this is when first loading product or resource left palette
                    this.getPartialLastVersionFullComponents(componentType, componentInternalType, leftPaletteData);
                } else {
                    this.updateCurrentIdsList(componentType, leftPaletteData);
                    if (leftPaletteData.currentUpdatingIdsList.length === 0) {
                        this.onFinishLoading(componentType, leftPaletteData); //when finish loading update view
                        return;
                    }
                    this.getPartialLastVersionFullComponents(componentType, componentInternalType, leftPaletteData);
                }
            });
        };

        public getLeftPanelComponentsForDisplay = (componentType: string): Array<Models.DisplayComponent> => {
            switch (componentType) {
                case Utils.Constants.ComponentType.SERVICE:
                    return this.serviceLeftPaletteData.displayLeftPanelComponents;
                case Utils.Constants.ComponentType.PRODUCT:
                    return this.productLeftPaletteData.displayLeftPanelComponents;
                default:
                    return this.resourceLeftPaletteData.displayLeftPanelComponents;
            }
        };

        public getFullDataComponentList = (componentType: string): Array<Models.Components.Component> => {
            switch (componentType) {
                case Utils.Constants.ResourceType.VL:
                    return this.vlData.fullDataLeftPaletteComponents;
                case Utils.Constants.ComponentType.SERVICE:
                    return this.serviceLeftPaletteData.fullDataLeftPaletteComponents;
                case Utils.Constants.ComponentType.PRODUCT:
                    return this.productLeftPaletteData.fullDataLeftPaletteComponents;
                default :
                    return this.resourceLeftPaletteData.fullDataLeftPaletteComponents;
            }
        };

        public getFullDataComponentListWithVls = (componentType: string): Array<Models.Components.Component> => {
            let listPart1: Array<Models.Components.Component>;
            let listPart2: Array<Models.Components.Component>;
            if (componentType === Utils.Constants.ResourceType.VL) {
                listPart1 = [];
                listPart2 = this.getFullDataComponentList(Utils.Constants.ResourceType.VL);
            } else {
                listPart1 = this.getFullDataComponentList(componentType);
                listPart2 = this.getFullDataComponentList(Utils.Constants.ResourceType.VL);
            }
            return listPart1.concat(listPart2);
        };

        public updateSpecificComponentLeftPalette = (component: Models.Components.Component, componentType: string): void => {
            let listComponents: Array<Models.Components.Component> = this.getFullDataComponentList(componentType);
            for (let i in listComponents) {
                if (listComponents[i].uniqueId === component.uniqueId) {
                    listComponents[i] = component;
                }
            }
        };

        public updateComponentLeftPalette = (componentType): void => {
            switch (componentType) {
                case Utils.Constants.ResourceType.VL:
                    this.updateLeftPalette(Utils.Constants.ComponentType.RESOURCE, Utils.Constants.ResourceType.VL, this.vlData);
                    break;
                case Utils.Constants.ComponentType.SERVICE:
                    this.updateLeftPalette(Utils.Constants.ComponentType.SERVICE, Utils.Constants.ComponentType.SERVICE, this.serviceLeftPaletteData);
                    break;
                case Utils.Constants.ComponentType.PRODUCT:
                    this.updateLeftPalette(Utils.Constants.ComponentType.PRODUCT, Utils.Constants.ComponentType.SERVICE, this.productLeftPaletteData);
                    break;
                default:
                    this.updateLeftPalette(Utils.Constants.ComponentType.RESOURCE, Utils.Constants.ResourceType.VF, this.resourceLeftPaletteData);
            }
        };
    }
}
