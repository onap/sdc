/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Fujitsu Limited. All rights reserved.
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

import * as _ from "lodash";
import {Component} from '@angular/core';
import {DropdownValue} from "app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component";
import { DataTypeService } from "app/ng2/services/data-type.service";
import {PropertyBEModel, DataTypesMap} from "app/models";
import {PROPERTY_DATA} from "app/utils";
import {PROPERTY_TYPES} from "../../../../utils";
import { ModalService } from "app/ng2/services/modal.service";
import { InstancePropertiesAPIMap } from "app/models/properties-inputs/property-fe-map";
import { ModalModel } from "app/models/modal";
import { DataTypeModel } from "app/models/data-types";



@Component({
    selector: 'declare-list',
    templateUrl: './declare-list.component.html',
    styleUrls:['./declare-list.component.less'],
})

export class DeclareListComponent {

    typesProperties: Array<DropdownValue>;
    typesSchemaProperties: Array<DropdownValue>;
    propertyModel: PropertyBEModel;
    //propertyNameValidationPattern:RegExp = /^[a-zA-Z0-9_:-]{1,50}$/;
    //commentValidationPattern:RegExp = /^[\u0000-\u00BF]*$/;
    //types:Array<string>;
    dataTypes:DataTypesMap;
    isLoading:boolean;
    inputsToCreate:InstancePropertiesAPIMap;
    propertiesListString:string;
    privateDataType: DataTypeModel;

    constructor(protected dataTypeService:DataTypeService, private modalService:ModalService) {}

    ngOnInit() {
        console.log('DeclareListComponent.ngOnInit() - enter');
        this.propertyModel = new PropertyBEModel();
        this.propertyModel.type = '';
        this.propertyModel.schema.property.type = '';
        const types: Array<string> =  PROPERTY_DATA.TYPES; //All types - simple type + map + list
        this.dataTypes = this.dataTypeService.getAllDataTypes(); //Get all data types in service
        const nonPrimitiveTypes :Array<string> = _.filter(Object.keys(this.dataTypes), (type:string)=> {
            return types.indexOf(type) == -1;
        });

        this.typesProperties = _.map(PROPERTY_DATA.TYPES,
            (type: string) => new DropdownValue(type, type)
        );
        let typesSimpleProperties = _.map(PROPERTY_DATA.SIMPLE_TYPES,
            (type: string) => new DropdownValue(type, type)
        );
        let nonPrimitiveTypesValues = _.map(nonPrimitiveTypes,
            (type: string) => new DropdownValue(type,
                    type.replace("org.openecomp.datatypes.heat.",""))
        );
        this.typesProperties = _.concat(this.typesProperties,nonPrimitiveTypesValues);
        this.typesSchemaProperties = _.concat(typesSimpleProperties,nonPrimitiveTypesValues);
        this.typesProperties.unshift(new DropdownValue('','Select Type...'));
        this.typesSchemaProperties.unshift(new DropdownValue('','Select Schema Type...'));

        this.inputsToCreate = this.modalService.currentModal.instance.dynamicContent.instance.input.properties;

        this.propertiesListString = this.modalService.currentModal.instance.dynamicContent.instance.input.propertyNameList.join(", ");

        this.privateDataType = new DataTypeModel(null);
        this.privateDataType.name = "datatype";

        console.log('DeclareListComponent.ngOnInit() - leave');
    }

    checkFormValidForSubmit(){
        const showSchema:boolean = this.showSchema();
        let isSchemaValid: boolean = (showSchema && !this.propertyModel.schema.property.type)? false : true;
        if (!showSchema){
            this.propertyModel.schema.property.type = '';
        }
        return this.propertyModel.name && this.propertyModel.type && isSchemaValid;
    }

    showSchema():boolean {
        return [PROPERTY_TYPES.LIST, PROPERTY_TYPES.MAP].indexOf(this.propertyModel.type) > -1;
    };

    onSchemaTypeChange():void {
        if (this.propertyModel.type == PROPERTY_TYPES.MAP) {
            this.propertyModel.value = JSON.stringify({'': null});
        } else if (this.propertyModel.type == PROPERTY_TYPES.LIST) {
            this.propertyModel.value = JSON.stringify([]);
        }
    };

}
