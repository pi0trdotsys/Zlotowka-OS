package pl.nullpointerstudio.zlotowka.data

/**
 * Domyślna lista kategorii pokazywana przy pierwszym uruchomieniu — czysto konfiguracyjna,
 * w pełni edytowalna i usuwalna z poziomu ekranu Kategorie. Aplikacja NIE zasiewa żadnych
 * transakcji ani celów: to użytkownik końcowy wpisuje własne dane od zera.
 */
object SeedData {

    fun categories(): List<CategoryEntity> = listOf(
        CategoryEntity("jedzenie", "Jedzenie", "🥦", "lime", 120_000, 0, isImpulse = true),
        CategoryEntity("transport", "Transport", "🚇", "cyan", 40_000, 1),
        CategoryEntity("mieszkanie", "Mieszkanie", "🏠", "violet", 230_000, 2),
        CategoryEntity("rozrywka", "Rozrywka", "🎧", "coral", 50_000, 3, isImpulse = true),
        CategoryEntity("zdrowie", "Zdrowie", "💊", "amber", 30_000, 4),
        CategoryEntity("subskrypcje", "Subskrypcje", "📺", "muted", 15_000, 5),
        CategoryEntity(FALLBACK_CATEGORY_ID, "Inne", "💰", "muted", 0, 6),
    )
}
