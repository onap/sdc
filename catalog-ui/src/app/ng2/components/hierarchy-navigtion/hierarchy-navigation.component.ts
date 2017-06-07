import {Component, Input, Output, EventEmitter} from '@angular/core';
import {HierarchyDisplayOptions} from './hierarchy-display-options';


@Component({
    selector: 'hierarchy-navigation',
    templateUrl: './hierarchy-navigation.component.html',
    styleUrls: ['./hierarchy-navigation.component.less']
})

export class HierarchyNavigationComponent {
    @Input() displayData: Array<any>;
    @Input() selectedItem: any;
    @Input() displayOptions: HierarchyDisplayOptions;

    @Output() updateSelected:EventEmitter<any> =  new EventEmitter();

    onClick = ($event, item) => {
        $event.stopPropagation();
        this.selectedItem = item;
        this.updateSelected.emit(item);
    };

    onSelectedUpdate = ($event) => {
        this.selectedItem = $event;
        this.updateSelected.emit($event);
    }
}
