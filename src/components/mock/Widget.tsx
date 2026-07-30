import { pln } from "@/data/mock";

/** Widget 4x2 — "Pasek dnia" */
export function WidgetWide() {
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
      <div className="mt-3 h-1.5 w-full overflow-hidden rounded-full bg-surface-2">
        <div className="h-full w-[59%] rounded-full bg-lime" />
      </div>
      <div className="tabular mt-2 flex justify-between text-[10px] text-muted-foreground">
        <span>Wydano dziś {pln(2890)}</span>
        <span className="text-lime">🔥 12 dni</span>
      </div>
    </div>
  );
}

/** Widget 2x2 — "Szybki BLIK" */
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
