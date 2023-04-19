package com.raafat.revise

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.ContextThemeWrapper
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.raafat.revise.data.Aya
import kotlinx.coroutines.runBlocking


class MainActivity : AppCompatActivity() {
    private lateinit var textView: PagedTextView
    private lateinit var slider: Slider
    private lateinit var spinner: Spinner
    private lateinit var previous: ExtendedFloatingActionButton

    private lateinit var hide: MaterialSwitch
    private lateinit var count: TextView
    private lateinit var page: TextView
    private lateinit var gson: List<Aya>

    private lateinit var basmalah: TextView

    private var hideAya = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.quran_content_tv)

        val getSharedPrefs: SharedPreferences = getSharedPreferences(packageName, MODE_PRIVATE)
        val savedSura = getSharedPrefs.getInt("spinner", 0)
        val savedAya = getSharedPrefs.getFloat("slider", 1f)


        hide = findViewById(R.id.hide)
        val defaultThumb = hide.thumbTintList



        previous = findViewById(R.id.previous_aya)

        count = findViewById(R.id.count)
        page = findViewById(R.id.page)

        spinner = findViewById(R.id.sura_spinner)
        slider = findViewById(R.id.slider)

        basmalah = findViewById(R.id.basmalah)
        basmalah.visibility = View.GONE






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



        var json: String

        var fileName : String

        var typeface = ResourcesCompat.getFont(this, R.font.hafs_smart)

        runBlocking {
            if(Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
                fileName = "hafsData.json"
                typeface = ResourcesCompat.getFont(this@MainActivity, R.font.uthmanic_hafs)!!
            }
            else {
                fileName = "data.json"
            }
            json = applicationContext.assets.open(fileName)
            .bufferedReader()
            .use { it.readText() }
        }


        textView.typeface = typeface

        val type = object : TypeToken<ArrayList<Aya>>() {}.type

        gson = Gson().fromJson(json, type)

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

        var savedPage = gson.filter { aya -> aya.ayaNo == ayaNo }
            .filter {aya -> aya.sora == sora + 1 }[0].page

        page.text = "صفحة   ".plus(savedPage)


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

        fun textViewPaddingFirst() {
            basmalah.visibility = View.VISIBLE
            textView.visibility = View.INVISIBLE
        }
        fun textViewPaddingRest() {
            basmalah.visibility = View.GONE
            textView.visibility = View.VISIBLE
        }

        if(savedAya == 1f)
            if (spinner.selectedItemPosition + 1 != 1 &&
                spinner.selectedItemPosition + 1 != 9)
                textViewPaddingFirst()

        fun nextClicked(){

            if (i < textView.size() - 1 ){
                if(basmalah.visibility == View.VISIBLE){
                    textView.next(i)
                }
                else
                    textView.next(++i)
            }

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

        fun specialPreviousClicked() {
            if (ayaNo > 1){
                if(i == 0) {
                    ayaNo--
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
                else{
                    i = 0
                    textView.next(i)
                }
            }else{
                i = 0
                textView.next(i)
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
                        if(sora != 1 && sora != 9)
                            textViewPaddingFirst()

                        if((sora == 1 || sora == 9) && basmalah.visibility == View.VISIBLE)
                            textViewPaddingRest()

                        slider.value = 1f

                        if(slider.value == 1f){
                            ayaNo = 1
                            (view as TextView?)?.text = "سورة ".plus(parent.selectedItem.toString().substringAfter(" "))

                            count.text = "الآية   ".plus(slider.value.toInt().toString())
                            savedPage = gson.filter { aya -> aya.ayaNo == slider.value.toInt() }
                                .filter {aya -> aya.sora == spinner.selectedItemPosition + 1 }[0].page

                            page.text = "صفحة   ".plus(savedPage)


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
                hide.thumbTintList = defaultThumb
                        hideAya = false

                        slider.valueTo = numberOfAyahsForSuraArray[position].toFloat()

                    } else {
                        clicked = true
                        sora = position + 1
                        (view as TextView?)?.text = "سورة ".plus(parent.selectedItem.toString().substringAfter(" "))


                        hide.isChecked = false
                hide.thumbTintList = defaultThumb
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
                hide.thumbTintList = defaultThumb
                hideAya = false

                if(slider.value == 1f){
                    ayaNo = 1

                    if(sora != 1 && sora != 9) {
                        basmalah.visibility = View.VISIBLE
                        textView.visibility = View.INVISIBLE
                    }

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
                    textViewPaddingRest()
                }
                i = 0
                textView.next(i)

            }

            override fun onStopTrackingTouch(slider: Slider) {
                ayaNo = slider.value.toInt()

                hide.isChecked = false
                hide.thumbTintList = defaultThumb
                hideAya = false

                if(slider.value == 1f){
                    ayaNo = 1

                    if(sora != 1 && sora != 9) {
                        basmalah.visibility = View.VISIBLE
                        textView.visibility = View.INVISIBLE
                    }

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
                    textViewPaddingRest()
                }
                i = 0
                textView.next(i)

            }

        })

        slider.addOnChangeListener { slider, value, fromUser ->
            count.text = "الآية   ".plus(slider.value.toInt().toString())
            savedPage = gson.filter { aya -> aya.ayaNo == slider.value.toInt() }
                .filter {aya -> aya.sora == spinner.selectedItemPosition + 1 }[0].page

            page.text = "صفحة   ".plus(savedPage)
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
            textViewPaddingRest()
            if (hideAya) {
                if (textView.size() > 1 && i > 0) {
                    i = 0
                    textView.next(i)
                }
                hide()
                hide.isChecked = true
                hide.thumbTintList = ColorStateList.valueOf(Color.WHITE)
            }
            else {
                textView.next(i)
                hide.isChecked = false
                hide.thumbTintList = defaultThumb
            }
        }

        hide.setOnTouchListener(object : View.OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event?.action == MotionEvent.ACTION_UP) {
                    textViewPaddingRest()
                    hideAya = !hideAya
                    if (hideAya) {
                        if (textView.size() > 1 && i > 0) {
                            i = 0
                            textView.next(i)
                        }
                        hide()
                        hide.isChecked = true
                        hide.thumbTintList = ColorStateList.valueOf(Color.WHITE)
                    } else {
                        textView.next(i)
                        hide.isChecked = false
                        hide.thumbTintList = defaultThumb
                    }
                }
                return true
            }

        })


        basmalah.setOnClickListener {
            basmalah.visibility = View.GONE
            textView.visibility = View.VISIBLE
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
            if(hideAya) undoClicked() else specialPreviousClicked()
        }

        fun vibratePhone() {
            val vibrator = this?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(20)
            }
        }

        previous.setOnLongClickListener(object : OnContinuousClickListener(2000){
            override fun onContinuousClick(v: View?) {
                if(hideAya){
                        i = 0
                        textView.next(i)
                        hide()
                        vibratePhone()

                }

            }

        })

    }

    override fun attachBaseContext(newBase: Context?) {

        val newOverride = Configuration(newBase?.resources?.configuration)
        newOverride.fontScale = 1.0f
        applyOverrideConfiguration(newOverride)

        super.attachBaseContext(newBase)
    }

    override fun onPause() {
        super.onPause()
        val putSharedPrefs: SharedPreferences = getSharedPreferences(packageName, MODE_PRIVATE)
        putSharedPrefs.edit().putInt("spinner", spinner.selectedItemPosition).apply()
        putSharedPrefs.edit().putFloat("slider", slider.value).apply()
    }
}