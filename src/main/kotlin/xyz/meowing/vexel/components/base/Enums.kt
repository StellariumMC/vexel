package xyz.meowing.vexel.components.base

enum class Size {
    Auto,
    ParentPerc,
    Pixels,
    Fill
}

enum class Pos {
    ParentPercent,
    ScreenPercent,
    ParentPixels,
    ScreenPixels,
    ParentCenter,
    ScreenCenter,
    AfterSibling,
    MatchSibling
}

enum class Offset {
    Pixels,
    Percent
}

enum class Alignment {
    None,
    Start,
    End
}


enum class TooltipPosition {
    Top,
    Bottom,
    Left,
    Right
}