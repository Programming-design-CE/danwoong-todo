/* =========================================================
   group-todo.js  –  프로젝트(공동) 할 일 프론트엔드 로직
   ========================================================= */

// ── 상수 / 상태 ──────────────────────────────────────
const API_BASE = '';
let currentGroupId = 1;          // 기본 그룹 (추후 동적)
let todos = [];
let editingTodoId = null;        // null → 추가, 숫자 → 수정
let selectedAssignees = [];
let selectedCategory = '';
let selectedPriority = 'MEDIUM';
let garlicReward = 10;
let distMode = 'equal';          // equal | custom
let customDistMap = {};          // { userId: percent }

// 더미 그룹 멤버 (서버 멤버 API 없으므로 프레젠테이션용)
const groupMembers = [
  { user_id: 1, nickname: '민수' },
  { user_id: 2, nickname: '지연' },
  { user_id: 3, nickname: '예린' },
  { user_id: 4, nickname: '준호' },
];

const CATEGORY_MAP = {
  '발표 준비': { css: 'presentation', icon: '📋' },
  '보고서': { css: 'report', icon: '📄' },
  '기능 구현': { css: 'dev', icon: '💻' },
  '자료 조사': { css: 'research', icon: '🔍' },
};

const PRIORITY_LABELS = { HIGH: '높음', MEDIUM: '보통', LOW: '낮음' };
const PRIORITY_CSS = { HIGH: 'high', MEDIUM: 'medium', LOW: 'low' };

// ── 유틸 ─────────────────────────────────────────────
function getAccessToken() { return localStorage.getItem('accessToken'); }

async function api(url, opts = {}) {
  const token = getAccessToken();
  const headers = { ...(opts.headers || {}) };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  if (!(opts.body instanceof FormData)) headers['Content-Type'] = 'application/json';

  const res = await fetch(API_BASE + url, { ...opts, headers });
  if (res.status === 204) return null;
  if (!res.ok) {
    console.error('API Error', res.status, await res.text());
    return null;
  }
  const text = await res.text();
  return text ? JSON.parse(text) : null;
}

function formatDeadline(d) {
  if (!d) return '';
  const dt = new Date(d);
  const days = ['일', '월', '화', '수', '목', '금', '토'];
  return `마감 ${dt.getMonth() + 1}/${String(dt.getDate()).padStart(2, '0')}(${days[dt.getDay()]})`;
}

function calcDday(d) {
  if (!d) return '';
  const diff = Math.ceil((new Date(d) - new Date()) / 86400000);
  return diff > 0 ? `D-${diff}` : diff === 0 ? 'D-Day' : `D+${Math.abs(diff)}`;
}

function avatarInitial(name) { return name ? name[0] : '?'; }

// ── 할 일 목록 렌더링 ────────────────────────────────
function renderTodoList() {
  const list = document.getElementById('todoList');
  list.innerHTML = '';
  const count = document.getElementById('inProgressCount');
  const inProgress = todos.filter(t => t.status !== 'COMPLETED');
  count.textContent = inProgress.length;

  if (todos.length === 0) {
    list.innerHTML = '<div style="text-align:center;padding:60px;color:#6a655c;">등록된 할 일이 없습니다.</div>';
    return;
  }

  todos.forEach(todo => {
    const catInfo = CATEGORY_MAP[todo.category] || { css: 'research', icon: '📌' };
    const prCss = PRIORITY_CSS[todo.priority] || 'medium';
    const prLabel = PRIORITY_LABELS[todo.priority] || '보통';
    const progress = todo.progress || Math.floor(Math.random() * 81 + 20);

    const assigneeHtml = (todo.assignees || []).slice(0, 3).map(a =>
      `<div class="assignee-avatar" title="${a.nickname}">${avatarInitial(a.nickname)}</div>`
    ).join('');
    const moreCount = (todo.assignees || []).length - 3;
    const moreHtml = moreCount > 0 ? `<div class="assignee-avatar assignee-more">+${moreCount}</div>` : '';

    const el = document.createElement('div');
    el.className = 'todo-item';
    el.dataset.todoId = todo.todo_id;
    el.innerHTML = `
      <div class="todo-status-icon ${catInfo.css}">${catInfo.icon}</div>
      <div class="todo-info">
        <span class="todo-name">${todo.todo_name}</span>
        <span class="todo-deadline">📅 ${formatDeadline(todo.deadline)}</span>
      </div>
      <span class="todo-category ${catInfo.css}">${todo.category || '기타'}</span>
      <div class="todo-progress">
        <div class="progress-bar"><div class="progress-fill ${prCss}" style="width:${progress}%"></div></div>
        <span class="progress-text">${progress}%</span>
      </div>
      <div class="todo-assignees">${assigneeHtml}${moreHtml}</div>
      <div class="todo-priority">
        <span class="priority-flag ${prCss}">🏴</span>
        <span class="priority-label ${prCss}">${prLabel}</span>
      </div>
      <button class="todo-more-btn" data-todo-id="${todo.todo_id}">⋮</button>
    `;
    list.appendChild(el);
  });
}

