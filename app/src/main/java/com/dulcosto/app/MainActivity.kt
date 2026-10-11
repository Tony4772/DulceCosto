package com.dulcosto.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dulcosto.app.domain.CostSettings
import com.dulcosto.app.domain.compatibleRecipeUnits
import com.dulcosto.app.domain.CostingEngine
import com.dulcosto.app.domain.RecipeLine
import com.dulcosto.app.domain.Supply
import com.google.android.gms.ads.MobileAds
import java.util.Locale

private val Mint = Color(0xFF006B5B)
private val Coral = Color(0xFFD85B52)
private val Ink = Color(0xFF202320)
private val Soft = Color(0xFFF4F7F2)

data class CurrencyOption(val code: String, val symbol: String, val name: String)

private val CurrencyOptions = listOf(
    CurrencyOption("PEN", "S/", "Sol peruano"),
    CurrencyOption("CUP", "₱", "Peso cubano"),
    CurrencyOption("MXN", "MX$", "Peso mexicano"),
    CurrencyOption("COP", "COL$", "Peso colombiano"),
    CurrencyOption("CLP", "CLP$", "Peso chileno"),
    CurrencyOption("ARS", "AR$", "Peso argentino"),
    CurrencyOption("DOP", "RD$", "Peso dominicano"),
    CurrencyOption("BRL", "R$", "Real brasileño"),
    CurrencyOption("BOB", "Bs", "Boliviano"),
    CurrencyOption("CRC", "₡", "Colón costarricense"),
    CurrencyOption("GTQ", "Q", "Quetzal guatemalteco"),
    CurrencyOption("HNL", "L", "Lempira hondureño"),
    CurrencyOption("NIO", "C$", "Córdoba nicaragüense"),
    CurrencyOption("PYG", "₲", "Guaraní paraguayo"),
    CurrencyOption("UYU", "\$U", "Peso uruguayo"),
    CurrencyOption("VES", "Bs.S", "Bolívar venezolano"),
    CurrencyOption("USD", "US$", "Dólar estadounidense")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            var showSplash by remember { mutableStateOf(true) }
            if (showSplash) {
                EbyzomSplash(
                    appName = "DulceCosto",
                    tagline = "Tu negocio, con números claros",
                    logo = painterResource(R.drawable.app_logo),
                    paper = Color(0xFFE7EEF2),
                    ink = Color(0xFF123F4D),
                    inkSoft = Color(0xFF5E7580),
                    onFinished = { showSplash = false },
                )
            } else {
                MainSystemBars()
                DulceCostoApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DulceCostoApp() {
    var tab by remember { mutableStateOf(0) }
    var recipeName by remember { mutableStateOf("") }
    var supplies by remember { mutableStateOf(emptyList<Supply>()) }
    var lines by remember { mutableStateOf(emptyList<RecipeLine>()) }
    var settings by remember { mutableStateOf(CostSettings(wastePercent = 0.0, labor = 0.0, packaging = 0.0, taxEnabled = false, deliveryEnabled = false)) }
    var salePrice by remember { mutableStateOf(0.0) }
    var showAdd by remember { mutableStateOf(false) }
    var editingSupply by remember { mutableStateOf<Supply?>(null) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var currency by remember { mutableStateOf(CurrencyOptions.first()) }
    val breakdown = CostingEngine.calculate(supplies, lines, settings, salePrice)
    MaterialTheme(colorScheme = androidx.compose.material3.lightColorScheme(primary = Mint, secondary = Coral, background = Soft, surface = Color.White, onSurface = Ink)) {
        Scaffold(
            containerColor = Soft,
            topBar = { TopAppBar(title = { Column { Text("DulceCosto", fontWeight = FontWeight.Bold); Text("Tu negocio, con números claros", fontSize = 12.sp, color = Mint) } }, actions = { Surface(shape = RoundedCornerShape(14.dp), color = Color.White, modifier = Modifier.clickable { showCurrencyPicker = true }) { Text("${currency.code} ${currency.symbol}", modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), color = Mint, fontWeight = FontWeight.Bold, fontSize = 12.sp) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Soft)) },
            bottomBar = {
                Column(Modifier.navigationBarsPadding().background(Color.White)) {
                    NavigationBar(containerColor = Color.White, windowInsets = WindowInsets(0, 0, 0, 0)) {
                        listOf("Resumen", "Insumos", "Receta", "Precio").forEachIndexed { index, label ->
                            NavigationBarItem(selected = tab == index, onClick = { tab = index }, icon = { Icon(listOf(Icons.Default.ShowChart, Icons.Default.Inventory2, Icons.Default.Cake, Icons.Default.AttachMoney)[index], label) }, label = { Text(label, fontSize = 11.sp) })
                        }
                    }
                    BottomBanner()
                }
            },
            floatingActionButton = { if (tab == 1) FloatingActionButton(onClick = { editingSupply = null; showAdd = true }, containerColor = Mint, contentColor = Color.White) { Icon(Icons.Default.Add, "Agregar insumo") } }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (tab) {
                    0 -> SummaryScreen(recipeName.ifBlank { "Sin receta seleccionada" }, breakdown, settings, salePrice, currency, hasRecipe = recipeName.isNotBlank() || lines.isNotEmpty())
                    1 -> SuppliesScreen(supplies, currency, onDelete = { id -> supplies = supplies.filterNot { it.id == id }; lines = lines.filterNot { it.supplyId == id } }, onEdit = { supply -> editingSupply = supply; showAdd = true }, onAdd = { supply -> supplies = supplies + supply; showAdd = false })
                    2 -> RecipeScreen(
                        recipeName = recipeName,
                        currency = currency,
                        supplies = supplies,
                        lines = lines,
                        onNameChange = { recipeName = it },
                        onAdd = { id -> supplies.firstOrNull { it.id == id }?.let { supply -> lines = lines + RecipeLine(id, 1.0, supply.recipeUnit) } },
                        onChange = { id, qty -> lines = lines.map { if (it.supplyId == id) it.copy(quantity = qty) else it } },
                        onUnitChange = { id, unit -> lines = lines.map { if (it.supplyId == id) it.copy(unit = unit) else it } },
                        onDelete = { id -> lines = lines.filterNot { it.supplyId == id } },
                        onDeleteRecipe = {
                            recipeName = ""
                            lines = emptyList()
                            salePrice = 0.0
                            settings = CostSettings(wastePercent = 0.0, labor = 0.0, packaging = 0.0, taxEnabled = false, deliveryEnabled = false)
                        }
                    )
                    3 -> PricingScreen(recipeName, lines.isNotEmpty(), breakdown, salePrice, settings, currency, onPriceChange = { salePrice = it }, onSettingsChange = { settings = it })
                }
            }
        }
    }
    if (showAdd) AddSupplyDialog(currency = currency, initial = editingSupply, nextId = (supplies.maxOfOrNull { it.id } ?: 0) + 1, onDismiss = { showAdd = false; editingSupply = null }, onSave = { saved -> supplies = if (editingSupply == null) supplies + saved else supplies.map { if (it.id == saved.id) saved else it }; showAdd = false; editingSupply = null })
    if (showCurrencyPicker) CurrencyPickerDialog(selected = currency, onDismiss = { showCurrencyPicker = false }, onSelect = { currency = it; showCurrencyPicker = false })
}

@Composable private fun SummaryScreen(name: String, b: com.dulcosto.app.domain.CostBreakdown, settings: CostSettings, price: Double, currency: CurrencyOption, hasRecipe: Boolean) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Spacer(Modifier.height(10.dp)); Text("Hola, pastelero", color = Mint, fontWeight = FontWeight.Bold); Text("Haz que cada venta cuente.", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item { Card(colors = CardDefaults.cardColors(containerColor = if (hasRecipe) Mint else Color(0xFFDEE8E2)), shape = RoundedCornerShape(24.dp)) { Column(Modifier.padding(20.dp)) { Text(if (hasRecipe) "RECETA ACTIVA" else "SIN RECETA", color = if (hasRecipe) Color(0xFFB8E7D9) else Mint, fontSize = 11.sp, fontWeight = FontWeight.Bold); Text(name, color = if (hasRecipe) Color.White else Ink, fontSize = 20.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(18.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Metric("Costo total", money(b.totalCost, currency), if (hasRecipe) Color.White else Ink); Metric("Ganancia", money(b.profit, currency), if (hasRecipe) Color(0xFFC9F7A8) else Ink); Metric("Margen", "${b.marginPercent.oneDecimal()}%", if (hasRecipe) Color(0xFFFFD28C) else Ink) } } } }
        item { Text("Lectura rápida", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) { MiniCard("Ingredientes", money(b.ingredients, currency), Icons.Default.Cake, Modifier.weight(1f)); MiniCard("Punto de equilibrio", money(b.breakEvenPrice, currency), Icons.Default.ShowChart, Modifier.weight(1f)) } }
        item { Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(18.dp)) { Text("Así se forma tu costo", fontWeight = FontWeight.Bold); CostRow("Ingredientes", b.ingredients, currency = currency); CostRow("Merma (${settings.wastePercent.oneDecimal()}%)", b.waste, currency = currency); CostRow("Mano de obra", b.labor, currency = currency); CostRow("Empaque", b.packaging, currency = currency); if (settings.taxEnabled) CostRow("IGV", b.tax, currency = currency); Divider(Modifier.padding(vertical = 8.dp)); CostRow("Costo final", b.totalCost, true, currency) } } }
        item { Text("Precio actual: ${money(price, currency)} · Delivery: ${if (settings.deliveryEnabled) settings.deliveryPercent.oneDecimal() else "0"}%", color = Color.Gray, fontSize = 13.sp); Spacer(Modifier.height(8.dp)) }
    }
}

