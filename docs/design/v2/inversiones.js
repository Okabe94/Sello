// Sello v2 · Inversiones (locked 2026-10-16). Owner picks: P1 (the Recibo slip), Gana / Pierde
// stamps, the ink “Anotar movimiento” dock, the recorded-values chart, T2 type chips in entry,
// withdrawals limited to the last recorded value, and Vale hoy starting as the first contribution. Synthetic data (COP, no symbol).
// Gain = value + withdrawals + dividends − contributions (ARCHITECTURE.md, investments);
// percentage = gain ÷ contributions, one decimal, since the first contribution. Not IRR.
//   Parqueadero  19.000.000 +       0 + 350.000 − 18.000.000 = +1.350.000 → +7,5 %
//   CDT Banco    10.746.000 +       0 +       0 − 10.000.000 =   +746.000 → +7,46 % → +7,5 %
//   Fondo         5.242.000 + 500.000 +       0 −  6.000.000 =   −258.000 → −4,3 %
//   Ecopetrol     3.050.000 +       0 + 178.000 −  3.000.000 =   +228.000 → +7,6 %
//   Total        38.038.000 + 500.000 + 528.000 − 37.000.000 = +2.066.000 → +5,58 % → +5,6 %
// Share of value: 49,9 % · 28,3 % · 13,8 % · 8,0 %.
// Total in loss: Fondo + Ecopetrol = 8.292.000 + 500.000 + 178.000 − 9.000.000 = −30.000 → −0,3 %.

const H = [
  { name: "Parqueadero", kind: "Finca raíz", icon: "home", value: 19000000, gain: 1350000, pct: "7,5", up: true, valued: "12 jun", share: 49.9 },
  { name: "CDT Banco", kind: "CDT a 360 días", icon: "account_balance", value: 10746000, gain: 746000, pct: "7,5", up: true, valued: "30 sep", share: 28.3 },
  { name: "Fondo de acciones", kind: "Fondo colectivo", icon: "insights", value: 5242000, gain: -258000, pct: "4,3", up: false, valued: "14 oct", share: 13.8 },
  { name: "Ecopetrol", kind: "Acciones", icon: "trending_up", value: 3050000, gain: 228000, pct: "7,6", up: true, valued: "9 oct", share: 8.0 },
];
const TOTAL = { value: 38038000, contributed: 37000000, withdrawn: 500000, paid: 528000, gain: 2066000, pct: "5,6", up: true, since: "mar 2025", from: "12 jun", to: "14 oct" };
const LOSS = { value: 8292000, contributed: 9000000, withdrawn: 500000, paid: 178000, gain: -30000, pct: "0,3", up: false, since: "mar 2025", from: "9 oct", to: "14 oct" };
const SHADES = [100, 68, 42, 22];
const shade = i => `color-mix(in srgb,var(--brand) ${SHADES[i]}%,var(--paper))`;

// Signed return: arrow, sign and words carry the same meaning as the colour.
const perf = (t, { words = true } = {}) => `<p class="perf ${t.up ? "gain" : "loss"}">${icon(t.up ? "trending_up" : "trending_down")}<span>${t.up ? "+" : "−"}${t.pct} %</span>${words ? `<span class="perf-gain">${t.up ? "+" : "−"}${fmt(Math.abs(t.gain))} de ${t.up ? "ganancia" : "pérdida"}</span>` : ""}</p>`;
const stamp = t => `<span class="stamp ${t.up ? "" : "loss"}">${t.up ? "Gana" : "Pierde"}</span>`;
const leader = (label, n, cls = "") => `<div class="leader"><span>${label}</span><i class="dots"></i><b class="${cls}">${n}</b></div>`;
const ages = t => `<button class="footnote">${icon("event")}<span>Valores anotados entre el <b>${t.from}</b> y el <b>${t.to}</b></span>${icon("chevron_right", "chev")}</button>`;
const breakdown = t => leader("Aportaste", money(t.contributed)) + leader("Retiraste", money(t.withdrawn)) + leader("Dividendos y rentas", money(t.paid)) + leader("Ganancia", `${t.gain < 0 ? "−" : "+"}${fmt(Math.abs(t.gain))}`, t.gain < 0 ? "neg" : "pos");

