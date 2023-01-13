package me.shangdelu.stretchez.uploadStretchPlan

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
import java.util.*

class UploadStretchPlanFragment: Fragment() {

    private var TAG = "UploadStretchPlanTest"

    private lateinit var uploadStretchPlanTextView: TextView
    private lateinit var uploadStretchPlanButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_upload_stretch_plan, container, false)

        uploadStretchPlanTextView = view.findViewById(R.id.upload_stretchPlan_textView) as TextView
        uploadStretchPlanButton = view.findViewById(R.id.upload_stretchPlan_button) as Button

        //Click the button to upload StretchPlan
        uploadStretchPlanButton.setOnClickListener {
            uploadCurrentPlan()
        }

        return view
    }

    private fun uploadCurrentPlan() {
        //set up test StretchPlan data to upload
        val testPlanID = UUID.randomUUID()
        val testUserID = UUID.fromString("42ad0079-cdf7-48a1-960d-7746473f6688")
        val currentPlan = UploadStretchPlan(testPlanID, testUserID, "Mobile Test Plan1",
                                    "Test Uploading Plan", 0, 100)

        //Build Retrofit
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.4.63:7000") //MyMac's Address
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        //Build UploadStretchPlanAPI
        val uploadStretchPlanAPI: UploadStretchPlanAPI = retrofit.create(UploadStretchPlanAPI::class.java)
        uploadStretchPlanAPI.uploadStretchPlan(currentPlan).enqueue(object: Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Log.d(TAG, "Called") //upload successful to remote
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.d(TAG, "Failed") //upload failed to remote
            }
        })

    }


    companion object {
        fun newInstance() = UploadStretchPlanFragment()
    }


}

