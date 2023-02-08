package com.ssafy.popcon.ui.add

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.*
import android.provider.MediaStore.Images
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.loader.content.CursorLoader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.soundcloud.android.crop.Crop
import com.ssafy.popcon.R
import com.ssafy.popcon.config.ApplicationClass
import com.ssafy.popcon.databinding.FragmentAddBinding
import com.ssafy.popcon.dto.*
import com.ssafy.popcon.ui.common.EventObserver
import com.ssafy.popcon.ui.common.MainActivity
import com.ssafy.popcon.ui.common.onSingleClickListener
import com.ssafy.popcon.ui.home.HomeFragment
import com.ssafy.popcon.ui.popup.GifticonDialogFragment.Companion.isShow
import com.ssafy.popcon.viewmodel.AddViewModel
import com.ssafy.popcon.viewmodel.ViewModelFactory
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "###_AddFragment"
class AddFragment : Fragment(), onItemClick {
    private lateinit var binding: FragmentAddBinding
    private lateinit var mainActivity: MainActivity
    private val viewModel: AddViewModel by viewModels { ViewModelFactory(requireContext()) }

    private var delImgUris = ArrayList<Uri>()
    private var multipartFiles = ArrayList<MultipartBody.Part>()
    private var ocrResults = ArrayList<OCRResult>()
    private var ocrSendList = ArrayList<OCRSend>()
    private var originalImgUris = ArrayList<GifticonImg>()
    private var productImgUris = ArrayList<GifticonImg>()
    private var barcodeImgUris = ArrayList<GifticonImg>()
    private var gifticonInfoList = ArrayList<AddInfo>()
    private var gifticonEffectiveness = ArrayList<AddInfoNoImgBoolean>()
    private lateinit var dialog: AlertDialog.Builder
    private lateinit var dialogCreate: AlertDialog
    private lateinit var addImgAdapter: AddImgAdapter
    val user = ApplicationClass.sharedPreferencesUtil.getUser()
    var imgNum = 0
    var clickCv = ""

    val PRODUCT = "Product"
    val BARCODE = "Barcode"

