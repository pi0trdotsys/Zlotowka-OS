/**
 * Dane makietowe (mock) dla aplikacji ZŁOTÓWKA OS.
 * Struktury tutaj = kontrakt danych, na którym oprzeć modele w Kotlinie.
 */

export type CategoryId =
  | "jedzenie"
  | "transport"
  | "mieszkanie"
  | "rozrywka"
  | "zdrowie"
  | "subskrypcje"
  | "inne";

export interface Category {
  id: CategoryId;
  label: string;
  /** nazwa tokenu koloru w design systemie */
  tone: "lime" | "cyan" | "coral" | "amber" | "violet" | "muted";
  icon: string;
  /** limit miesięczny w groszach */
  budgetMinor: number;
  spentMinor: number;
}

export interface Tx {
  id: string;
  title: string;
  category: CategoryId;
  /** kwota w groszach; ujemna = wydatek, dodatnia = dochód */
  amountMinor: number;
  date: string; // ISO
  method: "BLIK" | "Karta" | "Gotówka" | "Przelew";
}

export interface Goal {
  id: string;
  label: string;
  targetMinor: number;
  savedMinor: number;
  deadline: string;
  /** ile realnie odkładasz miesięcznie (grosze) */
  monthlyContribMinor: number;
  /** 1 = cel główny, wyżej = dalszy priorytet */
  priority: number;
}

export interface Milestone {
  pct: 25 | 50 | 75 | 100;
  reward: string;
  unlocked: boolean;
}

export interface CutSuggestion {
  categoryId: CategoryId;
  icon: string;
  label: string;
  /** ile grosze/miesiąc do wycięcia */
  cutMinor: number;
  /** o ile tygodni przyspiesza cel */
  weeksSaved: number;
  hint: string;
}

export const categories: Category[] = [
  { id: "jedzenie", label: "Jedzenie", tone: "lime", icon: "🥦", budgetMinor: 120000, spentMinor: 87450 },
  { id: "transport", label: "Transport", tone: "cyan", icon: "🚇", budgetMinor: 40000, spentMinor: 21300 },
  { id: "mieszkanie", label: "Mieszkanie", tone: "violet", icon: "🏠", budgetMinor: 230000, spentMinor: 230000 },
  { id: "rozrywka", label: "Rozrywka", tone: "coral", icon: "🎧", budgetMinor: 50000, spentMinor: 61200 },
  { id: "zdrowie", label: "Zdrowie", tone: "amber", icon: "💊", budgetMinor: 30000, spentMinor: 9900 },
  { id: "subskrypcje", label: "Subskrypcje", tone: "muted", icon: "📺", budgetMinor: 15000, spentMinor: 13797 },
];

export const transactions: Tx[] = [
  { id: "t1", title: "Żabka — kawa i drożdżówka", category: "jedzenie", amountMinor: -1490, date: "2026-07-30T08:12:00", method: "BLIK" },
  { id: "t2", title: "Bilet miesięczny ZTM", category: "transport", amountMinor: -11000, date: "2026-07-29T18:40:00", method: "Karta" },
  { id: "t3", title: "Wypłata — lipiec", category: "inne", amountMinor: 812000, date: "2026-07-28T09:00:00", method: "Przelew" },
  { id: "t4", title: "Spotify Family", category: "subskrypcje", amountMinor: -2999, date: "2026-07-27T12:05:00", method: "Karta" },
  { id: "t5", title: "Biedronka — duże zakupy", category: "jedzenie", amountMinor: -23784, date: "2026-07-26T17:22:00", method: "BLIK" },
  { id: "t6", title: "Kino Helios", category: "rozrywka", amountMinor: -4800, date: "2026-07-25T20:10:00", method: "Gotówka" },
];

export const goals: Goal[] = [
  { id: "g1", label: "Poduszka bezpieczeństwa", targetMinor: 1500000, savedMinor: 962000, deadline: "2026-12-31", monthlyContribMinor: 90000, priority: 2 },
  { id: "g2", label: "Wyjazd w Bieszczady", targetMinor: 350000, savedMinor: 128000, deadline: "2026-09-15", monthlyContribMinor: 55000, priority: 1 },
];

export const weeklySpend = [
  { day: "Pn", value: 4200 },
  { day: "Wt", value: 11800 },
  { day: "Śr", value: 2600 },
  { day: "Cz", value: 8900 },
  { day: "Pt", value: 15400 },
  { day: "So", value: 23700 },
  { day: "Nd", value: 6100 },
];

/* ---------- PRZEPŁYW DZIENNY: wydatki vs dochody ---------- */

export interface DayFlow {
  day: string;
  /** grosze wydane danego dnia (wartość dodatnia) */
  expenseMinor: number;
  /** grosze, które wpłynęły danego dnia */
  incomeMinor: number;
  isToday?: boolean;
}

