


# Simple Survey
[![license](https://img.shields.io/github/license/mashape/apistatus.svg)](https://opensource.org/licenses/MIT)

Library for simple survey creation.
This library has as main dependency the library [AppIntro](https://github.com/AppIntro/AppIntro) _(An excellent library for you to create introductions to your application)_.

**Simple Survey**, only provides an easy way to create forms with pre-defined question types.

## Features
- Support for the following question types:
	- Dichotomic choice (e.g. example YES/NO)
	- Single choice
	- Multiple choose
	- Open question
	- Date choice
	- Time choice
- Presentation page
- Question blocked until response is obtained.

## Installation
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

```
dependencies {
	 implementation 'com.github.nutes-uepb:simple-survey:v1.2.1'
}
```

## Using

#### [See example of use](https://github.com/nutes-uepb/simple-survey/blob/master/app/src/main/java/br/edu/uepb/nutes/simplesurvey/SimpleSurvey1.java)

- To start using the library it is necessary to inherit from the ** SimpleSurvey ** class and implement the desired interfaces:

**Example:** 
```
public class SimpleSurvey1 extends SimpleSurvey implements Infor.OnInfoListener,Dichotomic.OnDichotomicListener, Single.OnSingleListener,Date.OnDateListener, Time.OnTimeListener { }
```

- Using the `initView(){}` method You start the Layouts that will be used in your project.

**Example:** 

    protected void initView() {  
	    //  
	}
		
- We have five types of animations that are displayed in the layout change:

>setFadeAnimation()  
>setZoomAnimation()  
>setFlowAnimation()  
>setSlideOverAnimation()  
>setDepthAnimation()

 **Creating a Layout:**
- To start a layout, use the method `addQuestion()` , from it it is possible to opt for six `layouts` pre defined with some configurations that can vary:

>Dichotomic choice (e.g. example YES/NO)
>Single choice
>Multiple choose
>Open question
>Date choice
>Time choice
	
- From `addQuestion ()` a new layout `(new Dichotomic)` is defined and added `(new Dichotomic.Config (). )` Settings according to the desired functionality.


**Some settings available:** 
|                |                          |                        |
|----------------|-------------------------------|-----------------------------|
|.title() |define a title for your page and followed by a color for the letter|
|.colorBackground() |set the background color used|
|.buttonClose() |you can put a specific image for the close button|
|.inputBackground()|defines an image for the text input field |
|.inputColorText()|defines the color used in the text input field|
|.inputType() |defines a type* of input text|
|.pageNumber()|sets the page number of the layout|
|.descriptionTextSize()|allows you to set a size for the text font|
|.image()|allows you to place an image in the center of the layout|

**Example:** 

    addQuestion(new Open.Config()  
        .title("Title of the question 2", Color.WHITE)  
        .colorBackground(ContextCompat.getColor(this, R.color.colorDeepPurple))  
        .buttonClose(R.drawable.ic_action_close_dark)  
        .inputBackground(R.drawable.edittext_border_style)  
        .inputColorText(Color.WHITE)
        .inputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_FLAG_CAP_WORDS)  
        .pageNumber(2)  
        .build());
