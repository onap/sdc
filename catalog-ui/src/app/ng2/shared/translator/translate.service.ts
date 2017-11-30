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

import { Injectable, Inject } from "@angular/core";
import { Response, Http } from "@angular/http";
import { Observable, Observer, ConnectableObservable, Subscription } from "rxjs";
import { ITranslateServiceConfig, TranslateServiceConfigToken } from "./translate.service.config";

export { ITranslateServiceConfig, TranslateServiceConfigToken };

export interface ITranslateLanguageJson {
    [index:string]: string;
}

export interface ITranslateArgs {
    [index:string]: any;
}

export class PhraseTranslator {
    private _observable:ConnectableObservable<string>;
    private _observer:Observer<string>;
    private _languageChangeSubscription:Subscription;

    private _phraseKey:string;
    private _args:ITranslateArgs;
    private _language:string;

    private _markForCheck:boolean = false;
    private _lastParams: {
        phraseKey: string;
        args: {[index: string]: any};
        language: string;
    } = {
        phraseKey: undefined,
        args: undefined,
        language: undefined
    };

    constructor(private translateService:TranslateService) {
        this._observable = Observable.create(observer => {
            this._observer = observer;
            this._languageChangeSubscription = this.translateService.languageChangedObservable.subscribe(language => {
                // using the active language, then force update
                if (!this._language) {
                    this.update(true);
                }
            });
        }).publishReplay(1).refCount();
    }

    public get observable() {
        return this._observable;
    }

    public destroy() {
        this._observer.complete();
        this._languageChangeSubscription.unsubscribe();

        delete this._observable;
        delete this._observer;
        delete this._languageChangeSubscription;
    }

    public shouldUpdate() : boolean {
        if (!this._markForCheck) {
            return false;
        }
        this._markForCheck = false;
        return (
            this._language !== this._lastParams.language ||
            this._args !== this._lastParams.args ||
            this._phraseKey !== this._lastParams.phraseKey
        );
    }

    public update(forceUpdate:boolean=false) : void {
        // only update translation when having subscriptions connected.
        if (this._observer && !this._observer.closed) {
            if (forceUpdate || this.shouldUpdate()) {
                this._lastParams = {
                    phraseKey: this._phraseKey,
                    args: this._args,
                    language: this._language
                };
                this._markForCheck = false;

                const translated = this.translateService.translate(this._phraseKey, this._args, this._language);
                this._observer.next(translated);
            }
        }
    }

    private _changeParam(paramKey:string, value:any, update:boolean) : void {
        this[`_${paramKey}`] = value;
        this._markForCheck = true;
        if (update) {
            this.update();
        }
    }

    public changePhraseKey(phraseKey:string, update:boolean=true) : void {
        this._changeParam('phraseKey', phraseKey, update);
    }

    public changeArgs(args:ITranslateArgs, update:boolean=true) :void {
        this._changeParam('args', args, update);
    }

    public changeLangauge(language:string, update:boolean=true) :void {
        this._changeParam('language', language, update);
    }

    public changeParams(phraseKey:string, args:ITranslateArgs={}, language?:string, forceUpdate:boolean=false) {
        this._phraseKey = phraseKey;
        this._args = args;
        this._language = language;
        this._markForCheck = true;
        this.update(forceUpdate);
    }
}


@Injectable()
export class TranslateService {
    private _activeLanguage:string;
    private _languageChangedObservable:ConnectableObservable<string>;
    private _languageChangedObserver:Observer<string>;
    private _cacheLanguagesJsons:{[index:string]:ITranslateLanguageJson} = {};
    private _cacheLanguagesLoaders:{[index:string]:Observable<ITranslateLanguageJson>} = {};

    constructor(@Inject(TranslateServiceConfigToken) private config:ITranslateServiceConfig, private http:Http) {
        this.initLanguageObservable();
        this.loadAndActivateLanguage(this.config.defaultLanguage);
    }

    public get languageChangedObservable() : Observable<string> {
        return this._languageChangedObservable;
    }

    public get activeLanguage() {
        return this._activeLanguage;
    }

    private initLanguageObservable() {
        this._languageChangedObservable = ConnectableObservable.create(observer => {
            this._languageChangedObserver = observer;
        }).publishReplay(1);  // replay last emitted change on subscribe
        this._languageChangedObservable.connect();
    }

    private loadLanguageJsonFile(language:string, emitOnLoad:boolean=true) : Observable<ITranslateLanguageJson> {
        if (this.config.allowedLanguages.indexOf(language) === -1) {
            return Observable.throw(`Language "${language}" is not available.`);
        }

        if (this._cacheLanguagesJsons[language]) {
            return Observable.of(this._cacheLanguagesJsons[language]);
        }

        if (!(language in this._cacheLanguagesLoaders)) {
            const filePath = `${this.config.filePrefix}${language}${this.config.fileSuffix}`;
            this._cacheLanguagesLoaders[language] = this.http.get(filePath)
                .map<Response, ITranslateLanguageJson>(resp => resp.json())
                .catch(() => Observable.throw(`Failed to load language file for "${language}"`))
                .publish();
            (<ConnectableObservable<ITranslateLanguageJson>>this._cacheLanguagesLoaders[language]).connect();
            this._cacheLanguagesLoaders[language].subscribe(languageJson => {
                    this._cacheLanguagesJsons[language] = languageJson;
                    delete this._cacheLanguagesLoaders[language];
                    if (emitOnLoad) {
                        this._languageChangedObserver.next(language);
                    }
                    return languageJson;
                });
        }
        return this._cacheLanguagesLoaders[language];
    }

    public activateLanguage(language:string) : boolean {
        if (this._cacheLanguagesJsons[language]) {
            if (language !== this._activeLanguage) {
                this._activeLanguage = language;
                this._languageChangedObserver.next(this._activeLanguage);
            }
            return true;
        }
        return false;
    }

    public loadAndActivateLanguage(language:string) : Observable<ITranslateLanguageJson> {
        const loadLanguageObservable = this.loadLanguageJsonFile(language, false);
        loadLanguageObservable.subscribe(() => {
            this.activateLanguage(language);
        }, () => {});
        return loadLanguageObservable;
    }

    public translate(phraseKey:string, args:ITranslateArgs={}, language:string=this._activeLanguage) : string {
        const phrase:string = (this._cacheLanguagesJsons[language] || {})[phraseKey] || '';
        let translated:string;
        if (typeof(phrase) === 'string') {
            translated = phrase
                .replace(
                    /(^|[^\\]|\\\\)\{\{(\w+)\}\}/g,
                    (m, p1, p2) => `${p1}${args[p2]||''}`
                )
                .replace('\\{{', '{{')
                .replace('\\\\', '\\');
        }
        return translated;
    }

    public createPhraseTranslator(phraseKey?:string, args?:ITranslateArgs, language?:string) : PhraseTranslator {
        const phraseTranslator = new PhraseTranslator(this);
        phraseTranslator.changeParams(phraseKey, args, language);
        return phraseTranslator;
    }
}
