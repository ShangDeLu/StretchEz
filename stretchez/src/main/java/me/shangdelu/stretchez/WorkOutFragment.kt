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
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import me.shangdelu.stretchez.ui.main.ui.home.StretchStartFragment
import java.util.concurrent.TimeUnit

class WorkOutFragment : Fragment() {

    private var cdTimerText: TextView? = null
    private lateinit var videoView: VideoView
    private var congratulations: TextView? = null
    private lateinit var repeatButton: Button
    private lateinit var returnButton: Button
    private lateinit var finishLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_work_out, container, false)

        cdTimerText = view.findViewById(R.id.cdTimer) as TextView
        videoView = view.findViewById(R.id.workOutVideo) as VideoView
        videoView.setVideoURI(Uri.parse("android.resource://" + requireContext().packageName + "/" + R.raw.stretch1))
        videoView.setOnPreparedListener { mp ->
            mp?.isLooping = true //loop the demonstration of current exercise
        }
        videoView.requestFocus()
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
        //reset video path, requestFocus and start playing
        videoView.setVideoURI(Uri.parse("android.resource://" + requireContext().packageName + "/" + R.raw.stretch1))
        videoView.setOnPreparedListener { mp ->
            mp?.isLooping = true //loop the demonstration of current exercise
        }
        videoView.requestFocus()
        videoView.start()
        //hide the repeat and return button
        congratulations?.visibility = View.INVISIBLE
        finishLayout.visibility = View.INVISIBLE
    }


    private var countDownTimer = object : CountDownTimer(30000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            cdTimerText?.text = getString(
                R.string.formatted_time,
                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
             TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60)
        }

        override fun onFinish() {
            //will stop and release the media player instance and move to idle state
            videoView.stopPlayback()
            cdTimerText?.text = getString(R.string.complete_time)
            congratulations?.visibility = View.VISIBLE  //show congratulations after stretching
            finishLayout.visibility = View.VISIBLE  //make repeat and return button visible

            //Button to repeat current exercise
            repeatButton.setOnClickListener {
                //TODO: Reset the timer instead of replace the fragment
                repeat()
            }

            //Button to return to home screen
            returnButton.setOnClickListener {
                findNavController().navigate(R.id.action_navigation_work_out_to_navigation_home)
            }
        }
    }

    companion object {
        fun newInstance() = WorkOutFragment()
    }
}