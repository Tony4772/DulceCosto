package com.dulcosto.app.domain

import kotlin.math.max

/** Datos de compra: por ejemplo, 1 kg de azúcar por S/ 4.00. */
data class Supply(
    val id: Int,
    val name: String,
    val category: String,
    val purchasePrice: Double,
    val purchaseQuantity: Double,
    val unit: String,
    val stockNote: String = ""
) {
    val costPerUnit: Double get() = if (purchaseQuantity > 0) purchasePrice / purchaseQuantity else 0.0
    val recipeUnit: String get() = defaultRecipeUnit(unit)
    val costPerRecipeUnit: Double get() = costForQuantity(1.0, recipeUnit, unit) * costPerUnit
}

/** La línea usa la unidad práctica de receta: kg se usa como g, l como ml y docena como und. */
data class RecipeLine(val supplyId: Int, val quantity: Double, val unit: String = "")

data class CostSettings(
    val wastePercent: Double = 5.0,
    val labor: Double = 3.50,
    val packaging: Double = 2.00,
    val taxPercent: Double = 18.0,
    val taxEnabled: Boolean = true,
    val deliveryPercent: Double = 25.0,
    val deliveryEnabled: Boolean = true,
    val deliveryFixed: Double = 0.0
)

data class CostBreakdown(
    val ingredients: Double,
    val waste: Double,
    val labor: Double,
    val packaging: Double,
    val tax: Double,
    val delivery: Double,
    val totalCost: Double,
    val salePrice: Double
) {
    val profit: Double get() = salePrice - totalCost
    val marginPercent: Double get() = if (salePrice > 0) profit / salePrice * 100 else 0.0
    val breakEvenPrice: Double get() = totalCost
}

/** Unidad recomendada para escribir cantidades en una receta. */
fun defaultRecipeUnit(purchaseUnit: String): String = when (purchaseUnit.lowercase()) {
    "kg" -> "g"
    "l" -> "ml"
    "docena" -> "und"
    else -> purchaseUnit
}

/** Unidades que pueden elegirse en la receta sin mezclar familias incompatibles. */
fun compatibleRecipeUnits(purchaseUnit: String): List<String> = when (unitFamily(purchaseUnit.lowercase())) {
    "mass" -> listOf("g", "kg")
    "volume" -> listOf("ml", "l")
    "count" -> listOf("und", "docena")
    "length" -> listOf("cm")
    else -> listOf(purchaseUnit)
}

/** Convierte cantidades solo dentro de la misma familia de medida. */
fun convertQuantity(quantity: Double, fromUnit: String, toUnit: String): Double {
    val from = fromUnit.lowercase()
    val to = toUnit.lowercase()
    if (from == to) return quantity
    val fromFamily = unitFamily(from)
    val toFamily = unitFamily(to)
    if (fromFamily == null || fromFamily != toFamily) return quantity
    return quantity * unitFactor(from) / unitFactor(to)
}

private fun unitFamily(unit: String): String? = when (unit) {
    "g", "kg" -> "mass"
    "ml", "l" -> "volume"
    "und", "docena" -> "count"
    "cm" -> "length"
    else -> null
}

private fun unitFactor(unit: String): Double = when (unit) {
    "kg", "l" -> 1000.0
    "docena" -> 12.0
    else -> 1.0
}

fun costForQuantity(quantity: Double, fromUnit: String, purchaseUnit: String): Double =
    convertQuantity(quantity, fromUnit, purchaseUnit)

object CostingEngine {
    fun calculate(
        supplies: List<Supply>,
        lines: List<RecipeLine>,
        settings: CostSettings,
        salePrice: Double
    ): CostBreakdown {
        val indexed = supplies.associateBy { it.id }
        val ingredients = lines.sumOf { line ->
            val supply = indexed[line.supplyId] ?: return@sumOf 0.0
            val recipeUnit = line.unit.ifBlank { supply.recipeUnit }
            supply.costPerUnit * costForQuantity(line.quantity, recipeUnit, supply.unit)
        }
        val waste = ingredients * max(settings.wastePercent, 0.0) / 100.0
        val base = ingredients + waste + max(settings.labor, 0.0) + max(settings.packaging, 0.0)
        val tax = if (settings.taxEnabled) base * max(settings.taxPercent, 0.0) / 100.0 else 0.0
        val delivery = if (settings.deliveryEnabled) {
            salePrice * max(settings.deliveryPercent, 0.0) / 100.0 + max(settings.deliveryFixed, 0.0)
        } else 0.0
        return CostBreakdown(ingredients, waste, settings.labor, settings.packaging, tax, delivery, base + tax + delivery, salePrice)
    }

    fun priceForMargin(costWithoutDelivery: Double, marginPercent: Double): Double {
        val margin = (marginPercent / 100.0).coerceIn(0.0, 99.0)
        return if (margin == 1.0) costWithoutDelivery else costWithoutDelivery / (1.0 - margin)
    }
}