    companion object{
        var chkCnt = 1
        var clickItemPos = 0
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onStart() {
        super.onStart()
        mainActivity.hideBottomNav(true)
        isShow = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddBinding.inflate(inflater, container, false)
        binding.addInfo = AddInfo()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chkCnt = 1
        makeProgressDialog()
        openGalleryFirst()

        binding.cvAddCoupon.setOnClickListener {
            makeProgressDialogOnBackPressed()
            openGalleryFirst()
        }

        binding.cvProductImg.setOnClickListener(object : onSingleClickListener(){
            override fun onSingleClick(v: View) {
                clickCv = PRODUCT
                seeCropImgDialog(productImgUris[imgNum], PRODUCT)
            }
        })

        binding.cvBarcodeImg.setOnClickListener(object : onSingleClickListener(){
            override fun onSingleClick(v: View) {
                clickCv = BARCODE
                seeCropImgDialog(barcodeImgUris[imgNum], BARCODE)
            }
        })

        binding.btnOriginalSee.setOnClickListener {
            if (originalImgUris.size != 0){
                seeOriginalImgDialog(originalImgUris[imgNum])
            }
        }

        binding.cbPrice.setOnClickListener{
            clickChkState(imgNum)
        }

        productChk()
        brandChk()
        brandBarcodeNum()
        dateFormat()
        setMemo()

        binding.btnRegi.setOnClickListener {
            if (chkClickImgCnt() && chkEffectiveness()){
                makeProgressDialog()
                changeProgressDialogState(true)

                viewModel.addOtherFileToGCP(makeAddImgMultipartList())
                viewModel.gcpOtherResult.observe(viewLifecycleOwner, EventObserver{
                    viewModel.addImgInfo(makeAddImgInfoList(it))
                    for (i in 0 until delImgUris.size){
                        delCropImg(delImgUris[i])
                    }

                    viewModel.addGifticon(makeAddInfoList())
                    mainActivity.changeFragment(HomeFragment())
                })
            }
        }
    }

    private val result =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            when (it.resultCode) {
                Activity.RESULT_OK -> {
                    val clipData = it.data!!.clipData

                    if (clipData != null) {  //첫 add
                        cvAddCouponClick()

                        for (i in 0 until clipData.itemCount){
                            val originalImgUri = clipData.getItemAt(i).uri
                            originalImgUris.add(GifticonImg(originalImgUri))
                            gifticonEffectiveness.add(AddInfoNoImgBoolean())

                            val realData = originalImgUri.asMultipart("file", requireContext().contentResolver)
                            multipartFiles.add(realData!!)
                        }

                        viewModel.addFileToGCP(multipartFiles.toTypedArray())
                        viewModel.gcpResult.observe(viewLifecycleOwner, EventObserver{
                            for (i in 0 until it.size){
                                val gcpResult = it[i]
                                val originalImgBitmap = uriToBitmap(originalImgUris[i].imgUri)

                                ocrSendList.add(
                                    OCRSend(
                                        gcpResult.fileName, originalImgBitmap.width, originalImgBitmap.height
                                    )
                                )
                            }

                            viewModel.useOcr(ocrSendList.toTypedArray())
                            viewModel.ocrResult.observe(viewLifecycleOwner, EventObserver{
                                for (ocrResult in it){
                                    ocrResults.add(ocrResult)
                                }

                                for (i in 0 until it.size){  //clipData.itemCount
                                    val cropImgUri = cropXY(i, PRODUCT)
                                    val cropBarcodeUri = cropXY(i, BARCODE)

                                    productImgUris.add(GifticonImg(cropImgUri))
                                    barcodeImgUris.add(GifticonImg(cropBarcodeUri))
                                    delImgUris.add(cropImgUri)
                                    delImgUris.add(cropBarcodeUri)

                                    addGifticonInfo(i)
                                }

                                fillContent(0)
                                makeImgList()
                            })
                        })
                    } else{  //수동 크롭
                        if (clickCv == PRODUCT){
                            productImgUris[imgNum] = GifticonImg(Crop.getOutput(it.data))
                            delImgUris.add(productImgUris[imgNum].imgUri)
                        } else if (clickCv == BARCODE){
                            barcodeImgUris[imgNum] = GifticonImg(Crop.getOutput(it.data))
                            delImgUris.add(barcodeImgUris[imgNum].imgUri)
                        }

                        updateGifticonInfo(imgNum)
                        fillContent(imgNum)
                    }
                }
                Activity.RESULT_CANCELED -> {
                    if (originalImgUris.size == 0){ // add탭 클릭 후 이미지 선택 안하고 뒤로가기 클릭 시
                        mainActivity.changeFragment(HomeFragment())
                    }
                }
            }
        }

    // uri to multipart
    @SuppressLint("Range")
    private fun Uri.asMultipart(name: String, contentResolver: ContentResolver): MultipartBody.Part?{
        return contentResolver.query(this, null, null, null, null)?.let {
            if (it.moveToNext()){
                val displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                val requestBody = object : RequestBody(){
                    override fun contentType(): MediaType? {
                        return contentResolver.getType(this@asMultipart)?.toMediaType()
                    }

                    @SuppressLint("Recycle")
                    override fun writeTo(sink: BufferedSink) {
                        sink.writeAll(contentResolver.openInputStream(this@asMultipart)?.source()!!)
                    }
                }
                it.close()
                MultipartBody.Part.createFormData(name, displayName, requestBody)
            } else{
                it.close()
                null
            }
        }
    }

    // ocr결과 null체크
    private fun ocrResultNullChk(value: String?): String{
        if (value == null){
            return ""
        }
        return value
    }

