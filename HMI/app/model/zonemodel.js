//Model for Zone Dropdown List
$(function(){

  var zoneModel = kendo.observable({
    address: "",
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

      filter: { field: "address", operator: "eq", value: address }

    }),

    onSelect: function(e){
      var index = e.sender.select().index();
      var data = this.dataSource.view()[index];
      
      this.trigger("zone:selected", data);
    },

    onEdit: function(data){
      var index = e.sender.select().index();
      
      this.trigger("zone:edit", { 
        name : this.dataSource.view()[index],
        type : "zone"
      });
    }
  });

  //Controller Binder
  zoneModel.bind("zone:selected", ZoneChange);
  zoneModel.bind("zone:edit", EditName);
});