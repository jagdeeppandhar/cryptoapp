package com.app.androidassignment

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.app.androidassignment.databinding.ActivityMainBinding
import com.app.helpers.DeviceUtils
import com.app.helpers.KeyStoreManager
import com.app.helpers.WalletUtils
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.json.JSONObject
import org.web3j.crypto.Credentials
import java.io.IOException
import java.security.Security


class MainActivity : AppCompatActivity() {

    private val API_URL = "https://ares-arx-dev.azurewebsites.net/api/Users/AddDeviceAccount"
    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    private lateinit var binding: ActivityMainBinding
    private var deviceID=""
    private var publickey =""



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(!::binding.isInitialized)
            binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deviceID = DeviceUtils.getDeviceID(this)
        binding.deviceId.text=deviceID

        setupBouncyCastle()


        getCryptoData()
        sendData()
    }


    private fun setupBouncyCastle() {
        val provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)
            ?: // Web3j will set up the provider lazily when it's first used.
            return
        if (provider.javaClass == BouncyCastleProvider::class.java) {
            // BC with same package name, shouldn't happen in real life.
            return
        }
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.insertProviderAt(BouncyCastleProvider(), 1)
    }

    private fun getCryptoData() {
        try {
            val credentials: Credentials = WalletUtils.generateEthereumWallet()
            val publicKey = credentials.ecKeyPair.publicKey.toString(16)
            val privateKey = credentials.ecKeyPair.privateKey.toString(16)

            val encryptedPrivateKey = KeyStoreManager.encryptPrivateKey(this, privateKey.encodeToByteArray())
            KeyStoreManager.storeEncryptedPrivateKey(this, encryptedPrivateKey)
            binding.publicKey.text= publicKey
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun sendData() {
        val submitButton: Button = findViewById(R.id.submit_button)
        submitButton.setOnClickListener {

            if(binding.email.text.isEmpty())
            {
                Toast.makeText(this,"Please Enter email address",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            else if (binding.firstName.text.isEmpty()){
                Toast.makeText(this,"Please Enter First Name",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            else if (binding.lastName.text.isEmpty()){
                Toast.makeText(this,"Please Enter Last Name",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            else if (binding.pin.text.isEmpty()){
                Toast.makeText(this,"Please Enter Pin",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            else{

                binding.progressBar.visibility = View.VISIBLE

                val email = binding.email.text.toString()
                val firstName = binding.firstName.text.toString()
                val lastName = binding.lastName.text.toString()
                val pin = binding.pin.text.toString()
                val publicKey = publickey
                val deviceId = deviceID

                val jsonParams = JSONObject().apply {
                    put("EmailAddress", email)
                    put("FirstName", firstName)
                    put("LastName", lastName)
                    put("PinNumber", pin)
                    put("PublicKey", publicKey)
                    put("DeviceID", deviceId)
                }

                val body = RequestBody.create(JSON, jsonParams.toString())
                val request = Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        hideProgressBar()
                        e.printStackTrace()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        hideProgressBar()
                        if (response.isSuccessful) {
                            response.body?.use { responseBody ->
                                val data = responseBody.string()
                                setAsJsonWithColor(data)
                            }
                                binding.email.text.clear()
                                binding.firstName.text.clear()
                                binding.lastName.text.clear()
                                binding.pin.text.clear()

                                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)

                        } else {
                            Toast.makeText(this@MainActivity, "Failed to send data", Toast.LENGTH_SHORT).show()

                        }
                    }
                })
            }

        }
    }

    fun setAsJsonWithColor(data: String) {
        val jsonObject = JSONObject(data)
        val spannableStringBuilder = SpannableStringBuilder()

        for (key in jsonObject.keys()) {
            val keyString = "$key: "
            val valueString = jsonObject.getString(key)

            // Append key (black and bold)
            spannableStringBuilder.append(keyString)
            val startKey = spannableStringBuilder.length - keyString.length
            val endKey = spannableStringBuilder.length
            spannableStringBuilder.setSpan(
                StyleSpan(Typeface.BOLD),
                startKey,
                endKey,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannableStringBuilder.setSpan(
                ForegroundColorSpan(Color.BLACK),
                startKey,
                endKey,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Append value (green)
            spannableStringBuilder.append(valueString)
            val startValue = spannableStringBuilder.length - valueString.length
            val endValue = spannableStringBuilder.length
            spannableStringBuilder.setSpan(
                ForegroundColorSpan(Color.parseColor("#4CAF50")),
                startValue,
                endValue,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            // Append new line
            spannableStringBuilder.append("\n")
        }

        runOnUiThread {
            binding.responseView.text = spannableStringBuilder
        }
    }

    private fun hideProgressBar() {
        runOnUiThread {
            binding.progressBar.visibility = View.GONE
        }
    }

}