// ── 할 일 목록 로드 ──────────────────────────────────
async function loadTodos() {
  const data = await api(`/todo-groups/${currentGroupId}/todos?status=IN_PROGRESS`);
  if (data && data.todos) {
    todos = data.todos;
  } else {
    // 프레젠테이션용 더미 데이터
    todos = [
      { todo_id: 1, todo_name: '발표자료(PPT) 제작', category: '발표 준비', deadline: '2026-05-30', priority: 'HIGH', status: 'IN_PROGRESS', progress: 70, assignees: [{ user_id: 1, nickname: '민수' }, { user_id: 2, nickname: '지연' }, { user_id: 3, nickname: '예린' }, { user_id: 4, nickname: '준호' }] },
      { todo_id: 2, todo_name: '보고서 작성', category: '보고서', deadline: '2026-06-02', priority: 'MEDIUM', status: 'IN_PROGRESS', progress: 40, assignees: [{ user_id: 1, nickname: '민수' }, { user_id: 2, nickname: '지연' }, { user_id: 3, nickname: '예린' }] },
      { todo_id: 3, todo_name: '기능 구현 및 테스트', category: '기능 구현', deadline: '2026-06-05', priority: 'HIGH', status: 'IN_PROGRESS', progress: 85, assignees: [{ user_id: 1, nickname: '민수' }, { user_id: 2, nickname: '지연' }, { user_id: 3, nickname: '예린' }] },
      { todo_id: 4, todo_name: '자료 조사 및 아이디어 수집', category: '자료 조사', deadline: '2026-05-28', priority: 'LOW', status: 'IN_PROGRESS', progress: 30, assignees: [{ user_id: 1, nickname: '민수' }, { user_id: 2, nickname: '지연' }] },
    ];
  }
  renderTodoList();
}

// ── 메모장 ───────────────────────────────────────────
async function loadMemo() {
  const data = await api(`/todo-groups/${currentGroupId}/note`);
  const ta = document.getElementById('memoTextarea');
  if (data && data.content) ta.value = data.content;
}

let memoTimer = null;
function autoSaveMemo() {
  clearTimeout(memoTimer);
  memoTimer = setTimeout(async () => {
    const content = document.getElementById('memoTextarea').value;
    await api(`/todo-groups/${currentGroupId}/note`, {
      method: 'PUT',
      body: JSON.stringify({ content }),
    });
    const now = new Date();
    document.getElementById('memoSaveStatus').textContent =
      `자동 저장됨 · 오늘 ${now.getHours()}:${String(now.getMinutes()).padStart(2, '0')}`;
  }, 1500);
}

// ── 컨텍스트 메뉴 (수정/삭제) ────────────────────────
function showContextMenu(e, todoId) {
  e.stopPropagation();
  const menu = document.getElementById('contextMenu');
  menu.classList.remove('hidden');
  menu.style.left = `${e.clientX - 60}px`;
  menu.style.top = `${e.clientY + 4}px`;
  menu.dataset.todoId = todoId;
}

function hideContextMenu() {
  document.getElementById('contextMenu').classList.add('hidden');
}

