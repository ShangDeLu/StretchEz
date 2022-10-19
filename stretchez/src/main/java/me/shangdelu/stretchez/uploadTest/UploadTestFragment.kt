package me.shangdelu.stretchez.uploadTest

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

class UploadTestFragment: Fragment() {

    private var TAG = "UploadTest"

    private lateinit var uploadTextView: TextView
    private lateinit var uploadTestButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_upload, container, false)

        uploadTextView = view.findViewById(R.id.upload_textView) as TextView
        uploadTestButton = view.findViewById(R.id.upload_testButton) as Button

        uploadTestButton.setOnClickListener {
            uploadCurrent()
        }

        return view
    }

    private fun uploadCurrent() {
        // test data to upload
        val current = UploadData("001", "Bob")

        val retrofit: Retrofit = Retrofit.Builder()
            //.baseUrl("http://10.0.0.51:7000/") //Home address
            .baseUrl("http://192.168.5.94:7000") //Hanyu Home address
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val uploadTestAPI: UploadTestAPI = retrofit.create(UploadTestAPI::class.java)
        uploadTestAPI.uploadContents(current).enqueue(object: Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d(TAG, "Called") //upload successful to pi
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d(TAG, "Failed") //upload failed to pi
            }
        })
    }

    companion object {
        fun newInstance() = UploadTestFragment()
    }
}