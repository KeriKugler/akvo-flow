// ***********************************************//
//                      Navigation views
// ***********************************************//
/*global tooltip, makePlaceholders */


require('akvo-flow/core');
require('akvo-flow/views/surveys/preview-view');
require('akvo-flow/views/surveys/survey-group-views');
require('akvo-flow/views/surveys/survey-details-views');
require('akvo-flow/views/data/inspect-data-table-views');
require('akvo-flow/views/data/data-attribute-views');
require('akvo-flow/views/surveys/question-view');
require('akvo-flow/views/surveys/question-answer-view');
require('akvo-flow/views/reports/report-views');
require('akvo-flow/views/reports/export-reports-views');
require('akvo-flow/views/maps/map-views');
require('akvo-flow/views/messages/message-view');
require('akvo-flow/views/devices/devices-views');
require('akvo-flow/views/users/user-view');

FLOW.ApplicationView = Ember.View.extend({
  templateName: 'application/application',

  init: function() {
    var locale;

    this._super();

    // If available set language from local storage
    locale = localStorage.locale;
    if(typeof locale === 'undefined') {
      locale = 'en';
    }
    switch(locale) {
    case 'nl':
      Ember.STRINGS = Ember.STRINGS_NL;
      break;
    case 'fr':
      Ember.STRINGS = Ember.STRINGS_FR;
      break;
    case 'es':
      Ember.STRINGS = Ember.STRINGS_ES;
      break;
    default:
      Ember.STRINGS = Ember.STRINGS_EN;
      break;
    }
  }
});


// ***********************************************//
//                      Handlebar helpers
// ***********************************************//
// localisation helper
Ember.Handlebars.registerHelper('t', function(i18nKey, options) {
  return Ember.String.loc(i18nKey);
});

// add space to vertical bar helper
Ember.Handlebars.registerHelper('addSpace', function(property) {
  return Ember.get(this, property).replace(/\|/g, ' | ');
});

// date format helper
Ember.Handlebars.registerHelper("date", function(property) {
  var d = new Date(parseInt(Ember.get(this, property), 10));
  var m_names = new Array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

  var curr_date = d.getDate();
  var curr_month = d.getMonth();
  var curr_year = d.getFullYear();
  return(curr_date + " " + m_names[curr_month] + " " + curr_year);
});

// format used in devices table
Ember.Handlebars.registerHelper("date1", function(property) {
  var d, curr_date, curr_month, curr_year, curr_hour, curr_min, monthString, dateString, hourString, minString;
  if(Ember.get(this, property) !== null) {
    d = new Date(parseInt(Ember.get(this, property), 10));
    curr_date = d.getDate();
    curr_month = d.getMonth() + 1;
    curr_year = d.getFullYear();
    curr_hour = d.getHours();
    curr_min = d.getMinutes();

    if(curr_month < 10) {
      monthString = "0" + curr_month.toString();
    } else {
      monthString = curr_month.toString();
    }

    if(curr_date < 10) {
      dateString = "0" + curr_date.toString();
    } else {
      dateString = curr_date.toString();
    }

    if(curr_hour < 10) {
      hourString = "0" + curr_hour.toString();
    } else {
      hourString = curr_hour.toString();
    }

    if(curr_min < 10) {
      minString = "0" + curr_min.toString();
    } else {
      minString = curr_min.toString();
    }

    return(curr_year + "-" + monthString + "-" + dateString + "  " + hourString + ":" + minString);
  } else {
    return "";
  }
});

// Register a Handlebars helper that instantiates `view`.
// The view will have its `content` property bound to the
// helper argument.
FLOW.registerViewHelper = function(name, view) {
  Ember.Handlebars.registerHelper(name, function(property, options) {
    options.hash.contentBinding = property;
    return Ember.Handlebars.helpers.view.call(this, view, options);
  });
};

FLOW.registerViewHelper('date2', Ember.View.extend({
  tagName: 'span',

  template: Ember.Handlebars.compile('{{view.formattedContent}}'),

  formattedContent: (function() {
    var content, d, curr_date, curr_month, curr_year, curr_hour, curr_min, monthString, dateString, hourString, minString;
    content = this.get('content');

    if(content === null) {
      return "";
    }

    d = new Date(parseInt(content, 10));
    curr_date = d.getDate();
    curr_month = d.getMonth() + 1;
    curr_year = d.getFullYear();
    curr_hour = d.getHours();
    curr_min = d.getMinutes();

    if(curr_month < 10) {
      monthString = "0" + curr_month.toString();
    } else {
      monthString = curr_month.toString();
    }

    if(curr_date < 10) {
      dateString = "0" + curr_date.toString();
    } else {
      dateString = curr_date.toString();
    }

    if(curr_hour < 10) {
      hourString = "0" + curr_hour.toString();
    } else {
      hourString = curr_hour.toString();
    }

    if(curr_min < 10) {
      minString = "0" + curr_min.toString();
    } else {
      minString = curr_min.toString();
    }

    return(curr_year + "-" + monthString + "-" + dateString + "  " + hourString + ":" + minString);
  }).property('content')
}));

