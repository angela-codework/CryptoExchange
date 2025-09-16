package com.demo.cryptoexchange.presentation.navigation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.demo.cryptoexchange.presentation.navigation.model.RouteScreen
import com.demo.cryptoexchange.presentation.navigation.theme.AppNavigationItemColors

@Composable
fun AppBottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        RouteScreen.getBottomNavItems().forEach { item ->
            NavigationBarItem(
                modifier = modifier,
                icon = {},
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                selected = currentDestination?.hierarchy?.any {
                    it.route == item.route
                } == true,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                },
                colors = AppNavigationItemColors.default()
            )
        }
    }
}