package com.dash.rickymorty.api

import org.json.JSONObject
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

object RickAndMortyApi {
    private const val BASE_URL = "https://rickandmortyapi.com/api/character"

    fun getCharactersFullResponse(
        name: String? = null,
        status: String? = null,
        species: String? = null,
        type: String? = null,
        gender: String? = null,
        page: Int = 1
    ): JSONObject? {
        val urlBuilder = StringBuilder("$BASE_URL?page=$page")
        if (!name.isNullOrEmpty()) urlBuilder.append("&name=$name")
        if (!status.isNullOrEmpty()) urlBuilder.append("&status=$status")
        if (!species.isNullOrEmpty()) urlBuilder.append("&species=$species")
        if (!type.isNullOrEmpty()) urlBuilder.append("&type=$type")
        if (!gender.isNullOrEmpty()) urlBuilder.append("&gender=$gender")
        val url = URL(urlBuilder.toString())
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 5000
        conn.readTimeout = 5000

        return try {
            val response = conn.inputStream.bufferedReader().readText()
            JSONObject(response)
        } catch (e: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

    fun getCharacterById(id: Int): JSONObject? {
        val url = URL("$BASE_URL/$id")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 5000
        conn.readTimeout = 5000

        return try {
            val response = conn.inputStream.bufferedReader().readText()
            JSONObject(response)
        } catch (e: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

    fun getEpisodeById(id: Int): JSONObject? {
        val url = URL("https://rickandmortyapi.com/api/episode/$id")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connectTimeout = 5000
        conn.readTimeout = 5000

        return try {
            val response = conn.inputStream.bufferedReader().readText()
            JSONObject(response)
        } catch (e: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }
}
