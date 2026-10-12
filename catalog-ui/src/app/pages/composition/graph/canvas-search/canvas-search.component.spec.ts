import {HttpClient} from '@angular/common/http';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {TestBed} from '@angular/core/testing';
import {AutocompletePipe} from 'onap-ui-angular/dist/autocomplete/autocomplete.pipe';
import {CanvasSearchComponent} from './canvas-search.component';

describe('CanvasSearchComponent', () => {
    let component: CanvasSearchComponent;
    let http: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({imports: [HttpClientTestingModule]});
        http = TestBed.get(HttpTestingController);
        component = new CanvasSearchComponent(TestBed.get(HttpClient), new AutocompletePipe());
        component.ngOnInit();
    });

    afterEach(() => {
        component.ngOnDestroy();
        http.verify();
    });

    it('cancels a pending remote query when selecting a canvas item', () => {
        component.dataUrl = '/canvas-search';
        component.onSearchQueryChanged('server');
        const request = http.expectOne('/canvas-search?searchQuery=server');
        const selectedItem = {label: 'Server', value: 'server-id'};
        const searches: string[] = [];
        const selections: any[] = [];
        component.searchButtonClicked.subscribe((value) => searches.push(value));
        component.onSelectedItem.subscribe((value) => selections.push(value));

        component.onItemSelected(selectedItem);

        expect(request.cancelled).toBe(true);
        expect(component.searchQuery).toBe('server-id');
        expect(component.autoCompleteResults).toEqual([]);
        expect(searches).toEqual(['server-id']);
        expect(selections).toEqual([selectedItem]);
    });
});