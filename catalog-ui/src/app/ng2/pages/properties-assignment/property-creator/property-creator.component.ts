import * as _ from "lodash";
import {Component} from '@angular/core';
import {DropdownValue} from "app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component";
import { DataTypeService } from "app/ng2/services/data-type.service";
import {PropertyBEModel, DataTypesMap} from "app/models";
import {PROPERTY_DATA} from "app/utils";



@Component({
    selector: 'property-creator',
    templateUrl: './property-creator.component.html',
    styleUrls:['./property-creator.component.less'],
})

export class PropertyCreatorComponent {

    typesProperties: Array<DropdownValue>;
    propertyModel: PropertyBEModel;
    //propertyNameValidationPattern:RegExp = /^[a-zA-Z0-9_:-]{1,50}$/;
    //commentValidationPattern:RegExp = /^[\u0000-\u00BF]*$/;
    //types:Array<string>;
    dataTypes:DataTypesMap;
    isLoading:boolean;

    constructor(protected dataTypeService:DataTypeService) {}

    ngOnInit() {
        this.propertyModel = new PropertyBEModel();
        const types: Array<string> =  PROPERTY_DATA.TYPES; //All types - simple type + map + list
        this.dataTypes = this.dataTypeService.getAllDataTypes(); //Get all data types in service
        const nonPrimitiveTypes :Array<string> = _.filter(Object.keys(this.dataTypes), (type:string)=> {
            return types.indexOf(type) == -1;
        });

        this.typesProperties = _.map(PROPERTY_DATA.SIMPLE_TYPES,
            (type: string) => new DropdownValue(type, type)
        );
        let nonPrimitiveTypesValues = _.map(nonPrimitiveTypes,
            (type: string) => new DropdownValue(type,
                    type.replace("org.openecomp.datatypes.heat.",""))
        );
        this.typesProperties = _.concat(this.typesProperties,nonPrimitiveTypesValues);

    }

    checkFormValidForSubmit(){
        return this.propertyModel.name && this.propertyModel.type;
    }

}