// ********************************************************//
//                      main navigation
// ********************************************************//
FLOW.NavigationView = Em.View.extend({
  templateName: 'application/navigation',
  selectedBinding: 'controller.selected',

  onLanguageChange: function() {
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
// TODO check if doing this in View is not impacting performance,
// as some pages have a lot of views (all navigation elements, for example)
// one way could be use an extended copy of view, with the didInsertElement,
// for some of the elements, and not for others.
Ember.View.reopen({
  didInsertElement: function() {
    this._super();
    tooltip();
    //$("nav#topnav li.current").prev("nav#topnav li").css("background", "none");
    //$("nav#topnav li").hover(function() {
    //  $(this).prev().css("background", "none");
    //});
    // Adds needed classes to survey assets as nth-child selectors don't work in ie.
    //$('li.aSurvey:nth-child(4n+1)').addClass('firstOfRow');
    //$('li.aSurvey:nth-child(4n)').addClass('endOfRow');
    //$('table#devicesListTable tbody tr:nth-child(2n)').addClass('even');


    var nCount = 0;
    $(".addQuestion").click(function() {
      nCount++;
      $(".questionSetContent div.innerContent").fadeIn().css("box-shadow", "0 0 3px rgba(0,0,0,0.1)");
      $(this).insertAfter("div.innerContent");
      $("#numberQuestion").text(

      function() {
        if(nCount < 10) {
          $(this).text("0" + nCount);
        } else {
          $(this).text(nCount);
        }
      });
      var nQ = parseInt($("#numberQuestion").text(), 10);
      $("h1.questionNbr span").text(

      function() {
        if(nQ < 10) {
          $(this).text("0" + nQ);
        } else {
          $(this).text(nQ);
        }
      });
    });

    // Function displaying the options depending on question type
    //$('.formElems').hide();
    // listener for QR type
    //$("#questionType").change(function() {
     // var selected = $("#questionType option:selected").val();
     // $('.formElems').hide();
     // $("." + selected).show();
    //});
  }
});

FLOW.DateField = Ember.TextField.extend({
 didInsertElement: function() {
    this._super();
    
    // datepickers
    $("#from_date").datepicker({
      dateFormat: 'yy/mm/dd',
      defaultDate: "+1w",
      numberOfMonths: 1,
      onSelect: function(selectedDate) {
        $("#to_date").datepicker("option", "minDate", selectedDate);
        FLOW.dateControl.set('fromDate', selectedDate);
      }
    });

    $("#to_date").datepicker({
      dateFormat: 'yy/mm/dd',
      defaultDate: "+1w",
      numberOfMonths: 1,
      onSelect: function(selectedDate) {
        $("#from_date").datepicker("option", "maxDate", selectedDate);
        FLOW.dateControl.set('toDate', selectedDate);
      }
    });
  }
});

FLOW.DateField2 = Ember.TextField.extend({
 didInsertElement: function() {
    this._super();
  
    this.$().datepicker({
      dateFormat: 'yy/mm/dd',
      defaultDate: "+1w",
      numberOfMonths: 1
    });
  }
});



// home screen view
FLOW.NavHomeView = Ember.View.extend({
  templateName: 'navHome/nav-home'
});

// surveys views
FLOW.NavSurveysView = Ember.View.extend({
  templateName: 'navSurveys/nav-surveys'
});
FLOW.NavSurveysMainView = Ember.View.extend({
  templateName: 'navSurveys/nav-surveys-main'
});
FLOW.NavSurveysNewView = Ember.View.extend({
  templateName: 'navSurveys/nav-surveys-new'
});
FLOW.NavSurveysEditView = Ember.View.extend({
  templateName: 'navSurveys/nav-surveys-edit'
});

// devices views
FLOW.NavDevicesView = Ember.View.extend({
  templateName: 'navDevices/nav-devices'
});
FLOW.CurrentDevicesView = Ember.View.extend({
  templateName: 'navDevices/devices-list-tab/devices-list'
});
FLOW.AssignSurveysOverviewView = Ember.View.extend({
  templateName: 'navDevices/assignment-list-tab/assignment-list'
});
FLOW.EditSurveyAssignmentView = Ember.View.extend({
  templateName: 'navDevices/assignment-edit-tab/assignment-edit'
});


// data views
FLOW.NavDataView = Ember.View.extend({
  templateName: 'navData/nav-data'
});

FLOW.InspectDataView = Ember.View.extend({
  templateName: 'navData/inspect-data'
});

FLOW.ManageAttributesView = Ember.View.extend({
  templateName: 'navData/manage-attributes'
});

FLOW.ImportSurveyView = Ember.View.extend({
  templateName: 'navData/import-survey'
});
FLOW.ExcelImportView = Ember.View.extend({
  templateName: 'navData/excel-import'
});
FLOW.ExcelExportView = Ember.View.extend({
  templateName: 'navData/excel-export'
});

// reports views
FLOW.NavReportsView = Ember.View.extend({
  templateName: 'navReports/nav-reports'
});

FLOW.ExportReportsView = Ember.View.extend({
  templateName: 'navReports/export-reports'
});

FLOW.ChartReportsView = Ember.View.extend({
  templateName: 'navReports/chart-reports'
});


// applets
FLOW.rawDataReportApplet = Ember.View.extend({
  templateName: 'navReports/applets/raw-data-report-applet'
});

FLOW.comprehensiveReportApplet = Ember.View.extend({
  templateName: 'navReports/applets/comprehensive-report-applet'
});

FLOW.googleEarthFileApplet = Ember.View.extend({
  templateName: 'navReports/applets/google-earth-file-applet'
});

FLOW.surveyFormApplet = Ember.View.extend({
  templateName: 'navReports/applets/survey-form-applet'
});

FLOW.bulkImportApplet = Ember.View.extend({
  templateName: 'navData/applets/bulk-import-applet'
});

FLOW.rawDataImportApplet = Ember.View.extend({
  templateName: 'navData/applets/raw-data-import-applet'
});

// users views
FLOW.NavUsersView = Ember.View.extend({
  templateName: 'navUsers/nav-users'
});

// Messages views
FLOW.NavMessagesView = Ember.View.extend({
  templateName: 'navMessages/nav-messages'
});

// admin views
FLOW.NavAdminView = Ember.View.extend({
  templateName: 'navAdmin/nav-admin',

  onLanguageChange: function() {
    this.rerender();
  }.observes('FLOW.languageControl.dashboardLanguage')
});

FLOW.HeaderView = Ember.View.extend({
  templateName: 'application/header',

  onLanguageChange: function() {
    this.rerender();
  }.observes('FLOW.languageControl.dashboardLanguage')
});

FLOW.FooterView = Ember.View.extend({
  templateName: 'application/footer',

  onLanguageChange: function() {
    this.rerender();
  }.observes('FLOW.languageControl.dashboardLanguage')
});

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

// ********************************************************//
//             Subnavigation for the Device tabs
// ********************************************************//
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

// ********************************************************//
//             Subnavigation for the Reports tabs
// ********************************************************//
FLOW.ReportsSubnavView = Em.View.extend({
  templateName: 'navReports/reports-subnav',
  selectedBinding: 'controller.selected',
  NavItemView: Ember.View.extend({
    tagName: 'li',
    classNameBindings: 'isActive:active'.w(),

    isActive: function() {
      return this.get('item') === this.get('parentView.selected');
    }.property('item', 'parentView.selected').cacheable()
  })
});


// *************************************************************//
//             Generic table column view which supports sorting
// *************************************************************//
FLOW.ColumnView = Ember.View.extend({
  tagName: 'th',
  item: null,
  type: null,

  classNameBindings: ['isActiveAsc:sorting_asc', 'isActiveDesc:sorting_desc'],

  isActiveAsc: function() {
    return(this.get('item') === FLOW.tableColumnControl.get('selected')) && (FLOW.tableColumnControl.get('sortAscending') === true);
  }.property('item', 'FLOW.tableColumnControl.selected', 'FLOW.tableColumnControl.sortAscending').cacheable(),

  isActiveDesc: function() {
    return(this.get('item') === FLOW.tableColumnControl.get('selected')) && (FLOW.tableColumnControl.get('sortAscending') === false);
  }.property('item', 'FLOW.tableColumnControl.selected', 'FLOW.tableColumnControl.sortAscending').cacheable(),

  sort: function() {
    if((this.get('isActiveAsc')) || (this.get('isActiveDesc'))) {
      FLOW.tableColumnControl.toggleProperty('sortAscending');
    } else {
      FLOW.tableColumnControl.set('sortProperties', [this.get('item')]);
      FLOW.tableColumnControl.set('selected', this.get('item'));
    }

    if(this.get('type') === 'device') {
      FLOW.deviceControl.getSortInfo();
    } else if(this.get('type') === 'assignment') {
      FLOW.surveyAssignmentControl.getSortInfo();
    } else if(this.get('type') === 'attribute'){
      FLOW.attributeControl.getSortInfo();
    } else if(this.get('type') === 'message'){
      FLOW.messageControl.getSortInfo();
    }
  }
});

var set = Ember.set, get = Ember.get;
Ember.RadioButton = Ember.View.extend({
  title: null,
  checked: false,
  group: "radio_button",
  disabled: false,

  classNames: ['ember-radio-button'],

  defaultTemplate: Ember.Handlebars.compile('<label><input type="radio" {{ bindAttr disabled="view.disabled" name="view.group" value="view.option" checked="view.checked"}} />{{view.title}}</label>'),

  bindingChanged: function(){
   if(this.get("option") == get(this, 'value')){
       this.set("checked", true);
    }
  }.observes("value"),
    
  change: function() {
    Ember.run.once(this, this._updateElementValue);
  },

  _updateElementValue: function() {
    var input = this.$('input:radio');
    set(this, 'value', input.attr('value'));
  }
});