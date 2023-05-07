package br.edu.infnet.receitafacil

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import br.edu.infnet.receitafacil.databinding.ActivityLoginBinding
import br.edu.infnet.receitafacil.receitas.usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    // Firebase Database
    private lateinit var receitaDatabase: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicia Firebase Auth
        auth = Firebase.auth

    }

    fun Login(view: View) {
        val email = binding.txtEmail.text.toString()
        val password = binding.txtSenha.text.toString()

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            Toast.makeText(applicationContext, "Campos não podem ser vazios", Toast.LENGTH_SHORT).show()
        }
        else {
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // login ben sucedido
                    usuario = Firebase.auth.currentUser?.uid.toString()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                // Erro
            }.addOnFailureListener { exception ->
                Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()
            }

        }
    }

    fun NovoLogin(view: View) {
        val email = binding.txtEmail.text.toString()
        val password = binding.txtSenha.text.toString()

        // Verifica campos de email / senha
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            Toast.makeText(applicationContext, "Campos não podem ser vazios",Toast.LENGTH_SHORT).show()
        }
        else {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Novo usuário
                    Toast.makeText(applicationContext, "Novo usuário registrado com sucesso!",Toast.LENGTH_LONG).show()
                    usuario = Firebase.auth.currentUser?.uid.toString()

                    // Cria lista inicial de receitas para novo usuário
                    CriaReceitasBonus1()
                    CriaReceitasBonus2()
                    CriaReceitasBonus3()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                // Erro
            }.addOnFailureListener { exception ->
                Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun CriaReceitasBonus1(){
        val receitas_bonus = hashMapOf(
            "usuario" to usuario,
            "figura" to "https://firebasestorage.googleapis.com/v0/b/android-infnet.appspot.com/o/ic_food_1.png?alt=media&token=c0c60b1b-4fcc-4882-a14c-a13dacbbb84c",
            "nome" to "Arroz",
            "ingredientes" to "- 1 xícara de arroz\n- 1/2 colher de sal",
            "receita" to "Lavar o arroz; colocar sal; cozinhar por 30 minutos."
        )
        receitaDatabase = FirebaseFirestore.getInstance()
        receitaDatabase.collection("Receitas").document().set(receitas_bonus)
    }
    private fun CriaReceitasBonus2(){
        val receitas_bonus = hashMapOf(
            "usuario" to usuario,
            "figura" to "https://firebasestorage.googleapis.com/v0/b/android-infnet.appspot.com/o/ic_food_2.png?alt=media&token=3d2ec1e0-5353-40c6-944e-e3c56ca5f1de",
            "nome" to "Carne",
            "ingredientes" to "- 1 bife\n- tempero a gosto",
            "receita" to "Temperar a carne; assar até dourar."
        )
        receitaDatabase = FirebaseFirestore.getInstance()
        receitaDatabase.collection("Receitas").document().set(receitas_bonus)
    }
    private fun CriaReceitasBonus3(){
        val receitas_bonus = hashMapOf(
            "usuario" to usuario,
            "figura" to "https://firebasestorage.googleapis.com/v0/b/android-infnet.appspot.com/o/ic_food_3.png?alt=media&token=bb55fca7-6819-4ed9-b5d7-803eec1a39c5",
            "nome" to "Macarrão",
            "ingredientes" to "- 500g de macarrão\n- 1 lata de molho de tomate",
            "receita" to "Cozinhar o macarrão; escorrer e colocar o molho."
        )
        receitaDatabase = FirebaseFirestore.getInstance()
        receitaDatabase.collection("Receitas").document().set(receitas_bonus)
    }
}