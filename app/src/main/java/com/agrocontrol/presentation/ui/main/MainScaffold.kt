package com.agrocontrol.presentation.ui.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.agrocontrol.presentation.navigation.Screen
import com.agrocontrol.presentation.theme.PlusJakartaSansFamily
import com.agrocontrol.presentation.theme.Verde60

data class BottomNavItem(
    val route: String,
    val label: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector,
    val badgeCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavController,
    alertasCount: Int = 0,
    isOffline: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Dashboard.route,  "Inicio",    Icons.Filled.Home,        Icons.Outlined.Home),
        BottomNavItem(Screen.Cultivo.route,    "Cultivo",   Icons.Filled.Agriculture, Icons.Outlined.Agriculture),
        BottomNavItem(Screen.Clima.route,      "Clima",     Icons.Filled.WbSunny,     Icons.Outlined.WbSunny),
        BottomNavItem(Screen.Inventario.route, "Inventario",Icons.Filled.Inventory2,  Icons.Outlined.Inventory2),
        BottomNavItem(Screen.ChatIA.route,     "Asistente", Icons.Filled.SmartToy,    Icons.Outlined.SmartToy),
    )

    val isImeVisible = WindowInsets.ime.getBottom(androidx.compose.ui.platform.LocalDensity.current) > 0
    val showBottomNav = (currentRoute in bottomNavItems.map { it.route }) && !isImeVisible

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomNav,
                enter = slideInVertically { it } + fadeIn(tween(300)),
                exit  = slideOutVertically { it } + fadeOut(tween(200))
            ) {
                AgroBottomBar(
                    items        = bottomNavItems,
                    currentRoute = currentRoute,
                    alertasCount = alertasCount,
                    onItemClick  = { item ->
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            content(padding)
            com.agrocontrol.presentation.ui.components.OfflineBanner(
                isOffline = isOffline,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp)
            )
        }
    }
}

// ─── Paleta local (evita acoplamiento con el tema) ───────────────────────────
private val NavSurface       = Color(0xFF161B22)   // gris oscuro tipo Dock iOS
private val IndicatorActive  = Color(0xFF4ADE80)   // verde neón vibrante
private val IconActive       = Color(0xFF161B22)   // contraste
private val IconInactive     = Color(0xFF9CA3AF)   // gris neutro
private val LabelActive      = Color(0xFF4ADE80)
private val LabelInactive    = Color(0xFF6B7280)
private val BadgeBg          = Color(0xFFEF4444)
private val BadgeText        = Color.White

@Composable
private fun AgroBottomBar(
    items        : List<BottomNavItem>,
    currentRoute : String?,
    alertasCount : Int,
    onItemClick  : (BottomNavItem) -> Unit
) {
    // Contenedor principal flotante
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp) // Efecto flotante
    ) {
        // Dock redondeado
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp, 
                    shape = RoundedCornerShape(36.dp), 
                    spotColor = Color(0x33000000), 
                    ambientColor = Color(0x11000000)
                )
                .clip(RoundedCornerShape(36.dp))
                .background(NavSurface)
                .padding(horizontal = 8.dp, vertical = 8.dp) // Padding interior
        ) {
            Row(
                modifier            = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment   = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    val badge      = if (item.route == Screen.Dashboard.route) alertasCount else 0

                    AgroNavItem(
                        item       = item,
                        isSelected = isSelected,
                        badge      = badge,
                        onClick    = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RowScope.AgroNavItem(
    item       : BottomNavItem,
    isSelected : Boolean,
    badge      : Int,
    onClick    : () -> Unit
) {
    // Animación suave del ancho del indicador
    val indicatorWidth by animateDpAsState(
        targetValue  = if (isSelected) 56.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "indicatorWidth"
    )

    val iconScale by animateFloatAsState(
        targetValue  = if (isSelected) 1.12f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )

    val labelAlpha by animateFloatAsState(
        targetValue  = if (isSelected) 1f else 0.55f,
        animationSpec = tween(200),
        label = "labelAlpha"
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, 
                onClick = onClick
            )
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Píldora indicador + icono
        Box(contentAlignment = Alignment.Center) {
            // Indicador en forma de píldora detrás del icono
            Box(
                modifier = Modifier
                    .width(indicatorWidth)
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isSelected) IndicatorActive
                        else Color.Transparent
                    )
            )

            // Icono con posible badge
            if (badge > 0) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = BadgeBg,
                            contentColor   = BadgeText
                        ) {
                            Text(
                                text       = "$badge",
                                fontSize   = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector     = if (isSelected) item.iconSelected else item.iconUnselected,
                        contentDescription = item.label,
                        modifier        = Modifier
                            .size(20.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            },
                        tint            = if (isSelected) IconActive else IconInactive
                    )
                }
            } else {
                Icon(
                    imageVector     = if (isSelected) item.iconSelected else item.iconUnselected,
                    contentDescription = item.label,
                    modifier        = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        },
                    tint            = if (isSelected) IconActive else IconInactive
                )
            }
        }
        
        Text(
            text       = item.label,
            fontSize   = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontFamily = PlusJakartaSansFamily,
            color      = if (isSelected) LabelActive else LabelInactive,
            modifier   = Modifier.graphicsLayer { alpha = labelAlpha }
        )
    }
}
