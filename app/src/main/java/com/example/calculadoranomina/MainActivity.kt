package com.example.calculadoranomina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        PantallaNomina()
                    }
                }
            }
        }
    }
}

@Composable
fun PantallaNomina() {
    // 1. Variables de Estado
    var salarioBasicoInput by remember { mutableStateOf("") }
    var horasDiurnasInput by remember { mutableStateOf("") }
    var horasNocturnasInput by remember { mutableStateOf("") }
    var esDominical by remember { mutableStateOf(false) }
    var transporteEmpresa by remember { mutableStateOf(false) }

    // 2. Resultados y errores
    var resultadoNomina by remember { mutableStateOf<ResultadoNomina?>(null) }
    var mensajeError by remember { mutableStateOf("") }

    fun ejecutarCalculo() {
        val salario = salarioBasicoInput.toDoubleOrNull()
        if (salario == null) {
            mensajeError = "Ingrese un salario válido"
            resultadoNomina = null
            return
        }
        if (salario < SMMLV_2026) {
            mensajeError = "El salario no puede ser inferior al mínimo ($ 1.750.905)"
            resultadoNomina = null
            return
        }

        val hDiurnas = if (horasDiurnasInput.isBlank()) 0.0 else horasDiurnasInput.toDoubleOrNull()
        val hNocturnas = if (horasNocturnasInput.isBlank()) 0.0 else horasNocturnasInput.toDoubleOrNull()

        if (hDiurnas == null || hDiurnas < 0 || hNocturnas == null || hNocturnas < 0) {
            mensajeError = "Las horas extra deben ser un número mayor o igual a cero"
            resultadoNomina = null
            return
        }

        if ((hDiurnas + hNocturnas) > 48) {
            mensajeError = "El total de horas extra no puede superar 48 en el mes"
            resultadoNomina = null
            return
        }

        mensajeError = ""
        resultadoNomina = calcularNomina(
            salarioBasico = salario,
            horasDiurnas = hDiurnas,
            horasNocturnas = hNocturnas,
            esDominical = esDominical,
            transporteEmpresa = transporteEmpresa
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium
        )

        // F1: Campo Salario
        CampoNumerico(
            etiqueta = R.string.salario_basico,
            valor = salarioBasicoInput,
            onValueChange = { salarioBasicoInput = it },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            isError = mensajeError.isNotEmpty()
        )

        // F2: Horas Extra Diurnas
        CampoNumerico(
            etiqueta = R.string.horas_diurnas,
            valor = horasDiurnasInput,
            onValueChange = { horasDiurnasInput = it },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )

        // F3: Horas Extra Nocturnas
        CampoNumerico(
            etiqueta = R.string.horas_nocturnas,
            valor = horasNocturnasInput,
            onValueChange = { horasNocturnasInput = it },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            )
        )

        // F4 y F5: Interruptores
        FilaInterruptor(
            etiqueta = R.string.es_dominical,
            checked = esDominical,
            onCheckedChange = {
                esDominical = it
                if (resultadoNomina != null) ejecutarCalculo()
            }
        )

        FilaInterruptor(
            etiqueta = R.string.transporte_empresa,
            checked = transporteEmpresa,
            onCheckedChange = {
                transporteEmpresa = it
                if (resultadoNomina != null) ejecutarCalculo()
            }
        )

        // F10: Mensaje de error
        if (mensajeError.isNotEmpty()) {
            Text(
                text = mensajeError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // F6 & F11: Botones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { ejecutarCalculo() },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.btn_calcular))
            }

            OutlinedButton(
                onClick = {
                    salarioBasicoInput = ""
                    horasDiurnasInput = ""
                    horasNocturnasInput = ""
                    esDominical = false
                    transporteEmpresa = false
                    resultadoNomina = null
                    mensajeError = ""
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.btn_limpiar))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // F7, F8 y F9: Resultados
        resultadoNomina?.let { res ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stringResource(R.string.seccion_devengado),
                        style = MaterialTheme.typography.titleMedium
                    )
                    FilaResultado(R.string.lbl_valor_hora, res.valorHora.toCopCurrency())
                    FilaResultado(R.string.lbl_horas_extra, res.totalHorasExtra.toCopCurrency())
                    FilaResultado(R.string.lbl_aux_transporte, res.auxilioTransporte.toCopCurrency())
                    FilaResultado(R.string.lbl_total_devengado, res.totalDevengado.toCopCurrency(), esNegrita = true)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.seccion_deducciones),
                        style = MaterialTheme.typography.titleMedium
                    )
                    FilaResultado(R.string.lbl_salud, res.aporteSalud.toCopCurrency())
                    FilaResultado(R.string.lbl_pension, res.aportePension.toCopCurrency())
                    FilaResultado(R.string.lbl_fsp, res.fondoSolidaridad.toCopCurrency())
                    FilaResultado(R.string.lbl_total_deducciones, res.totalDeducciones.toCopCurrency(), esNegrita = true)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.lbl_salario_neto),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = res.salarioNeto.toCopCurrency(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val textoRango = when (res.rango) {
                        RangoSalarial.RANGO_1 -> R.string.rango_1_desc
                        RangoSalarial.RANGO_2 -> R.string.rango_2_desc
                        RangoSalarial.RANGO_3 -> R.string.rango_3_desc
                    }
                    Text(
                        text = stringResource(textoRango),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CampoNumerico(
    @StringRes etiqueta: Int,
    valor: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValueChange,
        label = { Text(text = stringResource(id = etiqueta)) },
        singleLine = true,
        isError = isError,
        keyboardOptions = keyboardOptions,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun FilaInterruptor(
    @StringRes etiqueta: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(id = etiqueta),
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun FilaResultado(@StringRes etiqueta: Int, valor: String, esNegrita: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = stringResource(etiqueta))
        Text(
            text = valor,
            fontWeight = if (esNegrita) FontWeight.Bold else null
        )
    }
}