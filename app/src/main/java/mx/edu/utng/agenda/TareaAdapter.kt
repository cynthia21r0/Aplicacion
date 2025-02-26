package mx.edu.utng.agenda


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.utng.agenda.R
import mx.edu.utng.agenda.Tarea

class TareaAdapter(
    private var listaTareas: List<Tarea>,
    private val onItemClick: (Tarea) -> Unit
) : RecyclerView.Adapter<TareaAdapter.TareaViewHolder>() {

    class TareaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTitulo: TextView = view.findViewById(R.id.txtTitulo)
        val txtResponsable: TextView = view.findViewById(R.id.txtPersona)
        val txtCantidad: TextView = view.findViewById(R.id.txtCantidad)
        val txtFechaHora: TextView = view.findViewById(R.id.txtFechaH)
        val txtTipo: TextView = view.findViewById(R.id.txtTipo)
        val txtPrioridad: TextView = view.findViewById(R.id.txtPrioridad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TareaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tarea, parent, false)
        return TareaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TareaViewHolder, position: Int) {
        val tarea = listaTareas[position]
        holder.txtTitulo.text = tarea.titulo
        holder.txtResponsable.text = tarea.responsable
        holder.txtCantidad.text = tarea.cantidad
        holder.txtFechaHora.text = "${tarea.fecha}"
        holder.txtTipo.text = "Tipo: ${tarea.tipo}"
        holder.txtPrioridad.text = "Prioridad: ${tarea.prioridad}"

        // Al hacer clic en la tarea, se abrirá la pantalla de detalles
        holder.itemView.setOnClickListener { onItemClick(tarea) }
    }

    override fun getItemCount(): Int = listaTareas.size

    // Método para actualizar la lista de tareas
    fun actualizarLista(nuevaLista: List<Tarea>) {
        listaTareas = nuevaLista
        notifyDataSetChanged()
    }
}
