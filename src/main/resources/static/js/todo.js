const TOKEN = localStorage.getItem('accessToken') || '';
const AUTH = { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + TOKEN };

// ── 프로젝트 목록 ─────────────────────────
async function loadProjects() {
    try {
        const res = await fetch('/todo-groups', { headers: AUTH });
        const data = await res.json();
        allGroups = data.groups || [];
        document.getElementById('progressCount').textContent = allGroups.length;
        renderProjects(allGroups);
    } catch(e) { console.error(e); }
}

function renderProjects(groups) {
    const list = document.getElementById('projectList');
    if (!groups.length) {
        list.innerHTML = `
            <div style="text-align:center;color:#444;padding:40px 0;font-size:13px;">진행중인 프로젝트가 없습니다.</div>
            <button class="btn-add-project" id="btnAddProject">+ 프로젝트 추가하기</button>`;
        document.getElementById('btnAddProject').addEventListener('click', () => {
            loadFriends();
            document.getElementById('modalOverlay').classList.add('open');
        });
        return;
    }
    const AUTO_COLORS = ['#b87ec8','#4CAF7D','#D4A843','#E07B54','#7B9EC8','#C85C5C','#7BC67E','#5B8AC8'];
    const AUTO_ICONS  = ['♥','⭐','📋','🎓','📚','👤','🎯','💼'];
    list.innerHTML = groups.map((g, idx) => {
        const total = g.total_garlic_reward || 0;
        const rem = g.remaining_garlic_reward || 0;
        const pct = total > 0 ? Math.round((total - rem) / total * 100) : 0;
        const dd = g.deadline ? dday(g.deadline) : '';
        const members = (g.members || []).slice(0,3).map(m =>
            m.character_thumbnail_url
                ? `<div class="member-avatar"><img src="${m.character_thumbnail_url}" alt="${m.nickname}"></div>`
                : `<div class="member-avatar">${m.nickname ? m.nickname[0] : '?'}</div>`
        ).join('');
        const more = g.member_count > 3 ? `<div class="member-more">+${g.member_count-3}</div>` : '';
        let icon = AUTO_ICONS[idx % AUTO_ICONS.length];
        let color = AUTO_COLORS[idx % AUTO_COLORS.length];
        try {
            const iconStore = JSON.parse(localStorage.getItem('groupIcons') || '{}');
            if (g.group_icon_url) {
                const iconData = JSON.parse(g.group_icon_url);
                if (iconData.icon)  icon  = iconData.icon;
                if (iconData.color) color = iconData.color;
            } else if (iconStore[g.group_name]) {
                icon  = iconStore[g.group_name].icon  || icon;
                color = iconStore[g.group_name].color || color;
            }
        } catch(e) {}
        const pri = g.priority === 'HIGH' ? '높음' : g.priority === 'LOW' ? '낮음' : '보통';
        const barColor = pct >= 70 ? '#6aad8e' : pct >= 40 ? '#aac87a' : '#b8a07a';
        return `
        <div class="project-card">
            <div class="project-icon" style="background:${color}22;color:${color}">${icon}</div>
            <div class="project-info">
                <div class="project-top">
                    <span class="project-name">${g.group_name}</span>
                    ${dd ? `<span class="project-dday">${dd}</span>` : ''}
                </div>
                <div class="project-bottom">
                    <div class="progress-bar"><div class="progress-fill" style="width:${pct}%;background:${barColor}"></div></div>
                    <span class="project-percent">${pct}%</span>
                    <div class="member-avatars">${members}${more}</div>
                    <span class="project-priority">🚩 ${pri}</span>
                </div>
            </div>
            <button class="project-more" onclick="event.stopPropagation()">⋮</button>
        </div>`;
    }).join('') + `
    <button class="btn-add-project" id="btnAddProject">+ 프로젝트 추가하기</button>`;
    // 버튼 이벤트 재바인딩
    document.getElementById('btnAddProject').addEventListener('click', () => {
        loadFriends();
        document.getElementById('modalOverlay').classList.add('open');
    });
}

