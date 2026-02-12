package com.example.areumdap.UI.Character

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.example.areumdap.R
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.areumdap.data.source.RetrofitClient
import com.bumptech.glide.Glide
import com.example.areumdap.UI.auth.MainActivity
import com.example.areumdap.data.repository.CharacterViewModelFactory
import com.example.areumdap.databinding.FragmentCharacterXpBinding
import kotlin.getValue
import com.example.areumdap.UI.auth.LoadingDialogFragment
import com.example.areumdap.data.source.TokenManager
import androidx.fragment.app.DialogFragment
import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

class CharacterXpFragment : Fragment() {
    lateinit var binding : FragmentCharacterXpBinding

    private val viewModel: CharacterViewModel by activityViewModels {
        CharacterViewModelFactory(RetrofitClient.service)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCharacterXpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? MainActivity)?.setBottomNavVisibility(true)

        val customMessage = "님의 아름이가 성장 중이에요"
        val loadingDialog = LoadingDialogFragment.newInstance(
            customMessage = customMessage,
            hideFirstLine = true
        )
        loadingDialog.show(parentFragmentManager, "loading_dialog")

        viewModel.fetchCharacterLevel()

        viewModel.characterLevel.observe(viewLifecycleOwner){ data ->
            data?.let {
                // 현재 달성한 레벨
                binding.characterXpLevelTv.text = "${it.displayLevel}"
                
                // 목표 경험치
                val targetXp = it.currentXp + it.maxXp
                binding.characterNextXpTv.text = "$targetXp"

                binding.characterXpIv.visibility = View.VISIBLE
                Glide.with(this)
                    .load(it.imageUrl)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            (parentFragmentManager.findFragmentByTag("loading_dialog") as? DialogFragment)?.dismiss()
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            (parentFragmentManager.findFragmentByTag("loading_dialog") as? DialogFragment)?.dismiss()
                            return false
                        }
                    })
                    .error(R.drawable.ic_character)
                    .into(binding.characterXpIv)
            }
        }

        binding.characterXpCloseIv.setOnClickListener {
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.main_frm, CharacterFragment())
            transaction.commit()
        }
        
        // 에러 메시지 관찰
        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                (parentFragmentManager.findFragmentByTag("loading_dialog") as? DialogFragment)?.dismiss()
                 android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }
}
