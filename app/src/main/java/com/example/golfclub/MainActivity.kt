package com.example.golfclub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.golfclub.ui.theme.GolfClubTheme

// ─── Colores ────────────────────────────────────────────────────────────────
val Navy        = Color(0xFF0D1F3C)
val NavyLight   = Color(0xFF162B50)
val NavyCard    = Color(0xFF1A2E4A)
val Green       = Color(0xFF34D399)
val GreenDeep   = Color(0xFF064E3B)
val TextPri     = Color(0xFFF0FDF4)
val TextMuted   = Color(0x80F0FDF4)
val CardBorder  = Color(0x14FFFFFF)
val GoldAccent  = Color(0xFFFBBF24)
val BlueAccent  = Color(0xFF60A5FA)
val PurpleAccent= Color(0xFFA78BFA)
val RedAccent   = Color(0xFFF87171)

// ─── Modelos de datos (sin Room, en memoria) ──────────────────────────────
enum class Estado { ACTIVA, CANCELADA, COMPLETADA }

data class Reserva(
    val id: Int,
    val cliente: String,
    val cancha: String,
    val fecha: String,
    val hora: String,
    val estado: Estado = Estado.ACTIVA
)

val canchas = listOf("Cancha 1", "Cancha 2", "Cancha 3", "Cancha 4", "Cancha 5")
val horas   = (7..20).flatMap { h -> listOf("%02d:00".format(h), "%02d:30".format(h)) }

// Datos de muestra
val reservasMuestra = mutableListOf(
    Reserva(1, "Carlos López",   "Cancha 1", "20/03/2026", "09:00", Estado.ACTIVA),
    Reserva(2, "Ana Martínez",   "Cancha 3", "20/03/2026", "11:30", Estado.COMPLETADA),
    Reserva(3, "Pedro Gómez",    "Cancha 2", "21/03/2026", "08:00", Estado.ACTIVA),
    Reserva(4, "Laura Ríos",     "Cancha 4", "21/03/2026", "15:00", Estado.CANCELADA),
    Reserva(5, "Sergio Torres",  "Cancha 1", "22/03/2026", "10:00", Estado.ACTIVA),
)

// ─── MainActivity ────────────────────────────────────────────────────────────
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GolfClubTheme {
                GolfApp()
            }
        }
    }
}

