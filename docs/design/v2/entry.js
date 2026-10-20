// Sello v2 · Expense and income entry. Decided: Sello pad (familiar 3 × 4, compact),
// delete key in the soft brand tint, note on its own row opening a composer docked to the
// keyboard, chips in at most two rows that scroll sideways, no category pre-selected.
// The text keyboard drawn here is an inert illustration, not an app control.

const CATEGORIES = [["Hogar", "home"], ["Comida", "restaurant"], ["Transporte", "directions_bus"], ["Café", "local_cafe"], ["Salud", "medical_services"], ["Ocio", "theaters"], ["Ropa", "checkroom"], ["Mascotas", "pets"], ["Educación", "school"], ["Gimnasio", "fitness_center"]];
const SOURCES = ["Salario", "Freelance", "Ingresos pasivos", "Transferencia", "Otro"];
const LONG_NOTE = "Almuerzo del equipo por el cumpleaños de Laura en el restaurante nuevo";

const SCENES = {
  opened: { mode: "expense", amount: null, category: null },
  ready:  { mode: "expense", amount: 48700, category: "Comida" },
  noted:  { mode: "expense", amount: 48700, category: "Comida", note: "Almuerzo del equipo" },
  income: { mode: "income", amount: 850000, category: "Otro", source: "Venta de la bicicleta" },
};

// The amount steps down in size as digits are added so 12 digits always fit on one line.
const sizeClass = n => { const d = String(n).length; return d > 9 ? "long" : d > 6 ? "mid" : ""; };

// `limit` swaps the label for the 12-digit message, so no line is reserved under the amount.
function amountSlip(s, limit = false) {
  const income = s.mode === "income";
  const cat = CATEGORIES.find(([n]) => n === s.category);
  const ic = cat ? cat[1] : "receipt_long";
  const circle = income ? "" : `<span class="circle" style="--fill:${cat ? 100 : 0}%" aria-hidden="true">${icon(ic)}<span class="on">${icon(ic)}</span></span>`;
  const value = s.amount === null
    ? `<div class="amount-value placeholder">0<span class="caret"></span></div>`
    : `<div class="amount-value ${sizeClass(s.amount)} ${income ? "income" : ""} ${limit ? "shake" : ""}">${income ? "+" : ""}${money(s.amount)}<span class="caret"></span></div>`;
  const label = limit ? `<span class="limit-msg" role="status">Máximo 12 dígitos</span>` : `<span>${income ? "Ingreso" : "Gasto"} en pesos</span>`;
  return `<section class="slip pinked amount-slip"><div class="amount-meta">${label}<button class="date-pill">${icon("event")}Hoy, jue 22</button></div><div class="amount-row">${circle}${value}</div></section>`;
}

function middle(s, locked) {
  const income = s.mode === "income";
  const dis = locked ? "disabled" : "";
  const chips = income
    ? SOURCES.map(n => `<button class="chip" aria-pressed="${n === s.category}" ${dis}>${n}</button>`).join("")
    : CATEGORIES.map(([n, ic]) => `<button class="chip" aria-pressed="${n === s.category}" ${dis}>${icon(ic)}${n}</button>`).join("") + `<button class="chip add" ${dis}>${icon("add")}Nueva</button>`;
  let h = `<h3>${income ? "Origen" : "Categoría"}</h3><div class="chips">${chips}</div>`;
  if (income && s.category === "Otro") h += `<button class="note-pill filled" ${dis}>${icon("account_balance")}<span>${s.source}</span><span class="edit">Cambiar</span></button>`;
  h += s.note ? `<button class="note-pill filled" ${dis}>${icon("edit_note")}<span>${s.note}</span><span class="edit">Editar</span></button>` : `<button class="note-pill" ${dis}>${icon("edit_note")}<span>Agregar nota</span></button>`;
  return `<div class="entry-mid">${h}</div>`;
}

const pad = locked => `<div class="pad ${locked ? "locked" : ""}" role="group" aria-label="Teclado de Sello">${["1", "2", "3", "4", "5", "6", "7", "8", "9", "000", "0"].map(k => `<button class="${k === "000" ? "k000" : ""}" aria-label="${k === "000" ? "Tres ceros" : k}" ${locked ? "disabled" : ""}>${k}</button>`).join("")}<button class="del" aria-label="Borrar (mantén para borrar todo)" ${locked ? "disabled" : ""}>${icon("backspace")}</button></div>`;
const imeText = () => `<div class="ime" aria-hidden="true"><div class="ime-tools"><span>☺</span><span>Español</span><span>⌄</span></div>${["qwertyuiop", "asdfghjklñ", "zxcvbnm"].map(r => `<div class="ime-row">${[...r].map(k => `<span>${k}</span>`).join("")}</div>`).join("")}<div class="ime-space">Español</div></div>`;

// Note composer, docked to the top of the text keyboard.
function composer(text, s) {
  const count = [...text].length, over = count > 60;
  const context = `<p class="note-context">${icon(CATEGORIES.find(([n]) => n === s.category)[1])}Gasto en <b>${s.category}</b> · <b>${fmt(s.amount)}</b></p>`;
  const foot = `<div class="field-foot">${over ? `<span class="err">Máximo 60 caracteres. Tu texto sigue aquí.</span>` : `<span>Opcional</span>`}<span class="${over ? "err" : ""}">${count}/60</span></div>`;
  return `<div class="scrim"><div class="composer" role="dialog" aria-label="Nota"><div class="composer-head">${context}<button class="text-link">Cancelar</button></div><div class="composer-row"><div class="text-box ${over ? "error" : ""}">${text}<span class="caret"></span></div><button class="gw-round" aria-label="Listo" ${over ? "disabled" : ""}>${icon("check")}</button></div>${foot}</div>${imeText()}</div>`;
}

