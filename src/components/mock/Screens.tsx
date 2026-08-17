import {
  categories,
  contributionsFor,
  dailyFlow,
  dayBalanceMinor,
  flowScaleMinor,
  goalEta,
  goalEtaFromHistory,
  goalPct,
  goals,
  milestonesFor,
  monthlyContribSeries,
  pln,
  plnShort,
  quickTopUps,
  savingScore,
  streakDays,
  suggestionsForGoal,
  toneBg,
  toneClass,
  transactions,
  weekTotals,
} from "@/data/mock";
import { StatusBar, TabBar } from "./PhoneFrame";

/* ---------- HISTOGRAM PRZEPŁYWU: wydatki vs dochody ---------- */
export function FlowHistogram() {
  const scale = flowScaleMinor();
  const totals = weekTotals();
  const H = 62; // maks. wysokość słupka w px (w każdą stronę)
  return (
    <div className="mt-5 rounded-2xl border border-border bg-surface p-4">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-[11px] uppercase tracking-[0.2em] text-muted-foreground">
            Przepływ 7 dni
          </p>
          <p className="tabular mt-1 text-sm">
            <span className="text-lime">+{plnShort(totals.incomeMinor)}</span>
            <span className="text-muted-foreground"> / </span>
            <span className="text-coral">−{plnShort(totals.expenseMinor)}</span>
          </p>
        </div>
        <div className="flex flex-col items-end gap-1 text-[9px] text-muted-foreground">
          <span className="flex items-center gap-1">
            <i className="h-1.5 w-3 rounded-full bg-lime" /> wpływy
          </span>
          <span className="flex items-center gap-1">
            <i className="h-1.5 w-3 rounded-full bg-coral" /> wydatki
          </span>
        </div>
      </div>

      <div className="mt-4 flex items-stretch gap-1.5">
        {dailyFlow.map((d) => {
          const inH = Math.round((d.incomeMinor / scale) * H);
          const outH = Math.round((d.expenseMinor / scale) * H);
          return (
            <div key={d.day} className="flex flex-1 flex-col items-center">
              {/* wpływy — nad osią */}
              <div className="flex h-[62px] w-full flex-col justify-end">
                <div
                  className={`w-full rounded-t-md ${d.incomeMinor > 0 ? "bg-lime" : "bg-transparent"}`}
                  style={{ height: `${Math.max(d.incomeMinor > 0 ? 3 : 0, inH)}px` }}
                />
              </div>
              {/* oś zera */}
              <div
                className={`h-px w-full ${d.isToday ? "bg-cyan" : "bg-border"}`}
                aria-hidden
              />
              {/* wydatki — pod osią */}
              <div className="h-[62px] w-full">
                <div
                  className={`w-full rounded-b-md ${d.expenseMinor > 0 ? "bg-coral" : "bg-transparent"}`}
                  style={{ height: `${Math.max(d.expenseMinor > 0 ? 3 : 0, outH)}px` }}
                />
              </div>
              <span
                className={`tabular mt-1.5 text-[9px] ${d.isToday ? "text-cyan" : "text-muted-foreground"}`}
              >
                {d.day}
              </span>
            </div>
          );
        })}
      </div>

      <div className="tabular mt-3 flex items-center justify-between border-t border-border pt-3 text-[10px]">
        <span className="text-muted-foreground">Bilans tygodnia</span>
        <span className={totals.balanceMinor >= 0 ? "text-lime" : "text-coral"}>
          {pln(totals.balanceMinor, { sign: true })}
        </span>
      </div>
      <div className="tabular mt-1 flex items-center justify-between text-[10px]">
        <span className="text-muted-foreground">Dziś</span>
        <span className={dayBalanceMinor(dailyFlow[6]) >= 0 ? "text-lime" : "text-coral"}>
          {pln(dayBalanceMinor(dailyFlow[6]), { sign: true })}
        </span>
      </div>
    </div>
  );
}

