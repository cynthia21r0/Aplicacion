package mx.edu.utng.agenda

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class EditarTareaActivity : AppCompatActivity() {

    private lateinit var edtTitulo: EditText
    private lateinit var edtResponsable: EditText
    private lateinit var edtCantidad: EditText
    private lateinit var edtDescripcion: EditText
    private lateinit var btnFecha: Button
    private lateinit var spnTipo: Spinner
    private lateinit var spnPrioridad: Spinner
    private lateinit var btnGuardar: Button
    private lateinit var btnEliminar: Button

    private lateinit var dbHelper: DatabaseHelper
    private var tareaId: Int = -1
    private var fechaSeleccionada: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_tarea)

        // Referencias a la UI
        edtTitulo = findViewById(R.id.edtTitulo)
        edtResponsable = findViewById(R.id.edtPersona)
        edtCantidad = findViewById(R.id.edtCantidad)
        edtDescripcion = findViewById(R.id.edtDescripcion)
        btnFecha = findViewById(R.id.btnFecha)
        spnTipo = findViewById(R.id.spnTipo)
        spnPrioridad = findViewById(R.id.spnPrioridad)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnEliminar = findViewById(R.id.btnEliminar) // Asignar botón de eliminar

        dbHelper = DatabaseHelper(this)

        // Configurar Spinner de tipo
        val tipos = arrayOf("Familiar", "Escolar", "Laboral","Entretenimiento")
        spnTipo.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tipos)
        // Configurar Spinner de prioridad
        val prioridades = arrayOf("Baja", "Media", "Alta")
        spnPrioridad.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, prioridades)

        // Revisar si estamos editando un gasto existente
        tareaId = intent.getIntExtra("TAREA_ID", -1)
        if (tareaId != -1) {
            cargarTarea()
        }

        // Botón para seleccionar fecha
        btnFecha.setOnClickListener { mostrarSelectorFecha() }

        // Botón para guardar gasto
        btnGuardar.setOnClickListener { guardarTarea() }

        // Botón para eliminar gasto
        btnEliminar.setOnClickListener { eliminarTarea() }
    }

    private fun cargarTarea() {
        val tarea = dbHelper.obtenerTareaPorId(tareaId)
        tarea?.let {
            edtTitulo.setText(it.titulo)
            edtResponsable.setText(it.responsable)
            edtCantidad.setText(it.cantidad)
            edtDescripcion.setText(it.descripcion)
            btnFecha.text = it.fecha
            fechaSeleccionada = it.fecha
            spnTipo.setSelection(
                when (it.tipo) {
                    "Familiar" -> 0
                    "Escolar" -> 1
                    "Laboral" -> 2
                    "Entretenimiento" -> 3
                    else -> 0
                }
            )
            spnPrioridad.setSelection(
                when (it.prioridad) {
                    "Baja" -> 0
                    "Media" -> 1
                    "Alta" -> 2
                    else -> 0
                }
            )
        }
    }

    private fun mostrarSelectorFecha() {
        val calendario = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                fechaSeleccionada = "$day/${month + 1}/$year"
                btnFecha.text = fechaSeleccionada
            },
            calendario.get(Calendar.YEAR),
            calendario.get(Calendar.MONTH),
            calendario.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun guardarTarea() {
        val titulo = edtTitulo.text.toString().trim()
        val responsable = edtResponsable.text.toString().trim()  // Corregido
        val cantidad = edtCantidad.text.toString().trim()  // Corregido
        val descripcion = edtDescripcion.text.toString().trim()
        val tipo = spnTipo.selectedItem.toString()
        val prioridad = spnPrioridad.selectedItem.toString()

        if (titulo.isEmpty() || descripcion.isEmpty() || fechaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val tarea = Tarea(
            tareaId,
            titulo,
            responsable,
            cantidad,
            descripcion,
            fechaSeleccionada,
            tipo,
            prioridad
        )

        if (tareaId == -1) {
            tareaId = dbHelper.insertarTarea(tarea).toInt()
            programarNotificacion(tarea.copy(id = tareaId))
            Toast.makeText(this, "Agregado", Toast.LENGTH_SHORT).show()
        } else {
            dbHelper.actualizarTarea(tarea)
            programarNotificacion(tarea)
            Toast.makeText(this, "Actualizado", Toast.LENGTH_SHORT).show()
        }

        finish()
    }


    private fun eliminarTarea() {
        if (tareaId != -1) {
            cancelarNotificacion(tareaId) // Cancelar la notificación antes de eliminar el gasto
            dbHelper.eliminarTarea(tareaId, this) // Llamar a la función de eliminar
            Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
            finish() // Cerrar la actividad después de eliminar
        }
    }

    private fun cancelarNotificacion(tareaId: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, TareaReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, tareaId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun programarNotificacion(tarea: Tarea) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Verificar si el usuario ha dado permiso para alarmas exactas (Android 12+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Enviar al usuario a la configuración de la app para activar el permiso
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)

                Toast.makeText(
                    this,
                    "Habilita el permiso de alarmas exactas en Configuración",
                    Toast.LENGTH_LONG
                ).show()
                return
            }
        }

        val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaHora = formatoFecha.parse("${tarea.fecha}")

        if (fechaHora != null) {
            val calendar = Calendar.getInstance()
            calendar.time = fechaHora

            val intent = Intent(this, TareaReceiver::class.java).apply {
                putExtra("TAREA_ID", tarea.id)
                putExtra("TITULO", tarea.titulo)
                putExtra("DESCRIPCION", tarea.descripcion)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                this, tarea.id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        }
    }
}