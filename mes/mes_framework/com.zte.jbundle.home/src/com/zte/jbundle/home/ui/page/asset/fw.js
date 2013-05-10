function ajaxTag() {	
	return new Date().getTime();
}

function format(s, o) {
	var ret = [];
	for (var start = 0; 1 == 1;) {
		var i = s.indexOf("${", start);
		if (i < 0) {
			ret.push(s.substring(start, s.length));
			break;
		}
		ret.push(s.substring(start, i));

		var e = s.indexOf("}", i + 2);
		if (e < 0) {
			return s;
		}

		var arg = s.substring(i + 2, e);
		arg = o[arg];
		if (arg) {
			ret.push(arg);
		}
		start = e + 1;
	}
	return ret.join("");
}

function adjustJbTbDiv() {
	var tbDiv = $(".jb_tbDiv");
	var tbTitle = $(".jb_tbTitle");
	tbDiv.width(tbTitle.width() - 1);
	$(".jb_lay_menu").height($(".jb_lay_page").height());
}