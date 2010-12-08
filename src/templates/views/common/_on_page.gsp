$openTag
/**
 * wizard refresh flow action
 *
 * When a page (/ partial) is rendered, any DOM event handlers need to be
 * (re-)attached. The af:ajaxButton, af:ajaxSubmitJs and af:redirect tags
 * supports calling a JavaScript after the page has been rendered by passing
 * the 'afterSuccess' argument.
 *
 * Example:	af:redirect afterSuccess="onPage();"
 * 		af:redirect afterSuccess="console.log('redirecting...');"
 *
 * Generally one would expect this code to add jQuery event handlers to
 * DOM objects in the rendered page (/ partial).
 *
 * @author Jeroen Wesbeek
 * @since  $today
 *
 * Revision information:
 * $Rev$
 * $Author$
 * $Date$
 */
$closeTag
<script type="text/javascript">
        function onPage() {
                if (console) {
                        console.log('calling onPage() which can be used to attach generic javascript handlers to DOM elements of a rendered page / partial');
                }
        }
</script>

