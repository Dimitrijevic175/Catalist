package com.example.catalist.cats.details

import android.content.*
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.compose.SubcomposeAsyncImage
import coil.compose.rememberImagePainter
import com.example.catalist.R
import com.example.catalist.cats.details.model.CatDetailsUiModel
import com.example.catalist.cats.list.CatListContract
import com.example.catalist.core.compose.AppIconButton


private val topBarContainerColor = Color.Green.copy(alpha = 0.5f)

fun NavGraphBuilder.catDetails(
    route: String,
    navController: NavController,
) = composable(
    route = route,
) {navBackStackEntry ->
    val id = navBackStackEntry.arguments?.getString("id")
        ?: throw IllegalArgumentException("id is required")

    val catDetailsViewModel = viewModel<CatDetailsViewModel>(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(CatDetailsViewModel::class.java)) {
                    return CatDetailsViewModel(cat_id = id) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        },
    )
    val state = catDetailsViewModel.state.collectAsState()

    CatDetailsScreen(
        state = state.value,
        eventPublisher = {catDetailsViewModel.setEvent(it)},
        onBack = {navController.popBackStack()}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatDetailsScreen(
    state: CatDetailsState,
    eventPublisher: (CatListContract.CatDetailsUiEvent) -> Unit,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        AppIconButton(
                            imageVector = Icons.Default.ArrowBack,
                            onClick = onBack,
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = topBarContainerColor,
                        scrolledContainerColor = topBarContainerColor,
                    ),
                    title = {
                        val title = state.data?.name?.let { name ->
                            state.data.alt_names?.split(",")?.firstOrNull()?.let { firstAlternativeName ->
                                if (firstAlternativeName.isNotEmpty()) "$name - $firstAlternativeName" else name
                            } ?: name
                        } ?: ""

                        Text(
                            text = title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Blue.copy(alpha = 0.1f))
            ) {
                if (state.loading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (state.error != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        val errorMessage = when (state.error) {
                            is CatDetailsState.DetailsError.LoadingFailed ->
                                "Failed to load. Error message: ${state.error.cause?.message}."
                        }
                        Text(text = errorMessage)
                    }
                } else if (state.data != null) {
                    CatInformation(paddingValues, state.data, context, state.imageUrl)
                } else {
                    CatNotFoundScreen()
                }
            }
        }
    )
}
@Composable
fun CatInformation(
    paddingValues: PaddingValues,
    data: CatDetailsUiModel,
    context: Context,
    imageUrl: String?
) {
    LazyColumn(
        modifier = Modifier
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
    ) {
        item {
            Column {
                HeaderWithAttributes(data = data, imageUrl = imageUrl, attributes = listOf(CatAttribute.ORIGIN, CatAttribute.LIFE_SPAN, CatAttribute.WEIGHT, CatAttribute.RARE))
                DescriptionSection(data)
                TemperamentSection(data)
                CharacteristicsSection(data)
                WikipediaButton(data, context)
            }
        }
    }
}


@Composable
private fun HeaderWithAttributes(data: CatDetailsUiModel, imageUrl: String?, attributes: List<CatAttribute>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = data.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            SubcomposeAsyncImage(
                model = imageUrl, contentDescription = null, modifier = Modifier.size(128.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f)
        ) {
            attributes.forEach { attribute ->
                when (attribute) {
                    CatAttribute.ORIGIN -> {
                        Text(
                            text = stringResource(R.string.origins) + ": ${data.origin}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    CatAttribute.LIFE_SPAN -> {
                        Text(
                            text = stringResource(R.string.life_span) + ": ${data.life_span} years",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    CatAttribute.WEIGHT -> {
                        Text(
                            text = stringResource(R.string.weight) + ": ${data.weight} kg",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    CatAttribute.RARE -> {
                        val rarity = if (data.rare == 1) "Rare" else "Common"
                        Text(
                            text = stringResource(R.string.rare) +": $rarity",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun TemperamentSection(data: CatDetailsUiModel) {
    Column {
        Text(
            text = stringResource(R.string.temperament),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        val temperaments = data.temperament.split(", ")
        LazyRow {
            items(temperaments) { temperament ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(4.dp)
                        .background(Color(0xFF00008B))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = temperament,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}



@Composable
private fun DescriptionSection(data: CatDetailsUiModel) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.description),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Text(
            text = data.description,
            fontSize = 14.sp
        )
    }
}


@Composable
private fun CharacteristicsSection(data: CatDetailsUiModel) {
    val ratings = listOf(
        Pair(R.string.adaptability, data.adaptability),
        Pair(R.string.affection_level, data.affection_level),
        Pair(R.string.dog_friendly, data.dog_friendly),
        Pair(R.string.energy_level, data.energy_level),
        Pair(R.string.health_issues, data.health_issues)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Characteristics",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .align(Alignment.Start)
        )
        ratings.forEach { (titleResId, rating) ->
            RatingItem(title = stringResource(id = titleResId), rating = rating)
        }
    }
}


@Composable
private fun WikipediaButton(data: CatDetailsUiModel, context: Context) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            modifier = Modifier
                .padding(all = 16.dp)
                .sizeIn(minWidth = 160.dp, minHeight = 56.dp),
            onClick = {
                data.wikipedia_url?.let {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it))
                    context.startActivity(intent)
                }
            }
        ) {
            Text(
                text = "Open Wiki"
            )
        }
    }
}

@Composable
fun RatingItem(title: String, rating: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Row(modifier = Modifier.padding(10.dp)) {
            for (i in 1..5) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (i <= rating) Color.Red else Color.Gray
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}


