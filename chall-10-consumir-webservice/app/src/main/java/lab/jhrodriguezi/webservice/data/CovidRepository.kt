package lab.jhrodriguezi.webservice.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CovidRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://www.datos.gov.co/resource/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(CovidApi::class.java)

    suspend fun getCovidCases(params: SearchParams): List<CovidCase> {
        return withContext(Dispatchers.IO) {
            try {
                val queryParams = mutableMapOf<String, String>()
                val whereConditions = buildWhereClause(params)

                if (whereConditions.isNotEmpty()) {
                    queryParams["\$where"] = whereConditions
                }

                queryParams["\$limit"] = "1000"
                queryParams["\$order"] = "fecha_reporte_web DESC"

                api.getCovidCases(queryParams)
            } catch (e: Exception) {
                println("Error: ${e.message}")
                emptyList()
            }
        }
    }

    private fun buildWhereClause(params: SearchParams): String {
        val conditions = mutableListOf<String>()

        // Filtros por fecha
        if (params.fechaReporteInicio.isNotEmpty() && params.fechaReporteFin.isNotEmpty()) {
            conditions.add("fecha_reporte_web between '${params.fechaReporteInicio}T00:00:00.000' and '${params.fechaReporteFin}T23:59:59.999'")
        }

        // Filtro por ID
        params.idCaso?.let { conditions.add("id_de_caso = $it") }

        // Filtros por ubicación
        params.departamentoCodigo?.let { conditions.add("departamento = $it") }
        if (params.departamentoNombre.isNotEmpty()) {
            conditions.add("departamento_nom like '%${params.departamentoNombre}%'")
        }

        params.ciudadCodigo?.let { conditions.add("ciudad_municipio = $it") }
        if (params.ciudadNombre.isNotEmpty()) {
            conditions.add("ciudad_municipio_nom like '%${params.ciudadNombre}%'")
        }

        // Filtros por edad
        params.edadMin?.let { min ->
            params.edadMax?.let { max ->
                conditions.add("edad between $min and $max")
            }
        }

        // Otros filtros
        if (params.sexo.isNotEmpty()) {
            conditions.add("sexo = '${params.sexo}'")
        }

        if (params.tipoContagio.isNotEmpty()) {
            conditions.add("fuente_tipo_contagio like '%${params.tipoContagio}%'")
        }

        if (params.ubicacion.isNotEmpty()) {
            conditions.add("ubicacion like '%${params.ubicacion}%'")
        }

        if (params.estado.isNotEmpty()) {
            conditions.add("estado = '${params.estado}'")
        }

        if (params.recuperado.isNotEmpty()) {
            conditions.add("recuperado = '${params.recuperado}'")
        }

        if (params.tipoRecuperacion.isNotEmpty()) {
            conditions.add("tipo_recuperacion like '%${params.tipoRecuperacion}%'")
        }

        params.pertenenciaEtnica?.let {
            conditions.add("per_etn_ = $it")
        }

        return conditions.joinToString(" AND ")
    }
}