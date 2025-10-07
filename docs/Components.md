### Core Components
Components are base objects that elements are built off of, they are often the lowest level of elements and rendering used.

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
Layout container with scrolling support (essentially invisible rectangles):

```kotlin
Container()
    .padding(16f)
    .scrollable(true)
    .setSizing(300f, Size.Pixels, 400f, Size.Pixels)
    .childOf(parent)
```

#### SvgImage
SVG image rendering with color support

```kotlin
SvgImage(svgPath = "/assets/mymod/icon.svg")
    .setSvgColor(Color.WHITE)
    .setSizing(32f, Size.Pixels, 32f, Size.Pixels)
    .setPositioning(10f, Pos.ParentPixels, 10f, Pos.ParentPixels)
    .childOf(parent)
```
<details>
<summary>⚠️ <b>Important: Using SVGs with Dynamic Colors ( .setSvgColor )</b></summary>

When using SVG files with dynamic or customizable colors, ensure that your SVG elements use the `currentColor` keyword.  
This allows Vexel to apply colors dynamically at runtime based on your theme or component color.

**Example:**
```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24">
  <path
    d="M6 9l6 6 6-6"
    fill="none"
    stroke="currentColor"
    stroke-width="2.5"
    stroke-linecap="round"
    stroke-linejoin="round"
  />
</svg>
```
</details>

#### Tooltip
Contextual tooltip display:

```kotlin
element.addTooltip("This is a helpful tooltip")
```
