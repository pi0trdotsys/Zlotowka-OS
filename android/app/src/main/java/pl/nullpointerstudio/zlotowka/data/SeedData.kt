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
        CategoryEntity("elektronika", "Elektronika", "💻", "violet", 0, 7),
        CategoryEntity("gry", "Gry", "🎮", "cyan", 0, 8, isImpulse = true),
        CategoryEntity("gadzety", "Drobiazgi/Gadżety", "🧩", "muted", 0, 9, isImpulse = true),
        CategoryEntity("alkohol", "Alkohol", "🍺", "coral", 0, 10, isImpulse = true),
        CategoryEntity("zakupy_spozywcze", "Zakupy spożywcze", "🛒", "lime", 0, 11),
        CategoryEntity("slodycze", "Chipsy/słodycze", "🍬", "amber", 0, 12, isImpulse = true),
        CategoryEntity("jedzenie_na_miescie", "Jedzenie na mieście", "🍽️", "coral", 0, 13, isImpulse = true),
        CategoryEntity("uroda", "Uroda/Pielęgnacja", "💅", "violet", 0, 14),
        CategoryEntity(FALLBACK_CATEGORY_ID, "Inne", "💰", "muted", 0, 6),

        // Dochody — osobna lista od wydatków, bo sposoby zarabiania są zupełnie inne.
        CategoryEntity("wyplata", "Wypłata/Pensja", "💼", "lime", 0, 20, kind = CategoryKind.INCOME),
        CategoryEntity("freelance", "Freelance/Zlecenia", "🧑‍💻", "cyan", 0, 21, kind = CategoryKind.INCOME),
        CategoryEntity("sprzedaz", "Sprzedaż rzeczy", "📦", "amber", 0, 22, kind = CategoryKind.INCOME),
        CategoryEntity("zwrot", "Zwrot/Refundacja", "↩️", "violet", 0, 23, kind = CategoryKind.INCOME),
        CategoryEntity("prezent", "Prezent", "🎁", "coral", 0, 24, kind = CategoryKind.INCOME),
        CategoryEntity("inwestycje", "Inwestycje/Dywidendy", "📈", "lime", 0, 25, kind = CategoryKind.INCOME),
        CategoryEntity("stypendium", "Stypendium/Świadczenia", "🎓", "cyan", 0, 26, kind = CategoryKind.INCOME),
        CategoryEntity(INCOME_FALLBACK_CATEGORY_ID, "Inne wpływy", "💰", "muted", 0, 27, kind = CategoryKind.INCOME),
    )
}