function dday(deadline) {
    const diff = Math.ceil((new Date(deadline) - new Date()) / 86400000);
    return diff >= 0 ? 'D-' + diff : 'D+' + Math.abs(diff);
}

// ── 메모장 ───────────────────────────────
async function loadMemo() {
    try {
        const res = await fetch('/todos/my/note', { headers: AUTH });
        const data = await res.json();
        const el = document.getElementById('memoTextarea');
        if (data.content) el.textContent = data.content;
    } catch(e) {}
}

function updatePlaceholder() {}

let memoTimer;
document.getElementById('memoTextarea').addEventListener('input', () => {
    clearTimeout(memoTimer);
    memoTimer = setTimeout(saveMemo, 1000);
});

async function saveMemo() {
    const content = document.getElementById('memoTextarea').textContent;
    try {
        await fetch('/todos/my/note', { method:'PUT', headers:AUTH, body:JSON.stringify({content}) });
        const now = new Date();
        const t = now.getHours().toString().padStart(2,'0') + ':' + now.getMinutes().toString().padStart(2,'0');
        document.getElementById('memoFooter').textContent = '자동 저장됨 · 오늘 ' + t;
    } catch(e) {}
}

// 메모장 드롭다운
document.getElementById('memoHeaderBtn').addEventListener('click', () => {
    document.getElementById('memoDropdown').classList.toggle('open');
});

// ── 탭 ──────────────────────────────────
let allGroups = [];

document.querySelectorAll('.todo-tab').forEach(tab => {
    tab.addEventListener('click', () => {
        document.querySelectorAll('.todo-tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        const tabName = tab.dataset.tab;
        // 진행중 탭만 프로젝트 목록 표시
        const list = document.getElementById('projectList');
        const addBtn = document.querySelector('.add-project-wrap');
        if (tabName === 'progress') {
            renderProjects(allGroups);
            addBtn.style.display = '';
        } else {
            list.innerHTML = '<div style="text-align:center;color:#444;padding:40px 0;font-size:13px;">' + tab.textContent.trim() + ' 준비 중입니다.</div>';
            addBtn.style.display = 'none';
        }
    });
});

// ── 모달 ────────────────────────────────
['modalClose','btnCancel'].forEach(id => {
    document.getElementById(id).addEventListener('click', closeModal);
});
document.getElementById('modalOverlay').addEventListener('click', e => {
    if (e.target === e.currentTarget) closeModal();
});

function closeModal() {
    document.getElementById('modalOverlay').classList.remove('open');
    document.getElementById('groupName').value = '';
    document.getElementById('groupDesc').value = '';
    document.getElementById('groupDeadline').value = '';
    document.getElementById('groupGarlic').value = '';
    document.getElementById('nameCount').textContent = '0';
    document.getElementById('descCount').textContent = '0';
    selectedAssignees = [];
    selectedColor = '#4CAF7D';
    document.querySelectorAll('.color-dot').forEach(d => { d.classList.remove('active'); d.textContent = ''; });
    document.querySelector('.color-dot[data-color="#4CAF7D"]').classList.add('active');
    document.querySelector('.color-dot[data-color="#4CAF7D"]').textContent = '✓';
    renderAssignees();
}

// 글자 수
document.getElementById('groupName').addEventListener('input', e => {
    document.getElementById('nameCount').textContent = e.target.value.length;
});
document.getElementById('groupDesc').addEventListener('input', e => {
    document.getElementById('descCount').textContent = e.target.value.length;
});

// 카테고리
const CATEGORY_ICONS = { '학교':'🎓', '대외활동':'📋', '스터디':'📚', '개인':'👤', '기타':'···' };
let selectedCategory = '학교';
let selectedIcon = '🎓';

document.querySelectorAll('.category-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.category-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        selectedCategory = btn.dataset.val;
        selectedIcon = CATEGORY_ICONS[selectedCategory] || '♥';
    });
});