    // 사진 추가버튼 클릭 시 기존 값 초기화
    private fun cvAddCouponClick(){
        originalImgUris.clear()
        productImgUris.clear()
        barcodeImgUris.clear()
        ocrSendList.clear()
        ocrResults.clear()
        delImgUris.clear()
        multipartFiles.clear()
        gifticonInfoList.clear()
        gifticonEffectiveness.clear()

        for (i in 0 until delImgUris.size){
            delCropImg(delImgUris[i])
        }
    }

    // 로딩화면 띄우기
    private fun makeProgressDialog(){
        dialog = AlertDialog.Builder(requireContext())
        dialog.setView(R.layout.dialog_progress).setCancelable(false)
        dialogCreate = dialog.create()
    }

    // 사진추가 버튼 클릭 시 뒤로가기
    private fun makeProgressDialogOnBackPressed(){
        dialog = AlertDialog.Builder(requireContext())
        dialog.setView(R.layout.dialog_progress).setCancelable(true)
        dialogCreate = dialog.create()
    }

    // 상태에 따라 다이얼로그 만들기/없애기
    private fun changeProgressDialogState(state: Boolean){
        if (state){
            dialogCreate.window!!.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            dialogCreate.show()
        } else{
            dialogCreate.dismiss()
        }
    }

    private fun addGifticonInfo(idx: Int){
        var price = ocrResults[idx].price
        if (price == -1){
            price = 0
        }

        val addInfo = AddInfo(
            originalImgUris[idx].imgUri,
            productImgUris[idx].imgUri,
            barcodeImgUris[idx].imgUri,
            ocrResultNullChk(ocrResults[idx].barcodeNum),
            ocrResultNullChk(ocrResults[idx].brandName),
            ocrResultNullChk(ocrResults[idx].productName),
            jsonParsingDate(ocrResults[idx].due),
            ocrResults[idx].isVoucher,
            price,
            "",
            user.email!!,
            user.social
        )
        gifticonInfoList.add(addInfo)
    }

    private fun updateGifticonInfo(idx: Int){
        val addInfo = AddInfo(
            originalImgUris[idx].imgUri,
            productImgUris[idx].imgUri,
            barcodeImgUris[idx].imgUri,
            ocrResultNullChk(gifticonInfoList[idx].barcodeNum),
            ocrResultNullChk(gifticonInfoList[idx].brandName),
            ocrResultNullChk(gifticonInfoList[idx].productName),
            gifticonInfoList[idx].due,
            gifticonInfoList[idx].isVoucher,
            gifticonInfoList[idx].price,
            "",
            user.email!!,
            user.social
        )
        gifticonInfoList[idx] = addInfo
    }

    // View 값 채우기
    private fun fillContent(idx: Int){
        imgNum = idx
        binding.addInfo = gifticonInfoList[idx]

        binding.cbPrice.isChecked = false
        binding.lPrice.visibility = View.GONE
        changeChkState(imgNum)
        setPrice()

        binding.ivCouponImgPlus.visibility = View.GONE
        binding.ivBarcodeImgPlus.visibility = View.GONE
        changeProgressDialogState(false)
    }

    override fun onClick(idx: Int) {
        updateGifticonInfo(idx)
        fillContent(idx)
    }

    // ocrResult 날짜 조합
    private fun jsonParsingDate(value: Map<String, String>?): String {
        if (value == null){
            return ""
        }

        val jsonObject = JsonParser.parseString(value.toString()).asJsonObject
        val result = Gson().fromJson(jsonObject, OCRResultDate::class.java)
        return "${result.Y}-${result.M}-${result.D}"
    }

    // ocrResult 이미지 좌표 split
    private fun jsonParsingCoordinate(value: Map<String, String>?): OCRResultCoordinate{
        if(value == null){
            return OCRResultCoordinate("0", "0", "0", "0", "0", "0", "0", "0")
        }

        val jsonObject = JsonParser.parseString(value.toString()).asJsonObject
        return Gson().fromJson(jsonObject, OCRResultCoordinate::class.java)
    }

