package com.example.childapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PasswordActivity : AppCompatActivity(R.layout.activity_password) {

    lateinit var checkBtn : Button
    lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkBtn = findViewById(R.id.check_pass)
        passwordEditText = findViewById(R.id.inputPassword)
        checkBtn.setOnClickListener {
            if (passwordEditText.text.toString() == "4554") {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "رمز وارد شده نادرست است!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}