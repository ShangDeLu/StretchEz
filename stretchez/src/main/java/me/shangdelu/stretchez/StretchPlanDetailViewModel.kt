package me.shangdelu.stretchez

import androidx.lifecycle.*
import me.shangdelu.stretchez.database.StretchPlan
import java.util.*

class StretchPlanDetailViewModel() : ViewModel() {

    private val stretchPlanRepository = StretchPlanRepository.get()
    private val stretchPlanIdLiveData = MutableLiveData<UUID>()

    var stretchPlanLiveData: LiveData<StretchPlan?> =
        //The old androidx.lifecycle when transformation is not removed yet
//        Transformations.switchMap(stretchPlanIdLiveData) { stretchPlanId ->
//            stretchPlanRepository.getStretchPlan(stretchPlanId)
//        }

        //Trying the new way of using switchMap as androidx.lifecycle.transformation got removed
        stretchPlanIdLiveData.switchMap { stretchPlanId ->
            stretchPlanRepository.getStretchPlan(stretchPlanId)
        }

    fun loadStretchPlan(stretchPlanId: UUID) {
        stretchPlanIdLiveData.value = stretchPlanId
    }

    fun saveStretchPlan(stretchPlan: StretchPlan) {
        stretchPlanRepository.updateStretchPlan(stretchPlan)
    }
}