package com.example.audius.shared.viewmodel

import android.content.Context
import com.example.audius.datalayer.Repository
import com.example.audius.viewmodel.AudiusViewModel
import com.squareup.sqldelight.android.AndroidSqliteDriver
import myLocal.db.LocalDb

fun AudiusViewModel.Factory.getAndroidInstance(context : Context) : AudiusViewModel {
    val sqlDriver = AndroidSqliteDriver(LocalDb.Schema, context, "Local.db")
    val repository = Repository(sqlDriver)
    return AudiusViewModel(repository)
}