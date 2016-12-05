var parseJson = function () {
    var data = $.parseJSON($('#chart-data').html());
    for (var i = 0; i < data.length; i++) {
        data[i]["x"] = "Build #" + data[i]["build_number"];
        data[i]["name"] = data[i]["status"];
        if (data[i]["status"] == "Pass") {
            data[i]["color"] = "rgba(0,255,0,0.8)";
            data[i]["y"] = 1;
        } else if (data[i]["status"] == "Fixed") {
            data[i]["color"] = "rgba(0,128,0,0.8)";
            data[i]["y"] = 1;
        } else if (data[i]["status"] == "Fail") {
            data[i]["color"] = "rgba(255,0,0,0.8)";
            data[i]["y"] = -1;
        } else if (data[i]["status"] == "Regression") {
            data[i]["color"] = "rgba(128,0,0,0.8)";
            data[i]["y"] = -1;
        }

        delete data[i]["status"];
        delete data[i]["build_number"];
    }
    console.log(data)
    return data;
};

var catCategories = function (data) {
    var retVal = [];
    for (var i = 0; i < data.length; i++) {
        retVal.push(data[i]["x"]);
        delete data[i]["x"];
    }
    return retVal;
}

$(function () {
    var data = parseJson();
    var axisCategories = catCategories(data);
    $('#chart').highcharts({
        chart: {
            type: 'column'
        },
        title: {
            text: 'Test Results History'
        },
        xAxis: {
            categories: axisCategories
        },
        yAxis: {
            min: -1,
            max: 1,
            title: {
                text: 'Pass/Fail'
            },
            labels: {
                formatter: function () {
                    if (this.value == 1) return "Pass";
                    else if (this.value == -1) return "Fail";
                    else if (this.value == 0) return "0";
                }
            }
        },
        tooltip: {
            headerFormat: '<span style="font-size:10px">{point.x}</span><table>',
            pointFormat: '<tr><td style="color:{series.color};padding:0">Status: </td>' +
                '<td style="padding:0"><b>{point.name}</b></td></tr>',
            footerFormat: '</table>',
            shared: false,
            useHTML: true
        },
        series: [{
                name: 'Status',
                color: 'rgba(0,0,0,1)',
                data: data
        },
      ]
    });
});