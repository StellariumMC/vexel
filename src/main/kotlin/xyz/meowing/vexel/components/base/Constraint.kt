package xyz.meowing.vexel.components.base

sealed class Constraint {
    data class SizeConstraint(val size: Size, val value: Float) : Constraint()
    data class PosConstraint(val pos: Pos, val offset: Float) : Constraint()
}

fun perc(value: Float): Constraint = Constraint.SizeConstraint(Size.ParentPerc, value)
val auto: Constraint get() = Constraint.SizeConstraint(Size.Auto, 0f)

operator fun Pos.plus(offset: Float): Constraint = Constraint.PosConstraint(this, offset)
operator fun Pos.plus(offset: Int): Constraint = Constraint.PosConstraint(this, offset.toFloat())