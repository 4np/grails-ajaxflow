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
package nl.grails.plugins.ajaxflow

import org.codehaus.groovy.grails.plugins.web.taglib.JavascriptTagLib

class AjaxflowTagLib extends JavascriptTagLib {
	// define the tag namespace (e.g.: <af:action ... />
	static namespace = "af"

	// define the AJAX provider to use
	final static ajaxProvider = "jquery"

	// define a map of number names below 100
	final static numbers = [
		'0'		: '',
	    '1'		: 'One',
	    '2'		: 'Two',
	    '3'		: 'Three',
	    '4'		: 'Four',
	    '5'		: 'Five',
	    '6'		: 'Six',
	    '7'		: 'Seven',
	    '8'		: 'Eight',
	    '9'		: 'Nine',
	    '10'	: 'Ten',
	    '11'	: 'Eleven',
	    '12'	: 'Twelve',
	    '13'	: 'Thirteen',
	    '14'	: 'Fourteen',
	    '15'	: 'Fifteen',
	    '16'	: 'Sixteen',
	    '17'	: 'Seventeen',
	    '18'	: 'Eighteen',
	    '19'	: 'Nineteen',
		'2x'	: 'Twenty',
		'3x'	: 'Thirty',
		'4x'	: 'Fourty',
		'5x'	: 'Fifty',
		'6x'	: 'Sixty',
		'7x'	: 'Seventy',
		'8x'	: 'Eighty',
		'9x'	: 'Ninety',
	]

	/**
	 * wizard tag
	 *
	 * The wizard tag is an alias for the generic form tag, but it already
	 * sets some values by default which are required for the ajaxified
	 * webflows to work properly
	 *
	 * @param Map		attributes
	 * @param Closure	body
	 */
	def flow = { attrs, body ->
		def formName 		= (attrs.get('name')) ? attrs.remove('name') : 'wizard'
		def spinner			= (attrs.get('spinner')) ? attrs.remove('spinner') : ''
		def formAttributes = [
			action	: (attrs.get('action')) ? attrs.remove('action') : 'pages',
			name	: formName,
			id		: formName,
			enctype	: (attrs.get('enctype')) ? attrs.remove('enctype') : 'multipart/form-data'
		]

		// got a class?
		if (attrs.get('class')) formAttributes['class'] = attrs.remove('class')

		// set variables in session
		session['ajaxflow'] = [
		    'formName'				: formName,
			'commonTemplatePath'	: (attrs.get('commons')) ? attrs.remove('commons') : '',
			'partialTemplatePath'	: (attrs.get('partials')) ? attrs.remove('partials') : '',
			'plugin'				: (attrs.get('plugin')) ? attrs.remove('plugin') : '',
			'webFlowController'		: (attrs.get('controller')) ? attrs.remove('controller') : [controller: formName, action: 'pages'],
			'spinner'				: spinner
		]

		// render wizard form
		out << form(
			formAttributes,
			(
				((session['ajaxflow']['spinner']) ?
					"""<script type=\"text/javascript\">
var spinnerVisible = false;
function showSpinner() {
	spinnerVisible = true;
	setTimeout("showDelayedSpinner();", 500);
}
function showDelayedSpinner() {
	if (spinnerVisible) {
		\$('div#$spinner').show(400);
	}
}
function hideSpinner() {
	spinnerVisible = false;
	\$('div#$spinner').hide(200);
}
</script>"""
					: "") +
				"\n\t<input type=\"hidden\" name=\"id\">\n" +
				"\n\t<div id=\"${formName}Page\">\n" +
				body() +
				"\t</div>\n"
			)
		)
	}

	/**
	 * Render the error div
	 *
	 * @param Map		attributes
	 * @param Closure	body
	 */
	def error = { attrs, body ->
		out << "\t<div id=\"${session['ajaxflow']['formName']}Error\"" + ((attrs.get('class')) ? " class=\"" + attrs.remove('class') + "\"" : '') + ">\n"
		out << body()
		out << "\t</div>"
	}

