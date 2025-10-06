
## Advanced Vexel Usage

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

#### Animation Types: `LINEAR`, `EASE_IN`, `EASE_OUT`, `EASE_IN_OUT`

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