@Composable private fun SuppliesScreen(supplies: List<Supply>, currency: CurrencyOption, onDelete: (Int) -> Unit, onEdit: (Supply) -> Unit, onAdd: (Supply) -> Unit) {
    var selectedFilter by remember { mutableStateOf("Todos") }
    val visibleSupplies = if (selectedFilter == "Todos") supplies else supplies.filter { it.category.equals(selectedFilter, ignoreCase = true) }
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { item { Text("Insumos", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Registra lo que compras y DulceCosto lo convierte a costo por unidad.", color = Color.Gray) }; item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Todos", "Base", "Empaque").forEach { filter -> FilterChip(selected = selectedFilter == filter, onClick = { selectedFilter = filter }, label = { Text(filter) }) } } }; items(visibleSupplies, key = { it.id }) { supply -> Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(42.dp).clip(CircleShape).background(Color(0xFFE4F2EC)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Inventory2, null, tint = Mint) }; Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(supply.name, fontWeight = FontWeight.Bold); Text("${money(supply.purchasePrice, currency)} / ${supply.purchaseQuantity.clean()} ${supply.unit} · ${money(supply.costPerUnit, currency)} por ${supply.unit}", color = Color.Gray, fontSize = 12.sp); Text(supply.stockNote, color = Mint, fontSize = 11.sp) }; IconButton(onClick = { onEdit(supply) }) { Icon(Icons.Default.Edit, "Editar", tint = Mint) }; IconButton(onClick = { onDelete(supply.id) }) { Icon(Icons.Default.DeleteOutline, "Eliminar", tint = Coral) } } } } }
}

