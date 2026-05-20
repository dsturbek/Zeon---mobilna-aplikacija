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
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private lateinit var repository: UserRepository
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val emailEditText = view.findViewById<EditText>(R.id.email)
        val passwordEditText = view.findViewById<EditText>(R.id.password)
        val signInButton = view.findViewById<Button>(R.id.sign_in)
        val toRegisterText = view.findViewById<TextView>(R.id.to_register)
        progressBar = view.findViewById(R.id.progress_bar)

        repository = UserRepository()

        signInButton.setOnClickListener {
            val username = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Unesite email i lozinku", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(username, password)
        }

        toRegisterText.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SignupFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun performLogin(username: String, password: String) {
        showLoading(true)

        lifecycleScope.launch {
            val result = repository.login(username, password, "client")

            result.onSuccess { user ->
                val profileResult = repository.getUserById(user.id)

                showLoading(false)

                profileResult.onSuccess { fullUser ->
                    UserSession.saveUser(requireContext(), fullUser)

                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(requireContext(), HomeActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }.onFailure {
                    UserSession.saveUser(requireContext(), user)

                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(requireContext(), HomeActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
            }.onFailure { exception ->
                showLoading(false)
                Toast.makeText(
                    requireContext(),
                    exception.message ?: "Login failed",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}