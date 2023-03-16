package com.example.ootd2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage


class settings : AppCompatActivity() {
    private lateinit var image: ImageView
    lateinit var ImageUri : Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        var imageUploaded = false
        val add1: Button = findViewById(R.id.addphoto)
      add1.setOnClickListener {
          image = findViewById(R.id.preview1)

          uploadImage(image)
          imageUploaded = true




        }


        FirebaseApp.initializeApp(this)


        val btnLogout1: Button = findViewById(R.id.btnLogout)
        btnLogout1.setOnClickListener {
            Firebase.auth.signOut()
            val intent13 = Intent(this, MainActivity::class.java)
                startActivity(intent13)
        }


        val submit3: Button = findViewById(R.id.submit)

        submit3.setOnClickListener {

            val hobby1: TextView = findViewById(R.id.hobby)
            val pro1: TextView = findViewById(R.id.professional)
            val hobby = hobby1.text.toString()
            val pro = pro1.text.toString()



            if (hobby.isEmpty() || pro.isEmpty()) {
                println("missing")
                val alert = AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Missing Professional Field and/or Hobby.")
                    .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
                    .create()
                alert.show()

            } else if  (!imageUploaded) {

                val alert = AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Missing Photo.")
                    .setPositiveButton("Ok") { dialog, _ -> dialog.dismiss() }
                    .create()
                alert.show()

            } else {

                val user = Firebase.auth.currentUser

                // proceed with your code
                user?.uid?.let {
                    val database = Firebase.database

                    val myRef = database.getReference("people")
                    myRef.child(it).child("Education").setValue(hobby)
                    myRef.child(it).child("WhatIamConsideringBuying").setValue(pro)

                    val storageRef = FirebaseStorage.getInstance().reference
                    val filePath = storageRef.child("people/$it}")

                    val uploadTask = filePath.putFile(ImageUri)

                    uploadTask.addOnSuccessListener {
                        filePath.downloadUrl.addOnSuccessListener {
                            val imageUrl = it.toString()
                            myRef.child(user.uid).child("imageUrl").setValue(imageUrl)                        }
                    }


                    }
                val progressView: ProgressBar = findViewById(R.id.progressView1)
                progressView.max = 100 // set the maximum value of the progress bar
                progressView.progress = 0 // set the initial progress to 0
                progressView.visibility = View.VISIBLE // make the progress bar visible

                val handler = Handler()
                val runnable = Runnable {
                    val intent = Intent(this, TabBarActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                val progressRunnable = object : Runnable {
                    var progress = 0
                    override fun run() {
                        progressView.progress = progress
                        progress += 18
                        if (progress > 100) {
                            handler.removeCallbacks(this)
                            handler.post(runnable)
                        } else {
                            handler.postDelayed(this, 1000)
                        }
                    }
                }
                handler.post(progressRunnable)
            }



        }


        }




    private fun uploadImage(image: ImageView?) {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, 1)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1) {
            image.setImageURI(data?.data)
            ImageUri = data?.data!!

        }
    }
}