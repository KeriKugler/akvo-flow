// ***********************************************//
//                      views
// ***********************************************//

require('akvo-flow/core');
require('akvo-flow/views/survey-group-views');
require('akvo-flow/views/survey-details-views');

FLOW.ApplicationView = Ember.View.extend({
	templateName: 'application'
});

// localisation helper
Ember.Handlebars.registerHelper('t', function(i18nKey, options) {
  return Ember.String.loc(i18nKey);
});

// date format helper
Ember.Handlebars.registerHelper("date", function(property) {
  var d = new Date(parseInt(Ember.get(this, property),10));
  var m_names = new Array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

  var curr_date = d.getDate();
  var curr_month = d.getMonth();
  var curr_year = d.getFullYear();
  return (curr_date + " " + m_names[curr_month] + " " + curr_year);
});

// ********************************************************//
//                      main navigation
// ********************************************************//

FLOW.NavigationView = Em.View.extend({
	templateName: 'navigation',
	selectedBinding: 'controller.selected',

	onLanguageChange:function(){
		this.rerender();
	}.observes('FLOW.languageControl.dashboardLanguage'),

	NavItemView: Ember.View.extend({
		tagName: 'li',
		classNameBindings: 'isActive:current navItem'.w(),

		navItem: function() {
			return this.get('item');
		}.property('item').cacheable(),

		isActive: function() {
			return this.get('item') === this.get('parentView.selected');
		}.property('item', 'parentView.selected').cacheable()
	})
});

// ********************************************************//
//                      standard views
// ********************************************************//

// home screen view
FLOW.NavHomeView = Ember.View.extend({ templateName: 'navHome/nav-home'});

// surveys views
FLOW.NavSurveysView = Ember.View.extend({ templateName: 'navSurveys/nav-surveys'});
FLOW.NavSurveysMainView = Ember.View.extend({ templateName: 'navSurveys/nav-surveys-main'});
FLOW.NavSurveysEditView = Ember.View.extend({ templateName: 'navSurveys/nav-surveys-edit'});

// devices views
FLOW.NavDevicesView = Ember.View.extend({ templateName: 'navDevices/nav-devices'});
FLOW.CurrentDevicesView = Ember.View.extend({ templateName: 'navDevices/current-devices'});
FLOW.AssignSurveysOverviewView = Ember.View.extend({ templateName: 'navDevices/assign-survey-overview'});
FLOW.EditSurveyAssignment = Ember.View.extend({ templateName: 'navDevices/edit-survey-assignment'});


// data views
FLOW.NavDataView = Ember.View.extend({ templateName: 'navData/nav-data'});
FLOW.InspectDataView = Ember.View.extend({ templateName: 'navData/inspect-data'});
FLOW.ImportSurveyView = Ember.View.extend({ templateName: 'navData/import-survey'});
FLOW.ExcelImportView = Ember.View.extend({ templateName: 'navData/excel-import'});
FLOW.ExcelExportView = Ember.View.extend({ templateName: 'navData/excel-export'});

// reports views
FLOW.NavReportsView = Ember.View.extend({ templateName: 'navReports/nav-reports'});

// maps views
FLOW.NavMapsView = Ember.View.extend({
  controller: FLOW.NavMapsController,
  templateName: "navMaps/nav-maps",
  didInsertElement: function() {
    var cloudMade = {
      apiKey: "a1029e8c8d9d42bc84e96b8a960bb42e",
      themeId: 1,
      tileSize: 256
    };
    var config = {
      annotation: "Map data &copy; Akvo FLOW",
      center: [0, 0],
      maxZoom: 18,
      tileUrl: "http://{s}.tile.cloudmade.com/" +
               cloudMade.apiKey + "/" +
               cloudMade.themeId + "/" +
               cloudMade.tileSize + "/" +
               "{z}/{x}/{y}.png",
      zoom: 2
    };
    var map = L.map("map").setView(config.center, config.zoom);
    L.tileLayer(config.tileUrl, {
      attribution: config.annotation,
      maxZoom: config.maxZoom
    }).addTo(map);
    $.each(this.controller.get("content"), function(index, surveyedLocale) {
      var marker = L.marker([surveyedLocale.latitude, surveyedLocale.longitude]).addTo(map);
      marker.bindPopup(surveyedLocale.text);
    });
    var legend = L.control({position: "bottomleft"});
    legend.onAdd = function() {
      var div = L.DomUtil.get("legend");
      return div;
    };
    legend.addTo(map);
  }
});

// users views
FLOW.NavUsersView = Ember.View.extend({	templateName: 'navUsers/nav-users'});

// admin views
FLOW.NavAdminView = Ember.View.extend({	templateName: 'navAdmin/nav-admin'});


// ********************************************************//
//             Subnavigation for the Data tabs
// ********************************************************//
FLOW.DatasubnavView = Em.View.extend({
	templateName: 'navData/data-subnav',
	selectedBinding: 'controller.selected',
	NavItemView: Ember.View.extend({
		tagName: 'li',
		classNameBindings: 'isActive:active'.w(),

		isActive: function() {
			return this.get('item') === this.get('parentView.selected');
		}.property('item', 'parentView.selected').cacheable()
	})
});

FLOW.DevicesSubnavView = Em.View.extend({
	templateName: 'navDevices/devices-subnav',
	selectedBinding: 'controller.selected',
	NavItemView: Ember.View.extend({
		tagName: 'li',
		classNameBindings: 'isActive:active'.w(),

		isActive: function() {
			return this.get('item') === this.get('parentView.selected');
		}.property('item', 'parentView.selected').cacheable()
	})
});

FLOW.DevicesTableHeaderView = Em.View.extend({
	templateName: 'navDevices/current-devices-table-header',
	tagName:'tr',
	//selectedBinding:'controller.selected',
	
	NavItemView: Ember.View.extend({
		tagName: 'th',
		item:null,
		
		classNameBindings: ['isActiveAsc:sorting_asc','isActiveDesc:sorting_desc'],
		
		isActiveAsc: function() {
			return (this.get('item') === FLOW.deviceControl.get('selected'))&&(FLOW.deviceControl.get('sortAscending')===true);
		}.property('item', 'FLOW.deviceControl.selected','FLOW.deviceControl.sortAscending').cacheable(),

		isActiveDesc: function() {
			return (this.get('item') === FLOW.deviceControl.get('selected'))&&(FLOW.deviceControl.get('sortAscending')===false);
		}.property('item', 'FLOW.deviceControl.selected','FLOW.deviceControl.sortAscending').cacheable(),


		sort:function(){
			if ((this.get('isActiveAsc'))||(this.get('isActiveDesc'))) {
				FLOW.deviceControl.toggleProperty('sortAscending');
			}
			else {
				FLOW.deviceControl.set('sortProperties',[this.get('item')]);
				FLOW.deviceControl.set('selected',this.get('item'));
			}
		}
	})
});


FLOW.CurrentDevicesTabView = Em.View.extend({
	showDeleteDevicesDialogue: function(){
		console.log("show dialogue");
	},

	doDeleteDevices: function(){

	}
});


