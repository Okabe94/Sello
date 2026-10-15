// Shared helpers for the Sello v2 mockups. Money is exact COP text, never computed policy.
const fmt = n => (n < 0 ? "−" : "") + Math.abs(n).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
const spoken = n => (n < 0 ? "menos " : "") + fmt(Math.abs(n)) + " pesos";
const icon = (name, cls = "") => `<svg class="icon ${cls}" aria-hidden="true"><use href="#i-${name}"></use></svg>`;
const money = (n, cls = "") => `<span class="money ${n < 0 ? "neg" : ""} ${cls}" aria-label="${spoken(n)}">${fmt(n)}</span>`;
const statusbar = () => `<div class="statusbar"><span>9:41</span><span>▲ ● ▮</span></div>`;
const reviewOptions = ids => Object.fromEntries(ids.map(id => [id, document.getElementById(id).value]));