// ─── App raíz ─────────────────────────────────────────────────────────────
@Composable
fun GolfApp() {
    var tabActual by remember { mutableStateOf(0) }  // 0=Resumen, 1=Reservas
    var reservas  by remember { mutableStateOf(reservasMuestra.toList()) }
    var nextId    by remember { mutableStateOf(reservasMuestra.size + 1) }

    // Dialogs
    var mostrarFormulario by remember { mutableStateOf(false) }
    var reservaAEditar    by remember { mutableStateOf<Reserva?>(null) }
    var reservaAEliminar  by remember { mutableStateOf<Reserva?>(null) }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = Navy,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = NavyLight,
                tonalElevation = 0.dp
            ) {
                listOf(
                    Triple(0, Icons.Default.Dashboard, "Resumen"),
                    Triple(1, Icons.Default.List,      "Reservas"),
                ).forEach { (idx, icon, label) ->
                    NavigationBarItem(
                        selected = tabActual == idx,
                        onClick  = { tabActual = idx },
                        icon = { Icon(icon, label, modifier = Modifier.size(22.dp)) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = Green,
                            selectedTextColor   = Green,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor      = Green.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (tabActual == 1) {
                FloatingActionButton(
                    onClick        = { reservaAEditar = null; mostrarFormulario = true },
                    containerColor = Green,
                    contentColor   = GreenDeep,
                    shape          = RoundedCornerShape(18.dp)
                ) {
                    Icon(Icons.Default.Add, "Nueva reserva")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (tabActual) {
                0 -> PantallaResumen(reservas)
                1 -> PantallaReservas(
                    reservas    = reservas,
                    onEditar    = { r -> reservaAEditar = r; mostrarFormulario = true },
                    onEliminar  = { r -> reservaAEliminar = r }
                )
            }
        }
    }

    // ── Formulario nueva/editar ──
    if (mostrarFormulario) {
        FormularioDialog(
            reservaExistente = reservaAEditar,
            reservasActuales = reservas,
            onGuardar = { nueva ->
                reservas = if (reservaAEditar != null) {
                    reservas.map { if (it.id == nueva.id) nueva else it }
                } else {
                    reservas + nueva.copy(id = nextId++)
                }
                mostrarFormulario = false
            },
            onCancelar = { mostrarFormulario = false }
        )
    }

    // ── Confirmar eliminar ──
    reservaAEliminar?.let { r ->
        AlertDialog(
            onDismissRequest = { reservaAEliminar = null },
            containerColor   = NavyCard,
            titleContentColor = TextPri,
            textContentColor  = TextMuted,
            title = { Text("Eliminar reserva", fontWeight = FontWeight.SemiBold) },
            text  = { Text("¿Eliminar la reserva de ${r.cliente} en ${r.cancha}?") },
            confirmButton = {
                TextButton(onClick = {
                    reservas = reservas.filter { it.id != r.id }
                    reservaAEliminar = null
                }) { Text("Eliminar", color = RedAccent, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { reservaAEliminar = null }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// PANTALLA 1 · RESUMEN
// ──────────────────────────────────────────────────────────────────────────────
@Composable
fun PantallaResumen(reservas: List<Reserva>) {
    val activas     = reservas.count { it.estado == Estado.ACTIVA }
    val canceladas  = reservas.count { it.estado == Estado.CANCELADA }
    val completadas = reservas.count { it.estado == Estado.COMPLETADA }
    val canchasOcupadas = reservas.filter { it.estado == Estado.ACTIVA }.map { it.cancha }.distinct().size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(listOf(NavyLight, Color(0xFF162040)))
                )
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Column {
                Text("⛳  GOLF CLUB", fontSize = 11.sp, letterSpacing = 2.sp,
                    color = Green, fontWeight = FontWeight.Medium)
                Text("Panel de Ocupación", fontSize = 26.sp,
                    fontWeight = FontWeight.Bold, color = TextPri,
                    modifier = Modifier.padding(top = 4.dp))
                Text("Temporada 2026 · Activo", fontSize = 12.sp, color = Green.copy(alpha = 0.6f))
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {

            // ── Grid de estadísticas ──
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(Modifier.weight(1f), "Activas",    activas.toString(),     Icons.Default.CheckCircle, Green,       Color(0xFF064E3B))
                StatCard(Modifier.weight(1f), "Canceladas", canceladas.toString(),  Icons.Default.Cancel,      GoldAccent,  Color(0xFF451A03))
            }
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(Modifier.weight(1f), "Completadas",   completadas.toString(),   Icons.Default.Done,       BlueAccent,   Color(0xFF1E3A5F))
                StatCard(Modifier.weight(1f), "Canchas en uso",canchasOcupadas.toString(),Icons.Default.GolfCourse, PurpleAccent, Color(0xFF2E1065))
            }

            Spacer(Modifier.height(22.dp))

            // ── Actividad reciente ──
            Text("ACTIVIDAD RECIENTE", fontSize = 11.sp, letterSpacing = 2.sp,
                color = Green, fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 12.dp))

            val recientes = reservas.takeLast(4).reversed()
            if (recientes.isEmpty()) {
                EmptyState("Sin reservas registradas")
            } else {
                recientes.forEach { r ->
                    MiniReservaItem(r)
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(10.dp))

            // ── Total ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NavyCard)
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, null, tint = Green, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Total de reservas", fontWeight = FontWeight.SemiBold,
                        color = TextPri, fontSize = 14.sp)
                    Text("${reservas.size} reservas registradas",
                        fontSize = 12.sp, color = TextMuted)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// PANTALLA 2 · LISTA DE RESERVAS
// ──────────────────────────────────────────────────────────────────────────────
@Composable
fun PantallaReservas(
    reservas: List<Reserva>,
    onEditar: (Reserva) -> Unit,
    onEliminar: (Reserva) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtradas = if (query.isBlank()) reservas
    else reservas.filter { it.cliente.contains(query, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy)
    ) {
        // ── Header ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(NavyLight, Color(0xFF162040))))
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Column {
                Text("LISTADO", fontSize = 11.sp, letterSpacing = 2.sp,
                    color = Green, fontWeight = FontWeight.Medium)
                Text("Reservas", fontSize = 26.sp,
                    fontWeight = FontWeight.Bold, color = TextPri,
                    modifier = Modifier.padding(top = 4.dp))
                Text("${reservas.size} reservas registradas", fontSize = 12.sp,
                    color = Green.copy(alpha = 0.6f))
            }
        }

        // ── Barra de búsqueda ──
        OutlinedTextField(
            value         = query,
            onValueChange = { query = it },
            placeholder   = { Text("Buscar por cliente...", color = TextMuted, fontSize = 14.sp) },
            leadingIcon   = { Icon(Icons.Default.Search, null, tint = Green, modifier = Modifier.size(20.dp)) },
            trailingIcon  = {
                if (query.isNotBlank())
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Default.Clear, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
            },
            modifier      = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            singleLine    = true,
            shape         = RoundedCornerShape(14.dp),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor    = Green,
                unfocusedBorderColor  = CardBorder,
                focusedTextColor      = TextPri,
                unfocusedTextColor    = TextPri,
                focusedContainerColor = NavyCard,
                unfocusedContainerColor = NavyCard,
                cursorColor           = Green,
                focusedLabelColor     = Green
            )
        )

        if (filtradas.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyState(if (query.isBlank()) "No hay reservas aún" else "Sin resultados para \"$query\"")
            }
        } else {
            LazyColumn(
                contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtradas, key = { it.id }) { r ->
                    ReservaCard(reserva = r, onEditar = { onEditar(r) }, onEliminar = { onEliminar(r) })
                }
                item { Spacer(Modifier.height(90.dp)) }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// FORMULARIO (Dialog)
// ──────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioDialog(
    reservaExistente: Reserva?,
    reservasActuales: List<Reserva>,
    onGuardar: (Reserva) -> Unit,
    onCancelar: () -> Unit
) {
    var nombre  by remember { mutableStateOf(reservaExistente?.cliente ?: "") }
    var cancha  by remember { mutableStateOf(reservaExistente?.cancha  ?: canchas[0]) }
    var fecha   by remember { mutableStateOf(reservaExistente?.fecha   ?: "") }
    var hora    by remember { mutableStateOf(reservaExistente?.hora    ?: horas[0]) }
    var estado  by remember { mutableStateOf(reservaExistente?.estado  ?: Estado.ACTIVA) }

    var errorNombre  by remember { mutableStateOf(false) }
    var errorFecha   by remember { mutableStateOf(false) }
    var errorConfl   by remember { mutableStateOf(false) }

    var exCancha by remember { mutableStateOf(false) }
    var exHora   by remember { mutableStateOf(false) }
    var exEstado by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onCancelar) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(NavyLight)
                .border(1.dp, CardBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Título
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Green.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (reservaExistente == null) Icons.Default.Add else Icons.Default.Edit,
                        null, tint = Green, modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        if (reservaExistente == null) "Nueva Reserva" else "Editar Reserva",
                        fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextPri
                    )
                    Text("Completa los campos", fontSize = 12.sp, color = TextMuted)
                }
            }

            HorizontalDivider(color = CardBorder)

            // Nombre
            FormField(
                value       = nombre,
                onChange    = { nombre = it; errorNombre = false; errorConfl = false },
                label       = "Nombre del cliente",
                icon        = Icons.Default.Person,
                isError     = errorNombre,
                placeholder = "Ej: Carlos López"
            )
            if (errorNombre) ErrorText("Campo requerido")

            // Cancha
            DropdownField("Cancha", cancha, Icons.Default.GolfCourse, canchas, exCancha,
                onExpand = { exCancha = it }, onSelect = { cancha = it; errorConfl = false })

            // Fecha
            FormField(
                value       = fecha,
                onChange    = { fecha = it; errorFecha = false; errorConfl = false },
                label       = "Fecha (dd/MM/yyyy)",
                icon        = Icons.Default.CalendarToday,
                isError     = errorFecha || errorConfl,
                placeholder = "Ej: 20/03/2026"
            )
            if (errorFecha) ErrorText("Ingresa una fecha válida")

            // Hora
            DropdownField("Hora", hora, Icons.Default.Schedule, horas, exHora,
                onExpand = { exHora = it }, onSelect = { hora = it; errorConfl = false },
                maxHeight = 180.dp)

            // Estado (solo en edición)
            if (reservaExistente != null) {
                DropdownField(
                    label    = "Estado",
                    valor    = estado.name.lowercase().replaceFirstChar { it.uppercase() },
                    icon     = Icons.Default.Info,
                    opciones = Estado.entries.map { it.name.lowercase().replaceFirstChar { c -> c.uppercase() } },
                    expanded = exEstado,
                    onExpand = { exEstado = it },
                    onSelect = { sel -> estado = Estado.valueOf(sel.uppercase()) }
                )
            }

            if (errorConfl) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(RedAccent.copy(alpha = 0.12f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, null, tint = RedAccent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ya existe una reserva activa en $cancha el $fecha a las $hora",
                        color = RedAccent, fontSize = 12.sp)
                }
            }

            HorizontalDivider(color = CardBorder)

            // Botones
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onCancelar,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape    = RoundedCornerShape(14.dp),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                ) { Text("Cancelar", fontSize = 13.sp) }

                Button(
                    onClick = {
                        errorNombre = nombre.isBlank()
                        errorFecha  = fecha.isBlank()
                        if (errorNombre || errorFecha) return@Button

                        // Validar conflicto
                        val conflicto = reservasActuales.any { r ->
                            r.cancha == cancha &&
                                    r.fecha  == fecha  &&
                                    r.hora   == hora   &&
                                    r.estado == Estado.ACTIVA &&
                                    r.id     != (reservaExistente?.id ?: -1)
                        }
                        if (conflicto) { errorConfl = true; return@Button }

                        onGuardar(
                            Reserva(
                                id      = reservaExistente?.id ?: 0,
                                cliente = nombre.trim(),
                                cancha  = cancha,
                                fecha   = fecha,
                                hora    = hora,
                                estado  = estado
                            )
                        )
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = Green, contentColor = GreenDeep)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (reservaExistente == null) "Guardar" else "Actualizar",
                        fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// COMPONENTES REUTILIZABLES
// ──────────────────────────────────────────────────────────────────────────────

@Composable
fun StatCard(
    modifier: Modifier,
    titulo: String,
    valor: String,
    icono: ImageVector,
    accentColor: Color,
    bgColor: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor.copy(alpha = 0.75f))
            .border(1.dp, accentColor.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Icon(icono, titulo, tint = accentColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(10.dp))
        Text(valor, fontSize = 30.sp, fontWeight = FontWeight.Bold, color = accentColor, lineHeight = 32.sp)
        Text(titulo.uppercase(), fontSize = 10.sp, letterSpacing = 1.sp,
            fontWeight = FontWeight.Medium, color = TextMuted)
    }
}

@Composable
fun EstadoBadge(estado: Estado) {
    val (bg, fg) = when (estado) {
        Estado.ACTIVA      -> Color(0xFF065F46) to Green
        Estado.CANCELADA   -> Color(0xFF501313) to RedAccent
        Estado.COMPLETADA  -> Color(0xFF1E3A5F) to BlueAccent
    }
    Surface(color = bg, shape = RoundedCornerShape(20.dp)) {
        Text(
            estado.name, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.8.sp, color = fg
        )
    }
}

@Composable
fun MiniReservaItem(reserva: Reserva) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NavyCard)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                .background(Green.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.GolfCourse, null, tint = Green, modifier = Modifier.size(18.dp)) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(reserva.cliente, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextPri)
            Text("${reserva.cancha}  ·  ${reserva.fecha}  ${reserva.hora}",
                fontSize = 11.sp, color = TextMuted)
        }
        EstadoBadge(reserva.estado)
    }
}

