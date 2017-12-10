import { Component, Input, Output, EventEmitter, ViewEncapsulation } from '@angular/core';
import { SearchBarComponent } from '../search-bar/search-bar.component';

@Component({
    selector: 'search-with-autocomplete',
    templateUrl: './search-with-autocomplete.component.html',
    styleUrls: ['./search-with-autocomplete.component.less'],
    encapsulation: ViewEncapsulation.None
})
export class SearchWithAutoCompleteComponent {

    @Input() searchPlaceholder: string;
    @Input() searchBarClass: string;
    @Input() searchQuery: string;
    @Input() autoCompleteValues: Array<string>;
    @Output() searchChanged: EventEmitter<any> = new EventEmitter<any>();
    @Output() searchButtonClicked: EventEmitter<string> = new EventEmitter<string>();

    searchChange = (searchTerm: string) => {
        this.searchQuery = searchTerm;
        this.searchChanged.emit(searchTerm);
    }

    updateSearch = (searchTerm: string) => {
        this.searchQuery = searchTerm;
        this.searchButtonClicked.emit(searchTerm);
        this.autoCompleteValues = [];
    }
}

