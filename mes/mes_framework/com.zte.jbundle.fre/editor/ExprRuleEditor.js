var erns = {};// namespace abbr: Expression Rule Name Space
erns.MSG_MORE_START_NODE = '只能有一个开始节点';
erns.MSG_RULE_DEFINE_ERR = '规则定义错误；流程需要一个开始节点和一个或多个的结束节点；工作节点都处于开始节点和结束节点之间';
erns.MSG_SYNC_TYPE_ERR = '同步节点，同步关系只能为："and"或"or"';
erns.isNum = function(v) {
	return typeof(v) == "number";
}
erns.IN = function() {
	var v = arguments.length > 1 ? arguments[0] : null;
	for (var i = arguments.length - 1; i > 0; i--) {
		if (v == arguments[i])
			return true;
	}
	return false;
}
erns.strVal = function(s) {
	if (s)
		return s;
	else
		return "";
}

erns.between = function(v, v1, v2, dv) {
	if (!dv) {
		dv = 0;
	}
	if (v1 < v2) {
		return v >= v1 - dv && v <= v2 + dv;
	} else {
		return v >= v2 - dv && v <= v1 + dv;
	}
}

erns.distance1 = function(x1, y1, x2, y2) {
	var dx = x1 - x2;
	var dy = y1 - y2;
	return dx * dx + dy * dy;
}

erns.distance2 = function(x, y, p) {
	var dx = x - p.x;
	var dy = y - p.y;
	return dx * dx + dy * dy;
}

erns.drawAnchor = function(ctx, x, y) {
	ctx.strokeRect(x - 4, y - 4, 8, 8);
}

erns.getById = function(array, id) {
	if (!array || !id) {
		return null;
	}
	for (var i = array.length - 1; i > -1; i--) {
		var ret = array[i];
		if (ret.id == id) {
			return ret;
		}
	}
	return null;
}

erns.indexOf = function(array, prop, value) {
	if (!array || !prop || !value) {
		return -1;
	}
	for (var i = array.length - 1; i > -1; i--) {
		if (array[i][prop] == value) {
			return i;
		}
	}
	return -1;
}

erns.getPointIn = function(array, x, y) {
	if (!array || !x || !y) {
		return null;
	}
	for (var i = array.length - 1; i > -1; i--) {
		var ret = array[i];
		if (ret.isPointIn(x, y)) {
			return ret;
		}
	}
	return null;
}

erns.attr = function(o, attr) {
	if (o) {
		return o[attr];
	} else {
		return null;
	}
}

erns.clearArray = function(arr) {
	for (var i = arr.length - 1; i > -1; i--) {
		if (arr[i].div) {
			arr[i].div.parentNode.removeChild(arr[i].div);
			delete arr[i].div;
			arr[i].div = null;
		}
		arr[i] = null;
	}
	arr.length = 0;
}

erns.innerToJson = function(o, array) {
	var type = typeof(o);
	if (o instanceof Array) {
		array.push("[");
		var sep = null;
		for (var i = 0, len = o.length; i < len; i++) {
			if (sep)
				array.push(sep);
			erns.innerToJson(o[i], array);
			sep = ",";
		}
		array.push("]");
	} else if (type == "number") {
		array.push(o);
	} else if (type == "string") {
		array.push("\"");
		for (var i = 0, len = o.length; i < len; i++) {
			var c = o.charAt(i);
			if (c == '\n') {
				array.push("\\n");
			} else if (c == "\"") {
				array.push('\\"');
			} else {
				array.push(c);
			}
		}
		array.push("\"");
	} else if (type == "object") {
		array.push("{");
		var sep = null;
		for (var k in o) {
			var v = o[k];
			if (sep)
				array.push(sep);
			array.push("\"" + k + "\"");
			array.push(":");
			erns.innerToJson(v, array);
			sep = ",";
		}
		array.push("}");
	}
}

erns.toJson = function(o) {
	var ret = [];
	erns.innerToJson(o, ret);
	return ret.join("");
}

erns.createDiv = function() {
	var div = document.createElement("div");
	div.style.position = "absolute";
	div.style.top = "0px";
	div.style.left = "0px";
	div.style.width = "0px";
	div.style.height = "0px";
	div.style.overflow = "hidden";
	div.style.wordBreak = "break-all";
	div.style.whiteSpace = "nowrap";
	div.style.fontSize = "13px";
	div.style.margin = "2px";
	div.style.border = "1px solid black";
	div.style.display = "none";
	div.style.backgroundColor = "rgb(240,240,240)";
	return div;
}
erns.innerText = "innerText";
/** 坐标点类 erns.TPoint */
erns.TPoint = function(x, y) {
	this.x = x;
	this.y = y;
}
erns.TPoint.prototype.update = function(x, y) {
	this.x = x;
	this.y = y;
}

erns.TPoint.prototype.moveBy = function(dx, dy) {
	this.x += dx;
	this.y += dy;
}
// ~ 坐标点类 erns.TPoint

