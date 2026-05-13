const API = '/api';

const PRIORITY_LABEL = { LOW: 'Baixa', MEDIUM: 'Média', HIGH: 'Alta', URGENT: 'Urgente' };
const STATUS_LABEL = { PENDING: 'Pendente', IN_PROGRESS: 'Em andamento', DONE: 'Concluída', CANCELLED: 'Cancelada' };

let categoriesCache = [];

// ---------- Utilities ----------
async function api(path, options = {}) {
    const response = await fetch(API + path, {
        headers: { 'Content-Type': 'application/json' },
        ...options
    });
    if (response.status === 204) return null;
    const data = await response.json().catch(() => null);
    if (!response.ok) {
        const msg = data?.message || `Erro ${response.status}`;
        const fields = data?.fieldErrors?.map(f => `${f.field}: ${f.message}`).join(', ');
        throw new Error(fields ? `${msg} (${fields})` : msg);
    }
    return data;
}

function toast(message, kind = 'success') {
    const el = document.getElementById('toast');
    el.textContent = message;
    el.className = `toast ${kind}`;
    el.hidden = false;
    clearTimeout(toast._t);
    toast._t = setTimeout(() => { el.hidden = true; }, 3000);
}

function fmtDate(iso) {
    if (!iso) return '—';
    const d = new Date(iso);
    return d.toLocaleDateString('pt-BR');
}

function isOverdue(task) {
    if (!task.dueDate || task.status === 'DONE' || task.status === 'CANCELLED') return false;
    const today = new Date(); today.setHours(0, 0, 0, 0);
    return new Date(task.dueDate) < today;
}

// ---------- Tabs ----------
document.querySelectorAll('.tab').forEach(tab => {
    tab.addEventListener('click', () => {
        document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        const target = tab.dataset.tab;
        document.querySelectorAll('.panel').forEach(p => p.classList.add('hidden'));
        document.getElementById('panel-' + target).classList.remove('hidden');
        if (target === 'tasks') loadTasks();
        if (target === 'categories') loadCategories();
        if (target === 'metrics') loadMetrics();
    });
});

// ---------- Tasks ----------
async function loadTasks() {
    const status = document.getElementById('filter-status').value;
    const priority = document.getElementById('filter-priority').value;
    const params = new URLSearchParams({ size: 50, sort: 'createdAt,desc' });
    if (status) params.append('status', status);
    if (priority) params.append('priority', priority);

    const listEl = document.getElementById('task-list');
    listEl.innerHTML = '<div class="empty">Carregando…</div>';
    try {
        await ensureCategories();
        const page = await api(`/tasks?${params.toString()}`);
        renderTasks(page.content || []);
    } catch (err) {
        listEl.innerHTML = `<div class="empty">Erro: ${err.message}</div>`;
    }
}

function renderTasks(tasks) {
    const listEl = document.getElementById('task-list');
    if (tasks.length === 0) {
        listEl.innerHTML = '<div class="empty">Nenhuma tarefa encontrada.</div>';
        return;
    }
    listEl.innerHTML = tasks.map(t => {
        const overdue = isOverdue(t);
        return `
        <div class="task-item">
            <div class="task-main">
                <div class="task-title ${t.status === 'DONE' ? 'done' : ''}">
                    ${escapeHtml(t.title)}
                    <span class="badge priority-${t.priority}">${PRIORITY_LABEL[t.priority]}</span>
                    <span class="badge status-${t.status}">${STATUS_LABEL[t.status]}</span>
                    ${overdue ? '<span class="badge overdue">Atrasada</span>' : ''}
                </div>
                ${t.description ? `<div class="task-desc">${escapeHtml(t.description)}</div>` : ''}
                <div class="task-meta">
                    ${t.category ? `<span>📁 ${escapeHtml(t.category.name)}</span>` : ''}
                    <span>Vence: ${fmtDate(t.dueDate)}</span>
                    <span>Criada: ${fmtDate(t.createdAt)}</span>
                </div>
            </div>
            <div class="task-actions">
                ${t.status !== 'DONE' && t.status !== 'CANCELLED' ? `<button class="btn-icon" onclick="completeTask(${t.id})" title="Concluir">✓</button>` : ''}
                <button class="btn-icon" onclick="editTask(${t.id})" title="Editar">✎</button>
                <button class="btn-icon danger" onclick="deleteTask(${t.id})" title="Remover">✕</button>
            </div>
        </div>`;
    }).join('');
}