// The overview: the Recibo answer slip, with Gana / Pierde as its stamp.
const heroSlip = t => `<section class="slip pinked"><div class="answer-head"><span class="label">Tus inversiones valen</span>${stamp(t)}</div><div class="hero ${t.value >= 10000000 ? "long" : ""}">${money(t.value)}</div>${perf(t)}<p class="caption inv-since">En total, desde ${t.since}</p><div class="tear" aria-hidden="true"></div>${breakdown(t)}${ages(t)}</section>`;

// Distribution: the income split bar, one shade of the ink per investment, largest first.
const distBar = list => `<div class="split inv-split" aria-hidden="true">${list.map((h, i) => `<i style="width:${h.share}%;background:${shade(i)}"></i>`).join("")}</div>`;
const row = (h, i) => `<button class="inv-row" aria-label="${h.name}, ${spoken(h.value)}, ${h.up ? "gana" : "pierde"} ${h.pct} por ciento, valor del ${h.valued}"><span class="inv-ic" style="--c:${shade(i)}">${icon(h.icon)}</span><span><b>${h.name}</b><small>${h.kind} · valor del ${h.valued}</small></span><span class="inv-right"><b>${fmt(h.value)}</b><small class="${h.up ? "pos" : "neg"}">${icon(h.up ? "trending_up" : "trending_down")}${h.up ? "+" : "−"}${h.pct} %</small></span></button>`;
// An investment without a recorded value: no amount and no %, only what was put in. It is not
// in the bar or the total (owner decision: no warning on the overview).
const rowNoValue = h => `<button class="inv-row" aria-label="${h.name}, sin valor anotado, aportaste ${spoken(h.contributed)}"><span class="inv-ic none">${icon(h.icon)}</span><span><b>${h.name}</b><small>${h.kind} · sin valor anotado</small></span><span class="inv-right"><b class="inv-none">Sin valor</b><small>aportaste ${fmt(h.contributed)}</small></span></button>`;
const listSlip = (list, extra = []) => `<section class="slip"><h2 class="q">Dónde está, de mayor a menor</h2>${distBar(list)}${list.map(row).join("")}${extra.map(rowNoValue).join("")}</section>`;
const CRIPTO = { name: "Cripto", kind: "Criptomoneda", icon: "insights", contributed: 2000000 };

const tab = (o, body, { tall = false, bandHtml = "", dock = "Anotar movimiento" } = {}) => `<div class="phone ${tall ? "tall" : ""}"><div class="app" data-ink="${o.ink}" data-theme="${o.theme}">${appShell({ title: "Inversiones", addLabel: "Agregar inversión", body, tabs: 4, active: 3, band: bandHtml, dock, dockClass: "inv" })}</div></div>`;

// Detail. Ecopetrol: aportes 2.000.000 (mar 2025) + 1.000.000 (jul 2025) = 3.000.000; dividends
// 89.000 (mar 2026) + 89.000 (sep 2026) = 178.000; recorded values below; gain +228.000 (+7,6 %).
// Fondo: aportes 6.000.000, retiro 500.000 (ago 2026), value 5.242.000; gain −258.000 (−4,3 %).
const DETAIL = {
  eco: { name: "Ecopetrol", kind: "Acciones", value: 3050000, contributed: 3000000, withdrawn: 0, paid: 178000, gain: 228000, pct: "7,6", up: true, since: "mar 2025", valued: "9 oct",
    // [month index from mar 2025, recorded value]; contributions as steps.
    values: [[0, 2000000], [4, 3010000], [9, 2900000], [13, 3120000], [19, 3050000]], steps: [[0, 2000000], [4, 3000000]],
    movs: [["value", "Valor de hoy", "vie 9 oct", "3.050.000"], ["paid", "Dividendo", "mar 15 sep", "+89.000"], ["value", "Valor de hoy", "jue 2 abr", "3.120.000"], ["paid", "Dividendo", "dom 15 mar", "+89.000"]], count: 9 },
  fondo: { name: "Fondo de acciones", kind: "Fondo colectivo", value: 5242000, contributed: 6000000, withdrawn: 500000, paid: 0, gain: -258000, pct: "4,3", up: false, since: "may 2025", valued: "14 oct",
    values: [[2, 3000000], [8, 6150000], [14, 5980000], [17, 5600000], [19, 5242000]], steps: [[2, 3000000], [8, 6000000], [17, 5500000]],
    movs: [["value", "Valor de hoy", "mié 14 oct", "5.242.000"], ["out", "Retiro", "lun 3 ago", "−500.000"], ["value", "Valor de hoy", "vie 3 jul", "5.600.000"], ["in", "Aporte", "lun 3 nov 2025", "+3.000.000"]], count: 7 },
};

