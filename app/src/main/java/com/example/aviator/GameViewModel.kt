package com.example.aviator

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GameViewModel : ViewModel() {

  private val _score = MutableLiveData<Int>(0)
  val score: LiveData<Int> = _score

  private val _health = MutableLiveData<Int>(12)
  val health: LiveData<Int> = _health

  fun getHit() {
    _health.value = _health.value!!.minus(1)
  }

  fun getSmallMedKit() {
    Log.d("vm", "smallKit")
    if (health.value!! >= 11)
      _health.postValue(12)
    else
      _health.value = _health.value!!.plus(2)
  }

  fun getBigMedKit() {
    _health.postValue(12)
  }

  fun destroyEnemy(){
    _score.value = _score.value!!.plus(10)
  }

  fun gameOver(){

  }
}