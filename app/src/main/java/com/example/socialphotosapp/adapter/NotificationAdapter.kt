package com.example.socialphotosapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.socialphotosapp.R
import com.example.socialphotosapp.fragments.PostDetailsFragment
import com.example.socialphotosapp.fragments.ProfileFragment
import com.example.socialphotosapp.model.Notification
import com.example.socialphotosapp.model.Post
import com.example.socialphotosapp.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class NotificationAdapter(private val mContext: Context,
                          private val mNotification: List<Notification>
                          ): RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.notifications_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mNotification.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = mNotification[position]

        if (notification.getText() == "started following you") {
            holder.text.text = "started following you"
        } else if (notification.getText() == "liked your post") {
            holder.text.text = "liked your post"
        } else if (notification.getText().contains("commented:")) {
            holder.text.text = notification.getText().replace("commented:", "commented: ")
        } else {
            holder.text.text = notification.getText()
        }

        userInfo(holder.profileImage, holder.username, notification.getUserId())

        if (notification.getIsPost()) {
            holder.postImage.visibility = View.VISIBLE
            getPostImage(holder.postImage, notification.getPostId())
        } else {
            holder.postImage.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            if (notification.getIsPost()) {
                val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                editor.putString("postId", notification.getPostId())
                editor.apply()
                (mContext as FragmentActivity).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, PostDetailsFragment()).commit()

            } else {
                val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                editor.putString("profileId", notification.getPostId())
                editor.apply()
                (mContext as FragmentActivity).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ProfileFragment()).commit()
            }
        }
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView) {
        var postImage: ImageView
        var profileImage: CircleImageView
        var username: TextView
        var text: TextView

        init {
            postImage= itemView.findViewById(R.id.notification_post_image)
            profileImage = itemView.findViewById(R.id.notification_profile_image)
            username = itemView.findViewById(R.id.username_notification)
            text = itemView.findViewById(R.id.comment_notification)
        }
    }

    private fun userInfo(imageView: ImageView, username: TextView, publisherId: String) {
        val usersRef = FirebaseDatabase.getInstance().reference
            .child("Users")
            .child(publisherId)

        usersRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(imageView)
                    username.text = user!!.getUsername()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getPostImage(imageView: ImageView, postIdParam: String) {
        val postRef = FirebaseDatabase.getInstance().reference
            .child("Posts")
            .child(postIdParam)

        postRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val post = dataSnapshot.getValue<Post>(Post::class.java)
                    Picasso.get().load(post!!.getPostImage()).placeholder(R.drawable.profile).into(imageView)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}