@Composable
fun ReservaCard(reserva: Reserva, onEditar: () -> Unit, onEliminar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(NavyCard)
            .border(1.dp, CardBorder, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                .background(Green.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.Default.GolfCourse, null, tint = Green, modifier = Modifier.size(22.dp)) }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(reserva.cliente, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
                color = TextPri, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${reserva.cancha}  ·  ${reserva.fecha}  ${reserva.hora}",
                fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(top = 2.dp))
            Spacer(Modifier.height(8.dp))
            EstadoBadge(reserva.estado)
        }

        Column(horizontalAlignment = Alignment.End) {
            IconButton(
                onClick = onEditar,
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                    .background(BlueAccent.copy(alpha = 0.12f))
            ) { Icon(Icons.Default.Edit, "Editar", tint = BlueAccent, modifier = Modifier.size(16.dp)) }
            Spacer(Modifier.height(6.dp))
            IconButton(
                onClick = onEliminar,
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                    .background(RedAccent.copy(alpha = 0.12f))
            ) { Icon(Icons.Default.Delete, "Eliminar", tint = RedAccent, modifier = Modifier.size(16.dp)) }
        }
    }
}

@Composable
fun EmptyState(mensaje: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.GolfCourse, null, modifier = Modifier.size(52.dp),
            tint = Green.copy(alpha = 0.25f))
        Spacer(Modifier.height(10.dp))
        Text(mensaje, color = TextMuted, fontSize = 14.sp)
    }
}

