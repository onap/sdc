import {Component, Input} from '@angular/core';
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
    @Input() isInputParam: boolean;

    propTypeEnum: Array<String> = [];
    filteredInputProps: Array<DropdownValue> = [];

    ngOnInit() {
        this.propTypeEnum = _.uniq(_.toArray(this.propTypes));
        this.onChangeType();
    }

    onChangeType() {
        this.filteredInputProps = _.filter(this.inputProps, prop => {
            return this.propTypes[prop.value] === this.param.type;
        });
    }
}