/** 路径类 */
erns.TEdge = function(wfe, x1, y1, x2, y2) {
	var Self = this;
	this.wfe = wfe;
	this.p1 = new erns.TPoint(x1, y1);
	this.p2 = new erns.TPoint(x2, y2);
	this.id = wfe.newId();
	this.arrow;
	this.srcId;
	this.dstId;
	this.expr = "";
	this.order = 1;
	this.iMove = {
		type : 0,
		p1 : new erns.TPoint(0, 0),
		p2 : new erns.TPoint(0, 0)
	};
	this.div = erns.createDiv();
	this.div.style.fontSize = "10px";
	this.div.style.margin = "1px";
	var $div = jQuery(this.div);
	$div.mousedown(function(e) {
		e.shape = Self;
		wfe.mouseDownEvent(e);
	});
	$div.mouseup(function(e) {
		wfe.mouseUpEvent(e);
	});
	$div.mousemove(function(e) {
		wfe.mouseMoveEvent(e);
	});
	$div.dblclick(function(e) {
		e.shape = Self;
		wfe.dblclickEvent(e);
	});
	wfe.$wrapper[0].appendChild(this.div);

	if (!erns.TEdge.$init$) {
		// 提取值对象
		erns.TEdge.prototype.extractValues = function() {
			var arrowX, arrowY;
			if (this.arrow) {
				arrowX = this.arrow.x;
				arrowY = this.arrow.y;
			}
			this.div.style.display = "none";
			return {
				x1 : this.p1.x,
				y1 : this.p1.y,
				x2 : this.p2.x,
				y2 : this.p2.y,
				ax : arrowX,
				ay : arrowY,
				id : this.id,
				srcId : this.srcId,
				dstId : this.dstId,
				expr : this.expr,
				order : this.order
			};
		}

		// 从值对象恢复值
		erns.TEdge.prototype.restoreValues = function(values) {
			this.div.style.display = "none";
			this.p1.update(values.x1, values.y1);
			this.p2.update(values.x2, values.y2);
			this.id = values.id;
			if (values.ax && values.ay) {
				if (this.arrow) {
					this.arrow.x = values.ax;
					this.arrow.y = values.ay;
				} else {
					this.arrow = new erns.TPoint(values.ax, values.ay);
				}
			}
			this.srcId = values.srcId;
			this.dstId = values.dstId;
			this.expr = values.expr;
			this.order = values.order;
		}

		erns.TEdge.prototype.draw = function() {
			var p1 = this.p1;
			var p2 = this.p2;
			var ctx = this.wfe.ctx;
			ctx.moveTo(p1.x, p1.y);
			ctx.lineTo(p2.x, p2.y);
			if (this.expr && this.expr.toString().length > 0) {
				this.div.style.height = "12px";
				this.div.style.width = "14px";
				this.div.innerHTML = this.order;

				var x = Math.round(wfe.border.x + (p1.x + p2.x) / 2) - 10;
				var y = Math.round(wfe.border.y + (p1.y + p2.y) / 2) - 7;
				this.div.style.left = x + "px";
				this.div.style.top = y + "px";

				this.div.style.display = "";
			} else {
				this.div.style.display = "none";
			}

			if (this.arrow) {
				var p2 = this.arrow;
				var a = 0;
				var dx = Math.abs(p2.x - p1.x);
				var dy = Math.abs(p2.y - p1.y);
				if (dx > 0) {
					a = Math.atan2(dy, dx);
				} else {
					a = Math.PI / 2;
				}

				var a1 = a - Math.PI / 8;
				var a2 = a + Math.PI / 8;

				var len = 12;
				var dx1 = len * Math.cos(a1);
				var dy1 = len * Math.sin(a1);
				var dx2 = len * Math.cos(a2);
				var dy2 = len * Math.sin(a2);
				if (p2.x > p1.x) {
					dx1 = -dx1;
					dx2 = -dx2;
				}

				if (p2.y > p1.y) {
					dy1 = -dy1;
					dy2 = -dy2;
				}

				ctx.moveTo(p2.x + dx1, p2.y + dy1);
				ctx.lineTo(p2.x, p2.y);
				ctx.moveTo(p2.x + dx2, p2.y + dy2);
				ctx.lineTo(p2.x, p2.y);
			}
		};

		erns.TEdge.prototype.drawSelected = function() {
			erns.drawAnchor(this.wfe.ctx, this.p1.x, this.p1.y);
			erns.drawAnchor(this.wfe.ctx, this.p2.x, this.p2.y);
		};

		erns.TEdge.prototype.startMove = function(x, y) {
			if (erns.distance2(x, y, this.p2) <= 16) {
				this.iMove.type = 2;
			} else if (erns.distance2(x, y, this.p1) <= 16) {
				this.iMove.type = 1;
			} else {
				this.iMove.type = 3;
			}
		}

		erns.TEdge.prototype.drawMove = function(dx, dy) {
			var wfe = this.wfe;
			var move = this.iMove;
			if (move.type == 1) {
				move.p1.update(this.p1.x + dx, this.p1.y + dy);
				move.p2.update(this.p2.x, this.p2.y);
			} else if (move.type == 2) {
				move.p1.update(this.p2.x + dx, this.p2.y + dy);
				move.p2.update(this.p1.x, this.p1.y);
			} else if (move.type == 3) {
				move.p1.update(this.p2.x + dx, this.p2.y + dy);
				move.p2.update(this.p1.x + dx, this.p1.y + dy);
			}

			wfe.ctx.moveTo(move.p1.x, move.p1.y);
			wfe.ctx.lineTo(move.p2.x, move.p2.y);
			var src = wfe.getPointInNode(move.p1.x, move.p1.y);
			var dst = wfe.getPointInNode(move.p2.x, move.p2.y);
			if (src) {
				src.drawSelected();
			}
			if (dst) {
				dst.drawSelected();
			}
		}

		/** 返回值0失败，1成功无后续操作，2存在相同来源节点的流程 */
		erns.TEdge.prototype.moveBy = function(dx, dy) {
			var oldValues = this.extractValues();
			var updated = false;
			var move = this.iMove;
			var wfe = this.wfe;
			if (move.type == 1) {
				var src = wfe.getPointInNode(this.p1.x + dx, this.p1.y + dy);
				if (src) {
					this.srcId = src.id;
					this.p1.moveBy(dx, dy);
					this.calcArrow(wfe);
					updated = true;
				}
			} else if (move.type == 2) {
				var dst = wfe.getPointInNode(this.p2.x + dx, this.p2.y + dy);
				if (dst) {
					this.dstId = dst.id;
					this.p2.moveBy(dx, dy);
					this.calcArrow(wfe);
					updated = true;
				}
			} else if (move.type == 3) {
				var src = wfe.getPointInNode(this.p1.x + dx, this.p1.y + dy);
				var dst = wfe.getPointInNode(this.p2.x + dx, this.p2.y + dy);
				if (src && dst) {
					this.srcId = src.id;
					this.dstId = dst.id;
					this.p1.moveBy(dx, dy);
					this.p2.moveBy(dx, dy);
					this.calcArrow(wfe);
					updated = true;
				}
			}

			if (updated) {
				var checkRet = this.checkSrcDst();
				if (checkRet == 0) {
					this.restoreValues(oldValues);
				} else if (wfe.getEdgeById(this.id)) {
					var cmd = new erns.TCommand();
					cmd.saveValues(this, oldValues, this.extractValues());
					wfe.cmdAdmin.add(cmd)
				}
				return checkRet;
			} else {
				return 0;
			}
		}

		erns.TEdge.prototype.isPointIn = function(x, y) {
			var x1 = this.p1.x;
			var y1 = this.p1.y;
			var y2 = this.p2.y;
			var x2 = this.p2.x;
			if (!erns.between(x, x1, x2, 2) || !erns.between(y, y1, y2, 2)) {
				return false;
			}

			var dx = x2 - x1;
			var dy = y2 - y1;
			var A = dy;
			var B = -dx;
			var C = dx * y1 - dy * x1;
			// 点到直线距离 d=|Ax0+By0+C|/sqrt(A平方+B平方)
			var d = Math.abs(A * x + B * y + C) / Math.sqrt(A * A + B * B);
			return d <= 4;
		};

		erns.TEdge.prototype.calcX = function(A, B, C, y, inRect, points) {
			if (erns.between(y, this.p1.y, this.p2.y)) {
				var x = (C - B * y) / A;
				if (erns.between(x, this.p1.x, this.p2.x)
						&& inRect.isPointIn(x, y)) {
					points.push(new erns.TPoint(x, y));
				}
			}
		}

		erns.TEdge.prototype.calcY = function(A, B, C, x, inRect, points) {
			if (erns.between(x, this.p1.x, this.p2.x)) {
				var y = (C - A * x) / B;
				if (erns.between(y, this.p1.y, this.p2.y)
						&& inRect.isPointIn(x, y)) {
					points.push(new erns.TPoint(x, y));
				}
			}
		}

		erns.TEdge.prototype.calcMinLenPoint = function(points, ptTarget) {
			if (points.length == 0) {
				return;
			}

			if (points.length == 1) {
				return points[0];
			}

			var len;
			var ret;
			for (var i = points.length - 1; i > -1; i--) {
				var pt = points[i];
				var dx = pt.x - ptTarget.x;
				var dy = pt.y - ptTarget.y;
				var cLen = dx * dx + dy * dy;
				if (!ret || len > cLen) {
					ret = pt;
					len = cLen;
				}
			}
			return ret;
		}

		// 计算箭头位置
		erns.TEdge.prototype.calcArrow = function(ctx) {
			var p1 = this.p1;
			var p2 = this.p2;
			if (!this.srcId || !this.dstId) {
				return;
			}
			var dst = ctx.getNodeById(this.dstId);
			if (!dst) {
				return;
			}

			var dx = p2.x - p1.x;
			var dy = p2.y - p1.y;

			// 转换成直线方程 Ax + By = C
			var A = dy;
			var B = -dx;
			var C = dy * p1.x - dx * p1.y;

			var points = new Array();
			if (A != 0) {
				this.calcX(A, B, C, dst.p.y, dst, points);
				this.calcX(A, B, C, dst.p.y + dst.h, dst, points);
			}

			if (B != 0) {
				this.calcY(A, B, C, dst.p.x, dst, points);
				this.calcY(A, B, C, dst.p.x + dst.w, dst, points);
			}

			this.arrow = this.calcMinLenPoint(points, p1);
		}

		/** 0来源目的相同(不允许),1不存在相同来源,2存在相同来源(提示) */
		erns.TEdge.prototype.checkSrcDst = function() {
			var edgeList = this.wfe.edgeList, edgeCount = 0;
			for (var i = edgeList.length - 1; i > -1; i--) {
				var t = edgeList[i];
				if (t != this && t.srcId == this.srcId) {
					edgeCount++;
					if (t.dstId == this.dstId)
						return 0;
				}
			}

			var dstNode = this.wfe.getNodeById(this.dstId);
			var srcNode = this.wfe.getNodeById(this.srcId);
			if (srcNode.ntype == 2 || dstNode.ntype == 1) {
				return 0;
			} else if (edgeCount > 0) {
				return 2
			} else {
				return 1;
			}
		}

		/** 返回值0失败，1成功无后续操作，2存在相同来源节点的流程 */
		erns.TEdge.prototype.addMe = function() {
			if (!this.srcId || !this.dstId || this.dstId == this.srcId) {
				return 0;
			}

			var checkRet = this.checkSrcDst();
			if (checkRet > 0) {
				wfe.edgeList.push(this);
			}
			return checkRet;
		}
	}
	erns.TEdge.$init$ = true;
}

