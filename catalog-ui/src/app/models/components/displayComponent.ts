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
 * Created by obarda on 7/5/2016.
 */

'use strict';
import { ComponentType, SdcElementType } from '../../utils/constants';
import { ComponentMetadata } from '../component-metadata';
import { PolicyMetadata } from '../policy-metadata';
import { GroupMetadata } from '../group-metadata';
import { RequirementsGroup } from '../requirement';
import { CapabilitiesGroup } from '../capability';

export enum LeftPaletteMetadataTypes {
    Component = 'COMPONENT',
    Group = 'GROUP',
    Policy = 'POLICY'
}

export class LeftPaletteComponent {

    uniqueId: string;
    type: string;
    version: string;
    mainCategory: string;
    subCategory: string;
    componentSubType: string;
    searchFilterTerms: string;
    certifiedIconClass: string;
    isDraggable: boolean;
    uuid: string;
    name: string;
    lifecycleState: string;
    allVersions: any;
    componentType: string;
    systemName: string;
    invariantUUID: string;
    capabilities: CapabilitiesGroup;
    requirements: RequirementsGroup;
    categoryType: LeftPaletteMetadataTypes;
    resourceType: string;
    icon: string;

    constructor(metadataType: LeftPaletteMetadataTypes, item: ComponentMetadata | PolicyMetadata | GroupMetadata) {
        if (metadataType === LeftPaletteMetadataTypes.Policy) {
            this.initPolicy(item as PolicyMetadata);
            return;
        }

        if (metadataType === LeftPaletteMetadataTypes.Group) {
            this.initGroup(item as GroupMetadata);
            return;
        }

        if (metadataType === LeftPaletteMetadataTypes.Component) {
            this.initComponent(item as ComponentMetadata);
            return;
        }
    }

    private initComponent(component: ComponentMetadata): void {

        this.version = component.version;
        this.uniqueId = component.uniqueId;
        this.uuid = component.uuid;
        this.name = component.name;
        this.allVersions = component.allVersions;
        this.componentType = component.componentType;
        this.systemName = component.systemName;
        this.invariantUUID = component.invariantUUID;
        this.isDraggable = true;
        if (component.categories && component.categories[0] && component.categories[0].subcategories && component.categories[0].subcategories[0]) {
            this.mainCategory = component.categories[0].name;
            this.subCategory = component.categories[0].subcategories[0].name;
        } else {
            this.mainCategory = 'Generic';
            this.subCategory = 'Generic';
        }
        // this.categoryType = LeftPaletteMetadataTypes.Component;
        // this.componentSubType = component. ? component.resourceType: ComponentType.SERVICE_PROXY;
        this.searchFilterTerms = (this.name + ' ' + component.description + ' ' + component.tags.join(' ')).toLowerCase() + ' ' + component.version;
        this.icon = component.icon;
        this.certifiedIconClass = component.lifecycleState != 'CERTIFIED' ? 'non-certified' : ''; // need to fix after onap fix

    }

    private initGroup(group: GroupMetadata): void {
        this.categoryType = LeftPaletteMetadataTypes.Group;
        this.uniqueId = group.uniqueId;
        this.name = group.name;
        this.mainCategory = 'Groups';
        this.subCategory = 'Groups';
        this.version = group.version;
        this.type = group.type;
        this.componentSubType = SdcElementType.GROUP;
        this.icon = SdcElementType.GROUP;
        this.searchFilterTerms = this.type + ' ' + group.name + ' ' + group.version;
        this.isDraggable = false;
    }

    private initPolicy(policy: PolicyMetadata): void {
        this.categoryType = LeftPaletteMetadataTypes.Policy;
        this.uniqueId = policy.uniqueId;
        this.name = policy.name;
        this.mainCategory = 'Policies';
        this.subCategory = 'Policies';
        this.version = policy.version;
        this.type = policy.type;
        this.componentSubType = SdcElementType.POLICY;
        this.icon = SdcElementType.POLICY;
        this.searchFilterTerms = this.type + ' ' + policy.name + ' ' + policy.version;
        this.isDraggable = false;
    }

    public getComponentSubType = (): string => {
        return this.componentSubType;
    };
}