@Composable private fun RecipeScreen(recipeName: String, currency: CurrencyOption, supplies: List<Supply>, lines: List<RecipeLine>, onNameChange: (String) -> Unit, onAdd: (Int) -> Unit, onChange: (Int, Double) -> Unit, onUnitChange: (Int, String) -> Unit, onDelete: (Int) -> Unit, onDeleteRecipe: () -> Unit) {
    val selected = lines.map { it.supplyId }.toSet()
    val available = supplies.filterNot { it.id in selected }
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Armar receta", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text("Crea una receta y define cuánto usas de cada insumo.", color = Color.Gray) }
        item { Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF2E8)), shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Cake, null, tint = Coral); Spacer(Modifier.width(12.dp)); Text(if (recipeName.isBlank()) "Nueva receta" else recipeName, fontWeight = FontWeight.Bold) }; OutlinedTextField(recipeName, onNameChange, label = { Text("Nombre de la receta") }, placeholder = { Text("Ej. Torta de chocolate") }, singleLine = true, modifier = Modifier.fillMaxWidth()); OutlinedButton(onClick = onDeleteRecipe, modifier = Modifier.fillMaxWidth(), enabled = recipeName.isNotBlank() || lines.isNotEmpty()) { Icon(Icons.Default.DeleteOutline, null, tint = Coral); Spacer(Modifier.width(8.dp)); Text("Eliminar receta") } } } }
        items(lines, key = { it.supplyId }) { line ->
            val supply = supplies.firstOrNull { it.id == line.supplyId } ?: return@items
            val currentUnit = line.unit.ifBlank { supply.recipeUnit }
            val allowedUnits = compatibleRecipeUnits(supply.unit)
            var unitMenuOpen by remember(line.supplyId, currentUnit) { mutableStateOf(false) }
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(supply.name, fontWeight = FontWeight.Bold); Text("${money(supply.costPerRecipeUnit, currency)} por ${supply.recipeUnit} · compra: ${supply.unit}", color = Color.Gray, fontSize = 12.sp) }; OutlinedTextField(line.quantity.clean(), { onChange(supply.id, it.toDoubleOrNull() ?: 0.0) }, label = { Text("Cantidad") }, modifier = Modifier.width(115.dp), singleLine = true); Box { OutlinedButton(onClick = { unitMenuOpen = true }) { Text(currentUnit) }; DropdownMenu(expanded = unitMenuOpen, onDismissRequest = { unitMenuOpen = false }) { allowedUnits.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { onUnitChange(supply.id, option); unitMenuOpen = false }) } } }; IconButton({ onDelete(supply.id) }) { Icon(Icons.Default.DeleteOutline, "Quitar", tint = Coral) } } }
        }
        if (available.isNotEmpty()) {
            item { Text("Añadir a la receta", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)) }
            items(available, key = { it.id }) { supply -> OutlinedButton({ onAdd(supply.id) }, Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("${supply.name}  ·  ${supply.unit}") } }
        }
    }
}

