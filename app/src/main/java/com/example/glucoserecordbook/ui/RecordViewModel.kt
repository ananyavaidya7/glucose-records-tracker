package com.example.glucoserecordbook.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glucoserecordbook.data.Reading
import com.example.glucoserecordbook.data.ReadingRepository
import com.example.glucoserecordbook.data.ReadingStatus
import com.example.glucoserecordbook.data.ReadingType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeState(
    val date: LocalDate = LocalDate.now(),
    val readings: Map<ReadingType, Reading> = emptyMap()
)

data class EntryState(
    val date: LocalDate,
    val type: ReadingType,
    val reading: Reading?,
    val number: String,
    val input: EntryInput = EntryInput.EMPTY,
    val error: String? = null,
    val saving: Boolean = false
)

enum class EntryInput { EMPTY, NUMBER, SKIPPED }

sealed interface Screen { data object Home : Screen; data object History : Screen; data class Entry(val state: EntryState) : Screen }

class RecordViewModel(private val repository: ReadingRepository) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val _screen = MutableStateFlow<Screen>(Screen.Home)
    val screen: StateFlow<Screen> = _screen.asStateFlow()
    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()

    val home: StateFlow<HomeState> = selectedDate.flatMapLatest { date ->
        repository.observeForDate(date).map { readings -> HomeState(date, readings.associateBy { it.type }) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeState())

    val history: StateFlow<Map<LocalDate, List<Reading>>> = repository.observeAll().map { records ->
        records.groupBy { it.date }.toSortedMap(compareByDescending { it })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun showDate(date: LocalDate) {
        if (!date.isAfter(LocalDate.now())) selectedDate.value = date
    }
    fun previousDate() = showDate(selectedDate.value.minusDays(1))
    fun nextDate() = showDate(selectedDate.value.plusDays(1))
    fun today() = showDate(LocalDate.now())
    fun showHistory() { _screen.value = Screen.History }
    fun showHome() { _screen.value = Screen.Home }

    fun beginEntry(type: ReadingType) {
        val existing = home.value.readings[type]
        _screen.value = Screen.Entry(
            EntryState(
                date = selectedDate.value,
                type = type,
                reading = existing,
                number = existing?.value?.toString().orEmpty(),
                input = when (existing?.status) {
                    ReadingStatus.RECORDED -> EntryInput.NUMBER
                    ReadingStatus.SKIPPED -> EntryInput.SKIPPED
                    null -> EntryInput.EMPTY
                }
            )
        )
    }
    fun cancelEntry() { _screen.value = Screen.Home }
    fun appendDigit(digit: Int) = updateEntry { state ->
        if (state.input == EntryInput.NUMBER && state.number.length >= ReadingInput.MAX_DIGITS) state
        else state.copy(number = state.number + digit, input = EntryInput.NUMBER, error = null)
    }
    fun markSkipped() = updateEntry { it.copy(number = "", input = EntryInput.SKIPPED, error = null) }
    fun backspace() = updateEntry {
        when (it.input) {
            EntryInput.SKIPPED -> it.copy(input = EntryInput.EMPTY, error = null)
            EntryInput.NUMBER -> {
                val updated = it.number.dropLast(1)
                it.copy(number = updated, input = if (updated.isEmpty()) EntryInput.EMPTY else EntryInput.NUMBER, error = null)
            }
            EntryInput.EMPTY -> it
        }
    }

    fun saveEntry() {
        val current = _screen.value as? Screen.Entry ?: return
        if (current.state.saving) return
        val error = if (current.state.input == EntryInput.NUMBER) ReadingInput.validationMessage(current.state.number)
        else if (current.state.input == EntryInput.EMPTY) ReadingInput.validationMessage("") else null
        if (error != null) { _screen.value = current.copy(state = current.state.copy(error = error)); return }
        updateEntry { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val state = (screen.value as? Screen.Entry)?.state ?: return@launch
            if (state.input == EntryInput.SKIPPED) repository.saveSkipped(state.date, state.type)
            else repository.save(state.date, state.type, state.number.toInt())
            _screen.value = Screen.Home
            _messages.emit(if (state.input == EntryInput.SKIPPED) "\u2014 saved" else "${state.number} saved")
        }
    }

    fun deleteEntry() {
        val state = (_screen.value as? Screen.Entry)?.state ?: return
        val existing = state.reading ?: return
        viewModelScope.launch {
            repository.delete(existing)
            _screen.value = Screen.Home
            _messages.emit("Reading removed")
        }
    }

    private fun updateEntry(transform: (EntryState) -> EntryState) {
        val current = _screen.value as? Screen.Entry ?: return
        if (!current.state.saving) _screen.value = current.copy(state = transform(current.state))
    }
}
