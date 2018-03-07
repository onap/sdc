/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import { NgModule } from '@angular/core';
import { NavbarModule } from "./navbar/navbar.module";
import {DynamicElementModule} from "./dynamic-element/dynamic-element.module";
import {FormElementsModule} from "./form-components/form-elements.module";
import {LoaderComponent} from "./loader/loader.component";
import {ModalModule} from "./modal/modal.module";
import {PopoverModule} from "./popover/popover.module";
import {SearchBarComponent} from "./search-bar/search-bar.component";
import {SearchWithAutoCompleteComponent} from "./search-with-autocomplete/search-with-autocomplete.component";
import {PalettePopupPanelComponent} from "./palette-popup-panel/palette-popup-panel.component";
import {ZoneContainerComponent} from "./canvas-zone/zone-container.component";
import {ZoneInstanceComponent } from "./canvas-zone/zone-instance/zone-instance.component";
import {PaletteAnimationComponent} from "./palette-animation/palette-animation.component"
import {TabModule} from "./tabs/tabs.module";
import {TooltipModule} from "./tooltip/tooltip.module";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {BrowserModule} from "@angular/platform-browser";
import {MultiStepsWizardModule} from "./multi-steps-wizard/multi-steps-wizard.module";
import {MenuListModule} from "./menu/menu-list.module";
import {MenuListNg2Module} from "../downgrade-wrappers/menu-list-ng2/menu-list-ng2.module";
//import {SdcUiComponentsModule} from "sdc-ui/lib/angular";

@NgModule({
  declarations: [
    LoaderComponent,
    SearchBarComponent,
    SearchWithAutoCompleteComponent,
    PalettePopupPanelComponent,
    ZoneContainerComponent,
    ZoneInstanceComponent,
    PaletteAnimationComponent
],

  imports: [
    //SdcUiComponentsModule,
    BrowserModule,
    FormsModule,
    CommonModule,
    DynamicElementModule,
    NavbarModule,
    FormElementsModule,
    ModalModule,
    PopoverModule,
    TabModule,
    TooltipModule,
    MultiStepsWizardModule,
    MenuListModule,
    MenuListNg2Module
  ],
  exports: [
    LoaderComponent,
    MultiStepsWizardModule,
    SearchBarComponent,
    SearchWithAutoCompleteComponent,
    PalettePopupPanelComponent,
    ZoneContainerComponent,
    ZoneInstanceComponent,
    DynamicElementModule,
    NavbarModule,
    FormElementsModule,
    ModalModule,
    PopoverModule,
    TabModule,
    TooltipModule,
    MenuListModule,
    MenuListNg2Module,
    PaletteAnimationComponent
  ],
  entryComponents: [SearchWithAutoCompleteComponent, PalettePopupPanelComponent, ZoneContainerComponent, ZoneInstanceComponent, PaletteAnimationComponent]
})

export class UiElementsModule {}
