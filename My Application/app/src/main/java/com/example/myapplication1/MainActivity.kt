package com.example.myapplication1

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.myapplication1.ui.theme.MyApplication1Theme
var flag = false
var newCount = 0
var notiText = ""
var mutList = mutableStateListOf<Any>()
var mutListBackup = ""
var flag1 = false
var phoneNum = ""
open class NotificationListener : NotificationListenerService(){
    private fun sendMessage() {
        val smsManager:SmsManager
        smsManager = this.getSystemService(SmsManager::class.java)
        smsManager.sendTextMessage(phoneNum,null, mutListBackup,null,null)
    }
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        notiText = sbn.notification.toString()
        newCount += 1
        if(sbn.packageName == "com.whatsapp")
        {
            val temp = sbn.notification.extras.getString(NotificationCompat.EXTRA_TITLE).toString()
            if(!flag1){
                mutList.add(temp)
                mutListBackup = sbn.notification.extras.getString(NotificationCompat.EXTRA_TEXT).toString()
                mutList.add(mutListBackup)
                flag1 = true
                sendMessage()
            }
            else if(flag1)
                flag1 = false
        }
    }
   override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
    }
    override fun onListenerConnected() {
        super.onListenerConnected()
        flag = true
    }
}
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplication1Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                )
                {
                    //val test = ActivityResultContracts.RequestPermission()
                    val context = LocalContext.current
                    var noOfClicks = 0
                    var text by remember { mutableStateOf(TextFieldValue("")) }
                    flag = false //Listener check, be default false and turns true when listener connects successfully
                    var checkPerm = false
                    val REQUEST_SMS_PERMISSION = 2
                    // Check if we have the sms permission
                    val hasSmsPermission = ContextCompat.checkSelfPermission(this, "android.permission.SEND_SMS") == PackageManager.PERMISSION_GRANTED
                    if (!hasSmsPermission) {
                        this.requestPermissions(arrayOf("android.permission.SEND_SMS"), REQUEST_SMS_PERMISSION)
                    }
                    if(ContextCompat.checkSelfPermission(this, "android.permission.SEND_SMS") == PackageManager.PERMISSION_DENIED)
                    {
                        Toast.makeText(this, "No SMS permission.",Toast.LENGTH_LONG).show()
                    }
                    else {
                        Toast.makeText(this, "SMS permission granted.",Toast.LENGTH_LONG).show()
                    }
                    checkPerm = NotificationManagerCompat.getEnabledListenerPackages(this).contains(this.packageName)
                    if(checkPerm)
                    {
                        Toast.makeText(this,"Notification Access Granted",Toast.LENGTH_LONG).show()
                        checkPerm = true
                    }
                    else
                    {
                        //checkPerm = true
                        val intent1 = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                        startActivity(intent1)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    )
                    {
                        Row(Modifier.padding(10.dp)){
                            //Text("Text in field = $text")
                            OutlinedTextField(
                                value = text,
                                label = { Text(text = "Enter phone no with +91") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                onValueChange = {
                                    text = it
                                    phoneNum = text.text
                                }
                            )
                        }
                        Column(
                            Modifier
                                .absolutePadding(10.dp, 10.dp, 10.dp, 0.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(Modifier.padding(10.dp)){
                                Button(
                                    onClick = {
                                        // Clear the list of items
                                        Toast.makeText(context,"clear button Pressed",Toast.LENGTH_LONG).show()
                                        mutList.clear()
                                    },Modifier
                                        .absolutePadding(10.dp, 10.dp, 10.dp, 0.dp)
                                ) {
                                    Text("Clear Screen")
                                }
                                Button(onClick = {
                                    val ntemp = NotificationListener()
                                    ntemp.onListenerConnected()
                                    if (flag) {
                                        Toast.makeText(
                                            context,
                                            "Listener Connected, nClicks: $noOfClicks and nc $newCount",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        noOfClicks += 1
                                    } else
                                        Toast.makeText(context, "Listener not connected", Toast.LENGTH_LONG).show()
                                },Modifier
                                    .absolutePadding(10.dp, 10.dp, 10.dp, 0.dp)
                                ) {
                                    Text("Button")
                                }
                            }
                            LazyColumn() {
                                mutList.forEach{ item->
                                    val temp = item.toString()
                                    item(
                                        content = { Text(temp) }
                                    )
                                }
                            }

                        }

                    }

                }
            }
        }
    }
}