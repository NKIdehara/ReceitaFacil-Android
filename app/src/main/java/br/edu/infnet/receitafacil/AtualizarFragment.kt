package br.edu.infnet.receitafacil

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import br.edu.infnet.receitafacil.databinding.FragmentAtualizarBinding
import br.edu.infnet.receitafacil.receitas.ReceitasViewModel
import br.edu.infnet.receitafacil.receitas.usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime

class AtualizarFragment : Fragment() {

    private val args by navArgs<AtualizarFragmentArgs>()

    lateinit var receitasViewModel: ReceitasViewModel
    private var _binding: FragmentAtualizarBinding? = null
    private val binding get() = _binding!!

    // Firebase Database
    private lateinit var receitaDatabase: FirebaseFirestore
    private val receitaRef = Firebase.firestore.collection("Receitas")

    // variáveis de controle para câmera
    private var TIROU_FOTO = 0
    val RESULT_OK = 1
    val REQUEST_CODE = 200
    private lateinit var local_figura: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAtualizarBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.progressBar?.isVisible = false

        // atualiza campos com dados existentes
        // dados recebidos como argumento de ReceitaAdapter
        Picasso.get().load(args.selecionado.figura).placeholder(R.drawable.ic_wait).into(binding.imgFotoAtualizar);
        binding.txtNomeAtualizar.setText(args.selecionado.nome)
        binding.txtIngredientesAtualizar.setText(args.selecionado.ingredientes)
        binding.txtReceitaAtualizar.setText(args.selecionado.receita)

        // tira foto ao pressionar imagem
        binding.imgFotoAtualizar.setOnClickListener{

            // Verifica se existe câmera no dispositivo
            val pm = getActivity()!!.getPackageManager()
            if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, REQUEST_CODE)
            }
            else {
                Toast.makeText(getActivity() , "Camera não disponível!", Toast.LENGTH_SHORT).show()
            }
        }

        // botão atualizar
        binding.btnAtualizar.setOnClickListener {
            AtualizarReceita()
            receitasViewModel = ViewModelProvider(requireActivity()).get(ReceitasViewModel::class.java)
            findNavController().navigate(R.id.action_nav_atualizar_to_nav_home)
            Toast.makeText(getActivity() , "Receita atualizada!", Toast.LENGTH_SHORT).show()
        }

        // botão apagar
        binding.btnApagar.setOnClickListener {
            ApagarReceita()
            receitasViewModel = ViewModelProvider(requireActivity()).get(ReceitasViewModel::class.java)
            findNavController().navigate(R.id.action_nav_atualizar_to_nav_home)
            Toast.makeText(getActivity() , "Receita apagada!", Toast.LENGTH_SHORT).show()
        }

        return root
    }

    private fun AtualizarReceita() = CoroutineScope(Dispatchers.IO).launch {
        // procura receita selecionada
        val atualizar_receita = receitaRef
            .whereEqualTo("usuario", usuario)
            .whereEqualTo("nome", args.selecionado.nome)
            .whereEqualTo("ingredientes", args.selecionado.ingredientes)
            .whereEqualTo("receita", args.selecionado.receita)
            .get()
            .await()

        if (atualizar_receita.documents.isNotEmpty()){
            for (receita in atualizar_receita) {
                try {
                    if (TIROU_FOTO == 1){
                        binding.progressBar?.isVisible = true

                        // Nome de arquivo com data e hora atual
                        val agora = LocalDateTime.now()
                        val arquivo = "${agora}.jpg"

                        // Bitmap
                        val bitmap = (binding.imgFotoAtualizar.drawable as BitmapDrawable).bitmap
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)

                        // Pegando a referência do serviço
                        val storageRef = FirebaseStorage.getInstance().reference
                        val arquivoRef = storageRef.child(arquivo)

                        // Upload de imagem
                        var uploadTask = arquivoRef.putBytes(baos.toByteArray())

                        uploadTask.addOnFailureListener { taskSnapshot ->
                            //Toast.makeText(getActivity() , taskSnapshot.message, Toast.LENGTH_LONG).show()
                        }.addOnSuccessListener { taskSnapshot ->
                            //Toast.makeText(getActivity() , taskSnapshot.error, Toast.LENGTH_LONG).show()
                        }

                        // Grava foto
                        val urlTask = uploadTask.continueWithTask { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let {
                                    throw it
                                }
                            }
                            arquivoRef.downloadUrl
                        }.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Upload de imagem bem sucedido
                                val downloadUri = task.result
                                local_figura = downloadUri.toString()
                                val receitaatual = hashMapOf(
                                    "usuario" to usuario,
                                    "figura" to  local_figura,
                                    "nome" to binding.txtNomeAtualizar.text.toString(),
                                    "ingredientes" to binding.txtIngredientesAtualizar.text.toString(),
                                    "receita" to binding.txtReceitaAtualizar.text.toString()
                                )
                                receitaRef.document(receita.id).set(receitaatual, SetOptions.merge())
                            } else {
                                Toast.makeText(getActivity(),"Erro ao salvar receita!",Toast.LENGTH_LONG).show()
                            }
                        }
                        binding.progressBar?.isVisible = false
                    }
                    // Receita sem foto
                    else{
                        local_figura = args.selecionado.figura
                        val receitaatual = hashMapOf(
                            "usuario" to usuario,
                            "figura" to  local_figura,
                            "nome" to binding.txtNomeAtualizar.text.toString(),
                            "ingredientes" to binding.txtIngredientesAtualizar.text.toString(),
                            "receita" to binding.txtReceitaAtualizar.text.toString()
                        )
                        receitaRef.document(receita.id).set(receitaatual, SetOptions.merge()).await()
                    }
                }
                catch (error: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(getActivity(), error.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        else {
            withContext(Dispatchers.Main) {
                Toast.makeText(getActivity(), "Nenhuma receita para atualizar.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun ApagarReceita() = CoroutineScope(Dispatchers.IO).launch {
        // procura receita selecionada
        val apagar_receita = receitaRef
            .whereEqualTo("usuario", usuario)
            .whereEqualTo("nome", args.selecionado.nome)
            .whereEqualTo("ingredientes", args.selecionado.ingredientes)
            .whereEqualTo("receita", args.selecionado.receita)
            .get()
            .await()

        if (apagar_receita.documents.isNotEmpty()){
            for (receita in apagar_receita) {
                try {
                    receitaRef.document(receita.id).delete().await()
                }
                catch (error: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(getActivity(), error.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        else {
            withContext(Dispatchers.Main) {
                Toast.makeText(getActivity(), "Nenhuma receita para apagar.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // adiciona foto tirada
        if (resultCode == -RESULT_OK && requestCode == REQUEST_CODE) {
            binding.imgFotoAtualizar.setImageDrawable(bitmapToDrawable(data?.extras?.get("data") as Bitmap))
            TIROU_FOTO = 1
        }
        else{
            Toast.makeText(getActivity() , "Erro ao tirar foto!", Toast.LENGTH_SHORT).show()
        }
    }

    // converte BitMap para Drawable
    private fun bitmapToDrawable(bitmap: Bitmap): BitmapDrawable {
        return BitmapDrawable(resources,bitmap)
    }

}