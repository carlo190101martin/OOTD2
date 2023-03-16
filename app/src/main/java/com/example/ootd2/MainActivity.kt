package com.example.ootd2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ootd2.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)


        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        val button4: Button = findViewById(R.id.register)
        button4.setOnClickListener {

            val email = binding.emailET.text.toString()
            val pass = binding.passET.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() ) {
                    firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                        if (it.isSuccessful) {
                            val intent = Intent(this , settings::class.java)
                            startActivity(intent)

                        } else {

                            Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }



            }
            else{
                    Toast.makeText( this , "Empty fields are not allowed", Toast.LENGTH_SHORT).show()

                }

            }
        val button5: Button = findViewById(R.id.Loginbutton)

       button5.setOnClickListener{
            val email = binding.emailET.text.toString()
            val pass = binding.passET.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() ) {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent1 = Intent(this , settings::class.java)
                        startActivity(intent1)


                    } else {
                        Toast.makeText(this , "Credential error. Please enter the correct email and password", Toast.LENGTH_SHORT).show()



                    }
                }

            }
                else {
                         Toast.makeText(this , "Empty fields are not allowed", Toast.LENGTH_SHORT).show()


                }


        }




    }
    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            val intent13 = Intent(this , settings::class.java)
            startActivity(intent13)

        }

    }

}


