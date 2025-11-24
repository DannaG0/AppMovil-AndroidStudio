package com.dash.rickymorty

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import android.content.Intent
import com.bumptech.glide.Glide
import com.dash.rickymorty.api.RickAndMortyApi
import kotlin.concurrent.thread

class pantalla_indetificacion_espacial : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pantalla_indetificacion_espacial)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val personajeId = intent.getIntExtra("personaje_id", -1)
        if (personajeId != -1) {
            thread {
                val personaje = RickAndMortyApi.getCharacterById(personajeId)
                personaje?.let {
                    val episodios = it.optJSONArray("episode")
                    var ultimaAparicion = ""
                    if (episodios != null && episodios.length() > 0) {
                        val ultimaUrl = episodios.optString(episodios.length() - 1)
                        val idEpisodio = ultimaUrl.substringAfterLast("/")
                        val episodioObj = RickAndMortyApi.getEpisodeById(idEpisodio.toIntOrNull() ?: 1)
                        episodioObj?.let { ep ->
                            ultimaAparicion = "${ep.optString("name")} (${ep.optString("air_date")})"
                        }
                    }
                    runOnUiThread {
                        val ivFoto = findViewById<ImageView>(R.id.iv_personaje_foto)
                        val tvEspecie = findViewById<TextView>(R.id.tv_especie)
                        val tvEstado = findViewById<TextView>(R.id.tv_estado)
                        val tvLocacion = findViewById<TextView>(R.id.tv_locacion)
                        val tvUltima = findViewById<TextView>(R.id.tv_ultima)

                        Glide.with(this).load(it.optString("image")).into(ivFoto)
                        tvEspecie.text = "ESPECIE: ${it.optString("species")}"
                        tvEstado.text = "ESTADO: ${it.optString("status")}"
                        tvLocacion.text = "LOCACIÓN: ${it.optJSONObject("location")?.optString("name")}"
                        tvUltima.text = "ULTIMA APARICIÓN: $ultimaAparicion"
                    }
                }
            }
        }

        val btnVolver = findViewById<Button>(R.id.btn_volver_catalogo)
        btnVolver.setOnClickListener {
            val intent = Intent(this, pantalla_busqueda_filtrado::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
    }
}