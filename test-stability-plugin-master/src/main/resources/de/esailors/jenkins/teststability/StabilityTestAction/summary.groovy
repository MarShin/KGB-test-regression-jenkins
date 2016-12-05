raw("<div><script src='https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js'></script>" +
		"<script src='${rootURL}/plugin/test-stability/hidefilteredtest.js'></script><script src='https://code.highcharts.com/highcharts.js'></script>" + "<div id='filter-data' hidden='true'>" + my.hiddenTestsString + "</div><div id='chart-data' hidden='true'>" + my.dataJson + 
		"</div><div id='chart' style='min-width: 310px; height: 400px; margin: 0 auto'></div><script src='${rootURL}/plugin/test-stability/historygraph.js'></script>" + 
		"<img src='${rootURL}${my.bigImagePath}'/> <span onmouseout='document.getElementById(\"test-stability-popup\").style.visibility = \"hidden\"' " +
		"onmouseover='document.getElementById(\"test-stability-popup\").style.visibility = \"visible\"'>${my.description}" +
		"<div id='test-stability-popup' style='visibility:hidden;position:relative;top:0px;left:35px;width:100%;opacity:1.0;z-index:100;background:#F5F5DC;'>" + 
		"Flakiest Child: " + my.nameOfFlakiestChild + (my.flakinessOfFlakiestChild < 0 ? "" : (" - Flakiness: " + my.flakinessOfFlakiestChild)) + 
		"<br/> Least Stable Child: " + my.nameOfLeastStableChild + (my.stabilityOfLeastStableChild < 0 ? "" : (" - Stablility: " + my.stabilityOfLeastStableChild)) +
		"</div></span> </div>" +
		(my.stackTrace == "" ? "" : "<h3> Stack Trace of Last Failure </h3>") +
		"<div>" + my.stackTrace + "</div>" + "<div id='disqus_thread'></div><script src='${rootURL}/plugin/test-stability/disqus.js'></script>") 