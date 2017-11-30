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

import { Pipe, PipeTransform } from '@angular/core';
import { TranslateService, ITranslateArgs } from "./translate.service";


@Pipe({
    name: 'translate',
    pure: false
})
export class TranslatePipe implements PipeTransform {
    private translated:string;
    private lastParams: {
        phrase: string;
        args: {[index: string]: any};
        language: string;
    } = {
        phrase: undefined,
        args: undefined,
        language: undefined
    };

    constructor(private translateService:TranslateService) {
    }

    private shouldUpdate(curParams:{[index:string]: any}) : boolean {
        return (
            curParams.language !== this.lastParams.language ||
            curParams.args !== this.lastParams.args ||
            curParams.phrase !== this.lastParams.phrase
        );
    }

    public transform(phrase:string, args:ITranslateArgs, language:string=this.translateService.activeLanguage) : string {
        const curParams = { phrase, args, language };
        if (this.shouldUpdate(curParams)) {
            this.lastParams = curParams;
            this.translated = this.translateService.translate(phrase, args, language);
            console.log('*updated:', this.translated);
        }

        return this.translated;
    }
}
