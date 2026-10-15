// Sello v2 · Resumen (locked 2026-10-15). Synthetic, hand-authored figures (COP, no symbol).
// Financial date 2026-10-22. October is in progress and is never projected.

const CAT_ICONS = { Hogar: "home", Comida: "restaurant", Café: "local_cafe", Transporte: "directions_bus", Ocio: "theaters" };
// This month's recorded spending by category, highest first (sums to 400.000).
const RANK = [["Hogar", 200000], ["Comida", 80000], ["Café", 60000], ["Transporte", 40000], ["Ocio", 20000]];
// Full months May–September, then October so far. Average of full months: 3.890.000 / 5 = 778.000.
const MONTHS = [["May", 720000], ["Jun", 680000], ["Jul", 810000], ["Ago", 760000], ["Sep", 920000], ["Oct", 400000, "partial"]];
// Average spending per weekday over the last 4 complete months (synthetic layout example).
const WEEKDAYS = [["L", 18200], ["M", 21500], ["X", 19800], ["J", 24100], ["V", 31400], ["S", 42700], ["D", 27300]];

const COMPARE = {
  less: { now: 400000, before: 450000, diff: 50000, word: "menos", pct: "11,1", arrow: "↓" },
  more: { now: 600000, before: 450000, diff: 150000, word: "más", pct: "33,3", arrow: "↑" },
};
const short = n => n >= 1000000 ? `${String(n / 1000000).replace(".", ",")} M` : `${Math.round(n / 1000)} mil`;

// Hero: the difference is the answer (R1); two same-day bars underneath show the evidence (R2).
function compareHero(c, { month = "Octubre", prev = "septiembre", basis = "al mismo día del mes", rows } = {}) {
  const r = rows ?? [[month, c.now, true], [prev[0].toUpperCase() + prev.slice(1), c.before, false]];
  const cutoff = rows ? "Meses completos" : "Del 1 al 22 de cada mes";
  const max = Math.max(...r.map(x => x[1]));
  const bars = r.map(([label, n, now]) => `<div class="vs-row ${now ? "now" : ""}"><span>${label}</span><span class="bar" style="width:${100 * n / max}%"></span><b>${money(n)}</b></div>`).join("");
  return `<section class="slip pinked"><h2 class="q">${month === "Octubre" ? "¿Cómo va este mes?" : `¿Cómo cerró ${month.toLowerCase()}?`}</h2><p class="answer">${money(c.diff)}<small>${c.word} que en ${prev}</small></p><p class="trend"><span class="arrow" aria-hidden="true">${c.arrow}</span>${c.pct} % ${basis}</p><div class="tear" aria-hidden="true"></div><p class="basis">${cutoff}</p><div class="vs">${bars}</div></section>`;
}
const leaderLine = (label, n) => `<div class="leader"><span>${label}</span><i class="dots" aria-hidden="true"></i><b>${money(n)}</b></div>`;

function monthChart() {
  const max = 1250000, limit = 1000000;
  const highest = Math.max(...MONTHS.filter(m => !m[2]).map(m => m[1]));
  const cols = MONTHS.map(([m, n, kind]) => {
    const label = kind ? `${m}*` : m;
    const showValue = kind || n === highest;
    return `<button class="col ${kind ? "now partial" : ""}" aria-label="${m}${kind ? ", en curso hasta el 22" : ""}: ${fmt(n)} pesos">${showValue ? `<span class="v">${short(n)}</span>` : ""}<span class="b" style="height:${100 * n / max}%"></span></button>`;
  }).join("");
  const axis = MONTHS.map(([m, , kind]) => `<span class="${kind ? "now" : ""}">${kind ? m + "*" : m}</span>`).join("");
  return `<section class="slip"><h2 class="q">Mes a mes</h2><p class="chart-pick"><span>Un mes normal</span><b>${money(778000)}</b></p><p class="basis">Promedio de mayo a septiembre</p><div class="chart" role="group" aria-label="Gastos por mes">${cols}<div class="limit-line" style="bottom:${100 * limit / max}%"><span>Límite 1 M</span></div></div><div class="axis">${axis}</div><p class="chart-note">* Octubre va hasta el 22; no se proyecta. Toca una columna para ver su valor exacto.</p></section>`;
}

function ranking() {
  const total = RANK.reduce((a, [, n]) => a + n, 0);
  const rows = RANK.map(([name, n]) => `<button class="rank-row"><span class="circle" style="--fill:100%" aria-hidden="true">${icon(CAT_ICONS[name])}<span class="on">${icon(CAT_ICONS[name])}</span></span><span class="rank-main"><span>${name}</span><span class="share" aria-hidden="true"><i style="width:${100 * n / total}%"></i></span></span><b>${money(n)}</b>${icon("chevron_right", "chev")}</button>`).join("");
  return `<section class="slip"><h2 class="q">¿Dónde se va?</h2><p class="basis">Lo gastado en octubre, hasta el 22</p><div class="rank">${rows}</div><div class="total-line"><span>Total</span>${money(total)}</div></section>`;
}

function weekdays() {
  const max = Math.max(...WEEKDAYS.map(w => w[1]));
  const cols = WEEKDAYS.map(([d, n]) => `<button class="col ${n === max ? "now" : ""}" aria-label="${d}: ${fmt(n)} pesos en promedio">${n === max ? `<span class="v">${short(n)}</span>` : ""}<span class="b" style="height:${100 * n / (max * 1.15)}%"></span></button>`).join("");
  return `<section class="slip"><h2 class="q">¿Qué días gastas más?</h2><p class="answer" style="font-size:1.65em">Los sábados</p><p class="basis">${money(42700)} en promedio · últimos 4 meses completos</p><div class="chart" style="height:120px">${cols}</div><div class="axis">${WEEKDAYS.map(([d]) => `<span>${d}</span>`).join("")}</div></section>`;
}

