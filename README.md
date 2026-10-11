# DulceCosto

Calculadora de costos de producción para dulcerías peruanas, construida con **Kotlin + Jetpack Compose**.

## Rebanada funcional incluida

- Catálogo inicial de insumos con presentación de compra: `1 kg = S/ 4.00` se convierte a `S/ 0.004 por gramo`.
- Alta y eliminación de insumos.
- Constructor de receta con cantidades por unidad de medida.
- Costo de ingredientes, merma, mano de obra y empaque.
- IGV opcional y comisión de delivery opcional.
- Precio de venta editable, ganancia neta, margen real y punto de equilibrio.
- Motor de cálculo aislado en `domain/Costing.kt`, con pruebas unitarias.

## Fórmulas

```text
costo unitario = precio de compra / cantidad comprada
costo ingredientes = suma(costo unitario × cantidad usada)
merma = costo ingredientes × merma%
base = ingredientes + merma + mano de obra + empaque
IGV = base × IGV% (si está activo)
delivery = precio de venta × comisión% + fijo (si está activo)
costo total = base + IGV + delivery
ganancia = precio de venta - costo total
margen = ganancia / precio de venta
```

## Ejecutar

Requiere Android Studio actualizado, JDK 17 y Android SDK 35.

```bash
./gradlew test
./gradlew assembleDebug
```

Instala el APK debug generado en `app/build/outputs/apk/debug/app-debug.apk`.

## Nota de alcance

Esta primera entrega mantiene el estado en memoria para validar el flujo de negocio. El siguiente incremento recomendado es persistencia local con Room, exportación de ficha de costo y edición de recetas guardadas.
