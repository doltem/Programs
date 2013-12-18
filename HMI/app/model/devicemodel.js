//Model for Device List
$(function(){

  var deviceModel = kendo.observable({
    area: "Bangunan",
    serviceurl: "http://localhost:80/service/location",

    dataSource: new kendo.data.DataSource({
      /*schema: {
        model: { id: "id"}
      }, ??????*/
      transport: {
        read: {
          url: serviceurl,
          dataType: "JSON",
          type: "GET"
        },
        update:{
          url: serviceurl,
          dataType: "JSON",
          type: "PUT"          
        }
      },

      filter: { field: "area", operator: "eq", value: area }

    }),

    onClick: function(e){
      var index = e.sender.select().index();
      var data = this.dataSource.view()[index];
      
      this.trigger("device:clicked", data);
    },

    onEdit: function(data){
      var index = e.sender.select().index();
      
      this.trigger("device:edit", { 
        name : this.dataSource.view()[index],
        type : "device"
      });
    }
  });

  //Controller Binder
  deviceModel.bind("device:clicked", DeviceSelect);
  deviceModel.bind("device:edit", EditName);
});