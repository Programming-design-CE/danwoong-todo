/* =========================================================
   group-todo.js  –  프로젝트(공동) 할 일 프론트엔드 로직
   ========================================================= */

// ── 상수 / 상태 ──────────────────────────────────────
const API_BASE = '';
const urlParams = new URLSearchParams(window.location.search);
let currentGroupId = Number(urlParams.get('groupId') || localStorage.getItem('currentGroupId') || 1);          // 기본 그룹 (동적 로드)
let todos = [];
let editingTodoId = null;        // null → 추가, 숫자 → 수정
let selectedAssignees = [];
let selectedCategory = '';
let selectedPriority = 'MEDIUM';
let garlicReward = 10;
let distMode = 'equal';          // equal | custom
let customDistMap = {};          // { userId: percent }

// 더미 데이터 및 멤버 상태 (프레젠테이션용)
let friendList = [
  { user_id: 2, nickname: 'lion_jiho', avatar: '🦁' },
  { user_id: 3, nickname: 'coding_mj', avatar: '🐱' },
  { user_id: 4, nickname: 'dev_kim', avatar: '🦊' },
  { user_id: 5, nickname: 'bear_js', avatar: '🐻' },
  { user_id: 6, nickname: 'fox_seo', avatar: '🦊' },
  { user_id: 7, nickname: 'rabbit_lee', avatar: '🐰' },
  { user_id: 8, nickname: 'uni_yoon', avatar: '🦄' },
];

let currentProjectMembers = [
  { user_id: 1, nickname: '나 (you)', avatar: '🐻', isMe: true },
  { user_id: 2, nickname: 'lion_jiho', avatar: '🦁', isLeader: true },
  { user_id: 3, nickname: 'coding_mj', avatar: '🐱' },
  { user_id: 4, nickname: 'dev_kim', avatar: '🦊' },
  { user_id: 5, nickname: 'bear_js', avatar: '🐻' },
];

// 할일 추가 팝업의 담당자 맵핑을 위해 groupMembers 가리키게 함
const groupMembers = currentProjectMembers;

let tempProjectMembers = [];


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
  if (data && data.content) ta.textContent = data.content;
}

let memoTimer = null;
function autoSaveMemo() {
  clearTimeout(memoTimer);
  memoTimer = setTimeout(async () => {
    const content = document.getElementById('memoTextarea').textContent;
    await api(`/todo-groups/${currentGroupId}/note`, {
      method: 'PUT',
      body: JSON.stringify({ content }),
    });
    const now = new Date();
    const footer = document.getElementById('memoFooter');
    if (footer) {
      footer.textContent = `자동 저장됨 · 오늘 ${now.getHours()}:${String(now.getMinutes()).padStart(2, '0')}`;
    }
  }, 800);
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

  const assigneesList = selectedAssignees.map(uid => {
    const m = groupMembers.find(g => g.user_id === uid);
    return m ? { user_id: m.user_id, nickname: m.nickname } : { user_id: uid, nickname: '멤버' };
  });

  let apiSuccess = false;

  if (editingTodoId) {
    body.status = 'IN_PROGRESS';
    const result = await api(`/todos/${editingTodoId}`, { method: 'PATCH', body: JSON.stringify(body) });
    apiSuccess = !!result;

    // 담당자 지정
    if (selectedAssignees.length > 0) {
      await api(`/todos/${editingTodoId}/assignees`, {
        method: 'PATCH',
        body: JSON.stringify({ user_ids: selectedAssignees }),
      });
    }

    // API 실패 시 로컬 데이터 수정 (프레젠테이션용)
    if (!apiSuccess) {
      const idx = todos.findIndex(t => t.todo_id == editingTodoId);
      if (idx !== -1) {
        todos[idx] = {
          ...todos[idx],
          todo_name: body.todo_name,
          description: body.description,
          deadline: body.deadline,
          garlic_reward: body.garlic_reward,
          priority: body.priority,
          category: body.category,
          assignees: assigneesList.length > 0 ? assigneesList : todos[idx].assignees,
        };
      }
    }
  } else {
    const result = await api(`/todo-groups/${currentGroupId}/todos`, { method: 'POST', body: JSON.stringify(body) });
    apiSuccess = !!result;

    // API 실패 시 로컬 데이터에 추가 (프레젠테이션용)
    if (!apiSuccess) {
      const newId = todos.length > 0 ? Math.max(...todos.map(t => t.todo_id)) + 1 : 1;
      todos.push({
        todo_id: newId,
        todo_name: body.todo_name,
        description: body.description,
        category: body.category || '기타',
        deadline: body.deadline,
        priority: body.priority,
        status: 'IN_PROGRESS',
        garlic_reward: body.garlic_reward,
        progress: 0,
        assignees: assigneesList,
      });
    }
  }

  closeModal();

  // API 성공 시 서버에서 다시 로드, 실패 시 로컬 데이터로 렌더링
  if (apiSuccess) {
    await loadTodos();
  } else {
    renderTodoList();
  }
}

// ── 할 일 삭제 ───────────────────────────────────────
async function deleteTodo(todoId) {
  if (!confirm('정말 삭제하시겠습니까?')) return;
  const result = await api(`/todos/${todoId}`, { method: 'DELETE' });

  // API 실패 시 로컬 데이터에서 제거 (프레젠테이션용)
  if (result === null) {
    todos = todos.filter(t => t.todo_id != todoId);
    renderTodoList();
  } else {
    await loadTodos();
  }
}

