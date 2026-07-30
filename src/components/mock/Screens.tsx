import {
  categories,
  goals,
  pln,
  savingScore,
  streakDays,
  toneBg,
  toneClass,
  transactions,
  weeklySpend,
} from "@/data/mock";
import { StatusBar, TabBar } from "./PhoneFrame";

/* ---------- 1. PULPIT ---------- */
export function ScreenDashboard() {
  const max = Math.max(...weeklySpend.map((d) => d.value));
  return (
    <>
      <StatusBar title="Pulpit" />
      <div className="glow-top flex-1 overflow-hidden px-5 pt-4">
        <p className="text-[11px] uppercase tracking-[0.24em] text-muted-foreground">
          Zostało do końca lipca
        </p>
        <div className="mt-1 flex items-end gap-2">
          <span className="tabular text-[38px] font-semibold leading-none text-foreground">
            2 148,53
          </span>
          <span className="pb-1 text-lg text-lime">zł</span>
        </div>
        <p className="mt-2 text-xs text-muted-foreground">
          To <span className="text-lime">71 zł/dzień</span> — o 12 zł więcej niż w czerwcu.
        </p>

        <div className="mt-5 rounded-2xl border border-border bg-surface p-4">
          <div className="flex items-center justify-between">
            <span className="text-[11px] uppercase tracking-[0.2em] text-muted-foreground">
              Puls oszczędzania
            </span>
            <span className="tabular text-sm text-lime">{savingScore}/100</span>
          </div>
          <div className="mt-3 h-1.5 w-full overflow-hidden rounded-full bg-surface-2">
            <div className="h-full rounded-full bg-lime" style={{ width: `${savingScore}%` }} />
          </div>
          <p className="mt-3 text-xs text-muted-foreground">
            🔥 {streakDays} dni bez wydatku impulsowego. Jeszcze 3 i odblokujesz odznakę
            <span className="text-lime"> „Twarda Waluta”</span>.
          </p>
        </div>

        <div className="mt-5 flex h-24 items-end gap-2">
          {weeklySpend.map((d) => (
            <div key={d.day} className="flex flex-1 flex-col items-center gap-1.5">
              <div
                className={`w-full rounded-t-md ${d.value === max ? "bg-coral" : "bg-surface-2"}`}
                style={{ height: `${(d.value / max) * 70}px` }}
              />
              <span className="tabular text-[9px] text-muted-foreground">{d.day}</span>
            </div>
          ))}
        </div>

        <div className="mt-4 space-y-2">
          {transactions.slice(0, 3).map((t) => {
            const cat = categories.find((c) => c.id === t.category);
            return (
              <div
                key={t.id}
                className="flex items-center gap-3 rounded-xl border border-border bg-surface px-3 py-2.5"
              >
                <span className="text-base">{cat?.icon ?? "💸"}</span>
                <div className="min-w-0 flex-1">
                  <p className="truncate text-xs text-foreground">{t.title}</p>
                  <p className="tabular text-[10px] text-muted-foreground">{t.method}</p>
                </div>
                <span
                  className={`tabular text-xs ${t.amountMinor < 0 ? "text-foreground" : "text-lime"}`}
                >
                  {pln(t.amountMinor, { sign: true })}
                </span>
              </div>
            );
          })}
        </div>
      </div>
      <TabBar active="home" />
    </>
  );
}

/* ---------- 2. DODAWANIE WYDATKU ---------- */
export function ScreenAdd() {
  return (
    <>
      <StatusBar title="Nowy wydatek" />
      <div className="flex-1 px-5 pt-6">
        <p className="text-[11px] uppercase tracking-[0.24em] text-muted-foreground">Kwota</p>
        <div className="mt-2 flex items-end gap-2 border-b border-lime/40 pb-3">
          <span className="tabular text-[44px] font-semibold leading-none text-foreground">
            34,90
          </span>
          <span className="pb-1.5 text-xl text-lime">zł</span>
        </div>

        <div className="mt-5 grid grid-cols-3 gap-2">
          {categories.slice(0, 6).map((c, i) => (
            <div
              key={c.id}
              className={`rounded-xl border px-2 py-3 text-center ${
                i === 0 ? "neon-ring border-lime/50 bg-lime/10" : "border-border bg-surface"
              }`}
            >
              <div className="text-lg">{c.icon}</div>
              <div className="mt-1 text-[10px] text-muted-foreground">{c.label}</div>
            </div>
          ))}
        </div>

        <div className="mt-5 rounded-xl border border-border bg-surface p-3">
          <p className="text-[10px] uppercase tracking-[0.2em] text-muted-foreground">Opis</p>
          <p className="mt-1 text-sm text-foreground">Lunch na mieście</p>
        </div>

        <div className="mt-3 flex gap-2">
          {["BLIK", "Karta", "Gotówka"].map((m, i) => (
            <span
              key={m}
              className={`rounded-full border px-3 py-1.5 text-[11px] ${
                i === 0
                  ? "border-cyan/50 bg-cyan/10 text-cyan"
                  : "border-border bg-surface text-muted-foreground"
              }`}
            >
              {m}
            </span>
          ))}
        </div>

        <div className="mt-5 rounded-xl border border-coral/30 bg-coral/10 p-3">
          <p className="text-xs text-coral">
            Uwaga: to 4. lunch w tym tygodniu. Gotowanie w domu zostawiłoby ci ok. 96 zł.
          </p>
        </div>

        <div className="neon-ring mt-6 rounded-full bg-lime py-3.5 text-center text-sm font-semibold text-primary-foreground">
          Zapisz wydatek
        </div>
      </div>
      <TabBar active="add" />
    </>
  );
}

