package lab.jhrodriguezi.webservice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import lab.jhrodriguezi.webservice.data.CovidCase
import lab.jhrodriguezi.webservice.data.SearchParams
import lab.jhrodriguezi.webservice.ui.CovidViewModel
import lab.jhrodriguezi.webservice.ui.theme.WebServiceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val covidVM = remember { CovidViewModel() }
            WebServiceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        CovidScreen(viewModel = covidVM)
                    }
                }
            }
        }
    }
}

@Composable
fun DateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text("YYYY-MM-DD") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchForm(
    searchParams: SearchParams,
    onParamChange: (SearchParams) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Campos de fecha
        DateField(
            value = searchParams.fechaReporteInicio,
            onValueChange = { onParamChange(searchParams.copy(fechaReporteInicio = it)) },
            label = "Fecha Inicio"
        )

        DateField(
            value = searchParams.fechaReporteFin,
            onValueChange = { onParamChange(searchParams.copy(fechaReporteFin = it)) },
            label = "Fecha Fin"
        )

        // ID de caso
        OutlinedTextField(
            value = searchParams.idCaso?.toString() ?: "",
            onValueChange = {
                onParamChange(searchParams.copy(idCaso = it.toIntOrNull()))
            },
            label = { Text("ID de Caso") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // Dropdown para sexo
//        ExposedDropdownMenuBox(
//            expanded = expanded,
//            onExpandedChange = { expanded = it }
//        ) {
//            OutlinedTextField(
//                value = searchParams.sexo,
//                onValueChange = {},
//                readOnly = true,
//                label = { Text("Sexo") },
//                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            ExposedDropdownMenu(
//                expanded = expanded,
//                onDismissRequest = { expanded = false }
//            ) {
//                listOf("M", "F").forEach { option ->
//                    DropdownMenuItem(
//                        onClick = {
//                            onParamChange(searchParams.copy(sexo = option))
//                            expanded = false
//                        },
//                        text = { Text(text = option) },
//                        modifier = Modifier.fillMaxWidth(),
//                    )
//                }
//            }
//        }

        Column(modifier = Modifier.padding(top = 8.dp).fillMaxWidth()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchParams.sexo.isBlank()) "Seleccionar Sexo" else searchParams.sexo,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (searchParams.sexo.isBlank())
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = if (expanded)
                            Icons.Filled.KeyboardArrowUp
                        else
                            Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Toggle dropdown",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    listOf("M", "F").forEach { classification ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onParamChange(searchParams.copy(sexo = classification))
                                    expanded = false
                                }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = classification,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (classification == searchParams.sexo) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Estado y Recuperado
        OutlinedTextField(
            value = searchParams.estado,
            onValueChange = { onParamChange(searchParams.copy(estado = it)) },
            label = { Text("Estado") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = searchParams.recuperado,
            onValueChange = { onParamChange(searchParams.copy(recuperado = it)) },
            label = { Text("Recuperado") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Buscar")
        }
    }
}

@Composable
fun CaseCard(case: CovidCase) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("ID: ${case.idCaso}", style = MaterialTheme.typography.headlineMedium)
            Text("Fecha reporte: ${case.fechaReporteWeb}")
            Text("${case.ciudadNombre}, ${case.departamentoNombre}")
            Text("Edad: ${case.edad} | Sexo: ${case.sexo}")
            Text("Estado: ${case.estado}")
            Text("Recuperado: ${case.recuperado}")
            Text("Tipo contagio: ${case.fuenteTipoContagio}")
        }
    }
}

@Composable
fun CovidScreen(viewModel: CovidViewModel) {
    val cases by viewModel.cases.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchParams by viewModel.searchParams.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "COVID-19 Colombia",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SearchForm(
            searchParams = searchParams,
            onParamChange = { viewModel.updateSearchParams(it) },
            onSearch = { viewModel.searchCases() },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        LazyColumn {
            items(cases) { case ->
                CaseCard(case = case)
            }
        }
    }
}