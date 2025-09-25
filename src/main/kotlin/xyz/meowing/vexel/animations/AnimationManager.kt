package xyz.meowing.vexel.animations

object AnimationManager {
    private val activeAnimations = mutableListOf<Animation<*>>()

    val activeCount: Int
        get() = activeAnimations.size

    fun register(animation: Animation<*>) {
        stopConflictingAnimations(animation.elementId, animation.animationType)
        activeAnimations.add(animation)
    }

    fun update() {
        val completedAnimations = mutableListOf<Animation<*>>()
        val activeList = activeAnimations.toList()

        for (animation in activeList) {
            if (animation.update()) {
                completedAnimations.add(animation)
            }
        }

        activeAnimations.removeAll(completedAnimations)
    }

    fun clear() {
        activeAnimations.clear()
    }

    fun stopAnimations(elementId: String, animationType: AnimationType? = null) {
        val toRemove = if (animationType != null) {
            activeAnimations.filter { it.elementId == elementId && it.animationType == animationType }
        } else {
            activeAnimations.filter { it.elementId == elementId }
        }

        activeAnimations.removeAll(toRemove.toSet())
    }

    private fun stopConflictingAnimations(elementId: String, animationType: AnimationType) {
        stopAnimations(elementId, animationType)
    }
}