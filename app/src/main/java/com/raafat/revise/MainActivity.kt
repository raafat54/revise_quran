package com.raafat.revise

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.google.gson.Gson
import com.raafat.revise.data.AyaList
import kotlinx.coroutines.runBlocking


class MainActivity : AppCompatActivity() {
    private lateinit var textView: PagedTextView
    private lateinit var menu: ImageButton
    private lateinit var slider: Slider
    private lateinit var spinner: Spinner
    private lateinit var previous: ExtendedFloatingActionButton

     private lateinit var launch: ExtendedFloatingActionButton
    private lateinit var hide: MaterialSwitch
    private lateinit var count: TextView
    private lateinit var gson: AyaList

    private var hideAya = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val getSharedPrefs: SharedPreferences = getSharedPreferences(packageName, MODE_PRIVATE)
        val savedSura = getSharedPrefs.getInt("spinner", 0)
        val savedAya = getSharedPrefs.getFloat("slider", 1f)

        val PREFS_NAME = "MyPrefsFile"

        val settings = getSharedPreferences(PREFS_NAME, 0)

        if (settings.getBoolean("my_first_time", true)) {

            // first time task
            showTutorial()
            // record the fact that the app has been started at least once
            settings.edit().putBoolean("my_first_time", false).apply()
        }
        hide = findViewById(R.id.hide)

        launch = findViewById(R.id.launch)

        menu = findViewById(R.id.menu)

        previous = findViewById(R.id.previous_aya)

        count = findViewById(R.id.count)

        spinner = findViewById(R.id.sura_spinner)
        slider = findViewById(R.id.slider)


        val drawable = R.drawable.ic_thumb
        slider.setCustomThumbDrawable(drawable)

        launch.setOnClickListener {
            val appisFound = isAppInstalled(this@MainActivity, "com.quran.labs.androidquran")
            if(appisFound) {
                startNewActivity(spinner.selectedItemPosition + 1, slider.value.toInt())
            }else{
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.quran.labs.androidquran")))
            }
        }

        menu.setOnClickListener {
            showPopupMenu(menu)
        }


        val numberOfAyahsForSuraArray = intArrayOf(
            /*  1 -  14 */ 7, 286, 200, 176, 120, 165, 206, 75, 129, 109, 123, 111, 43, 52,
            /* 15 -  28 */ 99, 128, 111, 110, 98, 135, 112, 78, 118, 64, 77, 227, 93, 88,
            /* 29 -  42 */ 69, 60, 34, 30, 73, 54, 45, 83, 182, 88, 75, 85, 54, 53,
            /* 43 -  56 */ 89, 59, 37, 35, 38, 29, 18, 45, 60, 49, 62, 55, 78, 96,
            /* 57 -  70 */ 29, 22, 24, 13, 14, 11, 11, 18, 12, 12, 30, 52, 52, 44,
            /* 71 -  84 */ 28, 28, 20, 56, 40, 31, 50, 40, 46, 42, 29, 19, 36, 25,
            /* 85 -  98 */ 22, 17, 19, 26, 30, 20, 15, 21, 11, 8, 8, 19, 5, 8,
            /* 99 - 114 */ 8, 11, 11, 8, 3, 9, 5, 4, 7, 3, 6, 3, 5, 4, 5, 6
        )

        textView = findViewById(R.id.quran_content_tv)


        var json: String

        runBlocking {
            json = applicationContext.assets.open("data.json")
            .bufferedReader()
            .use { it.readText() }
        }




        gson = Gson().fromJson(json, AyaList::class.java)

        val suras = resources.getStringArray(R.array.sura_names)

        val adapter = ArrayAdapter(
            this,
            R.layout.custom_spinner_item, suras
        )
        spinner.adapter = adapter


        var sora = savedSura
        spinner.setSelection(sora)
        var clicked = spinner.selectedItemPosition != savedSura
        var ayaNo = savedAya.toInt()
        slider.value = savedAya
        count.text = "الآية   ".plus(slider.value.toInt().toString())

