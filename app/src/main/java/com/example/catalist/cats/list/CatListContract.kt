package com.example.catalist.cats.list

import com.example.catalist.cats.list.model.CatListUiModel

interface CatListContract {

    data class CatListState(
        val loading:Boolean=false,
        val cats :List<CatListUiModel> = emptyList(),
        val error: ListError ?=null,
        val query: String = "",
        val isSearchMode: Boolean = false,
        val filteredCats: List<CatListUiModel> = emptyList(),
    ) {

        sealed class ListError {
            data class ListUpdateFailed(val cause: Throwable? = null) : ListError()
        }
    }

    sealed class CatListUiEvent{
        data class SearchQueryChanged(val query: String) : CatListUiEvent()
        data class GetBreedsDetails(val breedId: String) : CatListUiEvent()
        data object ClearSearch : CatListUiEvent()
        data object CloseSearchMode : CatListUiEvent()
        data object Error : CatListUiEvent()
        data object Dummy : CatListUiEvent()
    }

    sealed class CatDetailsUiEvent {
        data class RequestCatDetails(val catId: String) : CatDetailsUiEvent()
    }
}