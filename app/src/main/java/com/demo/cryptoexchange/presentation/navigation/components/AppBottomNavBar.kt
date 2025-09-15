package com.demo.cryptoexchange.presentation.navigation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.demo.cryptoexchange.presentation.navigation.constants.BottomNavBarItems
import com.demo.cryptoexchange.presentation.navigation.extensions.navigateToBottomNavDestination
import com.demo.cryptoexchange.presentation.navigation.theme.AppNavigationItemColors

@Composable
fun AppBottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        BottomNavBarItems.items.forEach { item ->
            NavigationBarItem(
                modifier = modifier,
                icon = {},
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                selected = currentDestination?.hierarchy?.any {
                    it.route == item.route
                } == true,
                onClick = {
                    navController.navigateToBottomNavDestination(item.route)
                },
                colors = AppNavigationItemColors.default()
            )
        }
    }
}