/* Review chrome only. No storage, services, financial arithmetic or production imports. */
"use strict";
const frames = [...document.querySelectorAll("iframe")];
const initial = {
  scope: "future", ink: "cobalt", theme: "light", scale: "1", window: "360,780",
  view: "compare", scenario: "ready", keyboard: "pad", "entry-state": "ready",
  page: "home", expenseAdded: false, expenseCategory: "Café", incomeAdded: false,
  goal: "emergency", notice: ""
};
let settings = {...initial};
const knownPages = new Set(["home", "entry", "summary"]);
function sendSettings() {
  const [width, height] = settings.window.split(",").map(Number);
  document.body.style.setProperty("--frame-width", `${width}px`);
  document.body.style.setProperty("--frame-height", `${height}px`);
  document.body.dataset.view = settings.view;
  document.body.classList.toggle("wide-review", width > 400);
  for (const element of document.querySelectorAll(".proposal")) {
    element.hidden = settings.view === "journey" && element.dataset.page !== settings.page;
    element.dataset.active = String(element.dataset.page === settings.page);
  }
  for (const button of document.querySelectorAll(".journey-nav button")) {
    if (button.dataset.page === settings.page) button.setAttribute("aria-current", "page");
    else button.removeAttribute("aria-current");
  }
  for (const frame of frames) frame.contentWindow.postMessage({kind: "sello-review-settings", settings}, location.origin);
  document.getElementById("scope-note").textContent = settings.scope === "future"
    ? "Dirección futura: se muestran Metas, reserva de pagos recurrentes y análisis histórico. No son funciones implementadas. El criterio de sugerencias es ilustrativo."
    : "Propuesta sin Metas ni recurrencia: dos destinos, sin reservas ni sugerencias avanzadas. La comparación al mismo corte también es una propuesta; no certifica funciones implementadas.";
}
function activate(page, focus = false) {
  if (!knownPages.has(page)) return;
  settings.page = page;
  sendSettings();
  if (focus && page === "entry") {
    document.getElementById("entry-frame").contentWindow.postMessage({kind: "sello-review-focus"}, location.origin);
  }
}
for (const id of ["scope", "ink", "theme", "scale", "window", "view", "scenario", "keyboard", "entry-state"]) {
  document.getElementById(id).addEventListener("change", event => {
    settings[id] = event.target.value;
    if (id === "scenario") {
      settings.expenseAdded = false;
      settings.incomeAdded = false;
      settings.notice = "";
      if (["paid", "reserved", "shortfall"].includes(settings.scenario)) {
        settings.scope = "future";
        document.getElementById("scope").value = "future";
      }
    }
    if (id === "scope" && settings.scope === "current" && ["paid", "reserved", "shortfall"].includes(settings.scenario)) {
      settings.scenario = "ready";
      document.getElementById("scenario").value = "ready";
    }
    sendSettings();
  });
}
document.getElementById("reset").addEventListener("click", () => {
  settings = {...initial};
  for (const [id, value] of Object.entries(initial)) {
    const control = document.getElementById(id);
    if (control?.tagName === "SELECT") control.value = value;
  }
  for (const frame of frames) frame.contentWindow.postMessage({kind: "sello-review-reset"}, location.origin);
  document.getElementById("review-live").textContent = "Ejemplo reiniciado. Gasto guiado: 10.000; ingreso guiado: 150.000.";
  sendSettings();
});
for (const button of document.querySelectorAll(".journey-nav button")) button.addEventListener("click", () => activate(button.dataset.page, button.dataset.page === "entry"));
window.addEventListener("message", event => {
  if (event.origin !== location.origin || !frames.some(frame => frame.contentWindow === event.source)) return;
  const message = event.data;
  if (message?.kind === "sello-review-ready") { sendSettings(); return; }
  if (message?.kind === "sello-review-entry") {
    settings.page = "entry";
    sendSettings();
    document.getElementById("entry-frame").contentWindow.postMessage({kind:"sello-review-entry-intent", mode:message.mode, category:message.category, origin:message.origin}, location.origin);
    return;
  }
  if (message?.kind !== "sello-review-action") return;
  if (message.action === "navigate") activate(message.page, message.page === "entry");
  if (message.action === "explain") document.getElementById("review-live").textContent = message.text;
  if (message.action === "save-expense") {
    settings.expenseAdded = true;
    settings.expenseCategory = message.category;
    settings.notice = "Gasto anotado";
    settings.scenario = "ready";
    document.getElementById("scenario").value = "ready";
    document.getElementById("review-live").textContent = "Resultado guiado: gasto de 10.000. El total cambia una vez; no aparece una pantalla de comprobante. No se escribió ningún dato real.";
    activate(message.origin || "home");
  }
  if (message.action === "save-income") {
    settings.incomeAdded = true;
    settings.incomeSource = message.source;
    settings.notice = "Ingreso anotado";
    document.getElementById("review-live").textContent = "Resultado guiado: ingreso de 150.000. Los presupuestos y la cantidad diaria de Recibo permanecen iguales. Sin guardado real.";
    activate(message.origin || "home");
  }
  if (message.action === "dismiss-notice") { settings.notice = ""; sendSettings(); }
  if (message.action === "goal") { settings.goal = message.goal; sendSettings(); }
  if (message.action === "theme") {
    settings.theme = message.theme;
    document.getElementById("theme").value = message.theme;
    sendSettings();
  }
});
for (const frame of frames) frame.addEventListener("load", sendSettings);
sendSettings();