/** 节点类 */
erns.TNode = function(wfe, x, y, ntype, title) {
	this.wfe = wfe;
	this.p = new erns.TPoint(x, y);
	this.w = 0;
	this.h = 0;
	this.id = wfe.newId();
	this.ntype = ntype;// 0:普通节点,1:开始节点,2:结束节点,3:同步节点
	this.title = title;
	this.roles = "";// 角色
	this.ext = "";// 扩展信息

	this.div = erns.createDiv();
	this.div.style.border = "";
	this.div.style.borderBottom = "1px solid black";
	var $div = jQuery(this.div);
	$div.mousedown(function(e) {
		wfe.mouseDownEvent(e);
	});
	$div.mouseup(function(e) {
		wfe.mouseUpEvent(e);
	});
	$div.mousemove(function(e) {
		wfe.mouseMoveEvent(e);
	});
	$div.dblclick(function(e) {
		wfe.dblclickEvent(e);
	});
	wfe.$wrapper[0].appendChild(this.div);

	this.iMove = {
		type : 0,
		x1 : 0,
		y1 : 0,
		x2 : 0,
		y2 : 0
	};

	if (!erns.TNode.$init$) {
		// 提取值对象
		erns.TNode.prototype.extractValues = function() {
			this.div.style.display = "none";
			return {
				x1 : this.p.x,
				y1 : this.p.y,
				w : this.w,
				h : this.h,
				id : this.id,
				ntype : this.ntype,
				title : this.title,
				roles : this.roles,
				ext : this.ext
			};
		}

		// 从值对象恢复值
		erns.TNode.prototype.restoreValues = function(values) {
			this.div.style.display = "none";
			this.p.update(values.x1, values.y1);
			this.w = values.w;
			this.h = values.h;
			this.id = values.id;
			this.ntype = parseInt(values.ntype);
			if (this.ntype != 0 && this.ntype != 1 && this.ntype != 2) {
				this.ntype = 0;
			}
			this.title = values.title;
			this.roles = values.roles;
			this.ext = values.ext;
		}

		erns.TNode.prototype.draw = function() {
			var ctx = wfe.ctx;
			var p = this.p;
			ctx.fillRect(p.x, p.y, this.w, this.h);
			ctx.strokeRect(p.x, p.y, this.w, this.h);
			if (this.ntype == 0 || this.ntype == 3) {
				this.div[erns.innerText] = this.title;
				this.div.style.display = "block";

				var x = Math.max(p.x, 0), y = Math.max(p.y, 0);
				var w = Math.min(this.w, wfe.border.w - x, p.x + this.w);
				var h = Math.min(17, wfe.border.h - y);
				var border = wfe.border;
				this.div.style.top = (y + border.y + 2) + "px";
				this.div.style.left = (1 + x + border.x) + "px";
				this.div.style.width = (w - 4) + "px";
				this.div.style.height = h + "px";
				if (this.ntype == 3) {
					this.div.style.fontWeight = "bolder";
					this.div.style.color = "navy";
				}
			}
		}

		erns.TNode.prototype.drawEx = function() {
			var ctx = wfe.ctx;
			var p = this.p;
			var cx = this.p.x + this.w / 2, cy = this.p.y + this.h / 2;
			if (this.ntype == 1) {
				ctx.moveTo(cx + 12, cy);
				ctx.lineTo(cx - 8, cy - 10);
				ctx.lineTo(cx - 8, cy + 10);
				ctx.lineTo(cx + 12, cy);
			} else if (this.ntype == 2) {
				ctx.fillRect(cx - 10, cy - 10, 20, 20);
			}
		}

		erns.TNode.prototype.startMove = function(x, y) {
			var moveType = 0;// 平移
			if (erns.distance2(x, y, this.p) <= 16) {
				moveType = 1;
			} else if (erns.distance1(x, y, this.p.x + this.w, this.p.y) <= 16) {
				moveType = 2;
			} else if (erns.distance1(x, y, this.p.x + this.w, this.p.y
					+ this.h) <= 16) {
				moveType = 3;
			} else if (erns.distance1(x, y, this.p.x, this.p.y + this.h) <= 16) {
				moveType = 4;
			} else if (erns.between(y, this.p.y, this.p.y, 3)
					&& erns.between(x, this.p.x, this.p.x + this.w, 3)) {
				moveType = 5;
			} else if (erns.between(x, this.p.x + this.w, this.p.x + this.w, 3)
					&& erns.between(y, this.p.y, this.p.y + this.h, 3)) {
				moveType = 6;
			} else if (erns.between(y, this.p.y + this.h, this.p.y + this.h, 3)
					&& erns.between(x, this.p.x, this.p.x + this.w, 3)) {
				moveType = 7;
			} else if (erns.between(x, this.p.x, this.p.x, 3)
					&& erns.between(y, this.p.y, this.p.y + this.h, 3)) {
				moveType = 8;
			}
			this.iMove.type = moveType;
		}

		erns.TNode.prototype.drawMove = function(dx, dy) {
			this.calcMovedPoints(dx, dy);
			var m = this.iMove;
			wfe.ctx.strokeRect(m.x1, m.y1, m.x2 - m.x1, m.y2 - m.y1);
		}

		erns.TNode.prototype.calcMovedPoints = function(dx, dy) {
			var move = this.iMove;
			move.x1 = this.p.x;
			move.y1 = this.p.y;
			move.x2 = this.p.x + this.w;
			move.y2 = this.p.y + this.h;
			if (move.type == 1) {
				move.x1 += dx;
				move.y1 += dy;
			} else if (move.type == 2) {
				move.y1 += dy;
				move.x2 += dx
			} else if (move.type == 3) {
				move.x2 += dx
				move.y2 += dy;
			} else if (move.type == 4) {
				move.x1 += dx;
				move.y2 += dy;
			} else if (move.type == 5) {
				move.y1 += dy;
			} else if (move.type == 6) {
				move.x2 += dx;
			} else if (move.type == 7) {
				move.y2 += dy;
			} else if (move.type == 8) {
				move.x1 += dx
			} else {
				move.x1 += dx;
				move.y1 += dy;
				move.x2 += dx;
				move.y2 += dy;
			}
		}

		erns.TNode.prototype.moveBy = function(dx, dy) {
			this.calcMovedPoints(dx, dy);
			var wfe = this.wfe;
			var move = this.iMove;
			var x1 = Math.min(move.x1, move.x2);
			var y1 = Math.min(move.y1, move.y2);
			var x2 = Math.max(move.x1, move.x2);
			var y2 = Math.max(move.y1, move.y2);
			var fx = x1 - this.p.x;
			var fy = y1 - this.p.y;
			var isNew = wfe.getNodeById(this.id) ? false : true;

			var cmd = new erns.TCommand();
			var oldValues = this.extractValues();
			if (this.ntype == 0 || this.ntype == 3) {
				this.p.update(x1, y1);
				this.w = x2 - x1;
				this.h = y2 - y1;
			} else if (isNew || move.type == 0) {
				this.p.update(x1, y1);
				this.w = 40;
				this.h = 40;
				if (x1 == oldValues.x1 && y1 == oldValues.y1)
					return;
			} else {
				return;
			}
			cmd.saveValues(this, oldValues, this.extractValues());

			for (var i = wfe.edgeList.length - 1; i > -1; i--) {
				var t = wfe.edgeList[i];
				if (t.dstId == this.id) {
					oldValues = t.extractValues();

					t.p2.moveBy(fx, fy);
					if (!this.isPointIn(t.p2.x, t.p2.y)) {
						t.p2.x = Math.round((x1 + x2) / 2);
						t.p2.y = Math.round((y1 + y2) / 2);
					}
					t.calcArrow(wfe);

					cmd.saveValues(t, oldValues, t.extractValues());
				} else if (t.srcId == this.id) {
					oldValues = t.extractValues();

					t.p1.moveBy(fx, fy);
					if (!this.isPointIn(t.p1.x, t.p1.y)) {
						t.p1.x = Math.round((x1 + x2) / 2);
						t.p1.y = Math.round((y1 + y2) / 2);
					}
					t.calcArrow(wfe);

					cmd.saveValues(t, oldValues, t.extractValues());
				}
			}

			if (!isNew) {
				wfe.cmdAdmin.add(cmd);
			}
			return 1;
		}

		erns.TNode.prototype.drawSelected = function() {
			var ctx = wfe.ctx;
			erns.drawAnchor(ctx, this.p.x, this.p.y);
			erns.drawAnchor(ctx, this.p.x + this.w, this.p.y);
			erns.drawAnchor(ctx, this.p.x, this.p.y + this.h);
			erns.drawAnchor(ctx, this.p.x + this.w, this.p.y + this.h);
		}

		erns.TNode.prototype.isPointIn = function(x, y) {
			var x1 = this.p.x;
			var y1 = this.p.y;
			var x2 = x1 + this.w;
			var y2 = y1 + this.h;
			return erns.between(x, x1, x2, 3) && erns.between(y, y1, y2, 3);
		}

		erns.TNode.prototype.addMe = function() {
			if (this.ntype == 1 && erns.indexOf(wfe.nodeList, "ntype", 1) > -1) {
				alert(erns.MSG_MORE_START_NODE);
				return 0;
			}
			wfe.nodeList.push(this);
			return 1;
		}

	}

	erns.TNode.$init$ = true;
}

