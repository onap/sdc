import { IDropDownOption } from 'onap-ui-angular/dist/form-elements/dropdown/dropdown-models';

export class AttributeOptions {
    public static readonly types: IDropDownOption[] = [
        {
            label: 'integer',
            value: 'integer',
        },
        {
            label: 'string',
            value: 'string',
        },
        {
            label: 'float',
            value: 'float'
        },
        {
            label: 'boolean',
            value: 'boolean'
        },
        {
            label: 'list',
            value: 'list'
        },
        {
            label: 'map',
            value: 'map'
        }
    ];

    public static readonly booleanValues: IDropDownOption[] = [
        {
            label: 'true',
            value: 'true',
        },
        {
            label: 'false',
            value: 'false',
        }
    ];

    public static readonly entrySchemaValues: IDropDownOption[] = [
        {
            label: 'integer',
            value: 'integer',
        },
        {
            label: 'string',
            value: 'string',
        },
        {
            label: 'float',
            value: 'float'
        },
        {
            label: 'boolean',
            value: 'boolean'
        }
    ];
}