    // 좌표로 이미지 크롭
    private fun cropXY(idx: Int, type:String): Uri{
        val fileName: String
        val coordinate: OCRResultCoordinate
        if (type == PRODUCT){
            fileName = "popconImg${PRODUCT}"
            coordinate = jsonParsingCoordinate(ocrResults[idx].productImg)
        } else{
            fileName = "popconImg${BARCODE}"
            coordinate = jsonParsingCoordinate(ocrResults[idx].barcodeImg)
        }

        val x1 = coordinate.x1.toInt()
        val y1 = coordinate.y1.toInt()
        val x4 = coordinate.x4.toInt()
        val y4 = coordinate.y4.toInt()

        val bitmap = uriToBitmap(originalImgUris[idx].imgUri)
        var newBitmap = Bitmap.createBitmap(bitmap, 0, 0, 100, 100)
        if (x1 == 0 && x4 == 0){
            return saveFile(fileName + System.currentTimeMillis(), newBitmap)!!
        }
        newBitmap = Bitmap.createBitmap(bitmap, x1, y1, (x4-x1), (y4-y1))
        return saveFile(fileName + System.currentTimeMillis(), newBitmap)!!
    }

    // add탭 클릭하자마자 나오는 갤러리
    private fun openGalleryFirst() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        intent.setDataAndType(Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        result.launch(intent)

        changeProgressDialogState(true)
    }

    // cardView를 클릭했을 때 나오는 갤러리
    fun openGallery(idx: Int, fromCv: String) {
        val bitmap = uriToBitmap(originalImgUris[idx].imgUri)
        var destination:Uri? = "".toUri()
        if (fromCv == PRODUCT){
            destination = saveFile("popconImgProduct", bitmap)
        } else if (fromCv == BARCODE){
            destination = saveFile("popconImgBarcode", bitmap)
        }
        val crop = Crop.of(originalImgUris[idx].imgUri, destination)

        result.launch(crop.getIntent(mainActivity))
    }

    // 크롭한 이미지 저장
    private fun saveFile(fileName:String, bitmap: Bitmap):Uri?{
        val values = ContentValues()
        values.put(Images.Media.DISPLAY_NAME, fileName)
        values.put(Images.Media.MIME_TYPE, "image/jpeg")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(Images.Media.IS_PENDING, 1)
        }

