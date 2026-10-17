import {Component, Input, Output, Inject, EventEmitter} from '@angular/core';
import {Component as ComponentModel} from 'app/models/components/component';
import {SdcMenuToken, IAppMenu} from "../../../config/sdc-menu.config";
import {ElementIcon} from "../sdc-element-icon/sdc-element-icon.component";
import {ComponentType, DEFAULT_MODEL_NAME, Icon, ResourceType} from "../../../utils/constants";
import {DataTypeCatalogComponent} from "../../../models/data-type-catalog-component";
import {ResourceNamePipe} from "../../../pipes/resource-name.pipe";

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

    /** e.g. "Basic_cnf_macro_gnb, VF, version 0.1, In Design": two assets of one name stay distinguishable. */
    public get accessibleName(): string {
        const component: any = this.component;
        const parts: string[] = [ResourceNamePipe.getDisplayName(component.name)];
        parts.push(component.isService() ? 'Service' : component.getComponentSubType());
        if (component.isResource() || component.isService()) {
            parts.push('version ' + component.version);
        }
        if (component.model) {
            parts.push('model ' + component.model);
        }
        if (component.isResource() || component.isService()) {
            parts.push(component.getStatus(this.sdcMenu));
        }
        return parts.filter((part) => !!part).join(', ');
    }

    public tileClicked() {
        this.onTileClick.emit(this.component);
    }
}
