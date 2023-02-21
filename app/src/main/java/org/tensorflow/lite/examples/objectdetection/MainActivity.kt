/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.examples.objectdetection

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.examples.objectdetection.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*

/**
 * Main entry point into our app. This app follows the single-activity pattern, and all
 * functionality is implemented in the form of fragments.
 */

const val TTS_ENGINE_RESULT_CODE = 26

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val checkTTSEngineIntent = Intent()
        checkTTSEngineIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
        startActivityForResult(checkTTSEngineIntent, TTS_ENGINE_RESULT_CODE)
    }

    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            // Workaround for Android Q memory leak issue in IRequestFinishCallback$Stub.
            // (https://issuetracker.google.com/issues/139738913)
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language specified is not supported!")
            }
            /*else {
                buttonSpeak!!.isEnabled = true
            }*/

        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TTS_ENGINE_RESULT_CODE) { // Check if the result code is the same as the one sent by the TTS check intent.
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) { // If the device has all the necessary TTS data.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.DONUT) {
                    tts = TextToSpeech(this, TextToSpeech.OnInitListener {
                        // Initialize the TTS engine.
                        tts?.language = Locale.getDefault() // Set the language to the device's locale.
                    })
                }
                button.setOnClickListener {
                    /**
                     * The speak() method signature changed in API 21.
                     */
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        tts?.speak(edit_text.text
                            ?: edit_text.hint, TextToSpeech.QUEUE_FLUSH, null, null) // Make the device read the text in the EditText.
                    } else {
                        tts?.speak((edit_text.text
                            ?: edit_text.hint) as String, TextToSpeech.QUEUE_FLUSH, null) // Make the device read the text in the EditText.
                    }
                }
            } else { // If the data is not available.
                val installTTSDataIntent = Intent() // Create a new intent.
                installTTSDataIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA // Define the intent action as one for installing this missing data, the TTS API provides this.
                startActivity(installTTSDataIntent) // Start the intent for downloading the missing data.
            }
        }
    }

}
