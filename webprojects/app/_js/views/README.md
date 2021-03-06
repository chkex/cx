All the files defined here are views.  Views should be stored as fields of
mvc.views.  Each view takes up an entire page, and never are two views being
displayed at the same time.  Views must implement the following interface:


* `viewName` - Automatic  
This is set by `mvc.init()`.  There is no need to set it in the view's file -
it will be overwritten.  The following test passes:
```
	for(v in mvc.views)
		assert(mvc.views[v].viewName == v);
```

* `title` - Required.  
The full name of the view, with proper capitalization and spaces

* `build($trgt, oldView)` - Optional, but highly recommended.  
This function is called every time the view is navigated to.  Its job is to
populate the element `$trgt` with the views content.  `$trgt` may not be
empty, but it should not contain any visible elements.  These non-visible
elements cannot be removed.  `oldView` is the view which was being used view
which was being used before. 

* `unbuild($trgt, newView)` - Optional, but highly recommended.  
This function is called every time the view is navigated away from.  Its job
is to empty the `$trgt` of visible elements, or just make them invisible.
Currently non-visible elements cannot be removed.  `newView` is the view to
be navigated to.

* `redirect(prevView)` - Optional.  
Either returns null or another view.  If it returns another view, that view
is navigated to in place of this one.  If not defined, equivalent to always
returning null

* `nextView()` - Optional.  
Returns the view which should be navigated to when the user presses the
confirm button.  If null is returned, no navigation occurs.  An alert should
generally be displayed if this happens explaining to the user why he/she was
not allowed to proceed.  If not defined equivalent to always returning null.

* `onResize(fSz)` - Optional.  
If this view is active, called wehen the window resizes.  fSz == font size
