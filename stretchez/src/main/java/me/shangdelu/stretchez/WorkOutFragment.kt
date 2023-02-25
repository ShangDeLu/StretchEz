package me.shangdelu.stretchez

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import me.shangdelu.stretchez.database.StretchExercise
import java.util.*
import java.util.concurrent.TimeUnit

private const val ARG_PLAN_ID = "plan_id"

class WorkOutFragment : Fragment() {

    private var cdTimerText: TextView? = null
    private lateinit var videoView: VideoView
    private var congratulations: TextView? = null
    private lateinit var repeatButton: Button
    private lateinit var returnButton: Button
    private lateinit var finishLayout: LinearLayout
    private lateinit var workOutRepository: StretchPlanRepository
    private var stretchPlanID: UUID? = null
    private lateinit var exercises: List<StretchExercise>
    //Index of current Exercise in list of stretchExercise
    private var currentExercise = 0
    //length of interval between exercises
    private var interval = 0
    //variable for MediaPlayer to implement pause and resume feature
    private lateinit var mediaPlayer: MediaPlayer
    //boolean flag to determine the state of mediaPlayer
    private var playingFlag: Boolean = true
    //variable for timer to be accessed by other function
    private lateinit var timer: CountDownTimer
    //variable for remaining time on the timer
    private var timeRemain: Long = 0
    //initialize ExerciseControl
    private val exerciseControl = ExerciseControl()
    private var intervalTextView: TextView? = null

    //Associate the Fragment with the ViewModel
    private val workOutFragmentViewModel: WorkOutFragmentViewModel by lazy {
        ViewModelProvider(this)[WorkOutFragmentViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workOutRepository = StretchPlanRepository.get()
        //Retrieve stretchPlanID from the fragment arguments
        stretchPlanID = arguments?.getSerializable(ARG_PLAN_ID) as UUID?
        //Retrieve interval from the fragment arguments
        interval = arguments?.getInt("Interval") ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_work_out, container, false)

        //observe the LiveData
        workOutFragmentViewModel.getExercisesOfPlan(stretchPlanID).observe(viewLifecycleOwner) {
            //retrieve the list of exercises of stretchPlanID from LiveData
            exercises = it
            //Get the first exercise of the plan
            val current = firstExercise()
            //Start the countdown timer
            timerStart(current.exerciseDuration.toLong() * 1000)
        }

        cdTimerText = view.findViewById(R.id.cdTimer) as TextView
        videoView = view.findViewById(R.id.workOutVideo) as VideoView
        //set the link of first exercise in exercises for videoView
        //videoView.setVideoURI(Uri.parse(exercises[currentExercise].exerciseLink))
        videoView.setVideoURI(Uri.parse("android.resource://" + requireContext().packageName + "/" + R.raw.stretch1))
        videoView.setOnPreparedListener { mp ->
            //get a reference of mp
            mediaPlayer = mp
            //loop the demonstration of current exercise
            mp.isLooping = true
        }

        //Start playing the exercise video
        videoView.start()

        finishLayout = view.findViewById(R.id.finish_layout) as LinearLayout
        congratulations = view.findViewById(R.id.congratulations) as TextView
        repeatButton = view.findViewById(R.id.repeat_button) as Button
        returnButton = view.findViewById(R.id.return_button) as Button
        intervalTextView = view.findViewById(R.id.interval_textView) as TextView

        //TODO: Implement the pause feature when user press the screen
        //TODO 2: CountdownTimer need to pause and resume as well
        videoView.setOnClickListener {
            if (playingFlag) {
                //if mediaPlayer is currently playing, pause it and change the state of flag
                mediaPlayer.pause()
                playingFlag = false
                //Pause the countdown timer
                timerPause()
            } else {
                //if mediaPlayer is currently pausing, start it and change the state of flag
                mediaPlayer.start()
                playingFlag = true
                //Resume the countdown timer
                timerResume()
            }
        }

        return view
    }

    private fun firstExercise(): StretchExercise {
        return exerciseControl.getCurrent(exercises)
    }

    private fun repeat() {
        //reset the countDownTimer and start it
        val reset = exerciseControl.reset(exercises)
        timerStart(reset.exerciseDuration.toLong() * 1000)
        //reset video path and start playing
        videoView.setVideoURI(Uri.parse("android.resource://" + requireContext().packageName + "/" + R.raw.stretch1))
        videoView.start()
        //hide the repeat and return button
        congratulations?.visibility = View.INVISIBLE
        finishLayout.visibility = View.INVISIBLE
    }


