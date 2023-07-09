package com.example.aviator

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.aviator.databinding.FragmentGameOverBinding

class GameOverFragment : Fragment() {
  private val args by navArgs<GameOverFragmentArgs>()
  private val binding by lazy {
    FragmentGameOverBinding.inflate(layoutInflater)
  }
  private val bestScore by lazy {
    requireActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(
      BEST_SCORE, 0
    )
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val currScore = args.score
    binding.score.score.text = currScore.toString()
    if (currScore > bestScore) {
      binding.score.scoreRoot.background =
        ResourcesCompat.getDrawable(resources, R.drawable.text_box02, null)
      requireActivity().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        .putInt(BEST_SCORE, currScore).apply()
      binding.bestScore.text = currScore.toString()
      binding.score.title.text = getString(R.string.new_best)
    } else {
      binding.bestScore.text = bestScore.toString()
    }
    binding.menu.setOnClickListener {
      findNavController().navigate(R.id.toMenu)
    }
    binding.score.okButton.setOnClickListener {
      findNavController().navigate(R.id.action_gameOverFragment_to_gameFragment)
    }
    binding.score.closeButton.setOnClickListener {
      findNavController().navigate(R.id.toMenu)
    }
  }

  companion object {
    const val PREFS = "prefs"
    const val BEST_SCORE = "best_score"
  }
}