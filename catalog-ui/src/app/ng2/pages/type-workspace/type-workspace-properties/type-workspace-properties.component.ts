/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

import {Component, Input, OnInit} from '@angular/core';
import {DataTypeModel} from "../../../../models/data-types";
import {DataTypeService} from "../../../services/data-type.service";
import {PropertyBEModel} from "../../../../models/properties-inputs/property-be-model";
import {Subject} from "rxjs";
import {debounceTime, distinctUntilChanged} from "rxjs/operators";
import {ModalService} from "../../../services/modal.service";
import {ModalModel} from "../../../../models/modal";
import {ButtonModel} from "../../../../models/button";
import {TranslateService} from "../../../shared/translator/translate.service";
import {AddPropertyComponent, PropertyValidationEvent} from "./add-property/add-property.component";

@Component({
    selector: 'app-type-workspace-properties',
    templateUrl: './type-workspace-properties.component.html',
    styleUrls: ['./type-workspace-properties.component.less']
})
export class TypeWorkspacePropertiesComponent implements OnInit {
    @Input() isViewOnly = true;
    @Input() dataType: DataTypeModel = new DataTypeModel();

    properties: Array<PropertyBEModel> = [];
    filteredProperties: Array<PropertyBEModel> = [];
    tableHeadersList: Array<TableHeader> = [];
    tableSortBy: string = 'name';
    tableColumnReverse: boolean = false;
    tableFilterTerm: string = undefined;
    tableSearchTermUpdate = new Subject<string>();

    constructor(private dataTypeService: DataTypeService, private modalService: ModalService, private translateService: TranslateService) {
    }

    ngOnInit(): void {
        this.initTable();
        this.initProperties();
        this.tableSearchTermUpdate.pipe(
            debounceTime(400),
            distinctUntilChanged())
        .subscribe(searchTerm => {
            this.filter(searchTerm);
        });
    }

    private initTable(): void {
        this.tableHeadersList = [
            {title: 'Name', property: 'name'},
            {title: 'Type', property: 'type'},
            {title: 'Schema', property: 'schema.property.type'},
            {title: 'Required', property: 'required'},
            {title: 'Description', property: 'description'},
        ];

        this.tableSortBy = this.tableHeadersList[0].property;
    }

    private initProperties(): void {
        this.dataTypeService.findAllProperties(this.dataType.uniqueId).subscribe(properties => {
            this.properties = properties.map(value => {
                const property = new PropertyBEModel(value);
                if (property.defaultValue) {
                    property.defaultValue = JSON.parse(property.defaultValue);
                }

                return property;
            });
            this.filteredProperties = Array.from(this.properties);
            this.sort();
        });
    }

    onUpdateSort(property: string): void {
        if (this.tableSortBy === property) {
            this.tableColumnReverse = !this.tableColumnReverse;
        } else {
            this.tableColumnReverse = false;
            this.tableSortBy = property;
        }
        this.sort();
    }

    private sort(): void {
        const field = this.tableSortBy;
        this.filteredProperties.sort((property1, property2) => {
            let result = 0;
            if (property1[field] > property2[field]) {
                result = 1;
            } else if (property1[field] < property2[field]) {
                result = -1;
            }
            return this.tableColumnReverse ? result * -1 : result;
        });
    }

    private filter(searchTerm?: string): void {
        if (searchTerm) {
            searchTerm = searchTerm.toLowerCase();
            this.filteredProperties = this.properties.filter(property =>
                property.name.toLowerCase().includes(searchTerm)
                || property.type.toLowerCase().includes(searchTerm)
                || (property.getSchemaType() && property.getSchemaType().toLowerCase().includes(searchTerm))
                || (property.description && property.description.toLowerCase().includes(searchTerm))
            );
        } else {
            this.filteredProperties = Array.from(this.properties);
        }
        this.sort();
    }

    private addProperty(property: PropertyBEModel) {
        this.properties.push(property);
        this.filter();
    }

    onClickAddProperty() {
        this.openAddPropertyModal();
    }

    private openAddPropertyModal(property?: PropertyBEModel, readOnly: boolean = false) {
        const modalTitle = this.translateService.translate('PROPERTY_ADD_MODAL_TITLE');
        const modalButtons = [];
        let disableSaveButtonFlag = true;
        let propertyFromModal: PropertyBEModel = undefined;
        const modal = this.modalService.createCustomModal(new ModalModel(
            'md',
            modalTitle,
            null,
            modalButtons,
            null
        ));
        if (readOnly) {
            modalButtons.push(new ButtonModel(this.translateService.translate('MODAL_CLOSE'), 'outline grey', () => {
                this.modalService.closeCurrentModal();
            }));
        } else {
            modalButtons.push(new ButtonModel(this.translateService.translate('MODAL_SAVE'), 'blue',
                () => {
                    disableSaveButtonFlag = true;
                    this.dataTypeService.createProperty(this.dataType.uniqueId, propertyFromModal).subscribe(property => {
                        this.addProperty(new PropertyBEModel(property));
                    });
                    this.modalService.closeCurrentModal();
                },
                (): boolean => {
                    return disableSaveButtonFlag
                }
            ));

            modalButtons.push(new ButtonModel(this.translateService.translate('MODAL_CANCEL'), 'outline grey', () => {
                this.modalService.closeCurrentModal();
            }));
        }

        this.modalService.addDynamicContentToModalAndBindInputs(modal, AddPropertyComponent, {
            'readOnly': readOnly,
            'property': property
        });
        modal.instance.dynamicContent.instance.onValidityChange.subscribe((validationEvent: PropertyValidationEvent) => {
            disableSaveButtonFlag = !validationEvent.isValid;
            if (validationEvent.isValid) {
                propertyFromModal = validationEvent.property;
            }
        });
        modal.instance.open();
    }

    onRowClick(property: PropertyBEModel) {
        this.openAddPropertyModal(property, true);
    }
}

interface TableHeader {
    title: string;
    property: string;
}