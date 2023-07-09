package com.example.aviator

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.aviator.databinding.FragmentGameBinding
import kotlin.concurrent.thread

class GameFragment : Fragment() {

  private var initialX = 0f
  private var offsetX = 0f
  private var keepGoing = true

  private val planeRect = Rect()
  private val smallMedKitRect = Rect()
  private val bigMedKitRect = Rect()
  private val enemyPlaneRect = Rect()
  private val bulletRect = Rect()
  private val enemyBulletRect = Rect()

  private val binding by lazy {
    FragmentGameBinding.inflate(layoutInflater)
  }

  private val viewModel by lazy {
    ViewModelProvider(this)[GameViewModel::class.java]
  }

  private val screenWidth by lazy {
    resources.displayMetrics.widthPixels - 300
  }
  private val screenHeight by lazy {
    resources.displayMetrics.heightPixels
  }

  private val healthPoints by lazy {
    listOf(
      binding.topBar.healthBar.health1,
      binding.topBar.healthBar.health2,
      binding.topBar.healthBar.health3,
      binding.topBar.healthBar.health4,
      binding.topBar.healthBar.health5,
      binding.topBar.healthBar.health6,
      binding.topBar.healthBar.health7,
      binding.topBar.healthBar.health8,
      binding.topBar.healthBar.health9,
      binding.topBar.healthBar.health10,
      binding.topBar.healthBar.health11,
      binding.topBar.healthBar.health12
    )
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return binding.root
  }

