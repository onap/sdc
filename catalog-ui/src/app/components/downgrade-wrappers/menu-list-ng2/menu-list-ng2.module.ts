import { NgModule } from "@angular/core";
import { CommonModule } from '@angular/common';
import { MenuListModule } from "../../ui/menu/menu-list.module";
import { MenuListNg2Component } from "./menu-list-ng2.component";

export {
    MenuListNg2Component
};

@NgModule({
    declarations: [
        MenuListNg2Component
    ],
    imports: [CommonModule, MenuListModule],
    exports: [
        MenuListNg2Component
    ],
    entryComponents: [ //need to add anything that will be dynamically created
        MenuListNg2Component
]
})
export class MenuListNg2Module {
}
