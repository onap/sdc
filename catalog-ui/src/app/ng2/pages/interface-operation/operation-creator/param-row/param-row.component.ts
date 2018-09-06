import {Component, Input} from '@angular/core';
import {PROPERTY_DATA} from "app/utils";
import {DataTypeService} from "app/ng2/services/data-type.service";
import {OperationParameter} from 'app/models';
import {DropdownValue} from "app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component";

@Component({
    selector: 'param-row',
    templateUrl: './param-row.component.html',
    styleUrls: ['./param-row.component.less']
})

export class ParamRowComponent {
    @Input() param: OperationParameter;
    @Input() inputProps: Array<DropdownValue>;
    @Input() propTypes: { [key: string]: string };
    @Input() onRemoveParam: Function;
    @Input() isAssociateWorkflow: boolean;
    @Input() readonly: boolean;

    propTypeEnum: Array<String> = [];
    filteredInputProps: Array<DropdownValue> = [];

    constructor(private dataTypeService:DataTypeService) {}

    ngOnInit() {
        const types = PROPERTY_DATA.TYPES.concat(
            _.filter(
                Object.keys(this.dataTypeService.getAllDataTypes()),
                type => PROPERTY_DATA.TYPES.indexOf(type) === -1
            )
        );
        this.propTypeEnum = _.filter(
            types,
            type => _.toArray(this.propTypes).indexOf(type) > -1
        );
        this.onChangeType();
    }

    onChangeType() {
        this.filteredInputProps = _.filter(this.inputProps, prop => {
            return this.propTypes[prop.value] === this.param.type;
        });
    }
}