// The evidence is a fact; the next step is a suggestion.
const EVIDENCE = `Café superó su límite en 3 de los últimos 4 meses: gastaste ${fmt(74000)} en promedio y su límite es ${fmt(50000)}.`;
// Owner pick: a tinted note with a light-bulb circle, dismissible.
const suggestion = () => `<section class="sug sug-tint"><div class="sug-head"><span class="sug-icon">${icon("insights")}</span><b>Una sugerencia</b><button class="icon-btn" aria-label="Descartar sugerencia">${icon("close")}</button></div><p class="sug-fact">${EVIDENCE}</p><p class="sug-advice">Podrías ajustar su límite para que refleje lo que de verdad gastas.</p><button class="text-link">Ajustar el presupuesto de Café${icon("chevron_right")}</button></section>`;
const income = (month = "octubre", inc = 2000000, net = 1600000) => `<section class="slip income-slip"><h2 class="q">Ingresos de ${month}</h2>${leaderLine("Ingresos anotados", inc)}${leaderLine("Ingresos menos gastos", net)}${leaderLine("Sin asignar a categorías", 1000000)}<button class="text-link">Ver ingresos y su destino${icon("chevron_right")}</button></section>`;

function body(o, { compare = "less", state } = {}) {
  if (state === "loading") return `<div role="status" aria-label="Cargando"><section class="slip"><div class="skel" style="width:40%"></div><div class="skel big"></div><div class="skel" style="width:70%"></div></section><section class="slip"><div class="skel" style="width:30%"></div><div class="skel" style="height:120px"></div></section></div>`;
  if (state === "failed") return `<section class="slip empty">${icon("error")}<h2>No pudimos leer tu resumen</h2><p>Tus registros siguen guardados. Vuelve a intentarlo.</p><button class="outline-btn">Reintentar</button></section>`;
  if (state === "none") return `<section class="slip empty">${icon("insights")}<h2>Aún no hay gastos este mes</h2><p>Cuando anotes tus gastos verás aquí cómo va el mes y dónde se va el dinero.</p><button class="outline-btn">Anotar gasto</button></section>`;
  const top = compareHero(COMPARE[compare]);
  if (state === "first") return `<section class="slip pinked"><h2 class="q">¿Cómo va este mes?</h2><p class="answer">${money(400000)}<small>gastados del 1 al 22</small></p><p class="basis">Cuando completes otro mes podrás comparar los mismos días.</p></section>${income()}${ranking()}<section class="slip empty" style="padding:18px"><p style="margin:0">Con un mes completo más verás aquí tu historia mes a mes, tus días de más gasto y sugerencias para tu presupuesto.</p></section>`;
  if (state === "past") return `${compareHero({ diff: 160000, word: "más", pct: "21,1", arrow: "↑" }, { month: "Septiembre", prev: "agosto", basis: "que agosto completo", rows: [["Septiembre", 920000, true], ["Agosto", 760000, false]] })}${income("septiembre", 2000000, 1080000)}${monthChart()}`;
  return `${top}${income()}${monthChart()}${ranking()}${weekdays()}${suggestion()}`;
}

const frame = (o, html, { tall = false, month, current } = {}) => `<div class="phone ${tall ? "tall" : ""}"><div class="app" data-ink="${o.ink}" data-theme="${o.theme}">${appShell({ body: html, tabs: 3, active: 1, month, current })}</div></div>`;
const card = (title, note, inner) => `<div class="frame-card"><h3>${title}</h3><p>${note}</p>${inner}</div>`;
const crop = (o, inner) => `<div class="app pad-crop" data-ink="${o.ink}" data-theme="${o.theme}" style="height:auto;display:block;padding:12px">${inner}</div>`;

function render() {
  const o = reviewOptions(["ink", "theme"]);
  const set = (id, h) => document.getElementById(id).innerHTML = h;
  set("hero",
    card("This month", "R1’s answer on top; R2’s same-day bars under the tear line. Income comes right after.", frame(o, body(o))) +
    card("When spending went up", "Same neutral ink and arrow: more spending is a fact, not a failure.", crop(o, compareHero(COMPARE.more))));
  set("whole", card("Whole screen, scrolled", "Hero → income → month by month → where it goes → which days → suggestion.", frame(o, body(o), { tall: true })));
  set("period",
    card("A past month · September", "The ribbon says which month you are viewing and brings you back in one tap. The hero compares full months; nothing is in progress; no suggestion.", frame(o, body(o, { state: "past" }), { tall: true, month: "Septiembre", current: "octubre" })));
  set("states",
    card("First month", "No comparable history yet: says what is missing, shows what is real, no invented comparisons or suggestions.", frame(o, body(o, { state: "first" }), { tall: true })) +
    card("No expenses this month", "Real empty state with a direct action.", frame(o, body(o, { state: "none" }))) +
    card("Loading", "Skeleton slips.", frame(o, body(o, { state: "loading" }))) +
    card("Failed read", "Never shown as zero or as an empty month.", frame(o, body(o, { state: "failed" }))));
}
document.querySelectorAll(".controls select").forEach(s => s.addEventListener("change", render));
render();
