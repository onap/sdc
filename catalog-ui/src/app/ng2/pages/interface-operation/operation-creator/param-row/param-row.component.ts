import {Component, Input} from '@angular/core';
import {DropdownValue} from "app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component";
import {OperationParam} from 'app/models';

@Component({
    selector: 'param-row',
    templateUrl: './param-row.component.html',
    styleUrls: ['./param-row.component.less']
})

export class ParamRowComponent {
    @Input() param:OperationParam;
    @Input() inputProps:Array<DropdownValue>;
    @Input() inputParams:Array<OperationParam>
    @Input() index:number;

    onRemove = ():void => {
        this.inputParams.splice(this.index, 1);
    }
}