    private fun intervalStart(timeLengthMilli: Long) {
        //Hide videoView
        videoView.visibility = View.INVISIBLE
        //Show the intervalTextView
        intervalTextView?.visibility = View.VISIBLE
        val nextExercise = resources.getString(R.string.next_exercise) + " " + exerciseControl.peekNext(exercises).exerciseName
        intervalTextView?.text = nextExercise
        val next = exerciseControl.moveNext(exercises) //move to the next exercise
        //setVideoURI as the link of next exercise in the stretchPlan
        //videoView.setVideoURI(Uri.parse(next.exerciseLink))
        videoView.setVideoURI(Uri.parse("android.resource://" + requireContext().packageName + "/" + R.raw.stretch2))

        val intervalTimer = object : CountDownTimer(timeLengthMilli, 1000) {
            override fun onTick(milliTillFinish: Long) {
                cdTimerText?.text = getString(
                    R.string.formatted_time,
                    TimeUnit.MILLISECONDS.toMinutes(milliTillFinish) % 60,
                    TimeUnit.MILLISECONDS.toSeconds(milliTillFinish) % 60)
            }
            override fun onFinish() {
                //Hide intervalTextView
                intervalTextView?.visibility = View.GONE
                //Show videoView
                videoView.visibility = View.VISIBLE

                //reset the countDownTimer and start it
                timerStart(next.exerciseDuration.toLong() * 1000)
                //start playing the next exercise
                videoView.start()
            }
        }
        intervalTimer.start()
    }
    private fun timerStart(timeLengthMilli: Long) {
        timer = object : CountDownTimer(timeLengthMilli, 1000) {
            override fun onTick(milliTillFinish: Long) {
                cdTimerText?.text = getString(
                    R.string.formatted_time,
                    TimeUnit.MILLISECONDS.toMinutes(milliTillFinish) % 60,
                    TimeUnit.MILLISECONDS.toSeconds(milliTillFinish) % 60)
                //record the remaining time
                timeRemain = milliTillFinish
            }

            override fun onFinish() {
                //if currentExercise is not the last exercise of the stretchPlan
                if (!exerciseControl.endOfList(exercises)) {
                    //stop and release the media player instance and move to idle state
                    videoView.stopPlayback()
                    //Start the interval countdown timer
                    intervalStart(interval.toLong() * 1000)
                } else {
                    //currentExercise is the last exercise of the stretchPlan
                    //will stop and release the media player instance and move to idle state
                    videoView.stopPlayback()
                    cdTimerText?.text = getString(R.string.complete_time)
                    congratulations?.visibility = View.VISIBLE  //show congratulations after stretching
                    finishLayout.visibility = View.VISIBLE  //make repeat and return button visible

                    //Button to repeat current exercise
                    repeatButton.setOnClickListener {
                        //Reset the timer instead of replace the fragment
                        repeat()
                    }

                    //Button to return to home screen
                    returnButton.setOnClickListener {
                        findNavController().navigate(R.id.action_navigation_work_out_to_navigation_home)
                    }
                }
            }
        }
        timer.start()
    }

    private fun timerPause() {
        timer.cancel()
    }

    private fun timerResume() {
        //start a new timer with the time remaining
        timerStart(timeRemain)
    }


    //TODO 1: Allow MediaPlayer to play multiple videos
    //TODO 2: When one video is done, notify user what the next video is with an interval
    //TODO 3: Allow user to pause during video, and pause between videos
    //TODO 4: User should be able to determine the length of the interval between videos
    //TODO 5: Consider about Unidirectional Architecture and responsibility chain
    //TODO 6: Encapsulate moving next and moving back of Exercises in a separate class, so index cannot be accessed by other class


    companion object {
        fun newInstance(stretchPlanID: UUID?, option: Int, interval: Int): Bundle {
            //This Bundle contains key-value pairs that work just like the intent extras of an Activity.
            //Each pair is known as an argument.
            return Bundle().apply {
                putSerializable(ARG_PLAN_ID, stretchPlanID)
                putInt("Option", option)
                putInt("Interval", interval)
            }
        }
    }
}