// 색상 — 선택 시 아이콘 미리보기 색상도 변경
let selectedColor = '#4CAF7D';
document.querySelectorAll('.color-dot').forEach(dot => {
    dot.addEventListener('click', () => {
        document.querySelectorAll('.color-dot').forEach(d => { d.classList.remove('active'); d.textContent = ''; });
        dot.classList.add('active');
        dot.textContent = '✓';
        selectedColor = dot.dataset.color;
    });
});

// 아이콘
document.querySelectorAll('.icon-btn').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.icon-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
    });
});

// ── 담당자 ───────────────────────────────
let friends = [];
let selectedAssignees = [];

async function loadFriends() {
    try {
        const res = await fetch('/friends', { headers: AUTH });
        friends = await res.json();
        renderFriendPicker();
    } catch(e) { friends = []; }
}

function renderFriendPicker() {
    const picker = document.getElementById('friendPicker');
    if (!friends.length) {
        picker.innerHTML = '<div class="friend-item" style="color:#555">친구가 없습니다.</div>';
        return;
    }
    picker.innerHTML = friends.map(f => `
        <div class="friend-item" onclick="addAssignee(${f.user_id}, '${f.nickname}', '${f.character_thumbnail_url || ''}')">
            <div class="friend-avatar">
                ${f.character_thumbnail_url
                    ? `<img src="${f.character_thumbnail_url}" alt="${f.nickname}">`
                    : f.nickname[0]}
            </div>
            ${f.nickname}
        </div>`).join('');
}

function addAssignee(id, nickname, thumbnail) {
    if (selectedAssignees.find(a => a.id === id)) return;
    selectedAssignees.push({ id, nickname, thumbnail });
    renderAssignees();
    document.getElementById('friendPicker').classList.remove('open');
}

function removeAssignee(id) {
    selectedAssignees = selectedAssignees.filter(a => a.id !== id);
    renderAssignees();
}

function renderAssignees() {
    const wrap = document.getElementById('assigneeWrap');
    const chips = selectedAssignees.map(a => `
        <div class="assignee-chip" onclick="removeAssignee(${a.id})">
            ${a.thumbnail ? `<img src="${a.thumbnail}" alt="${a.nickname}">` : `<span class="chip-initial">${a.nickname[0]}</span>`}
            <div class="chip-remove">✕</div>
        </div>`).join('');
    wrap.innerHTML = chips + `<button class="btn-add-assignee" id="btnAddAssignee" onclick="toggleFriendPicker(event)">+</button>`;
}

function toggleFriendPicker(e) {
    e.stopPropagation();
    document.getElementById('friendPicker').classList.toggle('open');
}

document.addEventListener('click', () => {
    document.getElementById('friendPicker').classList.remove('open');
});

// ── 제출 ────────────────────────────────
document.getElementById('btnSubmit').addEventListener('click', async () => {
    const groupName = document.getElementById('groupName').value.trim();
    if (!groupName) { alert('할 일 제목을 입력해주세요.'); return; }

    const body = {
        group_name: groupName,
        deadline: document.getElementById('groupDeadline').value || null,
        priority: document.getElementById('groupPriority').value,
        invitee_ids: selectedAssignees.map(a => a.id),
        group_icon_url: JSON.stringify({ icon: selectedIcon, color: selectedColor }),
    };

    try {
        const res = await fetch('/todo-groups', { method:'POST', headers:AUTH, body:JSON.stringify(body) });
        const text = await res.text();
        if (res.ok) {
            // 아이콘/색상 로컬에 저장 (백엔드 응답에 없어서)
            const resData = JSON.parse(text);
            const groupId = resData.data?.group_id || resData.group_id || groupName;
            const iconStore = JSON.parse(localStorage.getItem('groupIcons') || '{}');
            iconStore[groupName] = { icon: selectedIcon, color: selectedColor };
            localStorage.setItem('groupIcons', JSON.stringify(iconStore));
            closeModal();
            loadProjects();
        } else {
            console.error('실패:', res.status, text);
            alert('프로젝트 추가에 실패했습니다. (status: ' + res.status + ')');
        }
    } catch(e) {
        console.error(e);
        alert('네트워크 오류가 발생했습니다.');
    }
});

// ── 초기화 ──────────────────────────────
loadProjects();
loadMemo();
