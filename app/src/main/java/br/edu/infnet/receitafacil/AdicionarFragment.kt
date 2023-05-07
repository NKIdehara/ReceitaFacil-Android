package br.edu.infnet.receitafacil

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import br.edu.infnet.receitafacil.databinding.FragmentAdicionarBinding
import br.edu.infnet.receitafacil.receitas.ReceitasViewModel
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import br.edu.infnet.receitafacil.api.ReceitaApiViewModel
import br.edu.infnet.receitafacil.api.ReceitaApiViewModelFactory
import br.edu.infnet.receitafacil.api.Repository
import br.edu.infnet.receitafacil.receitas.usuario
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime

class AdicionarFragment : Fragment() {

    lateinit var receitasViewModel: ReceitasViewModel

    // Firebase Database
    private lateinit var receitaDatabase: FirebaseFirestore

    private var _binding: FragmentAdicionarBinding? = null
    private val binding get() = _binding!!

    // variáveis de controle para câmera
    private var TIROU_FOTO = 0
    val RESULT_OK = 1
    val REQUEST_CODE = 200
    private lateinit var local_figura: String

    // Receita API
    private lateinit var apiModel: ReceitaApiViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAdicionarBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar?.isVisible = false

        binding.btnAuto.setOnClickListener{
            binding.progressBar?.isVisible = true
            FotoApi()
            binding.progressBar?.isVisible = false
        }

        // tira foto ao pressionar imagem
        binding.imgFoto.setOnClickListener{

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

        // botão adicionar pressionado
        binding.btnAdicionar.setOnClickListener {
            if (binding.txtNome.text.toString() == "" || binding.txtReceita.text.toString() == ""){
                Toast.makeText(getActivity() , "Receita ruim!", Toast.LENGTH_SHORT).show()
            }
            else{
                // Receita com foto
                if (TIROU_FOTO == 1){
                    binding.progressBar?.isVisible = true

                    // Nome de arquivo com data e hora atual
                    val agora = LocalDateTime.now()
                    val arquivo = "${agora}.jpg"

                    // Bitmap
                    val bitmap = (binding.imgFoto.drawable as BitmapDrawable).bitmap
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

                    // Grava receita
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
                            val novareceita = hashMapOf(
                                "usuario" to usuario,
                                "figura" to local_figura,
                                "nome" to binding.txtNome.text.toString(),
                                "ingredientes" to binding.txtIngredientes.text.toString(),
                                "receita" to binding.txtReceita.text.toString()
                            )

                            receitaDatabase = FirebaseFirestore.getInstance()
                            receitaDatabase.collection("Receitas").document().set(novareceita)

                            binding.progressBar?.isVisible = false

                            /// abre ViewModel associado à Activity
                            receitasViewModel = ViewModelProvider(requireActivity()).get(ReceitasViewModel::class.java)
                            findNavController().navigate(R.id.action_nav_adicionar_to_nav_home)
                            Toast.makeText(getActivity() , "Receita adicionada!", Toast.LENGTH_SHORT).show()
                        } else {
                            binding.progressBar?.isVisible = false
                            Toast.makeText(getActivity() , "Erro ao salvar receita!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                // Receita sem foto
                else{
                    local_figura = "https://firebasestorage.googleapis.com/v0/b/android-infnet.appspot.com/o/ic_food_0.png?alt=media&token=319db917-8c70-4a70-8125-6fa43e284bb5"
                    val novareceita = hashMapOf(
                        "usuario" to usuario,
                        "figura" to local_figura,
                        "nome" to binding.txtNome.text.toString(),
                        "ingredientes" to binding.txtIngredientes.text.toString(),
                        "receita" to binding.txtReceita.text.toString()
                    )

                    receitaDatabase = FirebaseFirestore.getInstance()
                    receitaDatabase.collection("Receitas").document().set(novareceita)

                    /// abre ViewModel associado à Activity
                    receitasViewModel = ViewModelProvider(requireActivity()).get(ReceitasViewModel::class.java)
                    findNavController().navigate(R.id.action_nav_adicionar_to_nav_home)
                    Toast.makeText(getActivity() , "Receita adicionada!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // adiciona foto tirada
        if (resultCode == -RESULT_OK && requestCode == REQUEST_CODE) {
            binding.imgFoto.setImageDrawable(bitmapToDrawable(data?.extras?.get("data") as Bitmap))
            TIROU_FOTO = 1
        }
        else{
            Toast.makeText(getActivity() , "Erro ao tirar foto!", Toast.LENGTH_SHORT).show()
        }
    }

    // converte BitMap para Drawable
    private fun bitmapToDrawable(bitmap:Bitmap):BitmapDrawable{
        return BitmapDrawable(resources,bitmap)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Obtem foto aleatória
    fun FotoApi() {
        // Receitas API
        val repository = Repository()
        val apiModelFactory = ReceitaApiViewModelFactory(repository)
        apiModel = ViewModelProvider(this, apiModelFactory).get(ReceitaApiViewModel::class.java)
        apiModel.getPost()

        apiModel.myResponse.observe(this, Observer { response ->
            // Completou comunicação com site e recebeu JSON
            if(response.isSuccessful){
                val img = response.body()?.receita?.get(0)?.strMealThumb
                Picasso.get().load(img).placeholder(R.drawable.ic_wait).into(binding.imgFoto);
                TIROU_FOTO = 1
                Log.d("API", response.body()?.receita.toString())
            }
            else{
                Log.d("API", response.errorBody().toString())
                Toast.makeText(getActivity() , response.errorBody().toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }
}
