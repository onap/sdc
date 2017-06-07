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
import { PropertiesUtils } from './properties.utils';
import { PostsService } from "../../services/posts.service";
import { PropertiesValueInnerTableComponent } from "./../../components/properties-table/properties-value-inner-table/properties-value-inner-table.component";
import { ListPropertyComponent } from "./../../components/properties-table/list-property/list-property.component";
import { MapPropertyComponent } from "./../../components/properties-table/map-property/map-property.component";
import { DynamicElementModule } from 'app/ng2/components/dynamic-element/dynamic-element.module';
import { DynamicPropertyComponent } from './../../components/properties-table/dynamic-property/dynamic-property.component';
import { DerivedPropertyComponent } from './../../components/properties-table/derived-property/derived-property.component';
// import {PopoverContentComponent} from "../../components/popover/popover-content.component"
// import {PopoverComponent} from "../../components/popover/popover.component"
import { PopoverModule } from "../../components/popover/popover.module"
import { FilterPropertiesAssignmentComponent } from "./../../components/filter-properties-assignment/filter-properties-assignment.component";
import { GroupByPipe } from 'app/ng2/pipes/groupBy.pipe';
import { KeysPipe } from 'app/ng2/pipes/keys.pipe';
import {TooltipModule} from "../../components/tooltip/tooltip.module";

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
        PropertiesValueInnerTableComponent,
        ListPropertyComponent,
        MapPropertyComponent,
        DerivedPropertyComponent,
        DynamicPropertyComponent,
        // PopoverContentComponent,
        // PopoverComponent,
        FilterPropertiesAssignmentComponent
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
    providers: [PropertiesService, PropertiesUtils, DataTypeService, PostsService, ContentAfterLastDotPipe, GroupByPipe, KeysPipe]
})
export class PropertiesAssignmentModule {

}
