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

export enum UserRole {
    ADMIN,
    DESIGNER,
    TESTER,
    GOVERNOR,
    OPS
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
    userInfo:IUserProperties;
    getRole():UserRole;
    getRoleToView():string;
    getName():string;
    getFirstName():string;
    getLastName():string;
}

export class User implements IUser {

    constructor(public userInfo:IUserProperties) {
    }

    public getLastName = () => {
        return this.userInfo.lastName;
    };

    public getFirstName = () => {
        return this.userInfo.firstName;
    };

    public getName = () => {
        return this.userInfo.firstName + ' ' + this.userInfo.lastName;
    };

    public getLastLogin = () => {
        if (!this.userInfo.lastLoginTime || this.userInfo.lastLoginTime === "0") {
            return "";
        } else {
            return this.userInfo.lastLoginTime;
        }
    };

    public getRole = ():UserRole => {
        let role:UserRole;
        switch (UserRole[this.userInfo.role.toUpperCase()]) {
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
        }
        return role;
    };

    public getRoleToView = ():string => {
        let role:string = this.userInfo.role.toLowerCase().replace('governor', 'governance_Rep');
        return role.charAt(0).toUpperCase() + role.slice(1).replace('_', ' ');
    }
}
