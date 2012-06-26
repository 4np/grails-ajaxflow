/**
 *  Ajaxflow, a plugin for Ajaxified Webflows
 *  Copyright (C) 2010 Jeroen Wesbeek
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  $Author$
 *  $Rev$
 *  $Date$
 */
class AjaxflowGrailsPlugin {
    def version		= "0.2.3"
    def grailsVersion	= "1.3.4 > *"
    def dependsOn	= [webflow: "1.3.4 => *", jquery: "1.4 => *"]
    def pluginExcludes	= [
        	"grails-app/views/error.gsp",
		"grails-app/conf/DataSource.groovy",
                "web-app/css",
                "web-app/images",
                "web-app/js/prototype",
                "web-app/js/application.js"
    ]

    // TODO Fill in these fields
    def title		= "This plugin enables Ajaxified Webflows"
    def author		= "Jeroen Wesbeek"
    def authorEmail	= "work@osx.eu"
    def description	= '''\\
This plugin enables Ajaxified Webflows and is able to inject working customizable ajax webflows into your existing project(s)
'''
    def documentation	= "https://github.com/4np/grails-ajaxflow/blob/master/README.md"
    def license		= "APACHE"
    def issueManagement	= [ system: "github", url: "https://github.com/4np/grails-ajaxflow/issues" ]
    def scm		= [ url: "https://github.com/4np/grails-ajaxflow" ]

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
