const $ = (selector) => document.querySelector(selector);
const money = (value) => new Intl.NumberFormat('es-SV', {style:'currency', currency:'USD'}).format(Number(value));
const state = {page:'inicio', operation:'depositos', clients:[], accounts:[], dialogSubmit:null};

async function api(path, options = {}) {
  const response = await fetch('/api' + path, {...options, headers: {'Content-Type':'application/json', ...(options.headers || {})}});
  const value = await response.json();
  if (!response.ok) throw new Error(value.error || 'No se pudo completar la solicitud.');
  return value;
}
function showNotice(message, error = false) {
  const box = $('#notice'); box.textContent = message; box.hidden = false; box.classList.toggle('error', error);
  clearTimeout(showNotice.timer); showNotice.timer = setTimeout(() => { box.hidden = true; }, 7000);
}
function fail(error) { showNotice(error.message || String(error), true); }
function node(tag, text, className) {
  const n = document.createElement(tag); if (text !== undefined) n.textContent = String(text);
  if (className) n.className = className; return n;
}
function clear(container) { container.replaceChildren(); }
function emptyRow(container, count, text) { const tr=node('tr'); const td=node('td',text,'empty-cell'); td.colSpan=count; tr.append(td); container.append(tr); }
function cell(tr, text) { tr.append(node('td', text)); }
function accountAction(tr, number, active) {
  const td=node('td'); td.append(node('span', active?'Activa':'Inactiva',active?'badge':'badge off'));
  const history=node('button','Historial','mini-action'); history.type='button';
  history.addEventListener('click', () => { $('#history-form input').value=number; navigate('historial'); loadHistory(number); }); td.append(history);
  if(active){const off=node('button','Desactivar','mini-action danger'); off.type='button'; off.addEventListener('click',async()=>{
    if(!confirm('¿Desactivar la cuenta '+number+'? Esta acción no elimina el historial.')) return;
    try { const r=await api('/cuentas/'+encodeURIComponent(number)+'/desactivar',{method:'POST',body:'{}'});showNotice(r.mensaje);await loadAccounts();await loadSummary(); } catch(e){fail(e);}
  });td.append(off);} tr.append(td);
}
async function loadSummary() {
  try {const s=await api('/resumen');$('#stat-clientes').textContent=s.clientes;$('#stat-cuentas').textContent=s.cuentasActivas;$('#stat-saldo').textContent=money(s.saldoTotal);
    const list=$('#recent-list');clear(list);
    if(!s.recientes.length){list.append(node('div','Aún no hay movimientos registrados.','empty'));return;}
    s.recientes.forEach(t=>{const item=node('div',undefined,'activity-item');const icon=node('span','⇄','activity-icon');const main=node('div',undefined,'activity-main');main.append(node('strong',t.tipo),node('small',t.numeroCuenta+' · '+new Date(t.fecha).toLocaleString('es-SV')));item.append(icon,main,node('span',money(t.monto),'activity-amount'));list.append(item);});
  }catch(e){fail(e);}
}
async function loadClients() {
  try {state.clients=await api('/clientes');renderClients();}catch(e){fail(e);}
}
function renderClients() {
  const body=$('#clients-body');clear(body);const query=$('#client-search').value.toLowerCase();
  const matches=state.clients.filter(c=>(c.nombre+' '+c.apellidos+' '+c.dui).toLowerCase().includes(query));
  if(!matches.length){emptyRow(body,4,'No hay clientes que coincidan.');return;}
  matches.forEach(c=>{const tr=node('tr');const name=node('td');name.append(node('strong',c.nombre+' '+c.apellidos),node('small',c.direccion));tr.append(name);cell(tr,c.dui);const contact=node('td');contact.append(node('strong',c.correo),node('small',c.telefono));tr.append(contact);
    const actions=node('td');const edit=node('button','Editar','mini-action');edit.type='button';edit.addEventListener('click',()=>openClientDialog(c));actions.append(edit);
    const remove=node('button','Eliminar','mini-action danger');remove.type='button';remove.addEventListener('click',async()=>{if(!confirm('¿Eliminar a '+c.nombre+'? No se eliminarán cuentas ni transacciones bancarias.'))return;try{const r=await api('/clientes/'+encodeURIComponent(c.dui),{method:'DELETE'});showNotice(r.mensaje);await loadClients();await loadSummary();}catch(e){fail(e);}});actions.append(remove);tr.append(actions);body.append(tr);});
}
async function loadAccounts() {try{state.accounts=await api('/cuentas');renderAccounts();}catch(e){fail(e);}}
function renderAccounts(){const body=$('#accounts-body');clear(body);const q=$('#account-search').value.toLowerCase();const matches=state.accounts.filter(c=>(c.numeroCuenta+' '+c.duiCliente).toLowerCase().includes(q));if(!matches.length){emptyRow(body,5,'No hay cuentas que coincidan.');return;}matches.forEach(c=>{const tr=node('tr');cell(tr,c.numeroCuenta);cell(tr,c.duiCliente);cell(tr,c.tipoCuenta);cell(tr,money(c.saldo));accountAction(tr,c.numeroCuenta,c.activa);body.append(tr);});}
async function loadHistory(number){try{const items=await api('/cuentas/'+encodeURIComponent(number)+'/historial');const body=$('#history-body');clear(body);if(!items.length){emptyRow(body,5,'Esta cuenta aún no tiene movimientos.');return;}items.forEach(t=>{const tr=node('tr');cell(tr,new Date(t.fecha).toLocaleString('es-SV'));cell(tr,t.tipo);cell(tr,money(t.monto));cell(tr,money(t.saldoPosterior));cell(tr,t.descripcion||'—');body.append(tr);});}catch(e){fail(e);}}
function navigate(page){state.page=page;document.querySelectorAll('.page').forEach(p=>p.classList.toggle('active',p.id==='page-'+page));document.querySelectorAll('.nav-item').forEach(b=>b.classList.toggle('active',b.dataset.page===page));$('#page-title').textContent=({inicio:'Vista general',clientes:'Clientes',cuentas:'Cuentas',operaciones:'Operaciones',historial:'Historial bancario',reportes:'Reportes'})[page];if(page==='inicio')loadSummary();if(page==='clientes')loadClients();if(page==='cuentas')loadAccounts();window.scrollTo({top:0,behavior:'smooth'});}
function field(name,label,value='',opts={}){const wrap=node('label',label);const input=node('input');input.name=name;input.value=value;input.required=true;if(opts.type)input.type=opts.type;if(opts.pattern)input.pattern=opts.pattern;if(opts.placeholder)input.placeholder=opts.placeholder;if(opts.maxlength)input.maxLength=opts.maxlength;if(opts.readOnly)input.readOnly=true;wrap.append(input);return wrap;}
function dialog(title,kicker,fields,submit){$('#dialog-title').textContent=title;$('#dialog-kicker').textContent=kicker;$('#dialog-fields').replaceChildren(...fields);state.dialogSubmit=submit;$('#form-dialog').showModal();}
function openClientDialog(c){const editing=!!c;dialog(editing?'Editar cliente':'Registrar cliente','CLIENTES',[
  field('dui','DUI',c?.dui||'',{pattern:'[0-9]{8}-[0-9]',placeholder:'12345678-9',readOnly:editing}),
  field('fechaNacimiento','Fecha de nacimiento',c?.fechaNacimiento||'',{type:'date',readOnly:editing}),
  field('nombre','Nombre',c?.nombre||''),field('apellidos','Apellidos',c?.apellidos||''),
  field('correo','Correo electrónico',c?.correo||'',{type:'email'}),field('telefono','Teléfono',c?.telefono||'',{pattern:'[0-9]{8}',placeholder:'00000000'}),
  field('direccion','Dirección',c?.direccion||'')
],async data=>{const path=editing?'/clientes/'+encodeURIComponent(c.dui):'/clientes';const response=await api(path,{method:editing?'PUT':'POST',body:JSON.stringify(data)});showNotice(response.mensaje);await loadClients();await loadSummary();});}
function openAccountDialog(){dialog('Crear cuenta','CUENTAS',[
  field('numeroCuenta','Número de cuenta','',{placeholder:'Ej. FIN-001'}),field('duiCliente','DUI del cliente','',{pattern:'[0-9]{8}-[0-9]',placeholder:'12345678-9'}),
  (()=>{const label=node('label','Tipo de cuenta');const select=node('select');select.name='tipoCuenta';['Ahorro','Corriente'].forEach(v=>{const o=node('option',v);o.value=v;select.append(o);});label.append(select);return label;})()
],async data=>{const r=await api('/cuentas',{method:'POST',body:JSON.stringify(data)});showNotice(r.mensaje);await loadAccounts();await loadSummary();});}
function setOperation(value){state.operation=value;document.querySelectorAll('.tab').forEach(t=>t.classList.toggle('active',t.dataset.operation===value));const transfer=value==='transferencias';$('#origin-label').firstChild.textContent=transfer?'Cuenta origen':'Cuenta';$('#destination-wrap').hidden=!transfer;$('#destination-wrap input').required=transfer;}
async function generateReport(){const button=$('#generate-report');button.disabled=true;try{const r=await api('/reportes',{method:'POST',body:'{}'});$('#report-status').textContent='Generando reporte en segundo plano…';for(let i=0;i<40;i++){await new Promise(resolve=>setTimeout(resolve,500));const status=await api('/reportes/'+r.id);if(status.estado==='listo'){const link=node('a','Descargar reporte');link.href=status.url;link.download='finCore-reporte.txt';$('#report-status').replaceChildren(link);showNotice('Reporte listo para descargar.');return;}}$('#report-status').textContent='El reporte sigue procesándose. Vuelve a intentarlo.';}catch(e){fail(e);}finally{button.disabled=false;}}
document.querySelectorAll('[data-page]').forEach(b=>b.addEventListener('click',()=>navigate(b.dataset.page)));
document.querySelectorAll('[data-goto]').forEach(b=>b.addEventListener('click',()=>navigate(b.dataset.goto)));
$('#refresh-summary').addEventListener('click',loadSummary);$('#client-search').addEventListener('input',renderClients);$('#account-search').addEventListener('input',renderAccounts);
$('#new-client').addEventListener('click',()=>openClientDialog(null));$('#new-account').addEventListener('click',openAccountDialog);
$('#dialog-close').addEventListener('click',()=>$('#form-dialog').close());$('#dialog-cancel').addEventListener('click',()=>$('#form-dialog').close());
$('#dialog-form').addEventListener('submit',async e=>{e.preventDefault();const button=$('#dialog-submit');button.disabled=true;try{const data=Object.fromEntries(new FormData(e.currentTarget));await state.dialogSubmit(data);$('#form-dialog').close();}catch(error){fail(error);}finally{button.disabled=false;}});
document.querySelectorAll('.tab').forEach(b=>b.addEventListener('click',()=>setOperation(b.dataset.operation)));
$('#operation-form').addEventListener('submit',async e=>{e.preventDefault();const button=e.currentTarget.querySelector('button[type=submit]');button.disabled=true;try{const form=Object.fromEntries(new FormData(e.currentTarget));const data=state.operation==='transferencias'?{cuentaOrigen:form.numeroCuenta,cuentaDestino:form.cuentaDestino,monto:form.monto}:{numeroCuenta:form.numeroCuenta,monto:form.monto};const r=await api('/operaciones/'+state.operation,{method:'POST',body:JSON.stringify(data)});showNotice(r.mensaje);await loadSummary();e.currentTarget.reset();}catch(error){fail(error);}finally{button.disabled=false;}});
$('#history-form').addEventListener('submit',e=>{e.preventDefault();loadHistory(e.currentTarget.elements.namedItem('numeroCuenta').value.trim());});$('#generate-report').addEventListener('click',generateReport);
loadSummary();
