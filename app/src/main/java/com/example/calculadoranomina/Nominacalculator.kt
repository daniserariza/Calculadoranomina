package com.example.calculadoranomina

import java.text.NumberFormat
import java.util.Locale

// Constantes según la normativa colombiana de septiembre de 2026
const val SMMLV_2026 = 1750905.0 // Decreto 1469 de 2025
const val AUX_TRANSPORTE_2026 = 249095.0 // Decreto 1470 de 2025
const val HORAS_ORDINARIAS_MES = 210.0 // Jornada 42h/semana (Ley 2101 de 2021)

const val PORCENTAJE_SALUD = 0.04 // 4% Ley 100 de 1993
const val PORCENTAJE_PENSION = 0.04 // 4% Ley 100 de 1993
const val PORCENTAJE_FSP = 0.01 // 1% Ley 797 de 2003

// Enumeración para clasificar el rango salarial y cambiar la imagen
enum class RangoSalarial {
    RANGO_1, // <= 2 SMMLV (Tiene auxilio de transporte)
    RANGO_2, // > 2 SMMLV y < 4 SMMLV
    RANGO_3  // >= 4 SMMLV (Aporta al FSP)
}

// Objeto para almacenar el desglose completo del cálculo
data class ResultadoNomina(
    val valorHora: Double,
    val totalHorasExtra: Double,
    val auxilioTransporte: Double,
    val totalDevengado: Double,
    val aporteSalud: Double,
    val aportePension: Double,
    val fondoSolidaridad: Double,
    val totalDeducciones: Double,
    val salarioNeto: Double,
    val rango: RangoSalarial
)
fun clasificarRango(salarioBasico: Double): RangoSalarial {
    return when {
        salarioBasico <= 2 * SMMLV_2026 -> RangoSalarial.RANGO_1
        salarioBasico < 4 * SMMLV_2026 -> RangoSalarial.RANGO_2
        else -> RangoSalarial.RANGO_3
    }
}
fun calcularNomina(
    salarioBasico: Double,
    horasDiurnas: Double,
    horasNocturnas: Double,
    esDominical: Boolean,
    transporteEmpresa: Boolean
): ResultadoNomina {
    // Regla 1: Valor de la hora ordinaria (salarioBasico / 210)
    val valorHora = salarioBasico / HORAS_ORDINARIAS_MES

    // Regla 2: Factores para horas extra (hábil vs dominical/festivo)
    val factorDiurno = if (esDominical) 2.15 else 1.25
    val factorNocturno = if (esDominical) 2.65 else 1.75

    val pagoExtrasDiurnas = horasDiurnas * valorHora * factorDiurno
    val pagoExtrasNocturnas = horasNocturnas * valorHora * factorNocturno
    val totalHorasExtra = pagoExtrasDiurnas + pagoExtrasNocturnas

    // Regla 3: Ingreso Base de Cotización (IBC = básico + horas extra)
    val ibc = salarioBasico + totalHorasExtra

    // Regla 4: Auxilio de Transporte (<= 2 SMMLV y empresa NO suministra transporte)
    val auxilioTransporte = if (salarioBasico <= (2 * SMMLV_2026) && !transporteEmpresa) {
        AUX_TRANSPORTE_2026
    } else {
        0.0
    }

    // Regla 5: Total Devengado
    val totalDevengado = ibc + auxilioTransporte

    // Regla 6: Deducciones (Salud 4%, Pensión 4%, FSP 1% si IBC >= 4 SMMLV)
    val aporteSalud = ibc * PORCENTAJE_SALUD
    val aportePension = ibc * PORCENTAJE_PENSION
    val fondoSolidaridad = if (ibc >= (4 * SMMLV_2026)) ibc * PORCENTAJE_FSP else 0.0
    val totalDeducciones = aporteSalud + aportePension + fondoSolidaridad

    // Regla 7: Salario Neto
    val salarioNeto = totalDevengado - totalDeducciones

    return ResultadoNomina(
        valorHora = valorHora,
        totalHorasExtra = totalHorasExtra,
        auxilioTransporte = auxilioTransporte,
        totalDevengado = totalDevengado,
        aporteSalud = aporteSalud,
        aportePension = aportePension,
        fondoSolidaridad = fondoSolidaridad,
        totalDeducciones = totalDeducciones,
        salarioNeto = salarioNeto,
        rango = clasificarRango(salarioBasico)
    )
}
fun Double.toCopCurrency(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("es", "CO")).apply {
        maximumFractionDigits = 0
    }
    return format.format(this)
}
