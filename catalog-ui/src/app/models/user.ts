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

'use strict';
import {IUserResource} from "../services/user-resource-service";

export enum UserRole {
    ADMIN,
    DESIGNER,
    TESTER,
    GOVERNOR,
    OPS,
    PRODUCT_MANAGER,
    PRODUCT_STRATEGIST
}

export interface IUserManager {
    isInEditMode:boolean;
    filterTerm:string;
}

export interface IUserProperties extends IUserManager {
    firstName:string;
    lastName:string;
    userId:string;
    email:string;
    role:string;
    tempRole:string;
    lastLoginTime:string;
    status:string;
}

export interface IUser {
    resource:IUserResource;
    getRole():UserRole;
    getRoleToView():string;
    getName():string;
    getFirstName():string;
    getLastName():string;
}

export class User implements IUser {

    constructor(public resource:IUserResource) {
    }

    public getLastName = () => {
        return this.resource.lastName;
    }

    public getFirstName = () => {
        return this.resource.firstName;
    }

    public getName = () => {
        return this.resource.firstName + ' ' + this.resource.lastName;
    }

    public getLastLogin = () => {
        if (!this.resource.lastLoginTime || this.resource.lastLoginTime === "0") {
            return "";
        } else {
            return this.resource.lastLoginTime;
        }
    }

    public getRole = ():UserRole => {
        let role:UserRole;
        switch (UserRole[this.resource.role.toUpperCase()]) {
            case UserRole.ADMIN:
                role = UserRole.ADMIN;
                break;
            case UserRole.DESIGNER:
                role = UserRole.DESIGNER;
                break;
            case UserRole.TESTER:
                role = UserRole.TESTER;
                break;
            case UserRole.GOVERNOR:
                role = UserRole.GOVERNOR;
                break;
            case UserRole.OPS:
                role = UserRole.OPS;
                break;
            case UserRole.PRODUCT_MANAGER:
                role = UserRole.PRODUCT_MANAGER;
                break;
            case UserRole.PRODUCT_STRATEGIST:
                role = UserRole.PRODUCT_STRATEGIST;
                break;
        }
        return role;
    }

    public getRoleToView = ():string => {
        let role:string = this.resource.role.toLowerCase().replace('governor', 'governance_Rep');
        return role.charAt(0).toUpperCase() + role.slice(1).replace('_', ' ');
    }
}
