import {Component, Input} from '@angular/core';
import {DataTypeService} from "app/ng2/services/data-type.service";
import {OperationParameter, InputBEModel} from 'app/models';
import {DropdownValue} from "app/ng2/components/ui/form-components/dropdown/ui-element-dropdown.component";

@Component({
    selector: 'param-row',
    templateUrl: './param-row.component.html',
    styleUrls: ['./param-row.component.less']
})

export class ParamRowComponent {
    @Input() param: OperationParameter;
    @Input() inputProps: Array<InputBEModel>;
    @Input() onRemoveParam: Function;
    @Input() isAssociateWorkflow: boolean;
    @Input() readonly: boolean;
    @Input() isInputParam: boolean;

    propTypeEnum: Array<String> = [];
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
    }

    onChangeType() {
        this.filteredInputProps = _.map(
            _.filter(
                this.getPrimitiveSubtypes(),
                prop => prop.type === this.param.type
            ),
            prop => new DropdownValue(prop.uniqueId, prop.name)
        );
    }

    getPrimitiveSubtypes(): Array<InputBEModel> {
        const flattenedProps: Array<any> = [];
        const dataTypes = this.dataTypeService.getAllDataTypes();
        _.forEach(this.inputProps, prop => {
            const type = _.find(
                _.toArray(dataTypes),
                (type: any) => type.name === prop.type
            );
            if (!type.properties) {
                flattenedProps.push(prop);
            } else {
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

    isTypePrimitive(type): boolean {
        return (
            type === 'string' ||
            type === 'integer' ||
            type === 'float' ||
            type === 'boolean'
        );
    }
}
