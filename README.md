# Ajaxflow
This plugin enables [ajaxified webflows](http://blog.osx.eu/2010/01/18/ajaxifying-a-grails-webflow/) and extends the webflow functionality to allow rendering of partials. In general you can define the ajaxflow definition like any other regular webflow definition (see the webflow documentation), but on the client-side (the view) everything will work through Ajax requests rendering partials (or pages). This plugin specifically aims at making wizard-like webflows easy to accomplish.

To quickly get started without diving too deep into the tags below, you can just generate a new ajaxflow within your project using the following command:

```
grails create-ajaxflow my.package.name wizard
```

This will create a ajaxflow called 'wizard' which is working and ready for customization at http://localhost:8080/myProject/wizard
The command will set up an initial five page ajaxflow containing an initial controller, commons views, partial views, images and css.

![example1](https://dl.dropbox.com/s/s60c6pycmiwh6nj/ajaxflow-screenshot.png?dl=1)

It's easiest to first perform a svn update command before injecting a new ajaxflow as it allows you to easily remove the ajaxflow by reverting your project.

*If you intend to create more than one ajaxflow in your project it's probably best to group images and css together. Currently every new ajaxflow will have it's own images and css. Grouping them can be easily done by
* renaming the css to a generic name (e.g. web-app/css/ajaxflow.css)
* moving the images into a generic folder (e.g. web-app/images/ajaxflow/*)
* changing the image references in the css to the new image directory
* changing the css include in the index.gsp view*

## Note on Grails 2.2.0
If you are using Grails 2.2.0, there is an issue with resolving and installing the webflow plugin (on which ajaxflow depends). 
You need to add the webflow dependency as follows to your BuildConfig.groovy in order for it to work:

```groovy
    dependencies {
        compile "org.grails:grails-webflow:2.2.0"
    }

    plugins {
        ...
        compile ":webflow:2.0.0", {
            exclude 'grails-webflow'
        }
    }
```
More information can be found in the [Grails ticket](http://jira.grails.org/browse/GRAILS-9781#comment-73859).


## Differences
Ajaxflows and rendering partials is different from generic webflows because the browser will not render full HTML pages but will only render small sections within the page. This means that when you want to bind handlers to DOM element, you need to do that every time a partial page is rendered and -hence- after every ajax request. This can be done by using the ```afterSuccess``` parameter for ```af:triggerEvent``` , ```af:ajaxSubmitJs``` , ```af:ajaxButton``` or ```af:navigation``` tags. When not specified, it will always default to ```onPage();``` so make sure that Javascript function is available.

Example JS:
```js
<script type="text/javascript">
// bind handlers on document ready
$(document).ready(function() {
    doSomethingImportant();
});

// do some really important stuff like bind DOM event handlers
function doSomethingImportant() {
    console.log('i am so important');

    // initialize jquery-ui accordeon(s)
    $("#accordion").accordion({autoHeight: false});    
}

// define the onPage function to be used in the ajaxflows
function onPage() {
   console.log('i am called after each ajax call');
   doSomethingImportant();
}
</script>
```

The ```onPage()``` will be called on every ajax call (except if you defined another function call using the ```afterSuccess``` parameter). Also see the common/_on_page.gsp view in the generated ajaxflow, if you used the ```grails create-ajaxflow``` command.

## The af:flow tag
This tag is used to set up an ajaxflow. 

```rhtml
<af:flow name="test" class="ajaxFlow" commons="common" partials="pages" controller="[controller: 'test', action: 'pages']">
	<af:triggerEvent name="next" afterSuccess="onPage();" />
</af:flow>
```

The initial ajaxflow template only renders the ```af:triggerEvent``` tag (see below). This is done because when using ajax the partials / pages need to be rendered using an ajax request. While the initial page _could_ be rendered when starting the ajaxflow, this would mean that the webflow definition should contain duplicate logic: one for setting up the webflow, and one for the 'first partial'. This is needed because one cannot browse _back_ to the initial page as that is not a partial page, but a complete page. To easily solve this issue the ```af:triggerEvent``` just triggers a _next_ event in the webflow specification to render the initial page / partial.

|*parameter* | *type* | *description* | *example* | *optional* | *default* |
|------------|--------|---------------|-----------|------------|----------:|
| name | String  | the name of the ajaxflow | test | yes | wizard |
| class | String  | the class parameter given to the ajaxflow form | ajaxFlow | yes | - |
| commons | String | the relative path where the common views are stored | common | required | n/a |
| partials | String | the relative path where the partial (=page) views are stored | pages | required | n/a |
| controller | Map | the controller and webflow definition | \[controller: 'test', action: 'pages'\] | yes | \[controller: ajaxFlowName, action: 'pages'\] |


## The af:page tag
The ```af:page``` tag is used to encapsulate a partial / page view and does nothing more than render a page header, the page content and a page footer. It expects the following views to be available:

```
common/_page_header.gsp
common/_page_footer.gsp
```

Where the _common_ path was specified by the _commons_ parameter of the ```af:flow``` tag. Things one would expect in the page header and footer are for example:
* tabs / breadcrumbs
* navigation
* generic error handling
* etcetera

Example:
```rhtml
<af:page>
<h1>Page one of the '<i>test</i>' ajax flow</h1>
<p>
	Lorem ipsum dolor sit amet...
</p>
</af:page>
```

*This tag takes no parameters

## The af:error tag
The ```af:error``` tag is designed to be used together with the ```af:flow``` tag. Where the ```af:flow``` tag is used to set up the ajaxflow and will contain the partials / pages, the ```af:error``` tag will render a div element which will be updated with any ajax errors that occur. Generally this could be removed in a production environment but it may be wise or handy to keep this visible while in development.

Simple example:
```rhtml
<af:error class="ajaxFlowError">
	[ajax errors go in here, normally it's safe to delete the af:error part]
</af:error>
```

Complete example:
```rhtml
<div id="ajaxflow">
<af:flow name="test" class="ajaxFlow" commons="common" partials="pages" controller="[controller: 'test', action: 'pages']">
	<af:triggerEvent name="next" afterSuccess="onPage();" />
</af:flow>
<g:if env="development">
<af:error class="ajaxFlowError">
	[ajax errors go in here, normally it's safe to delete the af:error part]
</af:error>
</g:if>
</div>
```

| *parameter* | *type* | *description* | *example* | *optional* | *default* |
|-------------|--------|---------------|-----------|------------|----------:|
| class | String  | the class parameter given to the error div | ajaxFlowError | no | the name of the ajaxFlow + Error appended |

## The af:tabs tag
The ```af:tabs``` tag will dynamically render a navigational element based on a map containing all pages in the ajaxflow, and the current page. This is easily accomplished by setting up the following two variables in the flowscope:

```js
	/**
	 * WebFlow definition
	 * @void
	 */
	def pagesFlow = {
		// start the flow
		onStart {
			// define variables in the flow scope which is availabe
			// throughout the complete webflow also have a look at
			// the Flow Scopes section on http://www.grails.org/WebFlow
			//
			// The following flow scope variables are used to generate
			// wizard tabs. Also see common/_tabs.gsp for more information
			flow.page = 0
			flow.pages = [
				[title: 'Page One'],
				[title: 'Page Two'],
				[title: 'Page Three'],
				[title: 'Page Four'],
				[title: 'Done']
			]
			...

			success()
		}
                ...
	}
```

You could then update the ```page``` variable in every page / partial:
```js
		// second wizard page
		pageTwo {
			render(view: "_page_two")
			onRender {
				flow.page = 2
				success()
			}
			on("next").to "pageThree"
			on("previous").to "pageOne"
		}
```

And use the following generic code to render the tabs:

```rhtml
<af:tabs pages="${pages}" page="${page}" clickable="${true}" /> 
```

The ```clickable="true"``` parameter makes the tabs / breadcrumbs clickable an result in triggering events in your webflow definition like toPageOne, toPageTwo, toPageThree, etc... However, enabling the clickable parameter would mean every part of your webflow should be able to handle jumps to other pages / partials and this might not always be possible (pages relying on input on a previous page).

In the quick setup (grails create-ajaxflow my.package name) the tabs will look like this:

![example2](https://dl.dropbox.com/s/g035rjss5ei9l6y/ajaxflow-tabs.png?dl=1)

## The af:ajaxButton tag
As regular buttons are not possible from within an ajaxflow, the ```af:ajaxButton``` is the replacement tag to create buttons. They work similar to buttons in a normal webflow in the way that they also trigger a particular event in an ajaxflow's webflow definition.

```rhtml
<af:ajaxButton name="toPageFour" value="to page 4!" afterSuccess="onPage();" class="prevnext" />
```

Will render a button which will trigger the ```toPageFour``` event in the webflow definition:

```rhtml
		// second wizard page
		pageTwo {
			render(view: "_page_two")
			onRender {
				flow.page = 2
				success()
			}
			on("next").to "pageThree"
			on("previous").to "pageOne"
			on("toPageFour").to "pageFour"
		}
```

As of version 0.1.18 it is also possible to pass an id with the button by adding the 'id' parameter:

```rhtml
<af:ajaxButton name="toPageFour" value="to page 4!" id="1234" afterSuccess="onPage();" class="prevnext" />
```

The id will be received by the controller, and can be used in your controller logic.

| *parameter* | *type* | *description* | *example* | *optional* | *default* |
|-------------|--------|---------------|-----------|------------|----------:|
| name | string | the button name _and_ the event to call | "next" | required | - |
| value | string | the label of the button | "Next" | required | - |
| class | string | the css class the rendered buttons should have | "prevnext" | yes | -|
| afterSuccess | string | the JS to execute after a successful ajax call | "doSomething();" | yes | "onPage()"|
| src | string | an image url to use for button | ../images/button.gif | yes | -|
| alt | string | the alt tag to use with the button image | "my alt text" | yes | -|
| id | mixed | some data to pass to the controller | ${id} | yes | -|

## The af:navigation tag
The ```af:navigation``` tag will render a set of ajaxButtons resulting into diferent events in the webflow definition. The map contains a number of buttons, button labels, events to trigger and wether or not to show the particular button ( ```show```: boolean). The map should look as follows:
```
[
  event1: [
    label: 'The button label',
    show: true/false
  ],
  ...
  eventN: [
    label: 'The button label',
    show: true/false
  ]
]
```

Example:
```
<g:set var="showPrevious" value="${page>1 && page<pages.size}"/>
<g:set var="showNext" value="${page<pages.size}"/>
<af:navigation events="[previous:[label:'&laquo; prev',show: showPrevious], next:[label:'next &raquo;', show:showNext]]" separator="&nbsp; | &nbsp;" class="prevnext" />
```

![example2](http://grails.org/wikiImage/description-761/Screen_shot_2010-12-08_at_5.43.02_PM.png)

| *parameter* | *type* | *description* | *example* | *optional* | *default* |
|-------------|--------|---------------|-----------|------------|----------:|
| events | map | the navigational buttons to generate | \[next:\[label:'next &raquo;', show:showNext\]\] | required | n/a|
| seperator | string | the seperator between the buttons | "&nbsp; \| &nbsp;" | -|
| class | string | the css class the rendered buttons should have | "prevnext" | yes | -|
| afterSuccess | string | the JS to execute after a successful ajax call | "doSomething();" | yes | "onPage()"|

## The af:ajaxSubmitJs tag
The ```af:ajaxSubmitJs``` tag is actually an ```ajaxButton``` without button. It results in pure Javascript which you can use to create your own custom triggers. For example it is possible to bind an onChange handler to select elements automatically performing an ajax submit, or -how I use it in a project- trigger a ```refresh``` event in the webflow definition upon closing an opened jquery-ui dialog.

```html
<script type="text/javascript">
function refreshFlow() {
	<af:ajaxSubmitJs name="refresh" afterSuccess="onPage()" />
}
</script>
```

See the ajaxButton tag for more information on the supported parameters.

## The af:triggerEvent tag
The ```af:triggerEvent``` tag is basically a wrapper for the ```af:ajaxSubmit``` tag to immediately trigger the specific event in the webflow definition. This tag is mainly used when setting up a webflow to trigger an ajax event to load the initial partial page.

```rhtml
<div id="ajaxflow">
<af:flow name="test" class="ajaxFlow" commons="common" partials="pages" controller="[controller: 'test', action: 'pages']">
	<%	/**
	 	 * The initial rendering of this template will result
	 	 * in automatically triggering the 'next' event in
	 	 * the webflow. This is required to render the initial
	 	 * page / partial and done by using af:triggerEvent
		 */ %>
	<af:triggerEvent name="next" afterSuccess="onPage();" />
</af:flow>
<g:if env="development">
<af:error class="ajaxFlowError">
	[ajax errors go in here, normally it's safe to delete the af:error part]
</af:error>
</g:if>
</div>
<g:render template="common/on_page"/>
```
