package it.rapidosoft.semplice

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Donazioni tramite Google Play: prodotti in-app "consumabili", quindi acquistabili
 * più volte e senza nulla da sbloccare. I prodotti vanno creati nella Play Console
 * con questi ID (vedi docs/PLAY_STORE.md).
 */
class TipJar(context: Context) : PurchasesUpdatedListener {
    private val client = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    /** Vuota se Play non è disponibile o i prodotti non esistono: in quel caso non si mostra nulla. */
    val products: StateFlow<List<ProductDetails>> = _products

    private val _thanks = Channel<Unit>(Channel.CONFLATED)
    val thanks = _thanks.receiveAsFlow()

    fun connect() {
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode != BillingClient.BillingResponseCode.OK) return
                loadProducts()
                consumePending()
            }

            override fun onBillingServiceDisconnected() = Unit
        })
    }

    fun close() = client.endConnection()

    fun buy(activity: Activity, product: ProductDetails) {
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(product).build())
            )
            .build()
        client.launchBillingFlow(activity, params)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK) purchases?.forEach { consume(it, sayThanks = true) }
    }

    private fun loadProducts() {
        val params = QueryProductDetailsParams.newBuilder().setProductList(
            PRODUCT_IDS.map {
                QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(ProductType.INAPP).build()
            }
        ).build()
        client.queryProductDetailsAsync(params) { result, details ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _products.value = details.productDetailsList
                    .sortedBy { it.oneTimePurchaseOfferDetails?.priceAmountMicros ?: Long.MAX_VALUE }
            }
        }
    }

    /** Acquisti completati ma non ancora consumati (app chiusa durante il pagamento...). */
    private fun consumePending() {
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(ProductType.INAPP).build()) { result, list ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) list.forEach { consume(it, sayThanks = false) }
        }
    }

    private fun consume(purchase: Purchase, sayThanks: Boolean) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        client.consumeAsync(ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()) { result, _ ->
            if (sayThanks && result.responseCode == BillingClient.BillingResponseCode.OK) _thanks.trySend(Unit)
        }
    }

    companion object {
        val PRODUCT_IDS = listOf("coffee_small", "coffee_medium", "coffee_large")
    }
}
