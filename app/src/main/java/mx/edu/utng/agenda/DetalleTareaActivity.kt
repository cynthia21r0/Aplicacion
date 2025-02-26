package mx.edu.utng.agenda


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class DetalleTareaActivity : AppCompatActivity() {

    private lateinit var txtTitulo: TextView
    private lateinit var txtPersona: TextView
    private lateinit var txtCantidad: TextView
    private lateinit var txtDescripcion: TextView
    private lateinit var txtFechaHora: TextView
    private lateinit var txtTipo: TextView
    private lateinit var txtPrioridad: TextView
    private lateinit var btnEditar: Button
    private lateinit var btnEliminar: Button

    private lateinit var dbHelper: DatabaseHelper
    private var tareaId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_tarea)

        // Referencias a los elementos de la UI
        txtTitulo = findViewById(R.id.txtTitulo)
        txtPersona =findViewById(R.id.txtPersona)
        txtCantidad =findViewById(R.id.txtCantidad)
        txtDescripcion = findViewById(R.id.txtDescripcion)
        txtFechaHora = findViewById(R.id.txtFechaH)
        txtTipo =findViewById(R.id.txtTipo)
        txtPrioridad = findViewById(R.id.txtPrioridad)
        btnEditar = findViewById(R.id.btnEditar)
        btnEliminar = findViewById(R.id.btnEliminar)

        dbHelper = DatabaseHelper(this)

        // Obtener el ID de la tarea desde el Intent
        tareaId = intent.getIntExtra("TAREA_ID", -1)

        if (tareaId != -1) {
            cargarTarea()
        }

        // Botón para editar
        btnEditar.setOnClickListener {
            val intent = Intent(this, EditarTareaActivity::class.java)
            intent.putExtra("TAREA_ID", tareaId)
            startActivity(intent)
        }

        // Botón para eliminar con confirmación
        btnEliminar.setOnClickListener {
            mostrarDialogoEliminar()
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


    override fun onResume() {
        super.onResume()
        if (tareaId != -1) {
            cargarTarea() // Recargar datos si volvemos de editar
        }
    }

    private fun cargarTarea() {
        val tarea = dbHelper.obtenerTareaPorId(tareaId)
        tarea?.let {
            txtTitulo.text = it.titulo
            txtPersona.text = it.responsable
            txtCantidad.text = it.cantidad
            txtDescripcion.text = it.descripcion
            txtFechaHora.text = "${it.fecha}"
            txtTipo.text = "${it.tipo}"
            txtPrioridad.text = "${it.prioridad}"
        }
    }
    private fun mostrarDialogoEliminar() {
        AlertDialog.Builder(this) // ✅ Corrección: solo 'this' es suficiente
            .setTitle("Eliminar")
            .setMessage("¿Seguro que deseas eliminar este gasto?")
            .setPositiveButton("Sí") { _, _ ->
                dbHelper.eliminarTarea(tareaId, this) // ✅ Pasamos el contexto a la función eliminarTarea
                cancelarNotificacion(tareaId) // ✅ Cancelamos la notificación
                Toast.makeText(this, "Eliminado", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

}