// ── 모달 열기/닫기 ───────────────────────────────────
function openModal(mode, todoData) {
  editingTodoId = mode === 'edit' ? todoData.todo_id : null;
  const overlay = document.getElementById('todoModal');
  overlay.classList.add('show');

  document.getElementById('modalTitle').textContent = mode === 'edit' ? '할 일 수정하기' : '할 일 추가하기';
  document.getElementById('modalSubmitText').textContent = mode === 'edit' ? '수정하기' : '추가하기';

  // 폼 초기화 / 채우기
  const titleInput = document.getElementById('todoTitleInput');
  const descInput = document.getElementById('todoDescInput');
  const deadlineInput = document.getElementById('todoDeadlineInput');

  if (mode === 'edit' && todoData) {
    titleInput.value = todoData.todo_name || '';
    descInput.value = todoData.description || '';
    deadlineInput.value = todoData.deadline || '';
    selectedCategory = todoData.category || '';
    selectedPriority = todoData.priority || 'MEDIUM';
    garlicReward = todoData.garlic_reward || 10;
    selectedAssignees = (todoData.assignees || []).map(a => a.user_id);
  } else {
    titleInput.value = '';
    descInput.value = '';
    deadlineInput.value = '';
    selectedCategory = '';
    selectedPriority = 'MEDIUM';
    garlicReward = 10;
    selectedAssignees = [];
  }

  updateCharCounts();
  updateCategoryChips();
  updatePriorityDisplay();
  updateGarlicDisplay();
  updateAssigneeDisplay();
  updateDistribution();
  updateDatePlaceholder();
}

function closeModal() {
  document.getElementById('todoModal').classList.remove('show');
  editingTodoId = null;
}

// ── 모달 내부 업데이트 함수들 ────────────────────────
function updateCharCounts() {
  document.getElementById('titleCharCount').textContent = document.getElementById('todoTitleInput').value.length;
  document.getElementById('descCharCount').textContent = document.getElementById('todoDescInput').value.length;
}

function updateCategoryChips() {
  document.querySelectorAll('#categoryChips .chip:not(.chip-add)').forEach(c => {
    c.classList.toggle('active', c.dataset.cat === selectedCategory);
  });
}

function updatePriorityDisplay() {
  document.getElementById('priorityText').textContent = PRIORITY_LABELS[selectedPriority] || '보통';
}

function updateGarlicDisplay() {
  document.getElementById('garlicCount').textContent = garlicReward;
  document.getElementById('distTotalGarlic').textContent = garlicReward;
}

function updateDatePlaceholder() {
  const val = document.getElementById('todoDeadlineInput').value;
  document.getElementById('datePlaceholder').style.display = val ? 'none' : 'block';
}

function updateAssigneeDisplay() {
  const area = document.getElementById('assigneeArea');
  const addBtn = document.getElementById('assigneeAddBtn');
  // 기존 선택된 아바타 제거
  area.querySelectorAll('.assignee-selected').forEach(el => el.remove());

  selectedAssignees.forEach(uid => {
    const m = groupMembers.find(g => g.user_id === uid);
    if (!m) return;
    const el = document.createElement('div');
    el.className = 'assignee-selected';
    el.textContent = avatarInitial(m.nickname);
    el.title = m.nickname;
    area.insertBefore(el, addBtn);
  });
}

function updateDistribution() {
  // 균등 분배 미리보기
  const preview = document.getElementById('distEqualPreview');
  preview.innerHTML = '';
  const count = selectedAssignees.length || 1;
  const perPerson = Math.floor(garlicReward / count);

  selectedAssignees.forEach(uid => {
    const m = groupMembers.find(g => g.user_id === uid);
    if (!m) return;
    preview.innerHTML += `
      <div class="dist-preview-item">
        <div class="dist-preview-avatar">${avatarInitial(m.nickname)}</div>
        <span class="dist-preview-count">${perPerson}개</span>
      </div>`;
  });

  // 직접 조절 슬라이더
  const sliders = document.getElementById('distSliders');
  sliders.innerHTML = '';
  const equalPct = Math.floor(100 / count);

  selectedAssignees.forEach((uid, i) => {
    const m = groupMembers.find(g => g.user_id === uid);
    if (!m) return;
    const pct = customDistMap[uid] ?? equalPct;
    const gc = Math.round(garlicReward * pct / 100);
    sliders.innerHTML += `
      <div class="slider-row">
        <div class="slider-avatar">${avatarInitial(m.nickname)}</div>
        <span class="slider-name">${m.nickname}</span>
        <div class="slider-track" style="--val:${pct}%">
          <input type="range" class="slider-input" min="0" max="100" value="${pct}"
                 data-uid="${uid}" style="--val:${pct}%" />
        </div>
        <span class="slider-percent" data-uid-pct="${uid}">${pct}%</span>
        <span class="slider-count" data-uid-cnt="${uid}">${gc}개</span>
      </div>`;
  });

  // 슬라이더 이벤트
  sliders.querySelectorAll('.slider-input').forEach(input => {
    input.addEventListener('input', onSliderChange);
  });

  recalcDistSummary();
}