// Recorded values are dots joined by a thin line (only recorded points, nothing in between is
// claimed); what was put in (aportes − retiros) is a dashed step line.
function chart(d) {
  const W = 300, Hh = 110, max = d.value < 4000000 ? 3500000 : 7000000, x = m => 8 + m * (W - 16) / 19, y = v => Hh - 6 - v * (Hh - 16) / max;
  const steps = d.steps.map(([m, v], i) => `${i ? `H${x(m).toFixed(1)}V${y(v).toFixed(1)}` : `M${x(m).toFixed(1)},${y(v).toFixed(1)}`}`).join("") + `H${x(19)}`;
  const line = d.values.map(([m, v], i) => `${i ? "L" : "M"}${x(m).toFixed(1)},${y(v).toFixed(1)}`).join("");
  const dots = d.values.map(([m, v]) => `<circle cx="${x(m).toFixed(1)}" cy="${y(v).toFixed(1)}" r="4"/>`).join("");
  return `<section class="slip"><h2 class="q">Valor anotado y lo que pusiste</h2><svg class="inv-chart" viewBox="0 0 ${W} ${Hh}" role="img" aria-label="Valores anotados de ${d.since} a octubre 2026"><path class="put" d="${steps}"/><path class="val" d="${line}"/><g class="dots">${dots}</g></svg><div class="axis"><span>mar 25</span><span class="now">oct 26</span></div><p class="legend"><span><i class="val"></i>Valor anotado</span><span><i class="put"></i>Lo que pusiste</span></p></section>`;
}
const movIcon = { value: "insights", paid: "add", in: "add", out: "arrow_back" };
const movs = d => `<section class="slip"><div class="mov-head"><h2 class="q" style="margin:0">Movimientos</h2></div>${d.movs.map(([k, label, date, n], i) => `<div class="mov-row ${k === "out" ? "out" : ""} ${k === "value" ? "val" : ""} ${i === d.movs.length - 1 ? "last" : ""}"><span class="mi">${icon(movIcon[k])}</span><span><b>${label}</b><small>${date}</small></span><b>${n}</b></div>`).join("")}<button class="text-btn">Ver los ${d.count}${icon("chevron_right")}</button></section>`;
const detailHero = d => `<section class="slip pinked"><div class="answer-head"><span class="label">Vale</span>${stamp(d)}</div><div class="gd-saved">${money(d.value)}</div>${perf(d)}<p class="caption inv-since">Valor anotado el ${d.valued} · desde ${d.since}</p><div class="tear" aria-hidden="true"></div>${breakdown(d)}<button class="outline-btn wide inv-update">${icon("insights")}Anotar el valor de hoy</button></section>`;
const detail = (o, d, tall = false) => `<div class="phone ${tall ? "tall" : ""}"><div class="app" data-ink="${o.ink}" data-theme="${o.theme}">${subShell({ title: d.name, body: detailHero(d) + chart(d) + movs(d), tabs: 4, active: 3, dock: "Anotar movimiento", dockClass: "inv", action: ["edit_note", "Editar inversión"] })}</div></div>`;

