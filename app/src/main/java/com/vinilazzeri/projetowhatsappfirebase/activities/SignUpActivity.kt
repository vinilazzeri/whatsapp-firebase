package com.vinilazzeri.projetowhatsappfirebase.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.vinilazzeri.projetowhatsappfirebase.R
import com.vinilazzeri.projetowhatsappfirebase.databinding.ActivitySignUpBinding
import com.vinilazzeri.projetowhatsappfirebase.model.User
import com.vinilazzeri.projetowhatsappfirebase.utils.showMessage

class SignUpActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }

    private lateinit var username: String
    private lateinit var email: String
    private lateinit var password: String

    //Firebase
    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val firestore by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(binding.root)

        toolBarInitialize()
        initializeClickListeners()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


    }


    private fun initializeClickListeners() {
        binding.btnCreateAccount.setOnClickListener {
            if( checkInputs() ){
                createUser(username, email, password)
            }
        }
    }

    private fun createUser(username: String, email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(
            email, password
        ).addOnCompleteListener{
            if (it.isSuccessful){
                val user_id = it.result.user?.uid
                if (user_id != null){
                    val user = User(
                        id = user_id, name = username, email = email
                    )
                    saveUserFirestore(user)
                }
            }

        }.addOnFailureListener {
            try {
                throw it
            } catch ( invalidPassword: FirebaseAuthWeakPasswordException ){
                invalidPassword.printStackTrace()
                showMessage("Your password is too weak!")
            } catch ( invalidUserAlreadyExist: FirebaseAuthUserCollisionException ){
                invalidUserAlreadyExist.printStackTrace()
                showMessage("E-mail already belongs to another account!")
            } catch ( invalidLoginCredentials: FirebaseAuthInvalidCredentialsException ){
                invalidLoginCredentials.printStackTrace()
                showMessage("Invalid e-mail!")
            }
        }
    }

    private fun saveUserFirestore(user: User) {

        firestore
            .collection("users")
            .document(user.id)
            .set(user)
            .addOnSuccessListener {
                showMessage("Account successfully created!")
                startActivity(
                    Intent(applicationContext, MainActivity::class.java)
                )
            }.addOnFailureListener {
                showMessage("Error on creating your account!")
            }

    }

    private fun checkInputs(): Boolean {

        username = binding.textEditSignupName.text.toString()
        email = binding.textEditSignupEmail.text.toString()
        password = binding.textEditSignupPassword.text.toString()

        if(username.isNotEmpty()){
            binding.textInputLayoutSignupUsername.error = null

            if(email.isNotEmpty()){
                binding.textInputLayoutSignupEmail.error = null

                if(password.isNotEmpty()){
                    binding.textInputLayoutSignupPassword.error = null
                    return true
                }else{
                    binding.textInputLayoutSignupPassword.error = "Make sure you've filled password field."
                    return false
                }
            }else{
                binding.textInputLayoutSignupEmail.error = "Make sure you've filled e-mail field."
                return false
            }
        } else {
            binding.textInputLayoutSignupUsername.error = "Make sure you've filled name field."
            return false
        }
    }



    private fun toolBarInitialize() {
        val toolbar = binding.includeToolbar.mainTb
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            title = "Sign up now!"
            setDisplayHomeAsUpEnabled(true)
        }
    }
}