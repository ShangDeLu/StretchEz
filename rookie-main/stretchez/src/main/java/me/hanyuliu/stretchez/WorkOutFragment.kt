package me.hanyuliu.stretchez

import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import me.hanyuliu.stretchez.ui.main.StretchStartFragment
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
                mp?.isLooping = true
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
            cdTimerText?.text = getString(R.string.formatted_time,
                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60,
             TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60)
        }

        override fun onFinish() {
            videoView.stopPlayback()
            cdTimerText?.text = getString(R.string.complete_time)
            congratulations?.visibility = View.VISIBLE
            finishLayout.visibility = View.VISIBLE

            repeatButton.setOnClickListener {
                val workoutFragment = WorkOutFragment()
                val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
                transaction.replace(R.id.fragment_container, workoutFragment)
                transaction.commit()
            }

            returnButton.setOnClickListener {
                val stretchStartFragment = StretchStartFragment()
                val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
                transaction.replace(R.id.fragment_container, stretchStartFragment)
                transaction.commit()
            }
        }
    }

    companion object {
        fun newInstance() = WorkOutFragment()
    }
}