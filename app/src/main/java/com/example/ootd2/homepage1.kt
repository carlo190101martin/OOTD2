package com.example.ootd2

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


private lateinit var firebaseAuth: FirebaseAuth
data class Person(var imageUrl: String? = null)

private lateinit var recyclerView: RecyclerView
/**
 * A simple [Fragment] subclass.
 * Use the [homepage1.newInstance] factory method to
 * create an instance of this fragment.
 */
class homepage1 : Fragment() {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_homepage1, container, false)
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
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
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

            val recyclerView = view.findViewById<RecyclerView>(R.id.cycle1)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.addItemDecoration(VerticalSpaceItemDecoration(0))
            recyclerView.adapter = adapter
            adapter.startListening()
        }
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    ///override fun onCreateView(
        //inflater: LayoutInflater, container: ViewGroup?,

        //savedInstanceState: Bundle?

   // ): View? {
        // Inflate the layout for this fragment
        //val rootView = inflater.inflate(R.layout.fragment_homepage1, container, false)
        //recyclerView = rootView.findViewById(R.id.cycle1)
        // rootView


   // }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment homepage1.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            homepage1().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}




