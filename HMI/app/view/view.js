//list of layout and views in apps
$(function(){

var areaView = new kendo.View("area-template", {
	model: areaModel
});

var zoneView= new kendo.View("zone-template", {
	model: zoneModel
});

var deviceView = new kendo.View("device-template", {
	model: deviceModel
});

var statusView = new kendo.View("status-template", {
	model: statusModel
});

var eventView = new kendo.View("event-template", {
	model: eventModel
});

var editView = new kendo.View("edit-template", {
	model: nameContainer
});

var groupView = new kendo.View("group-template", {
	model: groupContainer
});

var layout = new kendo.Layout("layout-template", {

});

var html = layout.render();
$("body").html(html);

});
