import {ContentAfterLastDotPipe} from "./contentAfterLastDot.pipe";
import {SearchFilterPipe} from "./searchFilter.pipe";
import {KeysPipe} from "./keys.pipe";
import {GroupByPipe} from "./groupBy.pipe";
import {ResourceNamePipe} from "./resource-name.pipe";
import {NgModule} from "@angular/core";
import {SafeUrlSanitizerPipe} from "./safeUrlSanitizer.pipe";

@NgModule({
    declarations: [
        ContentAfterLastDotPipe,
        GroupByPipe,
        KeysPipe,
        SafeUrlSanitizerPipe,
        SearchFilterPipe,
        ResourceNamePipe
    ],
    exports: [
        ContentAfterLastDotPipe,
        GroupByPipe,
        KeysPipe,
        SafeUrlSanitizerPipe,
        SearchFilterPipe,
        ResourceNamePipe
    ]
})

export class GlobalPipesModule {}