document.getElementById('task-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('task-id').value;
    const categoryId = document.getElementById('task-category').value;
    const dueDate = document.getElementById('task-due').value;
    const body = {
        title: document.getElementById('task-title').value,
        description: document.getElementById('task-desc').value || null,
        priority: document.getElementById('task-priority').value,
        status: document.getElementById('task-status').value,
        dueDate: dueDate || null,
        categoryId: categoryId ? Number(categoryId) : null
    };
    try {
        if (id) {
            await api(`/tasks/${id}`, { method: 'PUT', body: JSON.stringify(body) });
            toast('Tarefa atualizada');
        } else {
            await api('/tasks', { method: 'POST', body: JSON.stringify(body) });
            toast('Tarefa criada');
        }
        resetTaskForm();
        loadTasks();
    } catch (err) {
        toast(err.message, 'error');
    }
});

async function editTask(id) {
    try {
        const t = await api(`/tasks/${id}`);
        document.getElementById('task-id').value = t.id;
        document.getElementById('task-title').value = t.title;
        document.getElementById('task-desc').value = t.description || '';
        document.getElementById('task-priority').value = t.priority;
        document.getElementById('task-status').value = t.status;
        document.getElementById('task-due').value = t.dueDate || '';
        document.getElementById('task-category').value = t.category?.id || '';
        document.getElementById('task-submit').textContent = 'Salvar alterações';
        document.getElementById('task-cancel').hidden = false;
        window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (err) {
        toast(err.message, 'error');
    }
}

document.getElementById('task-cancel').addEventListener('click', resetTaskForm);

function resetTaskForm() {
    document.getElementById('task-form').reset();
    document.getElementById('task-id').value = '';
    document.getElementById('task-submit').textContent = 'Adicionar tarefa';
    document.getElementById('task-cancel').hidden = true;
}

async function completeTask(id) {
    try {
        await api(`/tasks/${id}/complete`, { method: 'PATCH' });
        toast('Tarefa concluída');
        loadTasks();
    } catch (err) {
        toast(err.message, 'error');
    }
}

async function deleteTask(id) {
    if (!confirm('Remover esta tarefa?')) return;
    try {
        await api(`/tasks/${id}`, { method: 'DELETE' });
        toast('Tarefa removida');
        loadTasks();
    } catch (err) {
        toast(err.message, 'error');
    }
}

document.getElementById('filter-status').addEventListener('change', loadTasks);
document.getElementById('filter-priority').addEventListener('change', loadTasks);
document.getElementById('btn-reload-tasks').addEventListener('click', loadTasks);

// ---------- Categories ----------
async function ensureCategories() {
    if (categoriesCache.length === 0) {
        categoriesCache = await api('/categories');
        const select = document.getElementById('task-category');
        select.innerHTML = '<option value="">— sem categoria —</option>' +
            categoriesCache.map(c => `<option value="${c.id}">${escapeHtml(c.name)}</option>`).join('');
    }
    return categoriesCache;
}

async function loadCategories() {
    const listEl = document.getElementById('category-list');
    listEl.innerHTML = '<div class="empty">Carregando…</div>';
    try {
        categoriesCache = await api('/categories');
        renderCategories(categoriesCache);
        // refresh select in task form
        const select = document.getElementById('task-category');
        const current = select.value;
        select.innerHTML = '<option value="">— sem categoria —</option>' +
            categoriesCache.map(c => `<option value="${c.id}">${escapeHtml(c.name)}</option>`).join('');
        select.value = current;
    } catch (err) {
        listEl.innerHTML = `<div class="empty">Erro: ${err.message}</div>`;
    }
}

function renderCategories(items) {
    const listEl = document.getElementById('category-list');
    if (items.length === 0) {
        listEl.innerHTML = '<div class="empty">Nenhuma categoria cadastrada.</div>';
        return;
    }
    listEl.innerHTML = items.map(c => `
        <div class="category-item">
            <div class="task-main">
                <div class="task-title">${escapeHtml(c.name)}</div>
                ${c.description ? `<div class="task-desc">${escapeHtml(c.description)}</div>` : ''}
            </div>
            <div class="task-actions">
                <button class="btn-icon" onclick="editCategory(${c.id})" title="Editar">✎</button>
                <button class="btn-icon danger" onclick="deleteCategory(${c.id})" title="Remover">✕</button>
            </div>
        </div>
    `).join('');
}

document.getElementById('category-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const id = document.getElementById('category-id').value;
    const body = {
        name: document.getElementById('category-name').value,
        description: document.getElementById('category-desc').value || null
    };
    try {
        if (id) {
            await api(`/categories/${id}`, { method: 'PUT', body: JSON.stringify(body) });
            toast('Categoria atualizada');
        } else {
            await api('/categories', { method: 'POST', body: JSON.stringify(body) });
            toast('Categoria criada');
        }
        resetCategoryForm();
        categoriesCache = [];
        loadCategories();
    } catch (err) {
        toast(err.message, 'error');
    }
});

