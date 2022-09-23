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
                        this.catalogIcon = new ElementIcon(this.component.icon, Icon.SERVICE_TYPE_60, Icon.COLOR_LIGHTBLUE, Icon.COLOR_WHITE);
                    } else {
                        this.catalogIcon = new ElementIcon(this.component.icon, Icon.SERVICE_TYPE_60, '', Icon.COLOR_LIGHTBLUE);
                    }
                    break;
                case ComponentType.RESOURCE:
                    switch (this.component.getComponentSubType()) {
                        case ResourceType.CP:
                        case ResourceType.VL:
                            this.catalogIcon = new ElementIcon(this.component.icon, Icon.RESOURCE_TYPE_24, Icon.COLOR_PURPLE, Icon.COLOR_WHITE, Icon.SHAPE_CIRCLE, Icon.SIZE_MEDIUM);
                            break;
                        default:
                            if (this.component.icon === Icon.DEFAULT_ICON) {
                                this.catalogIcon = new ElementIcon(this.component.icon, Icon.RESOURCE_TYPE_60, Icon.COLOR_PURPLE, Icon.COLOR_WHITE, Icon.SHAPE_CIRCLE, Icon.SIZE_X_LARGE);
                            } else {
                                this.catalogIcon = new ElementIcon(this.component.icon, Icon.RESOURCE_TYPE_60, '', Icon.ERROR);
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
