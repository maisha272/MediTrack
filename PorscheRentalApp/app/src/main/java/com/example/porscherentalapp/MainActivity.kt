package com.example.porscherentalapp

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.MenuInflater
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.example.porscherentalapp.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import android.os.Parcelable
import android.os.Build


class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    private var shownCars: List<Car> = CarRepository.cars
    private var current = 0

    companion object {
        private const val KEY_CURRENT_MODEL = "current_model"
        private const val KEY_SEARCH_QUERY  = "search_query"
    }
    inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? =
        if (Build.VERSION.SDK_INT >= 33) getParcelableExtra(key, T::class.java)
        else @Suppress("DEPRECATION") getParcelableExtra(key)


    private val rentLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            b.tvBalance.text = getString(R.string.balance_format, Session.balance)

            when (result.resultCode) {
                RESULT_OK -> {
                    val bookedCar = result.data?.parcelable<Car>("booked_car")
                    val charged   = result.data?.getIntExtra("charged", 0) ?: 0

                    rebuildDisplay(resetIndex = true)

                    Snackbar.make(b.root, getString(R.string.msg_booked), Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo) {
                            bookedCar?.let { car ->
                                Session.balance += charged
                                if (CarRepository.cars.none { it.model.equals(car.model, true) }) {
                                    CarRepository.cars.add(car)
                                }
                                b.tvBalance.text = getString(R.string.balance_format, Session.balance)
                                rebuildDisplay(resetIndex = false)
                                indexOfModel(shownCars, car.model).takeIf { it >= 0 }?.let {
                                    current = it; bindCar()
                                } ?: bindCar()
                            }
                        }
                        .show()
                }
                RESULT_CANCELED -> {
                    val cancelCar = result.data?.parcelable<Car>("cancel_car")
                    Snackbar.make(b.root, getString(R.string.msg_cancelled), Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo) {
                            cancelCar?.let { car ->
                                val i = Intent(this@MainActivity, RentActivity::class.java)
                                    .putExtra("car", car)
                                    .putExtra("modelKey", car.model)
                                rentLauncher.launch(i)
                            }
                        }
                        .show()
                }
            }
        }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_CURRENT_MODEL, shownCars.getOrNull(current)?.model)
        outState.putString(KEY_SEARCH_QUERY, b.searchView.query?.toString() ?: "")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.tvBalance.text = getString(R.string.balance_format, Session.balance)

        // Dark mode
        b.switchDark.setOnCheckedChangeListener { _, checked ->
            AppCompatDelegate.setDefaultNightMode(
                if (checked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Search
        b.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                rebuildDisplay(query = newText.orEmpty(), resetIndex = true)
                return true
            }
        })

        // Sort menu
        b.btnSort.setOnClickListener { v ->
            val pm = PopupMenu(this, v)
            MenuInflater(this).inflate(R.menu.menu_sort, pm.menu)
            pm.setOnMenuItemClickListener { item ->
                shownCars = when (item.itemId) {
                    R.id.sort_rating -> shownCars.sortedByDescending { c -> c.rating }
                    R.id.sort_year   -> shownCars.sortedByDescending { c -> c.year }
                    R.id.sort_cost   -> shownCars.sortedBy { c -> c.costPerDay }
                    else -> shownCars
                }
                current = 0
                bindCar()
                true
            }
            pm.show()
        }

        // Nav
        b.btnNext.setOnClickListener {
            if (shownCars.isNotEmpty()) {
                current = (current + 1) % shownCars.size
                bindCar()
            }
        }
        b.btnPrev.setOnClickListener {
            if (shownCars.isNotEmpty()) {
                current = if (current - 1 < 0) shownCars.lastIndex else current - 1
                bindCar()
            }
        }

        // Rent
        b.btnRent.setOnClickListener {
            val car = shownCars.getOrNull(current) ?: return@setOnClickListener
            val i = Intent(this, RentActivity::class.java)
                .putExtra("car", car)
                .putExtra("modelKey", car.model)
            rentLauncher.launch(i)
        }

        // Favourite toggle
        b.btnFav.setOnClickListener {
            val car = shownCars.getOrNull(current) ?: return@setOnClickListener
            val nowFav = if (Session.favourites.contains(car.model)) {
                Session.favourites.remove(car.model); false
            } else {
                Session.favourites.add(car.model); true
            }
            b.btnFav.setImageResource(if (nowFav) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
            renderFavChips()
            snack(if (nowFav) getString(R.string.msg_added_fav) else getString(R.string.msg_removed_fav))
        }

        // First draw (preserve position across dark/light changes)
        val restoredModel = savedInstanceState?.getString(KEY_CURRENT_MODEL)
        val restoredQuery = savedInstanceState?.getString(KEY_SEARCH_QUERY).orEmpty()

        if (savedInstanceState == null) {
            // first launch
            rebuildDisplay(resetIndex = true)
        } else {
            // recreate after theme change (or other config change)
            b.searchView.setQuery(restoredQuery, false)
            rebuildDisplay(query = restoredQuery, resetIndex = false)  // DO NOT reset index
            restoredModel?.let {
                val idx = indexOfModel(shownCars, it)
                if (idx != -1) current = idx
            }
            bindCar()
        }

    }

    private fun rebuildDisplay(query: String = "", resetIndex: Boolean = false) {
        val q = query.trim().lowercase()
        shownCars = CarRepository.cars.filter {
            q.isEmpty() || "${it.name} ${it.model}".lowercase().contains(q)
        }
        if (resetIndex) current = 0
        bindCar()
        renderFavChips()
    }

    private fun bindCar() {
        val car = shownCars.getOrNull(current)
        if (car == null) {
            b.imgCar.setImageResource(R.drawable.ic_car_placeholder)
            b.imgCar.imageTintList = ColorStateList.valueOf(Color.GRAY)
            b.tvTitle.text = getString(R.string.no_cars)
            b.ratingBar.rating = 0f
            b.tvMeta.text = ""
            b.btnRent.isEnabled = false
            b.btnFav.isEnabled = false
            return
        }
        b.btnRent.isEnabled = true
        b.btnFav.isEnabled = true

        // Image
        b.imgCar.apply {
            setImageResource(imageFor(car.model))
            imageTintList = null
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(Color.TRANSPARENT)
            contentDescription = getString(R.string.car_image_desc_format, car.year, car.name, car.model)
        }

        // Texts
        b.tvTitle.text = getString(R.string.title_format, car.name, car.model, car.year)
        b.ratingBar.rating = car.rating
        b.tvRating.text = getString(R.string.rating_number, car.rating)

        b.tvMeta.text  = getString(R.string.meta_format, car.kilometers, car.costPerDay)

        // Accent
        val accent = ContextCompat.getColor(this, car.colorTheme)
        b.tvTitle.setTextColor(accent)
        b.btnPrev.imageTintList = ColorStateList.valueOf(accent)
        b.btnNext.imageTintList = ColorStateList.valueOf(accent)
        b.btnRent.backgroundTintList = ColorStateList.valueOf(accent)
        b.btnFav.imageTintList = ColorStateList.valueOf(accent)
        b.btnSort.imageTintList = ColorStateList.valueOf(accent)
        b.tvRating.setTextColor(accent)

        // Fav icon state
        val fav = Session.favourites.contains(car.model)
        b.btnFav.setImageResource(if (fav) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
    }

    private fun renderFavChips() {
        b.chipsFavs.removeAllViews()
        Session.favourites.forEach { favModel ->
            val chip = Chip(this).apply {
                text = favModel
                isCheckable = false
                setOnClickListener {
                    val idx = indexOfModel(shownCars, favModel)
                    if (idx != -1) { current = idx; bindCar() } else { snack(getString(R.string.msg_not_available)) }
                }
            }
            b.chipsFavs.addView(chip)
        }
    }

    private fun indexOfModel(list: List<Car>, model: String): Int {
        for (i in list.indices) {
            val car = list[i]
            if (car.model.equals(model, ignoreCase = true)) return i
        }
        return -1
    }

    private fun imageFor(model: String): Int = when (model.lowercase()) {
        "911 carrera" -> R.drawable.car_911
        "panamera"    -> R.drawable.car_panamera
        "taycan"      -> R.drawable.car_taycan
        "macan"       -> R.drawable.car_macan
        "918 spyder"  -> R.drawable.car_918
        else           -> R.drawable.ic_car_placeholder
    }

    private fun snack(msg: String) = Snackbar.make(b.root, msg, Snackbar.LENGTH_SHORT).show()
}