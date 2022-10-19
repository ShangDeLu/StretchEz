package me.shangdelu.stretchez

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import java.util.*

class StretchPlanDetailViewModel() : ViewModel() {

    private val stretchPlanRepository = StretchPlanRepository.get()
    private val stretchPlanIdLiveData = MutableLiveData<UUID>()

    var stretchPlanLiveData: LiveData<StretchPlan?> =
        Transformations.switchMap(stretchPlanIdLiveData) { stretchPlanId ->
            stretchPlanRepository.getStretchPlan(stretchPlanId)
        }

    fun loadStretchPlan(stretchPlanId: UUID) {
        stretchPlanIdLiveData.value = stretchPlanId
    }

    fun saveStretchPlan(stretchPlan: StretchPlan) {
        stretchPlanRepository.updateStretchPlan(stretchPlan)
    }
}