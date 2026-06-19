package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BankCard
import com.example.data.model.CardTransaction
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CardViewModel
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposePath
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Construct ViewModel utilizing simple manual Factory dependency injection
        val app = application as CardApplication
        val viewModel: CardViewModel by viewModels {
            CardViewModel.Factory(app.repository)
        }

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DashboardScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: CardViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Observers
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    val selectedCard by viewModel.selectedCard.collectAsStateWithLifecycle()
    val selectedTransactions by viewModel.selectedTransactions.collectAsStateWithLifecycle()

    // Dialog trigger states
    var showAddCardDialog by remember { mutableStateOf(false) }
    var showAddTxDialog by remember { mutableStateOf(false) }
    var txDialogType by remember { mutableStateOf("EXPENSE") } // "EXPENSE" or "INCOME"
    var showDeleteCardConfirm by remember { mutableStateOf(false) }

    // Floating UI notification overlay (Success cues)
    var successToastMessage by remember { mutableStateOf("") }
    LaunchedEffect(successToastMessage) {
        if (successToastMessage.isNotEmpty()) {
            Toast.makeText(context, successToastMessage, Toast.LENGTH_SHORT).show()
            successToastMessage = ""
        }
    }

    GlassBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // 1. Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Silhouette / Avatar inside liquid orb (Cyan to Blue gradient per spec)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF06B6D4), Color(0xFF2563EB))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User Avatar",
                            modifier = Modifier.size(28.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Good Morning",
                            color = Color(0xFF94A3B8), // slate-400
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Julian Vance",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.testTag("user_name_text")
                        )
                    }
                }

                // Add Card Trigger Button
                LiquidGlassIconButton(
                    onClick = { showAddCardDialog = true },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AddCard,
                            contentDescription = "Add bank card",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    testTag = "add_card_trigger"
                )
            }

            // 2. Combined Balance Card (Liquid Glass style per Vibrant Palette Spec)
            val combinedBalance = if (cards.isEmpty()) 0.0 else cards.sumOf { it.balance }
            val ccySymbol = selectedCard?.currencySymbol ?: "$"
            
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
                    .testTag("combined_balance_card"),
                shape = RoundedCornerShape(32.dp),
                backgroundBrush = Brush.verticalGradient(
                    colors = listOf(Color(0x1AFFFFFF), Color(0x05FFFFFF))
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    // Decorative Liquid Glow elements inside card to match HTML
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 30.dp, y = (-30).dp)
                            .size(120.dp)
                            .background(Color(0x2406B6D4), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset(x = (-30).dp, y = 30.dp)
                            .size(120.dp)
                            .background(Color(0x1F8B5CF6), CircleShape)
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "COMBINED BALANCE",
                            color = Color(0xFF94A3B8), // slate-400
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = formatCurrency(combinedBalance, ccySymbol),
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1).sp,
                            modifier = Modifier.testTag("combined_balance_value")
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Positive trending badge
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color(0x1F10B981)) // green-500/10
                                    .border(1.dp, Color(0x3310B981), RoundedCornerShape(100.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = "Trending Up",
                                    tint = Color(0xFF34D399), // green-400
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "+4.2%",
                                    color = Color(0xFF34D399),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Currency descriptor
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color(0x0FFFFFFF))
                                    .border(1.dp, Color(0x19FFFFFF), RoundedCornerShape(100.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "USD / EUR",
                                    color = Color(0xFFCBD5E1), // slate-300
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // 3. Card Carousel/Reel Selection
            Text(
                text = "Your Accounts & Cards",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
            )

            if (cards.isEmpty()) {
                // Empty state for Zero Cards
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .height(180.dp),
                    content = {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet icon",
                                tint = GlassPrimaryAccent,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Card Accounts Linked",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Tap the top right icon to link a card",
                                color = Color(0xBBFFFFFF),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    testTag = "cards_empty_state"
                )
            } else {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(cards, key = { it.id }) { item ->
                        val isSelected = selectedCard?.id == item.id
                        // Animate responsive card layout scaling for snappy feedback loop
                        val scaleAnimate by animateFloatAsState(
                            targetValue = if (isSelected) 1.0f else 0.88f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "ReelCardScale"
                        )
                        
                        val strokeAlpha by animateFloatAsState(
                            targetValue = if (isSelected) 0.8f else 0.15f,
                            label = "ReelCardStroke"
                        )

                        BankAccountGlassCard(
                            card = item,
                            isSelected = isSelected,
                            modifier = Modifier
                                .width(280.dp)
                                .height(165.dp)
                                .scale(scaleAnimate)
                                .shadow(
                                    elevation = if (isSelected) 16.dp else 2.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    ambientColor = Color(0xFF4CA1AF),
                                    spotColor = Color(0xFF4CA1AF)
                                )
                                .clickable { viewModel.selectCard(item.id) },
                            strokeAlpha = strokeAlpha
                        )
                    }
                }
            }

            // 3. Current Selected Account Control Hub
            selectedCard?.let { activeCard ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    // Beautiful balance displays
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        backgroundBrush = Brush.verticalGradient(
                            colors = listOf(Color(0x1F2A2A40), Color(0x0A101016))
                        ),
                        testTag = "selected_card_management_card"
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Text(
                                text = "DEPOSITED BALANCE",
                                color = GlassPrimaryAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Running balance
                            Text(
                                text = formatCurrency(activeCard.balance, activeCard.currencySymbol),
                                color = Color.White,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.SansSerif,
                                modifier = Modifier.testTag("animated_balance_text")
                            )

                            Text(
                                text = "${activeCard.bankName} Account • ${activeCard.cardType}",
                                color = Color(0xBBFFFFFF),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Management Action Grid (Check & Manage Separately)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Add Funds (Income) Button
                                LiquidGlassButton(
                                    onClick = {
                                        txDialogType = "INCOME"
                                        showAddTxDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = listOf(Color(0x1A4CAF50), Color(0x054CAF50)),
                                    borderColors = listOf(Color(0x8081C784), Color(0x1181C784)),
                                    testTag = "add_funds_button"
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = "Income",
                                        tint = Color(0xFF81C784),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Funds", color = Color(0xFFE8F5E9), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                // Record Spend (Expense) Button
                                LiquidGlassButton(
                                    onClick = {
                                        txDialogType = "EXPENSE"
                                        showAddTxDialog = true
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = listOf(Color(0x1AE53935), Color(0x05E53935)),
                                    borderColors = listOf(Color(0x80EF5350), Color(0x11EF5350)),
                                    testTag = "spend_funds_button"
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingDown,
                                        contentDescription = "Expense",
                                        tint = Color(0xFFEF5350),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Record Spend", color = Color(0xFFFFEBEE), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Secondary Quick Management Options
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Delete card account",
                                    color = Color(0x66FFFFFF),
                                    fontSize = 11.sp,
                                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                                    modifier = Modifier
                                        .clickable { showDeleteCardConfirm = true }
                                        .testTag("delete_card_text_button")
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "Card Status: Active",
                                    color = Color(0xFF81C784),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // 4. Transaction history section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Transaction History",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${selectedTransactions.size} entries",
                            color = Color(0x99FFFFFF),
                            fontSize = 12.sp
                        )
                    }

                    if (selectedTransactions.isEmpty()) {
                        // Empty states for transactions
                        LiquidGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            backgroundBrush = Brush.verticalGradient(
                                colors = listOf(Color(0x0EFFFFFF), Color(0x02FFFFFF))
                            ),
                            testTag = "tx_empty_state"
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = Color(0x33FFFFFF),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "No recorded visual history",
                                    color = Color(0x66FFFFFF),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Add custom funds or spends above!",
                                    color = Color(0x33FFFFFF),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        // Historical Transaction Feed List
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedTransactions.forEach { tx ->
                                TransactionGlassItem(
                                    transaction = tx,
                                    currencySymbol = activeCard.currencySymbol,
                                    onDelete = { viewModel.deleteTransaction(tx) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ============================================
        // DIALOG: LINK / ADD NEW BANK ACCOUNT
        // ============================================
        if (showAddCardDialog) {
            Dialog(
                onDismissRequest = { showAddCardDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xCE07080A))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showAddCardDialog = false },
                    contentAlignment = Alignment.Center
                ) {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .wrapContentHeight()
                            .clickable(enabled = false) {}, // Intercept click
                        shape = RoundedCornerShape(28.dp),
                        borderColors = listOf(Color(0x66FFFFFF), Color(0x13FFFFFF)),
                        backgroundBrush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF141722), Color(0xFF0F1118))
                        )
                    ) {
                        var bName by remember { mutableStateOf("") }
                        var bHolder by remember { mutableStateOf("") }
                        var bNumber by remember { mutableStateOf("") }
                        var bType by remember { mutableStateOf("Visa") }
                        var bBalance by remember { mutableStateOf("") }
                        var bExp by remember { mutableStateOf("") }
                        var bStyle by remember { mutableStateOf(0) }

                        val styleColors = listOf("#0C2340", "#11998E", "#8A2387", "#F12711")

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Link Bank Card",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                LiquidGlassIconButton(
                                    onClick = { showAddCardDialog = false },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    testTag = "add_card_dialog_close"
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Inputs
                            LiquidGlassTextField(
                                value = bName,
                                onValueChange = { bName = it },
                                label = "Bank Name (e.g. Chase, Barclays)",
                                placeholder = "Enter bank name",
                                leadingIcon = { Icon(Icons.Default.AccountBalance, null, tint = Color(0x88FFFFFF)) },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "add_card_bank_name_input"
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            LiquidGlassTextField(
                                value = bHolder,
                                onValueChange = { bHolder = it },
                                label = "Cardholder Name",
                                placeholder = "ALEX MERCER",
                                leadingIcon = { Icon(Icons.Default.Person, null, tint = Color(0x88FFFFFF)) },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "add_card_holder_input"
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                LiquidGlassTextField(
                                    value = bNumber,
                                    onValueChange = { bNumber = it },
                                    label = "Card Number",
                                    placeholder = "last 4 digits",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    leadingIcon = { Icon(Icons.Default.CreditCard, null, tint = Color(0x88FFFFFF)) },
                                    modifier = Modifier.weight(1.3f),
                                    testTag = "add_card_number_input"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                LiquidGlassTextField(
                                    value = bExp,
                                    onValueChange = { bExp = it },
                                    label = "Expiry",
                                    placeholder = "MM/YY",
                                    modifier = Modifier.weight(0.7f),
                                    testTag = "add_card_expiry_input"
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                LiquidGlassTextField(
                                    value = bBalance,
                                    onValueChange = { bBalance = it },
                                    label = "Current Balance ($)",
                                    placeholder = "5000",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = Color(0x88FFFFFF)) },
                                    modifier = Modifier.weight(1f),
                                    testTag = "add_card_balance_input"
                                )
                                
                                // Selection of Visa, MasterCard, Amex
                                Box(modifier = Modifier.weight(1f).height(54.dp)) {
                                    val types = listOf("Visa", "Mastercard", "Express")
                                    var expanded by remember { mutableStateOf(false) }
                                    
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(Color(0x0EFFFFFF))
                                            .border(BorderStroke(1.dp, Color(0x22FFFFFF)), RoundedCornerShape(16.dp))
                                            .clickable { expanded = true }
                                            .padding(horizontal = 14.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(bType, color = Color.White, fontSize = 13.sp)
                                            Icon(Icons.Default.ArrowDropDown, null, tint = Color.White)
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        modifier = Modifier.background(Color(0xFF141722))
                                    ) {
                                        types.forEach { type ->
                                            DropdownMenuItem(
                                                text = { Text(type, color = Color.White) },
                                                onClick = {
                                                    bType = type
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Customize card fluid backdrop styles (linear index styles colors selectors)
                            Text(
                                "Choose Card Style",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                styleColors.forEachIndexed { idx, col ->
                                    val isColSelected = bStyle == idx
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(col)))
                                            .border(
                                                width = if (isColSelected) 3.dp else 0.dp,
                                                color = if (isColSelected) Color.White else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { bStyle = idx }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Create button
                            LiquidGlassButton(
                                onClick = {
                                    val amountDouble = bBalance.toDoubleOrNull()
                                    if (bName.isBlank() || bHolder.isBlank() || bNumber.isBlank() || amountDouble == null) {
                                        Toast.makeText(context, "Please configure all card fields correctly.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.addCard(
                                            bankName = bName,
                                            cardHolder = bHolder,
                                            cardNumber = bNumber,
                                            cardType = bType,
                                            balance = amountDouble,
                                            expiryDate = if (bExp.isBlank()) "12/28" else bExp,
                                            styleIndex = bStyle,
                                            colorHex = styleColors[bStyle]
                                        )
                                        showAddCardDialog = false
                                        successToastMessage = "Linked credit card successfully!"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = listOf(Color(0x3B4CA1AF), Color(0x114CA1AF)),
                                borderColors = listOf(Color(0xFF4CA1AF), Color(0x224CA1AF)),
                                testTag = "add_card_submit_button"
                            ) {
                                Icon(Icons.Default.Check, null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Link / Register Account", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ============================================
        // DIALOG: ADD TRANSACTION
        // ============================================
        if (showAddTxDialog) {
            Dialog(
                onDismissRequest = { showAddTxDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xCE07080A))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showAddTxDialog = false },
                    contentAlignment = Alignment.Center
                ) {
                    LiquidGlassCard(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .wrapContentHeight()
                            .clickable(enabled = false) {}, // Intercept click
                        shape = RoundedCornerShape(28.dp),
                        borderColors = listOf(Color(0x66FFFFFF), Color(0x13FFFFFF)),
                        backgroundBrush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF141722), Color(0xFF0F1118))
                        )
                    ) {
                        var txTitle by remember { mutableStateOf("") }
                        var txAmount by remember { mutableStateOf("") }
                        var txCategory by remember { mutableStateOf("Food") }

                        val categories = listOf("Food", "Shopping", "Entertainment", "Utilities", "Salary", "Transport", "Other")

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (txDialogType == "INCOME") "Add Income Funds" else "Record Spend / Charge",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                LiquidGlassIconButton(
                                    onClick = { showAddTxDialog = false },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    testTag = "add_tx_dialog_close"
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            LiquidGlassTextField(
                                value = txTitle,
                                onValueChange = { txTitle = it },
                                label = "Label/Description",
                                placeholder = "e.g., Apple Store, Utilities, Gift",
                                leadingIcon = { Icon(Icons.Default.Label, null, tint = Color(0x88FFFFFF)) },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "add_tx_title_input"
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LiquidGlassTextField(
                                    value = txAmount,
                                    onValueChange = { txAmount = it },
                                    label = "Amount ($)",
                                    placeholder = "0.00",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    leadingIcon = { Icon(Icons.Default.AttachMoney, null, tint = Color(0x88FFFFFF)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    testTag = "add_tx_amount_input"
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "Category",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )

                            // Scrollable Row of visual pills category selects
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                categories.forEach { cat ->
                                    val isCatSelected = txCategory == cat
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isCatSelected) GlassPrimaryAccent else Color(0x0EFFFFFF))
                                            .clickable { txCategory = cat }
                                            .padding(vertical = 8.dp, horizontal = 14.dp)
                                    ) {
                                        Text(
                                            cat,
                                            color = if (isCatSelected) DeepDarkBack else Color.White,
                                            fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Submit Button
                            LiquidGlassButton(
                                onClick = {
                                    val amt = txAmount.toDoubleOrNull()
                                    if (txTitle.isBlank() || amt == null || amt <= 0) {
                                        Toast.makeText(context, "Input a valid item title and amount", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.addTransaction(
                                            title = txTitle,
                                            amount = amt,
                                            type = txDialogType,
                                            category = txCategory
                                        )
                                        showAddTxDialog = false
                                        successToastMessage = "Recorded transaction balance adjustment!"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = if (txDialogType == "INCOME") listOf(Color(0x3B81C784), Color(0x1181C784)) else listOf(Color(0x3BEF5350), Color(0x11EF5350)),
                                borderColors = if (txDialogType == "INCOME") listOf(Color(0xFF81C784), Color(0x2281C784)) else listOf(Color(0xFFEF5350), Color(0x22EF5350)),
                                testTag = "add_tx_submit_button"
                            ) {
                                Icon(Icons.Default.Check, null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Record", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // ============================================
        // DIALOG: CONFIRM DELETE CARD ACCOUNT
        // ============================================
        if (showDeleteCardConfirm) {
            Dialog(
                onDismissRequest = { showDeleteCardConfirm = false }
            ) {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(24.dp),
                    backgroundBrush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E1010), Color(0xFF0F0808))
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Unlink bank account?",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This will secure-wipe all balance tracking and transaction histories for ${selectedCard?.bankName} immediately. This action cannot be undone.",
                            color = Color(0xCCFFFFFF),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { showDeleteCardConfirm = false },
                                modifier = Modifier.testTag("delete_card_cancel_button")
                            ) {
                                Text("Cancel", color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    viewModel.deleteSelectedCard()
                                    showDeleteCardConfirm = false
                                    successToastMessage = "Unlinked bank card account."
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                                modifier = Modifier.testTag("delete_card_confirm_button")
                            ) {
                                Text("Wipe & Remove", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Beautiful liquid gradient card styling. Displays standard bank card structure.
 */
@Composable
fun BankAccountGlassCard(
    card: BankCard,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    strokeAlpha: Float
) {
    // Pick appropriate metallic fluid gradient brush depending on index
    val cardBrush = when (card.styleIndex) {
        0 -> Brush.linearGradient(colors = listOf(Gradient1Start, Gradient1End))
        1 -> Brush.linearGradient(colors = listOf(Gradient2Start, Gradient2End))
        2 -> Brush.linearGradient(colors = listOf(Gradient3Start, Gradient3End))
        else -> Brush.linearGradient(colors = listOf(Gradient4Start, Gradient4End))
    }

    Box(
        modifier = modifier
            .testTag("bank_card_${card.id}")
            .clip(RoundedCornerShape(24.dp))
            .background(cardBrush)
            .border(
                BorderStroke(
                    width = 1.3.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = strokeAlpha),
                            Color.Transparent
                        )
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        // Shine curved overlay
        Canvas(modifier = Modifier.fillMaxSize()) {
            val wavePath = android.graphics.Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                cubicTo(
                    size.width * 0.7f, size.height * 0.4f,
                    size.width * 0.3f, size.height * 0.1f,
                    0f, size.height * 0.55f
                )
                close()
            }
            drawPath(
                path = wavePath.asComposePath(),
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x1EFFFFFF), Color(0x00FFFFFF))
                )
            )
        }

        // Card specs overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Row 1: Bank Name and chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = card.bankName.uppercase(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.2.sp
                )

                // Chip symbol
                Box(
                    modifier = Modifier
                        .size(32.dp, 22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFF3C75B), Color(0xFFB58410))
                            )
                        )
                        .border(1.dp, Color(0x40FFFFFF), RoundedCornerShape(4.dp))
                )
            }

            // Row 2: Card Number and holder
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = card.cardNumber,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.6.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "CARD HOLDER",
                            color = Color(0x7AFFFFFF),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = card.cardHolder,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "EXPIRES",
                            color = Color(0x7AFFFFFF),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = card.expiryDate,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Card network visual logo indicator
                    Box(modifier = Modifier.size(28.dp, 18.dp)) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            if (card.cardType == "Visa") {
                                // Draw a sleek minimalistic Visa vibe triangle/circle accent
                                drawCircle(
                                    color = Color(0xFF1A1F71),
                                    radius = size.height * 0.45f,
                                    center = Offset(size.width * 0.4f, size.height * 0.5f)
                                )
                                drawCircle(
                                    color = Color(0xFFF79E1B),
                                    radius = size.height * 0.45f,
                                    center = Offset(size.width * 0.7f, size.height * 0.5f)
                                )
                            } else {
                                // Mastercard style intersection
                                drawCircle(
                                    color = Color(0xFFEB001B),
                                    radius = size.height * 0.45f,
                                    center = Offset(size.width * 0.35f, size.height * 0.5f)
                                )
                                drawCircle(
                                    color = Color(0xFFFF5F00),
                                    radius = size.height * 0.45f,
                                    center = Offset(size.width * 0.65f, size.height * 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Transaction lists item using liquid frost layouts.
 */
@Composable
fun TransactionGlassItem(
    transaction: CardTransaction,
    currencySymbol: String,
    onDelete: () -> Unit
) {
    val isIncome = transaction.type == "INCOME"
    val icon = remember(transaction.category) {
        when (transaction.category) {
            "Food" -> Icons.Outlined.Restaurant
            "Shopping" -> Icons.Outlined.ShoppingCart
            "Entertainment" -> Icons.Outlined.Tv
            "Utilities" -> Icons.Outlined.Lightbulb // safe standard icon
            "Salary" -> Icons.Outlined.MonetizationOn
            "Transport" -> Icons.Outlined.DirectionsCar
            else -> Icons.Outlined.Receipt // safe standard icon
        }
    }

    LiquidGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        borderColors = listOf(Color(0x1BFFFFFF), Color(0x05FFFFFF)),
        backgroundBrush = Brush.verticalGradient(
            colors = listOf(Color(0x0EFFFFFF), Color(0x03FFFFFF))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Circular icon frame with translucent light
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isIncome) Color(0x1581C784) else Color(0x12EF5350)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = transaction.category,
                        tint = if (isIncome) Color(0xFF81C784) else Color(0xFFEF5350),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.title,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = transaction.category + " • " + formatTimestamp(transaction.timestamp),
                        color = Color(0x66FFFFFF),
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val prefix = if (isIncome) "+" else "-"
                val textColor = if (isIncome) Color(0xFF81C784) else Color(0xFFFFEBEE)
                Text(
                    text = "$prefix ${formatCurrency(transaction.amount, currencySymbol)}",
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.testTag("tx_amount_text")
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Delete transaction sweep trigger
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete transaction",
                    tint = Color(0x44FFFFFF),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onDelete() }
                        .testTag("delete_tx_btn_${transaction.id}")
                )
            }
        }
    }
}

// Utility formatting functions
fun formatCurrency(amount: Double, symbol: String): String {
    val formatter = DecimalFormat("#,##0.00")
    return "$symbol${formatter.format(amount)}"
}

fun formatTimestamp(ms: Long): String {
    val date = Date(ms)
    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return sdf.format(date)
}
