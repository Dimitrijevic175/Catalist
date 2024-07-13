package com.example.catalist.cats.list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catalist.cats.api.model.CatApiModel
import com.example.catalist.cats.list.model.CatListUiModel
import com.example.catalist.cats.repository.CatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

class CatListViewModel (
    private val repository: CatRepository = CatRepository
): ViewModel(){

    private val _state = MutableStateFlow(CatListContract.CatListState())
    val state = _state.asStateFlow()

    private fun setState(reducer: CatListContract.CatListState.() -> CatListContract.CatListState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<CatListContract.CatListUiEvent>()

    fun setEvent(event: CatListContract.CatListUiEvent) {
        viewModelScope.launch {
            events.emit(event)
        }
    }

    init {
        observeEvents()
        fetchAllCats()
        observeSearchQuery()
    }

    private fun fetchAllCats(query: String = "") {
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                val cats = withContext(Dispatchers.IO) {
                    if (query.isEmpty()) {
                        repository.fetchAllCats()
                    } else {
                        repository.fetchCatsByQuery(query)
                    }.map { it.asCatListUiModel() }
                }
                setState { copy(cats = cats) }
            } catch (error: IOException) {
                setState { copy(error = CatListContract.CatListState.ListError.ListUpdateFailed(cause = error)) }
            } finally {

                setState { copy(loading = false) }
            }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    CatListContract.CatListUiEvent.ClearSearch -> {
                        setState { copy(query = "") }
                        fetchAllCats()
                    }
                    CatListContract.CatListUiEvent.CloseSearchMode -> {
                        setState { copy(isSearchMode = false) }
                        fetchAllCats()
                    }

                    is CatListContract.CatListUiEvent.SearchQueryChanged -> {
                        setState { copy(query = it.query) }
                        updateFilteredCats()
                    }
                    is CatListContract.CatListUiEvent.Error -> {
                        setState {
                            copy(loading = false)
                        }
                    }
                    is CatListContract.CatListUiEvent.Dummy -> Unit
                    is CatListContract.CatListUiEvent.GetBreedsDetails -> TODO()
                }
            }
        }
    }

    private fun updateFilteredCats() {
        val filtered = if (state.value.query.isBlank()) {
            state.value.cats // Ako je pretraga prazna, prikaži sve
        } else {
            state.value.cats.filter {
                it.name.contains(state.value.query, ignoreCase = true)
            }
        }
        setState { copy(filteredCats = filtered) }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        viewModelScope.launch {
            events
                .filterIsInstance<CatListContract.CatListUiEvent.SearchQueryChanged>()
                .debounce(1.seconds)
                .collect {
                    fetchAllCats(it.query)
                }
        }
    }

    private fun CatApiModel.asCatListUiModel() = CatListUiModel(
        id = this.id,
        name = this.name,
        alt_names = this.alt_names,
        description = this.description,
        temperament = this.temperament,
    )

}