/** 鼠标轨迹信息 */
erns.TMouseData = function(x, y) {
	this.ox = x;
	this.oy = y;
	this.dx = 0;
	this.dy = 0;
	this.ing = false;

	this.start = function(x, y) {
		this.ox = x;
		this.oy = y;
		this.dx = 0;
		this.dy = 0;
		this.ing = true;
	}

	this.move = function(newX, newY) {
		this.dx = newX - this.ox;
		this.dy = newY - this.oy;
	}

	this.changed = function() {
		return Math.abs(this.dx) > 1 || Math.abs(this.dy) > 1;
	}

	this.done = function() {
		this.ing = false;
	}
}

// 命令指令，包含多个命令数据
erns.TCommand = function() {
	this.datas = new Array();// 多个CmdData
}

erns.TCommand.prototype.saveValues = function(target, oldVal, newVal) {
	var data = {};
	data.target = target;
	data.oldVal = oldVal;
	data.newVal = newVal;
	this.datas.push(data);
}

erns.TCommandAdmin = function(wfe) {
	this.wfe = wfe;
	this.index = -1;
	this.commands = new Array();
}

erns.TCommandAdmin.prototype.clear = function(command) {
	erns.clearArray(this.commands);
	this.index = -1;
}

erns.TCommandAdmin.prototype.add = function(command) {
	this.index++;
	this.commands[this.index] = command;
	this.commands.length = this.index + 1;
}

