package lab.jhrodriguezi.webservice.data

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.QueryMap

data class CovidCase(
    @SerializedName("fecha_reporte_web") val fechaReporteWeb: String?,
    @SerializedName("id_de_caso") val idCaso: Int?,
    @SerializedName("fecha_de_notificaci_n") val fechaNotificacion: String?,
    @SerializedName("departamento") val departamentoCodigo: Int?,
    @SerializedName("departamento_nom") val departamentoNombre: String?,
    @SerializedName("ciudad_municipio") val ciudadCodigo: Int?,
    @SerializedName("ciudad_municipio_nom") val ciudadNombre: String?,
    @SerializedName("edad") val edad: Int?,
    @SerializedName("unidad_medida") val unidadMedida: Int?,
    @SerializedName("sexo") val sexo: String?,
    @SerializedName("fuente_tipo_contagio") val fuenteTipoContagio: String?,
    @SerializedName("ubicacion") val ubicacion: String?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("pais_viajo_1_cod") val paisViajoCodigo: Int?,
    @SerializedName("pais_viajo_1_nom") val paisViajeNombre: String?,
    @SerializedName("recuperado") val recuperado: String?,
    @SerializedName("fecha_inicio_sintomas") val fechaInicioSintomas: String?,
    @SerializedName("fecha_muerte") val fechaMuerte: String?,
    @SerializedName("fecha_diagnostico") val fechaDiagnostico: String?,
    @SerializedName("fecha_recuperado") val fechaRecuperado: String?,
    @SerializedName("tipo_recuperacion") val tipoRecuperacion: String?,
    @SerializedName("per_etn_") val pertenenciaEtnica: Int?,
    @SerializedName("nom_grupo_") val nombreGrupoEtnico: String?
)

// Data class para los parámetros de búsqueda
data class SearchParams(
    var fechaReporteInicio: String = "",
    var fechaReporteFin: String = "",
    var idCaso: Int? = null,
    var departamentoCodigo: Int? = null,
    var departamentoNombre: String = "",
    var ciudadCodigo: Int? = null,
    var ciudadNombre: String = "",
    var edadMin: Int? = null,
    var edadMax: Int? = null,
    var sexo: String = "",
    var tipoContagio: String = "",
    var ubicacion: String = "",
    var estado: String = "",
    var recuperado: String = "",
    var tipoRecuperacion: String = "",
    var pertenenciaEtnica: Int? = null
)

interface CovidApi {
    @GET("gt2j-8ykr.json")
    suspend fun getCovidCases(@QueryMap params: Map<String, String>): List<CovidCase>
}