        var i = 0
        var index = 1
        var list = gson.filter { aya -> aya.ayaNo == ayaNo }
            .filter { aya -> aya.sora == sora + 1 }[0].ayaText.split(" ").toList()

        var newList = list.subList(0, list.size - 1).joinToString(" ")
        var lastChar = "\u00a0".plus(list.last())

        var string = newList.plus(lastChar)
        textView.text = string


        slider.value = ayaNo.toFloat()


        slider.valueTo = numberOfAyahsForSuraArray[sora].toFloat()


        fun nextClicked(){
            if (i < textView.size() - 1 )
                textView.next(++i)

            else{
                if(ayaNo < numberOfAyahsForSuraArray[sora - 1]) {
                    ayaNo++
                    slider.value = ayaNo.toFloat()

                    list = gson.filter { aya -> aya.ayaNo == ayaNo }
                        .filter { aya -> aya.sora == sora }[0].ayaText.split(" ").toList()

                    newList = list.subList(0, list.size - 1).joinToString(" ")
                    lastChar = "\u00a0".plus(list.last())

                    string = newList.plus(lastChar)
                    textView.text = string

                    textView.paginate(textView.text)
                    i = 0
                    textView.next(i)
                }
            }
        }

        fun previousClicked(){
            if (i > 0 )
                textView.next(--i)

            else{
                if (ayaNo > 1) {
                    ayaNo--
                    slider.value = ayaNo.toFloat()

                    list = gson.filter { aya -> aya.ayaNo == ayaNo }
                        .filter { aya -> aya.sora == sora }[0].ayaText.split(" ").toList()
                    newList = list.subList(0, list.size - 1).joinToString(" ")
                    lastChar = "\u00a0".plus(list.last())

                    string = newList.plus(lastChar)
                    textView.text = string
                    textView.paginate(textView.text)
                    i = textView.size() - 1
                    textView.next(i)
                }
            }
        }

        fun setTextView(){
            previousClicked()
            nextClicked()
        }

        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                if (position >= 0){
                    if (clicked) {
                        sora = position + 1
                        ayaNo = 1


                        slider.value = 1f

                        if(slider.value == 1f){
                            ayaNo = 1

                            list = gson.filter { aya -> aya.ayaNo == ayaNo }
                                .filter { aya -> aya.sora == sora }[0].ayaText.split(" ").toList()

                            newList = list.subList(0, list.size - 1).joinToString(" ")
                            lastChar = "\u00a0".plus(list.last())

                            string = newList.plus(lastChar)
                            textView.text = string

                            textView.paginate(textView.text)
                            i = 0
                            textView.next(i)

                        }

                        hide.isChecked = false
                        hideAya = false

                        slider.valueTo = numberOfAyahsForSuraArray[position].toFloat()

                    } else {
                        clicked = true
                        sora = position + 1


                        hide.isChecked = false
                        hideAya = false


                        slider.valueTo = numberOfAyahsForSuraArray[position].toFloat()

                    }
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                ayaNo = slider.value.toInt()

                hide.isChecked = false
                hideAya = false

                if(slider.value == 1f){
                    ayaNo = 1

                    list = gson.filter { aya -> aya.ayaNo == ayaNo }
                        .filter { aya -> aya.sora == sora }[0].ayaText.split(" ").toList()

                    newList = list.subList(0, list.size - 1).joinToString(" ")
                    lastChar = "\u00a0".plus(list.last())

                    string = newList.plus(lastChar)
                    textView.text = string

                    textView.paginate(textView.text)
                    i = 0
                    textView.next(i)

                }
                else {
                    setTextView()
                }
                i = 0
                textView.next(i)

            }

