package com.software.sello.presentation.category

import androidx.annotation.StringRes
import com.software.sello.R
import com.software.sello.designsystem.icon.SelloIcon

/** An icon a category can have: its stored key, its drawing and what it is called. */
data class CategoryIconOption(val key: String, val icon: SelloIcon, @StringRes val label: Int)

/**
 * The icons offered for categories, in the order shown. The key is what is stored, so
 * it must never change once released; the drawing and the name may.
 */
object CategoryIcons {
    val options: List<CategoryIconOption> = listOf(
        CategoryIconOption("restaurant", SelloIcon.Restaurant, R.string.category_icon_restaurant),
        CategoryIconOption("home", SelloIcon.Home, R.string.category_icon_home),
        CategoryIconOption(
            "directions_bus",
            SelloIcon.DirectionsBus,
            R.string.category_icon_directions_bus
        ),
        CategoryIconOption("theaters", SelloIcon.Theaters, R.string.category_icon_theaters),
        CategoryIconOption(
            "medical_services",
            SelloIcon.MedicalServices,
            R.string.category_icon_medical_services
        ),
        CategoryIconOption("checkroom", SelloIcon.Checkroom, R.string.category_icon_checkroom),
        CategoryIconOption("local_cafe", SelloIcon.LocalCafe, R.string.category_icon_local_cafe),
        CategoryIconOption("school", SelloIcon.School, R.string.category_icon_school),
        CategoryIconOption("pets", SelloIcon.Pets, R.string.category_icon_pets),
        CategoryIconOption(
            "fitness_center",
            SelloIcon.FitnessCenter,
            R.string.category_icon_fitness_center
        ),
        CategoryIconOption(
            "shopping_cart",
            SelloIcon.ShoppingCart,
            R.string.category_icon_shopping_cart
        ),
        CategoryIconOption("flight", SelloIcon.Flight, R.string.category_icon_flight),
        CategoryIconOption(
            "phone_iphone",
            SelloIcon.PhoneIphone,
            R.string.category_icon_phone_iphone
        ),
        CategoryIconOption("child_care", SelloIcon.ChildCare, R.string.category_icon_child_care)
    )

    val keys: List<String> = options.map { it.key }

    /** A key this version has no drawing for, such as one from a newer backup, still shows something. */
    fun iconFor(key: String): SelloIcon =
        options.firstOrNull { it.key == key }?.icon ?: SelloIcon.ReceiptLong
}
