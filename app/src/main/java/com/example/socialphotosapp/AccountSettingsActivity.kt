package com.example.socialphotosapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.socialphotosapp.model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_settings.*

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        logout_btn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        change_image_text_btn.setOnClickListener {
            checker = "clicked"
            CropImage.activity()
                .setAspectRatio(1, 1)
                .start(this@AccountSettingsActivity)
        }

        save_info_profile_btn.setOnClickListener {
            if (checker == "clicked") {
                uploadImageAndUpdateInfo()
            } else {
                updateUserInfoOnly()
            }
        }

        close_profile_btn.setOnClickListener {
            val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        userInfo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE
            && resultCode == Activity.RESULT_OK
            && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            profile_image_view_profile_frag.setImageURI(imageUri)
        }
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
        val usersRef = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
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

    private fun uploadImageAndUpdateInfo() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Account Settings")
        progressDialog.setMessage("Please wait, updating info...")
        progressDialog.show()

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

        when {
            userMap.isEmpty() -> {
                Toast.makeText(this, "Please fill in a field.", Toast.LENGTH_LONG).show()
            }
            imageUri == null -> {
                Toast.makeText(this, "Please select an image.", Toast.LENGTH_LONG).show()
            }
            else -> {
                val fileRef = storageProfilePicRef!!.child(firebaseUser!!.uid + ".jpg")
                var uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(
                    Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }
                ).addOnCompleteListener(OnCompleteListener<Uri> { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val usersRef = FirebaseDatabase.getInstance().reference.child("Users")
                        userMap["image"] = myUrl
                        usersRef.child(firebaseUser.uid).updateChildren(userMap)

                        Toast.makeText(this, "Info updated successfully", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    progressDialog.dismiss()
                })
            }
        }
    }
}