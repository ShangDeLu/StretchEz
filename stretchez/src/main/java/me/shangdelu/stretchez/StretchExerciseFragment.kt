package me.shangdelu.stretchez

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import me.shangdelu.stretchez.database.StretchExercise
import java.util.regex.Matcher
import java.util.regex.Pattern

private const val TAG = "StretchExerciseFragment"
private const val ARG_EXERCISE_ID = "exercise_id"
private const val NAME_ERROR = "Exercise Name cannot be empty"
private const val LINK_ERROR = "Invalid Video Link"

class StretchExerciseFragment : Fragment() {

    private lateinit var exercise: StretchExercise
    private lateinit var stretchExerciseNameContainer: TextInputLayout
    private lateinit var stretchExerciseLinkContainer: TextInputLayout
    private lateinit var stretchExerciseName: TextInputEditText
    private lateinit var stretchExerciseDescription: TextInputEditText
    private lateinit var stretchExerciseLink: TextInputEditText
    private lateinit var stretchExerciseSaveButton: Button
    private lateinit var stretchExerciseCancelButton: Button
    private lateinit var stretchExerciseRepository: StretchPlanRepository
    private var argumentOption: Int = 0
    private var exerciseID: Int = 0
    //Spinner for minute duration of StretchExercise
    private lateinit var minuteSpinner: Spinner
    //Spinner for second duration of StretchExercise
    private lateinit var secondSpinner: Spinner
    //create an array that stores the options of minute for StretchExercise
    private var exerciseMinute = Array(61) {it}
    //create an array that stores the options of second for StretchExercise
    private var exerciseSecond = Array(60) {it}


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

        stretchExerciseName = view.findViewById(R.id.stretch_exercise_name_editText) as TextInputEditText
        stretchExerciseDescription = view.findViewById(R.id.stretch_exercise_description_editText) as TextInputEditText
        stretchExerciseLink = view.findViewById(R.id.stretch_exercise_link_editText) as TextInputEditText
        stretchExerciseSaveButton = view.findViewById(R.id.stretch_exercise_save) as Button
        stretchExerciseCancelButton = view.findViewById(R.id.stretch_exercise_cancel) as Button
        stretchExerciseNameContainer = view.findViewById(R.id.stretch_exercise_name_container) as TextInputLayout
        stretchExerciseLinkContainer = view.findViewById(R.id.stretch_exercise_link_container) as TextInputLayout
        minuteSpinner = view.findViewById(R.id.stretch_exercise_minute_spinner) as Spinner
        secondSpinner = view.findViewById(R.id.stretch_exercise_second_spinner) as Spinner

        //create the instance of ArrayAdapter having the list of minutes and seconds)
        val minuteAdapter = ArrayAdapter(this.requireContext(), android.R.layout.simple_spinner_item, exerciseMinute)
        val secondAdapter = ArrayAdapter(this.requireContext(), android.R.layout.simple_spinner_item, exerciseSecond)

        //set simple layout resource file for each item of spinner
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        secondAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        //set the ArrayAdapter data on the spinner which binds data to spinner
        minuteSpinner.adapter = minuteAdapter
        secondSpinner.adapter = secondAdapter

        //set the focus listener for Exercise Name Input
        exerciseNameFocusListener()
        //set the focus listener for Exercise Link Input
        exerciseLinkFocusListener()

