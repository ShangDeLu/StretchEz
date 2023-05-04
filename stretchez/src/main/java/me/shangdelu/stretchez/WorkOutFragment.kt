package me.shangdelu.stretchez

import android.media.MediaPlayer
import android.net.Uri
import android.opengl.Visibility
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

            //check if a videoID is successfully returned
            if (currentVideoID != "") { //if currentVideoID is not an empty string, means a videoID has been returned.

                //TODO: Ask if this part of code is actually meaningful, or can be deleted
                //setting web client with non-deprecated function
//                webView.webViewClient = object: WebViewClient() {
//                    override fun shouldOverrideUrlLoading(
//                        view: WebView?,
//                        request: WebResourceRequest?
//                    ): Boolean {
//                        return false
//                    }
//                }

                //show the webView as current video is a youtube video
                webView.visibility = View.VISIBLE

                //change the flag fromYoutube to true
                fromYoutube = true

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

            } else { //currentVideoID is an empty string, meaning it's not a youtube video
                //show the videoView as current video is not a youtube video.
                videoView.visibility = View.VISIBLE

                //change the flag fromYoutube to false
                fromYoutube = false

                //Start the countdown timer
                timerStart(current.exerciseDuration.toLong() * 1000)

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

        //set the link of first exercise in exercises for videoView
        //videoView.setVideoURI(Uri.parse(exercises[currentExercise].exerciseLink))
        videoView.setVideoURI(Uri.parse("android.resource://" + requireContext().packageName + "/" + R.raw.stretch1))
        videoView.setOnPreparedListener { mp ->
            //get a reference of mp
            mediaPlayer = mp
            //loop the demonstration of current exercise
            mp.isLooping = true
        }

        //set a onClickListener to control the state of the mediaPlayer.
        //TODO: Make sure this won't be triggered after countdownTimer stops
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

        //TODO 1: Make sure the videoView option is also working correctly, and won't have any contradiction with webView.
        //TODO 2: Make sure the spacing of each part of the view is right (Try constraintlayout.widget.Barrier)
        //TODO 3: Find out why there are blank space on top of the cdTimer.
        //TODO 4: Make sure exercise with youtube link and exercise with local resource can coexist in the same plan.
        //TODO 4: Add the duration feature, so the user can choose the duration for each exercise.
        //TODO 5: Possible feature: Schedule Planner and notification before the scheduled plan.



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
            //reset video path and start playing
            videoView.setVideoURI(Uri.parse("android.resource://" + requireContext().packageName + "/" + R.raw.stretch1))
            videoView.start()
        }
        //make the repeat and return button disabled
        repeatButton.isEnabled = false
        returnButton.isEnabled = false
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
                    //videoView.setVideoURI(Uri.parse(next.exerciseLink))
                    videoView.setVideoURI(Uri.parse("android.resource://" + requireContext().packageName + "/" + R.raw.stretch3))

                    //reset and start the countdownTimer (in the case if videoView is used)
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
                        //Start the interval countdown timer
                        intervalStart(interval.toLong() * 1000)
                    } else {
                        //currentExercise is the last exercise of the stretchPlan
                        //stop and release the media player instance and move to idle state
                        videoView.stopPlayback()

                        //TODO: try setBackgroundResource to see the effect
                        //clear the last frame of videoView to prevent onClickListener accidentally triggered
                        videoView.visibility = View.GONE
                        videoView.visibility = View.VISIBLE

                        //use evaluateJavascript to call stopVideo() in the javascript file
                        //stop the current video as countdown timer finished
                        webView.evaluateJavascript("stopVideo()", null)

                        cdTimerText?.text = getString(R.string.complete_time)
                        //enable the repeat and return button
                        repeatButton.isEnabled = true
                        returnButton.isEnabled = true
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
            //timer.start() need to be included in the runOnUiThread{}, otherwise the old timer will be called instead
            timer.start()
        }
    }


    override fun timerPause() {
        //test why the first click on videoView don't have any effect
        println("pause")
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