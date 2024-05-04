package com.catchyapps.whatsdelete.appactivities.activitystatussaver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.catchyapps.whatsdelete.databinding.InfoStatusDilaogLayoutBinding

class HowToUseStatusFragmentScreen : DialogFragment() {
    lateinit var binding: InfoStatusDilaogLayoutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = InfoStatusDilaogLayoutBinding.inflate(
            inflater,
            container,
            false
        )
        binding.ivBack.setOnClickListener { v: View? ->
            dismiss()
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    companion object {
        fun newInstance(): HowToUseStatusFragmentScreen {
            val f = HowToUseStatusFragmentScreen()
            val args = Bundle()
            f.arguments = args
            return f
        }
    }
}