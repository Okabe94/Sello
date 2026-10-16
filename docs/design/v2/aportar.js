// Sello v2 · Aportar / Retirar (locked 2026-10-15). The locked entry screen's language for goal
// movements: pinked amount slip, Sello pad, goal chips, note row, fixed primary action.
// Synthetic goals (COP, no symbol). Recording here only tracks progress: no money moves and
// no category budget or income figure changes.

const GOALS = [["Viaje a la costa", "flight", 57, 1710000], ["Celular nuevo", "phone_iphone", 37, 450000], ["Curso de inglés", "school", 15, 300000]];

const SCENES = {
  fromGoal:  { mode: "in", amount: 350000, goal: "Viaje a la costa" },
  fromTab:   { mode: "in", amount: null, goal: null },
  withdraw:  { mode: "out", amount: 400000, goal: "Viaje a la costa", note: "Arreglo del carro" },
  tooMuch:   { mode: "out", amount: 2000000, goal: "Viaje a la costa", over: true },
};

const sizeClass = n => { const d = String(n).length; return d > 9 ? "long" : d > 6 ? "mid" : ""; };

function amountSlip(s) {
  const g = GOALS.find(x => x[0] === s.goal);
  const ic = g ? g[1] : "flag";
  const label = s.over
    ? `<span class="limit-msg" role="status">Más de lo guardado (${fmt(g[3])})</span>`
    : `<span>${s.mode === "in" ? "Aporte" : "Retiro"} en pesos</span>`;
  const value = s.amount === null
    ? `<div class="amount-value placeholder">0<span class="caret"></span></div>`
    : `<div class="amount-value ${sizeClass(s.amount)} ${s.mode === "in" ? "goal-in" : ""}">${s.mode === "in" ? "+" : "−"}${money(s.amount)}<span class="caret"></span></div>`;
  return `<section class="slip pinked amount-slip"><div class="amount-meta">${label}<button class="date-pill">${icon("event")}Hoy, jue 22</button></div><div class="amount-row"><span class="circle" style="--fill:${g ? 100 : 0}%;--c:var(--gain)" aria-hidden="true">${icon(ic)}<span class="on">${icon(ic)}</span></span>${value}</div></section>`;
}

function middle(s, o) {
  const chips = GOALS.map(([n, ic, pct]) => `<button class="chip" aria-pressed="${n === s.goal}">${icon(ic)}${n}<span class="goal-chip-pct">${pct} %</span></button>`).join("");
  const g = GOALS.find(x => x[0] === s.goal);
  const fact = g ? `<p class="goal-fact">Llevas <b>${fmt(g[3])}</b> guardados en esta meta.</p>` : "";
  const note = s.note ? `<button class="note-pill filled">${icon("edit_note")}<span>${s.note}</span><span class="edit">Editar</span></button>` : `<button class="note-pill">${icon("edit_note")}<span>Agregar nota</span></button>`;
  return `<div class="entry-mid"><h3>Meta</h3><div class="chips">${chips}</div>${fact}${note}</div>`;
}

const pad = () => `<div class="pad" role="group" aria-label="Teclado de Sello">${["1", "2", "3", "4", "5", "6", "7", "8", "9", "000", "0"].map(k => `<button class="${k === "000" ? "k000" : ""}">${k}</button>`).join("")}<button class="del" aria-label="Borrar">${icon("backspace")}</button></div>`;

function form(s, o) {
  const ok = s.amount !== null && s.goal && !s.over;
  const action = s.mode === "in"
    ? `<button class="primary goal" ${ok ? "" : "disabled"}>${icon("flag")}Aportar</button>`
    : `<button class="primary" ${ok ? "" : "disabled"}>Registrar retiro</button>`;
  return `<div class="entry-top">${statusbar()}<div class="entry-bar"><button class="icon-btn" aria-label="Cerrar">${icon("close")}</button><div class="seg" role="group" aria-label="Tipo"><button aria-pressed="${s.mode === "in"}">Aportar</button><button aria-pressed="${s.mode === "out"}">Retirar</button></div><span class="spacer"></span></div></div>${amountSlip(s)}${middle(s, o)}<div class="entry-action">${action}</div>${pad()}<div class="gesture" style="background:var(--desk)"></div>`;
}

const phone = (o, inner) => `<div class="phone"><div class="app" data-ink="${o.ink}" data-theme="${o.theme}">${inner}</div></div>`;
const card = (title, note, inner) => `<div class="frame-card"><h3>${title}</h3><p>${note}</p>${inner}</div>`;

function render() {
  const o = reviewOptions(["ink", "theme"]);
  const set = (id, h) => document.getElementById(id).innerHTML = h;
  set("flow",
    card("From a goal", "Opened by “Aportar a esta meta”: the goal is already chosen. Green amount with +, green Aportar.", phone(o, form(SCENES.fromGoal, o))) +
    card("From the Metas tab", "Opened by “Aportar a una meta”: no goal chosen yet, so Aportar waits. Goals are ordered like the tab, with their %.", phone(o, form(SCENES.fromTab, o))) +
    card("Retirar", "Same form, other side of the switch. Ink amount with −, a neutral “Registrar retiro”: using savings is not a failure.", phone(o, form(SCENES.withdraw, o))) +
    card("More than what is saved", "A withdrawal can be at most what the goal has saved. The label becomes the message, as with 12 digits; the amount stays and the button waits.", phone(o, form(SCENES.tooMuch, o))));
}
document.querySelectorAll(".controls select").forEach(s => s.addEventListener("change", render));
render();
