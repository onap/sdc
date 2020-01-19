import {Component, Input, Output, Inject, EventEmitter} from '@angular/core';
import {Component as ComponentModel} from 'app/models';
import {SdcMenuToken, IAppMenu} from "../../../config/sdc-menu.config";
import {ElementIcon} from "../sdc-element-icon/sdc-element-icon.component";
import {ComponentType, ResourceType, SdcElementType} from "../../../../utils/constants";

@Component({
    selector: 'ui-tile',
    templateUrl: './tile.component.html',
    styleUrls: ['./tile.component.less']
})
export class TileComponent {
    @Input() public component: ComponentModel;
    @Output() public onTileClick: EventEmitter<ComponentModel>;
    public catalogIcon: ElementIcon;
    public hasEllipsis: boolean;

    constructor(@Inject(SdcMenuToken) public sdcMenu: IAppMenu) {
        this.onTileClick = new EventEmitter<ComponentModel>();
        this.hasEllipsis = false;
    }

    ngOnInit(): void {
        switch (this.component.componentType) {

            case ComponentType.SERVICE:
                if (this.component.icon === 'defaulticon') {
                    this.catalogIcon = new ElementIcon(this.component.icon, "services_60", 'lightBlue', 'white');
                } else {
                    this.catalogIcon = new ElementIcon(this.component.icon, "services_60", '', 'lightBlue');
                }
                break;
            case ComponentType.RESOURCE:
                switch (this.component.getComponentSubType()) {
                    case ResourceType.CP:
                    case ResourceType.VL:
                        this.catalogIcon = new ElementIcon(this.component.icon, "resources_24", "purple", "white", "circle", 'medium');
                        break;
                    default:
                        if (this.component.icon === 'defaulticon') {
                            this.catalogIcon = new ElementIcon(this.component.icon, "resources_60", "purple", "white", "circle", 'x_large');
                        } else {
                            this.catalogIcon = new ElementIcon(this.component.icon, "resources_60", '', "error");
                        }

                }

        }
    }

    public tileClicked() {
        this.onTileClick.emit(this.component);
    }
}
