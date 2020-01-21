
import { Component } from '@angular/core';
import { DataTypesMap, PropertyBEModel } from 'app/models';
import { DropdownValue } from 'app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component';
import { DataTypeService } from 'app/ng2/services/data-type.service';
import { PROPERTY_DATA } from 'app/utils';
import * as _ from 'lodash';
import { PROPERTY_TYPES } from '../../../../utils';

@Component({
    selector: 'property-creator',
    templateUrl: './property-creator.component.html',
    styleUrls: ['./property-creator.component.less'],
})

export class PropertyCreatorComponent {

    typesProperties: DropdownValue[];
    typesSchemaProperties: DropdownValue[];
    propertyModel: PropertyBEModel;
    // propertyNameValidationPattern:RegExp = /^[a-zA-Z0-9_:-]{1,50}$/;
    // commentValidationPattern:RegExp = /^[\u0000-\u00BF]*$/;
    // types:Array<string>;
    dataTypes: DataTypesMap;
    isLoading: boolean;

    constructor(protected dataTypeService: DataTypeService) {}

    ngOnInit() {
        this.propertyModel = new PropertyBEModel();
        this.propertyModel.type = '';
        this.propertyModel.schema.property.type = '';
        const types: string[] =  PROPERTY_DATA.TYPES; // All types - simple type + map + list
        this.dataTypes = this.dataTypeService.getAllDataTypes(); // Get all data types in service
        const nonPrimitiveTypes: string[] = _.filter(Object.keys(this.dataTypes), (type: string) => {
            return types.indexOf(type) === -1;
        });

        this.typesProperties = _.map(PROPERTY_DATA.TYPES,
            (type: string) => new DropdownValue(type, type)
        );
        const typesSimpleProperties = _.map(PROPERTY_DATA.SIMPLE_TYPES,
            (type: string) => new DropdownValue(type, type)
        );
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

    onSchemaTypeChange(): void {
        if (this.propertyModel.type === PROPERTY_TYPES.MAP) {
            this.propertyModel.value = JSON.stringify({'': null});
        } else if (this.propertyModel.type === PROPERTY_TYPES.LIST) {
            this.propertyModel.value = JSON.stringify([]);
        }
    }

}
