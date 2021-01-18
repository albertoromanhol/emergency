package com.example.emergency

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.provider.CallLog
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.util.Date
import kotlin.collections.ArrayList
import kotlin.math.log

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val requestReadLog = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (supportActionBar != null)
            supportActionBar!!.hide()

        val btn: Button = findViewById(R.id.button)
        btn.setBackgroundColor(Color.RED)
        btn.setOnClickListener(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CALL_LOG
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.SEND_SMS
                ), requestReadLog
            )
        } else {
            loadData()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == requestReadLog) loadData()
    }

    private fun loadData() {

        val list = getCallsDetails(this)

        val adapter = ListAdapter(this, list)

        list_view.adapter = adapter

    }

    private fun getCallsDetails(context: Context): ArrayList<CallDetails> {

        val callDetails = ArrayList<CallDetails>()
        val contentUri = CallLog.Calls.CONTENT_URI

        try {
            val cursor = context.contentResolver.query(contentUri, null, null, null, null)
            val nameUri = cursor!!.getColumnIndex(CallLog.Calls.CACHED_LOOKUP_URI)
            val number = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val duration = cursor.getColumnIndex(CallLog.Calls.DURATION)
            val date = cursor.getColumnIndex(CallLog.Calls.DATE)
            val type = cursor.getColumnIndex(CallLog.Calls.TYPE)

            if(cursor.moveToFirst()){
                do{
                    val callType = when(cursor.getInt(type)){
                        CallLog.Calls.INCOMING_TYPE -> "Incoming"
                        CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                        CallLog.Calls.MISSED_TYPE -> "Missed"
                        CallLog.Calls.REJECTED_TYPE -> "Rejected"
                        else -> "Not Defined"
                    }
                    val phoneNumber = cursor.getString(number)
                    val callerNameUri = cursor.getString(nameUri)
                    val callDate = cursor.getString(date)
                    val callDayTime = Date(callDate.toLong()).toString()
                    val callDuration = cursor.getString(duration)
                    callDetails.add(CallDetails(
                        getCallerName(callerNameUri),
                        phoneNumber,
                        callDuration,
                        callType,
                        callDayTime
                    ))
                }while (cursor.moveToNext())
            }
            cursor.close()
        }catch (e : SecurityException){
            Toast.makeText(this, "User denied permission", Toast.LENGTH_SHORT).show()
        }
        return callDetails

    }

    private fun getCallerName(callerNameUri: String?): String {
        return if(callerNameUri != null){
            val cursor = contentResolver.query(Uri.parse(callerNameUri), null, null, null, null)
            var name = ""
            if((cursor?.count ?: 0) > 0){
                while (cursor != null && cursor.moveToNext()){
                    name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                }
            }

            cursor!!.close()
            return name
        }else{
            "Not a contact!"
        }

    }


    override fun onClick(v: View?) {

        if (v?.id == R.id.button) {
            sendEmergencySMS()
            makeEmergencyCall()

        }
    }

    private fun sendEmergencySMS()
    {
        /// API SMS MANAGER
        val smsManager = SmsManager.getDefault() as SmsManager
        smsManager.sendTextMessage("+5531992827056", null, "Estou aqui", null, null)

        // HARD CODE
//        val uri = Uri.parse("smsto:+5531992827056")
//        val intent = Intent(Intent.ACTION_SENDTO, uri)
//        intent.putExtra("sms_body", "Estou aqui!")
//        startActivity(intent)
    }

    private fun makeEmergencyCall(): Boolean {
        try {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:38256237")
            startActivity(intent)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }


}