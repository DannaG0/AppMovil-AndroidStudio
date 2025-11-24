package com.dash.rickymorty

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.ImageButton
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dash.rickymorty.api.RickAndMortyApi
import kotlin.concurrent.thread
import org.json.JSONObject
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import android.util.Log
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import java.net.URLEncoder

class pantalla_busqueda_filtrado : AppCompatActivity() {

    private lateinit var adapter: PersonajeAdapter
    private val handler = Handler(Looper.getMainLooper())
    private var searchRunnable: Runnable? = null
    private val DEBOUNCE_MS = 500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pantalla_busqueda_filtrado)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.rv_personajes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = PersonajeAdapter { personaje ->
            val intent = Intent(this, pantalla_indetificacion_espacial::class.java)
            intent.putExtra("personaje_id", personaje.optInt("id"))
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        val spinnerFiltros = findViewById<Spinner>(R.id.spinner_filtros)
        val etNombre = findViewById<EditText>(R.id.et_nombre)
        val btnMenu = findViewById<ImageButton>(R.id.btn_menu_hamburguesa)
        val btnFiltrar = findViewById<Button>(R.id.btn_filtrar)

        val filtros = listOf(
            "Sin filtros",
            "Alive",
            "Dead",
            "Unknown",
            "Human",
            "Alien",
            "Humanoid",
            "Robot",
            "Female",
            "Male",
            "Genderless"
        )

        spinnerFiltros.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            filtros
        )

        btnMenu.setOnClickListener {
            val visible = etNombre.visibility == View.VISIBLE
            val newVisibility = if (visible) View.GONE else View.VISIBLE
            etNombre.visibility = newVisibility
            spinnerFiltros.visibility = newVisibility
            btnFiltrar.visibility = newVisibility
        }

        etNombre.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchRunnable?.let { handler.removeCallbacks(it) }
            }
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim().takeIf { !it.isNullOrEmpty() }
                searchRunnable = Runnable {
                    val spinnerSelection = spinnerFiltros.selectedItem?.toString()
                    performSearch(name = query, spinnerSelection = spinnerSelection)
                }
                handler.postDelayed(searchRunnable!!, DEBOUNCE_MS)
            }
        })

        btnFiltrar.setOnClickListener {
            val raw = etNombre.text.toString().trim().takeIf { it.isNotBlank() }
            val spinnerSelection = spinnerFiltros.selectedItem?.toString()
            performSearch(name = raw, spinnerSelection = spinnerSelection)
        }

        loadInitial()
    }

    private fun loadInitial() {
        thread {
            val response = RickAndMortyApi.getCharactersFullResponse()
            if (response == null) {
                runOnUiThread {
                    Toast.makeText(this, "No se pudo cargar la información inicial", Toast.LENGTH_LONG).show()
                }
                return@thread
            }
            val lista = mutableListOf<JSONObject>()
            val personajes = response.optJSONArray("results")
            if (personajes != null) {
                for (i in 0 until personajes.length()) {
                    lista.add(personajes.getJSONObject(i))
                }
            }
            runOnUiThread {
                adapter.setPersonajes(lista)
                Log.d("BusqInit", "Cargados: ${lista.size}")
            }
        }
    }

    private fun performSearch(
        name: String? = null,
        spinnerSelection: String? = null,
        status: String? = null,
        species: String? = null,
        gender: String? = null
    ) {
        var finalStatus = status
        var finalSpecies = species
        var finalGender = gender
        var encodedName: String? = null

        if (!name.isNullOrBlank()) {
            try {
                encodedName = URLEncoder.encode(name, "UTF-8")
            } catch (e: Exception) {
                encodedName = name.replace(" ", "%20")
            }
        }

        if (!spinnerSelection.isNullOrEmpty()) {
            when (spinnerSelection.lowercase()) {
                "alive", "dead", "unknown" -> finalStatus = spinnerSelection.lowercase()
                "human", "alien", "humanoid", "robot" -> finalSpecies = spinnerSelection
                "female", "male", "genderless" -> finalGender = spinnerSelection.lowercase()
                "sin filtros" -> { }
            }
        }

        Log.d("PerformSearch", "name(encoded)=$encodedName status=$finalStatus species=$finalSpecies gender=$finalGender")

        thread {
            val response = try {
                RickAndMortyApi.getCharactersFullResponse(
                    name = encodedName,
                    status = finalStatus,
                    species = finalSpecies,
                    type = null,
                    gender = finalGender
                )
            } catch (e: Exception) {
                null
            }

            if (response == null) {
                runOnUiThread {
                    Toast.makeText(this, "No se pudo contactar la API o no hay resultados", Toast.LENGTH_SHORT).show()
                }
                runOnUiThread { adapter.setPersonajes(mutableListOf()) }
                return@thread
            }

            val personajes = response.optJSONArray("results")
            if (personajes == null || personajes.length() == 0) {
                runOnUiThread {
                    Toast.makeText(this, "No se encontraron resultados", Toast.LENGTH_SHORT).show()
                    adapter.setPersonajes(mutableListOf())
                }
                return@thread
            }

            val lista = mutableListOf<JSONObject>()
            for (i in 0 until personajes.length()) {
                lista.add(personajes.getJSONObject(i))
            }

            runOnUiThread {
                adapter.setPersonajes(lista)
                Log.d("PerformSearch", "Resultados: ${lista.size}")
            }
        }
    }
}