// Anotar movimiento: the locked entry screen; the investment chips replace categories and one plain
// fact sits under them (no live preview, as on entry). The type is a row of chips above the
// investments (owner pick T2; four tabs in the top bar were too many).
const TYPES = [["in", "Aporte", "add", "Pones dinero en la inversión."], ["value", "Valor de hoy", "insights", "Cuánto vale ahora. Reemplaza el valor anterior."], ["out", "Retiro", "arrow_back", "Sacas dinero de la inversión."], ["paid", "Dividendo o renta", "add", "Te pagó sin que vendieras."]];
const NAMES = [["Parqueadero", "home"], ["CDT Banco", "account_balance"], ["Fondo de acciones", "insights"], ["Ecopetrol", "trending_up"], ["Cripto", "insights"]];
// Retiro is limited to the last recorded value (5.242.000 for Fondo, 14 oct): taking out more would
// leave a negative value. If it is worth more today, the person records the value first.
const MOVE = {
  aporte:  { type: "in", amount: 500000, inv: "Ecopetrol", fact: "Llevas <b>3.000.000</b> aportados en Ecopetrol." },
  valor:   { type: "value", amount: 3120000, inv: "Ecopetrol", fact: "Último valor: <b>3.050.000</b>, anotado el 9 oct. El nuevo lo reemplaza; no se suma." },
  retiro:  { type: "out", amount: 500000, inv: "Fondo de acciones", fact: "Vale <b>5.242.000</b> según el valor del 14 oct." },
  over:    { type: "out", amount: 6000000, inv: "Fondo de acciones", over: 5242000, fact: "Vale <b>5.242.000</b> según el valor del 14 oct. ¿Vale más hoy?", link: "Anotar el valor de hoy" },
  divid:   { type: "paid", amount: 89000, inv: "Ecopetrol", fact: "Llevas <b>178.000</b> en dividendos y rentas de Ecopetrol." },
  fromTab: { type: "in", amount: null, inv: null },
};
const LABEL = { in: "Aporte", value: "Valor de hoy", out: "Retiro", paid: "Dividendo o renta" };
const SIGN = { in: "+", value: "", out: "−", paid: "+" };
const ACTION = { in: "Anotar aporte", value: "Anotar valor", out: "Anotar retiro", paid: "Anotar dividendo" };
const pad = () => `<div class="pad" role="group" aria-label="Teclado de Sello">${["1", "2", "3", "4", "5", "6", "7", "8", "9", "000", "0"].map(k => `<button class="${k === "000" ? "k000" : ""}">${k}</button>`).join("")}<button class="del" aria-label="Borrar">${icon("backspace")}</button></div>`;
function moveForm(m) {
  const ic = (NAMES.find(n => n[0] === m.inv) ?? [, "trending_up"])[1];
  const label = m.over ? `<span class="limit-msg" role="status">Más de lo que vale (${fmt(m.over)})</span>` : `<span>${LABEL[m.type]} en pesos</span>`;
  const value = m.amount === null ? `<div class="amount-value placeholder">0<span class="caret"></span></div>` : `<div class="amount-value ${String(m.amount).length > 6 ? "mid" : ""}">${SIGN[m.type]}${money(m.amount)}<span class="caret"></span></div>`;
  const chips = NAMES.map(([n, i]) => `<button class="chip" aria-pressed="${n === m.inv}">${icon(i)}${n}</button>`).join("");
  const typeChips = `<h3>Tipo</h3><div class="chips one-row">${TYPES.map(([k, t, i]) => `<button class="chip" aria-pressed="${k === m.type}">${t}</button>`).join("")}</div>`;
  const fact = m.fact ? `<p class="goal-fact">${m.fact}${m.link ? ` <button class="text-btn inline">${m.link}</button>` : ""}</p>` : "";
  const ok = m.amount && m.inv && !m.over;
  return `<div class="entry-top">${statusbar()}<div class="entry-bar"><button class="icon-btn" aria-label="Cerrar">${icon("close")}</button><b style="flex:1;font-size:1.2em;margin-left:4px">Movimiento</b></div></div><section class="slip pinked amount-slip"><div class="amount-meta">${label}<button class="date-pill">${icon("event")}Hoy, vie 16</button></div><div class="amount-row"><span class="circle" style="--fill:${m.inv ? 100 : 0}%;--c:var(--ink)" aria-hidden="true">${icon(ic)}<span class="on">${icon(ic)}</span></span>${value}</div></section><div class="entry-mid">${typeChips}<h3>Inversión</h3><div class="chips one-row">${chips}</div>${fact}<button class="note-pill">${icon("edit_note")}<span>Agregar nota</span></button></div><div class="entry-action"><button class="primary inv" ${ok ? "" : "disabled"}>${ACTION[m.type]}</button></div>${pad()}<div class="gesture" style="background:var(--desk)"></div>`;
}

