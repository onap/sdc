import {ContentAfterLastDotPipe} from "./contentAfterLastDot.pipe";
import {SearchFilterPipe} from "./searchFilter.pipe";
import {KeysPipe} from "./keys.pipe";
import {GroupByPipe} from "./groupBy.pipe";
import {ResourceNamePipe} from "./resource-name.pipe";
import {NgModule} from "@angular/core";
import {SafeUrlSanitizerPipe} from "./safeUrlSanitizer.pipe";
import {OrderByPipe} from "./orderBy.pipe";

@NgModule({
    declarations: [
        ContentAfterLastDotPipe,
        GroupByPipe,
        KeysPipe,
        SafeUrlSanitizerPipe,
        SearchFilterPipe,
        ResourceNamePipe,
        OrderByPipe
    ],
    exports: [
        ContentAfterLastDotPipe,
        GroupByPipe,
        KeysPipe,
        SafeUrlSanitizerPipe,
        SearchFilterPipe,
        ResourceNamePipe,
        OrderByPipe
    ]
})

export class GlobalPipesModule {}
