/*package com.example.myapplication

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.Shader
import android.support.wearable.watchface.CanvasWatchFaceService
import android.view.SurfaceHolder
import androidx.core.content.res.ResourcesCompat
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.ComplicationSlotsManager

import java.util.Calendar
import java.util.GregorianCalendar

private const val INTERACTIVE_UPDATE_RATE_MS = 1000

class MyWatchFace: WatchFace() {

    override fun onCreateEngine(): WatchFaceService.Engine {
        return Engine()
    }

    private inner class Engine : WatchFaceService.Engine() {

        private var timeType: Typeface? = null

        private val hourStyle: Paint = Paint()
        private val minStyle: Paint = Paint()

        private val mainCol: Int = Color.parseColor("#ff0059")
        private val gradientCol: Int = Color.parseColor("#ff1900")
        private val whiteCol: Int = Color.parseColor("#ffffff")

        private var day: String = ""
        private var hour: String = ""
        private var min: String = ""
        private var sec: String = ""

        override fun onCreate(holder: SurfaceHolder) {

            super.onCreate(holder)
            initialize()
        }

        override fun onDraw(canvas: Canvas, bounds: Rect) {
            canvas.drawColor(Color.BLACK)
            update()
            draw(canvas,bounds)
            invalidate()
        }

        override fun onTimeTick() {
            super.onTimeTick()
            invalidate()
        }

        override fun onDestroy() {
            super.onDestroy()
        }

        fun initialize(){
            if(timeType==null){
                timeType = ResourcesCompat.getFont(applicationContext, R.font.chivo_black)
            }
            println("start")
            hourStyle.apply{
                typeface = timeType
                textSize = 200f
                color = whiteCol
                isAntiAlias = true
                alpha = 255
                textAlign = Paint.Align.CENTER
            }
            minStyle.apply{
                typeface = timeType
                textSize = 200f
                shader = LinearGradient(0f,0f,0f,100f,mainCol,gradientCol,Shader.TileMode.MIRROR)
                color = mainCol
                isAntiAlias = true
                alpha = 255
                textAlign = Paint.Align.CENTER
            }
        }

        fun update(){
            day = GregorianCalendar.getInstance().get(Calendar.DAY_OF_MONTH).toString()
            hour = GregorianCalendar.getInstance().get(Calendar.HOUR_OF_DAY).toString()
            min = GregorianCalendar.getInstance().get(Calendar.MINUTE).toString()
            sec = GregorianCalendar.getInstance().get(Calendar.SECOND).toString()
            if(true){
                if(hour.toInt()<10) hour = "0$hour"
                if(min.toInt()<10) min = "0$min"
                if(sec.toInt()<10) sec = "0$sec"
            }
        }

        fun draw(canvas: Canvas,bounds: Rect){
            canvas.drawText("$hour",bounds.centerX().toFloat(), bounds.centerY().toFloat()-10, hourStyle)
            canvas.drawText("$sec",bounds.centerX().toFloat(), bounds.centerY().toFloat()+minStyle.textSize*0.76f-10, minStyle)
        }
    }
}*/