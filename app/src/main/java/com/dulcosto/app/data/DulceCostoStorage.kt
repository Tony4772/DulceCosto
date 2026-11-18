package com.dulcosto.app.data

import android.content.Context
import com.dulcosto.app.domain.CostSettings
import com.dulcosto.app.domain.RecipeLine
import com.dulcosto.app.domain.Supply
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class SavedRecipe(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val lines: List<RecipeLine>,
    val salePrice: Double = 0.0,
    val settings: CostSettings = CostSettings(wastePercent = 0.0, labor = 0.0, taxEnabled = false, deliveryEnabled = false),
    val updatedAt: Long = System.currentTimeMillis()
)

data class AppState(
    val supplies: List<Supply>,
    val savedRecipes: List<SavedRecipe>,
    val activeRecipeId: String? = null,
    val activeRecipeName: String = "",
    val activeLines: List<RecipeLine> = emptyList(),
    val activeSalePrice: Double = 0.0,
    val activeSettings: CostSettings = CostSettings(wastePercent = 0.0, labor = 0.0, taxEnabled = false, deliveryEnabled = false),
    val currencyCode: String = "PEN"
)

class DulceCostoStorage(context: Context) {
    private val prefs = context.getSharedPreferences("dulce_costo_storage_v1", Context.MODE_PRIVATE)

    fun loadState(): AppState {
        val suppliesJson = prefs.getString("supplies", null)
        val supplies = if (suppliesJson != null) {
            parseSupplies(suppliesJson)
        } else {
            defaultStarterSupplies()
        }

        val recipesJson = prefs.getString("saved_recipes", null)
        val savedRecipes = if (recipesJson != null) parseSavedRecipes(recipesJson) else emptyList()

        val activeRecipeId = prefs.getString("active_recipe_id", null)
        val activeRecipeName = prefs.getString("active_recipe_name", "") ?: ""
        val activeLinesJson = prefs.getString("active_lines", null)
        val activeLines = if (activeLinesJson != null) parseRecipeLines(activeLinesJson) else emptyList()
        val activeSalePrice = prefs.getFloat("active_sale_price", 0.0f).toDouble()
        val currencyCode = prefs.getString("currency_code", "PEN") ?: "PEN"

        val settingsJson = prefs.getString("active_settings", null)
        val activeSettings = if (settingsJson != null) {
            parseSettings(JSONObject(settingsJson))
        } else {
            CostSettings(wastePercent = 0.0, labor = 0.0, taxEnabled = false, deliveryEnabled = false)
        }

        return AppState(
            supplies = supplies,
            savedRecipes = savedRecipes,
            activeRecipeId = activeRecipeId,
            activeRecipeName = activeRecipeName,
            activeLines = activeLines,
            activeSalePrice = activeSalePrice,
            activeSettings = activeSettings,
            currencyCode = currencyCode
        )
    }

    fun saveState(state: AppState) {
        prefs.edit().apply {
            putString("supplies", serializeSupplies(state.supplies))
            putString("saved_recipes", serializeSavedRecipes(state.savedRecipes))
            putString("active_recipe_id", state.activeRecipeId)
            putString("active_recipe_name", state.activeRecipeName)
            putString("active_lines", serializeRecipeLines(state.activeLines))
            putFloat("active_sale_price", state.activeSalePrice.toFloat())
            putString("active_settings", serializeSettings(state.activeSettings).toString())
            putString("currency_code", state.currencyCode)
            apply()
        }
    }

    private fun defaultStarterSupplies(): List<Supply> = listOf(
        Supply(1, "Harina sin preparar", "Base", 4.50, 1.0, "kg"),
        Supply(2, "Azúcar blanca", "Base", 3.80, 1.0, "kg"),
        Supply(3, "Huevos", "Base", 9.00, 1.0, "docena"),
        Supply(4, "Mantequilla sin sal", "Base", 8.50, 250.0, "g"),
        Supply(5, "Caja para torta", "Empaque", 25.00, 10.0, "und")
    )

    private fun serializeSupplies(supplies: List<Supply>): String {
        val array = JSONArray()
        for (s in supplies) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.name)
            obj.put("category", s.category)
            obj.put("purchasePrice", s.purchasePrice)
            obj.put("purchaseQuantity", s.purchaseQuantity)
            obj.put("unit", s.unit)
            obj.put("stockNote", s.stockNote)
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseSupplies(json: String): List<Supply> {
        val list = mutableListOf<Supply>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                Supply(
                    id = obj.getInt("id"),
                    name = obj.getString("name"),
                    category = obj.optString("category", "Base"),
                    purchasePrice = obj.optDouble("purchasePrice", 0.0),
                    purchaseQuantity = obj.optDouble("purchaseQuantity", 1.0),
                    unit = obj.optString("unit", "kg"),
                    stockNote = obj.optString("stockNote", "")
                )
            )
        }
        return list
    }

    private fun serializeRecipeLines(lines: List<RecipeLine>): String {
        val array = JSONArray()
        for (l in lines) {
            val obj = JSONObject()
            obj.put("supplyId", l.supplyId)
            obj.put("quantity", l.quantity)
            obj.put("unit", l.unit)
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseRecipeLines(json: String): List<RecipeLine> {
        val list = mutableListOf<RecipeLine>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                RecipeLine(
                    supplyId = obj.getInt("supplyId"),
                    quantity = obj.optDouble("quantity", 0.0),
                    unit = obj.optString("unit", "")
                )
            )
        }
        return list
    }

    private fun serializeSettings(settings: CostSettings): JSONObject {
        val obj = JSONObject()
        obj.put("wastePercent", settings.wastePercent)
        obj.put("labor", settings.labor)
        obj.put("taxPercent", settings.taxPercent)
        obj.put("taxEnabled", settings.taxEnabled)
        obj.put("deliveryPercent", settings.deliveryPercent)
        obj.put("deliveryEnabled", settings.deliveryEnabled)
        obj.put("deliveryFixed", settings.deliveryFixed)
        return obj
    }

    private fun parseSettings(obj: JSONObject): CostSettings {
        return CostSettings(
            wastePercent = obj.optDouble("wastePercent", 0.0),
            labor = obj.optDouble("labor", 0.0),
            taxPercent = obj.optDouble("taxPercent", 18.0),
            taxEnabled = obj.optBoolean("taxEnabled", false),
            deliveryPercent = obj.optDouble("deliveryPercent", 25.0),
            deliveryEnabled = obj.optBoolean("deliveryEnabled", false),
            deliveryFixed = obj.optDouble("deliveryFixed", 0.0)
        )
    }

    private fun serializeSavedRecipes(recipes: List<SavedRecipe>): String {
        val array = JSONArray()
        for (r in recipes) {
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("name", r.name)
            obj.put("lines", JSONArray(serializeRecipeLines(r.lines)))
            obj.put("salePrice", r.salePrice)
            obj.put("settings", serializeSettings(r.settings))
            obj.put("updatedAt", r.updatedAt)
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseSavedRecipes(json: String): List<SavedRecipe> {
        val list = mutableListOf<SavedRecipe>()
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val linesJson = obj.getJSONArray("lines").toString()
            val settingsObj = obj.optJSONObject("settings")
            list.add(
                SavedRecipe(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    lines = parseRecipeLines(linesJson),
                    salePrice = obj.optDouble("salePrice", 0.0),
                    settings = if (settingsObj != null) parseSettings(settingsObj) else CostSettings(),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            )
        }
        return list
    }
}
