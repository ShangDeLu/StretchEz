package me.shangdelu.stretchez

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
    var currentExercise = 0
    //Associate the Fragment with the ViewModel
    private val workOutFragmentViewModel: WorkOutFragmentViewModel by lazy {
        ViewModelProvider(this)[WorkOutFragmentViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        workOutRepository = StretchPlanRepository.get()
        //Retrieve stretchPlanID from the fragment arguments
        stretchPlanID = arguments?.getSerializable(ARG_PLAN_ID) as UUID?
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
        }

        cdTimerText = view.findViewById(R.id.cdTimer) as TextView
        videoView = view.findViewById(R.id.workOutVideo) as VideoView
        //set the link of first exercise in exercises for videoView
        //videoView.setVideoURI(Uri.parse(exercises[currentExercise].exerciseLink))
        videoView.setVideoURI(Uri.parse("android.resource://" + requireContext().packageName + "/" + R.raw.stretch1))
        videoView.setOnPreparedListener { mp ->
            mp?.isLooping = true //loop the demonstration of current exercise
        }
        videoView.start()

        finishLayout = view.findViewById(R.id.finish_layout) as LinearLayout
        congratulations = view.findViewById(R.id.congratulations) as TextView
        repeatButton = view.findViewById(R.id.repeat_button) as Button
        returnButton = view.findViewById(R.id.return_button) as Button

        return view
    }

    override fun onStart() {
        super.onStart()
        countDownTimer.start()
    }

    private fun repeat() {
        //reset the countDownTimer and start it
        countDownTimer.cancel()
        countDownTimer.start()
        //reset video path and start playing
        videoView.setVideoURI(Uri.parse("android.resource://" + requireContext().packageName + "/" + R.raw.stretch1))
        videoView.setOnPreparedListener { mp ->
            mp?.isLooping = true //loop the demonstration of current exercise
        }

        videoView.start()
        //hide the repeat and return button
        congratulations?.visibility = View.INVISIBLE
        finishLayout.visibility = View.INVISIBLE
    }


    private var countDownTimer = object : CountDownTimer(exercises[currentExercise].exerciseDuration.toLong() * 1000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            cdTimerText?.text = getString(
                R.string.formatted_time,
                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
             TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60)
        }

        override fun onFinish() {
            //if currentExercise is not the last exercise of the stretchPlan
            if (currentExercise != exercises.size - 1) {
                currentExercise++ //move to the next exercise
                //stop and release the media player instance and move to idle state
                videoView.stopPlayback()
                //setVideoURI as the link of next exercise in the stretchPlan
                //videoView.setVideoURI(Uri.parse(exercises[currentExercise].exerciseLink))
                videoView.setVideoURI(Uri.parse("android.resource://" + requireContext().packageName + "/" + R.raw.stretch1))
                //reset the countDownTimer and start it
                this.cancel()
                this.start()
                //start playing the next exercise
                videoView.start()
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

    //TODO 1: Allow MediaPlayer to play multiple videos
    //TODO 2: When one video is done, notify user what the next video is with an interval
    //TODO 3: Allow user to pause during video, and pause between videos
    //TODO 4: User should be able to determine the length of the interval between videos
    //TODO 5: Consider about Unidirectional Architecture and responsibility chain


    companion object {
        fun newInstance(stretchPlanID: UUID?, option: Int): Bundle {
            //This Bundle contains key-value pairs that work just like the intent extras of an Activity.
            //Each pair is known as an argument.
            return Bundle().apply {
                putSerializable(ARG_PLAN_ID, stretchPlanID)
                putInt("Option", option)
            }
        }
    }
}