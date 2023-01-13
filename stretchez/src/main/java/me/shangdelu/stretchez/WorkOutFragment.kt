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
import me.shangdelu.stretchez.ui.main.StretchStartFragment
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
        videoView.setOnPreparedListener(object: MediaPlayer.OnPreparedListener {
            override fun onPrepared(mp: MediaPlayer?) {
                mp?.isLooping = true //loop the demonstration of current exercise
            }

        })
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


    private var countDownTimer = object : CountDownTimer(30000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            cdTimerText?.text = getString(
                R.string.formatted_time,
                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
             TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60)
        }

        override fun onFinish() {
            videoView.stopPlayback()
            cdTimerText?.text = getString(R.string.complete_time)
            congratulations?.visibility = View.VISIBLE  //show congratulations after stretching
            finishLayout.visibility = View.VISIBLE  //make repeat and return button visible

            //Button to repeat current exercise
            repeatButton.setOnClickListener {
                val workoutFragment = WorkOutFragment()
                val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, workoutFragment)
                transaction.commit()
            }

            //Button to return to home screen
            returnButton.setOnClickListener {
                val stretchStartFragment = StretchStartFragment()
                val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
                transaction.replace(R.id.fragment_container, stretchStartFragment)
                transaction.commit()
            }
        }
    }

    companion object {
        fun newInstance() = WorkOutFragment()
    }
}