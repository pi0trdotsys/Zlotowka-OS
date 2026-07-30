import { createFileRoute, Link } from "@tanstack/react-router";
import { PhoneFrame } from "@/components/mock/PhoneFrame";
import {
  ScreenAdd,
  ScreenBudget,
  ScreenCategories,
  ScreenDashboard,
  ScreenGoals,
} from "@/components/mock/Screens";
import { AndroidHome } from "@/components/mock/Widget";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "ZŁOTÓWKA OS — makiety aplikacji do budżetu w PLN" },
      {
        name: "description",
        content:
          "Makiety UI aplikacji do śledzenia dochodów i wydatków w złotówkach: pulpit, dodawanie wydatku, kategorie, cele oszczędnościowe i widget na Androida.",
      },
      { property: "og:title", content: "ZŁOTÓWKA OS — makiety aplikacji do budżetu w PLN" },
      {
        property: "og:description",
        content:
          "Future-minimalizm, polska waluta, motywacja do oszczędzania. Makiety + dokumentacja pod natywną apkę w Kotlinie.",
      },
    ],
  }),
  component: Index,
});

function Index() {
  return (
    <main className="grid-bg min-h-screen">
      <header className="glow-top border-b border-border px-6 py-16 md:px-12">
        <div className="mx-auto max-w-6xl">
          <span className="tabular text-[11px] uppercase tracking-[0.3em] text-lime">
            makieta v0.1 · pl-PL
          </span>
          <h1 className="mt-4 max-w-3xl text-4xl font-semibold leading-[1.05] tracking-tight md:text-6xl">
            ZŁOTÓWKA <span className="text-lime">OS</span>
          </h1>
          <p className="mt-5 max-w-xl text-sm leading-relaxed text-muted-foreground md:text-base">
            Budżet osobisty jako panel sterowania. Każdy wydatek to zapis w rejestrze, każda
            zaoszczędzona złotówka to punkt. Zaprojektowane pod polską codzienność: BLIK, Żabka,
            bilet ZTM, wypłata 10-tego.
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Link
              to="/dokumentacja"
              className="neon-ring rounded-full bg-lime px-5 py-2.5 text-sm font-semibold text-primary-foreground"
            >
              Dokumentacja dla Kotlina
            </Link>
            <a
              href="#widget"
              className="rounded-full border border-border px-5 py-2.5 text-sm text-foreground"
            >
              Widget Androida
            </a>
          </div>
        </div>
      </header>

      <section className="mx-auto max-w-6xl px-6 py-16 md:px-12">
        <h2 className="text-[11px] uppercase tracking-[0.28em] text-muted-foreground">
          01 — Ekrany aplikacji
        </h2>
        <div className="mt-8 flex flex-wrap justify-center gap-10 lg:justify-start">
          <PhoneFrame label="Pulpit" caption="Ile zostało dziś, puls oszczędzania, ostatnie ruchy.">
            <ScreenDashboard />
          </PhoneFrame>
          <PhoneFrame
            label="Nowy wydatek"
            caption="Trzy dotknięcia: kwota → kategoria → zapis. Z przytykiem od aplikacji."
          >
            <ScreenAdd />
          </PhoneFrame>
          <PhoneFrame label="Kategorie" caption="Limity miesięczne z sygnałem przekroczenia.">
            <ScreenCategories />
          </PhoneFrame>
          <PhoneFrame label="Cele" caption="Cele oszczędnościowe i wyzwanie tygodnia.">
            <ScreenGoals />
          </PhoneFrame>
        </div>
      </section>

      <section id="widget" className="border-t border-border">
        <div className="mx-auto grid max-w-6xl gap-12 px-6 py-16 md:grid-cols-2 md:px-12">
          <div>
            <h2 className="text-[11px] uppercase tracking-[0.28em] text-muted-foreground">
              02 — Widget na ekran główny
            </h2>
            <p className="mt-5 text-2xl font-semibold leading-tight">
              Budżet widoczny zanim odblokujesz telefon.
            </p>
            <ul className="mt-6 space-y-3 text-sm text-muted-foreground">
              <li>
                <span className="text-lime">4×2</span> — „Pasek dnia”: pozostały dzienny limit,
                pasek postępu, seria dni bez impulsu, przycisk szybkiego dodania.
              </li>
              <li>
                <span className="text-lime">2×2</span> — „Puls”: wynik oszczędzania 0–100 i trzy
                skróty kwot (10 / 20 / 50 zł).
              </li>
              <li>
                Aktualizacja przez <code className="text-cyan">GlanceAppWidget</code> +{" "}
                <code className="text-cyan">WorkManager</code>, dane z Room przez repozytorium.
              </li>
            </ul>
          </div>
          <div className="flex justify-center">
            <AndroidHome />
          </div>
        </div>
      </section>

      <footer className="border-t border-border px-6 py-10 text-center text-xs text-muted-foreground md:px-12">
        Makieta poglądowa — brak logiki biznesowej. Kwoty w groszach (Int/Long), formatowanie pl-PL.
      </footer>
    </main>
  );
}
