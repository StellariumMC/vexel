# Elements
Elements are highly function objects that can have custom events and store values. Elements are built from components.

## Default Elements
#### Button
Clickable button with hover states:

```kotlin
Button("Submit")
    .backgroundColor(0xFF2196F3.toInt())
    .hoverColors(bg = 0xFF1E88E5.toInt())
    .padding(12f, 24f, 12f, 24f)
    .onClick { _, _, _ ->
        // Handle click
        true
    }
    .setSizing(Size.Auto, Size.Auto)
    .childOf(parent)
```

#### TextInput
Text input field with selection support:

```kotlin
TextInput()
    .placeholder("Enter text...")
    .fontSize(14f)
    .setSizing(200f, Size.Pixels, Size.Auto, Size.Auto)
    .onValueChange { value ->
        println("Text changed: $value")
    }
    .childOf(parent)
```

#### NumberInput
Numeric input with validation:

```kotlin
NumberInput(initialValue = 42)
    .allowDecimals(true)
    .allowNegative(true)
    .placeholder("Enter number...")
    .setSizing(150f, Size.Pixels, Size.Auto, Size.Auto)
    .onValueChange { value ->
        println("Number: $value")
    }
    .childOf(parent)
```

#### Slider
Value slider with customization:

```kotlin
Slider(value = 0.5f, minValue = 0f, maxValue = 1f)
    .step(0.1f)
    .thumbColor(0xFFFFFFFF.toInt())
    .trackFillColor(0xFF2196F3.toInt())
    .setSizing(200f, Size.Pixels, 30f, Size.Pixels)
    .onValueChange { value ->
        println("Slider value: $value")
    }
    .childOf(parent)
```

#### CheckBox
Boolean checkbox with animations:

```kotlin
CheckBox()
    .setChecked(false)
    .setSizing(20f, Size.Pixels, 20f, Size.Pixels)
    .onValueChange { checked ->
        println("Checkbox: $checked")
    }
    .childOf(parent)
```

#### Switch
Toggle switch component:

```kotlin
Switch()
    .trackEnabledColor(0xFF2196F3.toInt())
    .setEnabled(false)
    .setSizing(50f, Size.Pixels, 26f, Size.Pixels)
    .onValueChange { enabled ->
        println("Switch: $enabled")
    }
    .childOf(parent)
```

#### Dropdown
Selection dropdown with options:

```kotlin
Dropdown(options = listOf("Option 1", "Option 2", "Option 3"))
    .fontSize(14f)
    .setSizing(180f, Size.Pixels, Size.Auto, Size.Auto)
    .onValueChange { index ->
        println("Selected: $index")
    }
    .childOf(parent)
```

#### ColorPicker
Color selection interface:

```kotlin
ColorPicker(initialColor = Color.WHITE)
    .setSizing(30f, Size.Pixels, 20f, Size.Pixels)
    .onValueChange { color ->
        println("Color: ${color.hex()}")
    }
    .childOf(parent)
```

#### Keybind
Key binding input:

```kotlin
Keybind()
    .setSizing(100f, Size.Pixels, Size.Auto, Size.Auto)
    .onValueChange { keyCode ->
        println("Key bound: $keyCode")
    }
    .childOf(parent)
```

## Custom Elements
This is an example of how elements are made, they are composed of base components but are more advanced.
```kotlin
class CheckBox(
    var checkmarkColor: Int = 0xFF4c87f9.toInt(),
    var disabledBackgroundColor: Int = 0xFF303030.toInt(),
    var enabledBackgroundColor: Int = 0xFF212121.toInt(),
    var backgroundHoverColor: Int = 0xFF424242.toInt(),
    var backgroundPressedColor: Int = 0xFF1B1B1B.toInt(),
    backgroundColor: Int = 0x80404040.toInt(),
    borderColor: Int = 0xFF606060.toInt(),
    borderRadius: Float = 4f,
    borderThickness: Float = 1f,
    padding: FloatArray = floatArrayOf(4f, 4f, 4f, 4f),
    hoverColor: Int? = 0x80505050.toInt(),
    pressedColor: Int? = 0x80303030.toInt(),
    widthType: Size = Size.Pixels,
    heightType: Size = Size.Pixels
) : VexelElement<CheckBox>(widthType, heightType) {
    var checked: Boolean = false

    private val background = Rectangle(
        backgroundColor,
        borderColor,
        borderRadius,
        borderThickness,
        padding,
        hoverColor,
        pressedColor,
        Size.ParentPerc,
        Size.ParentPerc
    )
    .setSizing(100f, Size.ParentPerc, 100f, Size.ParentPerc)
    .ignoreMouseEvents()
    .childOf(this)

    val checkMark = SvgImage(
            svgPath = "/assets/vexel/checkmark.svg",
            color = Color(checkmarkColor and 0x00FFFFFF,true)
        )
        .setSizing(120f, Size.ParentPerc, 120f, Size.ParentPerc)
        .setPositioning(0f, Pos.ParentCenter, 0f, Pos.ParentCenter)
        .childOf(background)

    init {
        setSizing(20f, Size.Pixels, 20f, Size.Pixels)
        setPositioning(Pos.ParentPixels, Pos.ParentPixels)
        onClick { _, _, _ ->
            setChecked(!checked, animated = true)
            true
        }
    }

    fun setChecked(
        value: Boolean,
        animated: Boolean = true,
        silent: Boolean = false
    ) {
        checked = value
        if (!checked) {
            if (animated) {
                checkMark.color = Color(checkmarkColor, true)
                checkMark.fadeOut(100, EasingType.EASE_IN)
            }
        } else if (animated) {
            checkMark.fadeIn(100, EasingType.EASE_IN)
        }
        if (!silent) onValueChange?.invoke(checked)
    }

    private fun updateBackgroundColor() {
        val newColor = when {
            !checked -> disabledBackgroundColor
            pressed -> backgroundPressedColor
            hovered -> backgroundHoverColor
            else -> enabledBackgroundColor
        }
        background.backgroundColor(newColor)
    }

    override fun onRender(mouseX: Float, mouseY: Float) {
        background.isHovered = hovered
        background.isPressed = pressed
        updateBackgroundColor()
    }
}
```
