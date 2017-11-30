import {ContentAfterLastDotPipe} from "./contentAfterLastDot.pipe";
import {SearchFilterPipe} from "./searchFilter.pipe";
import {KeysPipe} from "./keys.pipe";
import {GroupByPipe} from "./groupBy.pipe";
import {NgModule} from "@angular/core";

@NgModule({
    declarations: [
        ContentAfterLastDotPipe,
        GroupByPipe,
        KeysPipe,
        SearchFilterPipe
        
    ],
    
    exports: [
        ContentAfterLastDotPipe,
        GroupByPipe,
        KeysPipe,
        SearchFilterPipe
    ]
})

export class GlobalPipesModule {}
