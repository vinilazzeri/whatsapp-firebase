package com.vinilazzeri.projetowhatsappfirebase.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.vinilazzeri.projetowhatsappfirebase.R
import com.vinilazzeri.projetowhatsappfirebase.databinding.ActivityLoginBinding
import com.vinilazzeri.projetowhatsappfirebase.utils.showMessage

class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private lateinit var email: String
    private lateinit var password: String

    //firebase
    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(binding.root)
        initializeClickListeners()
        firebaseAuth.signOut()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStart() {
        super.onStart()
        checkUserLoggedIn()
    }

    private fun checkUserLoggedIn() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null ){
            startActivity(
                Intent(this, MainActivity::class.java)
            )
        }
    }

    private fun initializeClickListeners(){
        binding.createAccountText.setOnClickListener {
            startActivity(
                Intent(this, SignUpActivity::class.java)
            )
        }

        binding.btnLogin.setOnClickListener {
            if ( checkInputs() ){
                userLogin()
            }
        }
    }

    private fun userLogin() {

        firebaseAuth.signInWithEmailAndPassword(
            email, password
        ).addOnSuccessListener {
            showMessage("Successfully logged in!")
            startActivity(
                Intent(this, MainActivity::class.java)
            )
        }.addOnFailureListener {

            try {
                throw it
            } catch ( invalidUser: FirebaseAuthInvalidUserException){
                invalidUser.printStackTrace()
                showMessage("E-mail not registered")
            } catch ( invalidCredentials: FirebaseAuthInvalidCredentialsException){
                invalidCredentials.printStackTrace()
                showMessage("E-mail or password ain't matching")
            }

        }

    }

    private fun checkInputs(): Boolean {
        email = binding.textEditLoginEmail.text.toString()
        password = binding.textEditLoginPassword.text.toString()

        if ( email.isNotEmpty()){
            binding.textInputLayoutLoginEmail.error = null
            if ( password.isNotEmpty() ){
                binding.textInputLayoutLoginPassword.error = null
                return true
            } else {
                binding.textInputLayoutLoginPassword.error = "Make sure password field is filled"
                return false
            }
        } else {
            binding.textInputLayoutLoginEmail.error = "Make sure e-mail field is filled"
            return false
        }
    }


}