            override fun onStopTrackingTouch(slider: Slider) {
                ayaNo = slider.value.toInt()

                hide.isChecked = false
                hideAya = false

                if(slider.value == 1f){
                    ayaNo = 1

                    list = gson.filter { aya -> aya.ayaNo == ayaNo }
                        .filter { aya -> aya.sora == sora }[0].ayaText.split(" ").toList()

                    newList = list.subList(0, list.size - 1).joinToString(" ")
                    lastChar = "\u00a0".plus(list.last())

                    string = newList.plus(lastChar)
                    textView.text = string

                    textView.paginate(textView.text)
                    i = 0
                    textView.next(i)
                }
                else {
                    setTextView()
                }
                i = 0
                textView.next(i)

            }

        })

        slider.addOnChangeListener { slider, value, fromUser ->
            count.text = "الآية   ".plus(slider.value.toInt().toString())
        }


        fun hide(){
            index = 1
            runOnUiThread {
                val textWithHighlights: Spannable = SpannableString(textView.text)

                textWithHighlights.setSpan(
                    ForegroundColorSpan(Color.TRANSPARENT),
                    0,
                    textView.text.length - 1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )

                textView.text = textWithHighlights
            }
        }
        if (hideAya)
            hide()


        fun undoClicked(){
            var text = textView.text.toString().split(" ","\u00a0").toList()
            if (index > 1 ){
                runOnUiThread {
                    val textWithHighlights: Spannable = SpannableString(textView.text)
                    textWithHighlights.setSpan(
                        ForegroundColorSpan(ContextCompat.getColor(this, R.color.gray)),
                        text.subList(0, index - 2).joinToString(" ").length,
                        text.subList(0, index - 1).joinToString(" ").length,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE
                    )
                    textView.text = textWithHighlights
                }
                index--
            }

            else{
                previousClicked()
                text = textView.text.toString().split(" ","\u00a0").toList()
                index = text.size
            }
        }



        fun textViewClicked() {
            val text = textView.text.toString().split(" ","\u00a0").toList()
            if (index <= text.size-1){
                runOnUiThread {
                    val textWithHighlights: Spannable = SpannableString(textView.text)
                    textWithHighlights.setSpan(
                        ForegroundColorSpan(Color.WHITE),
                        text.subList(0, 0).joinToString(" ").length,
                        text.subList(0, index).joinToString(" ").length,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE
                    )
                    textView.text = textWithHighlights
                }
                index++
            }
            else{
                if(slider.value == slider.valueTo && i == textView.size() - 1){
                    nextClicked()
                }
                else {
                    nextClicked()
                    hide()
                    textViewClicked()
                }

            }
        }


        hide.setOnClickListener{
            hideAya = !hideAya
            if (hideAya) {
                hide()
                hide.isChecked = true
            }
            else {
                textView.next(i)
                hide.isChecked = false
            }
        }




        textView.setOnClickListener {
            if (hideAya) textViewClicked() else nextClicked()

        }


        textView.setOnTouchListener { v, event ->
            if (MotionEvent.ACTION_DOWN == event.action) {
            } else if (MotionEvent.ACTION_UP == event.action) {
                v.performClick()
            }
            true
        }


        previous.setOnClickListener {
            if(hideAya) undoClicked() else previousClicked()
        }

        previous.setOnLongClickListener(object : OnContinuousClickListener(600){
            override fun onContinuousClick(v: View?) {
                if(hideAya) undoClicked() else previousClicked()

            }

        })

    }



    override fun onPause() {
        super.onPause()
        val putSharedPrefs: SharedPreferences = getSharedPreferences(packageName, MODE_PRIVATE)
        putSharedPrefs.edit().putInt("spinner", spinner.selectedItemPosition).apply()
        putSharedPrefs.edit().putFloat("slider", slider.value).apply()
    }

    private fun startNewActivity(sora: Int, ayaNo: Int) {
        Log.i("TAG", "showPopupMenu: ${sora} , ${ayaNo}")

        val intent =  Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse("quran://$sora/$ayaNo")
        startActivity(intent)

    }

    private fun isAppInstalled(context: Context, packageName: String?): Boolean {
        return try {
            if (packageName != null) {
                context.packageManager.getApplicationInfo(packageName, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }


    fun getFont(): Typeface {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) resources.getFont(R.font.uthmanic_hafs)
        else this.let { ResourcesCompat.getFont(it, R.font.uthmanic_hafs) }!!
    }


    private fun showTutorial() {
        TapTargetSequence(this@MainActivity)
            .targets(
                TapTarget.forView(findViewById(R.id.sura_spinner), "اختر السورة من القائمة")
                    .transparentTarget(true)
                    .textTypeface(getFont())
                    .titleTextSize(30)
                    .outerCircleAlpha(0.96f)
                    .outerCircleColor(R.color.white)
                    .targetCircleColor(R.color.white)
                    .textColor(R.color.black)
                    .dimColor(R.color.black)
                    .targetRadius(100)
                    .drawShadow(true)
                    .tintTarget(false)
                    .id(1)
                    .cancelable(false),
                TapTarget.forView(findViewById(R.id.slider), "اختر الآية")
                    .transparentTarget(true)
                    .textTypeface(getFont())
                    .titleTextSize(30)
                    .outerCircleAlpha(0.96f)
                    .outerCircleColor(R.color.white)
                    .targetCircleColor(R.color.white)
                    .textColor(R.color.black)
                    .dimColor(R.color.black)
                    .drawShadow(true)
                    .tintTarget(true)
                    .targetRadius(115)
                    .id(2)
                    .cancelable(false),
                TapTarget.forView(findViewById(R.id.previous_aya), "الرجوع للكلمة السابقة مع النقر المستمر لأكثر من كلمة  (الصفحة السابقة)")
                    .transparentTarget(true)
                    .textTypeface(getFont())
                    .titleTextSize(30)
                    .outerCircleAlpha(0.96f)
                    .outerCircleColor(R.color.white)
                    .targetCircleColor(R.color.white)
                    .textColor(R.color.black)
                    .dimColor(R.color.black)
                    .drawShadow(true)
                    .tintTarget(true)
                    .targetRadius(120)
                    .id(3)
                    .cancelable(false),
                TapTarget.forView(findViewById(R.id.quran_content_tv), "نقر على الشاشة لإظهار الكلمة  (الصفحة التالية)")
                    .transparentTarget(true)
                    .textTypeface(getFont())
                    .titleTextSize(30)
                    .outerCircleAlpha(0.96f)
                    .outerCircleColor(R.color.white)
                    .targetCircleColor(R.color.white)
                    .textColor(R.color.black)
                    .dimColor(R.color.black)
                    .drawShadow(true)
                    .tintTarget(true)
                    .targetRadius(120)
                    .id(5)
                    .cancelable(false),
                TapTarget.forView(findViewById(R.id.hide), "إخفاء الآيات")
                    .transparentTarget(true)
                    .textTypeface(getFont())
                    .titleTextSize(30)
                    .outerCircleAlpha(0.96f)
                    .outerCircleColor(R.color.white)
                    .targetCircleColor(R.color.white)
                    .textColor(R.color.black)
                    .dimColor(R.color.black)
                    .drawShadow(true)
                    .tintTarget(true)
                    .id(6)
                    .cancelable(false),
                TapTarget.forView(findViewById(R.id.launch), "فتح الآية فى تطبيق قرآن أندرويد")
                    .transparentTarget(true)
                    .textTypeface(getFont())
                    .titleTextSize(30)
                    .outerCircleAlpha(0.96f)
                    .outerCircleColor(R.color.white)
                    .targetCircleColor(R.color.white)
                    .textColor(R.color.black)
                    .dimColor(R.color.black)
                    .drawShadow(true)
                    .tintTarget(true)
                    .id(3)
                    .targetRadius(70)
                    .cancelable(false)

            )
            .start()
    }

    private fun showPopupMenu(view: View) = PopupMenu(view.context, view)
        .run {
            menuInflater.inflate(R.menu.main_menu, menu)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.tutorial -> {
                        showTutorial()
                        true
                    }
                    else -> {
                        true
                    }
                }
            }
            show()
        }
}