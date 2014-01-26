require.config({
    baseUrl : "",
    paths: {
        kendo            : "kendo/js/kendo.mobile.min",
        jquery           : "jquery/jquery.min",
        text             : "require/text",
        //models
        areamodel        : "app/model/areamodel",
        zonemodel        : "app/model/zonemodel",
        devicemodel      : "app/model/devicemodel",
        statusmodel      : "app/model/statusmodel",
        eventmodel       : "app/model/eventmodel",
        container        : "app/model/container",
        //views
        views            : "app/view/view",
        //controller
        viewcontroller   : "app/controller/viewcontroller",
        datacontroller   : "app/controller/datacontroller"
    },

    shim: {
        kendo: {
            deps: ['jquery'],
            exports: 'kendo'
        }
    }
});

var app;

require(['jquery','kendo'], function ($,kendo) {
    require(['areamodel','zonemodel','devicemodel','statusmodel','eventmodel','container','views','viewcontroller','datacontroller'], function (area,zone,device,status,event,container,view,conView,conPeta) {
        app = new kendo.mobile.Application();
        viewController.showMain();
    }); 
}); 