// Save states: busy, rejected (definitely not saved) and unknown (outcome not known yet).
function action(s, save) {
  const income = s.mode === "income";
  const canSave = s.amount !== null && s.category;
  const label = income ? "Anotar ingreso" : "Anotar gasto";
  if (save === "busy") return `<div class="entry-action"><button class="primary" disabled aria-busy="true"><span class="spinner" aria-hidden="true"></span>Guardando…</button></div>`;
  if (save === "unknown") return `<div class="entry-action"><p class="save-msg warn" role="status">${icon("error")}Aún no sabemos si se guardó. Compruébalo antes de volver a anotar.</p><button class="primary">Comprobar si se guardó</button></div>`;
  if (save === "rejected") return `<div class="entry-action"><p class="save-msg loss" role="status">${icon("error")}No se guardó. Tu borrador sigue aquí.</p><button class="primary">${icon("add")}${label}</button></div>`;
  return `<div class="entry-action"><button class="primary" ${canSave ? "" : "disabled"}>${icon("add")}${label}</button></div>`;
}

function entry(s, o, extra = {}) {
  const income = s.mode === "income";
  const locked = extra.save === "busy" || extra.save === "unknown";
  let h = `<div class="entry-top">${statusbar()}<div class="entry-bar"><button class="icon-btn" aria-label="Cerrar" ${extra.save === "busy" ? "disabled" : ""}>${icon("close")}</button><div class="seg" role="group" aria-label="Tipo"><button aria-pressed="${!income}" ${locked ? "disabled" : ""}>Gasto</button><button aria-pressed="${income}" ${locked ? "disabled" : ""}>Ingreso</button></div><span class="spacer"></span></div></div>`;
  h += amountSlip(s, extra.limit) + middle(s, locked) + action(s, extra.save) + pad(locked) + `<div class="gesture" style="background:var(--desk)"></div>`;
  if (extra.note !== undefined) h += composer(extra.note, s);
  return h;
}

const phone = (o, inner, { small = false, large = false } = {}) => `<div class="phone ${small ? "small" : ""}"><div class="app ${large ? "large" : ""}" data-ink="${o.ink}" data-theme="${o.theme}" style="--scale:${large ? 2 : 1}">${inner}</div></div>`;
const card = (title, note, body, small) => `<div class="frame-card ${small ? "small" : ""}"><h3>${title}</h3><p>${note}</p>${body}</div>`;
const crop = (o, inner) => `<div class="app pad-crop" data-ink="${o.ink}" data-theme="${o.theme}" style="height:auto;display:block">${inner}</div>`;

function render() {
  const o = reviewOptions(["ink", "theme"]);
  const set = (id, html) => document.getElementById(id).innerHTML = html;
  set("flow",
    card("Just opened", "No category chosen yet, so Anotar waits.", phone(o, entry(SCENES.opened, o))) +
    card("Ready, with a note", "Two rows of chips scroll sideways; the fade shows there is more.", phone(o, entry(SCENES.noted, o))) +
    card("Writing a note", "Composer docked to the keyboard; ✓ finishes, Cancelar keeps the old note.", phone(o, entry(SCENES.ready, o, { note: "Almuerzo del equipo" }))) +
    card("Note too long", "Text kept, red outline and counter, ✓ disabled.", phone(o, entry(SCENES.ready, o, { note: LONG_NOTE }))) +
    card("Income · Otro", "Green amount with +. The source name uses the same composer.", phone(o, entry(SCENES.income, o))) +
    card("Small phone · 320 × 640", "Two rows of chips still fit; the note row scrolls below them.", phone(o, entry(SCENES.noted, o), { small: true }), true) +
    card("Large text · 200%", "The middle scrolls; amount, Anotar and pad stay put.", phone(o, entry(SCENES.noted, o), { large: true })));
  set("save",
    card("Saving", "Spinner inside the button only. Everything is locked so the same expense cannot be sent twice.", phone(o, entry(SCENES.noted, o, { save: "busy" }))) +
    card("Not saved", "A definite failure: the draft stays and Anotar can be tried again.", phone(o, entry(SCENES.noted, o, { save: "rejected" }))) +
    card("Outcome unknown", "The app could not confirm the save. The only action is checking the same operation; no second write.", phone(o, entry(SCENES.noted, o, { save: "unknown" }))) +
    card("Saved", "The form closes and Home shows a brief confirmation. See the Home page’s “After saving” state.", `<a class="frame-link" href="home.html">Open Home ›</a>`));
  set("amounts", [
    [48700, false, "Up to 6 digits · full size"],
    [125000000, false, "7–9 digits · one step smaller"],
    [999999999999, false, "10–12 digits · smallest step"],
    [999999999999, true, "A 13th key press"],
  ].map(([n, limit, t]) => card(t, limit ? "The amount shakes, nothing changes, and the label becomes the message until the next valid key." : "The size steps down so the whole amount stays on one line.", crop(o, amountSlip({ ...SCENES.noted, amount: n }, limit)))).join(""));
}
document.querySelectorAll(".controls select").forEach(s => s.addEventListener("change", render));
render();
