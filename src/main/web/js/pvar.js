
Array.prototype.find = function(searchStr) {
	var returnArray = [];
	for (i=0; i<this.length; i++) {
		if (typeof(searchStr) == 'function') {
			if (searchStr.test(this[i])) returnArray.push(i);
		} else {
			if (this[i]===searchStr) returnArray.push(i);
		}
	}
	return returnArray;
}

function PVar(varIndex, initialValue, permutable) {
	this._varIndex = varIndex;
	this._permutable = permutable;
	this._value = initialValue;
	this._slider;
	this._allPossibleValues;
}

PVar.prototype.setSlider = function (v) {
	this._slider = v;
};

PVar.prototype.getSlider = function () {
	return this._slider;
};

PVar.prototype.setPossibleValues = function (v) {
	this._allPossibleValues = v;
};

PVar.prototype.getPossibleValues = function () {
	return this._allPossibleValues;
};

PVar.prototype.setPermutable = function (v) {
	this._permutable = v;
};

PVar.prototype.isPermutable = function () {
	return this._permutable;
};

PVar.prototype.setValue = function (v) {
	this._value = v;
};

PVar.prototype.getValue = function () {
	return this._value;
};

PVar.prototype.getValFieldId = function (type) {
	return type + "-varValue-" + this._varIndex;
};
