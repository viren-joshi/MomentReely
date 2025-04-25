package com.example.momentreely

import android.app.Application
import com.example.momentreely.data.AppContainer

class MomentReelyApplication: Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }

}