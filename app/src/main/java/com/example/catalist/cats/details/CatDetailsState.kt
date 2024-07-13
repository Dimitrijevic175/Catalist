package com.example.catalist.cats.details

import com.example.catalist.cats.details.model.CatDetailsUiModel

data class CatDetailsState(
    val id: String,
    val loading: Boolean = false,
    val data: CatDetailsUiModel? = null,
    val imageUrl: String? = null,
    val error: DetailsError ? = null,
 ){

    sealed class DetailsError {
        data class LoadingFailed(val cause: Throwable? = null) : DetailsError()
    }
}
