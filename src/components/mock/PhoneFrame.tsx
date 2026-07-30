import type { ReactNode } from "react";

export function PhoneFrame({
  label,
  caption,
  children,
}: {
  label: string;
  caption?: string;
  children: ReactNode;
}) {
  return (
    <figure className="flex w-full max-w-[320px] shrink-0 flex-col gap-4">
      <div className="relative rounded-[2.6rem] border border-border bg-surface-2/60 p-2 shadow-[0_40px_90px_-50px_rgba(0,0,0,0.95)]">
        <div className="relative h-[640px] w-full overflow-hidden rounded-[2.1rem] bg-background">
          <div className="pointer-events-none absolute left-1/2 top-2 z-20 h-6 w-24 -translate-x-1/2 rounded-full bg-black/80" />
          <div className="flex h-full flex-col overflow-hidden">{children}</div>
        </div>
      </div>
      <figcaption className="px-1">
        <div className="tabular text-[11px] uppercase tracking-[0.22em] text-lime">{label}</div>
        {caption ? <p className="mt-1 text-sm text-muted-foreground">{caption}</p> : null}
      </figcaption>
    </figure>
  );
}

export function StatusBar({ title }: { title: string }) {
  return (
    <div className="flex items-center justify-between px-5 pb-1 pt-3">
      <span className="tabular text-[10px] text-muted-foreground">9:41</span>
      <span className="tabular text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
        {title}
      </span>
      <span className="tabular text-[10px] text-muted-foreground">100%</span>
    </div>
  );
}

export function TabBar({ active }: { active: string }) {
  const items = [
    { id: "home", label: "Pulpit", icon: "◎" },
    { id: "add", label: "Dodaj", icon: "＋" },
    { id: "cats", label: "Kategorie", icon: "▤" },
    { id: "budget", label: "Budżet", icon: "◈" },
    { id: "goals", label: "Cele", icon: "◆" },
  ];
  return (
    <nav className="mt-auto flex items-center justify-between border-t border-border bg-surface/80 px-4 py-3 backdrop-blur">
      {items.map((i) => {
        const on = i.id === active;
        return (
          <div key={i.id} className="flex flex-1 flex-col items-center gap-1">
            <span className={on ? "text-lime" : "text-muted-foreground"}>{i.icon}</span>
            <span
              className={`text-[9px] uppercase tracking-[0.14em] ${
                on ? "text-foreground" : "text-muted-foreground"
              }`}
            >
              {i.label}
            </span>
          </div>
        );
      })}
    </nav>
  );
}
