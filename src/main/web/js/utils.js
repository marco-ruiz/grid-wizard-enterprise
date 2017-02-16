function toggle(groupName) {
	var title = document.getElementById(groupName + "Title");
	var ele = document.getElementById(groupName + "Div");
	var dots = document.getElementById(groupName + "Dots");
	if(ele.style.display == "block" || ele.style.display == "") {
		title.className = "closed";
		ele.style.display = "none";
		dots.style.display = "";
	} else {
		title.className = "opened";
		ele.style.display = "block";
		dots.style.display = "none";
	}
} 
