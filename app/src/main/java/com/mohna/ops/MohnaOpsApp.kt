package com.mohna.ops

import android.app.Application
import com.mohna.ops.data.repository.MohnaRepository

class MohnaOpsApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize repository and load initial cached seed data
        MohnaRepository.instance
    }
}
