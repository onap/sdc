(function (scope) {
    var Class = function (param1, param2) {

        var extend, mixins, definition;
        if (param2) {     //two parameters passed, first is extends, second definition object
            extend = Array.isArray(param1) ? param1[0] : param1;
            mixins = Array.isArray(param1) ? param1.slice(1) : null;
            definition = param2;
        } else {      //only one parameter passed => no extend, only definition
            extend = null;
            definition = param1;
        }


        var Definition = definition.hasOwnProperty("constructor") ? definition.constructor : function () {
        };

        Definition.prototype = Object.create(extend ? extend.prototype : null);
        var propertiesObject = definition.propertiesObject ? definition.propertiesObject : {};
        if (mixins) {
            var i, i2;
            for (i in mixins) {
                for (i2 in mixins[i].prototype) {
                    Definition.prototype[i2] = mixins[i].prototype[i2];
                }
                for (var i2 in mixins[i].prototype.propertiesObject) {
                    propertiesObject[i2] = mixins[i].prototype.propertiesObject[i2];
                }
            }
        }

        Definition.prototype.propertiesObject = propertiesObject;

        Object.defineProperties(Definition.prototype, propertiesObject);

        for (var key in definition) {
            if (definition.hasOwnProperty(key)) {
                Definition.prototype[key] = definition[key];
            }
        }

        Definition.prototype.constructor = Definition;

        return Definition;
    };


    var Interface = function (properties) {
        this.properties = properties;
    };

    var InterfaceException = function (message) {
        this.name = "InterfaceException";
        this.message = message || "";
    };

    InterfaceException.prototype = new Error();

    Interface.prototype.implements = function (target) {
        for (var i in this.properties) {
            if (target[this.properties[i]] == undefined) {
                throw new InterfaceException("Missing property " + this.properties[i]);
            }
        }
        return true;
    };

    Interface.prototype.doesImplement = function (target) {
        for (var i in this.properties) {
            if (target[this.properties[i]] === undefined) {
                return false;
            }
        }
        return true;
    };

    var VectorMath = {
        distance: function (vector1, vector2) {
            return Math.sqrt(Math.pow(vector1.x - vector2.x, 2) + Math.pow(vector1.y - vector2.y, 2));
        }
    };

    var EventDispatcher = Class({
        constructor: function () {
            this.events = {};
        },
        on: function (name, listener, context) {
            this.events[name] = this.events[name] ? this.events[name] : [];
            this.events[name].push({
                listener: listener,
                context: context
            })
        },
        once: function (name, listener, context) {
            this.off(name, listener, context);
            this.on(name, listener, context);
        },
        off: function (name, listener, context) {
            //no event with this name registered? => finish
            if (!this.events[name]) {
                return;
            }
            if (listener) {		//searching only for certains listeners
                for (var i in this.events[name]) {
                    if (this.events[name][i].listener === listener) {
                        if (!context || this.events[name][i].context === context) {
                            this.events[name].splice(i, 1);
                        }
                    }
                }
            } else {
                delete this.events[name];
            }
        },
        trigger: function (name) {
            var listeners = this.events[name];

            for (var i in listeners) {
                listeners[i].listener.apply(listeners[i].context, Array.prototype.slice.call(arguments, 1));
            }
        }
    });

    exports.CytoscapeEdgeEditation = Class({

        init: function (cy) {
            this.DOUBLE_CLICK_INTERVAL = 300;
            this.HANDLE_SIZE = 18;
            this.ARROW_END_ID = "ARROW_END_ID";

            this._handles = {};
            this._dragging = false;
            this._hover = null;
            this._tagMode = false;

            this._cy = cy;
            this._$container = $(cy.container());

            this._$canvas = $('<canvas></canvas>');
            this._$canvas.css("top", 0);

            this._ctx = this._$canvas[0].getContext('2d');
            this._$container.children("div").append(this._$canvas);

            this._resizeCanvas();

            this.initContainerEvents();

        },
        initContainerEvents: function () {
            this._cy.on("resize", this._resizeCanvas.bind(this));
            /*$(window).bind('resize', this._resizeCanvas.bind(this));
             $(window).bind('resize', this._resizeCanvas.bind(this));*/

             this._$container.bind('resize', function () {
                this._resizeCanvas();
            }.bind(this));

            this._cy.bind('zoom pan', this._redraw.bind(this));

            this._cy.on('showhandle', function (cy, target, customHandle) {
                this.permanentHandle = true;
                this._showHandles(target, customHandle);
            }.bind(this));

            this._cy.on('hidehandles', this._hideHandles.bind(this));

            this._$container.on('mouseout', function (e) {
                if (this.permanentHandle) {
                    return;
                }

                this._clear();
            }.bind(this));


        },
        initNodeEvents: function (){

            this._$canvas.on("mousedown", this._mouseDown.bind(this));
            this._$canvas.on("mousemove", this._mouseMove.bind(this));
            this._$canvas.on('mouseup', this._mouseUp.bind(this));

            this._cy.on('tapdragover', 'node', this._mouseOver.bind(this));
            this._cy.on('tapdragout', 'node', this._mouseOut.bind(this));

            

            //this._cy.on("select", "node", this._redraw.bind(this))

            this._cy.on("mousedown", "node", function () {
                if(!this._tagMode) {
                    this._nodeClicked = true;
                }
            }.bind(this));

            this._cy.on("mouseup", "node", function () {
                this._nodeClicked = false;
            }.bind(this));

            this._cy.on("remove", "node", function () {
                this._hover = false;
                this._clear();
            }.bind(this));

            // this._$container.on('mouseover', function (e) {
            //     if (this._hover) {
            //         this._mouseOver({cyTarget: this._hover});
            //     }
            // }.bind(this));

            this._cy.on('tagstart', function(){
                this._tagMode = true;
            }.bind(this));

            this._cy.on('tagend', function(){
                this._tagMode = false;
            }.bind(this))

        },
        registerHandle: function (handle) {
            
            if (handle.imageUrl) {

                var base_image = new Image();
                base_image.src = handle.imageUrl;
                base_image.onload = function() {
                    handle.image = base_image;
                  };
            }
            
            this._handles[handle.type] = this._handles[handle.type] || [];
            this._handles[handle.type] = handle;


        },
        _showHandles: function (target, handleType) {

            if(!handleType){
                handleType = 'add-edge'; //ie, CanvasHandleTypes.ADD_EDGE, which is the default
            }
            this._drawHandle(this._handles[handleType], target);

        },
        _clear: function () {

            var w = this._$container.width();
            var h = this._$container.height();
            this._ctx.clearRect(0, 0, w, h);
        },
        _drawHandle: function (handle, target) {

            target.data().handleType = handle.type;
            var position = this._getHandlePosition(target);
            var handleSize = this.HANDLE_SIZE * this._cy.zoom();
            this._ctx.clearRect(position.x, position.y, handleSize, handleSize);
            
            if (handle.image) {
                this._ctx.drawImage(handle.image, position.x, position.y, handleSize, handleSize);
            }
        },
        _drawArrow: function (fromNode, toPosition, handle) {
            var toNode;
            if (this._hover) {
                toNode = this._hover;
            } else {
                if (!this._arrowEnd) {
                    this._arrowEnd = this._cy.add({
                        group: "nodes",
                        data: {
                            "id": this.ARROW_END_ID,
                            "position": { x: 150, y: 150 }
                        }
                    });

                    this._arrowEnd.css({
                        "opacity": 0,
                        'width': 0.0001,
                        'height': 0.0001
                    });                   
                }

                this._arrowEnd.renderedPosition(toPosition);
                toNode = this._arrowEnd;
            }


            if (this._edge) {
                this._edge.remove();
            }

            this._edge = this._cy.add({
                group: "edges",
                data: {
                    id: "edge",
                    source: fromNode.id(),
                    target: toNode.id(),
                    type: 'temporary-link'
                },
                css: $.extend(
                    this._getEdgeCSSByHandle(handle),
                    {opacity: 0.5}
                )
            });

        },
        _clearArrow: function () {
            if (this._edge) {
                this._edge.remove();
                this._edge = null;
            }

            if (this._arrowEnd) {
                this._arrowEnd.remove();
                this._arrowEnd = null;
            }
        },
        _resizeCanvas: function () {
            this._$canvas
                .attr('height', this._$container.height())
                .attr('width', this._$container.width())
                .css({
                    'position': 'absolute',
                    'z-index': '999'
                });
        },
        _mouseDown: function (e) {
            if(this._tagMode){
                return;
            }
            //this._hit = this._hitTestHandles(e);

            if (this._hit) {
                this._lastClick = Date.now();
                this._dragging = this._hover;
                this._hover = null;
                e.stopImmediatePropagation();
            }

        },
        _hideHandles: function () {
            this.permanentHandle = false;
            this._clear();

        },
        _mouseUp: function (e) {
            if (this._hover) {
                if(this._tagMode){
                    if(this._hitTestHandles(e))
                    this._cy.trigger('handletagclick', {
                        nodeId: this._hover.data().id
                    });
                    //this._hover = null;
                } else if (this._hit && this._dragging) {
                    //check if custom listener was passed, if so trigger it and do not add edge
                    var listeners = this._cy._private.listeners;
                    for (var i = 0; i < listeners.length; i++) {
                        if (listeners[i].type === 'addedgemouseup') {
                            this._cy.trigger('addedgemouseup', {
                                source: this._dragging,
                                target: this._hover,
                                edge: this._edge
                            });
                            var that = this;
                            setTimeout(function () {
                                that._dragging = false;
                                that._clearArrow();
                                that._hit = null;
                            }, 0);


                            return;
                        }
                    }

                    var edgeToRemove = this._checkSingleEdge(this._hit.handle, this._dragging);
                    if (edgeToRemove) {
                        this._cy.remove("#" + edgeToRemove.id());
                    }
                    var edge = this._cy.add({
                        data: {
                            source: this._dragging.id(),
                            target: this._hover.id(),
                            type: "default"
                        }
                    });
                    this._initEdgeEvents(edge);
                }
            }
            this._cy.trigger('handlemouseout', {
                node: this._hover
            });
            $("body").css("cursor", "inherit");
            this._dragging = false;
            this._clearArrow();
        },
        _mouseMove: function (e) {
            if (this._hover) {
                if (!this._dragging) {
                    this._hit = this._hitTestHandles(e);
                    if (this._hit) {
                        this._cy.trigger('handlemouseover', {
                            node: this._hover
                        });
                        $("body").css("cursor", "pointer");
                    } else {
                        this._cy.trigger('handlemouseout', {
                            node: this._hover
                        });
                        if(!this._tagMode){
                            this._showHandles(this._hover);
                        }
                        $("body").css("cursor", "inherit");
                    }
                }
            }

            if (this._dragging && this._hit.handle) {
                this._drawArrow(this._dragging, this._getRelativePosition(e), this._hit.handle);
            }

            if (this._nodeClicked) {
                this._clear();
            }
        },
        _mouseOver: function (e) {

            if (this._dragging) {
                if ( (e.cyTarget.id() != this._dragging.id()) && e.cyTarget.data().allowConnection) {
                    this._hover = e.cyTarget;
                }
            } else {
                this._hover = e.cyTarget;
                if (!this._tagMode) {
                    this._showHandles(this._hover);
                }
            }
        },
        _mouseOut: function (e) {
            if(!this._dragging) {
                if (!this.permanentHandle) {
                    this._clear();
                }
                this._cy.trigger('handlemouseout', {
                    node: this._hover
                });
            }
            this._hover = null;
        },
        _removeEdge: function (edge) {
            edge.off("mousedown");
            this._cy.remove("#" + edge.id());
        },
        _initEdgeEvents: function (edge) {
            var self = this;
            edge.on("mousedown", function () {
                if (self.__lastClick && Date.now() - self.__lastClick < self.DOUBLE_CLICK_INTERVAL) {
                    self._removeEdge(this);
                }
                self.__lastClick = Date.now();
            })
        },
        _hitTestHandles: function (e) {
            var mousePoisition = this._getRelativePosition(e);

            //if (this._hover) {
                var position = this._getHandlePosition(this._hover);
                var renderedHandleSize = this.HANDLE_SIZE * this._cy.zoom(); //actual number of pixels that handle uses.
                if (VectorMath.distance(position, mousePoisition) < renderedHandleSize) {
                    var handleType = this._hover.data().handleType;
                    return {
                        handle: this._handles[handleType]
                    };
                }
            //}
        },
        _getHandlePosition: function (target) { //returns the upper left point at which to begin drawing the handle
            var position = target.renderedPosition();
            var width = target.renderedWidth();
            var height = target.renderedHeight();
            var renderedHandleSize = this.HANDLE_SIZE * this._cy.zoom(); //actual number of pixels that handle will use.
            var xpos = position.x + width / 2 - renderedHandleSize;
            var ypos = position.y - height / 2;

            return {x: xpos, y: ypos};
        },
        _getEdgeCSSByHandle: function (handle) {
            var color = handle.lineColor ? handle.lineColor : handle.color;
            return {
                "line-color": color,
                "target-arrow-color": color,
                "line-style": handle.lineStyle? handle.lineStyle: 'solid',
                "width": handle.width? handle.width : 3
            };
        },
        _getRelativePosition: function (e) {
            var containerPosition = this._$container.offset();
            return {
                x: e.pageX - containerPosition.left,
                y: e.pageY - containerPosition.top
            }
        },
        _checkSingleEdge: function (handle, node) {

            if (handle.noMultigraph) {
                var edges = this._cy.edges("[source='" + this._hover.id() + "'][target='" + node.id() + "'],[source='" + node.id() + "'][target='" + this._hover.id() + "']");

                for (var i = 0; i < edges.length; i++) {
                    return edges[i];
                }
            } else {

                if (handle.single == false) {
                    return;
                }
                var edges = this._cy.edges("[source='" + node.id() + "']");

                for (var i = 0; i < edges.length; i++) {
                    if (edges[i].data()["type"] == handle.type) {
                        return edges[i];
                    }
                }
            }
        },
        _redraw: function () {
            this._clear();
            if(this._tagMode) {
                this._cy.trigger('canvasredraw');
            }
        }
    });

})(this);

