//Controllers for changing the view
viewController = {
	editName : function(){

	},

	ViewGroup : function(){

	},

	getData : function(model,index){
		var data;
		switch(model){
			case "area":
				data=areaModel.dataSource.at(index);
				break;
			case "zone":
				data=areaModel.dataSource.at(index);
				break;
		}
		return data;
	},

	setFilter : function(model,data){
		switch(model){
			case "device":
				deviceModel.dataSource.filter({ field: "area", operator: "eq", value: data });
				break;
			case "zone":
				zoneModel.dataSource.filter({ field: "address", operator: "eq", value: data });
				break;
			case "status":
				statusModel.dataSource.filter({ field: "id", operator: "eq", value: data });
				break;
		}
    deviceModel.dataSource.filter({ field: "area", operator: "eq", value: data });
	},

	showMain : function(){
		layout.showIn("#dropdown", areaView);
		var area=viewController.getData("area",0);
		viewController.setFilter("device",area);
		layout.showIn("#content", view.statusView);
	},

	showEvent : function(){
		layout.showIn("#main", view.eventView);
	}
};