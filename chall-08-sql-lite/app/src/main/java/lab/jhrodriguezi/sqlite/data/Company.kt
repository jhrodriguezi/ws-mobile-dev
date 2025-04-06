package lab.jhrodriguezi.sqlite.data

data class Company(
    val id: Int = 0,
    val name: String,
    val website: String,
    val phone: String,
    val email: String,
    val products: String,
    val classification: String
)