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
