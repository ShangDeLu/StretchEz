package me.shangdelu.stretchez

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Button
import android.widget.TextView
import android.widget.VideoView
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import me.shangdelu.stretchez.database.StretchExercise
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern

private const val ARG_PLAN_ID = "plan_id"

interface CountDownTimerCallBacks {
    //Called when the state of cdTimer need to be changed
    fun timerPause()

    fun timerResume()
}

class WorkOutFragment : Fragment(), CountDownTimerCallBacks {

    private var cdTimerText: TextView? = null
    private lateinit var webView: WebView
    private lateinit var videoView: VideoView
    private lateinit var repeatButton: Button
    private lateinit var returnButton: Button
    private lateinit var workOutRepository: StretchPlanRepository
    private var stretchPlanID: UUID? = null
    private lateinit var exercises: List<StretchExercise>
    //Duration of current Exercise in list of stretchExercise
    private var currentExerciseDuration = 0
    //length of interval between exercises
    private var interval = 0
    //variable for MediaPlayer to implement pause and resume feature
    private lateinit var mediaPlayer: MediaPlayer
    //boolean flag to determine the state of mediaPlayer
    private var playingFlag: Boolean = true
    //boolean flag to determine if the video is resume from pausing or starting for the first time
    private var youtubeVideoResume: Boolean = false
    //boolean flag to determine if the video is from youtubeVideo (not using videoView but webView)
    private var fromYoutube: Boolean = true
    //variable for timer to be accessed by other function
    private lateinit var timer: CountDownTimer
    //variable for remaining time on the timer
    private var timeRemain: Long = 0
    //initialize ExerciseControl
    private val exerciseControl = ExerciseControl()
    private var intervalTextView: TextView? = null
    //jsBridge object that pass the videoID to youtube iFrame api
    private lateinit var jsBridge: JSBridge

    //Associate the Fragment with the ViewModel
    private val workOutFragmentViewModel: WorkOutFragmentViewModel by lazy {
        ViewModelProvider(this)[WorkOutFragmentViewModel::class.java]
    }


    //Receive message from webView and pass on to native
    class JSBridge(private val videoID: String) {

        private var cdTimerCallbacks: CountDownTimerCallBacks? = null

        fun setCallbackInterface(cdTimerCallbacks: CountDownTimerCallBacks) {
            this.cdTimerCallbacks = cdTimerCallbacks
        }

        @JavascriptInterface
        fun showMessageInNative(): String {
            //Received videoID from webView in native and pass it to JavaScript file.
            return videoID
        }

        @JavascriptInterface
        fun getDataFromJS(videoLifeCycle: String) {
            //if JS passed "Paused" back, the countdownTimer should be paused
            if (videoLifeCycle == "Paused") {
                //trigger the callback to pause the timer
                cdTimerCallbacks?.timerPause()
            }
            //if JS passed "Playing" back, the countdownTimer should be running
            if (videoLifeCycle == "Playing") {
                //trigger the callback to resume the timer
                cdTimerCallbacks?.timerResume()
            }
        }
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
            val current = currentExercise()
            //Get the duration of current exercise of the plan
            currentExerciseDuration = current.exerciseDuration

            //load the youtube iframe api
            webView.loadUrl("file:///android_res/raw/youtubeiframeapi.html")

            //set the link of first exercise in exercises for webView
            val exerciseLink = currentExercise().exerciseLink

            //call getYoutubeVideoID to get the videoID for current exercise
            val currentVideoID = getYoutubeVideoID(exerciseLink)

            //create the object of JSBridge class
            jsBridge = JSBridge(currentVideoID)
            //initialize the interface setter
            jsBridge.setCallbackInterface(this)

            //set JavaScript Interface with videoID
            webView.addJavascriptInterface(jsBridge, "JSBridge")

            //if JavaScript usage is not required, delete this line.
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.settings.allowFileAccess = true

            //check if a videoID is successfully returned
            if (currentVideoID != "") { //if currentVideoID is not an empty string, means a videoID has been returned.

                //show the webView as current video is a youtube video
                webView.visibility = View.VISIBLE

                //change the flag fromYoutube to true
                fromYoutube = true


            } else { //currentVideoID is an empty string, meaning it's not a youtube video
                //show the videoView as current video is not a youtube video.
                videoView.visibility = View.VISIBLE

                //change the flag fromYoutube to false
                fromYoutube = false

                //Start the countdown timer
                timerStart(current.exerciseDuration.toLong() * 1000)

                //set the link of first exercise in exercises for videoView
                videoView.setVideoURI(Uri.parse(currentExercise().exerciseLink))

                //Start playing the exercise video
                videoView.start()
            }
        }

        //set webView
        webView = view.findViewById(R.id.webVideo) as WebView
        //hide the webView
        webView.visibility = View.INVISIBLE

        //set videoView
        videoView = view.findViewById(R.id.workOutVideo) as VideoView
        videoView.visibility = View.INVISIBLE

