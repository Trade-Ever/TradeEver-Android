package com.trever.android.ui.sellcar

sealed class SellCarScreen(val route: String) {
    object ModelPrompt : SellCarScreen("sell_car_model_prompt")
    object SelectManufacturer : SellCarScreen("sell_car_select_manufacturer")
    object SelectModel : SellCarScreen("sell_car_select_model")
    object SelectYear : SellCarScreen("sell_car_select_year")
    object MileageAndType : SellCarScreen("sell_car_mileage_and_type")

}
