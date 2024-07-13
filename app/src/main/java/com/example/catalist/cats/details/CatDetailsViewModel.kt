package com.example.catalist.cats.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catalist.cats.api.model.CatApiModel
import com.example.catalist.cats.details.model.CatDetailsUiModel
import com.example.catalist.cats.list.CatListContract
import com.example.catalist.cats.repository.CatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CatDetailsViewModel(
    private val cat_id: String,
    private val repository: CatRepository = CatRepository
): ViewModel(){

    private val _state = MutableStateFlow(CatDetailsState(id = cat_id))
    val state = _state.asStateFlow()

    private fun setState(reducer: CatDetailsState.() -> CatDetailsState) =
        _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<CatListContract.CatDetailsUiEvent>()

    fun setEvent(event: CatListContract.CatDetailsUiEvent) {
        viewModelScope.launch {
            events.emit(event)
        }
    }

    init {
        observeEvents()
        fetchCatDetails()
    }

    private fun fetchCatPhotos(cat: CatApiModel) {
        viewModelScope.launch {
            try{
                val photoUrl = withContext(Dispatchers.IO){
                    cat.reference_image_id?.let { repository.getCatPhoto(cat_id = it) }
                }
                if(photoUrl?.isEmpty() == false){
                    val photo = photoUrl.first()
                    _state.value = _state.value.copy(imageUrl = photo.url)
                }
            }catch (error: Exception) {
                setState {
                    copy(error = CatDetailsState.DetailsError.LoadingFailed(cause = error))
                }
            }

        }
    }

    private fun fetchCatDetails() {
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                val cat = withContext(Dispatchers.IO) {
                    repository.getCatById(id = cat_id)
                }
                fetchCatPhotos(cat)
                setState { copy(data = cat?.asCatDetailsUiModel()) }
            } catch (error: Exception) {
                setState {
                    copy(error = CatDetailsState.DetailsError.LoadingFailed(cause = error))
                }
            } finally {
                setState { copy(loading = false) }
            }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is CatListContract.CatDetailsUiEvent.RequestCatDetails -> {

                    }
                }
            }
        }
    }


    private fun CatApiModel.asCatDetailsUiModel() = CatDetailsUiModel(
        id = this.id,
        name=this.name,
        temperament=this.temperament,
        origin=this.origin,
        description=this.description,
        life_span=this.life_span,
        alt_names=this.alt_names,
        adaptability=this.adaptability,
        affection_level=this.affection_level,
        stranger_friendly=this.stranger_friendly,
        dog_friendly=this.dog_friendly,
        energy_level=this.energy_level,
        social_needs=this.social_needs,
        health_issues=this.health_issues,
        intelligence=this.intelligence,
        rare = this.rare,
        wikipedia_url= this.wikipedia_url,
        reference_image_id = this.reference_image_id,
        weight=this.weight.metric
    )
}