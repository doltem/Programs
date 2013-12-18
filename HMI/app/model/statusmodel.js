//Model for Status List
$(function(){

  var statusModel = kendo.observable({
    id: "",
    serviceurl: "http://localhost:80/service/zone",

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

      filter: { field: "id", operator: "eq", value: id }

    }),

    onClick: function(e){
      var index = e.sender.select().index();
      var data = this.dataSource.view()[index];
      
      this.trigger("status:save", {
        zone : data.zone,
        address : data.address,
        mode : data.mode,
        setpoint : data.setpoint,
        errorband : data.erroband,
        lamp : data.lamp
      });
    }
  });

  //Controller Binder
  statusModel.bind("status:save", SendCommand);
});