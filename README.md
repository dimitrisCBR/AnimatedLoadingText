# AnimatedLoadingText

[![License](https://img.shields.io/badge/license-MIT-blue)](https://opensource.org/licenses/MIT)
[![API](https://img.shields.io/badge/API-16%2B-lightgrey.svg?style=flat)]()

<img src="/files/final_loading.gif" alt="sample" title="sample" vspace="32" hspace="16"/>

A simple library for creating animated loading texts. The animation reacts to the phone's tilt.


## Try it out:

The library is available from Jitpack.io. Add this to your top-level `build.gradle`:
``` gradle
 allprojects {
        repositories {
            jcenter()
            maven { url "https://jitpack.io" }
        }
   }
```
And this to your module's `build.gradle`:

``` gradle
dependencies {
  implementation 'com.github.dimitrisCBR:AnimatedLoadingText:1.0.0'
}
```

## Layout:

```xml
    <com.cbr.labs.alt.AnimatedLoadingTextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:alv_strokeColor="#A4A0A0"
        app:alv_strokeGap="4dp"
        app:alv_strokeWidth="8dp"
        app:alv_text="Loading"
        app:alv_textColor="#7D7676" 
        app:alv_textSize="40sp" />
```




The library supports the following attributes:




| First Header  | Second Header |
| ------------- | ------------- |
| `app:alv_strokeColor`  | color |
| `app:alv_strokeGap`   | dimension |
| `app:alv_strokeWidth`   | dimension  |
| `app:alv_text`   | string |
| `app:alv_textColor`   | color |
| `app:alv_textSize`   | dimension |
