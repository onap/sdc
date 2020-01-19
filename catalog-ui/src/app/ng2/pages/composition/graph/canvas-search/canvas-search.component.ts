import {Component, EventEmitter, Input, Output} from '@angular/core';
import {AutoCompleteComponent} from "onap-ui-angular/dist/autocomplete/autocomplete.component";

@Component({
    selector: 'canvas-search',
    templateUrl: './canvas-search.component.html',
    styleUrls: ['./canvas-search.component.less']
})
export class CanvasSearchComponent extends AutoCompleteComponent {

    @Output() public searchButtonClicked: EventEmitter<string> = new EventEmitter<string>();
    @Output() public onSelectedItem: EventEmitter<string> = new EventEmitter<string>();

    public onSearchClicked = (searchText:string)=> {
        this.searchButtonClicked.emit(searchText);
    }

    public onItemSelected = (selectedItem) => {
        this.searchQuery = selectedItem.value;
        this.autoCompleteResults = [];
        this.searchButtonClicked.emit(this.searchQuery);
        this.onSelectedItem.emit(selectedItem);
    }

}
