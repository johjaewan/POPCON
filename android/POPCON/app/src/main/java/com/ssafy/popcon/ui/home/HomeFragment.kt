package com.ssafy.popcon.ui.home

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.popcon.databinding.FragmentHomeBinding
import com.ssafy.popcon.dto.Badge
import com.ssafy.popcon.dto.Brand
import com.ssafy.popcon.dto.Gifticon
import com.ssafy.popcon.ui.brandtab.BrandTabFragment
import com.ssafy.popcon.ui.common.MainActivity
import com.ssafy.popcon.ui.popup.GifticonDialogFragment
import com.ssafy.popcon.ui.popup.GifticonDialogFragment.Companion.isShow
import com.ssafy.popcon.ui.settings.SettingsFragment
import com.ssafy.popcon.util.ShakeDetector
import com.ssafy.popcon.util.SharedPreferencesUtil
import com.ssafy.popcon.viewmodel.GifticonViewModel
import com.ssafy.popcon.viewmodel.ViewModelFactory

private const val TAG = "HomeFragment"

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var shakeDetector: ShakeDetector
    lateinit var gifticonAdapter: GiftconAdapter
    private lateinit var mainActivity: MainActivity
    private val viewModel: GifticonViewModel by viewModels { ViewModelFactory(requireContext()) }

    override fun onStart() {
        super.onStart()
        mainActivity = activity as MainActivity
    }

    override fun onResume() {
        super.onResume()
        setSensor()
        mainActivity.hideBottomNav(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSetting.setOnClickListener {
            mainActivity.addFragment(SettingsFragment())
        }

        setGifticonAdapter()

    }

    //상단 탭 설정
    @RequiresApi(Build.VERSION_CODES.N)
    private fun setTabAdapter() {
        val brands = mutableListOf<Brand>()
        brands.add(Brand("전체", ""))
        //brands.addAll(viewModel.getBrands())
        brands.add(
            Brand(
                "스타벅스",
                "https://user-images.githubusercontent.com/33195517/211949184-c6e4a8e1-89a2-430c-9ccf-4d0a20546c14.png"
            )
        )
        brands.add(
            Brand(
                "이디야",
                "https://user-images.githubusercontent.com/33195517/214757786-cc0aa65d-dcbb-4b9d-aded-a65cda7f17a6.png"
            )
        )
        brands.add(Brand("히스토리", ""))

        //BrandTabFragment().setBrandTab(brands)
    }

    //홈 기프티콘 어댑터 설정
    private fun setGifticonAdapter() {
        viewModel.getGifticonByUser(SharedPreferencesUtil(requireContext()).getUser())

        viewModel.gifticons.observe(viewLifecycleOwner) {
            gifticonAdapter = GiftconAdapter()
            binding.rvGifticon.apply {
                adapter = gifticonAdapter
                layoutManager = GridLayoutManager(context, 2)
                adapter!!.stateRestorationPolicy =
                    RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            }

            gifticonAdapter.submitList(it)
        }


        /*gifticonAdapter = GiftconAdapter()
        binding.rvGifticon.apply {
            adapter = gifticonAdapter
            layoutManager = GridLayoutManager(context, 2)
            adapter!!.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }

        val gifticonList = mutableListOf<Gifticon>()
        makeList(gifticonList)
        gifticonAdapter.submitList(gifticonList)*/
    }

    //홈화면 켜지면 센서 설정
    private fun setSensor() {
        shakeDetector = ShakeDetector()
        shakeDetector.setOnShakeListener(object : ShakeDetector.OnShakeListener {
            override fun onShake(count: Int) {
                if (!isShow) {
                    activity?.let {
                        GifticonDialogFragment().show(it.supportFragmentManager, "popup")
                    }
                }
            }
        })

        MainActivity().setShakeSensor(requireContext(), shakeDetector)
    }

    private fun makeList(gifticonList: MutableList<Gifticon>) {
        gifticonList.add(
            Gifticon(
                "1234-1234",
                "https://user-images.githubusercontent.com/33195517/214758057-5768a3d2-a441-4ba3-8f68-637143daceb3.png",
                Brand(
                    "스타벅스",
                    "https://user-images.githubusercontent.com/33195517/211949184-c6e4a8e1-89a2-430c-9ccf-4d0a20546c14.png"
                ),

                "2023-01-29 00:00:00.000000",
                -1,
                5000,
                "유라",
                "https://user-images.githubusercontent.com/33195517/214758165-4e216728-cade-45ff-a635-24599384997c.png",
                "아메리카노 T",
                "https://user-images.githubusercontent.com/33195517/214759061-e4fad749-656d-4feb-acf0-f1f579cef0b0.png"
            )
        )
        //
        gifticonList.add(
            Gifticon(
                "1234-1234",
                "https://user-images.githubusercontent.com/33195517/214758057-5768a3d2-a441-4ba3-8f68-637143daceb3.png",
                Brand(
                    "스타벅스",
                    "https://user-images.githubusercontent.com/33195517/211949184-c6e4a8e1-89a2-430c-9ccf-4d0a20546c14.png"
                ),
                "2023-02-10 00:00:00.000000",
                -1,
                30000,
                "유라",
                "https://user-images.githubusercontent.com/33195517/214758165-4e216728-cade-45ff-a635-24599384997c.png",
                "아이스 카페 라떼 T",
                "https://user-images.githubusercontent.com/33195517/214758856-5066c400-9544-4501-a80f-00e0ebceba74.png"
            )
        )
    }
}
