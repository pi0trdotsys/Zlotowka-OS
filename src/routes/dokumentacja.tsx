import { createFileRoute, Link } from "@tanstack/react-router";

export const Route = createFileRoute("/dokumentacja")({
  head: () => ({
    meta: [
      { title: "Dokumentacja makiety — ZŁOTÓWKA OS pod Kotlin/Compose" },
      {
        name: "description",
        content:
          "Specyfikacja design systemu, modeli danych, ekranów i widgetu Glance dla natywnej aplikacji budżetowej w Kotlinie.",
      },
      { property: "og:title", content: "Dokumentacja makiety — ZŁOTÓWKA OS" },
      {
        property: "og:description",
        content: "Tokeny, modele Room, nawigacja, widget Glance i reguły motywacyjne w jednym miejscu.",
      },
    ],
  }),
  component: Docs,
});

function Section({ n, title, children }: { n: string; title: string; children: React.ReactNode }) {
  return (
    <section className="border-t border-border py-10">
      <h2 className="text-[11px] uppercase tracking-[0.28em] text-muted-foreground">
        {n} — {title}
      </h2>
      <div className="mt-5 space-y-4 text-sm leading-relaxed text-muted-foreground">{children}</div>
    </section>
  );
}

function Code({ children }: { children: string }) {
  return (
    <pre className="tabular overflow-x-auto rounded-xl border border-border bg-surface p-4 text-[12px] leading-relaxed text-foreground">
      {children}
    </pre>
  );
}

