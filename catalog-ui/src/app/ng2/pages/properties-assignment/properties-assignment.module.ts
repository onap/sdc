import { NgModule } from "@angular/core";
import { PropertiesAssignmentComponent } from "./properties-assignment.page.component";
import { HierarchyNavigationComponent } from "./../../components/hierarchy-navigtion/hierarchy-navigation.component";
import { BrowserModule } from "@angular/platform-browser";
import { FormsModule } from "@angular/forms";
import { HttpModule } from "@angular/http";
import { TabModule } from '../../shared/tabs/tabs.module';
import { CheckboxModule} from '../../shared/checkbox/checkbox.module';
import { PropertiesTableComponent } from '../../components/properties-table/properties-table.component';
import { InputsTableComponent } from '../../components/inputs-table/inputs-table.component';
import { ContentAfterLastDotPipe } from "../../pipes/contentAfterLastDot.pipe";
import { SearchFilterPipe } from "../../pipes/searchFilter.pipe";
import { FilterChildPropertiesPipe } from "../../pipes/filterChildProperties.pipe";
import { DataTypeService } from './../../services/data-type.service';
import { PropertiesService } from './../../services/properties.service';
import { HierarchyNavService } from './../../services/hierarchy-nav.service';
import { PropertiesUtils } from './properties.utils';
import { PostsService } from "../../services/posts.service";
import { DynamicElementModule } from 'app/ng2/components/dynamic-element/dynamic-element.module';
import { DynamicPropertyComponent } from './../../components/properties-table/dynamic-property/dynamic-property.component';
import {ConfirmationDeleteInputComponent} from "app/ng2/components/inputs-table/confirmation-delete-input/confirmation-delete-input.component"
import { PopoverModule } from "../../components/popover/popover.module"
import { FilterPropertiesAssignmentComponent } from "./../../components/filter-properties-assignment/filter-properties-assignment.component";
import { GroupByPipe } from 'app/ng2/pipes/groupBy.pipe';
import { KeysPipe } from 'app/ng2/pipes/keys.pipe';
import {TooltipModule} from "../../components/tooltip/tooltip.module";
import { ComponentModeService } from "app/ng2/services/component-mode.service"
import { ModalComponent } from "app/ng2/components/modal/modal.component"
import {LoaderComponent} from "app/ng2/components/loader/loader.component"

@NgModule({
    declarations: [
        PropertiesAssignmentComponent,
        PropertiesTableComponent,
        InputsTableComponent,
        ContentAfterLastDotPipe,
        GroupByPipe,
        KeysPipe,
        SearchFilterPipe,
        FilterChildPropertiesPipe,
        HierarchyNavigationComponent,
        DynamicPropertyComponent,
        // PopoverContentComponent,
        // PopoverComponent,
        FilterPropertiesAssignmentComponent,
        ModalComponent,
        ConfirmationDeleteInputComponent,
        LoaderComponent
    ],
    imports: [
        BrowserModule,
        FormsModule,
        HttpModule,
        TabModule,
        CheckboxModule,
        DynamicElementModule,
        PopoverModule,
        TooltipModule
    ],
    entryComponents: [PropertiesAssignmentComponent],
    exports: [
        PropertiesAssignmentComponent
        // PopoverContentComponent,
        // PopoverComponent
    ],
    providers: [PropertiesService, HierarchyNavService, PropertiesUtils, DataTypeService, PostsService, ContentAfterLastDotPipe, GroupByPipe, KeysPipe, ComponentModeService]
})
export class PropertiesAssignmentModule {

}
