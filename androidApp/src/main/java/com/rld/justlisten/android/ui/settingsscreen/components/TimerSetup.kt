package com.rld.justlisten.android.ui.settingsscreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.rld.justlisten.android.workers.SleepWorker
import com.rld.justlisten.util.delay
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.SnapOffsets
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimerSetup(
    onConfirmClicked: (String, String) -> Unit,
    coroutineScope: CoroutineScope,
    scaffoldState: BottomSheetScaffoldState
) {
    val hours =
        (0..23).map { number -> if (number < 10) "0$number" else "$number" }.toList()
    val minutes =
        (0..59).map { number -> if (number < 10) "0$number" else "$number" }.toList()
    val hourListState = rememberLazyListState(Int.MAX_VALUE / 2)
    val minutesListState = rememberLazyListState(Int.MAX_VALUE / 2)

    Column {
        val context = LocalContext.current
        val workManager = WorkManager.getInstance(context)

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Button(
                onClick = {
                    val closeTimeHour =
                        hours[(hourListState.firstVisibleItemIndex + 2) % hours.size]
                    val closeTimeMinute =
                        minutes[(minutesListState.firstVisibleItemIndex + 2) % minutes.size]
                    onConfirmClicked(closeTimeHour, closeTimeMinute)
                    val rightNow = Calendar.getInstance()
                    val currentHour: Int = rightNow.get(Calendar.HOUR_OF_DAY)
                    val currentMinute: Int = rightNow.get(Calendar.MINUTE)
                    val delay = delay(
                        currentHour,
                        closeTimeHour.toInt(),
                        currentMinute,
                        closeTimeMinute.toInt()
                    )
                    val myWorkRequest = OneTimeWorkRequestBuilder<SleepWorker>()
                        .setInitialDelay(delay, TimeUnit.MINUTES)
                        .build()
                    workManager.beginUniqueWork(
                        "SleepWorker",
                        ExistingWorkPolicy.REPLACE,
                        myWorkRequest
                    ).enqueue()
                    coroutineScope.launch { scaffoldState.bottomSheetState.collapse() }

                },
                modifier = Modifier
                    .weight(0.45f)
                    .clip(CircleShape)
            ) {
                Text("Confirm")
            }
            Spacer(modifier = Modifier.weight(0.1f))
            Button(
                onClick = { coroutineScope.launch { scaffoldState.bottomSheetState.collapse() } },
                modifier = Modifier
                    .weight(0.45f)
                    .clip(
                        CircleShape
                    )
            ) {
                Text("Cancel")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp)
        ) {
            CircularList(
                hours,
                modifier = Modifier.weight(0.45f),
                isEndless = true,
                alignment = Alignment.End,
                hourListState
            )
            Spacer(modifier = Modifier.weight(0.1f))
            CircularList(
                minutes,
                modifier = Modifier.weight(0.45f),
                isEndless = true,
                alignment = Alignment.Start,
                minutesListState
            )
        }
    }
}

@OptIn(ExperimentalSnapperApi::class)
@Composable
fun CircularList(
    items: List<String>,
    modifier: Modifier = Modifier,
    isEndless: Boolean = false,
    alignment: Alignment.Horizontal,
    listState: LazyListState
) {

    val contentPadding = PaddingValues(2.dp)
    LazyColumn(
        state = listState,
        modifier = modifier,
        horizontalAlignment = alignment,
        flingBehavior = rememberSnapperFlingBehavior(
            lazyListState = listState,
            snapOffsetForItem = SnapOffsets.Start,
            endContentPadding = contentPadding.calculateBottomPadding(),
        ),
    ) {
        items(
            count = if (isEndless) Int.MAX_VALUE else items.size,
            itemContent = {
                val index = it % items.size
                Text(text = items[index], modifier = Modifier.padding(1.dp))
            }
        )
    }

}