	/**
	 * ajaxButton tag, this is a modified version of the default
	 * grails submitToRemote tag to work with grails webflows.
	 * Usage is identical to submitToRemote with the only exception
	 * that a 'name' form element attribute is required. E.g.
	 * <wizard:ajaxButton name="myAction" value="myButton ... />
	 *
	 * you can also provide a javascript function to execute after
	 * success. This behaviour differs from the default 'after'
	 * action which always fires after a button press...
	 *
	 * @link http://blog.osx.eu/2010/01/18/ajaxifying-a-grails-webflow/
	 * @link http://www.grails.org/WebFlow
	 * @link http://www.grails.org/Tag+-+submitToRemote
	 * @param Map		attributes
	 * @param Closure	body
	 */
	def ajaxButton = { attrs, body ->
		// get the jQuery version
		def jQueryVersion = grailsApplication.getMetadata()['plugins.jquery']

		// fetch the element name from the attributes
		def elementName = attrs['name'].replaceAll(/ /, "_")

		// javascript function to call after success
		def afterSuccess = attrs['afterSuccess']

		// src parameter?
		def src = attrs['src']
		def alt = attrs['alt']

		// got an id parameter?
		if (attrs.get('id')) {
			// make sure the id is passed to the recieving controller
			attrs['before'] = "\$(\'form#" + session['ajaxflow']['formName'] + " input[name=id]\').val(${attrs.remove('id')});" + ((attrs.get('before')) ? attrs.get('before') : '')
		}

		// set default url map if not specified
		if (!attrs.containsKey('url')) attrs['url'] = session['ajaxflow']['webFlowController'].clone()

		// set default update map if not specified
		if (!attrs.containsKey('update')) attrs['update'] = [success: session['ajaxflow']['formName'] + 'Page', error: session['ajaxflow']['formName'] + 'Error']

		// add support for a spinner :: part 1
		if (session['ajaxflow']['spinner']) {
			attrs['onFailure'] = 'hideSpinner();' + attrs.get('onFailure')
		}

		// generate a normal submitToRemote button
		def button = submitToRemote(attrs, body)

		/**
		 * as of now (grails 1.2.0 and jQuery 1.3.2.4) the grails webflow does
		 * not properly work with AJAX as the submitToRemote button does not
		 * handle and submit the form properly. In order to support webflows
		 * this method modifies two parts of a 'normal' submitToRemote button:
		 *
		 * 1) replace 'this' with 'this.form' as the 'this' selector in a button
		 *    action refers to the button and / or the action upon that button.
		 *    However, it should point to the form the button is part of as the
		 *    the button should submit the form data.
		 * 2) prepend the button name to the serialized data. The default behaviour
		 *    of submitToRemote is to remove the element name altogether, while
		 *    the grails webflow expects a parameter _eventId_BUTTONNAME to execute
		 *    the appropriate webflow action. Hence, we are going to prepend the
		 *    serialized formdata with an _eventId_BUTTONNAME parameter.
		 */
		if (jQueryVersion =~ /^1.([1|2|3]).(.*)/) {
			// fix for older jQuery plugin versions
			button = button.replaceFirst(/data\:jQuery\(this\)\.serialize\(\)/, "data:\'_eventId_${elementName}=1&\'+jQuery(this.form).serialize()")
		} else {
			// as of jQuery plugin version 1.4.0.1 submitToRemote has been modified and the
			// this.form part has been fixed. Consequently, our wrapper has changed as well...
			button = button.replaceFirst(/data\:jQuery/, "data:\'_eventId_${elementName}=1&\'+jQuery")
		}

		// add an after success function call?
		// usefull for performing actions on success data (hence on refreshed
		// wizard pages, such as attaching tooltips)
		if (afterSuccess) {
			if (session['ajaxflow']['spinner']) {
				button = button.replaceFirst(/\.html\(data\)\;/, '.html(data);hideSpinner();' + afterSuccess + ';')
			} else {
				button = button.replaceFirst(/\.html\(data\)\;/, '.html(data);' + afterSuccess + ';')
			}
		}

		// add support for a spinner :: part two
		if (session['ajaxflow']['spinner']) {
			// insert spinner
			button = button.replaceFirst(/onclick=\"/, 'onclick=\"showSpinner();')

			// change serialize part
			button = button.replaceFirst(
				/jQuery\(this\)\.parents\(\'form\:first\'\)\.serialize\(\)/,
				'\\$(\'form#' + session['ajaxflow']['formName'] + '\').serialize()'
			)
		} else {
			// insert formName
			button = button.replaceFirst(
				/jQuery\(this\)\.parents\(\'form\:first\'\)/,
				'\\$(\'form#' + session['ajaxflow']['formName'] + '\')'
			)
		}

		// got an src parameter?
		if (src) {
			def replace = 'type="image" src="' + src + '"'

			if (alt) replace = replace + ' alt="' + alt + '"'

			button = button.replaceFirst(/type="button"/, replace)
		}

		// replace double semi colons
		button = button.replaceAll(/;{2,}/, ';')

		// render button
		out << button
	}

	/**
	 * generate an ajax submit JavaScript
	 * 
	 * @link WizardTagLib::ajaxFlowRedirect
	 * @link WizardTagLib::baseElement (ajaxSubmitOnChange)
	 */
	def ajaxSubmitJs = { attrs, body ->
		// define AJAX provider
		setProvider([library: ajaxProvider])

		// got a function name?
		def functionName = attrs.remove('functionName')
		if (functionName && !attrs.get('name')) {
			attrs.name = functionName
		}

		// generate an ajax button
		def button = ajaxButton(attrs, body)

		// strip the button part to only leave the Ajax call
		button = button.replaceFirst(/<[^\"]*onclick=\"/, '')
		button = button.replaceFirst(/return false.*/, '')

		// change form if a form attribute is present
		/*
		if (attrs.get('form')) {
            button = button.replace(
				"jQuery(this).parents('form:first')",
				"\$('" + attrs.get('form') + "')"
			)
		}
		"\$('${session['ajaxflow']['formName']}')"
		*/

		// change 'this' if a this attribute is preset
		if (attrs.get('this')) {
			button = button.replace('this', attrs.get('this'))
		}

		out << button
	}

	/**
	 * generate ajax webflow javascript to trigger events
	 *
	 * As we have an Ajaxified webflow, the initial wizard page
	 * cannot contain a wizard form, as upon a failing submit
	 * (e.g. the form data does not validate) the form should be
	 * shown again. However, the Grails webflow then renders the
	 * complete initial wizard page into the success div. As this
	 * ruins the page layout (a page within a page) we want the
	 * initial page to redirect to the first wizard form to enter
	 * the webflow correctly. We do this by emulating an ajax post
	 * call which updates the wizard content with the first wizard
	 * form.
	 *
	 * Usage: <af:redirect form="form#wizardForm" name="next" url="[controller:'wizard',action:'pages']" update="[success:'wizardPage',failure:'wizardError']" afterSuccess="onWizardPage()"/>
	 * form = the form identifier
	 * name = the action to execute in the webflow
	 * update = the divs to update upon success or error
	 *
	 * OR: to generate a JavaScript function you can call yourself, use 'functionName' instead of 'name'
	 *
	 * Example initial webflow action to work with this javascript:
	 * ...
	 * mainPage {
	 * 	render(view: "/wizard/index")
	 * 	onRender {
	 * 		flow.page = 1
	 *  }
	 * 	on("next").to "pageOne"
	 * }
	 * ...
	 *
	 * @param Map attributes
	 * @param Closure body
	 */
	def triggerEvent = { attrs, body ->
		def attributes = [
			form			: "form#${session['ajaxflow']['formName']}",				// use the formName
			name			: ((attrs.get('name')) ? attrs.remove('name') : 'next'),	// default event is 'next'
		   	url				: session['ajaxflow']['webFlowController'].clone(),
			update			: [success: session['ajaxflow']['formName'] + 'Page',failure: session['ajaxflow']['formName'] + 'Error' ],
			afterSuccess	: ((attrs.get('afterSuccess')) ? attrs.remove('afterSuccess') : '')
		]

		// generate javascript
		out << '<script type="text/javascript">'
		out << '$(document).ready(function() {'
		out << ajaxSubmitJs(attributes, body)
		out << '});'
		out << '</script>'
	}

	/**
	 * bind an ajax submit to an onChange event
	 * @param attrs
	 * @return attrs
	 */
	def getAjaxOnChange = { attrs ->
		// work variables
		def internetExplorer	= (request.getHeader("User-Agent") =~ /MSIE/)
		def ajaxOnChange		= attrs.remove('ajaxOnChange')

		// is ajaxOnChange defined
		if (ajaxOnChange) {
			if (!attrs.onChange) attrs.onChange = ''

			// add onChange AjaxSubmit javascript
			if (internetExplorer) {
				// 		- somehow IE submits these onchanges twice which messes up some parts of the wizard
				//		  (especially the events page). In order to bypass this issue I have introduced an
				//		  if statement utilizing the 'before' and 'after' functionality of the submitToRemote
				//		  function. This check expects lastRequestTime to be in the global Javascript scope,
				//		  (@see pageContent) and calculates the time difference in miliseconds between two
				//		  onChange executions. If this is more than 100 miliseconds the request is executed,
				//		  otherwise it will be ignored... --> 20100527 - Jeroen Wesbeek
				attrs.onChange += ajaxSubmitJs(
					[
						before			: "var execute=true;try { var currentTime=new Date().getTime();execute = ((currentTime-lastRequestTime) > 100);lastRequestTime=currentTime;  } catch (e) {};if (execute) { 1",
						after			: "}",
						functionName	: ajaxOnChange,
						url				: session['ajaxflow']['webFlowController'].clone(),
						update			: [success: session['ajaxflow']['formName'] + 'Page',failure: session['ajaxflow']['formName'] + 'Error' ],
						afterSuccess	: ((attrs.get('afterSuccess')) ? attrs.remove('afterSuccess') : '')
					],
					''
				)
			} else {
				// this another W3C browser that actually behaves as expected... damn you IE, DAMN YOU!
				attrs.onChange += ajaxSubmitJs(
					[
						functionName	: ajaxOnChange,
						url				: session['ajaxflow']['webFlowController'].clone(),
						update			: [success: session['ajaxflow']['formName'] + 'Page',failure: session['ajaxflow']['formName'] + 'Error' ],
						afterSuccess	: ((attrs.get('afterSuccess')) ? attrs.remove('afterSuccess') : '')
					],
					''
				)
			}
		}

		return attrs
	}

	/**
	 * render the page template
	 *
	 * Example:
	 * <af:page>the body of my page / partial</af:page>
	 *
	 * @param Map attributes
	 * @param Closure body
	 */
	def page = { attrs, body ->
		// define AJAX provider
		setProvider([library: ajaxProvider])

		// was the commonTemplatePath defined in the
		// af:wizard tag ?
		if (session['ajaxflow']['commonTemplatePath']) {
			// yes, render a wizard page
			if (session['ajaxflow']['plugin']) {
				out << render([template: session['ajaxflow']['commonTemplatePath'] + '/page_header', plugin: session['ajaxflow']['plugin'] ])
				out << body()
				out << render([template: session['ajaxflow']['commonTemplatePath'] + '/page_footer', plugin: session['ajaxflow']['plugin'] ])
			} else {
				out << render([template: session['ajaxflow']['commonTemplatePath'] + '/page_header' ])
				out << body()
				out << render([template: session['ajaxflow']['commonTemplatePath'] + '/page_footer' ])
			}
		} else {
			// render with a warning
			out << "[_page_header.gsp is not available]<br/>"
			out << body() + "<br/>"
			out << "[_page_footer.gsp is not available]<br/>"
		}
	}

	/**
	 * render navigation button(s)
	 *
	 * Example:
	 * <af:navigation events="[previous:[label:'&laquo; prev',show: showPrevious], cancel:[label:'cancel', show: showCancel], quicksave:[label:'quicksave', show: showQuickSave], next:[label:'next &raquo;', show:showNext]]" separator="&nbsp; | &nbsp;" class="prevnext" />
	 * <af:navigation events=[MAP]" separator="&nbsp; | &nbsp;" class="prevnext" />
	 *
	 * The events map should contain the specification of
	 * what buttons to generate. An example
	 * 		[
	 * 			previous:[label:'&laquo; previous', show: false],
	 * 			next:[label:'next &raquo;', show: true],
	 * 			mySpecialButton:[]
	 * 		]
	 *
	 * @param Map attributes
	 */
	def navigation = { attrs ->
		def outputSent		= false

		out << "<div class=\"navigation\">"
		// iterate through navigational items
		attrs.get('events').each { action, data ->
			// cast to LinkedHashMap
			data = data as LinkedHashMap

			// check if we need to show this action
			if ((data.containsKey("show") && data.show) || !data.containsKey("show")) {
				// render separator
				if (attrs.get('separator') && outputSent) out << attrs.get('separator')

				// render button
				out << ajaxButton([
					name		: action,
					value		: ((data?.label) ? data.label : action),
					url			: attrs.get('url') ? attrs.get('url') : session['ajaxflow']['webFlowController'].clone(),
					update		: attrs.get('update') ? attrs.get('update') : [success: session['ajaxflow']['formName'] + 'Page', error: session['ajaxflow']['formName'] + 'Error'],
					class		: attrs.get('class'),
					afterSuccess: attrs.get('afterSuccess') ? attrs.get('afterSuccess') : 'onPage();'
				])

				// mark output sent
				outputSent = true
			}
		}
		out << "</div>"
	}

	/**
	 * render tabs based on a Map and an integer
	 *
	 * attrs.pages	Map
	 * attrs.page	int
	 *
	 * Generally these two variables would be set by
	 * the flow controller in the flow scope.
	 *
	 * Example:
	 * 		flow.page = 0
	 *		flow.pages = [
	 *			[title: 'Page One'],
	 *			[title: 'Page Two'],
	 *			[title: 'Page Three'],
	 *			[title: 'Page Four'],
	 *			[title: 'Done']
	 *		]
	 *
	 * And in the view you would then use:
	 * 		<af:tabs pages="${pages}" page="${page}" />
	 *
	 * @param Map attributes
	 */	
	def tabs = { attrs ->
		def pages		= attrs.get('pages') ? attrs.get('pages') : [:]
		def page		= attrs.get('page') ? attrs.get('page') as int : 1
		def clickable	= attrs.get('clickable') ? attrs.get('clickable') as boolean : false
		def i			= 1

		out << "<div class=\"tabs\">"
		out << " <ul>"
		pages.each { item ->
			out << "  <li" + ((i == page) ? " class=\"active\"" : '') + ">"
			out << "   <span class=\"content\">"
			if (clickable && i != page && page < (pages.size())) {
				// render a clickable label which will result
				// in a 'toPageX' event in the controller
				out << ajaxButton([
					name		: 'toPage' + intToString( i ),
					value		: "${i}. ${item.title}",
					url			: attrs.get('url') ? attrs.get('url') : session['ajaxflow']['webFlowController'].clone(),
					update		: attrs.get('update') ? attrs.get('update') : [success: session['ajaxflow']['formName'] + 'Page', error: session['ajaxflow']['formName'] + 'Error'],
					afterSuccess: attrs.get('afterSuccess') ? attrs.get('afterSuccess') : 'onPage();'
				])
			} else {
				// just render an unclickable label
				out << "   ${i}. ${item.title}"
			}
			out << "   </span>"
			out << "  </li>"
			i++
		}
		out << " </ul>"
		out << "</div>"
	}

	/**
	 * return a camelcased name of a certain number below 100
	 *
 	 * @param	integer number
	 * @return  string
	 */
	final private intToString( int number ) {
		if (number < 20) {
			return numbers[ "${number}" ]
		} else {
			def decimal	= Math.floor(number / 10) as int
			def num		= number - ( decimal * 10 )

			return numbers[ "${decimal}x" ] + numbers[ "${num}" ]
		}
	}
}