/* ---------- 1. PULPIT ---------- */
export function ScreenDashboard() {
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

        <FlowHistogram />

        <div className="mt-4 space-y-2">
          {transactions.slice(0, 2).map((t) => {
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
  const sorted = [...goals].sort((a, b) => a.priority - b.priority);
  const main = sorted[0];
  const mainPct = goalPct(main);
  const milestones = milestonesFor(main);
  const suggestions = suggestionsForGoal(main);
  const best = suggestions[0];

  return (
    <>
      <StatusBar title="Cele" />
      <div className="glow-top flex-1 overflow-y-auto px-5 pt-4 pb-4">
        <p className="text-[11px] uppercase tracking-[0.24em] text-muted-foreground">Cel główny</p>
        <div className="mt-1 flex items-baseline justify-between">
          <span className="text-sm text-foreground">{main.label}</span>
          <span className="tabular text-xs text-lime">{mainPct}%</span>
        </div>
        <p className="tabular mt-2 text-[32px] font-semibold leading-none text-lime">
          {pln(main.savedMinor)}
        </p>
        <p className="tabular mt-1 text-[11px] text-muted-foreground">
          z {pln(main.targetMinor)} · brakuje {pln(main.targetMinor - main.savedMinor)}
        </p>
        <div className="mt-3 h-2 w-full overflow-hidden rounded-full bg-surface-2">
          <div className="h-full rounded-full bg-lime" style={{ width: `${mainPct}%` }} />
        </div>
        <p className="mt-2 text-[11px] text-muted-foreground">
          Przy {pln(main.monthlyContribMinor)}/mies. dojdziesz{" "}
          <span className="text-lime">{goalEta(main)}</span>.
        </p>

        {/* mikro-nagrody */}
        <p className="mt-5 text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
          Mikro-nagrody
        </p>
        <div className="mt-2 grid grid-cols-4 gap-1.5">
          {milestones.map((m) => (
            <div
              key={m.pct}
              className={`rounded-xl border px-1.5 py-2 text-center ${
                m.unlocked ? "border-lime/50 bg-lime/10" : "border-border bg-surface"
              }`}
            >
              <div className={`text-sm ${m.unlocked ? "text-lime" : "text-muted-foreground"}`}>
                {m.unlocked ? "◆" : "◇"}
              </div>
              <div className="tabular mt-1 text-[9px] text-muted-foreground">{m.pct}%</div>
              <div
                className={`mt-0.5 text-[8px] leading-tight ${
                  m.unlocked ? "text-foreground" : "text-muted-foreground"
                }`}
              >
                {m.reward}
              </div>
            </div>
          ))}
        </div>

        {/* sugestie cięć */}
        <p className="mt-5 text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
          Co zmniejszyć, by przyspieszyć
        </p>
        <div className="mt-2 space-y-2">
          {suggestions.map((s) => (
            <div
              key={s.categoryId}
              className="flex items-center gap-3 rounded-xl border border-border bg-surface px-3 py-2.5"
            >
              <span className="text-base">{s.icon}</span>
              <div className="min-w-0 flex-1">
                <p className="truncate text-xs text-foreground">
                  {s.label} <span className="tabular text-coral">−{pln(s.cutMinor)}</span>/mies.
                </p>
                <p className="tabular text-[10px] text-muted-foreground">
                  {s.hint} · szybciej o {s.weeksSaved} tyg.
                </p>
              </div>
              <span className="rounded-full border border-cyan/40 bg-cyan/10 px-2.5 py-1 text-[10px] text-cyan">
                Zastosuj
              </span>
            </div>
          ))}
        </div>

        {/* pozostałe cele */}
        <p className="mt-5 text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
          Wszystkie cele
        </p>
        <div className="mt-2 space-y-3">
          {sorted.map((g) => {
            const pct = goalPct(g);
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
                  <span>z {pln(g.targetMinor)} · {goalEta(g)}</span>
                </div>
              </div>
            );
          })}
        </div>

        <div className="mt-5 rounded-2xl border border-cyan/30 bg-cyan/10 p-4">
          <p className="text-[11px] uppercase tracking-[0.2em] text-cyan">Wyzwanie tygodnia</p>
          <p className="mt-2 text-sm text-foreground">
            Nie zamawiaj jedzenia przez 7 dni → +180 zł do {main.label}.
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

        {best ? (
          <p className="mt-4 text-[11px] text-muted-foreground">
            Tnij {best.label.toLowerCase()} — cel wpada już{" "}
            <span className="text-lime">{goalEta(main, best.cutMinor)}</span>.
          </p>
        ) : null}

        <div className="mt-3 flex items-center gap-2">
          <span className="text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
            Dorzuć
          </span>
          {quickTopUps.map((v) => (
            <span
              key={v}
              className="tabular rounded-full border border-lime/40 bg-lime/10 px-3 py-1.5 text-[11px] text-lime"
            >
              +{pln(v)}
            </span>
          ))}
        </div>
      </div>
      <TabBar active="goals" />
    </>
  );
}

/* ---------- 5. BUDŻET MIESIĘCZNY ---------- */
export function ScreenBudget() {
  const budget = categories.reduce((s, c) => s + c.budgetMinor, 0);
  const spent = categories.reduce((s, c) => s + c.spentMinor, 0);
  const left = budget - spent;
  const usedPct = Math.round((spent / budget) * 100);
  const daysLeft = 2;
  const overCats = categories.filter((c) => c.spentMinor > c.budgetMinor);
  const safeCats = categories.filter((c) => c.spentMinor / c.budgetMinor <= 0.75);

  return (
    <>
      <StatusBar title="Budżet" />
      <div className="glow-top flex-1 overflow-hidden px-5 pt-4">
        <p className="text-[11px] uppercase tracking-[0.24em] text-muted-foreground">
          Lipiec 2026 · plan miesięczny
        </p>
        <div className="mt-1 flex items-end gap-2">
          <span className="tabular text-[34px] font-semibold leading-none text-foreground">
            {pln(left)}
          </span>
          <span className="pb-1 text-[11px] uppercase tracking-[0.18em] text-muted-foreground">
            wolne
          </span>
        </div>
        <p className="mt-2 text-xs text-muted-foreground">
          Wykorzystano <span className={usedPct > 90 ? "text-coral" : "text-lime"}>{usedPct}%</span>{" "}
          z {pln(budget)} · zostały {daysLeft} dni.
        </p>

        <div className="mt-3 h-2 w-full overflow-hidden rounded-full bg-surface-2">
          <div
            className={`h-full rounded-full ${usedPct > 90 ? "bg-coral" : "bg-lime"}`}
            style={{ width: `${Math.min(100, usedPct)}%` }}
          />
        </div>

        <div className="mt-4 grid grid-cols-3 gap-2">
          {[
            { k: "W limicie", v: `${safeCats.length}/${categories.length}`, c: "text-lime" },
            { k: "Przekroczone", v: String(overCats.length), c: "text-coral" },
            { k: "Dzienny luz", v: pln(Math.max(0, Math.round(left / daysLeft))), c: "text-cyan" },
          ].map((s) => (
            <div key={s.k} className="rounded-xl border border-border bg-surface px-2 py-2.5">
              <p className="text-[9px] uppercase tracking-[0.14em] text-muted-foreground">{s.k}</p>
              <p className={`tabular mt-1 text-xs ${s.c}`}>{s.v}</p>
            </div>
          ))}
        </div>

        <div className="mt-4 space-y-2">
          {categories.slice(0, 4).map((c) => {
            const pct = Math.round((c.spentMinor / c.budgetMinor) * 100);
            const over = c.spentMinor > c.budgetMinor;
            return (
              <div key={c.id} className="flex items-center gap-3">
                <span className="text-sm">{c.icon}</span>
                <div className="min-w-0 flex-1">
                  <div className="flex justify-between text-[10px]">
                    <span className="text-foreground">{c.label}</span>
                    <span className={`tabular ${over ? "text-coral" : toneClass[c.tone]}`}>
                      {over ? `+${pln(c.spentMinor - c.budgetMinor)}` : pln(c.budgetMinor - c.spentMinor)}
                    </span>
                  </div>
                  <div className="mt-1 h-1 w-full overflow-hidden rounded-full bg-surface-2">
                    <div
                      className={`h-full rounded-full ${over ? "bg-coral" : toneBg[c.tone]}`}
                      style={{ width: `${Math.min(100, pct)}%` }}
                    />
                  </div>
                </div>
              </div>
            );
          })}
        </div>

        <div className="mt-4 rounded-2xl border border-lime/30 bg-lime/10 p-3">
          <p className="text-[10px] uppercase tracking-[0.2em] text-lime">Zachęta</p>
          <p className="mt-1.5 text-xs text-foreground">
            Zejdź z Rozrywką o 60 zł, a domkniesz miesiąc na plusie — to{" "}
            <span className="text-lime">+1 tydzień</span> szybciej w Bieszczadach.
          </p>
        </div>

        <div className="mt-2 flex gap-2">
          <span className="rounded-full border border-cyan/40 bg-cyan/10 px-3 py-1.5 text-[10px] text-cyan">
            Odłóż resztę → cel
          </span>
          <span className="rounded-full border border-border bg-surface px-3 py-1.5 text-[10px] text-muted-foreground">
            Puls {savingScore}/100 · 🔥 {streakDays} dni
          </span>
        </div>
      </div>
      <TabBar active="budget" />
    </>
  );
}

/* ---------- 6. SZCZEGÓŁY CELU ---------- */
export function ScreenGoalDetail() {
  const goal = [...goals].sort((a, b) => a.priority - b.priority)[0];
  const pct = goalPct(goal);
  const history = contributionsFor(goal.id);
  const series = monthlyContribSeries(goal.id);
  const maxSeries = Math.max(1, ...series.map((s) => Math.abs(s.totalMinor)));
  const { eta, rateMinor, drift } = goalEtaFromHistory(goal);
  const declaredEta = goalEta(goal);
  const faster = drift > 0;

  return (
    <>
      <StatusBar title="Szczegóły celu" />
      <div className="glow-top flex-1 overflow-y-auto px-5 pb-4 pt-4">
        <div className="flex items-baseline justify-between">
          <span className="text-sm text-foreground">{goal.label}</span>
          <span className="tabular text-xs text-lime">{pct}%</span>
        </div>
        <p className="tabular mt-2 text-[30px] font-semibold leading-none text-lime">
          {pln(goal.savedMinor)}
        </p>
        <p className="tabular mt-1 text-[11px] text-muted-foreground">
          z {pln(goal.targetMinor)} · termin {goal.deadline}
        </p>
        <div className="mt-3 h-2 w-full overflow-hidden rounded-full bg-surface-2">
          <div className="h-full rounded-full bg-lime" style={{ width: `${pct}%` }} />
        </div>

        {/* prognoza */}
        <div className="mt-4 rounded-2xl border border-border bg-surface p-4">
          <p className="text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
            Prognoza ukończenia
          </p>
          <p className="tabular mt-1.5 text-lg text-foreground">{eta}</p>
          <p className="mt-1 text-[11px] text-muted-foreground">
            Realne tempo <span className="tabular text-foreground">{pln(rateMinor)}/mies.</span> ·
            plan {pln(goal.monthlyContribMinor)} ({declaredEta})
          </p>
          <p className={`mt-2 text-[11px] ${faster ? "text-lime" : "text-coral"}`}>
            {faster
              ? `Wpłacasz o ${pln(drift)} więcej niż zakładałeś — prognoza przyspiesza.`
              : `Wpłacasz o ${pln(Math.abs(drift))} mniej niż plan — prognoza się cofa.`}
          </p>
        </div>

        {/* wykres miesięczny */}
        <p className="mt-5 text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
          Wpłaty miesięcznie
        </p>
        <div className="mt-2 flex h-20 items-end gap-2">
          {series.map((s) => (
            <div key={s.label} className="flex flex-1 flex-col items-center gap-1.5">
              <div
                className={`w-full rounded-t-md ${s.totalMinor < 0 ? "bg-coral" : "bg-lime"}`}
                style={{ height: `${(Math.abs(s.totalMinor) / maxSeries) * 56}px` }}
              />
              <span className="tabular text-[9px] text-muted-foreground">{s.label}</span>
            </div>
          ))}
        </div>

        {/* historia */}
        <p className="mt-5 text-[10px] uppercase tracking-[0.2em] text-muted-foreground">
          Historia wpłat
        </p>
        <div className="mt-2 space-y-2">
          {history.map((c) => (
            <div
              key={c.id}
              className="flex items-center gap-3 rounded-xl border border-border bg-surface px-3 py-2.5"
            >
              <span className="text-base">{c.amountMinor < 0 ? "↩" : "↑"}</span>
              <div className="min-w-0 flex-1">
                <p className="truncate text-xs text-foreground">{c.note ?? c.source}</p>
                <p className="tabular text-[10px] text-muted-foreground">
                  {c.date.slice(0, 10)} · {c.source}
                </p>
              </div>
              <span
                className={`tabular text-xs ${c.amountMinor < 0 ? "text-coral" : "text-lime"}`}
              >
                {pln(c.amountMinor, { sign: true })}
              </span>
            </div>
          ))}
        </div>

        <div className="mt-4 flex items-center gap-2">
          <span className="text-[10px] uppercase tracking-[0.18em] text-muted-foreground">
            Dorzuć
          </span>
          {quickTopUps.map((v) => (
            <span
              key={v}
              className="tabular rounded-full border border-lime/40 bg-lime/10 px-3 py-1.5 text-[11px] text-lime"
            >
              +{pln(v)}
            </span>
          ))}
        </div>
      </div>
      <TabBar active="goals" />
    </>
  );
}