/** 7 ostatnich dni — podstawa histogramu na Pulpicie. */
export const dailyFlow: DayFlow[] = [
  { day: "Pn", expenseMinor: 4200, incomeMinor: 0 },
  { day: "Wt", expenseMinor: 11800, incomeMinor: 0 },
  { day: "Śr", expenseMinor: 2600, incomeMinor: 15000 },
  { day: "Cz", expenseMinor: 8900, incomeMinor: 0 },
  { day: "Pt", expenseMinor: 15400, incomeMinor: 812000 },
  { day: "So", expenseMinor: 23700, incomeMinor: 4000 },
  { day: "Nd", expenseMinor: 6100, incomeMinor: 0, isToday: true },
];

export function dayBalanceMinor(d: DayFlow): number {
  return d.incomeMinor - d.expenseMinor;
}

/** Największa wartość (wydatek lub dochód) — skala osi histogramu. */
export function flowScaleMinor(days: DayFlow[] = dailyFlow): number {
  return Math.max(1, ...days.flatMap((d) => [d.expenseMinor, d.incomeMinor]));
}

export const todayFlow: DayFlow = dailyFlow[dailyFlow.length - 1];

export function weekTotals(days: DayFlow[] = dailyFlow) {
  const expenseMinor = days.reduce((s, d) => s + d.expenseMinor, 0);
  const incomeMinor = days.reduce((s, d) => s + d.incomeMinor, 0);
  return { expenseMinor, incomeMinor, balanceMinor: incomeMinor - expenseMinor };
}

/** Krótki format kwoty do widgetów 2x1: „1,2 tys.” / „42,10”. */
export function plnShort(minor: number): string {
  const v = Math.abs(minor) / 100;
  if (v >= 1000) return `${(v / 1000).toFixed(1).replace(".", ",")} tys.`;
  return v.toFixed(2).replace(".", ",");
}


/** Formatowanie PLN: 1 234,56 zł — zgodne z pl-PL. */
export function pln(minor: number, opts?: { sign?: boolean }): string {
  const value = minor / 100;
  const formatted = new Intl.NumberFormat("pl-PL", {
    style: "currency",
    currency: "PLN",
    minimumFractionDigits: 2,
  }).format(Math.abs(value));
  if (!opts?.sign) return formatted;
  return `${minor < 0 ? "−" : "+"} ${formatted}`;
}

export const toneClass: Record<Category["tone"], string> = {
  lime: "text-lime",
  cyan: "text-cyan",
  coral: "text-coral",
  amber: "text-amber",
  violet: "text-violet",
  muted: "text-muted-foreground",
};

export const toneBg: Record<Category["tone"], string> = {
  lime: "bg-lime",
  cyan: "bg-cyan",
  coral: "bg-coral",
  amber: "bg-amber",
  violet: "bg-violet",
  muted: "bg-muted-foreground",
};

/** Wskaźnik motywacyjny: 0–100, im wyżej tym lepiej oszczędzasz. */
export const savingScore = 78;
export const streakDays = 12;

/* ---------- CELE: kamienie milowe, prognozy, sugestie ---------- */

const MILESTONE_REWARDS: Record<number, string> = {
  25: "Pierwsza ćwiartka",
  50: "Półmetek",
  75: "Ostatnia prosta",
  100: "Cel domknięty",
};

export function goalPct(g: Goal): number {
  return Math.min(100, Math.round((g.savedMinor / g.targetMinor) * 100));
}

export function milestonesFor(g: Goal): Milestone[] {
  const pct = goalPct(g);
  return ([25, 50, 75, 100] as const).map((p) => ({
    pct: p,
    reward: MILESTONE_REWARDS[p],
    unlocked: pct >= p,
  }));
}

/** Ile miesięcy do celu przy obecnym tempie odkładania. */
export function monthsToGoal(g: Goal, extraMonthlyMinor = 0): number {
  const rate = g.monthlyContribMinor + extraMonthlyMinor;
  if (rate <= 0) return Infinity;
  return Math.max(0, (g.targetMinor - g.savedMinor) / rate);
}

/** Szacowana data osiągnięcia celu, np. „paź 2026”. */
export function goalEta(g: Goal, extraMonthlyMinor = 0, from = new Date("2026-07-30")): string {
  const m = monthsToGoal(g, extraMonthlyMinor);
  if (!Number.isFinite(m)) return "—";
  const d = new Date(from);
  d.setMonth(d.getMonth() + Math.ceil(m));
  return new Intl.DateTimeFormat("pl-PL", { month: "short", year: "numeric" }).format(d);
}

