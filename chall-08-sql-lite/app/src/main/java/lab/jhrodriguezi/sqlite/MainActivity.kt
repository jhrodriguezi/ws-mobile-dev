package lab.jhrodriguezi.sqlite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import lab.jhrodriguezi.sqlite.data.DatabaseHelper
import lab.jhrodriguezi.sqlite.ui.AddEditCompanyScreen
import lab.jhrodriguezi.sqlite.ui.CompanyListScreen
import lab.jhrodriguezi.sqlite.ui.CompanyViewModel
import lab.jhrodriguezi.sqlite.ui.theme.SqliteTheme


class MainActivity : ComponentActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(this)

        setContent {
            SqliteTheme {
                val viewModel = remember { CompanyViewModel(dbHelper) }
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "companies") {
                    composable("companies") {
                        CompanyListScreen(
                            navController = navController,
                            viewModel = viewModel
                        )
                    }
                    composable("add_company") {
                        AddEditCompanyScreen(
                            navController = navController,
                            viewModel = viewModel
                        )
                    }
                    composable(
                        "edit_company/{companyId}",
                        arguments = listOf(navArgument("companyId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        AddEditCompanyScreen(
                            navController = navController,
                            viewModel = viewModel,
                            companyId = backStackEntry.arguments?.getInt("companyId")
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        dbHelper.close()
        super.onDestroy()
    }
}