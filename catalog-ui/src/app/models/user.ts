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
