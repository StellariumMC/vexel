# Vexel

Vexel is a modern, declarative GUI library for Minecraft mods that simplifies interface creation. Built on top of NanoVG, it offers both high-level declarative components and low-level rendering capabilities. Heavily inspired by Elementa, Vexel provides a clean API for creating responsive, animated user interfaces.

Have questions? Join our [discord!](https://discord.gg/QHFWnydFXr)

## Features

- **Declarative UI**: Build interfaces by describing what you want, not how to position it
- **Layout System**: Position and sizing with automatic calculations
- **Animation Support**: Built-in animations with presets
- **Component Library**: Pre-built elements like buttons, inputs, sliders, and more
- **NanoVG Integration**: Direct access to powerful vector graphics rendering

## Installation

Add Vexel to your `build.gradle.kts`:

```kotlin
dependencies {
    // For Fabric 1.21.9
    modImplementation(include("xyz.meowing:vexel-1.21.9-fabric:1.0.5")!!)
    
    // For Fabric 1.21.7
    modImplementation(include("xyz.meowing:vexel-1.21.7-fabric:1.0.5")!!)
    
    // For Fabric 1.21.5
    modImplementation(include("xyz.meowing:vexel-1.21.5-fabric:1.0.5")!!)

    // For Forge 1.8.9
    shadowImpl("xyz.meowing:vexel-1.8.9-forge:1.0.5")
    
    // ONLY FORGE 1.8.9 - In your ModInitializer class, call Vexel.init() like:
    class MyMod {
        @Mod.EventHandler
        fun init(event: FMLInitializationEvent) {
            Vexel.init()
            /* other code */
        }
    }
    
    // It is highly recommended that you use Deftu's Gradle Toolkit but if you use any other GT,
    // you may also need to add the following to your build script's repositories:
    repositories {
        maven("https://maven.deftu.dev/snapshots")
        maven("https://maven.deftu.dev/releases")
    }
}
```

## Quick Start

Create a simple screen with Vexel:

```kotlin
class MyScreen : VexelScreen() {
    override fun afterInitialization() {
        Button("Click me!")
            .setSizing(100f, Size.Pixels, 30f, Size.Pixels)
            .setPositioning(50f, Pos.ParentPixels, 50f, Pos.ParentPixels)
            .onClick { mouseX, mouseY, button ->
                println("Button clicked!")
                true // indicates the event was handled
            }
            .childOf(window)
    }
}
```

## Core Concepts

### Elements and Components

All UI elements extend `VexelElement<T>`, providing:
- **Positioning**: Flexible layout system
- **Sizing**: Auto, percentage, or pixel-based dimensions
- **Event Handling**: Mouse and keyboard interaction support
- **Parent-Child Relationships**: Hierarchical organization
- **Animation Support**: Built-in animation capabilities

### Layout System

Vexel uses positioning and sizing methods for layout control:

```kotlin
element
    .setSizing(200f, Size.Pixels, 100f, Size.Pixels)
    .setPositioning(10f, Pos.ParentPixels, 20f, Pos.ParentCenter)
```

#### Positioning Types (`Pos`)
- `ParentPixels`: Offset in pixels from parent
- `ScreenPixels`: Absolute screen coordinates
- `ParentPercent`: Percentage of parent dimensions
- `ScreenPercent`: Percentage of screen dimensions
- `ParentCenter`: Centered within parent
- `ScreenCenter`: Centered on screen
- `AfterSibling`: After the previous sibling element
- `MatchSibling`: Match position of previous sibling

#### Sizing Types (`Size`)
- `Pixels`: Fixed pixel size
- `ParentPerc`: Percentage of parent size, or screen size if no parent
- `Auto`: Automatic sizing based on content

### Animation System

Vexel provides a comprehensive animation system:

```kotlin
// Basic animations
element.animatePosition(100f, 50f, 500, EasingType.EASE_OUT)
element.animateSize(200f, 100f, 300, EasingType.EASE_IN_OUT)

// Animation presets
element.fadeIn(duration = 300)
element.slideIn(fromX = -100f, duration = 500)
element.bounceScale(scale = 1.2f, duration = 200)
```

#### Animation Types
- `LINEAR`
- `EASE_IN`
- `EASE_OUT`
- `EASE_IN_OUT`

## Built-in Components

### Core Components

#### Rectangle
Basic rectangular element with styling options:

```kotlin
Rectangle()
    .backgroundColor(0xFF2196F3.toInt())
    .borderColor(0xFF1976D2.toInt())
    .borderRadius(8f)
    .borderThickness(2f)
    .padding(16f)
    .hoverColor(0xFF1E88E5.toInt())
    .dropShadow()
    .setSizing(200f, Size.Pixels, 100f, Size.Pixels)
    .childOf(parent)
```

#### Text
Text rendering with font support:

```kotlin
Text("Hello World")
    .color(0xFFFFFFFF.toInt())
    .fontSize(16f)
    .shadow(true)
    .setPositioning(10f, Pos.ParentPixels, 10f, Pos.ParentPixels)
    .childOf(parent)
```

#### Container
Layout container with scrolling support:

```kotlin
Container()
    .padding(16f)
    .scrollable(true)
    .setSizing(300f, Size.Pixels, 400f, Size.Pixels)
    .childOf(parent)
```

#### SvgImage
SVG image rendering with color support:

```kotlin
SvgImage(svgPath = "/assets/mymod/icon.svg")
    .setSvgColor(Color.WHITE)
    .setSizing(32f, Size.Pixels, 32f, Size.Pixels)
    .setPositioning(10f, Pos.ParentPixels, 10f, Pos.ParentPixels)
    .childOf(parent)
```

#### Tooltip
Contextual tooltip display:

```kotlin
element.addTooltip("This is a helpful tooltip")
```

### Interactive Elements

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

## Event Handling

Vexel provides comprehensive event handling:

```kotlin
element
    .onMouseEnter { mouseX, mouseY ->
        // Mouse entered element
    }
    .onMouseExit { mouseX, mouseY ->
        // Mouse left element
    }
    .onClick { mouseX, mouseY, button ->
        // Element clicked
        true // Return true if handled
    }
    .onScroll { mouseX, mouseY, horizontal, vertical ->
        // Scroll event
        true
    }
    .onCharType { keyCode, scanCode, char ->
        // Character typed
        true
    }
    .onValueChange { value ->
        // Value changed for input elements
    }
```

## Advanced Features

### Focus Management
Control element focus:

```kotlin
element
    .setRequiresFocus()
    .onCharType { keyCode, scanCode, char ->
        // Only receives input when focused
        true
    }
```

### Scrolling
Enable scrolling for containers and rectangles:

```kotlin
Rectangle()
    .scrollable(true)
    .padding(16f)
    .setSizing(300f, Size.Pixels, 200f, Size.Pixels)
    .childOf(parent)
```

### Floating Elements
Create floating elements that don't affect parent layout:

```kotlin
element.setFloating()
```

### Mouse Event Control
Ignore mouse events for decorative elements:

```kotlin
element.ignoreMouseEvents()
```

### Custom Rendering
Access low-level rendering through NVGRenderer:

```kotlin
class CustomElement : VexelElement<CustomElement>() {
    override fun onRender(mouseX: Float, mouseY: Float) {
        NVGRenderer.rect(x, y, width, height, 0xFF2196F3.toInt(), 8f)
        NVGRenderer.text("Custom", x + 10f, y + 10f, 14f, 0xFFFFFFFF.toInt())
    }
}
```

### NVGRenderer Features

The NVGRenderer provides direct access to NanoVG functionality:

```kotlin
// Basic shapes
NVGRenderer.rect(x, y, width, height, color, radius)
NVGRenderer.circle(x, y, radius, color)
NVGRenderer.line(x1, y1, x2, y2, thickness, color)

// Text rendering
NVGRenderer.text(text, x, y, size, color, font)
NVGRenderer.textWidth(text, size, font)

// Gradients
NVGRenderer.gradientRect(x, y, w, h, color1, color2, gradient, radius)

// Images and SVGs
NVGRenderer.svg(id, x, y, width, height, alpha)
NVGRenderer.image(image, x, y, width, height, radius)

// Effects
NVGRenderer.dropShadow(x, y, width, height, blur, spread, radius)

// Transformations
NVGRenderer.push()
NVGRenderer.translate(x, y)
NVGRenderer.rotate(angle)
NVGRenderer.scale(x, y)
NVGRenderer.pop()

// Clipping
NVGRenderer.pushScissor(x, y, width, height)
NVGRenderer.popScissor()
```

## Layout Examples

### Vertical Stack Layout
```kotlin
val container = Container()
    .setSizing(300f, Size.Pixels, Size.Auto, Size.Auto)
    .padding(16f)
    .childOf(window)

Text("Title")
    .fontSize(18f)
    .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
    .childOf(container)

Button("Button 1")
    .setPositioning(0f, Pos.ParentPixels, 8f, Pos.AfterSibling)
    .childOf(container)

Button("Button 2")
    .setPositioning(0f, Pos.ParentPixels, 8f, Pos.AfterSibling)
    .childOf(container)
```

### Horizontal Layout
```kotlin
val container = Container()
    .setSizing(Size.Auto, Size.Auto)
    .padding(16f)
    .childOf(window)

Button("Left")
    .setPositioning(0f, Pos.ParentPixels, 0f, Pos.ParentPixels)
    .childOf(container)

Button("Center")
    .setPositioning(8f, Pos.AfterSibling, 0f, Pos.MatchSibling)
    .childOf(container)

Button("Right")
    .setPositioning(8f, Pos.AfterSibling, 0f, Pos.MatchSibling)
    .childOf(container)
```

### Centered Layout
```kotlin
Rectangle()
    .backgroundColor(0xFF2196F3.toInt())
    .borderRadius(8f)
    .setSizing(200f, Size.Pixels, 100f, Size.Pixels)
    .setPositioning(0f, Pos.ScreenCenter, 0f, Pos.ScreenCenter)
    .childOf(window)
```

### Responsive Layout
```kotlin
Container()
    .setSizing(80f, Size.ParentPerc, 60f, Size.ParentPerc)
    .setPositioning(0f, Pos.ScreenCenter, 0f, Pos.ScreenCenter)
    .padding(16f)
    .childOf(window)
```

## Animation Presets

### Fade Animations
```kotlin
// Fade in with children
element.fadeIn(duration = 300, includeChildren = true)

// Fade out
element.fadeOut(duration = 300) {
    // Callback when animation completes
    println("Fade out complete")
}
```

### Movement Animations
```kotlin
// Slide in from left
element.slideIn(fromX = -100f, duration = 500)

// Move to position
element.moveTo(x = 100f, y = 50f, duration = 300)
```

### Scale Animations
```kotlin
// Bounce scale effect
element.bounceScale(scale = 1.2f, duration = 200)

// Scale to size
element.scaleTo(width = 200f, height = 100f, duration = 300)
```

### Color Animations
```kotlin
// Animate to color
element.colorTo(0xFF00FF00.toInt(), duration = 500)
```

### Rotate Animations

```kotlin
// Rotate element (for Rectangle and SvgImage)
rectangle.rotateTo(45f, duration = 300)
```
