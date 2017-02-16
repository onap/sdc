/**
 * Created by obarda on 11/7/2016.
 */
/// <reference path="../../references"/>

module Sdc.Models.Graph {


    export class Point {
        /**
         * The two-argument constructor produces the Point(x, y).
         * @param {number} x
         * @param {number} y
         */
        constructor(x?:number, y?:number) {
            this.x = x || 0;
            this.y = y || 0;
        }

        /**Gets or sets the x value of the Point.*/
        x:number;

        /**Gets or sets the y value of the Point.*/
        y:number;
    }
}