// ── 이벤트 바인딩 ────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
  loadTodos();

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

  // 멤버 관리 모달 열기
  const openMemberBtn = document.getElementById('openMemberModal');
  if (openMemberBtn) openMemberBtn.addEventListener('click', openMemberModal);

  // 멤버 관리 모달 닫기
  const closeMemberBtn = document.getElementById('memberModalClose');
  if (closeMemberBtn) closeMemberBtn.addEventListener('click', closeMemberModal);
  const cancelMemberBtn = document.getElementById('memberModalCancel');
  if (cancelMemberBtn) cancelMemberBtn.addEventListener('click', closeMemberModal);

  const memberModalOverlay = document.getElementById('memberModal');
  if (memberModalOverlay) {
    memberModalOverlay.addEventListener('click', (e) => {
      if (e.target.id === 'memberModal') closeMemberModal();
    });
  }

  // 모두 제거 버튼
  const removeAllBtn = document.getElementById('memberRemoveAll');
  if (removeAllBtn) removeAllBtn.addEventListener('click', removeAllProjectMembers);

  // 친구 검색
  const searchInput = document.getElementById('memberSearchInput');
  if (searchInput) searchInput.addEventListener('input', renderMemberModal);

  // 확인 버튼
  const confirmMemberBtn = document.getElementById('memberModalConfirm');
  if (confirmMemberBtn) confirmMemberBtn.addEventListener('click', confirmMemberChanges);

  // 초기 그룹 멤버 아바타 그리기
  renderGroupMemberAvatars();
});

// ── 멤버 관리 모달 ───────────────────────────────────
function openMemberModal() {
  tempProjectMembers = [...currentProjectMembers];
  document.getElementById('memberModal').classList.add('show');
  renderMemberModal();
}

function closeMemberModal() {
  document.getElementById('memberModal').classList.remove('show');
}

function renderMemberModal() {
  const friendListArea = document.getElementById('friendListArea');
  const projectMemberListArea = document.getElementById('projectMemberListArea');
  const keyword = document.getElementById('memberSearchInput').value.toLowerCase();

  // 1. 친구 목록 렌더링
  friendListArea.innerHTML = '';
  const filteredFriends = friendList.filter(f => f.nickname.toLowerCase().includes(keyword));
  document.getElementById('friendCountLabel').textContent = `내 친구 (${friendList.length})`;

  filteredFriends.forEach(f => {
    const isAdded = tempProjectMembers.some(m => m.user_id === f.user_id);
    const row = document.createElement('div');
    row.className = 'member-row';
    row.innerHTML = `
      <div class="member-row-avatar">${f.avatar || '👤'}</div>
      <span class="member-row-name">${f.nickname}</span>
      <button class="member-row-action ${isAdded ? 'check' : 'add'}">${isAdded ? '✓' : '+'}</button>
    `;
    
    row.addEventListener('click', () => {
      if (isAdded) {
        tempProjectMembers = tempProjectMembers.filter(m => m.user_id !== f.user_id);
      } else {
        if (tempProjectMembers.length >= 10) {
          alert('프로젝트 멤버는 최대 10명까지 설정할 수 있습니다.');
          return;
        }
        tempProjectMembers.push({
          user_id: f.user_id,
          nickname: f.nickname,
          avatar: f.avatar
        });
      }
      renderMemberModal();
    });
    friendListArea.appendChild(row);
  });

  // 2. 프로젝트 멤버 렌더링
  projectMemberListArea.innerHTML = '';
  document.getElementById('projectMemberCountLabel').textContent = `프로젝트 멤버 (${tempProjectMembers.length}/10)`;
  document.getElementById('memberConfirmCount').textContent = tempProjectMembers.length;

  tempProjectMembers.forEach(m => {
    const row = document.createElement('div');
    row.className = 'member-row';
    
    let badgeHtml = '';
    if (m.isMe) {
      badgeHtml = `<span class="member-row-badge me">나</span>`;
    } else if (m.isLeader) {
      badgeHtml = `<span class="member-row-badge leader"><span style="color:#f5a623;">👑</span> 팀장</span>`;
    }

    let actionButtonHtml = '';
    if (!m.isMe) {
      actionButtonHtml = `<button class="member-row-action remove" data-uid="${m.user_id}">✕</button>`;
    } else {
      actionButtonHtml = `<span style="font-size:0.58vw;color:#8a8178;margin-right:0.3vw;">나</span>`;
    }

    row.innerHTML = `
      <div class="member-row-avatar ${m.isMe ? 'me' : ''}">${m.avatar || '👤'}</div>
      <span class="member-row-name">${m.nickname}</span>
      ${badgeHtml}
      ${actionButtonHtml}
    `;

    const removeBtn = row.querySelector('.member-row-action.remove');
    if (removeBtn) {
      removeBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        tempProjectMembers = tempProjectMembers.filter(pm => pm.user_id !== m.user_id);
        renderMemberModal();
      });
    }

    projectMemberListArea.appendChild(row);
  });
}

function confirmMemberChanges() {
  currentProjectMembers.length = 0;
  tempProjectMembers.forEach(m => currentProjectMembers.push(m));
  closeMemberModal();
  renderGroupMemberAvatars();
  alert('프로젝트 멤버가 수정되었습니다.');
}

function removeAllProjectMembers() {
  tempProjectMembers = tempProjectMembers.filter(m => m.isMe);
  renderMemberModal();
}

function renderGroupMemberAvatars() {
  const container = document.getElementById('groupMemberAvatars');
  if (!container) return;
  container.innerHTML = '';
  
  currentProjectMembers.slice(0, 4).forEach(m => {
    const mini = document.createElement('div');
    mini.className = 'group-member-mini';
    mini.textContent = m.nickname[0];
    mini.title = m.nickname;
    container.appendChild(mini);
  });

  if (currentProjectMembers.length > 4) {
    const more = document.createElement('div');
    more.className = 'group-member-mini';
    more.textContent = `+${currentProjectMembers.length - 4}`;
    container.appendChild(more);
  }
}

