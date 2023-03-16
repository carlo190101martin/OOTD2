package com.example.ootd2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.ootd2.databinding.ActivityTabBarBinding


class TabBarActivity : AppCompatActivity() {
    private lateinit var bindingw : ActivityTabBarBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingw = ActivityTabBarBinding.inflate(layoutInflater)
        setContentView(bindingw.root)
        replaceFragment(homepage1())


        bindingw.bottomNavigationView4.setOnItemSelectedListener {
            when (it.itemId) {

                R.id.home -> replaceFragment(homepage1())
                R.id.ranking -> replaceFragment(ranking())
                R.id.people -> replaceFragment(people())

                else -> {


                }

            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }
}