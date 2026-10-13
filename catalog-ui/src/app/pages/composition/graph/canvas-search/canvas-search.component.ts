import { HttpClient } from '@angular/common/http';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { AutoCompleteComponent } from 'onap-ui-angular/dist/autocomplete/autocomplete.component';
import { AutocompletePipe } from 'onap-ui-angular/dist/autocomplete/autocomplete.pipe';

@Component({
    selector: 'canvas-search',
    templateUrl: './canvas-search.component.html',
    styleUrls: ['./canvas-search.component.less']
})
export class CanvasSearchComponent extends AutoCompleteComponent {

    @Output() public searchButtonClicked: EventEmitter<string> = new EventEmitter<string>();
    @Output() public onSelectedItem: EventEmitter<string> = new EventEmitter<string>();

    public onItemSelected: (selectedItem) => void;

    constructor(http: HttpClient, autocompletePipe: AutocompletePipe) {
        super(http, autocompletePipe);
        const selectAutocompleteItem = this.onItemSelected;
        this.onItemSelected = (selectedItem) => {
            selectAutocompleteItem(selectedItem);
            this.searchButtonClicked.emit(this.searchQuery);
            this.onSelectedItem.emit(selectedItem);
        };
    }

    public onSearchClicked = (searchText: string) => {
        this.searchButtonClicked.emit(searchText);
    }

}
