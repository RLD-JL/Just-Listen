package com.rld.justlisten.shared.viewmodel

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.viewmodel.JustListenViewModel

fun JustListenViewModel.Factory.getAndroidInstance(context : Context) : JustListenViewModel {
    val sqlDriver = AndroidSqliteDriver(LocalDb.Schema, context, "Local.db")
    val repository = Repository(sqlDriver)
    return JustListenViewModel(repository)
}