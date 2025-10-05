package com.example.porscherentalapp

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.porscherentalapp.databinding.ActivityRentBinding
import com.google.android.material.snackbar.Snackbar
import android.content.Intent


class RentActivity : AppCompatActivity() {

    private lateinit var b: ActivityRentBinding
    private lateinit var car: Car
    private lateinit var modelKey: String

    private var days = 1
    private var total = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityRentBinding.inflate(layoutInflater)
        setContentView(b.root)

        car = intent.getParcelableExtra("car")!!
        modelKey = intent.getStringExtra("modelKey")!!

        bind()

        b.sliderDays.addOnChangeListener { _, v, _ ->
            days = v.toInt(); updateTotal()
        }

        b.btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED, Intent().putExtra("cancel_car", car))
            finish()
        }


        b.btnSave.setOnClickListener {
            if (total > 400) { Snackbar.make(b.root, R.string.err_over_400, Snackbar.LENGTH_LONG).show(); return@setOnClickListener }
            if (Session.balance < total) { Snackbar.make(b.root, R.string.err_insufficient, Snackbar.LENGTH_LONG).show(); return@setOnClickListener }

            // Apply booking here (as before)
            Session.balance -= total
            CarRepository.cars.removeIf { it.model.equals(modelKey, true) }

            // Return data so MainActivity can UNDO
            val data = Intent().apply {
                putExtra("booked_car", car)
                putExtra("charged", total)
            }
            setResult(RESULT_OK, data)
            finish()
        }

    }

    private fun bind() {
        b.tvTitleRent.text = getString(R.string.title_format, car.name, car.model, car.year)
        b.ratingBarRent.rating = car.rating
        b.tvRatingRent.text = getString(R.string.rating_number, car.rating)

        b.tvMetaRent.text = getString(R.string.meta_rent_format, car.kilometers, car.costPerDay, Session.balance)

        val accent = ContextCompat.getColor(this, car.colorTheme)
        b.tvTitleRent.setTextColor(accent)
        b.tvRatingRent.setTextColor(accent)
        b.btnSave.backgroundTintList = ColorStateList.valueOf(accent)

        b.imgCarRent.apply {
            setImageResource(imageFor(car.model))
            imageTintList = null
            setBackgroundColor(Color.TRANSPARENT)
            scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
            contentDescription = getString(R.string.car_image_desc_format, car.year, car.name, car.model)
        }
        updateTotal()
    }

    private fun imageFor(model: String): Int = when (model.lowercase()) {
        "911 carrera" -> R.drawable.car_911
        "panamera"    -> R.drawable.car_panamera
        "taycan"      -> R.drawable.car_taycan
        "macan"       -> R.drawable.car_macan
        "918 spyder"  -> R.drawable.car_918
        else           -> R.drawable.ic_car_placeholder
    }

    private fun updateTotal() {
        total = car.costPerDay * days
        b.tvTotal.text = getString(R.string.total_format, total, days)
    }
}