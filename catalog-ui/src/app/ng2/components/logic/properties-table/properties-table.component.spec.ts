import { NO_ERRORS_SCHEMA, SimpleChange } from '@angular/core';
import { ComponentFixture } from '@angular/core/testing';
import { ConfigureFn, configureTests } from '../../../../../jest/test-config.helper';
import { DerivedFEProperty } from '../../../../models/properties-inputs/derived-fe-property';
import { PropertyBEModel } from '../../../../models/properties-inputs/property-be-model';
import { PropertyFEModel } from '../../../../models/properties-inputs/property-fe-model';
import { ContentAfterLastDotPipe } from '../../../pipes/contentAfterLastDot.pipe';
import { KeysPipe } from '../../../pipes/keys.pipe';
import { PropertiesOrderByPipe } from '../../../pipes/properties-order-by.pipe';
import { SearchFilterPipe } from '../../../pipes/searchFilter.pipe';
import { ModalService } from '../../../services/modal.service';
import { PropertiesService } from '../../../services/properties.service';
import { PropertiesTableComponent, PropertyRowSelectedEvent } from './properties-table.component';

describe('properties-table component', () => {

    let fixture: ComponentFixture<PropertiesTableComponent>;
    let propertiesServiceMock: Partial<PropertiesService>;
    let modalServiceMock: Partial<ModalService>;

    beforeEach(
        () => {
            propertiesServiceMock = {
                undoDisableRelatedProperties: jest.fn(),
                disableRelatedProperties: jest.fn()
            };
            modalServiceMock = {

            };

            const configure: ConfigureFn = (testBed) => {
                testBed.configureTestingModule({
                    declarations: [
                        PropertiesTableComponent,
                        KeysPipe,
                        PropertiesOrderByPipe,
                        SearchFilterPipe,
                        ContentAfterLastDotPipe
                    ],
                    imports: [],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [
                        {provide: PropertiesService, useValue: propertiesServiceMock},
                        {provide: ModalService, useValue: modalServiceMock}
                    ],
                });
            };

            configureTests(configure).then((testBed) => {
                fixture = testBed.createComponent(PropertiesTableComponent);
            });
        }
    );

    it('When Properties assignment page is loaded, it is sorted by property name (acsending)', () => {
        const fePropertiesMapValues = new SimpleChange('previousValue', 'currentValue', true);
        const changes = {
            fePropertiesMap: fePropertiesMapValues
        };

        // init values before ngOnChanges was called
        fixture.componentInstance.sortBy = 'existingValue';

        fixture.componentInstance.ngOnChanges(changes);

        expect (fixture.componentInstance.reverse).toEqual(true);
        // expect (fixture.componentInstance.direction).toEqual(1);
        expect (fixture.componentInstance.direction).toEqual(fixture.componentInstance.ascUpperLettersFirst);
        expect (fixture.componentInstance.sortBy).toEqual('name');
        expect (fixture.componentInstance.path.length).toEqual(1);
        expect (fixture.componentInstance.path[0]).toEqual('name');
    });

    it('When ngOnChanges is called without fePropertiesMap,' +
        ' sortBy will remain as it was', () => {
        const fePropertiesMapValues = new SimpleChange('previousValue', 'currentValue', true);
        const changes = {
            dummyKey: fePropertiesMapValues
        };

        // init values before ngOnChanges was called
        fixture.componentInstance.sortBy = 'existingValue';
        fixture.componentInstance.sort = jest.fn();

        fixture.componentInstance.ngOnChanges(changes);

        expect (fixture.componentInstance.sortBy).toEqual('existingValue');
    });

    it ('When sort is called init this.direction to 1', () => {
        // init values
        fixture.componentInstance.reverse = false;
        fixture.componentInstance.direction = 0;
        fixture.componentInstance.sortBy = 'initialize.Value';
        fixture.componentInstance.path = [];

        // call sore function
        fixture.componentInstance.sort('initialize.Value');

        // expect that
        expect (fixture.componentInstance.reverse).toBe(true);
        expect (fixture.componentInstance.direction).toBe(fixture.componentInstance.ascUpperLettersFirst);
        expect (fixture.componentInstance.sortBy).toBe('initialize.Value');
        expect (fixture.componentInstance.path.length).toBe(2);
        expect (fixture.componentInstance.path[0]).toBe('initialize');
        expect (fixture.componentInstance.path[1]).toBe('Value');
    });

    it ('When sort is called init this.direction to -1', () => {
        // init values
        fixture.componentInstance.reverse = true;
        fixture.componentInstance.direction = 0;
        fixture.componentInstance.sortBy = 'initialize.Value';
        fixture.componentInstance.path = [];

        // call sore function
        fixture.componentInstance.sort('initialize.Value');

        // expect that
        expect (fixture.componentInstance.reverse).toBe(false);
        expect (fixture.componentInstance.direction).toBe(fixture.componentInstance.descLowerLettersFirst);
    });

    it ('When onPropertyChanged is called, event is emitted' , () => {
        spyOn(fixture.componentInstance.emitter, 'emit');
        fixture.componentInstance.onPropertyChanged('testProperty');
        expect(fixture.componentInstance.emitter.emit).toHaveBeenCalledWith('testProperty');
    });

    it ('When onClickPropertyRow is called, selectedPropertyId is updated and event is emitted.' , () => {
        const propertyFEModel = new PropertyFEModel(new PropertyBEModel());
        propertyFEModel.name = 'propertyName';
        const propertyRowSelectedEvent: PropertyRowSelectedEvent = new PropertyRowSelectedEvent(propertyFEModel, 'instanceName');

        spyOn(fixture.componentInstance.selectPropertyRow, 'emit');
        fixture.componentInstance.onClickPropertyRow(propertyFEModel, 'instanceName');

        expect (fixture.componentInstance.selectedPropertyId).toBe('propertyName');
        expect (fixture.componentInstance.selectPropertyRow.emit).toHaveBeenCalledWith(propertyRowSelectedEvent);
    });

    it ('When onClickPropertyInnerRow is called, event is emitted.' , () => {
        const derivedFEProperty = new DerivedFEProperty(new PropertyBEModel());
        const propertyRowSelectedEvent: PropertyRowSelectedEvent = new PropertyRowSelectedEvent(derivedFEProperty, 'instanceName');
        spyOn(fixture.componentInstance.selectPropertyRow, 'emit');
        fixture.componentInstance.onClickPropertyInnerRow(derivedFEProperty, 'instanceName');

        expect (fixture.componentInstance.selectPropertyRow.emit).toHaveBeenCalledWith(propertyRowSelectedEvent);
    });

    it ('When propertyChecked is called, propertiesService.undoDisableRelatedProperties is called and event is emitted.' , () => {

        const propertyFEModel = new PropertyFEModel(new PropertyBEModel());
        propertyFEModel.isSelected = false;
        const propertyRowSelectedEvent: PropertyRowSelectedEvent = new PropertyRowSelectedEvent(propertyFEModel, 'instanceName1');

        spyOn(fixture.componentInstance.updateCheckedPropertyCount, 'emit');
        fixture.componentInstance.propertyChecked(propertyFEModel);
        expect (propertiesServiceMock.undoDisableRelatedProperties).toHaveBeenCalledWith(propertyFEModel, undefined);
        expect (fixture.componentInstance.updateCheckedPropertyCount.emit).toHaveBeenCalledWith(false);
    });

    it ('When propertyChecked is called, propertiesService.disableRelatedProperties is called and event is emitted.' , () => {

        const propertyFEModel = new PropertyFEModel(new PropertyBEModel());
        propertyFEModel.isSelected = true;
        const propertyRowSelectedEvent: PropertyRowSelectedEvent = new PropertyRowSelectedEvent(propertyFEModel, 'instanceName1');

        spyOn(fixture.componentInstance.updateCheckedPropertyCount, 'emit');
        fixture.componentInstance.propertyChecked(propertyFEModel);
        expect (propertiesServiceMock.disableRelatedProperties).toHaveBeenCalledWith(propertyFEModel, undefined);
        expect (fixture.componentInstance.updateCheckedPropertyCount.emit).toHaveBeenCalledWith(true);
    });

});
