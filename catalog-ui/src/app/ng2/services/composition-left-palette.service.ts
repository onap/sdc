/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
import {forwardRef, Inject, Injectable} from "@angular/core";
import {HttpClient, HttpParams} from "@angular/common/http";
import {LeftPaletteComponent, LeftPaletteMetadataTypes} from "app/models/components/displayComponent";
import {Component} from "app/models/components/component";
import {EventListenerService} from "./event-listener.service";
// Nothing injects LeftPaletteLoaderService. The composition palette is served by
// CompositionPaletteService (ng2/pages/composition/palette/services/palette.service.ts), which builds
// the same sdcConfig.api.uicache_root + GET_uicache_left_palette URL itself; only app.module.ts's
// provider list and this file's spec still name this class, so the forwardRef below guards a cycle
// that is never constructed. Left verbatim because this change only moves files - the class is a
// deletion candidate for the dead-code sweep.
import {ComponentFactory} from "../../utils/component-factory";
import {IAppConfigurtaion} from "app/models/app-config";
import {ResourceType, ComponentType, EVENTS} from "../../utils/constants";
import {ComponentMetadata} from "app/models/component-metadata";
import {GroupMetadata, GroupTpes} from "app/models/group-metadata";
import {PolicyMetadata, PolicyTpes} from "app/models/policy-metadata";
import {Resource} from "app/models/components/resource";
import {SdcConfigToken} from "../config/sdc-config.config";

@Injectable()
export class LeftPaletteLoaderService {

    private baseUrl: string;
    // Non-enumerable for consistency with other migrated services (§SS pattern).
    private http: HttpClient;

    leftPanelComponents: Array<LeftPaletteComponent>;

    constructor(@Inject(SdcConfigToken) private sdcConfig: IAppConfigurtaion,
                http: HttpClient,
                @Inject(forwardRef(() => ComponentFactory)) private ComponentFactory: ComponentFactory,
                private EventListenerService: EventListenerService) {
        this.baseUrl = sdcConfig.api.root + sdcConfig.api.component_api_root;
        Object.defineProperty(this, 'http', {value: http, enumerable: false, writable: false, configurable: true});
    }

    public loadLeftPanel = (component: Component): void => {
        this.leftPanelComponents = [];
        this.updateLeftPaletteForTopologyTemplate(component);
    }

    private updateLeftPalette = (componentInternalType: string): void => {

        /* add components */
        this.http.get<Array<ComponentMetadata>>(
            this.sdcConfig.api.uicache_root + this.sdcConfig.api.GET_uicache_left_palette,
            {params: new HttpParams().set('internalComponentType', componentInternalType)}
        ).subscribe((leftPaletteComponentMetadata: Array<ComponentMetadata>) => {
            _.forEach(leftPaletteComponentMetadata, (componentMetadata: ComponentMetadata) => {
                this.leftPanelComponents.push(new LeftPaletteComponent(LeftPaletteMetadataTypes.Component, componentMetadata));
            });

            /* add groups */
            this.http.get<GroupTpes>(
                this.baseUrl + 'groupTypes',
                {params: new HttpParams().set('internalComponentType', componentInternalType)}
            ).subscribe((leftPaletteGroupTypes: GroupTpes) => {
                _.forEach(leftPaletteGroupTypes, (groupMetadata: GroupMetadata) => {
                    this.leftPanelComponents.push(new LeftPaletteComponent(LeftPaletteMetadataTypes.Group, groupMetadata));
                });
                this.EventListenerService.notifyObservers(EVENTS.LEFT_PALETTE_UPDATE_EVENT);
            });

            /* add policies */
            this.http.get<PolicyTpes>(
                this.baseUrl + 'policyTypes',
                {params: new HttpParams().set('internalComponentType', componentInternalType)}
            ).subscribe((leftPalettePolicyTypes: PolicyTpes) => {
                _.forEach(leftPalettePolicyTypes, (policyMetadata: PolicyMetadata) => {
                    this.leftPanelComponents.push(new LeftPaletteComponent(LeftPaletteMetadataTypes.Policy, policyMetadata));
                });
                this.EventListenerService.notifyObservers(EVENTS.LEFT_PALETTE_UPDATE_EVENT);
            });
        });
    }

    public getLeftPanelComponentsForDisplay = (component: Component): Array<LeftPaletteComponent> => {
        return this.leftPanelComponents;
    };

    /**
     * Update left palette items according to current topology templates we are in.
     */
    public updateLeftPaletteForTopologyTemplate = (component: Component): void => {
        switch (component.componentType) {
            case ComponentType.SERVICE:
                this.updateLeftPalette(ComponentType.SERVICE);
                break;
            case ComponentType.RESOURCE:
                this.updateLeftPalette((<Resource>component).resourceType);
                break;
            default:
                console.log('ERROR: Component type ' + component.componentType + ' is not exists');
        }
    };
}
