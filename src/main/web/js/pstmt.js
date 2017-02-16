
function PStmt(varNames, varValues, varDependencies, tableElement) {
	this._names = varNames;
	this._permutations = varValues;
	this._deps = varDependencies;
	this._table = tableElement;
	
	this._pvar = [];
	this._selPermIndex = 0;
	
	this._initialized = false;
	
	this.createPVars();
	this.recalculateAllPossibleValues(-1);
	this.createControlTable();
	this.createSliders();

	this._initialized = true;
	this.refreshControlTable();
}

PStmt.prototype.createPVars = function() {
	for (var varIndex = 0; varIndex < this._names.length; varIndex++) // Number of variables
		this._pvar[varIndex] = new PVar(varIndex, this._permutations[0][varIndex], this._deps[varIndex].find(-1).length == 0);
}

PStmt.prototype.recalculateAllPossibleValues = function(changedVarIndex) {
	var recalculation = [];
	for (var varIndex = 0; varIndex < this._names.length; varIndex++) // Number of variables
		recalculation[varIndex] = this.findPossibleValues(varIndex, changedVarIndex);
	
	for (var varIndex = 0; varIndex < this._names.length; varIndex++) // Number of variables
		this._pvar[varIndex].setPossibleValues(recalculation[varIndex]);
}

PStmt.prototype.findPossibleValues = function(varIndex, changedVarIndex) {
	if (changedVarIndex != -1 && this._deps[varIndex].find(changedVarIndex) == 0) 
		return this._pvar[varIndex].getPossibleValues(varIndex);
	
	var result = [];
	for (var jobIndex = 0; jobIndex < this._permutations.length; jobIndex++) {    // Number of jobs
		var perm = this._permutations[jobIndex];
		var val  = perm[varIndex];
		if (this.permSatisfiesDependencies(perm, this._deps[varIndex]) && result.find(val).length == 0) 
			result[result.length] = val; 
	}
	
	if (result.find(this._pvar[varIndex].getValue()).length == 0) 
		this._pvar[varIndex].setValue(result[0]);
	
	return result;
}

PStmt.prototype.permSatisfiesDependencies = function(perm, varDeps) {
	if (varDeps.find(-1)) return true;
	for (var index = 0; index < varDeps.length; index++) {    // Number of dependencies
		var depVarIndex = varDeps[index];
		if (perm[depVarIndex] != this._pvar[depVarIndex].getValue()) 
			return false;
	}
	
	return true;
}

PStmt.prototype.createControlTable = function() {
	var result = "<tr class='a'><th align='left'>Variable</th><th align='left'>Value</th></tr>";
	result += "<tr class='b'><td><label for='varSYSTEM_JOB_NUM'>Job Number:</label></td><td id='job-number'>" + (this._selPermIndex + 1) + "</td>";
	
	var rowOdd = true;
	for (var varIndex = 0; varIndex < this._names.length; varIndex++) { // Number of variables
		result += this.createVariableRowContent(varIndex, rowOdd ? "a" : "b");
		rowOdd = !rowOdd;
	}

	this._table.innerHTML = result;
}

PStmt.prototype.createVariableRowContent = function(varIndex, rowClass) {
	var result = "<tr class='" + rowClass + "'><td><label for='var" + varIndex + "'>" + this._names[varIndex] + ":</label>";
	
	result += "<div class='slider' ";
	if (!this._pvar[varIndex].isPermutable()) 
		result += "style='display:none;'";
	result += " id='slider-" + varIndex + "' tabIndex='" + varIndex + "'>";
	result += "<input class='slider-input' id='slider-input-" + varIndex + "' />";
	result += "</div>";

	result += "</td><td id='slider-value-" + varIndex + "'>" +
			"<img id='" + this._pvar[varIndex].getValFieldId("img") + "' src=''/>" +
			"<div id='" + this._pvar[varIndex].getValFieldId("txt") + "'>" + this._pvar[varIndex].getValue() + "</div></td></tr>";
	return result;
}

Slider.prototype.saveVarIndex = function(varIndex) {
	this._varIndex = varIndex;
}

PStmt.prototype.createSliders = function() {
	var oThis = this;
	for (var varIndex = 0; varIndex < this._names.length; varIndex++) { // Number of variables
		var slider = new Slider(document.getElementById("slider-" + varIndex), document.getElementById("slider-input-" + varIndex));
		this._pvar[varIndex].setSlider(slider);
		slider.setUnitIncrement(1);
		slider.setBlockIncrement(1);
		slider.saveVarIndex(varIndex);
		slider.onchange = function () {
			oThis.recalculateAllPossibleValues(this._varIndex);
			var possibleValues = oThis._pvar[this._varIndex].getPossibleValues();
			oThis._pvar[this._varIndex].setValue(possibleValues[this.getValue()]);
			oThis.refreshControlTable();
		};

		slider.setMaximum(this._pvar[varIndex].getPossibleValues().length - 1);
	}
}

PStmt.prototype.refreshControlTable = function() {
	if (!this._initialized) return;

	this._selPermIndex = this.findSelectedPermutationIndex();
	var perm = this._permutations[this._selPermIndex];
	document.getElementById("job-number").innerHTML = this._selPermIndex + 1;
	for (var varIndex = 0; varIndex < this._names.length; varIndex++) { // Number of variables
		var permVarVal = perm[varIndex];
		this._pvar[varIndex].setValue(permVarVal);
		this._pvar[varIndex].getSlider().setMaximum(this._pvar[varIndex].getPossibleValues().length - 1);
		
		var imgSource = (permVarVal.match("^(/[_a-zA-Z0-9.-]+)*/[_a-zA-Z0-9.-]+\.(gif|jpg|jpeg|png)$") == null) ? 
				"" : "/imageServ?filename=" + permVarVal;
		
		document.getElementById(this._pvar[varIndex].getValFieldId("img")).src = imgSource;
		document.getElementById(this._pvar[varIndex].getValFieldId("txt")).innerHTML = permVarVal;
	}
}

PStmt.prototype.findSelectedPermutationIndex = function() {
	for (var permIndex = 0; permIndex < this._permutations.length; permIndex++) { // Number of variables
		var perm = this._permutations[permIndex];
		for (var varIndex = 0; varIndex < this._names.length; varIndex++) { // Number of variables
			if (this._pvar[varIndex].isPermutable() && this._pvar[varIndex].getValue() != perm[varIndex])
				break;
			if (varIndex == this._names.length - 1)
				return permIndex;
		}
	}
	return 0;
}