        val uri = requireContext().contentResolver.insert(Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            val descriptor = requireContext().contentResolver.openFileDescriptor(uri, "w")

            if (descriptor != null) {
                val fos = FileOutputStream(descriptor.fileDescriptor)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                fos.close()
                descriptor.close()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(Images.Media.IS_PENDING, 0)
                    requireContext().contentResolver.update(uri, values, null, null)
                }
            }
        }
        return uri
    }

    // 이미지 절대경로 가져오기
    private fun getPath(uri: Uri):String{
        val data:Array<String> = arrayOf(Images.Media.DATA)
        val cursorLoader = CursorLoader(requireContext(), uri, data, null, null, null)
        val cursor = cursorLoader.loadInBackground()!!
        val idx = cursor.getColumnIndexOrThrow(Images.Media.DATA)
        cursor.moveToFirst()

        return cursor.getString(idx)
    }

    // uri -> bitmap
    private fun uriToBitmap(uri:Uri): Bitmap{
        lateinit var bitmap:Bitmap
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, uri))
        } else{
            bitmap = Images.Media.getBitmap(requireContext().contentResolver, uri)
        }

        return bitmap
    }

    // 크롭되면서 새로 생성된 이미지 삭제
    fun delCropImg(delImgUri: Uri){
        val file = File(getPath(delImgUri))
        file.delete()
    }

    // 상단 리사이클러뷰 만들기
    private fun makeImgList(){
        addImgAdapter = AddImgAdapter(
            gifticonInfoList,
            originalImgUris,
            productImgUris,
            barcodeImgUris,
            ocrSendList,
            gifticonEffectiveness,
            this
        )

        binding.rvCouponList.apply {
            adapter = addImgAdapter
            layoutManager = LinearLayoutManager(this.context, RecyclerView.HORIZONTAL, false)
            adapter!!.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }

    // 크롭된 이미지 다이얼로그
    private fun seeCropImgDialog(gifticonImg: GifticonImg, clickFromCv:String){
        val dialog = CropImgDialogFragment(gifticonImg, clickFromCv)
        dialog.show(childFragmentManager, "CropDialog")
        dialog.setOnClickListener(object: CropImgDialogFragment.BtnClickListener{
            override fun onClicked(fromCv: String) {
                if (fromCv == PRODUCT){
                    openGallery(imgNum, PRODUCT)
                } else if (fromCv == BARCODE){
                    openGallery(imgNum, BARCODE)
                }
            }
        })
    }

    // 이미지 원본보기
    private fun seeOriginalImgDialog(gifticonImg: GifticonImg){
        OriginalImgDialogFragment(gifticonImg).show(
            childFragmentManager, "OriginalDialog"
        )
    }

    // 상품명 리스트에 저장
    private fun productChk(){
        binding.etProductName.addTextChangedListener (object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                val pLength = p0.toString().length
                if(pLength < 1){
                    binding.tilProductName.error = "상품명을 입력해주세요"
                    gifticonEffectiveness[imgNum].productName = false
                } else{
                    binding.tilProductName.error = null
                    binding.tilProductName.isErrorEnabled = false

                    gifticonEffectiveness[imgNum].productName = true
                    gifticonInfoList[imgNum].productName = binding.etProductName.text.toString()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    // 브랜드 존재여부 검사
    private fun brandChk(){
        binding.tilProductBrand.error = "브랜드를 입력해주세요"

        binding.etProductBrand.addTextChangedListener (object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                viewModel.chkBrand(p0.toString())
                viewModel.brandChk.observe(viewLifecycleOwner, EventObserver{
                    if (it.result == 0){
                        binding.tilProductBrand.error = "올바른 브랜드를 입력해주세요"
                        gifticonEffectiveness[imgNum].brandName = false
                    } else{
                        binding.tilProductBrand.error = null
                        binding.tilProductBrand.isErrorEnabled = false

                        gifticonEffectiveness[imgNum].brandName = true
                        gifticonInfoList[imgNum].brandName = binding.etProductBrand.text.toString()
                    }
                })
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    // 바코드 번호 중복 검사
    private fun brandBarcodeNum(){
        binding.tilBarcode.error = "바코드 번호를 입력해주세요"

        binding.etBarcode.addTextChangedListener (object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                viewModel.chkBarcode(p0.toString())
                viewModel.barcodeChk.observe(viewLifecycleOwner, EventObserver{
                    gifticonEffectiveness[imgNum].barcodeNum = false

                    if (it.result == 0){
                        binding.tilBarcode.error = "이미 등록된 바코드 번호입니다"
                    } else if (it.result != 1){
                        binding.tilBarcode.error = "바코드 번호를 입력해주세요"
                    } else{
                        binding.tilBarcode.error = null
                        binding.tilBarcode.isErrorEnabled = false

                        gifticonEffectiveness[imgNum].barcodeNum = true
                        gifticonInfoList[imgNum].barcodeNum = binding.etBarcode.text.toString()
                    }
                })
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    // 유효기간 검사
    val dateArr = arrayOf(31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    private fun dateFormat(){
        binding.etDate.addTextChangedListener (object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                val dateLength = binding.etDate.text!!.length
                val nowText = p0.toString()

                when (dateLength){
                    10 -> {
                        val newYear = nowText.substring(0, 4).toInt()
                        val newMonth = nowText.substring(5, 7).toInt()
                        val newDay = nowText.substring(8).toInt()

                        val nowYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(System.currentTimeMillis()).toInt()
                        val nowDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis())
                        val nowDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(nowDateFormat)
                        var newDate = Date()
                        try {
                            newDate =  SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(p0.toString())!!
                        } catch (e: java.lang.Exception){
                            newDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(nowDateFormat)!!
                        }

                        val calDate = newDate.compareTo(nowDate)
                        gifticonEffectiveness[imgNum].due = false

                        if (newYear < nowYear || newYear > 2100 || newYear.toString().length < 4){
                            binding.tilDate.error = "정확한 날짜를 입력해주세요"
                        } else if(newMonth < 1 || newMonth > 12){
                            binding.tilDate.error = "정확한 날짜를 입력해주세요"
                        } else if(newDay > dateArr[newMonth-1] || newDay == 0){
                            binding.tilDate.error = "정확한 날짜를 입력해주세요"
                        } else if (calDate < 0){
                            binding.tilDate.error = "이미 지난 날짜입니다"
                        } else{
                            binding.tilDate.error = null
                            binding.tilDate.isErrorEnabled = false
                            gifticonEffectiveness[imgNum].due = true
                            gifticonInfoList[imgNum].due = nowText
                        }
                    }
                    else -> {
                        binding.tilDate.error = "정확한 날짜를 입력해주세요"
                        gifticonEffectiveness[imgNum].due = false
                    }
                }

                if (dateLength < 10){
                    binding.tilDate.error = "정확한 날짜를 입력해주세요"
                    gifticonEffectiveness[imgNum].due = false
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //p0: 현재 입력된 문자열, p1: 새로 추가될 문자열 위치, p2: 변경될 문자열의 수, p3: 새로 추가될 문자열 수
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //p0: 현재 입력된 문자열, p1: 새로 추가될 문자열 위치, p2: 삭제된 기존 문자열 수, p3: 새로 추가될 문자열 수
                val dateLength = binding.etDate.text!!.length
                if(dateLength==4 && p1!=4 || dateLength==7 && p1!=7){
                    val add = binding.etDate.text.toString() + "-"
                    binding.etDate.setText(add)
                    binding.etDate.setSelection(add.length)
                }
            }
        })
    }

    // 체크박스 클릭 시 상태변화
    private fun clickChkState(idx: Int){
        val chkState = binding.cbPrice.isChecked
        if (!chkState){
            gifticonInfoList[idx].isVoucher = 0
            gifticonInfoList[imgNum].price = -1
            binding.cbPrice.isChecked = false
            binding.lPrice.visibility = View.GONE
            gifticonEffectiveness[imgNum].isVoucher = false
        } else{
            gifticonInfoList[idx].isVoucher = 1
            binding.cbPrice.isChecked = true
            binding.lPrice.visibility = View.VISIBLE
            gifticonEffectiveness[imgNum].isVoucher = true
        }
    }

    // 체크박스 상태에 따른 변화
    private fun changeChkState(idx: Int){
        val voucherChk = gifticonInfoList[idx].isVoucher
        if (voucherChk != 1){
            binding.cbPrice.isChecked = false
            binding.lPrice.visibility = View.GONE
            gifticonEffectiveness[imgNum].isVoucher = false
        } else{
            binding.cbPrice.isChecked = true
            binding.lPrice.visibility = View.VISIBLE
            gifticonEffectiveness[imgNum].isVoucher = true
        }
    }

    // price를 리스트에 저장
    private fun setPrice(){
        binding.etPrice.addTextChangedListener (object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                var pLength = p0.toString().length
                if(pLength > 2){  //100원대부터
                    gifticonEffectiveness[imgNum].price = true
                    gifticonInfoList[imgNum].price = binding.etPrice.text.toString().toInt()
                } else{
                    gifticonEffectiveness[imgNum].price = false
                    gifticonInfoList[imgNum].price = -1
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    // memo를 리스트에 저장
    private fun setMemo(){
        binding.etWriteMemo.addTextChangedListener (object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {
                gifticonInfoList[imgNum].memo = binding.etWriteMemo.text.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    // 등록하기 클릭 시 디비에 저장할 이미지 리스트 생성
    private fun makeAddImgMultipartList(): Array<MultipartBody.Part>{
        val multipartImg = mutableListOf<MultipartBody.Part>()
        for (i in 0 until originalImgUris.size){
            val productData = productImgUris[i].imgUri.asMultipart("file", requireContext().contentResolver)!!
            val barcodeData = barcodeImgUris[i].imgUri.asMultipart("file", requireContext().contentResolver)!!

            multipartImg.add(productData)
            multipartImg.add(barcodeData)
        }

        return multipartImg.toTypedArray()
    }

    // 등록하기 클릭 시 디비에 저장할 이미지 정보 리스트 생성
    private fun makeAddImgInfoList(gcpResult: List<GCPResult>): Array<AddImgInfo>{
        var idx = 0
        val imgInfo = mutableListOf<AddImgInfo>()
        for (i in 0 until gcpResult.size step(2)){
            val productImgName = gcpResult[i].fileName
            val barcodeImgName = gcpResult[i+1].fileName

            imgInfo.add(
                AddImgInfo(
                    binding.etBarcode.text.toString(),
                    ocrSendList[idx++].fileName,
                    productImgName,
                    barcodeImgName
                )
            )
        }
        return imgInfo.toTypedArray()
    }

    // 등록하기 클릭 시 디비에 저장할 기프티콘 정보 리스트 생성
    private fun makeAddInfoList(): MutableList<AddInfoNoImg>{
        val addInfo = mutableListOf<AddInfoNoImg>()
        for (i in 0 until gifticonInfoList.size){
            addInfo.add(
                AddInfoNoImg(
                    gifticonInfoList[i].barcodeNum,
                    gifticonInfoList[i].brandName,
                    gifticonInfoList[i].productName,
                    gifticonInfoList[i].due,
                    gifticonInfoList[i].isVoucher,
                    gifticonInfoList[i].price,
                    gifticonInfoList[i].memo,
                    user.email!!,
                    user.social
                )
            )
        }
        return addInfo
    }

    // 리사이클러뷰의 기프티콘 이미지 모두 클릭했는지 확인
    private fun chkClickImgCnt(): Boolean{
        if (chkCnt >= originalImgUris.size){
            return true
        }
        Toast.makeText(requireContext(), "등록한 기프티콘을 확인해주세요", Toast.LENGTH_SHORT).show()
        return false
    }

    // 기프티콘 정보담긴 리스트 내용 검사
    private fun chkAllList(): Boolean{
        for (gifticon in gifticonEffectiveness){
            if (!gifticon.productName || !gifticon.brandName
                || !gifticon.barcodeNum || !gifticon.due){
                Log.d(TAG, "chkAllList111: ${gifticon.productName}\n ${gifticon.brandName}\n" +
                        "${gifticon.barcodeNum}\n${gifticon.due}\n")
                return false
            }
            if (gifticon.isVoucher && !gifticon.price){
                Log.d(TAG, "chkAllList222: ${gifticon.isVoucher}\n ${gifticon.price}\n")
                return false
            }
        }
        return true
    }

    // 유효성 검사
    private fun chkEffectiveness(): Boolean {
        if (binding.ivBarcodeImg.drawable == null
            || binding.ivCouponImg.drawable == null
            || !chkAllList()
        ) {
            Toast.makeText(requireContext(), "입력 정보를 확인해주세요", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        changeProgressDialogState(false)
    }

    override fun onDestroy() {
        super.onDestroy()

        mainActivity.hideBottomNav(false)
        isShow = false
    }
}