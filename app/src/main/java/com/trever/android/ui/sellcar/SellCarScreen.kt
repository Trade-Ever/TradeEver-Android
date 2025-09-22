package com.trever.android.ui.sellcar

sealed class SellCarScreen(val route: String) {
    object ModelPrompt : SellCarScreen("sell_car_model_prompt")
    object SelectManufacturer : SellCarScreen("sell_car_select_manufacturer")
    object SelectModel : SellCarScreen("sell_car_select_model")
    object SelectYear : SellCarScreen("sell_car_select_year")
    object MileageAndType : SellCarScreen("sell_car_mileage_and_type")
    // TODO: 여기에 차량 사진 추가, 사고이력, 판매방식, 판매가격 등 다른 단계들의 라우트 추가
    // 예: object AddPhotos : SellCarScreen("sell_car_add_photos")
    //     object AccidentHistory : SellCarScreen("sell_car_accident_history")
    //     object SetPrice : SellCarScreen("sell_car_set_price")
}
