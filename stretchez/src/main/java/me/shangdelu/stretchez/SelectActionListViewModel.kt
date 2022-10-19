package me.shangdelu.stretchez

import androidx.lifecycle.ViewModel

class SelectActionListViewModel : ViewModel() {

    val actions = mutableListOf<Action>()

    init {
        //testing, need update stretching exercise with demo
        for (i in 0 until 10) {
            val action = Action()
            action.title = "Action #$i"
            actions += action
        }
    }

}