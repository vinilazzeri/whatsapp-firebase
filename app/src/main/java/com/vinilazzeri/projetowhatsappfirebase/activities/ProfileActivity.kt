package com.vinilazzeri.projetowhatsappfirebase.activities

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.vinilazzeri.projetowhatsappfirebase.R
import com.vinilazzeri.projetowhatsappfirebase.databinding.ActivityProfileBinding
import com.vinilazzeri.projetowhatsappfirebase.utils.showMessage

class ProfileActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }

    private var cameraPermission = false
    private var galleryPermission = false



    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val storage by lazy {
        FirebaseStorage.getInstance()
    }
    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }


    private val galleryManager = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { url ->
        if (url != null) {
            binding.imgProfile.setImageURI(url)
            imgUploadStorage(url)
        } else {
            showMessage("There ain't no images selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        toolBarInitialize()
        permissionsRequest()
        initializeClickListeners()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStart() {
        super.onStart()
        fetchUserData()
    }

    private fun fetchUserData() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null){
            firestore
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener {
                    val userData = it.data
                    if (userData != null) {

                        val name = userData["name"] as String
                        val photo = userData["photo"] as String

                        binding.textEditProfileUsername.setText(name)
                        if ( photo.isNotEmpty() ){
                            Picasso.get()
                                .load(photo)
                                .into(binding.imgProfile)
                        }
                    }
                }
        }
    }

    private fun imgUploadStorage(url: Uri) {

        val idUser =  firebaseAuth.currentUser?.uid
        if (idUser != null){
            //photos -> user -> id -> profile.jpg
            storage
                .getReference("photos")
                .child("users")
                .child(idUser)
                .child("profile.jpg")
                .putFile(url)
                .addOnSuccessListener {
                    showMessage("Image successfully uploaded!")
                    it.metadata
                        ?.reference
                        ?.downloadUrl
                        ?.addOnSuccessListener {

                            val data = mapOf(
                                "photo" to it.toString()
                            )
                            profileDataUpdate(idUser, data)
                        }
                }.addOnFailureListener {
                    showMessage("Upload image error")
                }
        }
    }

    private fun profileDataUpdate(idUser: String, data: Map<String, String>) {

        firestore
            .collection("users")
            .document(idUser)
            .update(data)
            .addOnSuccessListener {
                showMessage("Profile successfully updated!")
            }
            .addOnFailureListener {
                showMessage("Profile update failed")
            }
    }



    private fun permissionsRequest() {
        // Verificar permissões de usuário
        cameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        galleryPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        // Lista de permissões negadas
        val deniedPermissionsList = mutableListOf<String>()
        if (!cameraPermission)
            deniedPermissionsList.add(Manifest.permission.CAMERA)
        if (!galleryPermission)
            deniedPermissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE)

        // Solicitar múltiplas permissões
        val permissionsManager = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            cameraPermission = permissions[Manifest.permission.CAMERA] ?: cameraPermission
            galleryPermission = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: galleryPermission
        }
        permissionsManager.launch(deniedPermissionsList.toTypedArray())
    }

    private fun initializeClickListeners() {
        binding.selectionFab.setOnClickListener {
            if (galleryPermission) {
                galleryManager.launch("image/*")
            } else {
                showMessage("User permission is required to access gallery images")
                permissionsRequest()
            }
        }

        binding.btnUpdateProfile.setOnClickListener {
            val username = binding.textEditProfileUsername.text.toString()
            if ( username.isNotEmpty() ) {
                val idUser = firebaseAuth.currentUser?.uid
                if (idUser != null){
                    val data = mapOf(
                        "name" to username
                    )
                    profileDataUpdate(idUser, data)
                }
            } else {
                showMessage("Make sure you've filled name section before upload it")
            }
        }

    }

    private fun toolBarInitialize() {
        val toolbar = binding.profileIncludeToolbar.mainTb
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Edit profile"
            setDisplayHomeAsUpEnabled(true)
        }
    }
}