// Agregar / editar inversión: the create-goal pattern. Name, an optional description (shown under
// the name in the list) and an icon; the first contribution is the big number; its value today
// starts as the same amount, changeable, so a new investment is not left without a value.
const ICONS = ["account_balance", "trending_up", "insights", "home", "shopping_cart", "school"];
function invForm(f, { confirm = false } = {}) {
  const strip = ICONS.map(i => `<button class="icon-choice" aria-pressed="${i === f.icon}">${icon(i)}</button>`).join("");
  const ident = `<section class="slip form-slip inv-ident"><div class="ident"><span class="ident-icon ${f.icon ? "" : "empty"} inv" aria-hidden="true">${icon(f.icon ?? "trending_up")}</span><div><label class="form-label">Nombre</label><div class="form-field ${f.name ? "" : "placeholder"}">${f.name || "CDT, fondo, acciones…"}</div></div></div><div class="form-msg"><span></span><span>${[...f.name].length}/40</span></div><label class="form-label">Descripción <small>(opcional)</small></label><div class="form-field ${f.kind ? "" : "placeholder"}">${f.kind || "Ej. CDT a 360 días"}</div><div class="icon-strip" role="group" aria-label="Ícono" style="margin-top:14px">${strip}</div></section>`;
  const first = f.edit ? "" : `<section class="slip pinked"><div class="plan-label"><span class="label">Primer aporte</span><button class="date-pill">${icon("event")}Hoy, vie 16</button></div><p class="plan-amount ${f.amount ? "" : "placeholder"}">${f.amount ? money(f.amount) : "0"}</p><div class="tear" aria-hidden="true"></div><span class="form-label">Vale hoy</span><div class="date-row"><button class="chip" aria-pressed="${!f.other}">Lo mismo que aporté</button><button class="chip" aria-pressed="${!!f.other}">Otro valor</button></div>${f.other ? `<p class="plan-amount small">${money(f.other)}</p>` : ""}</section>`;
  const danger = f.edit ? `<section class="slip danger"><h2>Borrar esta inversión</h2><p>Se borran la inversión y sus <b>9 movimientos</b>. Esto no se puede deshacer.</p><button class="outline-btn loss wide">Borrar inversión</button></section>` : "";
  const ok = f.name && f.icon && (f.edit || f.amount);
  const confirmSlip = confirm ? `<div class="scrim center"><div class="confirm" role="alertdialog" aria-label="Borrar inversión"><h2>¿Borrar ${f.name}?</h2><p>Se borran la inversión y sus <b>9 movimientos</b>. Esto no se puede deshacer.</p><div class="tear" aria-hidden="true"></div><div class="confirm-actions"><button class="ghost">Cancelar</button><button class="del">Borrar</button></div></div></div>` : "";
  return `<div class="entry-top">${statusbar()}<div class="entry-bar"><button class="icon-btn" aria-label="Cerrar">${icon("close")}</button><b style="flex:1;font-size:1.2em;margin-left:4px">${f.edit ? "Editar inversión" : "Nueva inversión"}</b></div></div><div class="entry-mid" style="padding-top:2px">${ident}${first}${danger}</div><div class="entry-action"><button class="primary inv" ${ok ? "" : "disabled"}>${f.edit ? "Guardar cambios" : "Crear inversión"}</button></div><div class="gesture" style="background:var(--desk)"></div>${confirmSlip}`;
}
const phone = (o, inner, tall = false) => `<div class="phone ${tall ? "tall" : ""}"><div class="app" data-ink="${o.ink}" data-theme="${o.theme}">${inner}</div></div>`;

const card = (title, note, inner) => `<div class="frame-card"><h3>${title}</h3><p>${note}</p>${inner}</div>`;

