package com.rld.justlisten.shared.viewmodel

import android.content.Context
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.viewmodel.JustListenViewModel
import com.squareup.sqldelight.android.AndroidSqliteDriver
import myLocal.db.LocalDb

fun JustListenViewModel.Factory.getAndroidInstance(context : Context) : JustListenViewModel {
    val sqlDriver = AndroidSqliteDriver(LocalDb.Schema, context, "Local.db")
    val repository = Repository(sqlDriver)
    return JustListenViewModel(repository)
}