function onSliderChange(e) {
  const uid = parseInt(e.target.dataset.uid);
  const val = parseInt(e.target.value);
  customDistMap[uid] = val;
  e.target.style.setProperty('--val', val + '%');
  e.target.closest('.slider-track').style.setProperty('--val', val + '%');

  const pctEl = document.querySelector(`[data-uid-pct="${uid}"]`);
  const cntEl = document.querySelector(`[data-uid-cnt="${uid}"]`);
  if (pctEl) pctEl.textContent = val + '%';
  if (cntEl) cntEl.textContent = Math.round(garlicReward * val / 100) + '개';

  recalcDistSummary();
}

function recalcDistSummary() {
  let sumPct = 0, sumCnt = 0;
  selectedAssignees.forEach(uid => {
    const pct = customDistMap[uid] ?? Math.floor(100 / (selectedAssignees.length || 1));
    sumPct += pct;
    sumCnt += Math.round(garlicReward * pct / 100);
  });
  const sp = document.getElementById('distSumPercent');
  const sc = document.getElementById('distSumCount');
  if (sp) sp.textContent = sumPct;
  if (sc) sc.textContent = sumCnt;
}

// ── 담당자 드롭다운 ──────────────────────────────────
function showAssigneeDropdown() {
  const dd = document.getElementById('assigneeDropdown');
  const btn = document.getElementById('assigneeAddBtn');
  const rect = btn.getBoundingClientRect();
  dd.style.left = `${rect.left}px`;
  dd.style.top = `${rect.bottom + 4}px`;
  dd.classList.remove('hidden');
  renderAssigneeOptions();
}

function hideAssigneeDropdown() {
  document.getElementById('assigneeDropdown').classList.add('hidden');
}

function renderAssigneeOptions() {
  const container = document.getElementById('assigneeOptions');
  const keyword = document.getElementById('assigneeSearch').value.toLowerCase();
  container.innerHTML = '';

  groupMembers.filter(m => m.nickname.toLowerCase().includes(keyword)).forEach(m => {
    const selected = selectedAssignees.includes(m.user_id);
    const el = document.createElement('div');
    el.className = `assignee-opt ${selected ? 'selected' : ''}`;
    el.innerHTML = `
      <div class="assignee-opt-avatar">${avatarInitial(m.nickname)}</div>
      <span class="assignee-opt-name">${m.nickname}</span>
      ${selected ? '<span class="assignee-opt-check">✓</span>' : ''}
    `;
    el.addEventListener('click', () => {
      if (selected) {
        selectedAssignees = selectedAssignees.filter(id => id !== m.user_id);
      } else {
        selectedAssignees.push(m.user_id);
      }
      updateAssigneeDisplay();
      updateDistribution();
      renderAssigneeOptions();
    });
    container.appendChild(el);
  });
}

// ── 할 일 추가/수정 제출 ─────────────────────────────
async function submitTodo() {
  const name = document.getElementById('todoTitleInput').value.trim();
  if (!name) { alert('할 일 제목을 입력해주세요.'); return; }

  const body = {
    todo_name: name,
    description: document.getElementById('todoDescInput').value.trim(),
    deadline: document.getElementById('todoDeadlineInput').value || null,
    garlic_reward: garlicReward,
    priority: selectedPriority,
    category: selectedCategory || null,
  };

  if (editingTodoId) {
    body.status = 'IN_PROGRESS';
    await api(`/todos/${editingTodoId}`, { method: 'PATCH', body: JSON.stringify(body) });
  } else {
    await api(`/todo-groups/${currentGroupId}/todos`, { method: 'POST', body: JSON.stringify(body) });
  }

  // 담당자 지정
  if (selectedAssignees.length > 0 && editingTodoId) {
    await api(`/todos/${editingTodoId}/assignees`, {
      method: 'PATCH',
      body: JSON.stringify({ user_ids: selectedAssignees }),
    });
  }

  closeModal();
  await loadTodos();
}

// ── 할 일 삭제 ───────────────────────────────────────
async function deleteTodo(todoId) {
  if (!confirm('정말 삭제하시겠습니까?')) return;
  await api(`/todos/${todoId}`, { method: 'DELETE' });
  await loadTodos();
}

