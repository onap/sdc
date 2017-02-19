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
interface String {
    format(variables:Array<string>):string
}

interface Array<T> {
    clean(o: T): Array<T>;
}


/**
 * This function will replace the %<number> with strings (from array).
 * Example: "Requested '%1' resource was not found.".format(["MyResource"]);
 * Note: in case the array contains empty string the function will also remove the '' or the "".
 */
if (!String.hasOwnProperty("format")) {
    String.prototype["format"] = function (variables:Array<string>) : string {

        if (variables===null || variables===undefined || variables.length===0){
            variables=[''];
        }

        for (let i=0;i<variables.length;i++){
            if (variables[i]==='' || variables[i]===null){
                variables[i]='--DELETE--';
            }
        }

        let res = this.replace(/%(\d+)/g, function(_,m) {
            return variables[--m];
        });

        res = res.replace(" '--DELETE--' "," ");
        res = res.replace(" \"--DELETE--\" "," ");
        res = res.replace("'--DELETE--'","");
        res = res.replace("\"--DELETE--\"","");
        res = res.replace("--DELETE--","");

        return res;
    };
}

if (!String.hasOwnProperty("capitalizeFirstLetter")) {
    String.prototype["capitalizeFirstLetter"] = function() {
        return this.charAt(0).toUpperCase() + this.slice(1);
    };
}

if (!String.hasOwnProperty("replaceAll")) {
    String.prototype["replaceAll"] = function (find:string, replace:string) : string {
        return this.replace(new RegExp(find, 'g'), replace);
    };
}

if (!Array.hasOwnProperty("clean")) {
    Array.prototype.clean = function (deleteValue) {
        for (let i = 0; i < this.length; i++) {
            if (this[i] == deleteValue) {
                this.splice(i, 1);
                i--;
            }
        }
        return this;
    };
}

if (!Array.prototype.map) {
    Array.prototype.map = function(callback, thisArg) {

        let T, A, k;

        if (this == null) {
            throw new TypeError(" this is null or not defined");
        }

        // 1. Let O be the result of calling ToObject passing the |this| value as the argument.
        let O = Object(this);

        // 2. Let lenValue be the result of calling the Get internal method of O with the argument "length".
        // 3. Let len be ToUint32(lenValue).
        let len = O.length >>> 0;

        // 4. If IsCallable(callback) is false, throw a TypeError exception.
        // See: http://es5.github.com/#x9.11
        if (typeof callback !== "function") {
            throw new TypeError(callback + " is not a function");
        }

        // 5. If thisArg was supplied, let T be thisArg; else let T be undefined.
        if (thisArg) {
            T = thisArg;
        }

        // 6. Let A be a new array created as if by the expression new Array(len) where Array is
        // the standard built-in constructor with that name and len is the value of len.
        A = new Array(len);

        // 7. Let k be 0
        k = 0;

        // 8. Repeat, while k < len
        while(k < len) {

            let kValue, mappedValue;

            // a. Let Pk be ToString(k).
            //   This is implicit for LHS operands of the in operator
            // b. Let kPresent be the result of calling the HasProperty internal method of O with argument Pk.
            //   This step can be combined with c
            // c. If kPresent is true, then
            if (k in O) {

                // i. Let kValue be the result of calling the Get internal method of O with argument Pk.
                kValue = O[ k ];

                // ii. Let mappedValue be the result of calling the Call internal method of callback
                // with T as the this value and argument list containing kValue, k, and O.
                mappedValue = callback.call(T, kValue, k, O);

                // iii. Call the DefineOwnProperty internal method of A with arguments
                // Pk, Property Descriptor {Value: mappedValue, : true, Enumerable: true, Configurable: true},
                // and false.

                // In browsers that support Object.defineProperty, use the following:
                // Object.defineProperty(A, Pk, { value: mappedValue, writable: true, enumerable: true, configurable: true });

                // For best browser support, use the following:
                A[ k ] = mappedValue;
            }
            // d. Increase k by 1.
            k++;
        }

        // 9. return A
        return A;
    };
}
