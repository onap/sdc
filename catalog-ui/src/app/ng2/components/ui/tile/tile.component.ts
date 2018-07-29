import {Component, Input, Output, Inject, EventEmitter} from '@angular/core';
import {Component as ComponentModel} from 'app/models';
import {SdcMenuToken, IAppMenu} from "../../../config/sdc-menu.config";

@Component({
    selector: 'ui-tile',
    templateUrl: './tile.component.html',
    styleUrls: ['./tile.component.less']
})
export class TileComponent {
    @Input() public component: ComponentModel;
    @Output() public onTileClick: EventEmitter<ComponentModel>;

    public hasEllipsis: boolean;

    constructor(@Inject(SdcMenuToken) public sdcMenu:IAppMenu) {
        this.onTileClick = new EventEmitter<ComponentModel>();
        this.hasEllipsis = false;
    }

    public tileClicked() {
        this.onTileClick.emit(this.component);
    }
}
