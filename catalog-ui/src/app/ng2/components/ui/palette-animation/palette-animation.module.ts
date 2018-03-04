import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { PaletteAnimationComponent } from "./palette-animation.component";


@NgModule({
    declarations: [
        PaletteAnimationComponent
    ],
    imports: [ CommonModule ],
    exports: [ PaletteAnimationComponent ]
})

export class PaletteAnimationModule {

}