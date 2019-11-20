package com.d87.nugkeyboard

import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.MotionEvent
import kotlin.math.PI
import kotlin.math.atan2

class SwipeTracker {
    internal val mPastX = FloatArray(NUM_PAST)
    internal val mPastY = FloatArray(NUM_PAST)
    internal val mPastTime = LongArray(NUM_PAST)

    var _msgLongPress: Message? = null

    private var originX = 0f
    private var originY = 0f
    private var currentLogIndex = 0
    private var _pointerID = -1

    var yVelocity: Float = 0f
        internal set
    var xVelocity: Float = 0f
        internal set

    fun clear() {
        //mPastTime[0] = 0
        mPastTime.fill(0)
        currentLogIndex = 0
    }

    fun start(ev: MotionEvent, pointerID: Int) {
        val pointerIndex = ev.findPointerIndex(pointerID)
        this.clear()

        originX = ev.getX(pointerIndex)
        originY = ev.getY(pointerIndex)
        val eventTime = ev.eventTime
        addPoint(originX, originY, eventTime)
    }

    fun getSwipeAngle(): Float {
        val (sx,sy) = this.getVectorSum()
        if (sx == 0f && sy == 0f) return 0f
        var angle = atan2(sx, -sy) * 180/PI // -180 to 180 starting from positive Y axis
        if (angle < 0) { // transform negative angles to 0 - 360 range
            val diff180 = 180+angle
            angle = 180 + diff180
        }
        return angle.toFloat()
    }

    fun isSwiping(): Boolean {
        if (currentLogIndex >= 2) return true
        return false
    }

    fun getOrigin(): Pair<Float, Float> {
        return Pair(originX, originY)
    }

    fun getLatestPosition(): Pair<Float, Float> {
        var i=0;
        while (i < NUM_PAST) {
            if (mPastTime[i] == 0L) break
            i++
        }
        i--
        if ( i == -1) return Pair(originX, originY)
        return Pair(mPastX[i], mPastY[i])
    }

    fun addMovement(ev: MotionEvent, pointerID: Int) {
        val pointerIndex = ev.findPointerIndex(pointerID)
        if (pointerIndex == -1) return
        //Log.d("PID->INDEX", "$pointerID -> $pointerIndex")

        val N = ev.historySize

        for (i in 0 until N) {
            val historyX = ev.getHistoricalX(pointerIndex, i)
            val historyY = ev.getHistoricalY(pointerIndex, i)
            val eventTime = ev.getHistoricalEventTime(i)
            addPoint(historyX, historyY, eventTime)
        }

        val time = ev.eventTime
        addPoint(ev.getX(pointerIndex), ev.getY(pointerIndex), time)
    }

    private fun addPoint(x: Float, y: Float, time: Long) {
        // TODO: Just drop everything that comes after LONGEST_PAST_TIME timeout
        var i = currentLogIndex
        val pastTime = mPastTime
        val pastX = mPastX
        val pastY = mPastY

        // Skipping events where pointer hasn't been moved since tracking was started
        if (i == 1 && x == originX && y == originY )
            return

        val chainStartTime = pastTime[0]
        if ( chainStartTime > 0 && time - chainStartTime > LONGEST_PAST_TIME ) {
            return
        }

        if (i < NUM_PAST) {
            pastX[i] = x
            pastY[i] = y
            pastTime[i] = time
            currentLogIndex++
        }
    }

    @JvmOverloads
    fun computeCurrentVelocity(units: Int, maxVelocity: Float = java.lang.Float.MAX_VALUE) {
        val pastX = mPastX
        val pastY = mPastY
        val pastTime = mPastTime
        val oldestX = pastX[0]
        val oldestY = pastY[0]
        val oldestTime = pastTime[0]
        var accumX = 0f
        var accumY = 0f
        var N = 0
        while (N < NUM_PAST) {
            if (pastTime[N] == 0L) {
                break
            }
            N++
        }
        for (i in 1 until N) {
            val dur = (pastTime[i] - oldestTime).toInt()
            if (dur == 0) continue
            var dist = pastX[i] - oldestX
            var vel = dist / dur * units   // pixels/frame.
            if (accumX == 0f)
                accumX = vel
            else
                accumX = (accumX + vel) * .5f
            dist = pastY[i] - oldestY
            vel = dist / dur * units   // pixels/frame.
            if (accumY == 0f)
                accumY = vel
            else
                accumY = (accumY + vel) * .5f
        }
        xVelocity = if (accumX < 0.0f)
            Math.max(accumX, -maxVelocity)
        else
            Math.min(accumX, maxVelocity)
        yVelocity = if (accumY < 0.0f)
            Math.max(accumY, -maxVelocity)
        else
            Math.min(accumY, maxVelocity)
    }

    fun getVectorSum(): Pair<Float, Float> {
        val historyTimestamp = mPastTime
        val historyX = mPastX
        val historyY = mPastY

        var prevX = originX
        var prevY = originY

        var resultX = 0f
        var resultY = 0f
        for (i in 0 until NUM_PAST) {
            val evTime = historyTimestamp[i]
            if (evTime == 0L) break
            val evX = historyX[i]
            val evY = historyY[i]

            resultX += evX - prevX
            resultY += evY - prevY

            prevX = evX
            prevY = evY
        }
        return Pair(resultX, resultY)
    }

    fun getVectorAverage(): Pair<Float, Float> {
        val historyTimestamp = mPastTime
        val historyX = mPastX
        val historyY = mPastY


        var resultX = 0f
        var resultY = 0f
        for (i in 0 until NUM_PAST) {
            val evTime = historyTimestamp[i]
            if (evTime == 0L) break
            val evX = historyX[i]
            val evY = historyY[i]

            resultX += evX - originX
            resultY += evY - originY
        }
        return Pair(resultX, resultY)
    }


    companion object {
        const val LONG_PRESS = 2
        const val NUM_PAST = 10
        const val LONGEST_PAST_TIME = 200 // If event comes after this amount of time since chain start, then drop the chain
    }
}