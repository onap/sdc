import {Component, Input} from '@angular/core';
import {DropdownValue} from "app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component";
import {OperationParameter} from 'app/models';

@Component({
    selector: 'param-row',
    templateUrl: './param-row.component.html',
    styleUrls: ['./param-row.component.less']
})

export class ParamRowComponent {
    @Input() param: OperationParameter;
    @Input() inputProps: Array<DropdownValue>;
    @Input() propTypes: {};
    @Input() onRemoveParam: Function;
    @Input() isAssociateWorkflow: boolean;

    propTypeEnum: Array<string> = ['boolean', 'float', 'integer', 'string'];
    filteredInputProps: Array<DropdownValue> = [];

    ngOnInit() {
        this.onChangeType();
    }

    onChangeType() {
        this.filteredInputProps = _.filter(this.inputProps, prop => {
            return this.propTypes[prop.value] === this.param.type;
        });
    }
}
