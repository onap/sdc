import { NO_ERRORS_SCHEMA } from '@angular/core';
import { async, ComponentFixture } from '@angular/core/testing';
import { ConfigureFn, configureTests } from '../../../../jest/test-config.helper';
import { FilterPropertiesAssignmentData } from '../../../models/filter-properties-assignment-data';
import { PopoverComponent } from '../../ui/popover/popover.component';
import { FilterPropertiesAssignmentComponent } from './filter-properties-assignment.component';

describe('filter-properties-assignemnt component', () => {

    let fixture: ComponentFixture<FilterPropertiesAssignmentComponent>;

    beforeEach(
        async(() => {

            const configure: ConfigureFn = (testBed) => {
                testBed.configureTestingModule({
                    declarations: [FilterPropertiesAssignmentComponent],
                    imports: [],
                    schemas: [NO_ERRORS_SCHEMA],
                    providers: [],
                });
            };

            configureTests(configure).then((testBed) => {
                fixture = testBed.createComponent(FilterPropertiesAssignmentComponent);

            });
        })
    );

    it('should match current snapshot of artifact-tab component', () => {
        expect(fixture).toMatchSnapshot();
    });

    it('on selectAll', () => {
        const filterData: FilterPropertiesAssignmentData = new FilterPropertiesAssignmentData();
        filterData.propertyName = 'testVal';
        const typesOptions: string[] = ['option1', 'option2', 'option3'];
        const selectedTypes: Object = {};

        fixture.componentInstance.filterData = filterData;
        fixture.componentInstance.typesOptions = typesOptions;
        fixture.componentInstance.selectedTypes = selectedTypes;

        fixture.componentInstance.selectAll();

        const expectedRes = {option1: false, option2: false, option3: false};
        expect(fixture.componentInstance.selectedTypes).toEqual(expectedRes);
    });

    it ('on onTypeSelected allSelected set to False', () => {
        const selectedTypes: Object = {option1: true, option2: false, option3: true};
        fixture.componentInstance.selectedTypes = selectedTypes;
        fixture.componentInstance.allSelected = true;
        fixture.componentInstance.onTypeSelected('option2');

        expect(fixture.componentInstance.allSelected).toBe(false);
    });

    it ('on onTypeSelected allSelected remains True', () => {
        const selectedTypes: Object = {option1: true, option2: true, option3: true};
        fixture.componentInstance.selectedTypes = selectedTypes;
        fixture.componentInstance.allSelected = true;
        fixture.componentInstance.onTypeSelected('option2');

        expect(fixture.componentInstance.allSelected).toBe(true);
    });

    it ('on clearAll', () => {
        const filterData: FilterPropertiesAssignmentData = new FilterPropertiesAssignmentData();
        filterData.propertyName = 'testVal';
        const selectedTypes: Object = {option1: true, option2: false, option3: true};

        fixture.componentInstance.filterData = filterData;
        fixture.componentInstance.selectedTypes = selectedTypes;
        fixture.componentInstance.allSelected = true;

        fixture.componentInstance.clearAll();

        expect(fixture.componentInstance.filterData.propertyName).toBe('');
        expect(fixture.componentInstance.allSelected).toBe(false);
    });

    it ('someTypesSelectedAndThereIsPropertyName return True', () => {
        const res = fixture.componentInstance.someTypesSelectedAndThereIsPropertyName();

        expect(res).toBe(true);
    });

    it ('someTypesSelectedAndThereIsPropertyName return Null', () => {
        const selectedTypes: Object = {option1: true, option2: false, option3: true};
        const filterData: FilterPropertiesAssignmentData = new FilterPropertiesAssignmentData();
        filterData.propertyName = 'testVal';

        fixture.componentInstance.selectedTypes = selectedTypes;
        fixture.componentInstance.filterData = filterData;

        const res = fixture.componentInstance.someTypesSelectedAndThereIsPropertyName();

        expect(res).toBe(null);
    });

    it ('search', () => {

        const filterData: FilterPropertiesAssignmentData = new FilterPropertiesAssignmentData();
        filterData.selectedTypes = ['CP'];
        fixture.componentInstance.filterData = filterData;

        const componentType: string = 'resource';
        fixture.componentInstance.componentType = componentType;

        const selectedTypes: Object = {option1: true, CP: true, option3: true};
        fixture.componentInstance.selectedTypes = selectedTypes;

        let temp: any;
        const filterPopover: PopoverComponent = new PopoverComponent(temp , temp );
        fixture.componentInstance.filterPopover = filterPopover;
        fixture.componentInstance.filterPopover.hide = jest.fn();

        fixture.componentInstance.search();

        expect(fixture.componentInstance.filterData.selectedTypes).toEqual(['CP']);
        expect(fixture.componentInstance.filterPopover.hide).toHaveBeenCalled();
    });

    it('close', () => {
        let temp: any;
        const filterPopover: PopoverComponent = new PopoverComponent(temp , temp );
        fixture.componentInstance.filterPopover = filterPopover;
        fixture.componentInstance.filterPopover.hide = jest.fn();

        fixture.componentInstance.close();

        expect(fixture.componentInstance.filterPopover.hide).toHaveBeenCalled();
    });

});
