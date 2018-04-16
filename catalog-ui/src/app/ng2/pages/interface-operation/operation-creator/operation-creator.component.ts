import * as _ from "lodash";
import {Component} from '@angular/core';
import {DropdownValue} from "app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component";
import {InputModel, OperationModel, OperationParam} from 'app/models';

@Component({
    selector: 'operation-creator',
    templateUrl: './operation-creator.component.html',
    styleUrls:['./operation-creator.component.less'],
})

export class OperationCreatorComponent {

    inputProperties: Array<DropdownValue>;
    input: any;
    inputParams: Array<OperationParam> = [];
    operation: OperationModel;
    isEditMode: boolean = false;

    ngOnInit() {
        this.operation = new OperationModel(this.input.operation || {});

        if (this.input.operation) {
            let {inputParams} = this.input.operation;
            if (inputParams) {
                _.forEach(inputParams.listToscaDataDefinition, (input: OperationParam) => {
                    this.addParam(input);
                });
            }
            this.isEditMode = true;
        }

        this.inputProperties = _.map(this.input.inputProperties,
            (input: InputModel) => new DropdownValue(input.uniqueId, input.name)
        );
    }

    addParam(param?: OperationParam): void {
        this.inputParams.push(new OperationParam(param));
    }

    isAddAllowed(): boolean {
        if (this.inputParams.length === 0) {
            return true;
        }

        const {paramId, paramName} = _.last(this.inputParams);
        return paramId && paramName.length > 0;
    }

    createInputParamList(): void {
        this.operation.createInputParamsList(this.inputParams);
    }

    checkFormValidForSubmit(): boolean {
        return this.operation.operationType && this.operation.operationType.length > 0 && this.isAddAllowed();
    }

}
