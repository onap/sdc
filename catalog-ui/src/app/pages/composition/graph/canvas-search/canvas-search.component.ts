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

    // AutoCompleteComponent declares onClearSearch protected, which AOT rejects because the
    // generated factory reads template-bound members off the instance from outside the class.
    // Widening it here emits no code — a declaration without an initializer leaves the base
    // class's assignment as the only one — so it is purely a compile-time bridge. Drop it once
    // onap-ui-angular publishes the fix (SDC-4895).
    public onClearSearch: () => void;

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
