package com.example.justlisten.shared.viewmodel

import android.content.Context
import com.example.justlisten.datalayer.Repository
import com.example.justlisten.viewmodel.JustListenViewModel
import com.squareup.sqldelight.android.AndroidSqliteDriver
import myLocal.db.LocalDb

fun JustListenViewModel.Factory.getAndroidInstance(context : Context) : JustListenViewModel {
    val sqlDriver = AndroidSqliteDriver(LocalDb.Schema, context, "Local.db")
    val repository = Repository(sqlDriver)
    return JustListenViewModel(repository)
}