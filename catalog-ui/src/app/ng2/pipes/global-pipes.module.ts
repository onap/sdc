import {ContentAfterLastDotPipe} from "./contentAfterLastDot.pipe";
import {SearchFilterPipe} from "./searchFilter.pipe";
import {KeysPipe} from "./keys.pipe";
import {GroupByPipe} from "./groupBy.pipe";
import {NgModule} from "@angular/core";
import {SafeUrlSanitizerPipe} from "./safeUrlSanitizer.pipe";

@NgModule({
    declarations: [
        ContentAfterLastDotPipe,
        GroupByPipe,
        KeysPipe,
        SearchFilterPipe,
        SafeUrlSanitizerPipe
    ],

    exports: [
        ContentAfterLastDotPipe,
        GroupByPipe,
        KeysPipe,
        SearchFilterPipe,
        SafeUrlSanitizerPipe
    ]
})

export class GlobalPipesModule {}
