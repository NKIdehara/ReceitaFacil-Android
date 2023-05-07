package br.edu.infnet.receitafacil.api

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import kotlin.collections.ArrayList

/*
    CRÉDITOS: TheMealDB
    API:      https://www.themealdb.com/api.php
 */
const val BASE_URL = "https://www.themealdb.com"

// Estrutura JSON
data class meals(
    val idMeal: Int,
    val strMeal: String,
    val strDrinkAlternate: String,
    val strCategory: String,
    val strArea: String,
    val strInstructions: String,
    val strMealThumb: String,
    val strTags: String,
    val strYoutube: String,
    val strIngredient1: String,
    val strIngredient2: String,
    val strIngredient3: String,
    val strIngredient4: String,
    val strIngredient5: String,
    val strIngredient6: String,
    val strIngredient7: String,
    val strIngredient8: String,
    val strIngredient9: String,
    val strIngredient10: String,
    val strIngredient11: String,
    val strIngredient12: String,
    val strIngredient13: String,
    val strIngredient14: String,
    val strIngredient15: String,
    val strIngredient16: String,
    val strIngredient17: String,
    val strIngredient18: String,
    val strIngredient19: String,
    val strIngredient20: String,
    val strMeasure1: String,
    val strMeasure2: String,
    val strMeasure3: String,
    val strMeasure4: String,
    val strMeasure5: String,
    val strMeasure6: String,
    val strMeasure7: String,
    val strMeasure8: String,
    val strMeasure9: String,
    val strMeasure10: String,
    val strMeasure11: String,
    val strMeasure12: String,
    val strMeasure13: String,
    val strMeasure14: String,
    val strMeasure15: String,
    val strMeasure16: String,
    val strMeasure17: String,
    val strMeasure18: String,
    val strMeasure19: String,
    val strMeasure20: String,
    val strSource: String,
    val strImageSource: String,
    val strCreativeCommonsConfirmed: String,
    val dateModified: String
)

data class ReceitaPost(
    @SerializedName("meals" ) var receita : ArrayList<meals> = arrayListOf()
)

// Obtem receita aleatória
interface ReceitaApi{
    @GET("/api/json/v1/1/random.php")
    suspend fun getPost(): Response<ReceitaPost>
}

object RetrofitInstance{
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ReceitaApi by lazy {
        retrofit.create(ReceitaApi::class.java)
    }
}

class Repository{
    suspend fun getPost(): Response<ReceitaPost>{
        return RetrofitInstance.api.getPost()
    }
}

class ReceitaApiViewModel(private val repository: Repository): ViewModel(){
    val myResponse: MutableLiveData<Response<ReceitaPost>> = MutableLiveData()

    // Executa ação em segundo plano
    fun getPost(){
        viewModelScope.launch {
            val response: Response<ReceitaPost> = repository.getPost()
            myResponse.value = response
        }
    }
}

class ReceitaApiViewModelFactory(private val repository: Repository): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReceitaApiViewModel(repository) as T
    }
}