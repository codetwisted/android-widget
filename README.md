# android-widget
Collection of different Android widgets that should simplify developer's life a little.

For now, it consists of:
* Drawer Layout
* BlurFrame Layout

Hopefully it will have additions soon.

## Drawer Layout
Drawer pattern implemented as a layout that has a content and a drawer with handle and can be snapped to any side of the screen.  
Each part of it (content, drawer and handle) can have any kind of content that fits into ViewGroup.

<img src="/images/drawerlayout-demo.gif" width="320" height="568"></img>

### How to:

##### 1. Gradle
Add Gradle dependency:
```
compile 'org.codetwisted.android-widget:drawerlayout:1.0'
```

##### 2. XML
Make DrawerLayout your most parent layout and mark each part of it with `nodeType` attribute.  
Specify it as `drawerContent` `drawerHandle` or `defaultNode`.  
* `drawerContent` specifies everything that should be placed inside come out drawer
* `drawerHandle` registers the grip that user can swipe or press to make drawerContent come in and out.
* `defaultNode` marks everything else i.e. all content that should be present in drawer-closed mode. Everything without `nodeType` specified is treated like `defaultNode`.

See pictures below for better understanding:  

<img src="/images/drawerlayout-drawerContent.png" width="280" height="498"></img>&nbsp;&nbsp;<img src="/images/drawerlayout-drawerHandle.png" width="280" height="498"></img>&nbsp;&nbsp;<img src="/images/drawerlayout-defaultNodes.png" width="280" height="498"></img>

You can also specify additional attributes:
* `android:gravity` - identifies the side to snap. `left` by default
* `drawerOffset` - set positive integer for drawer to stand out a little. `0` by default
* `animationTime` - set positive integer to specify slide animation duration, can't be less than `android.R.integer.config_shortAnimTime` and set to that by default
* `touchEnable` - defines can drawer be swiped in and out or not. `true` by default
* `seizeContent` - indicates should drawer content process swipes together with handler for drawer opening and closing or not. `false` by default

Example xml:
```xml
<org.codetwisted.widget.DrawerLayout
    ...
	android:id="@+id/drawer_layout"
	android:gravity="top">

	<LinearLayout ...>
		<Button .../>
		<EditText .../>
        ...
	</LinearLayout>

	<RelativeLayout ...
		app:nodeType="drawerContent">
		<ProgressBar .../>
		<TextView .../>
        ...
	</RelativeLayout>

	<View ...
		app:nodeType="drawerHandle"/>

</org.codetwisted.widget.DrawerLayout>
```

##### 3. Code
For more control over Drawer Layout implement `DrawerLayout.Listener`.  
It provides callbacks for `onDrawerOpened`, `onDrawerClosed` and `onDrawerSliding` situations.  
There are also methods to programmatically change all xml attributes that were described above (`setAnimationDuration`, `setGravity` etc.)  
It is also possible to open and close drawer from code like this:
```Java
drawerLayout.setDrawerOpen(true, true)
```
First parameter is responsible for desired state i.e. opened or closed (in example above the drawerLayout will be opened).  
Second one is used to determine does animation is needed (in above drawerLayout opening will be animated).

[//]: # (blurframelayout should go here)

## License
```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
