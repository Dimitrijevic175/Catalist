package com.example.catalist.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

import com.example.catalist.cats.details.catDetails
import com.example.catalist.cats.list.catList

@Composable
fun AppNavigation(){

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "list",
    ){
        catList(
            route = "list",
            navController = navController,
        )
        catDetails(
            route = "details/{id}",
            navController = navController,
        )
    }
}