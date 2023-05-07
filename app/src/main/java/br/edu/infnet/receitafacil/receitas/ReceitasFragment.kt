package br.edu.infnet.receitafacil.receitas

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import br.edu.infnet.receitafacil.R
import br.edu.infnet.receitafacil.databinding.FragmentReceitasBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.firestore.*

class ReceitasFragment : Fragment() {

    private lateinit var receitasViewModel: ReceitasViewModel
    private lateinit var receitaList: ArrayList<Receita>
    private lateinit var receitaAdapter: ReceitaAdapter

    // Firebase Database
    private lateinit var receitaDatabase: FirebaseFirestore

    // AdMob
    private lateinit var mAdView: AdView


    private var _binding: FragmentReceitasBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentReceitasBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MobileAds.initialize(getActivity()!!) {}
        mAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        val adView = AdView(getActivity()!!)
        adView.adSize = AdSize.BANNER

        // AdMob ad unit ID para versão de testes
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"

        // AdMob ad unit ID para versão de produção
        //adView.adUnitId = "ca-app-pub-2133589299901588/5318016492"

        binding.recyclerviewReceitas.layoutManager = LinearLayoutManager(getActivity())
        binding.recyclerviewReceitas.setHasFixedSize(true)

        /// abre ViewModel associado à Activity
        receitasViewModel = ViewModelProvider(requireActivity()).get(ReceitasViewModel::class.java)
        receitaList = arrayListOf()
        receitaAdapter = ReceitaAdapter(receitaList)
        binding.recyclerviewReceitas.adapter = receitaAdapter

        // Obtém dados do Firebase
        getReceitasData()

        // float action button pressionado
        // chama tela para adicionar nova receita
        binding.fabAdicionar.setOnClickListener{
            findNavController().navigate(R.id.action_nav_receitas_to_navigation_adicionar)
        }
    }

    private fun getReceitasData() {
        receitaDatabase = FirebaseFirestore.getInstance()
        receitaDatabase.collection("Receitas").whereEqualTo("usuario", usuario).orderBy("nome")
            .addSnapshotListener(object: EventListener<QuerySnapshot>{
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?){
                    if (error != null){
                        Toast.makeText(getActivity() , error.message, Toast.LENGTH_SHORT).show()
                    }
                    for (dc: DocumentChange in value?.documentChanges!!){
                        if (dc.type == DocumentChange.Type.ADDED){
                            receitaList.add(dc.document.toObject(Receita::class.java))
                        }
                    }
                    receitaAdapter.notifyDataSetChanged()
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}