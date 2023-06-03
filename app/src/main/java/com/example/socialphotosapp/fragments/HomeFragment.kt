package com.example.socialphotosapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.socialphotosapp.R
import com.example.socialphotosapp.adapter.PostAdapter
import com.example.socialphotosapp.adapter.StoryAdapter
import com.example.socialphotosapp.model.Post
import com.example.socialphotosapp.model.Story
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    private var postAdapter: PostAdapter? = null
    private var postList: MutableList<Post>? = null
    private var followingList: MutableList<String>? = null

    private var storyAdapter: StoryAdapter? = null
    private var storyList: MutableList<Story>? = null

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_home, container, false)


        // Posts recycler view
        var recyclerView: RecyclerView? = null
        recyclerView = view.findViewById(R.id.recycler_view_home)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView.layoutManager = linearLayoutManager
        //

        // Stories recycler view
        var recyclerViewStory: RecyclerView? = null
        recyclerViewStory = view.findViewById(R.id.recycler_view_story)
        val linearLayoutManager2 = LinearLayoutManager(context)
        linearLayoutManager2.reverseLayout = true
        linearLayoutManager2.stackFromEnd = true
        recyclerViewStory.layoutManager = linearLayoutManager2
        //


        postList = ArrayList()
        postAdapter = context?.let { PostAdapter(it, postList as ArrayList<Post>) }
        recyclerView.adapter = postAdapter

        storyList = ArrayList()
        storyAdapter = context?.let { StoryAdapter(it, storyList as ArrayList<Story>) }
        recyclerViewStory.adapter = storyAdapter

        checkFollowings()

        return view
    }

    private fun checkFollowings() {
        followingList = ArrayList()

        val followingRef = FirebaseDatabase.getInstance().reference
                .child("Follow").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("Following")

        followingRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    (followingList as ArrayList<String>).clear()

                    for (snapshot in dataSnapshot.children) {
                        snapshot.key?.let { (followingList as ArrayList<String>).add(it) }
                    }

                    retrievePosts()
                    retrieveStories()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun retrievePosts() {
        val postsRef = FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postList?.clear()

                for (snapshot in dataSnapshot.children) {
                    val post = snapshot.getValue(Post::class.java)
                    for (id in (followingList as ArrayList<String>)) {
                        if (post!!.getPublisher() == id) {
                            postList!!.add(post)
                        }

                        postAdapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun retrieveStories() {
        val storyRef = FirebaseDatabase.getInstance().reference.child("Story")

        storyRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val timeCurrent = System.currentTimeMillis()

                (storyList as ArrayList<Story>).clear()
                (storyList as ArrayList<Story>).add(Story("", 0, 0, "", FirebaseAuth.getInstance().currentUser!!.uid))

                for (id in followingList!!) {
                    var countStory = 0
                    var story: Story? = null

                    for (snapshot in dataSnapshot.child(id).children) {
                        story = snapshot.getValue(Story::class.java)

                        if (timeCurrent > story!!.getTimeStart() && timeCurrent < story!!.getTimeEnd()) {
                            countStory++
                        }
                    }
                    if (countStory > 0) {
                        (storyList as ArrayList<Story>).add(story!!)
                    }
                }
                storyAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}