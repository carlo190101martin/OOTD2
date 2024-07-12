package com.example.ootd2

import org.json.JSONArray
import org.json.JSONObject

object GooglePay {
    fun getPaymentDataRequest(): JSONObject {
        val paymentDataRequest = JSONObject()

        paymentDataRequest.put("apiVersion", 2)
        paymentDataRequest.put("apiVersionMinor", 0)
        paymentDataRequest.put("allowedPaymentMethods", JSONArray().put(getBaseCardPaymentMethod()))
        paymentDataRequest.put("transactionInfo", getTransactionInfo())
        paymentDataRequest.put("merchantInfo", getMerchantInfo())

        return paymentDataRequest
    }

    private fun getBaseCardPaymentMethod(): JSONObject {
        val cardPaymentMethod = JSONObject()
        cardPaymentMethod.put("type", "CARD")
        val parameters = JSONObject()
        parameters.put("allowedAuthMethods", JSONArray().put("PAN_ONLY").put("CRYPTOGRAM_3DS"))
        parameters.put("allowedCardNetworks", JSONArray().put("AMEX").put("DISCOVER").put("MASTERCARD").put("VISA"))

        val tokenizationSpecification = JSONObject()
        tokenizationSpecification.put("type", "PAYMENT_GATEWAY")
        val tokenizationParameters = JSONObject()
        tokenizationParameters.put("gateway", "stripe")
        tokenizationParameters.put("stripe:version", "2023-10-16")
        tokenizationParameters.put("stripe:publishableKey", "pk_test_51OJtoIJnspWGpRzOy5eJQgHgvddkyRyxaiMTYbFTPkDA1l0oY6loEUXVGTeUHeznW861ikNNeqtrAxrID4cVObAF00kCPd75Ab")

        tokenizationSpecification.put("parameters", tokenizationParameters)
        cardPaymentMethod.put("tokenizationSpecification", tokenizationSpecification)
        cardPaymentMethod.put("parameters", parameters)

        return cardPaymentMethod
    }

    private fun getTransactionInfo(): JSONObject {
        val transactionInfo = JSONObject()
        transactionInfo.put("totalPrice", "10.00") // Example amount
        transactionInfo.put("totalPriceStatus", "FINAL")
        transactionInfo.put("currencyCode", "EUR")
        return transactionInfo
    }

    private fun getMerchantInfo(): JSONObject {
        val merchantInfo = JSONObject()
        merchantInfo.put("merchantName", "Example Merchant")
        return merchantInfo
    }
}
