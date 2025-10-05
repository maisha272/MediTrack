package com.example.porscherentalapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.porscherentalapp.databinding.ActivityBookingsBinding
import com.example.porscherentalapp.databinding.RowBookingBinding
import com.google.android.material.snackbar.Snackbar

class BookingsActivity : AppCompatActivity() {

    private lateinit var b: ActivityBookingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityBookingsBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.toolbar.setNavigationOnClickListener { finish() }
        render()
    }

    private fun render() {
        b.container.removeAllViews()
        val list = Session.bookings
        b.stateEmpty.isVisible = list.isEmpty()

        val infl = LayoutInflater.from(this)
        list.forEachIndexed { idx, booking ->
            val item = RowBookingBinding.inflate(infl, b.container, false)
            val car = booking.car
            item.img.setImageResource(imageFor(car.model))
            item.tvTitle.text = getString(R.string.title_format, car.name, car.model, car.year)
            item.tvMeta.text  = getString(R.string.booking_meta_format, booking.days, booking.total)

            item.btnCancel.setOnClickListener {
                // refund + restore car + remove booking
                Session.balance += booking.total
                if (CarRepository.cars.none { it.model.equals(car.model, true) }) {
                    CarRepository.cars.add(car)
                }
                Session.bookings.removeAt(idx)
                render()
                setResult(RESULT_OK, Intent())    // tell Main to refresh
                Snackbar.make(b.root, R.string.msg_booking_cancelled, Snackbar.LENGTH_LONG).show()
            }
            b.container.addView(item.root)
        }
    }

    private fun imageFor(model: String): Int = when (model.lowercase()) {
        "911 carrera" -> R.drawable.car_911
        "panamera"    -> R.drawable.car_panamera
        "taycan"      -> R.drawable.car_taycan
        "macan"       -> R.drawable.car_macan
        "918 spyder"  -> R.drawable.car_918
        else          -> R.drawable.ic_car_placeholder
    }
}
