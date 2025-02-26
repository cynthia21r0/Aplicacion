package mx.edu.utng.agenda


data class Tarea(
    val id: Int = 0,
    val titulo: String,
    val responsable: String,
    val cantidad: String,
    val descripcion: String,
    val fecha: String,  // Formato: "YYYY-MM-DD"
    val tipo: String,
    val prioridad: String, // Baja, Media, Alta
)
