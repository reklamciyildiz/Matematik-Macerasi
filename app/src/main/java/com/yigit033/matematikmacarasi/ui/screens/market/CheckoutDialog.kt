package com.yigit033.matematikmacarasi.ui.screens.market

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.games.models.market.ShoppingCart
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import com.yigit033.matematikmacarasi.services.payments.AdManager
import android.app.Activity

/**
 * Ödeme ekranı dialog bileşeni - Çocuk dostu eğitici versiyonu
 * 
 * @param shoppingCart Alışveriş sepeti
 * @param onDismiss Dialog kapatma callback'i
 * @param onPaymentSubmit Ödeme gönderme callback'i
 * @param budget Bütçe parametresi
 */
@Composable
fun CheckoutDialog(
    shoppingCart: ShoppingCart,
    onDismiss: () -> Unit,
    onPaymentSubmit: (Double) -> Unit,
    budget: Double = 0.0
) {
    // Toplam fiyat
    val totalPrice = shoppingCart.getTotalPrice()
    
    // Para değerleri
    val coins = listOf(0.10, 0.25, 0.50, 1.0)
    val bills = listOf(5.0, 10.0, 20.0, 50.0, 100.0, 200.0)
    
    // Seçilen para birimleri
    val selectedMoney = remember { mutableStateListOf<Double>() }
    
    // Hesaplanan toplam seçilen para
    val totalSelectedMoney = selectedMoney.sum()
    
    // Para üstü hesapla
    val change = totalSelectedMoney - totalPrice
    
    // Para seçimi kısmından ödeme kısmına geçme durumu
    var showPayment by remember { mutableStateOf(false) }
    
    // Ödenecek miktar manuel girişi için
    var manualAmountText by remember { mutableStateOf("") }
    var showManualInput by remember { mutableStateOf(false) }
    
    // İpucu gösterimi
    var showHint by remember { mutableStateOf(false) }
    var showCalculationHelp by remember { mutableStateOf(false) }
    
    // AdManager için gerekli değişkenler
    val context = LocalContext.current
    val adManager = remember { AdManager.getInstance(context) }
    var showRewardedAdDialog by remember { mutableStateOf(false) }
    
    // Animasyonla toplam para değeri
    val totalAmountAlpha by animateFloatAsState(
        targetValue = if (totalSelectedMoney > 0) 1f else 0.3f,
        label = "totalAmountAnimation"
    )
    
    // Yeni eklenen değişken
    var showCancelConfirmationDialog by remember { mutableStateOf(false) }
    
    // Ödüllü reklam diyaloğu
    if (showRewardedAdDialog) {
        Dialog(
            onDismissRequest = { showRewardedAdDialog = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // İpucu ikonu
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                color = Color(0xFFE3F2FD),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = "İpucu",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Başlık
                    Text(
                        text = "Ödemenin İpucunu Al",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Açıklama
                    Text(
                        text = "Çok kısa bir video izle ve market ödemeni nasıl yapacağına dair ipucuyu kap!",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Butonlar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // İptal butonu
                        OutlinedButton(
                            onClick = { showRewardedAdDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Gray
                            ),
                            border = BorderStroke(1.dp, Color.Gray),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                                .height(48.dp)
                        ) {
                            Text("İptal")
                        }
                        
                        // Reklam izle butonu
                        Button(
                            onClick = {
                                showRewardedAdDialog = false
                                // Ödüllü reklamı göster
                                adManager.showRewardedAd(
                                    context as Activity,
                                    onRewarded = { rewardAmount ->
                                        // Reklamı izleyince ipucu göster
                                        showHint = true
                                    },
                                    onAdNotAvailable = {
                                        // Reklam yoksa direkt ipucu göster
                                        showHint = true
                                    }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                                .height(48.dp)
                        ) {
                            Text("İpucu Al")
                        }
                    }
                }
            }
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .heightIn(max = 600.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            // İptal onay dialog'u
            if (showCancelConfirmationDialog) {
                Dialog(
                    onDismissRequest = { showCancelConfirmationDialog = false },
                    properties = DialogProperties(
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Uyarı ikonu
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(
                                        color = Color(0xFFFFEBEE),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Uyarı",
                                    tint = Color(0xFFE57373),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Başlık
                            Text(
                                text = "Alışverişi İptal Et",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE57373),
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Açıklama
                            Text(
                                text = "Alışverişini iptal etmek istediğine emin misin? Bence geri dönüp alışverişini tamamla ve gerçek bir market alışverişinde matematiğin ne kadar işe yaradığını gör.",
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                color = Color.DarkGray
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Butonlar
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Alışverişe devam et butonu
                                OutlinedButton(
                                    onClick = { showCancelConfirmationDialog = false },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .height(48.dp)
                                ) {
                                    Text("Alışverişe Devam Et")
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Alışverişi iptal et butonu
                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE57373)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .height(48.dp)
                                ) {
                                    Text("Alışverişimi İptal Et")
                                }
                            }
                        }
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Başlık ve Kapatma Butonu Satırı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ödeme",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // İptal butonu
                    IconButton(
                        onClick = {
                            showCancelConfirmationDialog = true
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEBEE))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "İptal",
                            tint = Color(0xFFE57373),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Alışveriş Özeti
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Alışveriş Özeti",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Ödenecek Toplam
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Ödenecek Toplam:",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Text(
                                text = String.format("%.2f ₺", totalPrice),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // Bütçe bilgisi
                        if (budget > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Bütçen:",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                
                                Text(
                                    text = String.format("%.2f ₺", budget),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (budget >= totalPrice) Color(0xFF4CAF50) else Color(0xFFE57373)
                                )
                            }
                        }
                        
                        // Para üstü bilgisi (eğer toplam para seçilmişse)
                        if (totalSelectedMoney > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider(color = Color(0xFFBBDEFB))
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Verdiğin Para:",
                                    fontSize = 14.sp
                                )
                                
                                Text(
                                    text = String.format("%.2f ₺", totalSelectedMoney),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            if (totalSelectedMoney >= totalPrice) {
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Para Üstü:",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    
                                    Text(
                                        text = String.format("%.2f ₺", change),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Para üstü hesaplama gösterimi
                AnimatedVisibility(
                    visible = totalSelectedMoney >= totalPrice
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8F5E9)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Para Üstü Hesaplaması",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF43A047)
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Para üstü hesaplama adımları
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                        .background(
                                            color = Color(0xFFF1F8E9),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    // Verilen para
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Verdiğin Para:",
                                            fontSize = 16.sp
                                        )
                                        
                                        Text(
                                            text = String.format("%.2f", totalSelectedMoney),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    // Çıkarma işlemi
                                    Text(
                                        text = buildAnnotatedString {
                                            append("Para üstü hesaplaması: ")
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append(String.format("%.2f", totalSelectedMoney))
                                            }
                                            append(" - ")
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                                append(String.format("%.2f", totalPrice))
                                            }
                                            append(" = ")
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF43A047))) {
                                                append(String.format("%.2f", change))
                                            }
                                            append(" ₺")
                                        },
                                        fontSize = 16.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Para üstü
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Para Üstü:",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    
                                    Text(
                                        text = String.format("%.2f ₺", change),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF43A047)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Öde/İptal butonları (para üstü hesaplama gösterildiğinde) - DÜZENLEME
                if (totalSelectedMoney >= totalPrice) {
                    // Bütçe kontrolü
                    val canPayment = budget <= 0 || totalSelectedMoney <= budget
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // İptal butonu
                        OutlinedButton(
                            onClick = {
                                showCancelConfirmationDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFE57373)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFE57373)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "İptal",
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 4.dp)
                            )
                            Text("İptal")
                        }
                        
                        // Öde butonu
                        Button(
                            onClick = { 
                                if (canPayment) {
                                    onPaymentSubmit(totalSelectedMoney) 
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canPayment) Color(0xFF4CAF50) else Color.Gray,
                                disabledContainerColor = Color.Gray
                            ),
                            enabled = canPayment,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Ödemeyi Tamamla",
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 4.dp)
                            )
                            Text("Öde")
                        }
                    }
                    
                    // Bütçe aşımı uyarısı
                    if (!canPayment) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFFFEBEE),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Uyarı",
                                tint = Color(0xFFE57373),
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = "Bütçeni aştığın için ödeme yapamazsın! Daha az para seç veya para iadesini kullan.",
                                fontSize = 14.sp,
                                color = Color(0xFFE57373)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // İpucu butonu - ÖNCEKİ İPUCU BUTONU VE SPACER'LARI KALDIRDIM
                // SADECE İHTİYAÇ OLDUĞUNDA GÖSTER
                if (totalSelectedMoney < totalPrice) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { showRewardedAdDialog = true },  // Değişiklik: Artık ipucu direkt gözükmek yerine reklam diyaloğunu açacak
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE3F2FD)
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "İpucu",
                                    tint = Color(0xFF1565C0),
                                    modifier = Modifier.size(20.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = "Reklam İzle ve İpucuyu kap",
                                    color = Color(0xFF1565C0)
                                )
                            }
                        }
                    }
                }
                
                // İpucu gösterimi
                AnimatedVisibility(visible = showHint) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE3F2FD)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "İpucu:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF1565C0)
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "Toplam tutarı ödemek için, en az ${String.format("%.2f", totalPrice)} ₺ değerinde para seçmen gerekiyor.",
                                fontSize = 14.sp,
                                color = Color(0xFF1565C0)
                            )
                            
                            if (budget > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                Text(
                                    text = "Cebindeki para miktarı: ${String.format("%.2f", budget)} ₺",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1565C0)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Text(
                                text = "Çıkarma işlemi yaparak para üstünü hesaplayabilirsin!",
                                fontSize = 14.sp,
                                color = Color(0xFF1565C0)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Seçilen para miktarı gösterimi
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            budget > 0 && totalSelectedMoney > budget -> Color(0xFFFFEBEE) // Bütçe aşımı - kırmızı
                            else -> Color(0xFFFFF3E0) // Normal - turuncu
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color.LightGray,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachMoney,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            if (totalSelectedMoney > 0) {
                                Icon(
                                    imageVector = when {
                                        budget > 0 && totalSelectedMoney > budget -> Icons.Default.Check
                                        else -> Icons.Default.Info
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        budget > 0 && totalSelectedMoney > budget -> Color(0xFF4CAF50) // Yeşil
                                        else -> Color(0xFFFF9800) // Turuncu
                                    }
                                )
                            }
                        }
                        
                        Text(
                            text = String.format("%.2f ₺", totalSelectedMoney),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                budget > 0 && totalSelectedMoney > budget -> Color(0xFF4CAF50) // Yeşil
                                else -> Color(0xFFFF9800) // Turuncu
                            },
                            modifier = Modifier.alpha(totalAmountAlpha)
                        )
                    }
                    
                    // Çeşitli uyarılar
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        // Bütçe aşımı uyarısı
                        AnimatedVisibility(
                            visible = budget > 0 && totalSelectedMoney > budget,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFE53935),
                                    modifier = Modifier.size(16.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(4.dp))
                                
                                Text(
                                    text = String.format("%.2f ₺ fazla para seçtin", totalSelectedMoney - budget),
                                    fontSize = 14.sp,
                                    color = Color(0xFFE53935)
                                )
                            }
                        }
                        
                        // Yetersiz para uyarısı
                        AnimatedVisibility(
                            visible = totalSelectedMoney > 0 && totalSelectedMoney < totalPrice && budget > 0,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(16.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(4.dp))
                                
                                Text(
                                    text = String.format("%.2f ₺ daha eklemelisin", totalPrice - totalSelectedMoney),
                                    fontSize = 14.sp,
                                    color = Color(0xFFFF9800)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Ödeme yapmak için paraları seç bölümü
                Text(
                    text = "Ödeme yapmak için paraları seç:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Banknotlar
                Text(
                    text = "Banknotlar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Banknotlar grid - BOYUT SORUNUNU DÜZELT
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        items(bills) { bill ->
                            MoneyButton(
                                value = bill,
                                onClick = { selectedMoney.add(bill) },
                                color = getBillColor(bill)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Madeni Paralar
                Text(
                    text = "Madeni Paralar",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Madeni Paralar grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(coins) { coin ->
                        CoinButton(
                            value = coin,
                            onClick = { selectedMoney.add(coin) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Eklenen paraları gösterme - YENİ
                if (selectedMoney.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Eklenen Paralar",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                // Tümünü temizle butonu
                                IconButton(
                                    onClick = { selectedMoney.clear() },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Tümünü Temizle",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                            
                            // Seçilen paraları listele
                            selectedMoney.forEachIndexed { index, amount ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                color = if (amount >= 5.0) getBillColor(amount) else getCoinColor(amount),
                                                shape = if (amount >= 5.0) RoundedCornerShape(4.dp) else CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (amount >= 5.0) "${amount.toInt()}" else String.format("%.2f", amount),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                    
                    Text(
                                        text = String.format("%.2f ₺", amount),
                                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                                    IconButton(
                                        onClick = { 
                                            selectedMoney.removeAt(index)
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Para Birimini Sil",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                
                                if (index < selectedMoney.size - 1) {
                                    Divider(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        color = Color.LightGray
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Para ekleme ve silme butonları
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Tümünü temizle butonu
                    Button(
                        onClick = { selectedMoney.clear() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.LightGray
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Temizle",
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                        Text(
                            text = "Temizle",
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Son eklenen parayı kaldır
                    Button(
                        onClick = {
                            if (selectedMoney.isNotEmpty()) {
                                selectedMoney.removeAt(selectedMoney.lastIndex)
                            }
                        },
                        enabled = selectedMoney.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Son Parayı Çıkar",
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 4.dp)
                        )
                        Text(
                            text = "Geri Al",
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Para butonu bileşeni
 */
@Composable
private fun MoneyButton(
    value: Double,
    onClick: () -> Unit,
    color: Color
) {
    Box(
        modifier = Modifier
            .size(width = 100.dp, height = 48.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = color
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${value.toInt()} ₺", 
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Visible
            )
        }
    }
}

/**
 * Madeni para butonu bileşeni
 */
@Composable
private fun CoinButton(
    value: Double,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(60.dp)
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = getCoinColor(value)
            ),
            shape = CircleShape,
            contentPadding = PaddingValues(4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = String.format("%.2f", value).replace(",", "."),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = if (value < 1) 14.sp else 16.sp,
                maxLines = 1
            )
        }
    }
}

/**
 * Banknot rengini döndürür
 */
@Composable
private fun getBillColor(value: Double): Color {
    return when (value) {
        5.0 -> Color(0xFF8BC34A)    // Yeşil
        10.0 -> Color(0xFF29B6F6)   // Mavi
        20.0 -> Color(0xFF9C27B0)   // Mor
        50.0 -> Color(0xFFF44336)   // Kırmızı
        100.0 -> Color(0xFF1E88E5)  // Koyu Mavi
        200.0 -> Color(0xFF009688)  // Turkuaz
        else -> MaterialTheme.colorScheme.primary
    }
}

/**
 * Madeni para rengini döndürür
 */
@Composable
private fun getCoinColor(value: Double): Color {
    return when (value) {
        0.10 -> Color(0xFFD4E157)  // Açık yeşil
        0.25 -> Color(0xFFFFD54F)  // Sarı
        0.50 -> Color(0xFFFFB74D)  // Turuncu
        1.0 -> Color(0xFFFF8A65)   // Kırmızımsı turuncu
        else -> MaterialTheme.colorScheme.primary
    }
} 