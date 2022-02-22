package br.com.tabajara.continuousscrollable

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSprit = findViewById<Button>(R.id.btn_sprit)
        btnSprit?.setOnClickListener {
            val intent = Intent(applicationContext, SpriteActivity::class.java)
            startActivity(intent)
        }
    }
}