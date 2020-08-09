package com.d87.nugkeyboard

class BindingsConfig {
    var roll: Float = 0f // Angle to adjust divisions
    var enableKeyRepeat = true
    // var type: String = "Normal"
    var divisions: Int = 4
    var onPressAction: KeyboardAction = KeyboardAction(ActionType.NOOP)
    // var onTouchDownAction: KeyboardAction? = null
    // var onTouchUpAction: KeyboardAction? = null
    var onSwipeActions = arrayListOf<KeyboardAction>()

    var divisionAngle: Float = 90f
    class SwipeZone(
        var start: Float,
        var end: Float,
        var action: KeyboardAction
    ){}
    var swipeZones: ArrayList<SwipeZone> = arrayListOf()

    fun getMainAction(): KeyboardAction {
        return onPressAction
    }

    fun getActionByAngle(angle: Float): KeyboardAction? {
        var rolledAngle = angle - roll
        if (rolledAngle >= 360) rolledAngle -= 360
        var matchedZone: SwipeZone
        for (zone in swipeZones) {
            if (rolledAngle >= zone.start && rolledAngle < zone.end )
                return zone.action
        }
        return null
    }

    fun updateSwipeZones() {
        divisionAngle = if (divisions > 0) 360f/divisions else 90f

        val actions = onSwipeActions
        var curAngle = 0f
        var i = 0;
        val numBinds = actions.size
        var previousZone: SwipeZone? = null
        while (i < numBinds) {
            val action = actions[i]
            curAngle = i.toFloat() * divisionAngle
            if (previousZone != null && action.type == ActionType.CONTINUE)
            {
                previousZone?.let{
                    it.end += divisionAngle
                }
            } else {
                var zoneEnd = curAngle+divisionAngle
                if (360-zoneEnd < 1) zoneEnd = 360f
                val newZone = SwipeZone(curAngle, zoneEnd, action)
                swipeZones.add(newZone)
                previousZone = newZone
            }
            i++
        }
    }
}