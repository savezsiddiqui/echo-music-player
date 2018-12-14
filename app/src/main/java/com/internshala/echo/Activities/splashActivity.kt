package com.internshala.echo.Activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.view.WindowManager
import android.widget.Toast
import com.internshala.echo.R

class splashActivity : AppCompatActivity() {

    var permissionList = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS)

    override fun onCreate(savedInstanceState: Bundle?) {

        //Removes notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if(!hasAllPermissions(this@splashActivity,*permissionList)){
           //we need to ask for permissions
            ActivityCompat.requestPermissions(this@splashActivity, permissionList, 131)
        }
        else Handler().postDelayed({
            var startIntent = Intent(this@splashActivity, MainActivity:: class.java)
            startActivity(startIntent)
            this.finish()
        }, 1000)
    }

    fun hasAllPermissions(context: Context, vararg Permissions : String) : Boolean
    {
        var flag = true
        for(permission in Permissions)
        {
            var res = context.checkCallingOrSelfPermission(permission)
            if(res != PackageManager.PERMISSION_GRANTED)
                flag = false
        }
        return flag
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
         when(requestCode){
             131 -> {
                 if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                         && grantResults[1] == PackageManager.PERMISSION_GRANTED
                         && grantResults[2] == PackageManager.PERMISSION_GRANTED
                         && grantResults[3] == PackageManager.PERMISSION_GRANTED
                         && grantResults[4] == PackageManager.PERMISSION_GRANTED) {
                     Handler().postDelayed({
                         var startIntent = Intent(this@splashActivity, MainActivity:: class.java)
                         startActivity(startIntent)
                         this.finish()
                     }, 1000)
                     return
                         }
                 else{
                     Toast.makeText(this@splashActivity, "Please grant all permissions to continue ",Toast.LENGTH_LONG).show()
                     this.finish()
                 }
             }
             else ->{
                 Toast.makeText(this@splashActivity, "Something went Wrong", Toast.LENGTH_SHORT).show()
                 this.finish()
                 return
             }
         }
    }
}