@Composable
fun ErrorText(msg: String) {
    Text(msg, color = RedAccent, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
}

@Composable
fun FormField(
    value: String, onChange: (String) -> Unit, label: String,
    icon: ImageVector, isError: Boolean = false, placeholder: String = ""
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onChange,
        label         = { Text(label, fontSize = 13.sp) },
        leadingIcon   = { Icon(icon, null, tint = Green, modifier = Modifier.size(18.dp)) },
        placeholder   = { Text(placeholder, color = TextMuted, fontSize = 13.sp) },
        isError       = isError,
        singleLine    = true,
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(14.dp),
        colors        = golfFieldColors()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String, valor: String, icon: ImageVector,
    opciones: List<String>, expanded: Boolean,
    onExpand: (Boolean) -> Unit, onSelect: (String) -> Unit,
    maxHeight: androidx.compose.ui.unit.Dp = androidx.compose.ui.unit.Dp.Unspecified
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpand) {
        OutlinedTextField(
            value         = valor,
            onValueChange = {},
            readOnly      = true,
            label         = { Text(label, fontSize = 13.sp) },
            leadingIcon   = { Icon(icon, null, tint = Green, modifier = Modifier.size(18.dp)) },
            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier      = Modifier.fillMaxWidth().menuAnchor(),
            shape         = RoundedCornerShape(14.dp),
            colors        = golfFieldColors()
        )
        ExposedDropdownMenu(
            expanded          = expanded,
            onDismissRequest  = { onExpand(false) },
            modifier          = Modifier.background(NavyLight).then(
                if (maxHeight != androidx.compose.ui.unit.Dp.Unspecified)
                    Modifier.heightIn(max = maxHeight) else Modifier
            )
        ) {
            opciones.forEach { opcion ->
                DropdownMenuItem(
                    text    = { Text(opcion, color = TextPri, fontSize = 14.sp) },
                    onClick = { onSelect(opcion); onExpand(false) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun golfFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = Green,
    unfocusedBorderColor    = CardBorder,
    focusedLabelColor       = Green,
    unfocusedLabelColor     = TextMuted,
    focusedTextColor        = TextPri,
    unfocusedTextColor      = TextPri,
    focusedContainerColor   = NavyCard,
    unfocusedContainerColor = NavyCard,
    cursorColor             = Green,
    errorBorderColor        = RedAccent,
    errorLabelColor         = RedAccent
)

// ── Preview ──────────────────────────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFF0D1F3C)
@Composable
fun AppPreview() {
    GolfClubTheme {
        GolfApp()
    }
}