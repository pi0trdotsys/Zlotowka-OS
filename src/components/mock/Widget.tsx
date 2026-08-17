import { dailyFlow, flowScales, pln, plnShort, todayFlow, weekTotals } from "@/data/mock";

/* ============ 2x1 — pasek jednowierszowy (najważniejszy format) ============ */

/** 2x1 A — „Dzisiejszy bilans”: wpłynęło vs wydane + wynik dnia. */
export function Widget2x1Balance() {
  const balance = todayFlow.incomeMinor - todayFlow.expenseMinor;
  return (
    <div className="flex w-[170px] items-center justify-between rounded-2xl border border-border bg-surface/90 px-3 py-2.5 backdrop-blur">
      <div>
        <p className="text-[9px] uppercase tracking-[0.2em] text-muted-foreground">Bilans dnia</p>
        <p
          className={`tabular mt-0.5 text-lg font-semibold leading-none ${
            balance >= 0 ? "text-lime" : "text-coral"
          }`}
        >
          {pln(balance, { sign: true })}
        </p>
      </div>
      <div className="tabular text-right text-[9px] leading-tight">
        <p className="text-lime">+{plnShort(todayFlow.incomeMinor)}</p>
        <p className="text-coral">−{plnShort(todayFlow.expenseMinor)}</p>
      </div>
    </div>
  );
}

/** 2x1 B — „Zostało dziś” z cienkim paskiem limitu. */
export function Widget2x1Left() {
  return (
    <div className="w-[170px] rounded-2xl border border-border bg-surface/90 px-3 py-2.5 backdrop-blur">
      <div className="flex items-baseline justify-between">
        <p className="text-[9px] uppercase tracking-[0.2em] text-muted-foreground">Zostało dziś</p>
        <span className="tabular text-[9px] text-muted-foreground">z 71 zł</span>
      </div>
      <p className="tabular mt-0.5 text-lg font-semibold leading-none">42,10 zł</p>
      <div className="mt-2 h-1 w-full overflow-hidden rounded-full bg-surface-2">
        <div className="h-full w-[59%] rounded-full bg-lime" />
      </div>
    </div>
  );
}

/** 2x1 C — mikro-histogram 7 dni (wpływy nad osią, wydatki pod). */
export function Widget2x1Spark() {
  const { incomeMax, expenseMax } = flowScales();
  return (
    <div className="flex w-[170px] items-center gap-3 rounded-2xl border border-border bg-surface/90 px-3 py-2.5 backdrop-blur">
      <div className="flex flex-1 items-stretch gap-[3px]">
        {dailyFlow.map((d) => (
          <div key={d.day} className="flex flex-1 flex-col items-center">
            <div className="flex h-[14px] w-full flex-col justify-end">
              <div
                className="w-full rounded-t-sm bg-lime"
                style={{ height: `${Math.round((d.incomeMinor / incomeMax) * 14)}px` }}
              />
            </div>
            <div className="h-px w-full bg-border" />
            <div className="h-[14px] w-full">
              <div
                className="w-full rounded-b-sm bg-coral"
                style={{ height: `${Math.round((d.expenseMinor / expenseMax) * 14)}px` }}
              />
            </div>
          </div>
        ))}
      </div>
      <div className="tabular text-right text-[9px] leading-tight">
        <p className="text-muted-foreground">7 dni</p>
        <p className={weekTotals().balanceMinor >= 0 ? "text-lime" : "text-coral"}>
          {plnShort(weekTotals().balanceMinor)}
        </p>
      </div>
    </div>
  );
}

/** 2x1 D — szybkie dodanie wydatku (BLIK-owe kwoty). */
export function Widget2x1QuickAdd() {
  return (
    <div className="flex w-[170px] items-center gap-2 rounded-2xl border border-border bg-surface/90 px-3 py-2.5 backdrop-blur">
      <div className="neon-ring flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-lime text-base text-primary-foreground">
        ＋
      </div>
      <div className="grid flex-1 grid-cols-3 gap-1">
        {["10", "20", "50"].map((v) => (
          <span
            key={v}
            className="tabular rounded-md bg-surface-2 py-1 text-center text-[10px] text-foreground"
          >
            {v}
          </span>
        ))}
      </div>
    </div>
  );
}

/* ============ 4x2 — „Pasek dnia” ============ */
export function WidgetWide() {
  const { incomeMax, expenseMax } = flowScales();
  return (
    <div className="w-full max-w-[340px] rounded-3xl border border-border bg-surface/90 p-4 backdrop-blur">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-[10px] uppercase tracking-[0.22em] text-muted-foreground">
            Dziś zostało
          </p>
          <p className="tabular mt-1 text-2xl font-semibold leading-none">42,10 zł</p>
        </div>
        <div className="neon-ring flex h-9 w-9 items-center justify-center rounded-full bg-lime text-lg text-primary-foreground">
          ＋
        </div>
      </div>
      <div className="mt-3 flex items-stretch gap-1">
        {dailyFlow.map((d) => (
          <div key={d.day} className="flex flex-1 flex-col items-center">
            <div className="flex h-[18px] w-full flex-col justify-end">
              <div
                className="w-full rounded-t-sm bg-lime"
                style={{ height: `${Math.round((d.incomeMinor / incomeMax) * 18)}px` }}
              />
            </div>
            <div className={`h-px w-full ${d.isToday ? "bg-cyan" : "bg-border"}`} />
            <div className="h-[18px] w-full">
              <div
                className="w-full rounded-b-sm bg-coral"
                style={{ height: `${Math.round((d.expenseMinor / expenseMax) * 18)}px` }}
              />
            </div>
            <span className="tabular mt-1 text-[8px] text-muted-foreground">{d.day}</span>
          </div>
        ))}
      </div>
      <div className="tabular mt-2 flex justify-between text-[10px]">
        <span className="text-coral">−{plnShort(todayFlow.expenseMinor)} dziś</span>
        <span className="text-lime">🔥 12 dni</span>
      </div>
    </div>
  );
}

/* ============ 2x2 — „Puls” ============ */
export function WidgetSmall() {
  return (
    <div className="w-[160px] rounded-3xl border border-border bg-surface/90 p-4">
      <p className="text-[10px] uppercase tracking-[0.2em] text-muted-foreground">Puls</p>
      <p className="tabular mt-1 text-3xl font-semibold leading-none text-lime">78</p>
      <p className="mt-1 text-[10px] text-muted-foreground">na 100 pkt</p>
      <div className="mt-3 grid grid-cols-3 gap-1">
        {["10", "20", "50"].map((v) => (
          <span
            key={v}
            className="tabular rounded-lg bg-surface-2 py-1 text-center text-[10px] text-foreground"
          >
            {v}
          </span>
        ))}
      </div>
    </div>
  );
}

export function AndroidHome() {
  return (
    <div className="relative w-full max-w-[380px] overflow-hidden rounded-[2.4rem] border border-border bg-[linear-gradient(200deg,oklch(0.24_0.03_260),oklch(0.14_0.01_250))] p-5">
      <div className="tabular flex justify-between text-[10px] text-muted-foreground">
        <span>9:41</span>
        <span>Android · ekran główny</span>
      </div>
      <div className="mt-6 space-y-4">
        <WidgetWide />
        <div className="grid grid-cols-2 gap-3">
          <Widget2x1Balance />
          <Widget2x1Left />
          <Widget2x1Spark />
          <Widget2x1QuickAdd />
        </div>
        <div className="flex gap-4">
          <WidgetSmall />
          <div className="grid flex-1 grid-cols-3 content-start gap-3">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="aspect-square rounded-2xl bg-surface-2/60" />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