erns.TCommandAdmin.prototype.redo = function() {
	var index = this.index + 1;
	if (index < 0 || index >= this.commands.length) {
		return;
	}

	var datas = this.commands[index].datas;
	var nodeList = this.wfe.nodeList;
	var edgeList = this.wfe.edgeList;
	for (var i = datas.length - 1; i > -1; i--) {
		var cmdData = datas[i];
		if (cmdData.oldVal && cmdData.newVal) {// update
			cmdData.target.restoreValues(cmdData.newVal);
		} else if (cmdData.oldVal) {
			cmdData.target.restoreValues(cmdData.oldVal);
			if (cmdData.target instanceof erns.TNode) {
				for (var k = nodeList.length - 1; k > -1; k--) {
					if (nodeList[k] == cmdData.target) {
						nodeList.splice(k, 1);
					}
				}
			} else {
				for (var k = edgeList.length - 1; k > -1; k--) {
					if (edgeList[k] == cmdData.target) {
						edgeList.splice(k, 1);
					}
				}
			}
		} else if (cmdData.newVal) {
			cmdData.target.restoreValues(cmdData.newVal);
			if (cmdData.target instanceof erns.TNode) {
				nodeList.push(cmdData.target);
			} else {
				edgeList.push(cmdData.target);
			}
		}
	}

	this.index = index;
}

