package com.dulcosto.app

import com.dulcosto.app.domain.CostSettings
import com.dulcosto.app.domain.CostingEngine
import com.dulcosto.app.domain.RecipeLine
import com.dulcosto.app.domain.Supply
import org.junit.Assert.assertEquals
import org.junit.Test

class CostingEngineTest {
    private val sugar = Supply(1, "Azúcar", "Base", 4.0, 1000.0, "g")
    private val flour = Supply(2, "Harina", "Base", 3.5, 1000.0, "g")

    @Test fun calculatesCostPerPurchasedUnit() {
        assertEquals(0.004, sugar.costPerUnit, 0.00001)
    }

    @Test fun calculatesRecipeCostWithWasteAndFixedCosts() {
        val result = CostingEngine.calculate(
            listOf(sugar, flour),
            listOf(RecipeLine(1, 200.0), RecipeLine(2, 300.0)),
            CostSettings(wastePercent = 5.0, labor = 3.5, packaging = 2.0, taxEnabled = false, deliveryEnabled = false),
            salePrice = 20.0
        )
        assertEquals(1.85, result.ingredients, 0.001)
        assertEquals(0.0925, result.waste, 0.001)
        assertEquals(7.4425, result.totalCost, 0.001)
    }

    @Test fun convertsPurchasedKilogramsToRecipeGrams() {
        val sugarByKilogram = Supply(3, "Azúcar", "Base", 4.0, 1.0, "kg")
        val result = CostingEngine.calculate(
            listOf(sugarByKilogram),
            listOf(RecipeLine(3, 300.0, "g")),
            CostSettings(wastePercent = 0.0, labor = 0.0, packaging = 0.0, taxEnabled = false, deliveryEnabled = false),
            salePrice = 0.0
        )
        assertEquals(1.20, result.ingredients, 0.001)
    }

    @Test fun suggestsPriceForTargetMargin() {
        assertEquals(20.0, CostingEngine.priceForMargin(12.0, 40.0), 0.001)
    }
}
