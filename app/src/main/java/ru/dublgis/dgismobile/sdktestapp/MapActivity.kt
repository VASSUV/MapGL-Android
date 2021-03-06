package ru.dublgis.dgismobile.sdktestapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import ru.dublgis.dgismobile.mapsdk.LonLat
import ru.dublgis.dgismobile.mapsdk.Map
import ru.dublgis.dgismobile.mapsdk.location.UserLocationOptions
import kotlin.reflect.KClass
import ru.dublgis.dgismobile.mapsdk.MapFragment as DGisMapFragment


abstract class MapActivity : AppCompatActivity() {

    protected var map: Map? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.ActionBarAppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = intent.getStringExtra(TEXT_EXTRA)
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment)
                as DGisMapFragment

        val apiKey = resources.getString(R.string.dgis_map_key)

        mapFragment.mapReadyCallback = this::onDGisMapReady
        mapFragment.setup(
            apiKey = apiKey,
            center = LonLat(55.30771, 25.20314),
            zoom = 12.0
        )

        mapOf(
            R.id.zoom_in to this::zoomInMap,
            R.id.zoom_out to this::zoomOutMap,
            R.id.location to this::centerMap

        ).forEach {
            val btn = findViewById<ImageButton>(it.key)
            btn.setOnClickListener(it.value)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.information_menu, menu)
        return true
    }

    private fun onDGisMapReady(controller: Map?) {
        map = controller
        map?.enableUserLocation(UserLocationOptions(isVisible = true))
        map?.userLocation?.observe(this, Observer {
            Log.i(ru.dublgis.dgismobile.mapsdk.TAG, "Location: $it")
        })

        onDGisMapReady()
    }

    protected abstract fun onDGisMapReady()

    private fun centerMap(@Suppress("UNUSED_PARAMETER") view: View?) {
        map?.run {
            this.userLocation.value?.let {
                center = LonLat(it.longitude, it.latitude)
                zoom = 16.0
            }
        }
    }

    private fun zoomInMap(@Suppress("UNUSED_PARAMETER") view: View?) {
        map?.run {
            zoom = zoom.inc()
        }
    }

    private fun zoomOutMap(@Suppress("UNUSED_PARAMETER") view: View?) {
        map?.run {
            zoom = zoom.dec()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId === android.R.id.home) {
            finish()
        } else if (item.itemId === R.id.information) {
            showInfoDialog()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showInfoDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.info_dialog_title))
        builder.setMessage(getString(R.string.info_dialog_message))
        builder.setCancelable(false)
        builder.setPositiveButton(getString(R.string.info_dialog_positive_text)) { dialog, _ ->
            val openURL = Intent(android.content.Intent.ACTION_VIEW)
            openURL.data = Uri.parse(getString(R.string.info_url))
            startActivity(openURL)
            dialog.dismiss()
        }

        builder.setNegativeButton(getString(R.string.info_dialog_negative_text)) { dialog, _ ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)
    }

    companion object {
        const val TEXT_EXTRA = "TEXT_EXTRA"
        fun startActivity(context: Context, text: String, kClass: KClass<out MapActivity>) {
            val intent = Intent(context, kClass.java)
            intent.putExtra(TEXT_EXTRA, text)
            context.startActivity(intent)
        }
    }
}