package pl.nullpointerstudio.zlotowka.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = PaymentMethod.valueOf(value)

    @TypeConverter
    fun fromContributionSource(value: ContributionSource): String = value.name

    @TypeConverter
    fun toContributionSource(value: String): ContributionSource = ContributionSource.valueOf(value)

    @TypeConverter
    fun fromCategoryKind(value: CategoryKind): String = value.name

    @TypeConverter
    fun toCategoryKind(value: String): CategoryKind = CategoryKind.valueOf(value)
}
