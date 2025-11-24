package com.dash.rickymorty

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class PersonajeAdapter(
    private val onVerMasClick: (JSONObject) -> Unit
) : RecyclerView.Adapter<PersonajeAdapter.PersonajeViewHolder>() {

    private val personajes = mutableListOf<JSONObject>()

    fun setPersonajes(list: List<JSONObject>) {
        personajes.clear()
        personajes.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonajeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_personaje_card, parent, false)
        return PersonajeViewHolder(view as ViewGroup)
    }

    override fun getItemCount(): Int = personajes.size

    override fun onBindViewHolder(holder: PersonajeViewHolder, position: Int) {
        val personaje = personajes[position]
        holder.bind(personaje, onVerMasClick)
    }

    class PersonajeViewHolder(private val view: ViewGroup) : RecyclerView.ViewHolder(view) {
        fun bind(personaje: JSONObject, onVerMasClick: (JSONObject) -> Unit) {
            val nombre = view.findViewById<TextView>(R.id.tv_nombre)
            val imagen = view.findViewById<ImageView>(R.id.iv_imagen)
            val btnVerMas = view.findViewById<Button>(R.id.btn_ver_mas)
            nombre.text = personaje.optString("name")
            Glide.with(view.context).load(personaje.optString("image")).into(imagen)
            btnVerMas.setOnClickListener { 
                onVerMasClick(personaje)
            }
        }
    }
}
