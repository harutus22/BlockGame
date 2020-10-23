package com.example.bloackgame

import android.content.Intent
import android.content.res.Configuration
import android.graphics.RectF
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.jawnnypoo.physicslayout.Physics
import com.jawnnypoo.physicslayout.PhysicsConfig
import kotlinx.android.synthetic.main.activity_play.*
import kotlinx.coroutines.*
import org.jbox2d.dynamics.BodyDef
import org.jbox2d.dynamics.BodyType
import java.util.*


class PlayActivity : AppCompatActivity() {

    private lateinit var slots: IntArray
    private lateinit var platforms: IntArray
    private var points = 0
    private var levelCount = 1
    private var blockBuild = false
    private var touched = false
    private var isKeepPressed = false
    private var leftTouched = false
    private var rightTouched = false
    private var isStartCheck = false
    private var bothTouch = false
    private var slot: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        initArrays()
        initViews()
    }

    override fun onStart() {
        super.onStart()
        physics_layout.physics.setOnCollisionListener(object : Physics.OnCollisionListener {
            override fun onCollisionExited(viewIdA: Int, viewIdB: Int) {
                if (viewIdA == R.id.targetOne && viewIdB == R.id.slot) {
                    rightTouched = false
                } else if (viewIdA == R.id.targetTwo && viewIdB == R.id.slot) {
                    leftTouched = false
                }
            }

            override fun onCollisionEntered(viewIdA: Int, viewIdB: Int) {
                if (!touched && !bothTouch) {
                    if (viewIdA == R.id.targetOne && viewIdB == R.id.slot) {
                        if (!rightTouched) {
                            rightTouched = true
                            startCheckCoroutine()
                            if (leftTouched) {
                                startCountdown()
                            }
                        }

//                        val rectLeft = calculateRectOnScreen(first)
//                        val rectRight = calculateRectOnScreen(second)

//                        val distance = (abs(rectLeft.right - rectRight.left))
//                        val dens = distance / baseContext.resources.displayMetrics.density
//                        val result = dens - slot!!.layoutParams.width / baseContext.resources.displayMetrics.density

//                        if (result in 1.0..9.0) {
//                            points += 5
//                            val view = physics_layout.findViewById<ImageView>(R.id.slot)
//                            physics_layout.removeView(view)
//                            slot = null
//                            blockBuild = false
//                            Toast.makeText(this@PlayActivity, "Good job!!!", Toast.LENGTH_SHORT).show()
//                        } else {
//                            touched = true
//                            Toast.makeText(this@PlayActivity, "Box should be almost same size as hole", Toast.LENGTH_SHORT).show()
//                            startActivity(
//                                Intent(this@PlayActivity, ResultActivity::class.java).putExtra(
//                                    SCORE, points
//                                )
//                            )
//                            finish()
//                        }

                    } else if (viewIdA == R.id.targetTwo && viewIdB == R.id.slot) {
                        if (!leftTouched) {
                            leftTouched = true
                            startCheckCoroutine()
                            if (rightTouched) {
                                startCountdown()
                            }
                        }
                    } else {
                        touched = true
                        bothTouch = true
                        rightTouched = false
                        leftTouched = false
                        Toast.makeText(this@PlayActivity, "Touched border!!!", Toast.LENGTH_SHORT)
                            .show()

                        startActivity(
                            Intent(this@PlayActivity, ResultActivity::class.java).putExtra(
                                SCORE, points
                            )
                        )
                        finish()
                    }
                }
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (slot == null) {
            hold.visibility = View.GONE
            slot = ImageView(baseContext)
            slot?.id = R.id.slot
            leftTouched = false
            rightTouched = false
            isStartCheck = false
            bothTouch = false
            val pixels = 10 * baseContext.resources.displayMetrics.density
            slot?.x = event!!.x
            slot?.y = event.y - 50

            val random = Random()
            slot?.setImageDrawable(ContextCompat.getDrawable(baseContext, slots[random.nextInt(5)]))
            slot?.layoutParams = ViewGroup.LayoutParams(
                pixels.toInt(),
                pixels.toInt()
            )
        }
        if (event?.action == MotionEvent.ACTION_DOWN && !blockBuild) {
            physics_layout.physics.disablePhysics()
            isKeepPressed = true
            physics_layout.addView(slot)
            GlobalScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    while (isKeepPressed) {

                        val height = GlobalScope.async {
                            slot!!.layoutParams.height = withContext(Dispatchers.Default) {
                                slot!!.layoutParams.height + 1
                            }
                        }
                        val width = GlobalScope.async {
                            slot!!.layoutParams.width = withContext(Dispatchers.Default) {
                                slot!!.layoutParams.width + 1
                            }
                        }
                        height.join()
                        width.join()
                        slot!!.requestLayout()
                        delay(10)
                    }
                }
            }
        } else if (event?.action == MotionEvent.ACTION_UP) {
            blockBuild = true
            slot!!.adjustViewBounds = true
            isKeepPressed = false
            physics_layout.physics.enablePhysics()
        }
        return true
    }


    private fun initArrays() {
        slots = intArrayOf(
            R.drawable.slot_1,
            R.drawable.slot_2,
            R.drawable.slot_3,
            R.drawable.slot_4,
            R.drawable.slot_5
        )

        platforms = intArrayOf(
            R.drawable.long_platform,
            R.drawable.short_platform
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SCORE, points)
        outState.putInt(LEVEL, levelCount)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        points = savedInstanceState.getInt(SCORE)
        levelCount = savedInstanceState.getInt(LEVEL)
        levelCountText.text = levelCount.toString()
    }

    private fun calculateRectOnScreen(view: View): RectF {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return RectF(
            location[0].toFloat(),
            location[1].toFloat(),
            location[0].toFloat() + view.measuredWidth,
            location[1].toFloat() + view.measuredHeight
        )
    }

    private fun initViews() {
        levelCountText.text = levelCount.toString()
        val random = Random().nextInt(50) + 150
        val pixels = random * baseContext.resources.displayMetrics.density.toInt()
        if (findViewById<ImageView>(R.id.targetOne) != null) {
            physics_layout.removeAllViews()
        }
        if (!checkOrientation()) {
            createView(pixels, R.id.platformThree, 0, 4)
            createView(pixels, R.id.platformFour, 0, 5)
        }
        createView(pixels, R.id.platformOne, 0, 0)
        createView(pixels, R.id.platformTwo, 0, 1)
        createView(pixels, R.id.targetOne, 1, 2)
        createView(pixels, R.id.targetTwo, 1, 3)

    }

    private fun createView(size: Int, id: Int, platformImage: Int, platformType: Int) {
        val view = ImageView(this)
        view.id = id
        view.setImageDrawable(ContextCompat.getDrawable(this, platforms[platformImage]))
        val layoutParams = if (platformType == 0 || platformType == 1
            || platformType == 4 || platformType == 5
        ) {
            val layout = RelativeLayout.LayoutParams(size, size * 2 / 3 - size / 8)
            if (checkOrientation()) {
                if (platformType == 0) {
                    layout.addRule(RelativeLayout.ALIGN_PARENT_END, 1)
                    layout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1)
                } else if (platformType == 1) {
                    layout.addRule(RelativeLayout.ALIGN_PARENT_START, 1)
                    layout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1)
                }
                layout
            } else {
                when (platformType) {
                    4 -> {
                        layout.addRule(RelativeLayout.ALIGN_PARENT_END, 1)
                        layout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1)
                    }
                    5 -> {
                        layout.addRule(RelativeLayout.ALIGN_PARENT_START, 1)
                        layout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1)
                    }
                    0 -> {
                        layout.addRule(RelativeLayout.START_OF, R.id.platformThree)
                        layout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1)
                    }
                    1 -> {
                        layout.addRule(RelativeLayout.END_OF, R.id.platformFour)
                        layout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1)
                    }
                }
                layout
            }
        } else {
            val layout = RelativeLayout.LayoutParams(size / 3 * 2 / 3, size / 3)
            if (platformType == 2) {
                layout.addRule(RelativeLayout.START_OF, R.id.platformOne)
                layout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1)
            } else {
                layout.addRule(RelativeLayout.END_OF, R.id.platformTwo)
                layout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1)
            }
            layout
        }
        addPhysicsToView(view, layoutParams)
    }

    private fun addPhysicsToView(view: View, layoutParams: RelativeLayout.LayoutParams) {
        val config = PhysicsConfig.create()
        val bodyDef = BodyDef()
        bodyDef.type = BodyType.KINEMATIC
        config.bodyDef = bodyDef
        Physics.setPhysicsConfig(view, config)
        physics_layout.addView(view, layoutParams)
    }

    private fun startCountdown() {
        if (rightTouched && leftTouched) {
            GlobalScope.launch {
                delay(2000)
                if (rightTouched && leftTouched && !bothTouch) {
                    points += 5
                    levelCount++
                    blockBuild = false
                    bothTouch = true
                    withContext(Dispatchers.Main) {
                        val view = physics_layout.findViewById<ImageView>(R.id.slot)
                        physics_layout.removeView(view)
                        slot = null
                        Toast.makeText(this@PlayActivity, "Good job!!!", Toast.LENGTH_SHORT).show()
                        initViews()
                    }

                } else if (!rightTouched && leftTouched || rightTouched && !leftTouched) {
                    touched = true
                    bothTouch = false
                    leftTouched = false
                    rightTouched = false
                    withContext(Dispatchers.Main) {
                        startActivity(
                            Intent(this@PlayActivity, ResultActivity::class.java).putExtra(
                                SCORE, points
                            )
                        )
                        finish()
                    }
                }
            }
        }
    }

    private fun startCheckCoroutine() {
        if(!isStartCheck) {
            isStartCheck = true
            GlobalScope.launch {
                delay(2500)
                if ((!rightTouched || !leftTouched) && !bothTouch) {
                    withContext(Dispatchers.Main) {
                        startActivity(
                            Intent(this@PlayActivity, ResultActivity::class.java).putExtra(
                                SCORE, points
                            )
                        )
                        finish()
                    }
                }
            }
        }
    }

    private fun checkOrientation(): Boolean =
        this.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
}