function editCategory(id) {
    const c = categoriesCache.find(x => x.id === id);
    if (!c) return;
    document.getElementById('category-id').value = c.id;
    document.getElementById('category-name').value = c.name;
    document.getElementById('category-desc').value = c.description || '';
    document.getElementById('category-submit').textContent = 'Salvar alterações';
    document.getElementById('category-cancel').hidden = false;
}

document.getElementById('category-cancel').addEventListener('click', resetCategoryForm);

function resetCategoryForm() {
    document.getElementById('category-form').reset();
    document.getElementById('category-id').value = '';
    document.getElementById('category-submit').textContent = 'Adicionar categoria';
    document.getElementById('category-cancel').hidden = true;
}

async function deleteCategory(id) {
    if (!confirm('Remover esta categoria?')) return;
    try {
        await api(`/categories/${id}`, { method: 'DELETE' });
        toast('Categoria removida');
        categoriesCache = [];
        loadCategories();
    } catch (err) {
        toast(err.message, 'error');
    }
}

// ---------- Metrics ----------
async function loadMetrics() {
    const overview = document.getElementById('metrics-overview');
    overview.innerHTML = '<div class="empty">Carregando…</div>';
    try {
        const m = await api('/metrics');
        overview.innerHTML = `
            <div class="metric-card">
                <div class="metric-label">Total</div>
                <div class="metric-value">${m.totalTasks}</div>
            </div>
            <div class="metric-card accent">
                <div class="metric-label">Em andamento</div>
                <div class="metric-value">${m.inProgressTasks}</div>
            </div>
            <div class="metric-card success">
                <div class="metric-label">Concluídas</div>
                <div class="metric-value">${m.doneTasks}</div>
            </div>
            <div class="metric-card danger">
                <div class="metric-label">Atrasadas</div>
                <div class="metric-value">${m.overdueTasks}</div>
            </div>
            <div class="metric-card">
                <div class="metric-label">Taxa de conclusão</div>
                <div class="metric-value">${m.completionRate}%</div>
            </div>
        `;
        renderBars('metrics-status', m.tasksByStatus, STATUS_LABEL);
        renderBars('metrics-priority', m.tasksByPriority, PRIORITY_LABEL);
    } catch (err) {
        overview.innerHTML = `<div class="empty">Erro: ${err.message}</div>`;
    }
}

function renderBars(elId, data, labels) {
    const el = document.getElementById(elId);
    const total = Math.max(1, Object.values(data).reduce((a, b) => a + b, 0));
    el.innerHTML = Object.entries(data).map(([k, v]) => `
        <div class="bar-row">
            <span class="label">${labels[k] || k}</span>
            <div class="bar"><div class="bar-fill" style="width: ${(v / total * 100).toFixed(1)}%"></div></div>
            <span class="value">${v}</span>
        </div>
    `).join('');
}

document.getElementById('btn-reload-metrics').addEventListener('click', loadMetrics);

// ---------- Helpers ----------
function escapeHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

// ---------- Init ----------
loadTasks();
