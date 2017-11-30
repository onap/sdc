import { NgModule } from "@angular/core";
import { CommonModule } from '@angular/common';
import { MenuListComponent } from "./menu-list.component";
import { MenuItemComponent } from "./menu-item.component";

export {
    MenuListComponent,
    MenuItemComponent
};

@NgModule({
    declarations: [
        MenuListComponent,
        MenuItemComponent
    ],
    imports: [CommonModule],
    exports: [
        MenuListComponent,
        MenuItemComponent
    ],
    entryComponents: [ //need to add anything that will be dynamically created
        MenuListComponent,
        MenuItemComponent
    ]
})
export class MenuListModule {
}
