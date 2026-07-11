package com.example.glucoserecordbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.glucoserecordbook.data.RecordDatabase
import com.example.glucoserecordbook.data.ReadingRepository
import com.example.glucoserecordbook.ui.RecordApp
import com.example.glucoserecordbook.ui.RecordViewModel
import com.example.glucoserecordbook.ui.RecordViewModelFactory
import com.example.glucoserecordbook.ui.theme.GlucoseRecordBookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = ReadingRepository(RecordDatabase.create(this))
        setContent {
            GlucoseRecordBookTheme {
                Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
                    val model: RecordViewModel = viewModel(factory = RecordViewModelFactory(repository))
                    RecordApp(model)
                }
            }
        }
    }
}