        //Button to save current stretch exercise
        stretchExerciseSaveButton.setOnClickListener {
            //get the item selected on minuteSpinner and save it as Int
            val minute = minuteSpinner.selectedItem as Int
            //get the item selected on secondSpinner and save it as Int
            val second = secondSpinner.selectedItem as Int
            //calculate the duration using selected item on both spinners
            val duration = minute * 60 + second
            //get the exercise link input
            val linkText = stretchExerciseLink.text.toString()

            if (!validExerciseLink(linkText) && exercise.isTemplate == 0) {
                //if the input is an invalid link, and exercise is not a template
                //show an error message to the user
                stretchExerciseLinkContainer.error = LINK_ERROR
            }

            if (stretchExerciseName.text.toString().isEmpty()) {
                //if Exercise Name input is empty, show an error message to the user
                stretchExerciseNameContainer.error = NAME_ERROR
            }

            //Exercise Name input cannot be empty and Exercise Link need to be valid, ignore if it's an template exercise.
            if((stretchExerciseName.text.toString().isNotEmpty() && validExerciseLink(linkText)) || exercise.isTemplate == 1) {
                if (exercise.isTemplate == 1) { //when exercise is an template exercise
                    stretchExerciseRepository.updateExercise(
                        //only update changes in exerciseDuration, while others should be immutable.
                        StretchExercise(
                            exerciseDuration = duration
                        )
                    )
                } else if (argumentOption == 0) { //when exercise already exist, option = 0
                    stretchExerciseRepository.updateExercise(
                        StretchExercise(
                            exerciseID = exerciseID,
                            exerciseName = stretchExerciseName.text.toString(),
                            exerciseDescription = stretchExerciseDescription.text.toString(),
                            exerciseLink = stretchExerciseLink.text.toString(),
                            exerciseDuration = duration
                        )
                    )
                    findNavController().navigate(R.id.action_navigation_exercise_to_navigation_exercise_list)

                } else { //when creating new exercise, option = 1
                    stretchExerciseRepository.addTemplateExercise(
                        StretchExercise(
                            exerciseName = stretchExerciseName.text.toString(),
                            exerciseDescription = stretchExerciseDescription.text.toString(),
                            exerciseLink = stretchExerciseLink.text.toString(),
                            exerciseDuration = duration
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

    private fun exerciseNameFocusListener() {
        //use setOnFocusChangeListener on the exercise name input
        stretchExerciseName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                //Once the Exercise Name Input lose focus, check the input
                if (stretchExerciseName.text.toString().isNotEmpty()) {
                    //If the Exercise Name input is not empty, stop showing the error message
                    stretchExerciseNameContainer.isErrorEnabled = false
                } else {
                    //if Exercise Name input is empty, show an error message to the user
                    stretchExerciseNameContainer.error = NAME_ERROR
                }
            }
        }
    }

    private fun exerciseLinkFocusListener() {
        //use setOnFocusChangeListener on the exercise link input
        stretchExerciseLink.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                //Once the Exercise Link Input lose focus, check the input
                val exerciseLinkText = stretchExerciseLink.text.toString()
                //if the input is an valid link
                if (validExerciseLink(exerciseLinkText)) {
                    //stop showing the error message
                    stretchExerciseLinkContainer.isErrorEnabled = false
                } else {
                    //if the input is an invalid link, show an error message to the user
                    stretchExerciseLinkContainer.error = LINK_ERROR
                }
            }
        }
    }

    private fun validExerciseLink(exerciseLink: String): Boolean {
        //Use pattern and matcher to check if the input link is valid
        val youtubePattern = "(?<=watch]]?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|watch\\?v%3D|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*"
        val compiledPattern: Pattern = Pattern.compile(youtubePattern)
        val matcher: Matcher = compiledPattern.matcher(exerciseLink)

        //check if the link matches the pattern
        if (matcher.find()) {
            //if the pattern is matched, return true
            return true
        }
        //otherwise, return false
        return false
    }


    private fun updateUI() {
        //check the isTemplate of the exercise
        if (exercise.isTemplate == 1) { //if isTemplate is 1, exercise is a template
            //setText for the name and description of the exercise
            stretchExerciseName.setText(exercise.exerciseName)
            stretchExerciseDescription.setText(exercise.exerciseDescription)
            //set ExerciseName and ExerciseDescription to not focusable,
            //user should not be able to change that of template exercise
            stretchExerciseName.isFocusable = false
            stretchExerciseDescription.isFocusable = false
            //do not show link of the local resource of template exercise by disable
            //textInputEditText of ExerciseLink
            stretchExerciseLink.isEnabled = false
        } else { //isTemplate is 0, exercise is not a template
            //setText for the name, description and link of the exercise
            stretchExerciseName.setText(exercise.exerciseName)
            stretchExerciseDescription.setText(exercise.exerciseDescription)
            stretchExerciseLink.setText(exercise.exerciseLink)
        }
        //calculate the minute of the exercise and pass the index to the spinner
        minuteSpinner.setSelection(exercise.exerciseDuration / 60)
        //calculate the second of the exercise and pass the index to the spinner
        secondSpinner.setSelection(exercise.exerciseDuration % 60)
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
