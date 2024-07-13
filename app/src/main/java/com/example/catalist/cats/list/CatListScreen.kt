package com.example.catalist.cats.list

import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.catalist.R
import com.example.catalist.cats.list.model.CatListUiModel


private val topBarContainerColor = Color.Green.copy(alpha = 0.5f)

fun NavGraphBuilder.catList(
    route: String,
    navController: NavController,
) = composable(
    route = route,
) { navBackStackEntry ->
    val catListViewModel = viewModel<CatListViewModel>()
    val state by catListViewModel.state.collectAsState()

    CatListScreen(
        state = state,
        eventPublisher = { catListViewModel.setEvent(it) },
        onItemClick = { cat ->
            navController.navigate(route = "details/${cat.id}")
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatListScreen(
    state: CatListContract.CatListState,
    eventPublisher: (uiEvent: CatListContract.CatListUiEvent) -> Unit,
    onItemClick: (CatListUiModel) -> Unit
) {
    // za pracenje teksta u polju
    var searchText by rememberSaveable { mutableStateOf("") }

    val filteredCats = if (state.query.isBlank()) state.cats else state.filteredCats


    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = topBarContainerColor,
                        scrolledContainerColor =  topBarContainerColor,
                    ),
                    title = {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            fontSize = 20.sp,
                        )
                    },
                )
                Divider()
            }
        },
        content = {paddingValues ->
            if (state.loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()
                ) {
                    TextField(
                        value = searchText,
                        onValueChange = {
                            searchText = it
                            if (it.isNotEmpty()) {
                                eventPublisher(CatListContract.CatListUiEvent.SearchQueryChanged(it))
                            } else {
                                eventPublisher(CatListContract.CatListUiEvent.ClearSearch)
                            }
                        },
                        label = { Text("Search") },
                        leadingIcon = { Icon(Icons.Filled.Search, "Search") },
                        trailingIcon = {
                            if (searchText.isNotEmpty()) {
                                IconButton({ searchText = ""; eventPublisher(CatListContract.CatListUiEvent.ClearSearch) }) {
                                    Icon(Icons.Filled.Close, "Clear search")
                                }
                            }
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 8.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    CatList(
                        paddingValues = paddingValues,
                        filteredItems = filteredCats,
                        onItemClick = onItemClick
                    )
                }
            }
        }
    )
}



@Composable
private fun CatList(
    filteredItems: List<CatListUiModel>,
    paddingValues: PaddingValues,
    onItemClick: (CatListUiModel) -> Unit
){
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ){
        items(filteredItems, key = {it.id}){
            CatListItem(
                catBreed = it,
                onItemClick = {onItemClick(it)}
            )
        }
    }
}

@Composable
private fun CatListItem(
    catBreed: CatListUiModel,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onItemClick),
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(Color.LightGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = catBreed.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            if (!catBreed.alt_names.isNullOrEmpty()) {
                Text(
                    text = "(${catBreed.alt_names})",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            val temperaments = catBreed.temperament.split(", ").take(3)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                temperaments.forEach { temperament ->
                    SuggestionChip(
                        onClick = {  },
                        label = { Text(text = temperament, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier.padding(3.dp).sizeIn(minWidth = 48.dp, minHeight = 24.dp)
                    )
                }
            }

            Text(
                text = if (catBreed.description.isNotBlank() && catBreed.description.length > 250)
                    "${catBreed.description.take(250)}..."
                else
                    catBreed.description,
                fontSize = 16.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(vertical = 8.dp)
            )

        }
    }
}



