package com.example.ootd2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class homepage : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    data class Person(var imageUrl: String? = null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val query = FirebaseDatabase.getInstance().reference.child("people")
            val options = FirebaseRecyclerOptions.Builder<Person>()
                .setQuery(query, Person::class.java)
                .build()
            Log.d("DataSnapshot1", options.toString())


            class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                val photoImageView = itemView.findViewById<ImageView>(R.id.image_view)
            }

            val adapter = object : FirebaseRecyclerAdapter<Person, PhotoViewHolder>(options) {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
                    val view =
                        LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
                    return PhotoViewHolder(view)
                }

                override fun onBindViewHolder(holder: PhotoViewHolder, position: Int, model: Person) {
                    val imageUrl = model.imageUrl
                    if (imageUrl != null) {
                        Log.d("ImageUrl", imageUrl)
                        Glide.with(holder.itemView.context)
                            .load(imageUrl)
                            .into(holder.photoImageView)
                    }
                }

            }

            val recyclerView = findViewById<RecyclerView>(R.id.cycle)
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
            adapter.startListening()
        }
    }
}



