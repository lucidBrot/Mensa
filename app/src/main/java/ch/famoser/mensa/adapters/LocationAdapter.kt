package ch.famoser.mensa.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ch.famoser.mensa.R
import ch.famoser.mensa.activities.MainActivity
import ch.famoser.mensa.models.Location
import kotlinx.android.synthetic.main.row_location.view.*
import java.util.*
import kotlin.collections.ArrayList

class LocationAdapter(
    private val parentActivity: MainActivity,
    private val twoPane: Boolean,
    private val values: List<Location>
) :
    RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_location, parent, false)
        return ViewHolder(view)
    }

    private val mensaAdapters: MutableList<MensaAdapter> = ArrayList()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]

        val adapter = MensaAdapter(parentActivity, item.mensas, twoPane)
        mensaAdapters.add(adapter)

        holder.titleView.text = item.title
        holder.mensaView.adapter = adapter

        with(holder.itemView) {
            tag = item
        }
    }

    override fun getItemCount() = values.size

    fun mensaMenusRefreshed(mensaId: UUID) {
        for (mensaAdapter in mensaAdapters) {
            mensaAdapter.mensaMenusRefreshed(mensaId)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.title
        val mensaView: RecyclerView = view.mensa_list
    }
}