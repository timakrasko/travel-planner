const API_URL = '/api/travel-plans';
let planModal; // Змінна для керування модальним вікном

document.addEventListener('DOMContentLoaded', () => {
    loadPlans();
    planModal = new bootstrap.Modal(document.getElementById('planModal'));
});

document.getElementById('createPlanForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const submitBtn = e.target.querySelector('button[type="submit"]');
    const originalText = submitBtn.innerHTML;
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';

    const data = {
        title: document.getElementById('title').value,
        description: document.getElementById('description').value,
        budget: parseFloat(document.getElementById('budget').value) || 0,
        currency: document.getElementById('currency').value,
        is_public: document.getElementById('isPublic').checked
    };

    try {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        if (response.ok) {
            document.getElementById('createPlanForm').reset();
            await loadPlans();
        } else {
            const err = await response.json();
            alert('Помилка: ' + (err.message || 'Не вдалося створити'));
        }
    } catch (error) {
        console.error(error);
        alert('Помилка сервера');
    } finally {
        submitBtn.disabled = false;
        submitBtn.innerHTML = originalText;
    }
});

async function loadPlans() {
    const container = document.getElementById('plansContainer');
    const countBadge = document.getElementById('plansCount');

    try {
        const response = await fetch(API_URL);
        if (!response.ok) throw new Error('Failed to fetch');

        const plans = await response.json();
        container.innerHTML = '';
        countBadge.textContent = plans.length;

        if (plans.length === 0) {
            container.innerHTML = `
                <div class="col-12 text-center text-muted py-4">
                    <i class="bi bi-emoji-frown fs-1"></i>
                    <p class="mt-2">Створіть свою першу подорож!</p>
                </div>`;
            return;
        }

        plans.forEach(plan => {
            const col = document.createElement('div');
            col.className = 'col';
            col.innerHTML = `
                <div class="card h-100 shadow-sm hover-shadow">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-start mb-2">
                            <h5 class="card-title text-primary text-truncate mb-0" title="${escapeHtml(plan.title)}">
                                ${escapeHtml(plan.title)}
                            </h5>
                            <span class="badge ${plan.is_public ? 'text-bg-info' : 'text-bg-secondary'} rounded-pill">
                                ${plan.is_public ? 'Public' : 'Private'}
                            </span>
                        </div>
                        <h6 class="card-subtitle mb-3 text-success fw-bold">
                            ${plan.budget ? plan.budget.toFixed(2) : '0.00'} ${plan.currency}
                        </h6>
                        <p class="card-text text-muted small" style="display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden;">
                            ${escapeHtml(plan.description || 'Немає опису')}
                        </p>
                    </div>
                    <div class="card-footer bg-white border-top-0 d-flex gap-2 pb-3">
                        <button onclick="openPlanDetails('${plan.id}')" class="btn btn-outline-primary btn-sm flex-grow-1">
                            <i class="bi bi-map"></i> Деталі
                        </button>
                        <button onclick="deletePlan('${plan.id}')" class="btn btn-outline-danger btn-sm">
                            <i class="bi bi-trash"></i>
                        </button>
                    </div>
                </div>
            `;
            container.appendChild(col);
        });
    } catch (error) {
        container.innerHTML = `<div class="alert alert-danger w-100">Не вдалося завантажити дані. Сервер не відповідає.</div>`;
    }
}

window.openPlanDetails = async (id) => {
    try {
        const response = await fetch(`${API_URL}/${id}`);
        if (!response.ok) throw new Error('Not found');

        const plan = await response.json();

        document.getElementById('modalPlanTitle').textContent = plan.title;
        document.getElementById('modalPlanDescription').textContent = plan.description || 'Немає опису';
        document.getElementById('modalPlanBudget').textContent = `${plan.budget} ${plan.currency}`;
        document.getElementById('modalPlanStatus').textContent = plan.is_public ? 'Публічна' : 'Приватна';

        document.getElementById('currentPlanId').value = plan.id;

        renderLocationsList(plan.locations || []);

        planModal.show();
    } catch (error) {
        console.error(error);
        alert('Не вдалося завантажити деталі подорожі');
    }
};

document.getElementById('addLocationForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const planId = document.getElementById('currentPlanId').value;
    const nameInput = document.getElementById('locName');
    const notesInput = document.getElementById('locNotes');

    const newLocation = {
        name: nameInput.value,
        notes: notesInput.value,
        visitOrder: document.getElementById('locationsList').children.length + 1
    };

    try {
        const response = await fetch(`${API_URL}/${planId}/locations`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newLocation)
        });

        if (response.ok) {
            nameInput.value = '';
            notesInput.value = '';
            openPlanDetails(planId);
        } else {
            const err = await response.json();
            alert('Помилка: ' + (err.message || 'Не вдалося додати локацію'));
        }
    } catch (error) {
        console.error(error);
        alert('Помилка з\'єднання');
    }
});

function renderLocationsList(locations) {
    const list = document.getElementById('locationsList');
    list.innerHTML = '';

    if (!locations || locations.length === 0) {
        list.innerHTML = '<div class="text-center text-muted p-3 fst-italic">Локацій ще немає</div>';
        return;
    }

    locations.sort((a, b) => (a.visitOrder || 0) - (b.visitOrder || 0));

    locations.forEach((loc, idx) => {
        const item = document.createElement('div');
        item.className = 'list-group-item d-flex justify-content-between align-items-center';
        item.innerHTML = `
            <div>
                <span class="badge bg-light text-dark border me-2">${idx + 1}</span>
                <span class="fw-medium">${escapeHtml(loc.name)}</span>
                <div class="small text-muted ms-4">${escapeHtml(loc.notes || '')}</div>
            </div>
        `;
        list.appendChild(item);
    });
}

window.deletePlan = async (id) => {
    if (!confirm('Видалити цю подорож?')) return;
    try {
        const res = await fetch(`${API_URL}/${id}`, { method: 'DELETE' });
        if (res.ok) loadPlans();
        else alert('Не вдалося видалити');
    } catch (e) {
        console.error(e);
        alert('Помилка');
    }
};

function escapeHtml(text) {
    if (!text) return '';
    return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}