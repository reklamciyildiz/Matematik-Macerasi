package com.yigit033.matematikmacarasi.ui.screens.market

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yigit033.matematikmacarasi.games.models.market.MarketGamePhase
import com.yigit033.matematikmacarasi.games.models.market.MarketGameState
import com.yigit033.matematikmacarasi.games.models.market.Product
import com.yigit033.matematikmacarasi.games.models.market.ProductCategory

/**
 * Market oyunu ana ekranı
 * 
 * @param state Oyun durumu
 * @param onProductClick Ürüne tıklandığında çağrılacak fonksiyon
 * @param onQuantityChange Ürün miktarı değiştiğinde çağrılacak fonksiyon
 * @param onRemoveItem Ürün kaldırıldığında çağrılacak fonksiyon
 * @param onCheckout Kasaya gidildiğinde çağrılacak fonksiyon
 * @param onPaymentSubmit Ödeme yapıldığında çağrılacak fonksiyon
 * @param onPlayAgain Tekrar oyna butonuna tıklandığında çağrılacak fonksiyon
 * @param onBack Geri butonuna tıklandığında çağrılacak fonksiyon
 * @param showTutorial İlk açılışta tutorial gösterilsin mi
 */
@Composable
fun MarketGameScreen(
    state: MarketGameState,
    onProductClick: (Product) -> Unit,
    onQuantityChange: (Int, Double) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onCheckout: () -> Unit,
    onPaymentSubmit: (Double) -> Unit,
    onPlayAgain: () -> Unit,
    onBack: () -> Unit,
    showTutorial: Boolean = true
) {
    // Seçili kategori
    var selectedCategory by remember { mutableStateOf(ProductCategory.ALL) }
    
    // Dialog durumları
    var showTutorialDialog by remember { mutableStateOf(showTutorial) }
    var tutorialPage by remember { mutableStateOf(1) }
    var showShoppingListDialog by remember { mutableStateOf(false) }
    var showShoppingCartDialog by remember { mutableStateOf(false) }
    
    // Oyun tanıtım diyaloğu
    if (showTutorialDialog) {
        GameTutorialDialog(
            onDismiss = { showTutorialDialog = false },
            onNext = { 
                if (tutorialPage < 3) {
                    tutorialPage++
                } else {
                    showTutorialDialog = false
                }
            },
            currentPage = tutorialPage
        )
    }
    
    // Alışveriş listesi detay diyaloğu
    if (showShoppingListDialog) {
        ShoppingListDialog(
            shoppingList = state.level,
            onDismiss = { showShoppingListDialog = false }
        )
    }
    
    // Alışveriş sepeti detay diyaloğu
    if (showShoppingCartDialog) {
        ShoppingCartDialog(
            cart = state.shoppingCart,
            onDismiss = { showShoppingCartDialog = false },
            onQuantityChange = onQuantityChange,
            onRemoveItem = onRemoveItem,
            onCheckout = onCheckout,
            shoppingList = state.level
        )
    }
    
    when (state.phase) {
        MarketGamePhase.SHOPPING -> {
            Scaffold(
                topBar = {
                    MarketTopBar(
                        budget = state.level.budget,
                        cartTotal = state.shoppingCart.getTotalPrice(),
                        timeRemaining = state.timeRemaining,
                        onBackClick = onBack,
                        onCartClick = { showShoppingCartDialog = true }
                    )
                },
                bottomBar = {
                    MarketBottomBar(
                        level = state.level,
                        cartTotal = state.shoppingCart.getTotalPrice(),
                        onCheckout = onCheckout,
                        onShowCart = { showShoppingCartDialog = true }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        // Alışveriş listesi (tıklanabilir)
                        ShoppingListView(
                            level = state.level,
                            onShowFullList = { showShoppingListDialog = true },
                            modifier = Modifier.weight(0.3f)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Kategori seçici
                        CategorySelector(
                            selectedCategory = selectedCategory,
                            onCategorySelected = { selectedCategory = it }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Ürün listesi
                        ProductGrid(
                            products = state.availableProducts,
                            selectedCategory = selectedCategory,
                            onProductClick = { 
                                onProductClick(it)
                                
                                // Ürün eklendiğinde sepeti otomatik gösterme opsiyonu
                                // Bu özelliği kapatmak için aşağıdaki 3 satırı yorum haline getirebilirsiniz
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    showShoppingCartDialog = true
                                }, 500)
                            },
                            modifier = Modifier.weight(0.7f)
                        )
                    }
                }
            }
        }
        
        MarketGamePhase.CHECKOUT -> {
            // Ödeme ekranı
            CheckoutDialog(
                shoppingCart = state.shoppingCart,
                onDismiss = onBack,
                onPaymentSubmit = onPaymentSubmit,
                budget = state.level.budget
            )
        }
        
        MarketGamePhase.COMPLETED -> {
            // Oyun tamamlandı ekranı
            GameCompletedScreen(
                score = state.score,
                stars = state.stars,
                shoppingCart = state.shoppingCart,
                change = state.cashGiven - state.shoppingCart.getTotalPrice(),
                onPlayAgain = onPlayAgain,
                onBackToMenu = onBack
            )
        }
        
        MarketGamePhase.FAILED -> {
            // Oyun başarısız ekranı
            GameFailedScreen(
                reason = state.failReason,
                onPlayAgain = onPlayAgain,
                onBackToMenu = onBack
            )
        }
    }
} 