package com.raafat.revise

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.google.gson.Gson
import com.raafat.revise.data.AyaList
import kotlinx.coroutines.runBlocking
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var textView: TextView

    private lateinit var slider: Slider
    private lateinit var spinner: Spinner

    private lateinit var previous: ImageButton
    private lateinit var next: ImageButton
    private lateinit var undo: ImageButton
    private lateinit var launch: ImageButton



    private lateinit var gson: AyaList
    private var currentSura = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val getSharedPrefs: SharedPreferences = getSharedPreferences(packageName, MODE_PRIVATE)
        val savedSura = getSharedPrefs.getInt("spinner", 0)
        val savedAya = getSharedPrefs.getFloat("slider", 1f)

        val ayaCount = findViewById<Button>(R.id.aya_count)

        previous = findViewById(R.id.previous_aya)
        next = findViewById(R.id.next_aya)
        undo = findViewById(R.id.undo)
        launch = findViewById(R.id.launch)

        val stack = Stack<String>()



        spinner = findViewById(R.id.sura_spinner)
        slider = findViewById(R.id.slider)


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

        val current = StringBuilder()

        var json: String

        runBlocking {
            json = applicationContext.assets.open("data.json")
            .bufferedReader()
            .use { it.readText() }
        }


        var i = 0


        gson = Gson().fromJson(json, AyaList::class.java)

        val suras = resources.getStringArray(R.array.sura_names)

        val adapter = ArrayAdapter(
            this,
            R.layout.custom_spinner_item, suras
        )
        spinner.adapter = adapter

        var sura = savedSura
        spinner.setSelection(sura)
        var clicked = spinner.selectedItemPosition != savedSura
        var verse = savedAya.toInt()
        slider.value = verse.toFloat()
        ayaCount.text = "${slider.value.toInt()}"


        slider.valueTo = numberOfAyahsForSuraArray[sura].toFloat()
        var globalVerse =
            ((gson.filter { Sura -> Sura.sora == sura + 1 }).filter { Aya -> Aya.ayaNo == verse })[0].id - 1



        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?, position: Int, id: Long
            ) {
                if (clicked) {
                    sura = position + 1
                    i = 0
                    current.clear()


                    slider.value = 1f
                    ayaCount.text = "${slider.value.toInt()}"
                    globalVerse =
                        ((gson.filter { Sura -> Sura.sora == sura }).filter { Aya -> Aya.ayaNo == 1 })[0].id - 1

                    textView.text = getWords(globalVerse, gson).joinToString(" ")
                    textView.setTextColor(Color.GRAY)
                    textView.maxLines = 1

                    currentSura = gson[globalVerse].sora
                    slider.valueTo = numberOfAyahsForSuraArray[position].toFloat()

                } else {
                    clicked = true
                    sura = position + 1
                    i = 0
                    current.clear()


                    textView.text = getWords(globalVerse, gson).joinToString(" ")
                    textView.setTextColor(Color.GRAY)
                    textView.maxLines = 1

                    currentSura = gson[globalVerse].sora
                    slider.valueTo = numberOfAyahsForSuraArray[position].toFloat()

                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                verse = slider.value.toInt()
                ayaCount.text = "${slider.value.toInt()}"

                globalVerse =
                    ((gson.filter { Sura -> Sura.sora == sura }).filter { Aya -> Aya.ayaNo == verse })[0].id - 1
                i = 0
                current.clear()


                textView.text = getWords(globalVerse, gson).joinToString(" ")
                textView.setTextColor(Color.GRAY)
                textView.maxLines = 1


            }

            override fun onStopTrackingTouch(slider: Slider) {
                verse = slider.value.toInt()
                ayaCount.text = "${slider.value.toInt()}"

                globalVerse =
                    ((gson.filter { Sura -> Sura.sora == sura }).filter { Aya -> Aya.ayaNo == verse })[0].id - 1
                i = 0
                current.clear()


                if (slider.value == 1f)
                    textView.text = getWords(globalVerse, gson).joinToString(" ")
                else
                    textView.text = getWords(globalVerse, gson).joinToString(" ")
                textView.setTextColor(Color.GRAY)
                textView.maxLines = 1


            }

        })

        slider.addOnChangeListener { slider, _, _ ->
            ayaCount.text = "${slider.value.toInt()}"


        }


        currentSura = gson[globalVerse].sora


        textView.setOnClickListener {

            undo.isClickable = true

            textView.maxLines = 10
            textView.setTextColor(Color.WHITE)

            if (i <= getWords(globalVerse, gson).size - 2) {
                if (i == getWords(globalVerse, gson).size - 2) {
                    textView.text = current.append(getWords(globalVerse, gson)[i++]).append(" ")
                        .append(getWords(globalVerse, gson)[i++]).append(" ")


                } else {
                    textView.text = current.append(getWords(globalVerse, gson)[i++]).append(" ")
                }
            } else {
                if (globalVerse != 6235) {
                    i = 0
                    ++globalVerse
                    stack.push(current.toString())

                    slider.value = gson[globalVerse].ayaNo.toFloat()
                    ayaCount.text = "${slider.value.toInt()}"
                    if (currentSura != gson[globalVerse].sora) {
                        spinner.setSelection(currentSura, true)
                        current.clear()
                        stack.clear()
                        currentSura = gson[globalVerse].sora
                    }
                    textView.text = current.append(getWords(globalVerse, gson)[i++]).append(" ")

                }
            }



            if ((textView.lineCount + 2) * textView.lineHeight  > textView.height) {
                if (i == getWords(globalVerse, gson).size) {
                    textView.text = current
                } else if(i == 1) {
                    textView.text =
                        current.clear().append(getWords(globalVerse, gson)[i - 1]).append(" ")
                } else{
                    stack.push(
                        current.removeRange(
                            current.length - " ${getWords(globalVerse, gson)[i - 1]}".length,
                            current.length
                        ).toString()
                    )

                    textView.text =
                        current.clear().append(getWords(globalVerse, gson)[i - 1]).append(" ")
                }
            }

        }

        previous.setOnClickListener {

            if (globalVerse > 0) {
                i = 0

                --globalVerse
                slider.value = gson[globalVerse].ayaNo.toFloat()
                ayaCount.text = "${slider.value.toInt()}"

                if (currentSura != gson[globalVerse].sora) {
                    spinner.setSelection(currentSura - 2, true)
                    slider.valueTo = numberOfAyahsForSuraArray[gson[globalVerse].sora - 1].toFloat()
                    clicked = false
                    current.clear()
                    currentSura = gson[globalVerse].sora
                }
                current.clear()
                stack.clear()

                textView.text = getWords(globalVerse, gson).joinToString(" ")
                textView.setTextColor(Color.GRAY)
                textView.maxLines = 1
            }
        }

        next.setOnClickListener {

            if (globalVerse != 6235) {
                i = 0

                ++globalVerse
                slider.value = gson[globalVerse].ayaNo.toFloat()
                ayaCount.text = "${slider.value.toInt()}"

                if (currentSura != gson[globalVerse].sora) {
                    spinner.setSelection(currentSura, true)
                    clicked = false
                    current.clear()
                    currentSura = gson[globalVerse].sora
                }
                current.clear()
                stack.clear()

                textView.text = getWords(globalVerse, gson).joinToString(" ")
                textView.setTextColor(Color.GRAY)
                textView.maxLines = 1
            }
        }


        undo.setOnLongClickListener(object : OnContinuousClickListener(200) {
            override fun onContinuousClick(v: View?) {
                if (i > 0 && current.isNotEmpty()) {

                    if (i == getWords(globalVerse, gson).size) {
                        current.delete(
                            current.length - "   ${getWords(globalVerse, gson)[i - 2]}".length,
                            current.length
                        )
                        i-=2
                    }
                    else {

                        current.delete(
                            current.length - " ${getWords(globalVerse, gson)[i - 1]}".length,
                            current.length
                        )
                        i--
                    }

                    textView.text = current.toString()


                    if (i > 0 && current.length < 2)
                        textView.text = current.clear().append(stack.pop())

                }


                if (i == 0 && stack.isNotEmpty()){
                    globalVerse--
                    i = getWords(globalVerse, gson).size
                    textView.text = current.clear().append(stack.pop())
                    slider.value = gson[globalVerse].ayaNo.toFloat()
                    ayaCount.text = "${slider.value.toInt()}"
                }
            }

        })

        undo.setOnClickListener {

            if (i > 0 && current.isNotEmpty()) {

                if (i == getWords(globalVerse, gson).size) {
                    current.delete(
                        current.length - "   ${getWords(globalVerse, gson)[i - 2]}".length,
                        current.length
                    )
                    i-=2
                }
                else {

                    current.delete(
                        current.length - " ${getWords(globalVerse, gson)[i - 1]}".length,
                        current.length
                    )
                    i--
                }

                textView.text = current.toString()


                if (i > 0 && current.length < 2)
                    textView.text = current.clear().append(stack.pop())

            }


            if (i == 0 && stack.isNotEmpty()){
                globalVerse--
                i = getWords(globalVerse, gson).size
                textView.text = current.clear().append(stack.pop())
                slider.value = gson[globalVerse].ayaNo.toFloat()
                ayaCount.text = "${slider.value.toInt()}"
            }



        }

        ayaCount.setOnClickListener {
            i = 0
            current.clear()
            textView.text = getWords(globalVerse, gson).joinToString(" ")
            textView.setTextColor(Color.GRAY)
            textView.maxLines = 1
            stack.clear()
        }

        launch.setOnClickListener {
            val appisFound = isAppInstalled(this, "com.quran.labs.androidquran")
            if(appisFound)
                startNewActivity(globalVerse)
            else{
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.quran.labs.androidquran")))
            }

        }
    }


    private fun getWords(verse: Int, ayaList: AyaList): Array<String> {
        return ayaList[verse].ayaText.split(" ").toTypedArray()
    }


    override fun onPause() {
        super.onPause()
        val putSharedPrefs: SharedPreferences = getSharedPreferences(packageName, MODE_PRIVATE)
        putSharedPrefs.edit().putInt("spinner", spinner.selectedItemPosition).apply()
        putSharedPrefs.edit().putFloat("slider", slider.value).apply()

    }

    private fun startNewActivity(verse: Int) {
        val intent =  Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse("quran://${gson[verse].sora}/${gson[verse].ayaNo}")
        startActivity(intent)
    }

    fun isAppInstalled(context: Context, packageName: String?): Boolean {
        return try {
            if (packageName != null) {
                context.packageManager.getApplicationInfo(packageName, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }




}