        //set countdown timer text
        cdTimerText = view.findViewById(R.id.cdTimer) as TextView

        repeatButton = view.findViewById(R.id.repeat_button) as Button
        returnButton = view.findViewById(R.id.return_button) as Button
        intervalTextView = view.findViewById(R.id.interval_textView) as TextView

        //grey out the background of the repeatButton to notify user it is disabled
        repeatButton.background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(requireContext().getColor(R.color.grey), BlendModeCompat.SRC_ATOP)

        //Button to return to stretch plan list
        returnButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_work_out_to_navigation_stretch_plan_list)
        }

        videoView.setOnPreparedListener { mp ->
            //get a reference of mp
            mediaPlayer = mp
            //loop the demonstration of current exercise
            mp.isLooping = true
            //requestFocus on videoView to prevent extra click needed to requestFocus.
            videoView.requestFocus()
        }

        //set a onClickListener to control the state of the mediaPlayer.
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

        //TODO 0: Ask about why ErrorTextAppearance is not working in xml file.
        //TODO 0.1: Ask if there are better ways to initialize youtubeiframeapi when the first video is not a youtube video.
        //TODO 0.2: Add icon or image in workoutFragment after the stretchPlan is complete. (To congratulate user for finishing the plan)
        //TODO 0.3: User should not be able to start stretching if current plan does not contain any exercise yet.
        //TODO 0.4: Notify user how many exercise is in the current plan (at least notify user when current plan is empty with no exercise at all).
        //TODO 1: Learn about SharedPreference, ActivityLifeCycleCallbacks and observeForever.
        //TODO 2: Possible Feature: Try setting the Listener on a separate button and get feedback
        //TODO 3: Possible feature: User need to enter edit mode to make changes on existing plan and exercise instead of directly make changes.
        //TODO 4: Possible feature: Schedule Planner and notification before the scheduled plan.
        //TODO 4.1: Learn about time picker, as it can be used to choose date/time and schedule the event.
        //TODO 5: Version 1: User can create a new plan, add exercise to plan and finish a stretching process without any guidance.

        //Feedback 1: More user guidance visually, so first time user can have a more smooth experience.
        //1.1: Notify user to click the "+" button on top right when the stretch plan list is empty.
        //Feedback 2: Consider adding different language options (such as Chinese).
        //Feedback 3: Swipe to delete and tap screen to pause did not determined by the user until mentioned.
        //3.1: Consider adding a pause/play button instead of tap the screen.
        //3.2: Consider adding a separate delete button while still enable swipe to delete.
        //Feedback 4: Home screen looks too empty and sometimes may lead to confusion.

        return view
    }

    private fun getYoutubeVideoID(videoUrl: String): String {
        //Use pattern and matcher to get the youtube video ID from the URL link
        val youtubePattern = "(?<=watch]]?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|watch\\?v%3D|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*"
        val compiledPattern: Pattern = Pattern.compile(youtubePattern)
        val matcher: Matcher = compiledPattern.matcher(videoUrl)

        //check if the link matches the pattern
        if (matcher.find()) {
            //if the pattern is matched, return the videoID of the given url
            return matcher.group()
        }
        //otherwise, return an empty string meaning that pattern is not matched
        return ""
    }

    private fun currentExercise(): StretchExercise {
        return exerciseControl.getCurrent(exercises)
    }

    private fun repeat() {
        //reset the stretch exercise list back to index 0
        val reset = exerciseControl.reset(exercises)

        //reset the youtubeVideoResume flag to false, since new exercise is incoming
        youtubeVideoResume = false

        //call getYoutubeVideoID to get the videoID for the first exercise
        //and check if it's a youtube video link
        val firstVideoID = getYoutubeVideoID(reset.exerciseLink)

        //check if a videoID is successfully returned
        if (firstVideoID != "") { //if firstVideoID is not an empty string, means a videoID has been returned.
            //change the flag fromYoutube to true
            fromYoutube = true
            //use evaluateJavascript to call the Queueing functions in the iframe api to play the first video.
            webView.evaluateJavascript("playVideoById('$firstVideoID')", null)
        } else { //firstVideoID is an empty string, meaning it's not a youtube video
            //change the flag fromYoutube to false
            fromYoutube = false
            //reset the timer with the duration of first exercise
            timerStart(reset.exerciseDuration.toLong() * 1000)
            //show videoView
            videoView.visibility = View.VISIBLE
            //reset video path and start playing
            videoView.setVideoURI(Uri.parse(reset.exerciseLink))
            videoView.start()
        }
        //make the repeat disabled
        repeatButton.isEnabled = false
        //grey out the background of the repeatButton to notify user it is disabled
        repeatButton.background.colorFilter = BlendModeColorFilterCompat
            .createBlendModeColorFilterCompat(requireContext().getColor(R.color.grey), BlendModeCompat.SRC_ATOP)
        //change the text on cdTimer to notify the user repeat has processed.
        cdTimerText?.text = getString(R.string.default_time)
    }


    private fun intervalStart(timeLengthMilli: Long) {
        //Hide webView
        webView.visibility = View.INVISIBLE

        //Hide videoView
        videoView.visibility = View.INVISIBLE

        //Show the intervalTextView
        intervalTextView?.visibility = View.VISIBLE
        val nextExercise = resources.getString(R.string.next_exercise) + " " + exerciseControl.peekNext(exercises).exerciseName
        intervalTextView?.text = nextExercise
        val next = exerciseControl.moveNext(exercises) //move to the next exercise


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

                //reset the youtubeVideoResume flag to false, since new exercise is incoming
                youtubeVideoResume = false
                //set the exercise duration to that of the next exercise
                currentExerciseDuration = next.exerciseDuration

                //get the videoID for the next exercise coming up
                val nextVideoID = getYoutubeVideoID(next.exerciseLink)

                //check if a videoID is successfully returned
                if (nextVideoID != "") { //if nextVideoID is not an empty string, means a videoID has been returned.
                    //change the flag fromYoutube to true
                    fromYoutube = true

                    //Show webView
                    webView.visibility = View.VISIBLE

                    //use evaluateJavascript to call the Queueing functions in the iframe api to play the next video.
                    webView.evaluateJavascript("playVideoById('$nextVideoID')", null)
                } else { //nextVideoID is an empty string, meaning it's not a youtube video
                    //change the flag fromYoutube to false
                    fromYoutube = false

                    //Show videoView
                    videoView.visibility = View.VISIBLE

                    //setVideoURI as the link of next exercise in the stretchPlan
                    videoView.setVideoURI(Uri.parse(next.exerciseLink))

                    //reset and start the countdownTimer
                    timerStart(next.exerciseDuration.toLong() * 1000)

                    //start playing the next exercise
                    videoView.start()
                }
            }
        }
        intervalTimer.start()
    }
    private fun timerStart(timeLengthMilli: Long) {

        //Use runOnUiThread as only the original thread that created a view hierarchy can touch its views
        activity?.runOnUiThread {
            timer = object : CountDownTimer(timeLengthMilli, 1000) {
                override fun onTick(milliTillFinish: Long) {
                    cdTimerText?.text = getString(
                        R.string.formatted_time,
                        TimeUnit.MILLISECONDS.toMinutes(milliTillFinish) % 60,
                        TimeUnit.MILLISECONDS.toSeconds(milliTillFinish) % 60
                    )
                    //record the remaining time
                    timeRemain = milliTillFinish
                }

                override fun onFinish() {
                    //if currentExercise is not the last exercise of the stretchPlan
                    if (!exerciseControl.endOfList(exercises)) {
                        //stop and release the media player instance and move to idle state
                        videoView.stopPlayback()
                        //stop the current video as countdown timer finished
                        webView.evaluateJavascript("stopVideo()", null)
                        //Start the interval countdown timer
                        intervalStart(interval.toLong() * 1000)
                    } else {
                        //currentExercise is the last exercise of the stretchPlan
                        //stop and release the media player instance and move to idle state
                        videoView.stopPlayback()

                        //clear the last frame of videoView to prevent onClickListener accidentally triggered
                        videoView.visibility = View.GONE

                        //use evaluateJavascript to call stopVideo() in the javascript file
                        //stop the current video as countdown timer finished
                        webView.evaluateJavascript("stopVideo()", null)

                        //show "Complete" on cdTimer
                        cdTimerText?.text = getString(R.string.complete_time)
                        //enable the repeat button
                        repeatButton.isEnabled = true
                        //change the background of the repeatButton back to normal notifying the button is now enabled
                        repeatButton.background.colorFilter = BlendModeColorFilterCompat
                            .createBlendModeColorFilterCompat(requireContext().getColor(R.color.bright_lavender), BlendModeCompat.SRC_ATOP)
                        //Button to repeat current exercise
                        repeatButton.setOnClickListener {
                            //Reset the timer instead of replace the fragment
                            repeat()
                        }
                    }
                }
            }
            //timer.start() need to be included in the runOnUiThread{}, otherwise the old timer will be called instead
            timer.start()
        }
    }


    override fun timerPause() {
        //stop the current timer
        timer.cancel()
    }

    override fun timerResume() {
        if (!youtubeVideoResume && fromYoutube) {
            //youtubeVideoResume is false means the video is playing for the first time
            //start the countdown timer with current exercise duration
            timerStart(currentExerciseDuration.toLong() * 1000)
            //change the boolean flag to true as all calls after this will be video resuming
            youtubeVideoResume = true
        } else {
            //youtubeVideoResume is true means the video is resuming from pausing state
            //start a new timer with the time remaining
            timerStart(timeRemain)
        }
    }


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