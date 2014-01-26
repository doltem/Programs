//Model for Event List
eventModel = kendo.observable({
    area: "Bangunan",
    serviceurl: "http://localhost:80/service/event",

    dataSource: new kendo.data.DataSource({
      /*schema: {
        model: { id: "id"}
      }, ??????*/
      transport: {
        read: {
          url: serviceurl,
          dataType: "JSON",
          type: "GET"
        }        
      }

    }),
});
