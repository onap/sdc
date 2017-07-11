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

class basePattern {
    pattern:RegExp;
    base:number;

    constructor(pattern:RegExp, base:number) {
        this.pattern = pattern;
        this.base = base;
    }
}

export interface IMapRegex {
    integer:RegExp;
    boolean:RegExp;
    float:RegExp;
    string:RegExp;
}

export class ValidationUtils {

    static '$inject' = [
        'IntegerNoLeadingZeroValidationPattern',
        'FloatValidationPattern',
        'CommentValidationPattern',
        'BooleanValidationPattern',
        'NumberValidationPattern',
        'LabelValidationPattern',
    ];
    private trueRegex:string = '[t][r][u][e]|[t]|[o][n]|[y]|[y][e][s]|[1]';
    private falseRegex:string = '[f][a][l][s][e]|[f]|[o][f][f]|[n]|[n][o]|[0]';
    private heatBooleanValidationPattern:RegExp = new RegExp('^(' + this.trueRegex + '|' + this.falseRegex + ')$');


    constructor(private IntegerNoLeadingZeroValidationPattern:RegExp,
                private FloatValidationPattern:RegExp,
                private CommentValidationPattern:RegExp,
                private BooleanValidationPattern:RegExp,
                private NumberValidationPattern:RegExp,
                private LabelValidationPattern:RegExp) {
    }

    public stripAndSanitize(text:string):string {
        if (!text) {
            return null;
        }
        return text.replace(/\s+/g, ' ').replace(/%[A-Fa-f0-9]{2}/g, '').trim();
    }

    public getValidationPattern = (validationType:string, parameterType?:string):RegExp => {
        switch (validationType) {
            case 'integer':
                return this.IntegerNoLeadingZeroValidationPattern;
            case 'float':
                return this.FloatValidationPattern;
            case 'number':
                return this.NumberValidationPattern;
            case 'string':
                return this.CommentValidationPattern;
            case 'boolean':
            {
                //Bug Fix DE197437 [Patch]Mismatch between BE to FE regarding supported characters in Boolean filed
                if (parameterType && parameterType === 'heat') {
                    return this.heatBooleanValidationPattern;
                }
                else {
                    return this.BooleanValidationPattern;
                }
            }

            case 'label':
                return this.LabelValidationPattern;
            case 'category':
                return this.LabelValidationPattern;
            default :
                return null;
        }
    };

    public getPropertyListPatterns():IMapRegex {
        return {
            integer: /^(0|[-+]?[1-9][0-9]*|[-+]?0x[0-9a-fA-F]+|[-+]?0o[0-7]+)(,?(0|[-+]?[1-9][0-9]*|[-+]?0x[0-9a-fA-F]+|[-+]?0o[0-7]+))*$/,
            string: /^"[\u0000-\u0021\u0023-\u00BF]+"(\s*,?\s*"[\u0000-\u0021\u0023-\u00BF]+")*$/,
            boolean: /^([Tt][Rr][Uu][Ee]|[Ff][Aa][Ll][Ss][Ee])(,?([Tt][Rr][Uu][Ee]|[Ff][Aa][Ll][Ss][Ee]))*$/,
            float: /^[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?(,?[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?f?)*$/
        };
    }

    public getPropertyMapPatterns():IMapRegex {
        return {
            integer: /^"\w+"\s*:\s?(0|[-+]?[1-9][0-9]*|[-+]?0x[0-9a-fA-F]+|[-+]?0o[0-7]+)+(\s*,?\s*"\w+"\s?:\s?(0|[-+]?[1-9][0-9]*|[-+]?0x[0-9a-fA-F]+|[-+]?0o[0-7]+)+)*$/,
            string: /^"\w+"\s?:\s?"[\u0000-\u0021\u0023-\u00BF]*"(\s*,?\s*"\w+"\s?:\s?"[\u0000-\u0021\u0023-\u00BF]*")*$/,
            boolean: /^"\w+"\s?:\s?([Tt][Rr][Uu][Ee]|[Ff][Aa][Ll][Ss][Ee])(\s*,?\s*"\w+"\s?:\s?([Tt][Rr][Uu][Ee]|[Ff][Aa][Ll][Ss][Ee]))*$/,
            float: /^"\w+"\s?:\s?[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?f?(\s*,?\s*"\w+"\s?:\s?[-+]?[0-9]*\.?[0-9]+([eE][-+]?[0-9]+)?f?)*$/
        };
    }

    public validateUniqueKeys(viewValue:string):boolean {
        if (!viewValue) {
            return true; //allow empty value
        }

        let json:string = "{" + viewValue.replace(/\s\s+/g, ' ') + "}";
        try {
            let obj:any = JSON.parse(json);
            /*
             //Method #1 : check json string length before & after parsing
             let newJson:string = JSON.stringify(obj);
             if (newJson.length < json.length) {
             return false;
             }*/

            //Method #2 : check how many times we can find "KEY": in json string
            let result:boolean = true;
            Object.keys(obj).forEach((key:string) => {
                result = result && json.split('"' + key + '":').length === 2;
            });
            return result;

        } catch (e) {
            return false; //not a valid JSON
        }

        //return true;
    }

    public validateJson = (json:string):boolean => {
        try {
            JSON.parse(json);
            return true;
        } catch (err) {
            console.log('invalid json');
            return false;
        }
    };

    public validateIntRange = (value:string):boolean => {

        let base8 = new basePattern(/^([-+]?0o[0-7]+)$/, 8);
        let base10 = new basePattern(/^(0|[-+]?[1-9][0-9]*)$/, 10);
        let base16 = new basePattern(/^([-+]?0x[0-9a-fA-F]+)$/, 16);

        let min:number = -0x80000000;
        let max:number = 0x7fffffff;
        let intPatterns:Array<basePattern> = [base8, base10, base16];
        let matchedBase = _.find(intPatterns, (item)=> {
            return item.pattern.test(value);
        });

        let parsed:number = parseInt(value.replace('o', ''), matchedBase.base);
        if (parsed) {
            return min <= parsed && max >= parsed;
        }
    }
}
