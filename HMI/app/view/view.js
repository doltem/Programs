//list of layout and views in apps
	areaView = new kendo.View("area-template", {
		model: areaModel
	});

	zoneView= new kendo.View("zone-template", {
		model: zoneModel
	});

	deviceView = new kendo.View("device-template", {
		model: deviceModel
	});

	statusView = new kendo.View("status-template", {
		model: statusModel
	});

	eventView = new kendo.View("event-template", {
		model: eventModel
	});

	editView = new kendo.View("edit-template", {
		model: nameContainer
	});

	groupView = new kendo.View("group-template", {
		model: groupContainer
	});

	layout = new kendo.Layout("layout-template", {

	});
