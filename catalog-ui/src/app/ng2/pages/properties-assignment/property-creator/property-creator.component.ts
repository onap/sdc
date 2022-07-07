
import { Component } from '@angular/core';
import { DataTypesMap, PropertyBEModel } from 'app/models';
import { DropdownValue } from 'app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component';
import { DataTypeService } from 'app/ng2/services/data-type.service';
import { PROPERTY_DATA } from 'app/utils';
import * as _ from 'lodash';
import { PROPERTY_TYPES } from '../../../../utils';
import {Validation} from "../../../../view-models/workspace/tabs/general/general-view-model";
import {WorkspaceService} from "../../workspace/workspace.service";

@Component({
    selector: 'property-creator',
    templateUrl: './property-creator.component.html',
    styleUrls: ['./property-creator.component.less'],
})

export class PropertyCreatorComponent {

    validation:Validation;
    typesProperties: DropdownValue[];
    typesSchemaProperties: DropdownValue[];
    propertyModel: PropertyBEModel;
    dataTypes: DataTypesMap;
    isLoading: boolean;

    constructor(protected dataTypeService: DataTypeService, private workspaceService: WorkspaceService) {
        this.filterDataTypesByModel(this.workspaceService.metadata.model);
    }

    checkFormValidForSubmit() {
        const showSchema: boolean = this.showSchema();
        const isSchemaValid: boolean = (showSchema && !this.propertyModel.schema.property.type) ? false : true;
        if (!showSchema) {
            this.propertyModel.schema.property.type = '';
        }
        return this.propertyModel.name && this.propertyModel.type && isSchemaValid;
    }

    showSchema(): boolean {
        return [PROPERTY_TYPES.LIST, PROPERTY_TYPES.MAP].indexOf(this.propertyModel.type) > -1;
    }

    onTypeChange(): void {
        this.propertyModel.schema.property.type='';
        const typeList =  this.typesProperties;
        if(this.propertyModel.type === PROPERTY_TYPES.MAP){
            this.typesSchemaProperties = typeList.filter(dropdownObject => (dropdownObject.label != 'list' && dropdownObject.label != 'map'));
        }
        if(this.propertyModel.type === PROPERTY_TYPES.LIST){
            this.typesSchemaProperties = typeList.filter(dropdownObject => dropdownObject.label != 'list');
        }
    }

    onSchemaTypeChange(): void {
        if (this.propertyModel.type === PROPERTY_TYPES.MAP) {
            this.propertyModel.value = JSON.stringify({'': null});
        } else if (this.propertyModel.type === PROPERTY_TYPES.LIST) {
            this.propertyModel.value = JSON.stringify([]);
        }
    }

    public filterDataTypesByModel = (modelName: string) => {
        this.dataTypes = new DataTypesMap(null);
        this.dataTypes = this.dataTypeService.getDataTypeByModel(modelName);
        this.propertyModel = new PropertyBEModel();
        this.propertyModel.type = '';
        this.propertyModel.schema.property.type = '';
        const types: string[] =  PROPERTY_DATA.TYPES; // All types - simple type + map + list
        const nonPrimitiveTypes: string[] = _.filter(Object.keys(this.dataTypes), (type: string) => {
            return types.indexOf(type) === -1;
        });

        this.typesProperties = _.map(PROPERTY_DATA.TYPES,
            (type: string) => new DropdownValue(type, type)
        );
        const typesSimpleProperties = this.typesProperties.filter(dropdownObject => dropdownObject.label != 'list');
        const nonPrimitiveTypesValues = _.map(nonPrimitiveTypes,
            (type: string) => new DropdownValue(type,
                type.replace('org.openecomp.datatypes.heat.', ''))
        )
        .sort((a, b) => a.label.localeCompare(b.label));
        this.typesProperties = _.concat(this.typesProperties, nonPrimitiveTypesValues);
        this.typesSchemaProperties = _.concat(typesSimpleProperties, nonPrimitiveTypesValues);
        this.typesProperties.unshift(new DropdownValue('', 'Select Type...'));
        this.typesSchemaProperties.unshift(new DropdownValue('', 'Select Schema Type...'));
    }

}
