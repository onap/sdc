import { Component, Input, Output, EventEmitter, ViewEncapsulation } from '@angular/core';

@Component({
    selector: 'search-bar',
    templateUrl: './search-bar.component.html',
    styleUrls: ['./search-bar.component.less'],
    encapsulation: ViewEncapsulation.None
})
export class SearchBarComponent {

    @Input() placeholder: string;
    @Input() class: string;
    @Input() searchQuery: string;
    @Output() searchChanged: EventEmitter<any> = new EventEmitter<any>();
    @Output() searchButtonClicked: EventEmitter<string> = new EventEmitter<string>();

    searchButtonClick = (): void => {
        if (this.searchQuery) { //do not allow empty search
            this.searchButtonClicked.emit(this.searchQuery);
        }
    }

    searchQueryChange = ($event): void => {
        this.searchChanged.emit($event);
    }

    private clearSearchQuery = (): void => {
        this.searchQuery = "";
        this.searchButtonClicked.emit(this.searchQuery);
    }
}

