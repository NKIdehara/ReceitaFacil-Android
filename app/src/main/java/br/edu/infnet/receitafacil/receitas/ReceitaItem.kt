package br.edu.infnet.receitafacil.receitas

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Receita(
    var usuario: String = "",
    var figura: String = "",
    var nome: String = "",
    var ingredientes: String = "",
    var receita: String = ""
): Parcelable

// usuário logado
lateinit var usuario: String