erns.TCommandAdmin.prototype.undo = function() {
	if (this.index < 0 || this.index >= this.commands.length) {
		return;
	}

	var datas = this.commands[this.index].datas;
	var nodeList = this.wfe.nodeList;
	var edgeList = this.wfe.edgeList;
	for (var i = datas.length - 1; i > -1; i--) {
		var cmdData = datas[i];
		if (cmdData.oldVal && cmdData.newVal) {// update
			cmdData.target.restoreValues(cmdData.oldVal);
		} else if (cmdData.oldVal) {
			cmdData.target.restoreValues(cmdData.oldVal);
			if (cmdData.target instanceof erns.TNode) {
				nodeList.push(cmdData.target);
			} else {
				edgeList.push(cmdData.target);
			}
		} else if (cmdData.newVal) {
			cmdData.target.restoreValues(cmdData.newVal);
			if (cmdData.target instanceof erns.TNode) {
				for (var k = nodeList.length - 1; k > -1; k--) {
					if (nodeList[k] == cmdData.target) {
						nodeList.splice(k, 1);
					}
				}
			} else {
				for (var k = edgeList.length - 1; k > -1; k--) {
					if (edgeList[k] == cmdData.target) {
						edgeList.splice(k, 1);
					}
				}
			}
		}
	}

	this.index--;
}

erns.TWfeStatus = function(wfe) {
	this.wfe = wfe;
	this.i = 0; // 状态码：0:浏览模式 1:绘制流程 2:绘制节点
	this.drawing;// 绘制流程、节点时的对象
	this.params = {};// 上下文信息不同模式自己使用

	this.isBrowse = function() {
		return this.i == 0;
	}

	this.setBrowse = function() {
		this.i = 0;
		this.drawing = null;
	}

	this.isDrawEdge = function() {
		return this.i == 1;
	}

	this.setDrawEdge = function() {
		this.i = 1;
	}

	this.isDrawNode = function() {
		return this.i == 2;
	};

	this.setDrawNode = function(ntype, title, roles, ext) {
		this.i = 2;
		this.params.ntype = ntype;
		this.params.title = title;
		this.params.roles = roles;
		this.params.ext = ext;
	}

	/* 根据状态和当前鼠标位置，计算出drawing对象 */
	this.calcDrawing = function(x, y) {
		this.drawing = null;
		if (this.isDrawEdge()) {
			var srcNode = this.wfe.getPointInNode(x, y);
			if (srcNode) {
				this.drawing = new erns.TEdge(this.wfe, x, y, x, y);
				this.drawing.srcId = srcNode.id;
			}
		} else if (this.isDrawNode()) {
			this.drawing = new erns.TNode(this.wfe, x, y, this.params.ntype,
					this.params.title);
			this.drawing.roles = this.params.roles;
			this.drawing.ext = this.params.ext;
		}
	}

}

/**
 * 节点流编辑器主类;<br>
 * 1)canvasId为canvas控件Id;<br>
 * 2)wraperId为包围canvas控件的外部控件<br>
 * 3)TExprRuleEditor会把canvas充满wraper<br>
 * 
 * 如HTML代码为： <div id="div"> <canvas id="canvas"/></div> <br>
 * 调用js代码为：var wfe = new erns.TExprRuleEditor("canvas", "div");;
 * 
 */
