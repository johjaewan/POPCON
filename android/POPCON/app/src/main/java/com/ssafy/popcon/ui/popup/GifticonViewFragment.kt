package com.ssafy.popcon.ui.popup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.ssafy.popcon.databinding.ItemGifticonPopupBinding
import com.ssafy.popcon.dto.Gifticon
import com.ssafy.popcon.ui.history.HistoryDialogFragment

class GifticonViewFragment : Fragment() {
    private var gifticonInfo: Gifticon? = null
    lateinit var binding: ItemGifticonPopupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gifticonInfo = arguments?.getSerializable(EXTRA_KEY_GIFTICON_INFO) as Gifticon
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ItemGifticonPopupBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLayout()
    }

    //금액권, 아닐 경우 레이아웃 설정
    private fun setLayout() {
        binding.gifticon = gifticonInfo
        if (gifticonInfo?.price == null) {
            binding.btnUse.isVisible = true
            binding.btnPrice.isVisible = false
            binding.tvLeft.isVisible = false
        } else {
            binding.btnUse.isVisible = false
            binding.btnPrice.isVisible = true
            binding.tvLeft.isVisible = true
        }

        binding.btnPrice.setOnClickListener {
            val args = Bundle()
            args.putSerializable("gifticon", gifticonInfo)

            val dialogFragment = EditPriceDialogFragment()
            dialogFragment.arguments = args
            dialogFragment.show(childFragmentManager, "editPrice")
        }

        binding.tvLeft.text = gifticonInfo!!.price.toString() + " 원 사용가능"
    }

    companion object {
        private const val EXTRA_KEY_GIFTICON_INFO = "extra_key_gifticon_info"
        fun newInstance(gifticon: Gifticon): GifticonViewFragment {
            val fragment = GifticonViewFragment()
            val args = Bundle()
            args.putSerializable(EXTRA_KEY_GIFTICON_INFO, gifticon)
            fragment.arguments = args
            return fragment
        }
    }
}