/**
 * Sugestie „co zmniejszyć”: kategorie z największym przekroczeniem / udziałem.
 * Cięcie = 100% nadwyżki ponad limit, a gdy w limicie — 15% wydatku.
 */
export function suggestionsForGoal(g: Goal, limit = 3): CutSuggestion[] {
  const baseMonths = monthsToGoal(g);
  return categories
    .map((c) => {
      const over = Math.max(0, c.spentMinor - c.budgetMinor);
      const cutMinor = Math.round((over > 0 ? over : c.spentMinor * 0.15) / 100) * 100;
      const weeksSaved = Math.max(
        1,
        Math.round((baseMonths - monthsToGoal(g, cutMinor)) * 4.345),
      );
      return {
        categoryId: c.id,
        icon: c.icon,
        label: c.label,
        cutMinor,
        weeksSaved,
        hint: over > 0 ? `przekroczone o ${pln(over)}` : `−15% miesięcznie`,
      };
    })
    .sort((a, b) => b.cutMinor - a.cutMinor)
    .slice(0, limit);
}

/** Szybkie dorzucenia do celu (grosze). */
export const quickTopUps = [1000, 2000, 5000];

/* ---------- HISTORIA WPŁAT NA CEL ---------- */

export interface Contribution {
  id: string;
  goalId: string;
  /** grosze; dodatnia = wpłata, ujemna = wypłata z celu */
  amountMinor: number;
  date: string; // ISO
  source: "Ręcznie" | "Auto" | "Zaokrąglenie" | "Wyzwanie" | "Cięcie";
  note?: string;
}

export const contributions: Contribution[] = [
  { id: "c1", goalId: "g2", amountMinor: 18000, date: "2026-07-28T09:05:00", source: "Auto", note: "Stałe zlecenie po wypłacie" },
  { id: "c2", goalId: "g2", amountMinor: 4500, date: "2026-07-24T20:31:00", source: "Wyzwanie", note: "Tydzień bez dowozu" },
  { id: "c3", goalId: "g2", amountMinor: 1230, date: "2026-07-21T11:48:00", source: "Zaokrąglenie", note: "Reszty z 14 transakcji" },
  { id: "c4", goalId: "g2", amountMinor: -6000, date: "2026-07-16T17:02:00", source: "Ręcznie", note: "Wypłata na serwis roweru" },
  { id: "c5", goalId: "g2", amountMinor: 9000, date: "2026-07-10T08:15:00", source: "Cięcie", note: "Rozrywka −90 zł" },
  { id: "c6", goalId: "g2", amountMinor: 18000, date: "2026-06-28T09:04:00", source: "Auto", note: "Stałe zlecenie po wypłacie" },
  { id: "c7", goalId: "g1", amountMinor: 30000, date: "2026-07-28T09:05:00", source: "Auto", note: "Stałe zlecenie po wypłacie" },
  { id: "c8", goalId: "g1", amountMinor: 7500, date: "2026-07-12T13:20:00", source: "Ręcznie" },
];

export function contributionsFor(goalId: string): Contribution[] {
  return contributions
    .filter((c) => c.goalId === goalId)
    .sort((a, b) => (a.date < b.date ? 1 : -1));
}

/** Średnie realne tempo z ostatnich `months` miesięcy (grosze/mies.). */
export function actualMonthlyRate(goalId: string, months = 2): number {
  const list = contributionsFor(goalId);
  if (list.length === 0) return 0;
  const sum = list.reduce((s, c) => s + c.amountMinor, 0);
  return Math.round(sum / months);
}

/** Prognoza oparta na realnym tempie wpłat, a nie na deklarowanym. */
export function goalEtaFromHistory(g: Goal): { eta: string; rateMinor: number; drift: number } {
  const rate = actualMonthlyRate(g.id);
  const declared = g.monthlyContribMinor;
  const eta =
    rate > 0
      ? goalEta({ ...g, monthlyContribMinor: rate })
      : "—";
  return { eta, rateMinor: rate, drift: rate - declared };
}

/** Miesięczne sumy wpłat — do mini-wykresu w szczegółach celu. */
export function monthlyContribSeries(goalId: string): { label: string; totalMinor: number }[] {
  const buckets = new Map<string, number>();
  for (const c of contributionsFor(goalId)) {
    const key = c.date.slice(0, 7);
    buckets.set(key, (buckets.get(key) ?? 0) + c.amountMinor);
  }
  return [...buckets.entries()]
    .sort((a, b) => (a[0] < b[0] ? -1 : 1))
    .map(([key, totalMinor]) => ({
      label: new Intl.DateTimeFormat("pl-PL", { month: "short" }).format(new Date(`${key}-01`)),
      totalMinor,
    }));
}