  private fun handleTouch(event: MotionEvent) {
    when (event.action) {
      MotionEvent.ACTION_DOWN -> {
        initialX = binding.plane.x
        offsetX = event.rawX - binding.plane.x
      }

      MotionEvent.ACTION_MOVE -> {
        val newX = event.rawX - offsetX
        binding.plane.x = newX
      }
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.plane.setOnTouchListener { _, motionEvent ->
      handleTouch(motionEvent)
      true
    }
    initObjects()
    startFall(binding.smallMedkit, SMALL_MED_KIT)
    startFall(binding.bigMedkit, BIG_MED_KIT)
    startFall(binding.enemyPlane, ENEMY_PLANE)
    startBullet(DOWN, binding.enemyPlane)
    startBullet(UP, binding.plane)

    viewModel.score.observe(viewLifecycleOwner) {
      binding.topBar.score.text = it.toString()
    }

    viewModel.health.observe(viewLifecycleOwner) {
      for (i in 0 until it) {
        healthPoints[i].isVisible = true
      }
      for (i in it until MAX_HEALTH)
        healthPoints[i].isVisible = false
      if (it == 0) {
        findNavController().navigate(
          GameFragmentDirections.actionGameFragmentToGameOverFragment(viewModel.score.value ?: 0)
        )
      }
    }
    binding.topBar.menu.setOnClickListener {
      findNavController().navigate(R.id.toMenu)
    }
  }

  private fun initObjects() {
    binding.smallMedkit.y = SMALL_MED_KIT.appearDelayHeight
    binding.smallMedkit.x = (0..screenWidth).random().toFloat()
    binding.bigMedkit.y = BIG_MED_KIT.appearDelayHeight
    binding.bigMedkit.x = (0..screenWidth).random().toFloat()
  }

  private fun startFall(view: ImageView, fallingObject: FallingObject) {
    val collisionHandler: () -> Unit = {
      when (fallingObject.id) {
        SMALL_MED_KIT.id -> {
          handleSmallMed()
        }

        BIG_MED_KIT.id -> {
          handleBigMed()
        }

        ENEMY_PLANE.id -> {
          //no collision handling
        }
      }
    }
    thread {
      val handler = Handler(Looper.getMainLooper())
      handler.post(object : Runnable {
        override fun run() {
          if (keepGoing) {
            collisionHandler()
            view.y += 10
            if (view.y < screenHeight) {
              handler.postDelayed(this, fallingObject.fallingSpeed)
            } else {
              goUp(view, fallingObject.appearDelayHeight)
              handler.postDelayed(this, fallingObject.fallingSpeed)
            }
          }
        }
      })
    }
  }

  private fun startBullet(direction: Boolean, plane: ImageView) {
    thread {
      if (direction == DOWN) {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
          override fun run() {
            if (keepGoing) {
              handleEnemyBullet(Pair(plane.x + PLANE_SIZE / 2, plane.y + PLANE_SIZE))
              binding.enemyBullet.y += 20
              if (binding.enemyBullet.y < screenHeight + 600) {
                handler.postDelayed(this, ENEMY_BULLET.fallingSpeed)
              } else {
                binding.enemyBullet.x = plane.x + PLANE_SIZE / 2
                binding.enemyBullet.y = plane.y + PLANE_SIZE
                handler.postDelayed(this, ENEMY_BULLET.fallingSpeed)
              }
            }
          }
        })
      } else {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
          override fun run() {
            if (keepGoing) {
              handleBullet(Pair(plane.x + PLANE_SIZE / 2, plane.y + PLANE_SIZE))
              binding.bullet.y -= 20
              if (binding.bullet.y > 0) {
                handler.postDelayed(this, ENEMY_BULLET.fallingSpeed)
              } else {
                binding.bullet.x = plane.x + PLANE_SIZE / 2
                binding.bullet.y = plane.y + PLANE_SIZE
                handler.postDelayed(this, ENEMY_BULLET.fallingSpeed)
              }
            }
          }
        })
      }
    }
  }


  private fun handleSmallMed() {
    binding.plane.getHitRect(planeRect)
    binding.smallMedkit.getHitRect(smallMedKitRect)
    if (Rect.intersects(planeRect, smallMedKitRect)) {
      goUp(binding.smallMedkit, SMALL_MED_KIT.appearDelayHeight)
      viewModel.getSmallMedKit()
    }
  }

  private fun handleBigMed() {
    binding.plane.getHitRect(planeRect)
    binding.bigMedkit.getHitRect(bigMedKitRect)
    if (Rect.intersects(planeRect, bigMedKitRect)) {
      goUp(binding.bigMedkit, BIG_MED_KIT.appearDelayHeight)
      viewModel.getBigMedKit()
    }
  }

  private fun handleEnemyBullet(currentPlanePos: Pair<Float, Float>) {
    binding.plane.getHitRect(planeRect)
    binding.enemyBullet.getHitRect(enemyBulletRect)
    if (Rect.intersects(planeRect, enemyBulletRect)) {
      binding.enemyBullet.x = currentPlanePos.first
      binding.enemyBullet.y = currentPlanePos.second
      viewModel.getHit()
    }
  }

  private fun handleBullet(currentPlanePos: Pair<Float, Float>) {
    binding.enemyPlane.getHitRect(enemyPlaneRect)
    binding.bullet.getHitRect(bulletRect)
    if (Rect.intersects(enemyPlaneRect, bulletRect)) {
      binding.bullet.x = currentPlanePos.first
      binding.bullet.y = currentPlanePos.second
      //viewModel.getHit()
      destroyEnemyPlane()
    }
  }

  private fun destroyEnemyPlane() {
    viewModel.destroyEnemy()
    binding.enemyPlane.y = ENEMY_PLANE.appearDelayHeight
    binding.enemyPlane.x = (0..screenWidth).random().toFloat()
  }

  private fun goUp(view: ImageView, delay: Float) {
    view.y = delay
    view.x = (0..screenWidth).random().toFloat()
  }

  override fun onDestroy() {
    super.onDestroy()
    keepGoing = false
  }

  companion object {
    private const val PLANE_SIZE = 200
    private const val SMALL_MED_KIT_ID = 0
    private const val BIG_MED_KIT_ID = 1
    private const val ENEMY_PLANE_ID = 2
    private const val ENEMY_BULLET_ID = 3
    private const val UP = true
    private const val DOWN = false
    private const val MAX_HEALTH = 12


    private val SMALL_MED_KIT = FallingObject(
      SMALL_MED_KIT_ID,
      -1500f,
      20L
    )


    private val BIG_MED_KIT = FallingObject(
      BIG_MED_KIT_ID,
      -4000f,
      30L
    )

    private val ENEMY_PLANE = FallingObject(
      ENEMY_PLANE_ID,
      -200f,
      40L
    )
    private val ENEMY_BULLET = FallingObject(
      ENEMY_BULLET_ID,
      0f,
      3L
    )
  }

  data class FallingObject(
    val id: Int,
    val appearDelayHeight: Float,
    val fallingSpeed: Long
  )
}