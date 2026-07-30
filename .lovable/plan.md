# Ekran „Cele oszczędnościowe” — rozbudowa

Istniejący ekran `Cele` jest prosty (lista celów + wyzwanie tygodnia). Zastępuję go pełnym ekranem celów z paskiem postępu, mikro-nagrodami i sugestiami cięć.

## Co powstanie na ekranie

1. **Nagłówek celu głównego** — wybrany cel (Wyjazd w Bieszczady) z dużą kwotą, procentem, paskiem postępu i prognozą „przy obecnym tempie: ~data”.
2. **Karuzela / lista wszystkich celów** — każdy z paskiem postępu, kwotą odłożoną vs. docelową, terminem i miesięczną ratą potrzebną, by zdążyć.
3. **Mikro-nagrody (kamienie milowe)** — 25/50/75/100% celu jako odznaki: odblokowane podświetlone (limonka), kolejne wyszarzone, z krótką nazwą nagrody, np. „Pierwsza ćwiartka”, „Półmetek”, „Twarda Waluta”.
4. **Sugestie co zmniejszyć** — 3 karty wyliczone z danych kategorii (kategorie z największym przekroczeniem / największym udziałem): „Rozrywka −60 zł/mies. → cel szybciej o 2 tygodnie”, z przyciskiem „Zastosuj”.
5. **Wyzwanie tygodnia** — zachowane, 7 kropek postępu.
6. **CTA** — „Dorzuć do celu” (10 / 20 / 50 zł).

## Dane (mock)

W `src/data/mock.ts`:
- rozszerzam `Goal` o `monthlyContribMinor` i `priority`,
- dodaję typ `Milestone` (procent, nazwa nagrody, czy odblokowana) generowany z postępu,
- dodaję helper `suggestionsForGoal()` liczący z `categories` ile miesięcy/tygodni oszczędza dane cięcie.

Wszystko dalej w groszach, formatowanie przez `pln()`.

## Zmiany w plikach

- `src/data/mock.ts` — pola celu, milestone'y, helper sugestii.
- `src/components/mock/Screens.tsx` — przepisany `ScreenGoals` (dłuższy, przewijalny wewnątrz ramki).
- `src/routes/index.tsx` — zaktualizowany podpis kafla „Cele”.
- `src/routes/dokumentacja.tsx` — sekcja o ekranie celów: encja `goals` + `milestones`, reguła wyliczania sugestii i ETA, mapowanie na Compose.

Bez backendu — makieta pozostaje statyczna.
