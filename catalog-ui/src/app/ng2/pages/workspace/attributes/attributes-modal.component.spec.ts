import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture } from '@angular/core/testing';
import { ConfigureFn, configureTests } from '../../../../../jest/test-config.helper';
import { AttributeModel } from '../../../../models/attributes';
import { ValidationUtils } from '../../../../utils/validation-utils';
import { CacheService } from '../../../services/cache.service';
import { TranslatePipe } from '../../../shared/translator/translate.pipe';
import { TranslateService } from '../../../shared/translator/translate.service';
import { AttributeModalComponent } from './attribute-modal.component';

describe('attributes modal component', () => {

    let fixture: ComponentFixture<AttributeModalComponent>;

    // Mocks
    let translateServiceMock: Partial<TranslateService>;
    let cacheServiceMock: Partial<CacheService>;

    const validationPatterns = {
        integerNoLeadingZero : 'int_regx',
        number : 'number_regx'
    };

    const newAttribute = {
        uniqueId: '1', name: 'attr1', description: 'description1', type: 'string', hidden: false, defaultValue: 'val1', schema: null
    };

    beforeEach(
        async(() => {

            translateServiceMock = {
                translate: jest.fn()
            };

            cacheServiceMock = {
                get: jest.fn().mockImplementation((k) => {
                    return { validationPatterns};
                } )
            };

            const configure: ConfigureFn = (testBed) => {
                testBed.configureTestingModule({
                    declarations: [AttributeModalComponent, TranslatePipe],
                    imports: [],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {provide: TranslateService, useValue: translateServiceMock},
                        {provide: CacheService, useValue: cacheServiceMock},
                    ]
                });
            };

            configureTests(configure).then((testBed) => {
                fixture = testBed.createComponent(AttributeModalComponent);
            });
        })
    );

    it('test that when hidden is clicked, hidden attribute is set', async () => {
        fixture.componentInstance.attributeToEdit = new AttributeModel();
        const hidden = fixture.componentInstance.attributeToEdit.hidden;
        fixture.componentInstance.ngOnInit();

        expect(hidden).toBe(false);
        fixture.componentInstance.onHiddenCheckboxClicked(true);
        expect(fixture.componentInstance.attributeToEdit.hidden).toBe(true);
    });

    it('test that when type is set to boolean default value is cleared', async () => {
        const component = fixture.componentInstance;
        component.attributeToEdit = new AttributeModel();
        component.ngOnInit();

        component.onTypeSelected({ value : 'string', label : 'string'});
        component.attributeToEdit.defaultValue = 'some_value';
        component.onTypeSelected({ value : 'boolean', label : 'boolean'});
        expect(component.attributeToEdit.defaultValue).toBe('');

        component.onBooleanDefaultValueSelected({ value : 'true', label : 'true'});
        expect(component.attributeToEdit.defaultValue).toBe('true');
    });

    it('test that when certain type is selected, the correct regex pattern is chosen', async () => {
        const component = fixture.componentInstance;
        component.attributeToEdit = new AttributeModel();
        component.ngOnInit();

        // integer
        component.onTypeSelected({ value : 'integer', label : 'integer'});
        expect(component.defaultValuePattern).toBe(validationPatterns.integerNoLeadingZero);

        // float
        component.onTypeSelected({ value : 'float', label : 'float'});
        expect(component.defaultValuePattern).toBe(validationPatterns.number);

        // list is chosen with no schema, regex pattern is set to default
        component.onTypeSelected({ value : 'list', label : 'list'});
        expect(component.defaultValuePattern).toEqual('.*');

        // schema is set to list of int
        component.onEntrySchemaTypeSelected({ value : 'integer', label : 'integer' });
        expect(component.defaultValuePattern).toEqual(ValidationUtils.getPropertyListPatterns().integer);

        // schema is set to list of float
        component.onEntrySchemaTypeSelected({ value : 'float', label : 'float' });
        expect(component.defaultValuePattern).toEqual(ValidationUtils.getPropertyListPatterns().float);

        // map is selected (float schema is still selected from previous line)
        component.onTypeSelected({ value : 'map', label : 'map'});
        expect(component.defaultValuePattern).toEqual(ValidationUtils.getPropertyMapPatterns().float);

        // change schema type to boolean
        component.onEntrySchemaTypeSelected({ value : 'boolean', label : 'boolean' });
    });

    it('should detect map with non-unique keys', async () => {
        const component = fixture.componentInstance;
        component.attributeToEdit = new AttributeModel();
        component.ngOnInit();
        expect(component.isMapUnique()).toBe(true); // map is not selected so return true by default
        component.onTypeSelected({ value : 'map', label : 'map'});
        component.onEntrySchemaTypeSelected({ value : 'boolean', label : 'boolean' });
        component.attributeToEdit.defaultValue = '"1":true,"2":false';
        expect(component.isMapUnique()).toBe(true);
        component.attributeToEdit.defaultValue = '"1":true,"1":false';
        expect(component.isMapUnique()).toBe(false);
    });
});
