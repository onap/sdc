import {Component, Input, Output, Inject, EventEmitter} from '@angular/core';
import {Component as ComponentModel} from 'app/models';
import {SdcMenuToken, IAppMenu} from "../../../config/sdc-menu.config";
import {ElementIcon} from "../sdc-element-icon/sdc-element-icon.component";
import {ComponentType, DEFAULT_MODEL_NAME, Icon, ResourceType} from "../../../../utils/constants";
import {DataTypeCatalogComponent} from "../../../../models/data-type-catalog-component";

@Component({
    selector: 'ui-tile',
    templateUrl: './tile.component.html',
    styleUrls: ['./tile.component.less']
})
export class TileComponent {
    @Input() public component: ComponentModel | DataTypeCatalogComponent;
    @Output() public onTileClick: EventEmitter<ComponentModel | DataTypeCatalogComponent>;
    public catalogIcon: ElementIcon;
    public hasEllipsis: boolean;

    constructor(@Inject(SdcMenuToken) public sdcMenu: IAppMenu) {
        this.onTileClick = new EventEmitter<ComponentModel | DataTypeCatalogComponent>();
        this.hasEllipsis = false;
    }

    ngOnInit(): void {
        if (this.component instanceof ComponentModel) {
            switch (this.component.componentType) {
                case ComponentType.SERVICE:
                    if (this.component.icon === Icon.DEFAULT_ICON) {
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
                            if (this.component.icon === Icon.DEFAULT_ICON) {
                                this.catalogIcon = new ElementIcon(this.component.icon, "resources_60", "purple", "white", "circle", 'x_large');
                            } else {
                                this.catalogIcon = new ElementIcon(this.component.icon, "resources_60", '', "error");
                            }

                    }
            }
        }
    }

    public getComponentModel() {
        if (this.component.model === undefined) {
            return DEFAULT_MODEL_NAME;
        } else {
            return this.component.model;
        }
    }

    public tileClicked() {
        this.onTileClick.emit(this.component);
    }
}