function Docs() {
  return (
    <main className="mx-auto max-w-3xl px-6 py-16 md:px-8">
      <Link to="/" className="tabular text-[11px] uppercase tracking-[0.24em] text-lime">
        ← wróć do makiet
      </Link>
      <h1 className="mt-6 text-4xl font-semibold tracking-tight">Dokumentacja implementacyjna</h1>
      <p className="mt-4 text-sm text-muted-foreground">
        Makiety w TypeScript są referencją wizualną. Poniżej mapowanie 1:1 na natywną aplikację
        Android (Kotlin + Jetpack Compose + Room + Glance).
      </p>

      <Section n="01" title="Design system (tokeny)">
        <p>Tło grafitowe, jeden neon (limonka) jako akcent akcji, cyjan = oszczędności, koral = przekroczenia.</p>
        <Code>{`// Color.kt
val Background = Color(0xFF14161A)
val Surface     = Color(0xFF1D2026)
val Surface2    = Color(0xFF262A31)
val Lime        = Color(0xFFC8F751)  // akcent / CTA / postęp
val Cyan        = Color(0xFF6FE3E1)  // oszczędności, wyzwania
val Coral       = Color(0xFFF2704E)  // przekroczony limit
val Amber       = Color(0xFFF0B34A)
val Violet      = Color(0xFFB48CF2)
val TextPrimary = Color(0xFFF4F5F7)
val TextMuted   = Color(0xFF9AA1AC)

// Typografia: Space Grotesk (nagłówki/UI), JetBrains Mono (kwoty, tabular figures)
// Promienie: card 20.dp, pill 999.dp, telefon-sheet 28.dp
// Odstępy: skala 4 / 8 / 12 / 20 / 32`}</Code>
        <p>
          Zasada: kwoty ZAWSZE monospace z <code className="text-cyan">tabular figures</code>, żeby
          cyfry nie „skakały” przy animacji licznika.
        </p>
      </Section>

      <Section n="02" title="Model danych (Room)">
        <p>
          Kwoty przechowujemy w <strong className="text-foreground">groszach jako Long</strong> —
          nigdy Double. Wydatek = wartość ujemna, dochód = dodatnia.
        </p>
        <Code>{`@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val categoryId: String,
    val amountMinor: Long,        // grosze; < 0 wydatek, > 0 dochód
    val timestamp: Long,          // epoch millis
    val method: PaymentMethod,    // BLIK, CARD, CASH, TRANSFER
    val note: String? = null
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val label: String,
    val emoji: String,
    val colorToken: String,       // "lime" | "cyan" | "coral" | "amber" | "violet"
    val monthlyBudgetMinor: Long
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val label: String,
    val targetMinor: Long,
    val savedMinor: Long,
    val deadline: Long,
    val monthlyContribMinor: Long,   // realne tempo odkładania
    val priority: Int                // 1 = cel główny
)

@Entity(tableName = "goal_milestones")
data class MilestoneEntity(
    @PrimaryKey val id: String,      // "\${goalId}-25"
    val goalId: String,
    val pct: Int,                    // 25 | 50 | 75 | 100
    val reward: String,              // "Pierwsza ćwiartka", "Półmetek", ...
    val unlockedAt: Long? = null     // null = jeszcze zablokowana
)

@Entity(tableName = "goal_contributions")
data class ContributionEntity(
    @PrimaryKey val id: String,
    val goalId: String,
    val amountMinor: Long,           // > 0 wpłata, < 0 wypłata z celu
    val timestamp: Long,
    val source: ContributionSource,  // MANUAL, AUTO, ROUNDUP, CHALLENGE, CUT
    val note: String? = null
)`}</Code>
      </Section>



      <Section n="03" title="Formatowanie PLN">
        <Code>{`fun Long.toPln(withSign: Boolean = false): String {
    val nf = NumberFormat.getCurrencyInstance(Locale("pl", "PL")).apply {
        currency = Currency.getInstance("PLN")
        minimumFractionDigits = 2
    }
    val text = nf.format(abs(this) / 100.0)   // "1 234,56 zł"
    return if (!withSign) text else (if (this < 0) "− " else "+ ") + text
}`}</Code>
        <p>Separator tysięcy: spacja nierozdzielająca. Symbol „zł” po kwocie. Minus typograficzny (−), nie łącznik.</p>
      </Section>

      <Section n="04" title="Ekrany i nawigacja">
        <Code>{`NavHost(startDestination = "dashboard") {
  composable("dashboard")     { DashboardScreen() }   // ekran 1
  composable("add")           { AddExpenseScreen() }  // ekran 2 (bottom sheet lub full)
  composable("categories")    { CategoriesScreen() }  // ekran 3
  composable("goals")         { GoalsScreen() }       // ekran 4
}`}</Code>
        <ul className="list-disc space-y-2 pl-5">
          <li>
            <strong className="text-foreground">Pulpit</strong>: „zostało do końca miesiąca”, dzienny
            budżet, Puls oszczędzania (0–100), słupki 7 dni (najwyższy = koral), 3 ostatnie ruchy.
          </li>
          <li>
            <strong className="text-foreground">Nowy wydatek</strong>: numpad → siatka kategorii 3×2
            → metoda płatności → zapis. Maks. 3 dotknięcia do zapisania.
          </li>
          <li>
            <strong className="text-foreground">Kategorie</strong>: lista z paskiem limitu; &gt;100%
            przełącza kolor na koral i pokazuje procent.
          </li>
          <li>
            <strong className="text-foreground">Cele</strong>: cel główny (priority = 1) z paskiem
            postępu i prognozą daty, pas mikro-nagród 25/50/75/100%, lista sugestii „co zmniejszyć”,
            wszystkie cele, wyzwanie tygodnia i szybkie dorzucenia (10/20/50 zł).
          </li>
          <li>
            <strong className="text-foreground">Szczegóły celu</strong>: karta prognozy (realne tempo
            vs. plan), słupki wpłat miesięcznych, pełna historia wpłat/wypłat ze źródłem i notatką,
            szybkie dorzucenia.
          </li>

        </ul>
      </Section>

      <Section n="05" title="Logika motywacyjna">
        <Code>{`// Puls oszczędzania (0..100), liczony raz dziennie
score = 0.4 * budgetAdherence   // % kategorii w limicie
      + 0.3 * savingsRate       // (dochody - wydatki) / dochody, clamp 0..1
      + 0.3 * streakFactor      // min(streakDays / 30, 1)

// Seria (streak): dzień bez wydatku w kategorii oznaczonej jako "impulsowa"
// Odznaki: 3 dni "Pierwszy Grosz", 7 "Oszczędny", 14 "Twarda Waluta", 30 "Żelazny Budżet"

// --- CELE ---
fun monthsToGoal(g: GoalEntity, extraMonthly: Long = 0L): Double {
    val rate = g.monthlyContribMinor + extraMonthly
    if (rate <= 0) return Double.POSITIVE_INFINITY
    return max(0.0, (g.targetMinor - g.savedMinor).toDouble() / rate)
}

// ETA: dzisiaj + ceil(monthsToGoal) miesięcy, format "LLL yyyy" (pl-PL)

// Mikro-nagrody: odblokuj próg, gdy savedMinor/targetMinor >= pct/100.
// Odblokowanie zapisuje unlockedAt i wyzwala notyfikację + animację.

// Sugestie "co zmniejszyć" (top 3):
// cut = if (spent > budget) spent - budget else round(spent * 0.15)
// weeksSaved = ((monthsToGoal(g) - monthsToGoal(g, cut)) * 4.345).roundToInt().coerceAtLeast(1)
// sortuj malejąco po cut; akcja "Zastosuj" obniża monthlyBudgetMinor kategorii o cut
// i podnosi monthlyContribMinor celu o tę samą kwotę.

// --- SZCZEGÓŁY CELU: prognoza z historii wpłat ---
// realne tempo = suma wpłat z ostatnich N miesięcy / N (domyślnie N = 2)
fun actualMonthlyRate(items: List<ContributionEntity>, months: Int = 2): Long =
    if (items.isEmpty()) 0L else items.sumOf { it.amountMinor } / months

// ETA z historii = monthsToGoal(g.copy(monthlyContribMinor = actualMonthlyRate(...)))
// drift = realneTempo - plan; drift > 0 → limonka "przyspiesza", < 0 → koral "cofa się"
// Prognozę przelicza się po każdym INSERT do goal_contributions (Flow z Room).
// Wypłata z celu = ujemny amountMinor; nie kasuje odblokowanych mikro-nagród.`}</Code>

        <p>
          Przytyk kontekstowy przy dodawaniu: jeśli w bieżącym tygodniu są ≥3 wydatki tej samej
          kategorii i podkategorii, pokaż kartę z alternatywnym kosztem (np. „gotowanie zostawiłoby
          96 zł”).
        </p>
      </Section>


      <Section n="06" title="Widget (Glance)">
        <Code>{`class BudgetWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(
        setOf(DpSize(180.dp, 110.dp),  // 2x2 "Puls"
              DpSize(330.dp, 110.dp))  // 4x2 "Pasek dnia"
    )
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = repository.widgetState()   // dailyLeftMinor, progress, streak, score
        provideContent { WidgetContent(state) }
    }
}

// Odświeżanie: WorkManager co 30 min + natychmiast po zapisie transakcji
BudgetWidget().updateAll(context)

// Akcje: actionStartActivity<MainActivity>(intent z "route" = "add")
// Kwoty szybkie 10/20/50 zł -> actionRunCallback<QuickAddAction>()`}</Code>
        <p>
          Tło widgetu: <code className="text-cyan">Surface</code> z 90% alfa + zaokrąglenie 28.dp
          (użyj <code className="text-cyan">android:widgetBackground</code> i dynamicznego koloru
          jako opcji).
        </p>
      </Section>

      <Section n="07" title="Mapowanie plików makiety">
        <Code>{`src/data/mock.ts                  -> kontrakt danych (encje, formatowanie)
src/components/mock/Screens.tsx   -> 4 ekrany Compose
src/components/mock/Widget.tsx    -> layouty Glance 4x2 i 2x2
src/styles.css                    -> tokeny -> Color.kt / Type.kt / Shape.kt`}</Code>
      </Section>
    </main>
  );
}
