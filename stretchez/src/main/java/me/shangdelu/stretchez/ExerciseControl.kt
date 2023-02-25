package me.shangdelu.stretchez

import me.shangdelu.stretchez.database.StretchExercise

class ExerciseControl {

    //current index of List of StretchExercise
    private var curr = 0

    private fun indexIncrement(index: Int): Int {
        curr = index + 1
        return curr
    }

    private fun indexDecrement(index: Int): Int {
        curr = index - 1
        return curr
    }

    private fun indexReset(): Int {
        curr = 0
        return curr
    }

    fun getCurrent(list: List<StretchExercise>): StretchExercise {
        return list[curr]
    }

    fun peekNext(list: List<StretchExercise>): StretchExercise {
        return list[curr + 1]
    }

    fun moveNext(list: List<StretchExercise>): StretchExercise {
        val index = indexIncrement(curr)
        return list[index]
    }

    fun moveBack(list: List<StretchExercise>): StretchExercise {
        val index = indexDecrement(curr)
        return list[index]
    }

    fun reset(list: List<StretchExercise>): StretchExercise {
        val index = indexReset()
        return list[index]
    }

    fun endOfList(list: List<StretchExercise>): Boolean {
        if (curr == list.size - 1) {
            return true
        }
        return false
    }

}