/* ---------- 3. KATEGORIE ---------- */
export function ScreenCategories() {
  return (
    <>
      <StatusBar title="Kategorie" />
      <div className="flex-1 overflow-hidden px-5 pt-4">
        <p className="text-[11px] uppercase tracking-[0.24em] text-muted-foreground">
          Lipiec 2026 · wydano
        </p>
        <p className="tabular mt-1 text-[30px] font-semibold leading-none">4 236,47 zł</p>

        <div className="mt-5 space-y-3">
          {categories.map((c) => {
            const pct = Math.min(150, Math.round((c.spentMinor / c.budgetMinor) * 100));
            const over = c.spentMinor > c.budgetMinor;
            return (
              <div key={c.id} className="rounded-xl border border-border bg-surface px-3 py-2.5">
                <div className="flex items-center gap-2">
                  <span>{c.icon}</span>
                  <span className="flex-1 text-xs text-foreground">{c.label}</span>
                  <span className={`tabular text-xs ${over ? "text-coral" : toneClass[c.tone]}`}>
                    {pln(c.spentMinor)}
                  </span>
                </div>
                <div className="mt-2 h-1 w-full overflow-hidden rounded-full bg-surface-2">
                  <div
                    className={`h-full rounded-full ${over ? "bg-coral" : toneBg[c.tone]}`}
                    style={{ width: `${Math.min(100, pct)}%` }}
                  />
                </div>
                <div className="tabular mt-1.5 flex justify-between text-[10px] text-muted-foreground">
                  <span>limit {pln(c.budgetMinor)}</span>
                  <span className={over ? "text-coral" : ""}>{pct}%</span>
                </div>
              </div>
            );
          })}
        </div>
      </div>
      <TabBar active="cats" />
    </>
  );
}

/* ---------- 4. CELE / MOTYWACJA ---------- */
export function ScreenGoals() {
  return (
    <>
      <StatusBar title="Cele" />
      <div className="glow-top flex-1 px-5 pt-4">
        <p className="text-[11px] uppercase tracking-[0.24em] text-muted-foreground">
          Odłożone łącznie
        </p>
        <p className="tabular mt-1 text-[32px] font-semibold leading-none text-lime">10 900,00 zł</p>

        <div className="mt-5 space-y-3">
          {goals.map((g) => {
            const pct = Math.round((g.savedMinor / g.targetMinor) * 100);
            return (
              <div key={g.id} className="rounded-2xl border border-border bg-surface p-4">
                <div className="flex items-baseline justify-between">
                  <span className="text-sm text-foreground">{g.label}</span>
                  <span className="tabular text-xs text-lime">{pct}%</span>
                </div>
                <div className="mt-3 h-2 w-full overflow-hidden rounded-full bg-surface-2">
                  <div className="h-full rounded-full bg-lime" style={{ width: `${pct}%` }} />
                </div>
                <div className="tabular mt-2 flex justify-between text-[10px] text-muted-foreground">
                  <span>{pln(g.savedMinor)}</span>
                  <span>z {pln(g.targetMinor)}</span>
                </div>
              </div>
            );
          })}
        </div>

        <div className="mt-5 rounded-2xl border border-cyan/30 bg-cyan/10 p-4">
          <p className="text-[11px] uppercase tracking-[0.2em] text-cyan">Wyzwanie tygodnia</p>
          <p className="mt-2 text-sm text-foreground">
            Nie zamawiaj jedzenia przez 7 dni → +180 zł do Bieszczad.
          </p>
          <div className="mt-3 flex gap-1">
            {Array.from({ length: 7 }).map((_, i) => (
              <span
                key={i}
                className={`h-1.5 flex-1 rounded-full ${i < 4 ? "bg-cyan" : "bg-surface-2"}`}
              />
            ))}
          </div>
        </div>
      </div>
      <TabBar active="goals" />
    </>
  );
}
