package com.example.myapplication

import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.os.BatteryManager
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.wear.watchface.*
import androidx.wear.watchface.complications.ComplicationSlotBounds
import androidx.wear.watchface.complications.DefaultComplicationDataSourcePolicy
import androidx.wear.watchface.complications.SystemDataSources
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.rendering.*
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyleSchema
import java.time.ZonedDateTime
import java.util.*
import kotlin.math.*


class SecondWatchFace: WatchFaceService() {
    override fun createUserStyleSchema(): UserStyleSchema {
        return super.createUserStyleSchema()
    }

    fun createComplicationSlotsManager(
        context: Context,
        currentUserStyleRepository: CurrentUserStyleRepository,
    ): ComplicationSlotsManager {
        val complicationDrawable = ComplicationDrawable(this)
        complicationDrawable.activeStyle.textColor = Color.WHITE
        complicationDrawable.activeStyle.textSize = 100
        complicationDrawable.activeStyle.backgroundColor = Color.GRAY
        val defaultCompFactory = CanvasComplicationFactory { watchState, listener ->
                CanvasComplicationDrawable(complicationDrawable, watchState, listener)
        }

        val someCompSlot = ComplicationSlot.createRoundRectComplicationSlotBuilder(
            id=100,
            canvasComplicationFactory = defaultCompFactory,
            supportedTypes = listOf(ComplicationType.SHORT_TEXT, ComplicationType.LONG_TEXT, ComplicationType.SMALL_IMAGE),
            defaultDataSourcePolicy = DefaultComplicationDataSourcePolicy(
                SystemDataSources.DATA_SOURCE_DATE,
                ComplicationType.SHORT_TEXT
                ),
            bounds = ComplicationSlotBounds(RectF(0.1f, 0.1f, 0.9f, 0.9f))
        ).build()

        return ComplicationSlotsManager(
            listOf(someCompSlot),
            currentUserStyleRepository
        )
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
        WatchFaceType.DIGITAL,
        object : Renderer.CanvasRenderer2<MySharedAssets>(
            surfaceHolder,
            currentUserStyleRepository,
            watchState,
            CanvasType.HARDWARE,
            interactiveDrawModeUpdateDelayMillis = 32,
            clearWithBackgroundTintBeforeRenderingHighlightLayer = true
        ){
            private var timeType: Typeface? = null

            private val hourStyle: Paint = Paint()
            private val minStyle: Paint = Paint()
            private val minShadowStyle: Paint = Paint()
            private val dialPaint: Paint = Paint()
            private val batPercentPaint: Paint = Paint()
            private val batOutlinePaint: Paint = Paint()
            private val batHollowPaint: Paint = Paint()
            private val secHandPaint: Paint = Paint()

            private val mainCol: Int = Color.parseColor("#ff004c")
            private val gradientCol: Int = Color.parseColor("#ff5900")
            private val whiteCol: Int = Color.parseColor("#ffffff")

            private var day: String = ""
            private var hour: String = ""
            private var min: String = ""
            private var sec: String = ""

            private var clockSize: Float = 180f
            private var secondHandT: Float = 0f
            private var prevSecond: Int = 0
            private var targetSecond: Int = 0
            private var deltaTime: Float = 0f
            private var prevFrameTime: Long = System.currentTimeMillis()

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
                    textSize = clockSize
                    color = whiteCol
                    isAntiAlias = true
                    alpha = 255
                    textAlign = Paint.Align.CENTER
                }
                hourStyle.setShadowLayer(30f,5f,5f,Color.BLACK)
                minStyle.apply{
                    typeface = timeType
                    textSize = clockSize
                    shader = LinearGradient(0f,0f,0f,200f,mainCol,mainCol,Shader.TileMode.MIRROR)
                    color = mainCol
                    isAntiAlias = true
                    alpha = 255
                    textAlign = Paint.Align.CENTER
                }
                minShadowStyle.apply{
                    typeface = timeType
                    textSize = clockSize
                    textAlign = Paint.Align.CENTER
                }
                //minShadowStyle.setShadowLayer(30f,5f,5f,Color.BLACK)
                dialPaint.apply {
                    color = mainCol
                    style = Paint.Style.STROKE
                    strokeWidth = 8f
                    isAntiAlias = true
                }
                batPercentPaint.apply{
                    style = Paint.Style.STROKE
                    strokeWidth = 40f
                    strokeCap = Paint.Cap.SQUARE
                    color = mainCol
                    isAntiAlias = true
                }
                batOutlinePaint.apply{
                    style = Paint.Style.STROKE
                    strokeWidth = 50f
                    strokeCap = Paint.Cap.SQUARE
                    color = mainCol
                    isAntiAlias = true
                }
                batHollowPaint.apply{
                    style = Paint.Style.STROKE
                    strokeWidth = 34f
                    strokeCap = Paint.Cap.SQUARE
                    color = Color.BLACK
                    isAntiAlias = true
                }
                secHandPaint.apply{
                    color = mainCol
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
            }

            fun update(){
                day = GregorianCalendar.getInstance().get(Calendar.DAY_OF_MONTH).toString()
                hour = GregorianCalendar.getInstance().get(Calendar.HOUR_OF_DAY).toString()
                min = GregorianCalendar.getInstance().get(Calendar.MINUTE).toString()
                sec = GregorianCalendar.getInstance().get(Calendar.SECOND).toString()
                if(hour.toInt()<10) hour = "0$hour"
                if(min.toInt()<10) min = "0$min"
                deltaTime = (System.currentTimeMillis() - prevFrameTime).toFloat()*0.001f
                prevFrameTime = System.currentTimeMillis()
            }

            fun draw(canvas: Canvas,bounds: Rect){
                // Draw Battery
                val batMargin:Float = 60f
                val rect = RectF(bounds.left+batMargin,bounds.top+batMargin,bounds.right-batMargin,bounds.bottom-batMargin)
                canvas.drawArc(rect,-195f,210f,false,batOutlinePaint)
                canvas.drawArc(rect,-195f,210f,false,batHollowPaint)
                canvas.drawArc(rect,-195f,210f* getBatteryPercentage(applicationContext)/100,false,batPercentPaint)

                // Draw Second Dial
                val minPerDash: Float = 5f
                val dashThickness: Float = 102f
                val edgeMargin: Float = 20f
                dialPaint.pathEffect = DashPathEffect(floatArrayOf(dashThickness, (((bounds.width()-edgeMargin*2)*PI.toFloat())*(minPerDash/60f))-dashThickness), -((((bounds.width()-edgeMargin*2)*PI.toFloat())*(minPerDash/60f))-dashThickness)/2)
                canvas.save()
                canvas.rotate(-90f,bounds.exactCenterX(),bounds.exactCenterY())
                canvas.drawCircle(bounds.exactCenterX(),bounds.exactCenterY(),abs(bounds.right)/2-edgeMargin,dialPaint)
                canvas.restore()

                // Draw Second Hand
                val secHandOffset:Float = bounds.exactCenterX()+1f
                if(secondHandT>0) secondHandT-=deltaTime*1.5f
                else if(sec.toInt()!=targetSecond){
                    targetSecond = sec.toInt()
                    prevSecond = targetSecond-1
                    secondHandT = 1f
                }
                val trianglePath = Path()
                trianglePath.moveTo(bounds.exactCenterX(), bounds.exactCenterY()-secHandOffset+12f)
                trianglePath.lineTo(bounds.exactCenterX()+10f, bounds.exactCenterY()-secHandOffset)
                trianglePath.lineTo(bounds.exactCenterX()-10f, bounds.exactCenterY()-secHandOffset)
                trianglePath.close()
                canvas.save()
                canvas.rotate(lerp(targetSecond.toFloat()*6,prevSecond.toFloat()*6,secondHandT*secondHandT*secondHandT*secondHandT),bounds.exactCenterX(),bounds.exactCenterY())
                canvas.drawPath(trianglePath, secHandPaint)
                canvas.restore()

                // Draw Clock
                canvas.drawText("$hour",bounds.exactCenterX(), bounds.exactCenterY()-10, hourStyle)
                //canvas.drawText("$min",bounds.exactCenterX(), bounds.exactCenterY()+clockSize*0.76f-10, minShadowStyle)
                canvas.drawText("$min",bounds.exactCenterX(), bounds.exactCenterY()+clockSize*0.76f-10, minStyle)

                //for ((id,complication) in complicationSlotsManager.complicationSlots){
                //    if(true){
                //        complication.render(canvas, ZonedDateTime.now(),renderParameters)
                //    }
                //}
            }
        }
    )
}

fun lerp(a: Float, b: Float, f: Float): Float {
    return a + f * (b - a)
}
fun getBatteryPercentage(context: Context): Float {
    val batteryStatus: Intent? = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

    return level / scale.toFloat() * 100
}