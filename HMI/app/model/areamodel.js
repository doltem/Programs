//Model for Area Dropdown List
areaModel = kendo.observable({
    area: "Bangunan",
    serviceurl: "http://localhost:80/service/area",

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
        create: {
          url: serviceurl,
          dataType: "JSON",
          type: "POST"
        }
        update:{
          url: serviceurl,
          dataType: "JSON",
          type: "PUT"          
        }
      }

    }),

    onSelect: function(e){
      var dataItem = this.dataItem(e.item.index());
      this.trigger("zone:selected", dataItem.value);
    },

    onEdit: function(data){
      var index = e.sender.select().index();
      
      this.trigger("area:edit", { 
        name : this.dataSource.view()[index],
        type : "area"
      });
    }
  });

  //Controller Binder
  areaModel.bind("area:selected", AreaChange);
  areaModel.bind("area:edit", EditName);

  //function list
  function AreaChange(area){
    viewController.setFilter("device",area);
    layout.showIn("#content", view.deviceView);
  }

  function EditName(){

  }
