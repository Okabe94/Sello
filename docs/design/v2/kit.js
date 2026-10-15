// Shared helpers for the Sello v2 mockups. Money is exact COP text, never computed policy.
const fmt = n => (n < 0 ? "−" : "") + Math.abs(n).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
const spoken = n => (n < 0 ? "menos " : "") + fmt(Math.abs(n)) + " pesos";
const icon = (name, cls = "") => `<svg class="icon ${cls}" aria-hidden="true"><use href="#i-${name}"></use></svg>`;
const money = (n, cls = "") => `<span class="money ${n < 0 ? "neg" : ""} ${cls}" aria-label="${spoken(n)}">${fmt(n)}</span>`;
const statusbar = () => `<div class="statusbar"><span>9:41</span><span>▲ ● ▮</span></div>`;
const reviewOptions = ids => Object.fromEntries(ids.map(id => [id, document.getElementById(id).value]));

// App frame shared by the tab screens: month bar, scrolling content, Anotar dock and tabs.
function appShell({ month = "Octubre", body, tabs = 3, active = 0, toast, current }) {
  const all = [["receipt_long", "Recibo"], ["insights", "Resumen"], ["flag", "Metas"], ["account_balance", "Inversiones"]].slice(0, tabs);
  const nav = all.map(([ic, t], i) => `<button ${i === active ? 'aria-current="page"' : ""}><span class="pill">${icon(ic)}</span><span class="t">${t}</span></button>`).join("");
  return `<div class="top">${statusbar()}<div class="topbar"><button class="month" aria-label="Cambiar mes, ${month} 2026">${month}<small>2026</small>${icon("keyboard_arrow_down")}</button><button class="icon-btn" aria-label="Configuración">${icon("settings")}</button></div>${current ? pastRibbon(month, current) : ""}</div><main class="content">${body}</main><div class="bottom"><div class="dock"><button class="primary">${icon("add")}Anotar</button></div><nav class="nav">${nav}</nav><div class="gesture"></div></div>${toast ? `<div class="toast" role="status">${icon("check")}${toast}</div>` : ""}`;
}

// Shown on every tab screen while the selected month is not the current one.
const pastRibbon = (month, current) => `<div class="past-ribbon" role="status">${icon("event")}<span>Estás viendo <b>${month.toLowerCase()}</b></span><button>Volver a ${current}</button></div>`;
