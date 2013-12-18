//Model for Containers
$(function(){

  var nameContainer = kendo.observable({
    name: "",
    type: "",
    newname:"",

    onClick: function(e){
      var index = e.sender.select().index();
      var data = this.dataSource.view()[index];
      
      this.trigger("name:save", {
        name : this,get("name"),
        type : this,get("type"),
        newname : this,get("newname")
      });
    }
  });

  var groupContainer = kendo.observable({
    device: "",
    area: "",

    onClick: function(e){
      var index = e.sender.select().index();
      var data = this.dataSource.view()[index];
      
      this.trigger("group:save", {
        device : this,get("device"),
        area : this,get("area")
      });
    }
  });

  nameContainer.bind("name:save", SaveName);
  groupContainer.bind("group:save", SaveGroup);

});