erns.TExprRuleEditor = function(canvasId, wraperId) {
	var Self = this;// 当前实例引用

	this.edgeList = new Array();// 流程列表
	this.nodeList = new Array();// 节点列表

	this.$wrapper = jQuery("#" + wraperId);
	erns.innerText = this.$wrapper[0].textContent ? "textContent" : "innerText";
	var $canvas = jQuery("#" + canvasId);
	this.border = {
		x : 0,// 位于HTML中的offsetLeft
		y : 0,// 位于HTML中的offsetTop
		sx : 0,// 位于HTML中的scrollLeft
		sy : 0,// 位于HTML中的scrollTop
		w : 0,
		h : 0
	};

	this.calcBorder = function(calcSize) {
		if (calcSize) {
			this.border.w = this.$wrapper.width();
			this.border.h = this.$wrapper.height();
			$canvas[0].width = Self.border.w;
			$canvas[0].height = Self.border.h;
		}
		with (this.$wrapper.offset()) {
			this.border.x = left;
			this.border.y = top;
		}
		this.border.sx = 0;
		this.border.sy = 0;

		var parent = this.$wrapper[0].parentNode;
		while (parent && erns.isNum(parent.scrollLeft)
				&& erns.isNum(parent.scrollTop)) {
			this.border.sx += parent.scrollLeft;
			this.border.sy += parent.scrollTop;
			parent = parent.parentNode;
		}
	};

	Self.calcBorder(true);
	this.$wrapper.resize(function(e) {
		Self.calcBorder(true);
		drawAll();
	});

	$canvas.css("cursor", "pointer");
	$canvas.mousedown(function(e) {
		Self.mouseDownEvent(e);
	});
	$canvas.mouseup(function(e) {
		Self.mouseUpEvent(e);
	});
	$canvas.mousemove(function(e) {
		Self.mouseMoveEvent(e);
	});
	$canvas.dblclick(function(e) {
		Self.dblclickEvent(e);
	});

	this.ctx = $canvas[0].getContext("2d");// 展示画布
	this.ctx.font = "14px Arial";

	var selected;
	var iMouse = new erns.TMouseData(0, 0);// 鼠标按移动偏移量

	/** 编辑器状态 */
	this.wfeStatus = new erns.TWfeStatus(this);
	/** 撤销重做管理器 */
	this.cmdAdmin = new erns.TCommandAdmin(this);
	/** id生成种子 */
	this.seedId = 1;
	this.newId = function() {
		return this.seedId++;
	}

	// API 状态->添加工作节点
	this._addNode_ = function(ntype, title, roles, ext) {
		roles = erns.strVal(roles);
		ext = erns.strVal(ext);
		this.wfeStatus.setDrawNode(ntype, title, roles, ext);
		selected = null;
		$canvas.focus();
		drawAll();
	}
	// API 状态->添加工作节点
	this.addWorkNode = function(title, roles, ext) {
		this._addNode_(0, title, roles, ext);
	}

	// API 状态->添加开始节点
	this.addStartNode = function() {
		this._addNode_(1, "<<start>>", "", "");
	}

	// API 状态->添加结束节点
	this.addEndNode = function() {
		this._addNode_(2, "<<end>>", "", "");
	}

	// API 状态->添加同步节点
	this.addSyncNode = function() {
		this._addNode_(3, "<<AND>>", "", "and");
	}

	// API 状态->添加流程
	this.addEdge = function() {
		this.wfeStatus.setDrawEdge();
	}

	// API 删除选中对象
	this.deleteSelected = function() {
		if (!selected) {
			return;
		}

		var cmd = new erns.TCommand();
		if (selected instanceof erns.TNode) {
			for (var i = this.nodeList.length - 1; i > -1; i--) {
				if (selected == this.nodeList[i]) {
					cmd.saveValues(selected, selected.extractValues(), null);
					this.nodeList.splice(i, 1);
					break;
				}
			}
			for (var i = this.edgeList.length - 1; i > -1; i--) {
				var edge = this.edgeList[i];
				if (selected.id == edge.srcId || selected.id == edge.dstId) {
					cmd.saveValues(edge, edge.extractValues(), null);
					this.edgeList.splice(i, 1);
				}
			}

		}

		if (selected instanceof erns.TEdge) {
			for (var i = this.edgeList.length - 1; i > -1; i--) {
				if (selected == this.edgeList[i]) {
					cmd.saveValues(selected, selected.extractValues(), null);
					this.edgeList.splice(i, 1);
					break;
				}
			}
		}

		Self.cmdAdmin.add(cmd);
		selected = null;
		drawAll();
	}

	// API
	this.redo = function() {
		this.cmdAdmin.redo(this);
		selected = null;
		drawAll();
	}

	// API
	this.undo = function() {
		this.cmdAdmin.undo(this);
		selected = null;
		drawAll();
	}

	// API保存编辑内容，返回json串
	this.save = function() {
		var nodes = this.nodeList, edges = this.edgeList;
		var istart = 0
		for (var i = nodes.length - 1; i > -1; i--) {
			var n = nodes[i];
			var inputs = 0, outputs = 0;
			for (var j = edges.length - 1; j > -1; j--) {
				if (edges[j].srcId == n.id) {
					outputs++;
				}
				if (edges[j].dstId == n.id) {
					inputs++;
				}
			}
			// start
			if (inputs == 0 && (++istart > 1 || n.ntype != 1)) {
				alert(erns.MSG_RULE_DEFINE_ERR);
				return;
			}
			if (outputs == 0 && n.ntype != 2) {
				alert(erns.MSG_RULE_DEFINE_ERR);
				return;
			}
		}

		var ret = {
			seedId : this.seedId,
			nodes : new Array(),
			edges : new Array()
		};

		for (var i = 0, len = nodes.length; i < len; i++) {
			ret.nodes.push(nodes[i].extractValues());
		}

		for (var i = 0, len = edges.length; i < len; i++) {
			ret.edges.push(edges[i].extractValues());
		}
		drawAll();
		return erns.toJson(ret);
	}

	this.load = function(json) {
		var ret = eval("(" + json + ")");
		if (typeof(ret.seedId) != 'number' || !(ret.nodes instanceof Array)
				|| !(ret.edges instanceof Array)) {
			return;
		}

		var edgeList = new Array();
		var nodeList = new Array();
		for (var i = 0, len = ret.edges.length; i < len; i++) {
			var edge = new erns.TEdge(this, 0, 0, 0, 0);
			edge.restoreValues(ret.edges[i]);
			edgeList.push(edge);
		}
		for (var i = 0, len = ret.nodes.length; i < len; i++) {
			var node = new erns.TNode(this, 0, 0, 0, 0);
			node.restoreValues(ret.nodes[i]);
			nodeList.push(node);
		}

		erns.clearArray(this.nodeList);
		erns.clearArray(this.edgeList);

		this.seedId = ret.seedId;
		this.nodeList = nodeList;
		this.edgeList = edgeList;
		selected = null;

		this.calcBorder();
		this.wfeStatus.setBrowse();
		this.cmdAdmin.clear();
		drawAll();
	}

	// API Event 编辑流程事件, edgeId:流程ID,srcNode:来源节点
	// dstNode:目的节点,expr:表达式,order:顺序
	this.onEditingEdge = function(edgeId, srcNode, dstNode, expr, order) {
	}

	// API 编辑流程，edgeId:流程ID,expr:表达式,order:顺序
	this.updateEdge = function(edgeId, expr, order) {
		var edge = this.getEdgeById(edgeId);
		if (!edge || (edge.expr == expr && edge.order == order)) {
			return;
		}

		var cmd = new erns.TCommand();
		var oldValues = edge.extractValues();
		edge.expr = expr + "";
		if (!isNaN(order)) {
			edge.order = parseInt(order);
		}
		cmd.saveValues(edge, oldValues, edge.extractValues());
		this.cmdAdmin.add(cmd);
		drawAll();
	}

	// API Event 编辑节点事件, nodeId:节点ID,title:标题,roles:角色,ext: 扩展信息
	this.onEditingNode = function(nodeId, ntype, title, roles, ext) {
	}

	// API编辑节点，nodeId:节点ID,title:标题
	this.updateNode = function(nodeId, title, roles, ext) {
		var node = this.getNodeById(nodeId);
		if (!node) {
			return;
		}
		title = erns.strVal(title);
		roles = erns.strVal(roles);
		ext = erns.strVal(ext);
		if ((node.ntype == 0 && node.title == title && roles == node.roles && node.ext == ext)
				|| (node.ntype != 0 && node.ext == ext)) {
			return;
		}

		if (node.ntype == 3) {
			if (!erns.IN(ext, "and", "or")) {
				alert(erns.MSG_SYNC_TYPE_ERR);
				return;
			}
			node.title = "<<" + ext.toUpperCase() + ">>";
		}

		var cmd = new erns.TCommand();
		var oldValues = node.extractValues();
		if (node.ntype == 0) {
			node.title = title;
			node.roles = roles;
		}
		node.ext = ext;
		cmd.saveValues(node, oldValues, node.extractValues());
		this.cmdAdmin.add(cmd);
		drawAll();
	}

	// 发起编辑流程事件
	this.fireEditingEdgeEvent = function(edge) {
		var src = this.getNodeById(edge.srcId);
		var dst = this.getNodeById(edge.dstId);
		if (src && dst && this.onEditingEdge) {
			var node1 = {
				title : src.title,
				roles : src.roles,
				ext : src.ext
			};
			var node2 = {
				title : dst.title,
				roles : dst.roles,
				ext : dst.ext
			};
			this.onEditingEdge(edge.id, node1, node2, edge.expr, edge.order);
		}
	}

	// 发起编辑节点事件
	this.fireEditingNodeEvent = function(n) {
		if (n && n.ntype != 1 && this.onEditingNode) {
			this.onEditingNode(n.id, n.ntype, n.title, n.roles, n.ext);
		}
	}

	this.getNodeById = function(id) {
		return erns.getById(this.nodeList, id);
	}

	this.getEdgeById = function(id) {
		return erns.getById(this.edgeList, id);
	}

	this.getPointInNode = function(x, y) {
		return erns.getPointIn(Self.nodeList, x, y);
	}

	this.getPtEdge = function(x, y) {
		return erns.getPointIn(Self.edgeList, x, y);
	}

	this.dblclickEvent = function(event) {
		event.preventDefault();
		event.stopPropagation();

		this.calcBorder();
		var x = event.clientX - this.border.x + this.border.sx;
		var y = event.clientY - this.border.y + this.border.sy;

		selected = event.shape || this.getPtEdge(x, y)
				|| this.getPointInNode(x, y);
		if (selected) {
			if (selected instanceof erns.TEdge) {
				this.fireEditingEdgeEvent(selected);
			} else {
				this.fireEditingNodeEvent(selected);
			}
		}
	}

	this.mouseDownEvent = function(event) {
		event.preventDefault();
		event.stopPropagation();
		if (event.which != 1) {
			return;// 鼠标左键
		}

		this.calcBorder();
		var x = event.clientX - this.border.x + this.border.sx;
		var y = event.clientY - this.border.y + this.border.sy;

		iMouse.start(x, y);
		selected = null;
		if (this.wfeStatus.isBrowse()) {
			selected = event.shape || this.getPtEdge(x, y)
					|| this.getPointInNode(x, y);
			if (selected) {
				selected.startMove(x, y);
				drawAll();
			}
		} else {
			this.wfeStatus.calcDrawing(x, y);
			if (this.wfeStatus.drawing) {
				this.wfeStatus.drawing.startMove(x, y);
				drawAll();
			}
		}
	}

	this.mouseMoveEvent = function(event) {
		event.preventDefault();
		event.stopPropagation();
		if (!iMouse.ing) {
			return;
		}

		var x = event.clientX - this.border.x + this.border.sx;
		var y = event.clientY - this.border.y + this.border.sy;
		iMouse.move(x, y);
		drawAll();
	}

	this.mouseUpEvent = function(event) {
		var wfeStatus = this.wfeStatus;
		if (event.which != 1) {
			wfeStatus.setBrowse();
			iMouse.done();
			drawAll();
			return;
		}
		if (!iMouse.ing) {
			return;
		}
		event.preventDefault();
		event.stopPropagation();

		var editingEdge = false;
		var x = event.clientX - this.border.x + this.border.sx;
		var y = event.clientY - this.border.y + this.border.sy;
		iMouse.move(x, y);
		if (iMouse.changed()) {
			var drawing = wfeStatus.drawing;
			if (selected) {
				editingEdge = selected.moveBy(iMouse.dx, iMouse.dy) == 2;
			} else if (drawing) {
				drawing.moveBy(iMouse.dx, iMouse.dy);
				var iAdd = drawing.addMe();
				if (iAdd > 0) {
					selected = drawing;
					var cmd = new erns.TCommand();
					cmd.saveValues(drawing, null, drawing.extractValues());
					this.cmdAdmin.add(cmd);
				}
				editingEdge = iAdd == 2;
				wfeStatus.setBrowse();
			} else {
				selected = null;
			}
			drawingEdge = null;

		}
		iMouse.done();
		drawAll();

		if (editingEdge) {
			this.fireEditingEdgeEvent(selected);
		}
	}

	function drawAll() {
		var ctx = Self.ctx;

		ctx.clearRect(0, 0, Self.border.w, Self.border.h);
		ctx.beginPath();
		ctx.fillStyle = "RGB(255,255,255)";
		ctx.strokeStyle = "black";
		var drawingEdgeLine = null;
		for (var i = 0; i < Self.edgeList.length; i++) {
			Self.edgeList[i].draw();
		}
		ctx.stroke();
		ctx.closePath();

		for (var i = 0; i < Self.nodeList.length; i++) {
			Self.nodeList[i].draw();
		}

		ctx.fillStyle = "green";
		ctx.beginPath();
		for (var i = 0; i < Self.nodeList.length; i++) {
			if (Self.nodeList[i].ntype == 1)
				Self.nodeList[i].drawEx();
		}
		ctx.fill();
		ctx.closePath();

		ctx.fillStyle = "red";
		for (var i = 0; i < Self.nodeList.length; i++) {
			if (Self.nodeList[i].ntype == 2)
				Self.nodeList[i].drawEx();
		}

		if (iMouse.ing || selected) {
			ctx.beginPath();
			ctx.strokeStyle = "rgb(200,200,0)";
			if (selected)
				selected.drawSelected();

			if (iMouse.ing) {
				if (Self.wfeStatus.drawing)
					Self.wfeStatus.drawing.drawMove(iMouse.dx, iMouse.dy);
				if (selected)
					selected.drawMove(iMouse.dx, iMouse.dy);
			}
			ctx.stroke();
			ctx.closePath();
		}
	}
}