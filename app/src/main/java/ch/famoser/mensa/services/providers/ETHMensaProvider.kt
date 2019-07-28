package ch.famoser.mensa.services.providers

import android.content.res.AssetManager
import android.net.Uri
import ch.famoser.mensa.models.Location
import ch.famoser.mensa.models.Mensa
import java.net.URL
import java.util.*
import kotlin.collections.HashMap
import ch.famoser.mensa.models.Menu
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.collections.ArrayList


class ETHMensaProvider(assetManager: AssetManager) : AbstractMensaProvider(assetManager) {
    private val mensaMap: MutableMap<Mensa, EthMensa> = HashMap()

    override fun getMenus(mensa: Mensa, date: LocalDate): List<Menu> {
        val ethMensa = mensaMap[mensa]
        if (ethMensa === null)
            throw IllegalArgumentException("You may only pass objects generated by this provider.")

        try {
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            val dateSlug = date.format(dateFormatter);
            val apiUrl =
                "https://www.webservices.ethz.ch/gastro/v1/RVRI/Q1E1/mensas/${ethMensa.idSlug}/de/menus/daily/$dateSlug/${ethMensa.timeSlug}?language=de"
            val response = URL(apiUrl).readText()
            val apiMensa = jsonToT(response, ApiMensa::class.java);

            return apiMensa.menu.meals.map { apiMeal ->
                Menu(
                    apiMeal.label,
                    normalizeText(apiMeal.description.joinToString(separator = "\n")),
                    apiMeal.prices.run { arrayOf<String?>(student, staff, extern) }.filterNotNull().toTypedArray(),
                    apiMeal.allergens
                        .fold(ArrayList<String>(), { acc, apiAllergen -> acc.add(apiAllergen.label); acc })
                        .joinToString(separator = ", ")
                )
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            return ArrayList()
        }
    }

    override fun getLocations(): List<Location> {
        val ethLocations = super.readJsonAssetFileToListOfT("eth/inventory.json", EthLocation::class.java);

        return ethLocations.map { ethLocation ->
            Location(ethLocation.title, ethLocation.mensas.map {
                val mensaId = UUID.fromString(it.id)
                val imageName = it.infoUrlSlug.substring(it.infoUrlSlug.indexOf("/") + 1)
                val mensa = Mensa(
                    mensaId,
                    it.title,
                    it.mealTime,
                    Uri.parse("https://ethz.ch/de/campus/erleben/gastronomie-und-einkaufen/gastronomie/restaurants-und-cafeterias/" + it.infoUrlSlug),
                    "eth/images/$imageName.jpg"
                )
                mensaMap[mensa] = it
                mensa
            })
        }
    }


    data class ApiMensa(val id: Int, val mensa: String, val daytime: String, val hours: ApiHours, val menu: ApiMenu)
    data class ApiHours(val opening: List<ApiOpening>, val mealtime: List<ApiMealtime>)
    data class ApiOpening(val from: String, val to: String, val type: String)
    data class ApiMealtime(val from: String, val to: String, val type: String)
    data class ApiMenu(val date: String, val day: String, val meals: List<ApiMeal>)
    data class ApiMeal(
        val id: Int,
        val type: String,
        val label: String,
        val description: List<String>,
        val position: Int,
        val prices: ApiPrices,
        val allergens: List<ApiAllergen>,
        val origins: List<ApiOrigin>
    )

    data class ApiPrices(val student: String, val staff: String, val extern: String)
    data class ApiAllergen(val allergen_id: Int, val label: String)
    data class ApiOrigin(val origin_id: Int, val label: String)

    data class EthLocation(val title: String, val mensas: List<EthMensa>)
    data class EthMensa(
        val id: String,
        val title: String,
        val mealTime: String,
        val idSlug: Int,
        val timeSlug: String,
        val infoUrlSlug: String
    )
}