package com.localvoicejournal.mobile.car

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

class JournalCarSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen {
        return JournalCarScreen(carContext)
    }
}
