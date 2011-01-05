/**
 * Ajaxflow, a plugin for Ajaxified Webflows
 * Copyright (C) 2010 Jeroen Wesbeek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision information:
 * $Author$
 * $Rev$
 * $Date$
 */
import groovy.text.SimpleTemplateEngine

includeTargets << grailsScript("_GrailsArgParsing")

USAGE = """
    create-ajaxflow PKG NAME

where
    PKG  = The package name to use for your controller.
    NAME = The name of your controller and the name of the folder for your views and images

"""

def parseArgs() {
	args = args ? args.split('\n') : []
    switch (args.size()) {
            case 2:
            		println "Using package ${args[0]} to create the Ajax Flow Controller"
                    println "and using the following name for the Controller name, and the"
					println "view and images folders: ${args[1]}"
                    return [args[0], args[1]]
                    break
            default:
                	usage()
                	break
    }
}

private void usage() {
	println "Usage:\n${USAGE}"
	System.exit(1)
}

target(default: 'Sets up a new ajax base flow, ready for customization') {
	// define variables
	def packageName, name, packageDir, nameDir

	// get arguments
	(packageName, name) = parseArgs()

	// change package name into directory
    packageDir		= packageName.replaceAll(/\.|\//, '/')
	camelCaseNameDir= name.replace(' ','-')
	nameDir			= camelCaseNameDir.toLowerCase()

	// define date stamp (YYYYMMDD)
	def today = String.format('%tY%<tm%<td', new GregorianCalendar() )

	// set up the template engine
	def engine = new SimpleTemplateEngine()

	// define bindings
	def controllerName = name[0].toUpperCase() + name.substring(1) + "Controller"
	def binding	= [
		'packageName'	: packageName,
		'className'		: controllerName,
		'viewDir'		: camelCaseNameDir,
		'imageDir'		: nameDir,
		'name'			: name,
		'nameError'		: name + 'Error',
		'css'			: name + '.css',
		'today'			: today,
		'openTag'		: '<%',
		'closeTag'		: '%>'
	]

	// create the ajax flow controller
	mkdir(dir:"${basedir}/grails-app/controllers/${packageDir}")
	def controllerTemplate	= engine.createTemplate(loadAndEscape("${ajaxflowPluginDir}/src/templates/controllers/WizardController.groovy")).make(binding)
	new File("${basedir}/grails-app/controllers/${packageDir}/${controllerName}.groovy").write(controllerTemplate.toString())

	// create the ajax flow javascript
	mkdir(dir:"${basedir}/web-app/css")
	def jsTemplate	= engine.createTemplate(loadAndEscape("${ajaxflowPluginDir}/src/templates/css/wizard.css")).make(binding)
	new File("${basedir}/web-app/css/${name}.css").write(jsTemplate.toString())

	// set up the ajax flow views
	def views	= [
		// index view
	    'index.gsp',

		// common views
		'common/_ajaxflow.gsp',
		'common/_navigation.gsp',
		'common/_page_header.gsp',
		'common/_page_footer.gsp',
		'common/_tabs.gsp',
		'common/_on_page.gsp',
		'common/_refresh_flow.gsp',
		'common/_please_wait.gsp',

		// page views
		'pages/_page_one.gsp',
		'pages/_page_two.gsp',
		'pages/_page_three.gsp',
		'pages/_page_four.gsp',
		'pages/_final_page.gsp',
		'pages/_error.gsp'
	]
	def viewTemplate = ''
	mkdir(dir:"${basedir}/grails-app/views/${camelCaseNameDir}")
	mkdir(dir:"${basedir}/grails-app/views/${camelCaseNameDir}/common")
	mkdir(dir:"${basedir}/grails-app/views/${camelCaseNameDir}/pages")
	views.each {
		// parse view template
		viewTemplate = engine.createTemplate(loadAndEscape("${ajaxflowPluginDir}/src/templates/views/${it}")).make(binding)

		// write file
		new File("${basedir}/grails-app/views/${camelCaseNameDir}/${it}").write(viewTemplate.toString())
	}

	// set up images
	mkdir(dir:"${basedir}/web-app/images/${nameDir}")
	copy(file:"${ajaxflowPluginDir}/src/templates/images/arrowL.gif", tofile: "${basedir}/web-app/images/${nameDir}/arrowL.gif", overwrite: false)
	copy(file:"${ajaxflowPluginDir}/src/templates/images/arrowR.gif", tofile: "${basedir}/web-app/images/${nameDir}/arrowR.gif", overwrite: false)
	copy(file:"${ajaxflowPluginDir}/src/templates/images/spacer.gif", tofile: "${basedir}/web-app/images/${nameDir}/spacer.gif", overwrite: false)
	copy(file:"${ajaxflowPluginDir}/src/templates/images/ajax-loader.gif", tofile: "${basedir}/web-app/images/${nameDir}/ajax-loader.gif", overwrite: false)

	println ""
	println "Your Ajax Flow is set up, please browse to /${name} to view your new Ajax Flow"
}

def loadAndEscape(path) {
	// load file
	def file	= new FileReader(path)
	def content	= file.text

	// escape svn keyword expansion
	return content.replaceAll(
		/\$([A-Za-z]+):([^\$]+)\$/,
		'\\\\\\\$$1: $2\\\\\\\$'
	)
}