@Composable private fun PricingScreen(recipeName: String, hasRecipe: Boolean, b: com.dulcosto.app.domain.CostBreakdown, price: Double, settings: CostSettings, currency: CurrencyOption, onPriceChange: (Double) -> Unit, onSettingsChange: (CostSettings) -> Unit) {
    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Precio y margen", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold); Text(if (hasRecipe) "Precio de venta de: ${recipeName.ifBlank { "Receta sin nombre" }}" else "Primero crea una receta para definir su precio", color = if (hasRecipe) Mint else Coral, fontWeight = FontWeight.Bold); Text("Este precio se guarda para la receta activa, no para todos tus productos.", color = Color.Gray, fontSize = 12.sp) }
        item { Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Precio de venta de la receta", fontWeight = FontWeight.Bold); OutlinedTextField(price.clean(), { onPriceChange(it.toDoubleOrNull() ?: 0.0) }, enabled = hasRecipe, prefix = { Text("${currency.symbol} ") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Ganancia neta", color = Color.Gray); Text(money(b.profit, currency), color = if (b.profit >= 0) Mint else Coral, fontWeight = FontWeight.Bold, fontSize = 20.sp) }; Text("Margen real: ${b.marginPercent.oneDecimal()}%", color = Mint, fontWeight = FontWeight.Bold) } } }
        item { Text("Costos opcionales", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
        item { SettingRow("IGV", "Se suma al costo cuando corresponde", settings.taxEnabled) { onSettingsChange(settings.copy(taxEnabled = it)) } }
        if (settings.taxEnabled) item { PercentField("Porcentaje IGV", settings.taxPercent) { onSettingsChange(settings.copy(taxPercent = it)) } }
        item { SettingRow("Comisión delivery", "Rappi, PedidosYa u otra app", settings.deliveryEnabled) { onSettingsChange(settings.copy(deliveryEnabled = it)) } }
        if (settings.deliveryEnabled) item { PercentField("Comisión (%)", settings.deliveryPercent) { onSettingsChange(settings.copy(deliveryPercent = it)) } }
        item { PercentField("Merma de producción (%)", settings.wastePercent) { onSettingsChange(settings.copy(wastePercent = it)) } }
        item { Text("Tip: un margen sano no es lo mismo que marcar 40% sobre el costo. DulceCosto calcula la ganancia sobre tu precio de venta.", color = Color.Gray, fontSize = 13.sp); Spacer(Modifier.height(10.dp)) }
    }
}

