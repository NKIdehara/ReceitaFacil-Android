package br.edu.infnet.receitafacil.receitas

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import br.edu.infnet.receitafacil.R
import com.squareup.picasso.Picasso

class ReceitaAdapter(private val receitaList: List<Receita>) :
    RecyclerView.Adapter<ReceitaAdapter.ReceitaViewHolder>() {

    class ReceitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView:  CardView = itemView.findViewById(R.id.ReceitaLayout)
        val imageView: ImageView = itemView.findViewById(R.id.img_receita_item)
        val textView1: TextView = itemView.findViewById(R.id.txt_nome_item)
        val textView2: TextView = itemView.findViewById(R.id.txt_ingredientes_item)
        val textView3: TextView = itemView.findViewById(R.id.txt_receita_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceitaViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item, parent, false)
        return ReceitaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ReceitaViewHolder, position: Int) {
        val currentItem = receitaList[position]

        Picasso.get().load(currentItem.figura).placeholder(R.drawable.ic_wait).into(holder.imageView);
        holder.textView1.text = currentItem.nome
        holder.textView2.text = currentItem.ingredientes
        holder.textView3.text = currentItem.receita

        // Ao selecionar item, abre tela para atualizar / apagar cadastro
        holder.cardView.setOnClickListener {
            val action = ReceitasFragmentDirections.actionNavReceitasToNavAtualizar(currentItem) // envia item selecionado como argumento para AtualizarFragment
            holder.itemView.findNavController().navigate(action)
        }
    }

    override fun getItemCount() = receitaList.size
}