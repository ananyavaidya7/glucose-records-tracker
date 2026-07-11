package com.example.glucoserecordbook.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.glucoserecordbook.data.ReadingRepository

class RecordViewModelFactory(private val repository: ReadingRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = RecordViewModel(repository) as T
}
