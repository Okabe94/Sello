/* Synthetic, pre-reviewed frames. These are layout fixtures, not a financial engine. */
"use strict";
const screenName = new URLSearchParams(location.search).get("screen") || "home";
const root = document.getElementById("app-root");
const dialog = document.getElementById("detail-dialog");
let settings = {scope:"future", theme:"light", ink:"cobalt", scale:"1", scenario:"ready", keyboard:"pad", "entry-state":"ready", expenseAdded:false, incomeAdded:false, expenseCategory:"Café", goal:"emergency", notice:"", page:"home"};
let mode = "expense";
let origin = "home";
let selectedHistory = "oct";
let incomeView = false;
const initialDraft = {expense:{amount:"10.000", category:"Café", note:"", date:"2026-10-22"}, income:{amount:"150.000", category:"Salario", note:"", other:"", date:"2026-10-22"}};
let draft = structuredClone(initialDraft);
let submitting = false;
const icons = {Hogar:"home", Comida:"restaurant", Transporte:"directions_bus", Café:"local_cafe", Salario:"account_balance", Freelance:"edit_note", "Ingresos pasivos":"insights", Transferencia:"receipt_long", Otro:"add"};
const categories = [
  {name:"Hogar", limit:"600.000", spent:"200.000", left:"400.000", fill:33},
  {name:"Comida", limit:"200.000", spent:"50.000", left:"150.000", fill:25},
  {name:"Transporte", limit:"100.000", spent:"40.000", left:"60.000", fill:40},
  {name:"Café", limit:"100.000", spent:"110.000", left:"−10.000", fill:100, over:true}
];
const expenseRows = {
  Hogar: {spent:"210.000", left:"390.000", fill:35},
  Comida: {spent:"60.000", left:"140.000", fill:30},
  Transporte: {spent:"50.000", left:"50.000", fill:50},
  Café: {spent:"120.000", left:"−20.000", fill:100, over:true}
};
const history = [
  {id:"may", label:"may", amount:"720.000", short:"720 mil", height:101},
  {id:"jun", label:"jun", amount:"680.000", short:"680 mil", height:96},
  {id:"jul", label:"jul", amount:"810.000", short:"810 mil", height:114},
  {id:"ago", label:"ago", amount:"760.000", short:"760 mil", height:107},
  {id:"sep", label:"sep", amount:"920.000", short:"920 mil", height:130}
];
const weekday = [
  {id:"lun",label:"lun",amount:"18.000",short:"18 mil",height:47},
  {id:"mar",label:"mar",amount:"24.000",short:"24 mil",height:62},
  {id:"mie",label:"mié",amount:"21.000",short:"21 mil",height:55},
  {id:"jue",label:"jue",amount:"30.000",short:"30 mil",height:78},
  {id:"vie",label:"vie",amount:"42.000",short:"42 mil",height:109},
  {id:"sab",label:"sáb",amount:"50.000",short:"50 mil",height:130},
  {id:"dom",label:"dom",amount:"28.000",short:"28 mil",height:73}
];
const goals = {
  emergency:{name:"Fondo de emergencia", percent:"57%", fill:57, icon:"home", saved:"570.000", target:"1.000.000"},
  holidays:{name:"Viaje a la costa", percent:"38%", fill:38, icon:"flight", saved:"456.000", target:"1.200.000"}
};
function escape(value) { return String(value).replace(/[&<>"']/g, c => ({"&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;"}[c])); }
function icon(name, extra="") { return `<svg class="icon ${extra}" aria-hidden="true"><use href="#i-${escape(name)}"></use></svg>`; }
function money(value, className="secondary-money") { return `<span class="money ${className}" aria-label="${escape(String(value).replace("−","menos "))} pesos">${escape(value).replaceAll(".",".<wbr>")}</span>`; }
function action(name, label, extra="") { return `<button type="button" class="text-action" data-action="${name}" ${extra}>${label}</button>`; }
function explain(text) { post("explain",{text}); }
function post(actionName, extra={}) { parent.postMessage({kind:"sello-review-action", action:actionName, ...extra}, location.origin); }
function nav(page, extra={}) { post("navigate",{page,...extra}); }
function label(text) { return `<span class="label">${text}</span>`; }
function leader(text, amount, className="") { return `<div class="leader ${className}"><span>${text}</span><span class="dots" aria-hidden="true"></span><b>${money(amount)}</b></div>`; }
function frameData() {
  const future = settings.scope === "future";
  const added = settings.expenseAdded;
  const rows = categories.map(row => ({...row, ...(added && row.name === settings.expenseCategory ? expenseRows[row.name] : {})}));
  const normal = {amount:future ? (added?"390.000":"400.000") : (added?"590.000":"600.000"), daily:future ? (added?"39.000":"40.000") : (added?"59.000":"60.000"), reserved:future?"200.000":null, spent:added?"410.000":"400.000", heading:"Te quedan este mes", stamp:"Al día", ink:"good", rows, comparison:added?"40.000":"50.000", change:added?"8,9%":"11,1%", compareWord:"menos", octShort:added?"410 mil":"400 mil", octHeight:added?58:57};
  if (settings.scenario === "paid") return {...normal, amount:"400.000",daily:"40.000",reserved:null,spent:"600.000",comparison:"150.000",change:"33,3%",compareWord:"más",octShort:"600 mil",octHeight:85,rows:categories.map(row=>row.name==="Hogar"?{...row,spent:"400.000",left:"200.000",fill:67}:row)};
  if (settings.scenario === "reserved") return {...normal,amount:"0",daily:"0",reserved:"100.000",spent:"900.000",stamp:"Sin margen libre",ink:"neutral",comparison:"450.000",change:"100%",compareWord:"más",octShort:"900 mil",octHeight:127,rows:pressureRows()};
  if (settings.scenario === "shortfall") return {...normal,amount:"50.000",daily:"0",reserved:"150.000",spent:"900.000",heading:"Por cubrir en tu presupuesto",stamp:"Por ajustar",ink:"neutral",comparison:"450.000",change:"100%",compareWord:"más",octShort:"900 mil",octHeight:127,rows:pressureRows()};
  if (settings.scenario === "over") return {...normal,amount:"−80.000",daily:"0",reserved:null,spent:"1.080.000",heading:"Restante del presupuesto",stamp:"Sobre el límite",ink:"over",comparison:"630.000",change:"140%",compareWord:"más",octShort:"1,08 M",octHeight:130,rows:[{...categories[0],spent:"600.000",left:"0",fill:100},{...categories[1],spent:"220.000",left:"−20.000",fill:100,over:true},{...categories[2],spent:"120.000",left:"−20.000",fill:100,over:true},{...categories[3],spent:"140.000",left:"−40.000",fill:100,over:true}]};
  if (settings.scenario === "no-limit") return {...normal,amount:normal.spent,daily:null,reserved:null,heading:"Has anotado en gastos",stamp:"Sin límite",ink:"neutral",rows:rows.map(row=>({...row,left:row.spent,fill:0,over:false,limit:null}))};
  return normal;
}
function pressureRows() { return [{...categories[0],spent:"600.000",left:"0",fill:100},{...categories[1],spent:"200.000",left:"0",fill:100},{...categories[2],spent:"50.000",left:"50.000",fill:50},{...categories[3],spent:"50.000",left:"50.000",fill:50,over:false}]; }
function statusbar() { return `<div class="statusbar" aria-hidden="true"><span>9:41</span><span class="status-icons"><svg viewBox="0 0 16 16"><path d="M1 13h3V9H1zm5 0h3V5H6zm5 0h3V1h-3z"/></svg><svg viewBox="0 0 16 16"><path d="M1 5a12 12 0 0 1 14 0L8 13z"/></svg><svg viewBox="0 0 16 16"><path d="M5 1h6v2h2v12H3V3h2z"/></svg></span></div>`; }
function topbar(title, subtitle="", back=false) { return `<div class="top-area">${statusbar()}<header class="topbar">${back?`<button class="icon-button" data-action="back" aria-label="Volver">${icon("arrow_back")}</button>`:""}<h1>${title}${subtitle?`<small>${subtitle}</small>`:""}</h1>${back?"":`<button class="icon-button" data-action="settings" aria-label="Configuración">${icon("settings")}</button>`}</header></div>`; }
function bottom(page) {
  const items = [["home","receipt_long","Recibo"],["summary","insights","Resumen"]];
  if(settings.scope === "future") items.push(["goals","account_balance","Metas"]);
  return `<div class="main-action"><button class="primary-button" data-action="new-expense" aria-label="Anotar un gasto">${icon("add")}Anotar</button></div><nav class="bottom-nav" aria-label="Navegación principal">${items.map(([id,ico,text])=>`<button data-action="tab" data-page="${id}" ${id===page?'aria-current="page"':""}>${icon(ico)}${text}</button>`).join("")}</nav><div class="gesture" aria-hidden="true"></div>`;
}
function notification() { return settings.notice?`<div class="notice" role="status"><span>${icon("check")} ${escape(settings.notice)}</span><button class="icon-button" data-action="dismiss-notice" aria-label="Cerrar aviso">×</button></div>`:""; }
function skeletons() { return `<div aria-label="Cargando información" role="status">${[1,2].map(()=>`<div class="slip"><div class="skeleton short"></div><div class="skeleton hero"></div><div class="tear"></div><div class="skeleton"></div></div>`).join("")}</div>`; }
function empty(title,body,button,actionName="new-expense") { return `<section class="slip pinked empty-slip">${icon("receipt_long","empty-icon")}<h2>${title}</h2><p>${body}</p><button class="primary-button outline" data-action="${actionName}">${button}</button></section>`; }
function failed() { return empty("No pudimos leer tus datos","Tus registros siguen aquí. Vuelve a intentarlo.","Reintentar","retry"); }
function stale() { return `<div class="stale-line" role="status">Estos datos podrían estar desactualizados.<button data-action="retry">Reintentar</button></div>`; }
function hero(data) { return `<section class="slip pinked summary-slip" aria-label="Resumen del presupuesto"><div class="answer-heading">${label(data.heading)}<span class="stamp ${data.ink}" aria-label="${data.stamp}">${data.stamp}</span></div><div class="hero-money ${data.ink==="over"?"budget-deficit":""}">${money(data.amount)}</div>${data.daily!==null?`<div class="tear" aria-hidden="true"></div><div class="allowance-row"><span>Puedes usar al día<br><span class="small">10 días, incluido hoy</span></span><b>${money(data.daily)}<small> / día</small></b></div>`:""}${data.reserved?`<p class="reserved">${icon("event")}<span>${money(data.reserved)} reservados para pagos previstos</span></p>`:""}${settings.scenario==="shortfall"?'<p class="small muted">Tus pagos previstos superan el presupuesto en 50.000.</p>':""}${settings.scenario==="over"?action("review-categories","Revisar categorías"):""}</section>`; }
function goalSlip() { const goal=goals[settings.goal]; return `<section class="slip goal-slip" aria-label="Tu meta destacada"><div class="goal-title"><span>Para lo que importa</span><span>${goal.percent}</span></div><div class="goal-name">${icon(goal.icon)}${goal.name}</div><div class="progress-track" role="progressbar" aria-label="${goal.name}" aria-valuenow="${goal.fill}" aria-valuemin="0" aria-valuemax="100"><span style="width:${goal.fill}%"></span></div><div class="goal-actions">${action("goal-contribute",`${icon("add")}Aportar`)}${action("goals","Ver meta →")}</div></section>`; }
function categorySlip(data) { return `<section class="slip" id="categories"><div class="section-heading"><div><h2>Tus categorías</h2><p class="small muted">${settings.scenario==="no-limit"?"Lo que has gastado":"Queda en cada una"}</p></div><button class="icon-button" data-action="create-category" aria-label="Agregar categoría">${icon("add")}</button></div><div class="category-grid">${data.rows.map(row=>`<button class="category-cell ${row.over?"over":""}" data-action="category" data-name="${row.name}" aria-label="${row.name}. ${row.over?"Sobre el límite por ":row.limit?"Quedan ":"Gastado "}${row.left.replace("−","")} pesos."><span class="category-circle" style="--fill:${row.fill}%">${icon(icons[row.name])}<span class="circle-on">${icon(icons[row.name])}</span></span><span class="category-text"><span>${row.name}</span><b>${money(row.left)}</b></span></button>`).join("")}</div></section>`; }
function home() {
  const data=frameData();
  let body;
  if(settings.scenario==="loading") body=skeletons();
  else if(settings.scenario==="failed") body=failed();
  else if(settings.scenario==="empty") body=empty("Dale forma a tu presupuesto","Empieza con una categoría para lo que importa este mes.","Crear categoría","create-category");
  else body=`${settings.scenario==="stale"?stale():""}<div class="home-columns"><div>${hero(data)}${settings.scope==="future"?goalSlip():""}${settings.scenario==="invitation"?`<button class="invitation" data-action="summary">${icon("insights")}Ver cómo cerraste septiembre →</button>`:""}</div><div class="home-side">${categorySlip(data)}</div></div>`;
  root.innerHTML=`<div class="screen">${topbar(`<button class="month-control" data-action="month">Octubre 2026 ${icon("chevron_right")}</button>`)}${`<main class="screen-content home-content blotter-underlay">${body}</main>`}${bottom("home")}${notification()}</div>`;
}
// Approximate chart geometry only. Exact displayed money always comes from fixtures.
function distributionRows(data) {
  const spent = row => Number(row.spent.replaceAll(".", ""));
  const total = Number(data.spent.replaceAll(".", ""));
  return data.rows.map(row => ({...row, share: 100 * spent(row) / total})).sort((a,b) => spent(b) - spent(a));
}
function summary() {
  if(incomeView) { income(); return; }
  const data=frameData();
  let body;
  if(settings.scenario==="loading") body=skeletons();
  else if(settings.scenario==="failed") body=failed();
  else if(settings.scenario==="empty") body=empty("Tu historia empieza aquí","Con tus primeros gastos podrás ver cómo va cambiando tu presupuesto.","Anotar gasto");
  else {
    const insufficient=settings.scenario==="insufficient";
    const oct={id:"oct",label:"oct*",amount:data.spent,short:data.octShort,height:data.octHeight};
    const months=[...history,oct];
    const maximum=Math.max(...months.map(item=>Number(item.amount.replaceAll(".",""))));
    const monthBars=months.map(item=>({...item,height:130*Number(item.amount.replaceAll(".",""))/maximum}));
    const selected=months.find(item=>item.id===selectedHistory)||oct;
    body=`${settings.scenario==="stale"?stale():""}<div class="summary-slips"><section class="slip pinked summary-comparison"><h2>¿Cómo va este mes?</h2>${insufficient?`<p class="comparison-answer">Tu primer mes</p><p class="comparison-caption">Cuando tengas otro mes podrás comparar el mismo período.</p>${leader("Gastos anotados",data.spent)}`:`<p class="comparison-answer">${money(data.comparison)}<span>${data.compareWord} que en septiembre</span></p><div class="trend ${data.compareWord==="más"?"plain":""}">${data.compareWord==="más"?"↑":"↓"} ${data.change.replace("%"," %")} al mismo corte</div><p class="comparison-caption">Comparamos del 1 al 22 de ambos meses.</p><div class="tear"></div>${leader("Octubre, hasta hoy",data.spent)}${leader("Septiembre, hasta el 22","450.000")}`}</section>${insufficient?empty("Un poco más de historia","Con otro mes de datos verás aquí tus comparaciones.","Anotar gasto"):`<section class="slip summary-history"><h2>Mes a mes</h2><p class="small muted">Gastos registrados · sin proyecciones</p><div class="history-chart" role="group" aria-label="Gastos por mes">${monthBars.map(item=>bar(item,item.id===selectedHistory,item.id==="oct")).join("")}</div><div class="chart-selection" aria-live="polite">${selected.label.replace("*","")}${selected.id==="oct"?", hasta el 22":" completo"}: <b>${money(selected.amount)}</b></div><p class="small muted">* Octubre está en curso. Los demás meses están completos.</p></section>`}<section class="slip summary-distribution"><h2>¿Dónde se va?</h2><p class="small muted">Este mes, hasta el 22 de octubre</p>${distributionRows(data).map(row=>`<button class="distribution-row" data-action="category" data-name="${row.name}">${icon(icons[row.name])}<span class="distribution-label">${row.name}<span class="progress-track" aria-hidden="true"><span style="width:${row.share}%"></span></span></span><b>${money(row.spent)}</b></button>`).join("")}${leader("Total",data.spent,"total-line")}</section>${settings.scope==="future"&&!insufficient?`<section class="slip weekday-slip"><h2>¿Qué días concentran tus gastos?</h2><p class="comparison-answer"><span>Los sábados</span></p><p class="small muted">Promedio por día · últimos 4 meses completos</p><div class="history-chart" aria-label="Promedios por día de la semana">${weekday.map(item=>bar(item,false,item.id==="sab","weekday")).join("")}</div><p class="small muted" id="weekday-detail">Cada día compara el total registrado con todas sus ocurrencias, incluidos días sin gastos.</p></section><section class="slip insights-slip"><h2>Una idea para tu presupuesto</h2><div class="insight"><strong>Café superó su límite en 3 de los últimos 4 meses.</strong><p class="muted">Puedes revisar su presupuesto para que encaje mejor contigo.</p>${action("budget-coffee","Revisar Café →")}</div></section>`:""}<button class="income-link" data-action="income">Tus ingresos y su destino ${icon("chevron_right")}</button></div>`;
  }
  root.innerHTML=`<div class="screen">${topbar("Resumen","Octubre · hasta el día 22")}<main class="screen-content summary-content blotter-underlay">${body}</main>${bottom("summary")}${notification()}</div>`;
}
function bar(item,selected,current,kind="history") { return `<button class="bar-mark ${current?"current":""}" data-action="${kind}" data-id="${item.id}" aria-pressed="${selected}" aria-label="${item.label.replace("*","")}: ${item.amount} pesos${item.id==="oct"?", mes en curso hasta el día 22":""}"><span class="bar-value">${item.short}</span><span class="bar" style="height:${item.height}px" aria-hidden="true"></span><span class="bar-label">${item.label}</span></button>`; }
function income() {
  if(["loading","failed","empty"].includes(settings.scenario)) {
    const content=settings.scenario==="loading"?skeletons():settings.scenario==="failed"?failed():empty("Tus ingresos empiezan aquí","Anota los ingresos que has recibido para entender cómo encajan en tu presupuesto.","Anotar ingreso","new-income");
    root.innerHTML=`<div class="screen">${topbar("Ingresos","Octubre 2026",true)}<main class="screen-content">${content}</main>${bottom("summary")}</div>`;
    return;
  }
  const noLimits=settings.scenario==="no-limit";
  const amount=settings.incomeAdded?"2.150.000":"2.000.000";
  const unassigned=noLimits?(settings.incomeAdded?"2.150.000":"2.000.000"):(settings.incomeAdded?"1.150.000":"1.000.000");
  const planned=noLimits?(settings.incomeAdded?"3.150.000":"3.000.000"):(settings.incomeAdded?"2.150.000":"2.000.000");
  root.innerHTML=`<div class="screen">${topbar("Ingresos","Octubre 2026",true)}<main class="screen-content">${settings.scenario==="stale"?stale():""}<section class="slip pinked"><p class="income-heading">Ingresos registrados</p><div class="hero-money">${money(amount)}</div><div class="tear"></div>${leader("Presupuestado en categorías",noLimits?"0":"1.000.000")}${leader("Por asignar al presupuesto",unassigned)}<p class="small muted">Una referencia para planear. No es el saldo de una cuenta.</p></section>${settings.scope==="future"?`<section class="slip"><h2>Lo previsto para este mes</h2>${leader("Salario pendiente","1.000.000")}${leader("Por asignar previsto",planned)}<p class="small muted">Incluye el ingreso pendiente. Aún no está registrado.</p></section>`:""}<section class="slip"><h2>Ingresos del mes</h2><button class="leader-button" data-action="income-record">${leader("Salario · jue 1 oct","2.000.000")}</button>${settings.incomeAdded?leader(`${escape(settings.incomeSource||"Salario")} · hoy, jue 22`,"150.000"):""}${action("new-income",`${icon("add")}Anotar ingreso`)}</section></main>${bottom("summary")}${notification()}</div>`;
}
function fixedPad() {
  const disabled=["busy","unknown"].includes(settings["entry-state"]);
  return `<div class="fixed-pad" role="group" aria-label="Teclado numérico de Sello">${["1","2","3","4","5","6","7","8","9","000","0","backspace"].map(key=>`<button type="button" data-action="pad-key" data-key="${key}" aria-label="${key==="backspace"?"Borrar último dígito":key==="000"?"Añadir tres ceros":key}" ${disabled?"disabled":""}>${key==="backspace"?"⌫":key}</button>`).join("")}</div>`;
}
function keyboard(type) {
  if(type==="pad") return fixedPad();
  if(type==="hidden") return "";
  return `<div class="system-keyboard" role="img" aria-label="Ilustración del teclado ${type==="text"?"de texto":"numérico"} del sistema; usa el campo o tu teclado físico para escribir"><div class="system-tools"><span>◉</span><span>${type==="text"?"Español":"123"}</span><span>⌄</span></div>${type==="numeric"?`<div class="system-numbers">${["1","2","3","⌫","4","5","6","↵","7","8","9","",".","0",",",""] .map((key,index)=>`<span class="system-key ${index%4===3?"alt":""}">${key}</span>`).join("")}</div>`:`<div class="system-letters">${["qwertyuiop","asdfghjkl","zxcvbnm"].map(row=>`<div class="system-letter-row">${[...row].map(key=>`<span>${key}</span>`).join("")}</div>`).join("")}<div class="system-space">Español</div></div>`}<div class="gesture"></div></div>`;
}
function amountError(value) {
  if(!value) return "";
  if(!/^(?:[1-9][0-9]{0,11}|[1-9][0-9]{0,2}(?:\.[0-9]{3}){1,3})$/.test(value)) return "Escribe un monto entero, como 10.000. Conservamos lo que escribiste.";
  return "";
}
function noteError(data) { return [...data.note].length > 60 ? "La nota puede tener hasta 60 caracteres. Conservamos el texto completo." : ""; }
function sourceError(data) {
  if(mode !== "income" || data.category !== "Otro") return "";
  const count = [...(data.other || "").trim()].length;
  return count === 0 ? "Escribe el nombre del origen." : count > 24 ? "El nombre puede tener hasta 24 caracteres." : "";
}
function canSubmit(data) { return Boolean(data.amount) && !amountError(data.amount) && !noteError(data) && !sourceError(data); }
function dateLabel(value) { return value === "2026-10-22" ? "Hoy, jue 22" : value.split("-").reverse().join("/"); }
function entry() {
  const data=draft[mode];
  const state=settings["entry-state"];
  const busy=state==="busy"||state==="unknown";
  const options=mode==="expense"?Object.keys(icons).slice(0,4):["Salario","Freelance","Ingresos pasivos","Transferencia","Otro"];
  const title=mode==="expense"?"Anotar gasto":"Anotar ingreso";
  root.innerHTML=`<div class="screen entry-screen ${settings.keyboard==="pad"?"fixed-pad-entry":""}">${topbar(title,"",true)}<div class="entry-switch" role="group" aria-label="Tipo de registro"><button data-action="entry-mode" data-mode="expense" aria-pressed="${mode==="expense"}" ${busy?"disabled":""}>Gasto</button><button data-action="entry-mode" data-mode="income" aria-pressed="${mode==="income"}" ${busy?"disabled":""}>Ingreso</button></div><main class="entry-content"><div class="entry-columns"><div><section class="slip amount-slip"><div class="amount-top"><label for="amount">Monto en pesos</label><button class="date-control" data-action="date" ${busy?"disabled":""}>${icon("event")}${dateLabel(data.date)}</button></div><input id="amount" class="amount-input" type="text" inputmode="${settings.keyboard==="pad"?"none":"numeric"}" enterkeyhint="done" autocomplete="off" spellcheck="false" value="${escape(data.amount)}" placeholder="0" aria-describedby="amount-error" aria-invalid="${Boolean(amountError(data.amount))}" ${busy?"disabled":""}><p id="amount-error" class="field-error" role="status" ${!amountError(data.amount)?"hidden":""}>${amountError(data.amount)}</p></section>${state==="restored"?'<p class="entry-restored" role="status">Tu borrador está aquí.</p>':""}</div><div class="entry-fields"><p class="entry-section-label">${mode==="expense"?"Categoría":"Origen"}</p><div class="chip-row" role="group" aria-label="${mode==="expense"?"Categoría del gasto":"Origen del ingreso"}">${options.map(name=>`<button class="chip" data-action="choose-category" data-name="${name}" aria-pressed="${data.category===name}" ${busy?"disabled":""}>${icon(icons[name])}${name}</button>`).join("")}</div>${mode==="income"&&data.category==="Otro"?`<label class="entry-section-label" for="source-name">Nombre del origen</label><button type="button" class="source-input note-control" id="source-name" data-action="edit-source" aria-haspopup="dialog" aria-describedby="source-error" ${busy?"disabled":""}>${escape(data.other||"¿De dónde viene?")}</button><p id="source-error" class="field-error" role="status" ${!sourceError(data)?"hidden":""}>${sourceError(data)}</p>`:""}<label class="entry-section-label" for="note">Nota <span class="muted">· opcional</span></label><button type="button" id="note" class="note-input note-control" data-action="edit-note" aria-haspopup="dialog" aria-describedby="note-error" ${busy?"disabled":""}>${escape(data.note||"Agregar nota")} ${icon("edit_note")}</button><p id="note-error" class="field-error" role="status" ${!noteError(data)?"hidden":""}>${noteError(data)}</p></div></div></main><div class="entry-action">${state==="unknown"?'<div class="entry-feedback" role="status">Aún no sabemos si se guardó. Comprueba el resultado antes de volver a anotar.</div>':state==="rejected"?'<div class="entry-feedback entry-failed" role="status">No se guardó. Tu borrador sigue aquí.</div>':""}<button id="submit" class="primary-button" data-action="submit" ${state==="busy"||(state!=="unknown"&&!canSubmit(data))?"disabled":""}>${state==="busy"?"Guardando…":state==="unknown"?"Comprobar si se guardó":`${icon("add")}${title}`}</button></div><div id="keyboard-slot">${keyboard(settings.keyboard)}</div>${settings.keyboard==="hidden"?'<div class="gesture"></div>':""}</div>`;
  if(innerHeight<480 && Number(settings.scale)>=1.3 && settings.keyboard!=="pad") root.querySelector(".entry-content").prepend(root.querySelector(".entry-switch"));
  document.getElementById("amount").addEventListener("input",event=>{
    data.amount=event.target.value;
    const error=amountError(data.amount);
    const errorNode=document.getElementById("amount-error");
    errorNode.textContent=error;
    errorNode.hidden=!error;
    event.target.setAttribute("aria-invalid",String(Boolean(error)));
    document.getElementById("submit").disabled=!canSubmit(data);
  });
  document.getElementById("amount").addEventListener("focus",()=>showKeyboard("numeric"));
  document.getElementById("amount").addEventListener("keydown",event=>{if(event.key==="Enter"){event.preventDefault();submit();}});

}
function showKeyboard(type) { document.getElementById("keyboard-slot").innerHTML=keyboard(settings.keyboard==="pad"?"pad":settings.keyboard==="hidden"?"hidden":type); }
function editText(field) {
  const isNote=field==="note", data=draft[mode], limit=isNote?60:24;
  const title=isNote?"Nota del registro":"Nombre del origen";
  openDialog(`<h2 id="dialog-heading">${title}</h2><label class="entry-section-label" for="text-draft">${isNote?"Opcional · hasta 60 caracteres":"Hasta 24 caracteres"}</label><textarea id="text-draft" class="note-input" rows="2" aria-describedby="text-error">${escape(data[field]||"")}</textarea><p id="text-error" class="field-error" role="status" hidden></p><div class="dialog-actions">${action("close-dialog","Cancelar")}<button id="confirm-text" class="primary-button" data-action="confirm-text" data-field="${field}">Listo</button></div><div class="dialog-keyboard">${keyboard("text")}</div>`,"text-editor");
  const input=document.getElementById("text-draft");
  const validate=()=>{
    const count=[...(isNote?input.value:input.value.trim())].length;
    const error=count>limit?`Usa hasta ${limit} caracteres. Conservamos lo que escribiste.`:!isNote&&count===0?"Escribe el nombre del origen.":"";
    document.getElementById("text-error").textContent=error;
    document.getElementById("text-error").hidden=!error;
    input.setAttribute("aria-invalid",String(Boolean(error)));
    document.getElementById("confirm-text").disabled=Boolean(error);
  };
  input.addEventListener("input",validate);validate();input.focus({preventScroll:true});
}
function padKey(key) {
  if(["busy","unknown"].includes(settings["entry-state"]))return;
  const input=document.getElementById("amount"), raw=input.value;
  if(raw && amountError(raw) && raw!=="0") {
    if(key==="backspace")input.setRangeText("",Math.max(0,(input.selectionStart??raw.length)-1),input.selectionEnd??raw.length,"end");
    else { explain("Corrige el monto original o bórralo antes de usar el pad. No se repara un pegado inválido."); return; }
  } else {
    const start=input.selectionStart??raw.length,end=input.selectionEnd??raw.length;
    const before=raw.slice(0,start).replaceAll(".",""),after=raw.slice(end).replaceAll(".","");
    let digits=key==="backspace"?(start===end?before.slice(0,-1):before)+after:before+key+after;
    if(key!=="backspace")digits=digits.replace(/^0+(?=\d)/,"");
    input.value=digits.replace(/\B(?=(\d{3})+(?!\d))/g,".");
  }
  input.dispatchEvent(new Event("input"));
  input.focus({preventScroll:true});
  input.setSelectionRange(input.value.length,input.value.length);
}
function submit() {
  const state=settings["entry-state"];
  if(state==="busy") return;
  if(state==="unknown") { explain("Revisión: este botón recupera el resultado de la misma operación; nunca crea otra. La maqueta no tiene una operación real que consultar."); return; }
  const data=draft[mode];
  if(submitting || !canSubmit(data)) return;
  if(settings[mode === "expense" ? "expenseAdded" : "incomeAdded"]) { explain("Este ejemplo ya incluye ese registro. Reinicia el ejemplo para repetir el recorrido sin duplicar sus cifras."); return; }
  if(!["ready", "insufficient", "invitation"].includes(settings.scenario)) { explain("Los estados alternativos son escenas independientes. Reinicia el ejemplo para probar un guardado guiado."); return; }
  const plain=data.amount.replaceAll(".","");
  if(plain!==(mode==="expense"?"10000":"150000")||data.date!=="2026-10-22") {
    explain("El campo permite revisar la entrada, pero las cifras de resultado son fixtures: usa 10.000 para un gasto o 150.000 para un ingreso y la fecha de ejemplo. No se guardó ningún dato. Reiniciar ejemplo restaura esos valores."); return;
  }
  submitting=true;
  document.getElementById("submit").disabled=true;
  post(mode==="expense"?"save-expense":"save-income",{category:data.category,source:data.category==="Otro"?data.other:data.category,origin});
}
function render() {
  document.body.dataset.theme=settings.theme;
  document.body.dataset.ink=settings.ink;
  document.body.style.setProperty("--scale",settings.scale);
  document.body.classList.toggle("large-type",Number(settings.scale)>=1.3);
  if(screenName==="home") home(); else if(screenName==="summary") summary(); else entry();
}
function openDialog(content, className="") { dialog.className=className;dialog.innerHTML=`<div class="dialog-body"><div class="dialog-close"><button class="icon-button" data-action="close-dialog" aria-label="Cerrar">×</button></div>${content}</div>`; dialog.showModal(); }
function openCategory(name) {
  const row=frameData().rows.find(item=>item.name===name);
  if(!row)return;
  openDialog(`<h2 id="dialog-heading">${row.name}</h2><p class="small muted">Octubre 2026</p>${leader("Gastado",row.spent)}${row.limit?leader("Límite",row.limit):'<p>Sin límite</p>'}${row.limit?leader("Restante",row.left):""}<button class="primary-button" data-action="new-expense" data-name="${name}">Anotar aquí</button>${action("edit-category","Editar categoría")}`);
}
function openGoals() {
  openDialog(`<h2 id="dialog-heading">Tus metas</h2>${Object.entries(goals).map(([id,goal])=>`<div class="goal-option"><b>${goal.name}</b><p>${money(goal.saved)} de ${money(goal.target)}</p><div class="progress-track" role="progressbar" aria-label="${goal.name}" aria-valuenow="${goal.fill}" aria-valuemin="0" aria-valuemax="100"><span style="width:${goal.fill}%"></span></div><p class="small muted">${goal.percent} completado</p>${action("pin-goal",settings.goal===id?"Elegida para Recibo":"Mostrar en Recibo",`data-goal="${id}"`)}</div>`).join("")}`);
}
root.addEventListener("pointerdown",event=>{if(event.target.closest('[data-action="pad-key"]'))event.preventDefault();});
root.addEventListener("click",handleAction);
dialog.addEventListener("click",handleAction);
function dirtyDraft() { return Object.keys(initialDraft[mode]).some(key => draft[mode][key] !== initialDraft[mode][key]); }
function handleAction(event) {
  const control=event.target.closest("[data-action]");
  if(!control||control.disabled)return;
  const actionName=control.dataset.action;
  if(actionName==="close-dialog")dialog.close();
  if(actionName==="dismiss-notice")post("dismiss-notice");
  if(actionName==="new-expense"||actionName==="new-income") {
    if(dialog.open)dialog.close();
    origin=screenName==="summary"?"summary":"home";
    // The entry frame owns its draft; communicate intent through the parent.
    parent.postMessage({kind:"sello-review-entry",mode:actionName==="new-income"?"income":"expense",category:control.dataset.name,origin},location.origin);
  }
  if(actionName==="tab") { if(control.dataset.page==="goals")openGoals();else nav(control.dataset.page); }
  if(actionName==="summary")nav("summary");
  if(actionName==="back") {
    if(screenName==="entry"&&["busy","unknown"].includes(settings["entry-state"])) { explain("Durante el guardado o un resultado incierto se conserva la operación; primero se comprueba su resultado."); return; }
    if(screenName==="summary"&&incomeView){incomeView=false;render();}
    else if(screenName==="entry"&&dirtyDraft()) openDialog(`<h2 id="dialog-heading">¿Descartar este borrador?</h2><p>Puedes seguir anotando o volver al recibo.</p>${action("close-dialog","Seguir anotando")}<button class="primary-button outline" data-action="discard">Descartar</button>`);
    else nav(origin);
  }
  if(actionName==="discard"){draft[mode]={...initialDraft[mode]};dialog.close();nav(origin);}
  if(actionName==="entry-mode"){mode=control.dataset.mode;entry();document.getElementById("amount").focus({preventScroll:true});}
  if(actionName==="choose-category"){draft[mode].category=control.dataset.name;const scroll=root.querySelector("main").scrollTop;entry();root.querySelector("main").scrollTop=scroll;document.getElementById(mode==="income"&&draft[mode].category==="Otro"?"source-name":"amount").focus({preventScroll:true});}
  if(actionName==="pad-key")padKey(control.dataset.key);
  if(actionName==="edit-note")editText("note");
  if(actionName==="edit-source")editText("other");
  if(actionName==="confirm-text") {
    const main=root.querySelector("main"),scroll=main.scrollTop;
    draft[mode][control.dataset.field]=document.getElementById("text-draft").value;
    dialog.close();entry();root.querySelector("main").scrollTop=scroll;document.getElementById("amount").focus({preventScroll:true});
  }
  if(actionName==="submit")submit();
  if(actionName==="date")openDialog(`<h2 id="dialog-heading">Fecha del registro</h2><label for="effective-date">Elige una fecha</label><input class="entry-date-native" type="date" id="effective-date" max="2026-10-22" value="${escape(draft[mode].date)}"><button class="primary-button" data-action="choose-date">Listo</button>`);
  if(actionName==="choose-date") {
    const value=document.getElementById("effective-date").value;
    if(value&&value<="2026-10-22"){draft[mode].date=value;dialog.close();entry();}
  }
  if(actionName==="income"){incomeView=true;render();}
  if(actionName==="category"||actionName==="budget-coffee")openCategory(control.dataset.name||"Café");
  if(actionName==="history"){selectedHistory=control.dataset.id;const old=root.querySelector("main")?.scrollTop;summary();root.querySelector("main").scrollTop=old;}
  if(actionName==="weekday"){const point=weekday.find(item=>item.id===control.dataset.id);document.getElementById("weekday-detail").innerHTML=`${point.label}: promedio ${money(point.amount)} por día en el período mostrado.`;}
  if(actionName==="review-categories")document.getElementById("categories")?.scrollIntoView({block:"nearest"});
  if(actionName==="goals")openGoals();
  if(actionName==="pin-goal"){post("goal",{goal:control.dataset.goal});dialog.close();}
  if(actionName==="retry")explain("Estado de lectura para revisión. Cambia Estado a Al día para comparar con una lectura correcta. No se sustituye un fallo por cifras inventadas.");
  if(actionName==="settings")openDialog(`<h2 id="dialog-heading">Apariencia</h2><p>Elige el tema.</p><div class="chip-row"><button class="chip" data-action="set-theme" data-theme="light" aria-pressed="${settings.theme==="light"}">Claro</button><button class="chip" data-action="set-theme" data-theme="dark" aria-pressed="${settings.theme==="dark"}">Oscuro</button></div>`);
  if(actionName==="set-theme"){post("theme",{theme:control.dataset.theme});dialog.close();}
  const outOfScope={"month":"El selector de mes existente se conserva. Esta revisión fija octubre de 2026 para comparar las mismas cifras; no rediseña el selector ni aprueba planificación futura.","create-category":"Crear categoría conserva su flujo existente. Esta maqueta propone su acceso visible en la cabecera de categorías; el editor conectado no se modifica.","edit-category":"Editar categoría conserva su flujo existente y sus confirmaciones. No se guarda nada desde esta maqueta.","income-record":"El historial sigue abriendo el editor de ingreso por identificador. Esta propuesta no modifica la corrección ni el deshacer.","goal-contribute":"Aportar abre el flujo propio de la meta. El gesto queda visible en Recibo; su formulario y celebración se diseñan cuando se implemente Metas."};
  if(outOfScope[actionName])explain(outOfScope[actionName]);
}
window.addEventListener("message",event=>{
  if(event.origin!==location.origin||event.source!==parent)return;
  const message=event.data;
  if(message?.kind==="sello-review-settings") {
    const oldScenario=settings["entry-state"];
    settings=message.settings;
    if(screenName==="entry"&&oldScenario!==settings["entry-state"]) {
      if(settings["entry-state"]==="empty")draft[mode].amount="";
      else if(settings["entry-state"]==="invalid")draft[mode].amount="1e3";
      else draft[mode].amount=mode==="expense"?"10.000":"150.000";
    }
    render();
  }
  if(message?.kind==="sello-review-focus"&&screenName==="entry") document.getElementById("amount")?.focus({preventScroll:true});
  if(message?.kind==="sello-review-entry-intent"&&screenName==="entry") {submitting=false;mode=message.mode;origin=message.origin;if(message.category)draft.expense.category=message.category;entry();document.getElementById("amount")?.focus({preventScroll:true});}
  if(message?.kind==="sello-review-reset") {mode="expense";incomeView=false;selectedHistory="oct";draft=structuredClone(initialDraft);submitting=false;if(dialog.open)dialog.close();render();}
});
render();
parent.postMessage({kind:"sello-review-ready"},location.origin);
