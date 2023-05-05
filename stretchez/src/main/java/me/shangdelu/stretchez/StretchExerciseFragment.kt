package me.shangdelu.stretchez

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import me.shangdelu.stretchez.database.StretchExercise

private const val TAG = "StretchExerciseFragment"
private const val ARG_EXERCISE_ID = "exercise_id"

class StretchExerciseFragment : Fragment() {

    private lateinit var exercise: StretchExercise
    private lateinit var stretchExerciseName: EditText
    private lateinit var stretchExerciseDescription: EditText
    private lateinit var stretchExerciseLink: EditText
    private lateinit var stretchExerciseSaveButton: Button
    private lateinit var stretchExerciseCancelButton: Button
    private lateinit var stretchExerciseRepository: StretchPlanRepository
    private var argumentOption: Int = 0
    private var exerciseID: Int = 0
    //Associate the Fragment with the ViewModel
    private val stretchExerciseDetailViewModel: StretchExerciseDetailViewModel by lazy {
        ViewModelProvider(this)[StretchExerciseDetailViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exercise = StretchExercise()
        //Retrieve exerciseID from the fragment arguments
        exerciseID = arguments?.getSerializable(ARG_EXERCISE_ID) as Int
        //request ViewModel to load the stretch Exercise
        exerciseID.let {
            stretchExerciseDetailViewModel.loadExercise(it)
        }
        stretchExerciseRepository = StretchPlanRepository.get()
        argumentOption = arguments?.getInt("Option") ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //inflate the fragment's view
        val view = inflater.inflate(R.layout.fragment_stretch_exercise, container, false)

        stretchExerciseName = view.findViewById(R.id.stretch_exercise_name) as EditText
        stretchExerciseDescription = view.findViewById(R.id.stretch_exercise_description) as EditText
        stretchExerciseLink = view.findViewById(R.id.stretch_exercise_link) as EditText
        stretchExerciseSaveButton = view.findViewById(R.id.stretch_exercise_save) as Button
        stretchExerciseCancelButton = view.findViewById(R.id.stretch_exercise_cancel) as Button

        //Button to save current stretch exercise
        stretchExerciseSaveButton.setOnClickListener {
            if (stretchExerciseName.text.toString().isEmpty()) {
                Toast.makeText(this.context, R.string.no_title_toast, Toast.LENGTH_LONG).show()
            } else {
                if (argumentOption == 0) { //when exercise already exist, option = 0
                    stretchExerciseRepository.updateExercise(
                        StretchExercise(
                            exerciseID = exerciseID,
                            exerciseName = stretchExerciseName.text.toString(),
                            exerciseDescription = stretchExerciseDescription.text.toString(),
                            exerciseLink = stretchExerciseLink.text.toString()
                        )
                    )
                    findNavController().navigate(R.id.action_navigation_exercise_to_navigation_exercise_list)

                } else { //when creating new exercise, option = 1
                    stretchExerciseRepository.addTemplateExercise(
                        StretchExercise(
                            exerciseName = stretchExerciseName.text.toString(),
                            exerciseDescription = stretchExerciseDescription.text.toString(),
                            exerciseLink = stretchExerciseLink.text.toString()
                        )
                    )
                    findNavController().navigate(R.id.action_navigation_exercise_to_navigation_exercise_list)
                }
            }
        }

        //Button to return to list of stretch exercise
        stretchExerciseCancelButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_exercise_to_navigation_exercise_list)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //observe exerciseLiveData, and update the UI any time new data is published
        stretchExerciseDetailViewModel.exerciseLiveData.observe(viewLifecycleOwner)
        { exercise ->
            exercise?.let {
                this.exercise = exercise
                updateUI()
            }
        }
    }

    private fun updateUI() {
        stretchExerciseName.setText(exercise.exerciseName)
        stretchExerciseDescription.setText(exercise.exerciseDescription)
        stretchExerciseLink.setText(exercise.exerciseLink)
    }

    //Use Fragment Arguments to pass the exerciseID, as Fragment arguments help keep fragment encapsulated
    companion object {
        fun newInstance(exerciseID: Int?, option: Int): Bundle {
            //This Bundle contains key-value pairs that work just like the intent extras of an Activity.
            //Each pair is known as an argument.
            return Bundle().apply {
                putSerializable(ARG_EXERCISE_ID, exerciseID)
                putInt("Option", option)
            }
        }
    }
}