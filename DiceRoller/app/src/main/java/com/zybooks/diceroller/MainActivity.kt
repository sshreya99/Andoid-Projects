package com.zybooks.diceroller

import android.os.Bundle
import android.os.CountDownTimer
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

const val MAX_DICE = 3

class MainActivity : AppCompatActivity(),  RollLengthDialogFragment.OnRollLengthSelectedListener {

    private var timer: CountDownTimer? = null
    private var timerLength = 2000L
    private lateinit var optionsMenu: Menu
    private var numVisibleDice = MAX_DICE
    private lateinit var diceList: MutableList<Dice>
    private lateinit var diceImageViewList: MutableList<ImageView>
    private var selectedDie = 0
    private var initialY: Float = 0f
    private val MOVE_THRESHOLD = 20


    override fun onRollLengthClick(which: Int) {
        // Convert to milliseconds
        timerLength = 1000L * (which + 1)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        // Determine which menu option was chosen
        return when (item.itemId) {
            R.id.action_one -> {
                changeDiceVisibility(1)
                showDice()
                true
            }
            R.id.action_two -> {
                changeDiceVisibility(2)
                showDice()
                true
            }
            R.id.action_three -> {
                changeDiceVisibility(3)
                showDice()
                true
            }
            R.id.action_stop -> {
                timer?.cancel()
                item.isVisible = false
                true
            }
            R.id.action_roll -> {
                rollDice()
                true
            }
            R.id.action_roll_length -> {
                val dialog = RollLengthDialogFragment()
                dialog.show(supportFragmentManager, "rollLengthDialog")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun rollDice() {
        optionsMenu.findItem(R.id.action_stop).isVisible = true
        timer?.cancel()

        // Start a timer that periodically changes each visible dice
        timer = object : CountDownTimer(timerLength, 100) {
            override fun onTick(millisUntilFinished: Long) {
                for (i in 0 until numVisibleDice) {
                    diceList[i].roll()
                }
                showDice()
            }

            override fun onFinish() {
                optionsMenu.findItem(R.id.action_stop).isVisible = false
            }
        }.start()
    }

    private fun changeDiceVisibility(numVisible: Int) {
        numVisibleDice = numVisible

        // Make dice visible
        for (i in 0 until numVisible) {
            diceImageViewList[i].visibility = View.VISIBLE
        }

        // Hide remaining dice
        for (i in numVisible until MAX_DICE) {
            diceImageViewList[i].visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Create list of Dice
        diceList = mutableListOf()
        for (i in 0 until MAX_DICE) {
            diceList.add(Dice(i + 1))
        }

        // Create list of ImageViews
        diceImageViewList = mutableListOf(
            findViewById(R.id.dice1), findViewById(R.id.dice2), findViewById(R.id.dice3))

        showDice()
        registerForContextMenu(diceImageViewList[0])
        // Register context menus for all dice and tag each die
        for (i in 0 until diceImageViewList.size) {
            registerForContextMenu(diceImageViewList[i])
            diceImageViewList[i].tag = i
        }

        // Setup touch listener for dice
        for (imageView in diceImageViewList) {
            imageView.setOnTouchListener { v, event ->
                val action = event.action
                when (action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Save the initial position
                        initialY = event.y
                        true // Indicate we've handled the event
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val newY = event.y
                        val deltaY = newY - initialY
                        if (Math.abs(deltaY) > MOVE_THRESHOLD) {
                            // Determine the direction of the move
                            if (deltaY > 0) {
                                // Moved downwards
                                decreaseDieNumber(v.tag as Int)
                            } else {
                                // Moved upwards
                                increaseDieNumber(v.tag as Int)
                            }
                            initialY = newY // Reset initial position to new position after moving
                        }
                        true // Indicate we've handled the event
                    }
                    else -> false // We're not interested in other touch events
                }
            }
        }
    }

    private fun increaseDieNumber(dieIndex: Int) {
        val dice = diceList[dieIndex]
        if (dice.number < LARGEST_NUM) {
            dice.number++
            showDice()
        }
    }

    private fun decreaseDieNumber(dieIndex: Int) {
        val dice = diceList[dieIndex]
        if (dice.number > SMALLEST_NUM) {
            dice.number--
            showDice()
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu?,
                                     v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        // Save which die is selected
        selectedDie = v?.tag as Int

        menuInflater.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_one -> {
                diceList[selectedDie].number++
                showDice()
                true
            }
            R.id.subtract_one -> {
                diceList[selectedDie].number--
                showDice()
                true
            }
            R.id.roll -> {
                rollDice()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.appbar_menu, menu)
        optionsMenu = menu!!
        return super.onCreateOptionsMenu(menu)
    }

    private fun showDice() {

        // Show visible dice
        for (i in 0 until numVisibleDice) {
            val diceDrawable = ContextCompat.getDrawable(this, diceList[i].imageId)
            diceImageViewList[i].setImageDrawable(diceDrawable)
            diceImageViewList[i].contentDescription = diceList[i].imageId.toString()
        }
    }
}