@Composable private fun SettingRow(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) { Card(colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(if (title == "IGV") Icons.Default.Percent else Icons.Default.AttachMoney, null, tint = Mint); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, fontWeight = FontWeight.Bold); Text(subtitle, color = Color.Gray, fontSize = 12.sp) }; Switch(checked, onCheckedChange = onChecked) } } }
@Composable private fun PercentField(label: String, value: Double, onChange: (Double) -> Unit) { OutlinedTextField(value.clean(), { onChange((it.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0)) }, label = { Text(label) }, suffix = { Text("%") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
@Composable private fun Metric(label: String, value: String, color: Color) { Column { Text(label, color = color.copy(alpha = .75f), fontSize = 11.sp); Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp) } }
@Composable private fun MiniCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) { Card(modifier, colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(16.dp)) { Column(Modifier.padding(15.dp)) { Icon(icon, null, tint = Mint, modifier = Modifier.size(22.dp)); Spacer(Modifier.height(10.dp)); Text(label, color = Color.Gray, fontSize = 12.sp); Text(value, fontWeight = FontWeight.Bold, fontSize = 17.sp) } } }
@Composable private fun CostRow(label: String, value: Double, strong: Boolean = false, currency: CurrencyOption = CurrencyOptions.first()) { Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal); Text(money(value, currency), fontWeight = if (strong) FontWeight.Bold else FontWeight.Normal, color = if (strong) Mint else Ink) } }
@Composable private fun CurrencyPickerDialog(selected: CurrencyOption, onDismiss: () -> Unit, onSelect: (CurrencyOption) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Moneda de trabajo", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(CurrencyOptions, key = { it.code }) { option ->
                    OutlinedButton(onClick = { onSelect(option) }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (option.code == selected.code) "✓  " else "    ", color = Mint)
                        Text("${option.code} · ${option.name} (${option.symbol})")
                    }
                }
            }
        },
        confirmButton = { OutlinedButton(onClick = onDismiss) { Text("Cerrar") } }
    )
}

@Composable private fun AddSupplyDialog(currency: CurrencyOption, initial: Supply?, nextId: Int, onDismiss: () -> Unit, onSave: (Supply) -> Unit) {
    var name by remember(initial?.id) { mutableStateOf(initial?.name ?: "") }
    var price by remember(initial?.id) { mutableStateOf(initial?.purchasePrice?.clean() ?: "") }
    var qty by remember(initial?.id) { mutableStateOf(initial?.purchaseQuantity?.clean() ?: "") }
    var unit by remember(initial?.id) { mutableStateOf(initial?.unit ?: "g") }
    var category by remember(initial?.id) { mutableStateOf(initial?.category ?: "Base") }
    var unitMenuOpen by remember { mutableStateOf(false) }
    val units = listOf("g", "kg", "ml", "l", "und", "docena", "cm")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nuevo insumo" else "Editar insumo", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ingresa el precio de compra y selecciona su unidad de medida.", color = Color.Gray, fontSize = 13.sp)
                OutlinedTextField(name, { name = it }, label = { Text("Nombre") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(price, { price = it }, label = { Text("Precio ${currency.symbol}") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(qty, { qty = it }, label = { Text("Cantidad") }, modifier = Modifier.weight(1f))
                }
                Text("Tipo de insumo", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Base", "Empaque").forEach { option -> FilterChip(selected = category == option, onClick = { category = option }, label = { Text(option) }) }
                }
                Box {
                    OutlinedButton(onClick = { unitMenuOpen = true }, modifier = Modifier.fillMaxWidth()) { Text("Unidad de medida: $unit") }
                    DropdownMenu(expanded = unitMenuOpen, onDismissRequest = { unitMenuOpen = false }) {
                        units.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { unit = option; unitMenuOpen = false }) }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onSave(Supply(initial?.id ?: nextId, name.ifBlank { "Nuevo insumo" }, category, price.toDoubleOrNull() ?: 0.0, qty.toDoubleOrNull() ?: 1.0, unit, initial?.stockNote ?: "")) }) { Text(if (initial == null) "Guardar" else "Guardar cambios") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
private fun money(value: Double, currency: CurrencyOption) = "${currency.symbol} ${String.format(Locale.US, "%.2f", value)}"
private fun Double.clean() = if (this % 1.0 == 0.0) String.format(Locale.US, "%.0f", this) else String.format(Locale.US, "%.2f", this)
private fun Double.oneDecimal() = String.format(Locale.US, "%.1f", this)