function render() {
  const o = reviewOptions(["ink", "theme"]);
  const set = (id, h) => document.getElementById(id).innerHTML = h;
  set("overview",
    card("Overview", "Value as the big number with a Gana / Pierde stamp, the signed return with arrow and pesos, then what produced it under the tear. A quiet footnote says when the values were recorded. Then one bar for where the money is and a row per investment.", tab(o, heroSlip(TOTAL) + listSlip(H))) +
    card("Whole screen", "Rows show each value’s date, so an old valuation (Parqueadero, 12 jun) is visible without opening it.", tab(o, heroSlip(TOTAL) + listSlip(H), { tall: true })));
  set("detail",
    card("Gaining, with dividends", "Same slip language: value, stamp, signed return, the breakdown and “Anotar el valor de hoy”. The chart shows only recorded values as dots; the dashed line is what was put in.", detail(o, DETAIL.eco)) +
    card("Losing, after a withdrawal", "−4,3 % in red with a down arrow and “de pérdida”, stated plainly. The withdrawal lowers what was put in, so it is not shown as a loss.", detail(o, DETAIL.fondo)) +
    card("Detail, whole screen", "Movements: Valor de hoy, Dividendo, Aporte, Retiro. Anotar movimiento opens the entry screen below.", detail(o, DETAIL.eco, true)));
  set("states",
    card("Total in loss", "Pierde, −0,3 %, −30.000 de pérdida. Factual, no advice.", tab(o, heroSlip(LOSS) + listSlip([{ ...H[2], share: 63.2 }, { ...H[3], share: 36.8 }]))) +
    card("An investment without a value", "No message on the overview. Cripto’s row says Sin valor and what was put in; it stays out of the bar and the total until a value is recorded, never counted as 0.", tab(o, heroSlip(TOTAL) + listSlip(H, [CRIPTO]), { tall: true })) +
    card("No investments", "Welcoming, no invented chart. The dock becomes Agregar inversión, as Metas does with Crear una meta.", tab(o, `<section class="slip pinked empty">${icon("trending_up")}<h2>Anota lo que tienes invertido</h2><p>Un CDT, un fondo, acciones o un local. Anotas cuánto pusiste y, de vez en cuando, cuánto vale. Sello no se conecta a ningún banco.</p></section>`, { dock: "Agregar inversión" })) +
    card("Loading", "Skeleton slips.", tab(o, `<div role="status" aria-label="Cargando"><section class="slip"><div class="skel" style="width:50%"></div><div class="skel big"></div><div class="skel" style="width:70%"></div></section><section class="slip"><div class="skel" style="width:60%"></div><div class="skel"></div><div class="skel"></div></section></div>`)) +
    card("Failed read", "Never shown as zero or as no investments.", tab(o, `<section class="slip empty">${icon("error")}<h2>No pudimos leer tus inversiones</h2><p>Tus registros siguen guardados. Vuelve a intentarlo.</p><button class="outline-btn">Reintentar</button></section>`)));
  set("move",
    card("Aporte, from the investment", "Opened from Ecopetrol: it is already chosen. One row of type chips, then the investments in one row. The fact says where it stands.", phone(o, moveForm(MOVE.aporte))) +
    card("Valor de hoy", "No sign: a value replaces the last one, it is not added. “Anotar el valor de hoy” in the detail opens it here.", phone(o, moveForm(MOVE.valor))) +
    card("Retiro", "Minus sign, neutral button.", phone(o, moveForm(MOVE.retiro))) +
    card("More than it is worth", "A withdrawal is limited to the last recorded value; the label becomes the message and the button waits. If it is worth more today, Anotar el valor de hoy first.", phone(o, moveForm(MOVE.over))) +
    card("Dividendo o renta", "Money the investment paid out. It counts in the gain, not in the value.", phone(o, moveForm(MOVE.divid))) +
    card("From the tab’s dock", "No investment chosen yet, so the button waits. Same order as the list.", phone(o, moveForm(MOVE.fromTab))));
  set("create",
    card("Nueva inversión", "Name and icon as their own section, an optional description, then the first contribution as the big number with its date. Vale hoy starts as the same amount.", phone(o, invForm({ name: "CDT Banco", kind: "CDT a 360 días", icon: "account_balance", amount: 10000000 }), true)) +
    card("Otro valor", "If it is already worth something else (an investment from before Sello), the person types it.", phone(o, invForm({ name: "Parqueadero", kind: "Finca raíz", icon: "home", amount: 18000000, other: 19000000 }), true)) +
    card("Editar inversión", "Name, description and icon only; movements are edited from the list. Deleting is its own card with the confirm slip.", phone(o, invForm({ name: "Ecopetrol", kind: "Acciones", icon: "trending_up", edit: true }), true)) +
    card("Deleting asks once more", "Same confirm slip as goals.", phone(o, invForm({ name: "Ecopetrol", kind: "Acciones", icon: "trending_up", edit: true }, { confirm: true }))));
}
document.querySelectorAll(".controls select").forEach(s => s.addEventListener("change", render));
render();
