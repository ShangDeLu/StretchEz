package me.shangdelu.stretchez.raspberryPi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import me.shangdelu.stretchez.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RaspberryFragment : Fragment() {

    private var TAG = "RaspTest"

    private lateinit var piTextView: TextView
    private lateinit var piTestButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pi, container, false)

        piTextView = view.findViewById(R.id.pi_textView) as TextView
        piTestButton = view.findViewById(R.id.pi_testButton) as Button

        piTestButton.setOnClickListener {
            getCurrentData()
        }

        return view
    }

    private fun getCurrentData() {
        val retrofit: Retrofit = Retrofit.Builder()
            //.baseUrl("http://10.0.0.51:7000/") //Home address
            .baseUrl("http://192.168.5.94:7000") //Hanyu Home address
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val raspberryAPI: RaspberryAPI = retrofit.create(RaspberryAPI::class.java)

        val raspberryRequest : Call<List<RaspberryItem>> = raspberryAPI.fetchContents()
        raspberryRequest.enqueue(object : Callback<List<RaspberryItem>> {
            override fun onFailure(call: Call<List<RaspberryItem>>, t: Throwable) {
                Log.d(TAG, t.toString())
                piTextView.setText(R.string.failure_message) //failed to retrieve from pi
            }

            override fun onResponse( //show the data retrieved from pi
                call: Call<List<RaspberryItem>>,
                response: Response<List<RaspberryItem>>
            ) {
                val data = response.body()
                Log.d(TAG, data.toString())
                piTextView.text = data.toString()
            }
        })
    }

    companion object {
        fun newInstance() = RaspberryFragment()
    }
}