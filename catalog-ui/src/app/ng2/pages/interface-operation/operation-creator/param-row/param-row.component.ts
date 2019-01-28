import {Component, Input} from '@angular/core';
import {DataTypeService} from "app/ng2/services/data-type.service";
import {OperationModel, OperationParameter, InputBEModel} from 'app/models';
import {DropDownOption} from "../operation-creator.component";
import {DropdownValue} from "app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component";

class DropdownValueType extends DropdownValue {
    type: String;

    constructor(value: string, label: string, type?: String) {
        super(value, label);
        if (type) {
            this.type = type;
        }
    }
}

@Component({
    selector: 'param-row',
    templateUrl: './param-row.component.html',
    styleUrls: ['./param-row.component.less']
})

export class ParamRowComponent {
    @Input() param: OperationParameter;
    @Input() inputProps: Array<InputBEModel>;
    @Input() operationOutputs: Array<OperationModel>;
    @Input() onRemoveParam: Function;
    @Input() isAssociateWorkflow: boolean;
    @Input() readonly: boolean;
    @Input() isInputParam: boolean;
    @Input() validityChanged: Function;

    propTypeEnum: Array<string> = [];
    operationOutputCats: Array<{ operationName: string, outputs: Array<DropdownValueType> }> = [];
    filteredInputProps: Array<DropdownValue> = [];

    constructor(private dataTypeService: DataTypeService) {}

    ngOnInit() {
        this.propTypeEnum = _.uniq(
            _.map(
                this.getPrimitiveSubtypes(),
                prop => prop.type
            )
        );

        this.onChangeType();
        this.validityChanged();
    }

    onChangeName() {
        this.validityChanged();
    }

    onChangeType() {
        this.filteredInputProps = _.map(
            _.filter(
                this.getPrimitiveSubtypes(),
                prop => !this.param.type || prop.type === this.param.type
            ),
            prop => new DropdownValue(prop.uniqueId, prop.name)
        );

        this.operationOutputCats = _.filter(
            _.map(
                this.operationOutputs,
                op => {
                    return {
                        operationName: `${op.displayType()}.${op.name}`,
                        outputs: _.map(
                            _.filter(op.outputs.listToscaDataDefinition, output => !this.param.type || output.type === this.param.type),
                            output => new DropdownValueType(
                                `${op.interfaceType}.${op.name}.${output.name}`,
                                output.name,
                                output.type
                            )
                        )
                    };
                }
            ),
            category => category.outputs.length > 0
        );

        if (this.param.inputId) {
            const selProp = this.getSelectedProp();
            if (selProp && selProp.type === this.param.type) {
                this.param.inputId = '-1';
                setTimeout(() => this.param.inputId = selProp.uniqueId || selProp.value, 0);
            } else {
                this.param.inputId = null;
            }
        }

        this.validityChanged();
    }

    onChangeProperty() {
        const newProp = this.getSelectedProp();

        if (!this.param.type) {
            this.param.type = newProp.type;
            this.onChangeType();
        }

        if (!this.param.name) {
            this.param.name = newProp.name || newProp.label;
        }

        this.validityChanged();
    }

    getPrimitiveSubtypes(): Array<InputBEModel> {
        const flattenedProps: Array<any> = [];
        const dataTypes = this.dataTypeService.getAllDataTypes();

        _.forEach(this.inputProps, prop => {
            const type = _.find(
                _.toArray(dataTypes),
                (type: any) => type.name === prop.type
            );
            flattenedProps.push(prop);
            if (type.properties) {
                _.forEach(type.properties, subType => {
                    if (this.isTypePrimitive(subType.type)) {
                        flattenedProps.push({
                            type: subType.type,
                            name: `${prop.name}.${subType.name}`,
                            uniqueId: `${prop.uniqueId}.${subType.name}`
                        });
                    }
                });
            }
        });

        return flattenedProps;
    }

    getSelectedProp() {
        return _.find(
            this.getPrimitiveSubtypes(),
            prop => this.param.inputId === prop.uniqueId
        ) || _.find(
            _.reduce(
                this.operationOutputCats,
                (acc, cat) => [...acc, ...cat.outputs],
            []),
            (out: DropdownValueType) => this.param.inputId === out.value
        );
    }

    isTypePrimitive(type): boolean {
        return (
            type === 'string' ||
            type === 'integer' ||
            type === 'float' ||
            type === 'boolean'
        );
    }
}
