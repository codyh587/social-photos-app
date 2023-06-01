package com.example.socialphotosapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.socialphotosapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account_settings.*

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        logout_btn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        save_info_profile_btn.setOnClickListener {
            if (checker == "clicked") {

            } else {
                updateUserInfoOnly()
            }
        }

        userInfo()
    }

    private fun updateUserInfoOnly() {
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        val userMap = HashMap<String, Any>()

        when {
            full_name_profile_frag.text.toString() != "" -> {
                userMap["fullname"] = full_name_profile_frag.text.toString().toLowerCase()
            }
            username_profile_frag.text.toString() != "" -> {
                userMap["username"] = username_profile_frag.text.toString().toLowerCase()
            }
            bio_profile_frag.text.toString() != "" -> {
                userMap["bio"] = bio_profile_frag.text.toString().toLowerCase()
            }
        }

        if (userMap.isEmpty()) {
            Toast.makeText(this, "Please fill in a field.", Toast.LENGTH_LONG).show()
        } else {
            usersRef.child(firebaseUser.uid).updateChildren(userMap)
            Toast.makeText(this, "Info edited successfully", Toast.LENGTH_LONG).show()
            val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun userInfo() {
        val usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseUser.uid)
        usersRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profile_image_view_profile_frag)
                    username_profile_frag.setText(user.getUsername())
                    full_name_profile_frag.setText(user.getFullname())
                    bio_profile_frag.setText(user.getBio())
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}