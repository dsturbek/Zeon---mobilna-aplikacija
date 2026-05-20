package com.example.zeon.presentation.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.zeon.HomeActivity
import com.example.zeon.R
import com.example.zeon.data.repository.UserRepository
import com.example.zeon.helpers.UserSession
import com.example.zeon.presentation.ui.activities.CompleteProfileActivity
import kotlinx.coroutines.launch

class SignupFragment : Fragment() {

    private lateinit var repository: UserRepository
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        val emailEditText = view.findViewById<EditText>(R.id.email)
        val passwordEditText = view.findViewById<EditText>(R.id.password)
        val confirmPasswordEditText = view.findViewById<EditText>(R.id.confirm_password)
        val signUpButton = view.findViewById<Button>(R.id.sign_in)
        val toLoginText = view.findViewById<TextView>(R.id.to_login)
        progressBar = view.findViewById(R.id.progress_bar)

        repository = UserRepository()

        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Unesite sve podatke", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(requireContext(), "Lozinka mora imati minimalno 6 znakova", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Lozinke se ne podudaraju", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performRegistration(email, password)
        }

        toLoginText.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun performRegistration(email: String, password: String) {
        showLoading(true)

        val username = email.substringBefore("@")
        val nameSurname = username

        lifecycleScope.launch {
            val result = repository.registerClient(nameSurname, email, username, password, null)

            showLoading(false)

            result.onSuccess { user ->
                val profileResult = repository.getUserById(user.id)

                profileResult.onSuccess { fullUser ->
                    UserSession.saveUser(requireContext(), fullUser)

                    val intent = Intent(requireContext(), CompleteProfileActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }.onFailure { exception ->
                Toast.makeText(
                    requireContext(),
                    exception.message ?: "Registration failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}