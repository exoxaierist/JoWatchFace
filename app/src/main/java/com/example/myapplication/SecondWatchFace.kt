package com.example.myapplication

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Shader
import android.view.SurfaceHolder
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyleSchema
import androidx.core.content.res.ResourcesCompat
import androidx.wear.watchface.*
import java.time.ZonedDateTime
import java.util.*

class SecondWatchFace: WatchFaceService() {
    override fun createUserStyleSchema(): UserStyleSchema {
        return super.createUserStyleSchema()
    }

    override fun createComplicationSlotsManager(currentUserStyleRepository: CurrentUserStyleRepository): ComplicationSlotsManager {
        return super.createComplicationSlotsManager(currentUserStyleRepository)
    }

    inner class MySharedAssets : Renderer.SharedAssets {
        override fun onDestroy() {
        }
    }

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ) = WatchFace(
        WatchFaceType.ANALOG,
        object : Renderer.CanvasRenderer2<MySharedAssets>(
            surfaceHolder,
            currentUserStyleRepository,
            watchState,
            CanvasType.HARDWARE,
            interactiveDrawModeUpdateDelayMillis = 16,
            clearWithBackgroundTintBeforeRenderingHighlightLayer = true
        ){
            private var timeType: Typeface? = null

            private val hourStyle: Paint = Paint()
            private val minStyle: Paint = Paint()

            private val mainCol: Int = Color.parseColor("#ff0045")
            private val gradientCol: Int = Color.parseColor("#4300ff")
            private val whiteCol: Int = Color.parseColor("#ffffff")

            private var day: String = ""
            private var hour: String = ""
            private var min: String = ""
            private var sec: String = ""

            init{
                initialize()
            }

            override fun render(
                canvas: Canvas,
                bounds: Rect,
                zonedDateTime: ZonedDateTime,
                sharedAssets: MySharedAssets
            ) {
                canvas.drawColor(Color.BLACK)
                update()
                draw(canvas,bounds)
                invalidate()
            }

            override fun renderHighlightLayer(
                canvas: Canvas,
                bounds: Rect,
                zonedDateTime: ZonedDateTime,
                sharedAssets: MySharedAssets
            ) {

            }

            override suspend fun createSharedAssets(): MySharedAssets {
                return MySharedAssets()
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
                    shader = LinearGradient(0f,0f,0f,200f,mainCol,gradientCol,Shader.TileMode.MIRROR)
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
                canvas.drawText("$min",bounds.centerX().toFloat(), bounds.centerY().toFloat()+minStyle.textSize*0.76f-10, minStyle)
            }
        }
    )
}