// ── 이벤트 바인딩 ────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  loadTodos();
  loadMemo();

  // 뒤로가기
  document.getElementById('backBtn').addEventListener('click', () => {
    window.location.href = '/main';
  });

  // 메모 자동저장
  document.getElementById('memoTextarea').addEventListener('input', autoSaveMemo);

  // 할 일 추가 버튼
  document.getElementById('addTodoBtn').addEventListener('click', () => openModal('add'));

  // 모달 닫기
  document.getElementById('modalCloseBtn').addEventListener('click', closeModal);
  document.getElementById('modalCancelBtn').addEventListener('click', closeModal);
  document.getElementById('todoModal').addEventListener('click', (e) => {
    if (e.target.id === 'todoModal') closeModal();
  });

  // 모달 제출
  document.getElementById('modalSubmitBtn').addEventListener('click', submitTodo);

  // 글자수
  document.getElementById('todoTitleInput').addEventListener('input', updateCharCounts);
  document.getElementById('todoDescInput').addEventListener('input', updateCharCounts);

  // 카테고리 칩
  document.getElementById('categoryChips').addEventListener('click', (e) => {
    const chip = e.target.closest('.chip:not(.chip-add)');
    if (chip) {
      selectedCategory = selectedCategory === chip.dataset.cat ? '' : chip.dataset.cat;
      updateCategoryChips();
    }
  });

  // 우선순위
  document.getElementById('prioritySelect').addEventListener('click', () => {
    document.getElementById('priorityDropdown').classList.toggle('hidden');
  });
  document.querySelectorAll('.priority-option').forEach(btn => {
    btn.addEventListener('click', () => {
      selectedPriority = btn.dataset.priority;
      updatePriorityDisplay();
      document.getElementById('priorityDropdown').classList.add('hidden');
    });
  });

  // 마늘 보상 +/-
  document.getElementById('garlicMinus').addEventListener('click', () => {
    if (garlicReward > 1) { garlicReward--; updateGarlicDisplay(); updateDistribution(); }
  });
  document.getElementById('garlicPlus').addEventListener('click', () => {
    garlicReward++; updateGarlicDisplay(); updateDistribution();
  });

  // 날짜 placeholder
  document.getElementById('todoDeadlineInput').addEventListener('change', updateDatePlaceholder);

  // 분배 탭
  document.getElementById('distEqualTab').addEventListener('click', () => {
    distMode = 'equal';
    document.getElementById('distEqualTab').classList.add('active');
    document.getElementById('distCustomTab').classList.remove('active');
    document.getElementById('distEqualView').classList.remove('hidden');
    document.getElementById('distCustomView').classList.add('hidden');
  });
  document.getElementById('distCustomTab').addEventListener('click', () => {
    distMode = 'custom';
    document.getElementById('distCustomTab').classList.add('active');
    document.getElementById('distEqualTab').classList.remove('active');
    document.getElementById('distCustomView').classList.remove('hidden');
    document.getElementById('distEqualView').classList.add('hidden');
  });

  // 담당자 추가
  document.getElementById('assigneeAddBtn').addEventListener('click', (e) => {
    e.stopPropagation();
    showAssigneeDropdown();
  });
  document.getElementById('assigneeSearch').addEventListener('input', renderAssigneeOptions);

  // 컨텍스트 메뉴 - 수정/삭제
  document.getElementById('todoList').addEventListener('click', (e) => {
    const moreBtn = e.target.closest('.todo-more-btn');
    if (moreBtn) {
      showContextMenu(e, moreBtn.dataset.todoId);
    }
  });

  document.getElementById('contextEdit').addEventListener('click', async () => {
    const todoId = document.getElementById('contextMenu').dataset.todoId;
    hideContextMenu();
    // 상세 조회 후 모달 열기
    let todoData = await api(`/todos/${todoId}`);
    if (!todoData) {
      todoData = todos.find(t => t.todo_id == todoId) || {};
    }
    openModal('edit', todoData);
  });

  document.getElementById('contextDelete').addEventListener('click', () => {
    const todoId = document.getElementById('contextMenu').dataset.todoId;
    hideContextMenu();
    deleteTodo(todoId);
  });

  // 전역 클릭 → 메뉴/드롭다운 닫기
  document.addEventListener('click', (e) => {
    if (!e.target.closest('.context-menu') && !e.target.closest('.todo-more-btn')) {
      hideContextMenu();
    }
    if (!e.target.closest('.assignee-dropdown') && !e.target.closest('.assignee-add-btn')) {
      hideAssigneeDropdown();
    }
    if (!e.target.closest('.priority-select') && !e.target.closest('.priority-dropdown')) {
      document.getElementById('priorityDropdown')?.classList.add('hidden');
    }
  });
});
