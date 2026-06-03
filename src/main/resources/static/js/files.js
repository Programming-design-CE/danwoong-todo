let currentGroupId = null;
let currentProject = null;
let rootFolderId = null;
let currentFolderId = null;
let currentItems = [];
let folderTrail = [];
let projectItems = [];
let viewMode = "PROJECTS";

document.addEventListener("DOMContentLoaded", () => {
    initFilesPage();
    bindFilesEvents();
});

function getAccessToken() {
    return localStorage.getItem("accessToken");
}

function setCurrentGroupId(groupId) {
    if (groupId == null) {
        localStorage.removeItem("currentGroupId");
        return;
    }

    localStorage.setItem("currentGroupId", String(groupId));
}

async function requestAuthApi(url, options = {}) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        alert("로그인이 필요합니다.");
        window.location.href = "/login";
        return null;
    }

    const isFormData = options.body instanceof FormData;
    const headers = {
        Authorization: `Bearer ${accessToken}`,
        ...(options.headers || {})
    };

    if (!isFormData) {
        headers["Content-Type"] = "application/json";
    }

    const response = await fetch(url, {
        ...options,
        headers
    });

    if (response.status === 401) {
        alert("로그인이 만료되었습니다. 다시 로그인해주세요.");
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        window.location.href = "/login";
        return null;
    }

    if (response.status === 204) {
        return null;
    }

    const text = await response.text();
    const parsed = parseResponseText(text);

    if (!response.ok) {
        const errorMessage = parsed?.message
            || (typeof parsed === "string" && parsed)
            || `API 요청이 실패했습니다. (${response.status})`;

        console.error("API 실패 상태코드:", response.status);
        console.error("API 실패 응답:", parsed || text);
        console.error("요청 URL:", url);
        console.error("요청 options:", options);

        throw new Error(errorMessage);
    }

    return parsed;
}

function parseResponseText(text) {
    if (!text) {
        return null;
    }

    try {
        return JSON.parse(text);
    } catch (error) {
        return text;
    }
}

async function initFilesPage() {
    await loadProjectRoot();
}

function bindFilesEvents() {
    const createFolderBtn = document.getElementById("createFolderBtn");
    const uploadFileBtn = document.getElementById("uploadFileBtn");
    const fileInput = document.getElementById("fileInput");
    const fileTypeFilter = document.getElementById("fileTypeFilter");
    const sortOption = document.getElementById("sortOption");
    const backButton = document.getElementById("folderBackBtn");

    if (!createFolderBtn || !uploadFileBtn || !fileInput || !fileTypeFilter || !sortOption || !backButton) {
        console.warn("파일 페이지 요소를 찾지 못했습니다.");
        return;
    }

    createFolderBtn.addEventListener("click", addFolderInputRow);
    uploadFileBtn.addEventListener("click", () => fileInput.click());
    backButton.addEventListener("click", () => goBackFolder());

    fileInput.addEventListener("change", (event) => {
        const file = event.target.files?.[0];
        if (file) {
            uploadFile(file);
        }
    });

    fileTypeFilter.addEventListener("change", () => {
        renderItems(viewMode === "PROJECTS" ? projectItems : currentItems);
    });

    sortOption.addEventListener("change", () => {
        renderItems(viewMode === "PROJECTS" ? projectItems : currentItems);
    });
}

async function loadProjectRoot() {
    viewMode = "PROJECTS";
    currentGroupId = null;
    currentProject = null;
    rootFolderId = null;
    currentFolderId = null;
    currentItems = [];
    folderTrail = [];

    updateActionState();
    renderFolderNavigator();

    try {
        const response = await requestAuthApi("/todo-groups");
        if (!response) {
            return;
        }

        const groups = getGroupsFromResponse(response);
        projectItems = await Promise.all(groups.map((group) => buildProjectItem(group)));
        renderItems(projectItems);
    } catch (error) {
        console.error(error);
        showEmpty(error.message || "프로젝트 목록을 불러오지 못했습니다.");
    }
}

async function openProject(projectItem) {
    const groupId = getProjectGroupId(projectItem);
    if (!groupId) {
        alert("프로젝트 정보를 찾을 수 없습니다.");
        return;
    }

    currentProject = projectItem;
    currentGroupId = groupId;
    setCurrentGroupId(groupId);

    await loadRootFolder();
}

async function loadRootFolder() {
    if (!currentGroupId) {
        await loadProjectRoot();
        return;
    }

    try {
        const response = await requestAuthApi(`/todo-groups/${currentGroupId}/folders/root`);
        if (!response) {
            return;
        }

        const rootFolder = getFolderFromResponse(response);

        rootFolderId = rootFolder?.folderId || rootFolder?.folder_id || rootFolder?.id || null;
        currentFolderId = rootFolderId;
        viewMode = "FOLDER";
        folderTrail = rootFolder ? [toFolderNode(rootFolder)] : [];

        updateActionState();
        renderFolderNavigator();

        if (!rootFolderId) {
            showEmpty("프로젝트 루트 폴더를 찾을 수 없습니다.");
            return;
        }

        await loadFolderItems(rootFolderId);
    } catch (error) {
        console.error(error);
        showEmpty(error.message || "프로젝트 폴더를 불러오지 못했습니다.");
    }
}

async function loadFolderItems(folderId) {
    if (!currentGroupId || !folderId) {
        showEmpty("폴더 정보를 불러올 수 없습니다.");
        return;
    }

    try {
        const response = await requestAuthApi(`/todo-groups/${currentGroupId}/folders/${folderId}/items`);
        if (!response) {
            return;
        }

        viewMode = "FOLDER";
        currentFolderId = folderId;
        currentItems = getItemsFromResponse(response);

        const currentFolder = getCurrentFolderFromResponse(response);
        syncFolderTrail(currentFolder);
        updateActionState();
        renderFolderNavigator();
        renderItems(currentItems);
    } catch (error) {
        console.error(error);
        showEmpty(error.message || "파일 목록을 불러오지 못했습니다.");
    }
}

function syncFolderTrail(currentFolder) {
    if (!currentFolder) {
        return;
    }

    const node = toFolderNode(currentFolder);
    const existingIndex = folderTrail.findIndex((item) => item.id === node.id);

    if (existingIndex >= 0) {
        folderTrail = folderTrail.slice(0, existingIndex + 1);
        folderTrail[existingIndex] = node;
        return;
    }

    if (node.parentFolderId == null || node.id === rootFolderId) {
        folderTrail = [node];
        return;
    }

    folderTrail = [...folderTrail, node];
}

function renderFolderNavigator() {
    const backButton = document.getElementById("folderBackBtn");
    const breadcrumb = document.getElementById("folderBreadcrumb");
    const currentLabel = document.getElementById("currentFolderLabel");

    if (!backButton || !breadcrumb || !currentLabel) {
        return;
    }

    breadcrumb.innerHTML = "";

    if (viewMode === "PROJECTS") {
        backButton.disabled = true;
        currentLabel.textContent = "프로젝트 자료함";
        appendCrumb(breadcrumb, "전체 프로젝트", true, null);
        return;
    }

    const canGoBack = folderTrail.length > 0;
    backButton.disabled = !canGoBack;

    const currentFolder = folderTrail[folderTrail.length - 1];
    currentLabel.textContent = currentFolder?.name || currentProject?.name || "프로젝트 폴더";

    appendCrumb(breadcrumb, "전체 프로젝트", false, () => loadProjectRoot());

    folderTrail.forEach((folder, index) => {
        appendSeparator(breadcrumb);

        const isLast = index === folderTrail.length - 1;
        appendCrumb(
            breadcrumb,
            folder.name,
            isLast,
            isLast ? null : () => {
                folderTrail = folderTrail.slice(0, index + 1);
                loadFolderItems(folder.id);
            }
        );
    });
}

function appendCrumb(container, label, disabled, onClick) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "folder-crumb";
    button.textContent = label;
    button.disabled = disabled;

    if (!disabled && typeof onClick === "function") {
        button.addEventListener("click", onClick);
    }

    container.appendChild(button);
}

function appendSeparator(container) {
    const separator = document.createElement("span");
    separator.className = "folder-crumb-separator";
    separator.textContent = "/";
    container.appendChild(separator);
}

function goBackFolder() {
    if (viewMode !== "FOLDER") {
        return;
    }

    if (folderTrail.length <= 1) {
        loadProjectRoot();
        return;
    }

    folderTrail = folderTrail.slice(0, -1);
    const previousFolder = folderTrail[folderTrail.length - 1];

    if (previousFolder) {
        loadFolderItems(previousFolder.id);
    }
}

function updateActionState() {
    const createFolderBtn = document.getElementById("createFolderBtn");
    const uploadFileBtn = document.getElementById("uploadFileBtn");
    const fileInput = document.getElementById("fileInput");
    const isInsideProject = viewMode === "FOLDER" && Boolean(currentGroupId) && Boolean(currentFolderId);

    if (createFolderBtn) {
        createFolderBtn.disabled = !isInsideProject;
        createFolderBtn.title = isInsideProject ? "" : "프로젝트 폴더 안에서만 새 폴더를 만들 수 있습니다.";
    }

    if (uploadFileBtn) {
        uploadFileBtn.disabled = !isInsideProject;
        uploadFileBtn.title = isInsideProject ? "" : "프로젝트 폴더 안에서만 파일을 업로드할 수 있습니다.";
    }

    if (fileInput) {
        fileInput.disabled = !isInsideProject;
    }
}

function renderItems(items) {
    const table = document.getElementById("fileTable");
    const tbody = document.getElementById("fileTableBody");
    const empty = document.getElementById("fileEmpty");
    const typeFilter = document.getElementById("fileTypeFilter");
    const sortOption = document.getElementById("sortOption");

    if (!table || !tbody || !empty || !typeFilter || !sortOption) {
        return;
    }

    tbody.innerHTML = "";

    let result = [...items];
    result = result.filter((item) => matchesFilter(item, typeFilter.value));

    if (sortOption.value === "NAME") {
        result.sort((a, b) => getItemName(a).localeCompare(getItemName(b), "ko"));
    } else {
        result.sort((a, b) => {
            const aTime = new Date(getItemDate(a)).getTime() || 0;
            const bTime = new Date(getItemDate(b)).getTime() || 0;
            return bTime - aTime;
        });
    }

    if (result.length === 0) {
        table.style.display = "none";
        empty.style.display = "flex";
        empty.innerHTML = getEmptyMarkup(items.length === 0);
        return;
    }

    table.style.display = "table";
    empty.style.display = "none";

    result.forEach((item) => {
        const kind = getItemKind(item);
        const id = getItemId(item);
        const name = getItemName(item);
        const date = formatDate(getItemDate(item));
        const size = getItemSize(item);
        const fileUrl = item.fileUrl || item.file_url || null;
        const canDelete = kind === "FILE" || kind === "FOLDER";

        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>
                <button class="file-name-btn" type="button">
                    <span class="file-icon">${kind === "FILE" ? "📄" : "📁"}</span>
                    <span>${escapeHtml(name)}</span>
                </button>
            </td>
            <td>${escapeHtml(date)}</td>
            <td>${escapeHtml(size)}</td>
            <td>
                ${canDelete ? '<button class="file-more-btn" type="button">⋯</button>' : '<span class="file-more-placeholder"></span>'}
            </td>
        `;

        const nameButton = tr.querySelector(".file-name-btn");
        const moreButton = tr.querySelector(".file-more-btn");

        nameButton?.addEventListener("click", () => {
            if (kind === "PROJECT") {
                openProject(item);
                return;
            }

            if (!id) {
                alert("항목 ID를 찾을 수 없습니다.");
                return;
            }

            if (kind === "FOLDER") {
                loadFolderItems(id);
                return;
            }

            openFile(id, fileUrl);
        });

        moreButton?.addEventListener("click", async (event) => {
            event.stopPropagation();

            if (!id) {
                alert("항목 ID를 찾을 수 없습니다.");
                return;
            }

            if (kind === "FOLDER") {
                await deleteFolder(id);
                return;
            }

            await deleteFile(id);
        });

        tbody.appendChild(tr);
    });
}

function getEmptyMarkup(isSourceEmpty) {
    if (viewMode === "PROJECTS") {
        if (isSourceEmpty) {
            return `
                <div class="files-empty-icon">📁</div>
                <h3>아직 참여 중인 프로젝트가 없습니다.</h3>
                <p>프로젝트를 만들면 프로젝트명 폴더가 이 화면에 자동으로 생깁니다.</p>
            `;
        }

        return `
            <div class="files-empty-icon">📁</div>
            <h3>조건에 맞는 프로젝트 폴더가 없습니다.</h3>
            <p>필터를 바꾸거나 다른 정렬 기준으로 다시 확인해주세요.</p>
        `;
    }

    if (isSourceEmpty) {
        return `
            <div class="files-empty-icon">📁</div>
            <h3>현재 폴더가 비어 있습니다.</h3>
            <p>새 폴더를 만들거나 파일을 업로드해보세요.</p>
        `;
    }

    return `
        <div class="files-empty-icon">📁</div>
        <h3>조건에 맞는 항목이 없습니다.</h3>
        <p>필터를 바꾸거나 상위 폴더로 이동해보세요.</p>
    `;
}

function matchesFilter(item, filterValue) {
    if (filterValue === "ALL") {
        return true;
    }

    const kind = getItemKind(item);

    if (filterValue === "FOLDER") {
        return kind === "FOLDER" || kind === "PROJECT";
    }

    return kind === "FILE";
}

function addFolderInputRow() {
    const table = document.getElementById("fileTable");
    const tbody = document.getElementById("fileTableBody");
    const empty = document.getElementById("fileEmpty");

    if (!table || !tbody || !empty) {
        return;
    }

    if (viewMode !== "FOLDER" || !currentGroupId || !currentFolderId) {
        alert("프로젝트 폴더 안에서만 새 폴더를 만들 수 있습니다.");
        return;
    }

    table.style.display = "table";
    empty.style.display = "none";

    const tr = document.createElement("tr");
    tr.className = "new-folder-row";
    tr.innerHTML = `
        <td>
            <div class="new-folder-input-wrap">
                <span class="file-icon">📁</span>
                <input id="newFolderInput" class="new-folder-input" type="text" value="새 폴더">
            </div>
        </td>
        <td>-</td>
        <td>-</td>
        <td>
            <button class="inline-confirm-btn" type="button">확인</button>
            <button class="inline-cancel-btn" type="button">취소</button>
        </td>
    `;

    tbody.prepend(tr);

    const input = tr.querySelector("#newFolderInput");
    input?.focus();
    input?.select();

    tr.querySelector(".inline-confirm-btn")?.addEventListener("click", () => {
        createFolder(input?.value.trim(), tr);
    });

    tr.querySelector(".inline-cancel-btn")?.addEventListener("click", () => {
        tr.remove();
        renderItems(currentItems);
    });

    input?.addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
            createFolder(input.value.trim(), tr);
        }

        if (event.key === "Escape") {
            tr.remove();
            renderItems(currentItems);
        }
    });
}

async function createFolder(folderName, rowElement) {
    if (!folderName) {
        alert("폴더명을 입력해주세요.");
        return;
    }

    try {
        await requestAuthApi(`/todo-groups/${currentGroupId}/folders/${currentFolderId}/folders`, {
            method: "POST",
            body: JSON.stringify({ folderName })
        });

        rowElement?.remove();
        await loadFolderItems(currentFolderId);
    } catch (error) {
        console.error(error);
        alert(error.message || "폴더 생성에 실패했습니다.");
    }
}

async function uploadFile(file) {
    const fileInput = document.getElementById("fileInput");

    if (viewMode !== "FOLDER" || !currentGroupId || !currentFolderId) {
        alert("프로젝트 폴더 안에서만 파일을 업로드할 수 있습니다.");
        return;
    }

    try {
        const formData = new FormData();
        formData.append("file", file);

        await requestAuthApi(`/todo-groups/${currentGroupId}/folders/${currentFolderId}/files`, {
            method: "POST",
            body: formData
        });

        if (fileInput) {
            fileInput.value = "";
        }

        await loadFolderItems(currentFolderId);
    } catch (error) {
        console.error(error);
        alert(error.message || "파일 업로드에 실패했습니다.");
    }
}

async function openFile(fileId, fileUrl) {
    const accessToken = getAccessToken();

    if (!accessToken) {
        alert("로그인이 필요합니다.");
        window.location.href = "/login";
        return;
    }

    try {
        const response = await fetch(fileUrl || `/files/${fileId}`, {
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        });

        if (response.status === 401) {
            alert("로그인이 만료되었습니다. 다시 로그인해주세요.");
            window.location.href = "/login";
            return;
        }

        if (!response.ok) {
            const errorText = await response.text();
            let errorMessage = "파일을 열 수 없습니다.";

            try {
                const errorBody = JSON.parse(errorText);
                errorMessage = errorBody.message || errorMessage;
            } catch (error) {
                if (errorText) {
                    errorMessage = errorText;
                }
            }

            throw new Error(errorMessage);
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        window.open(url, "_blank");
    } catch (error) {
        console.error(error);
        alert(error.message || "파일을 열 수 없습니다.");
    }
}

async function deleteFile(fileId) {
    if (!confirm("이 파일을 삭제하시겠습니까?")) {
        return;
    }

    try {
        await requestAuthApi(`/files/${fileId}`, {
            method: "DELETE"
        });

        await loadFolderItems(currentFolderId);
    } catch (error) {
        console.error(error);
        alert(error.message || "파일 삭제에 실패했습니다.");
    }
}

async function deleteFolder(folderId) {
    if (!confirm("이 폴더를 삭제하시겠습니까?")) {
        return;
    }

    try {
        await requestAuthApi(`/folders/${folderId}`, {
            method: "DELETE"
        });

        await loadFolderItems(currentFolderId);
    } catch (error) {
        console.error(error);
        alert(error.message || "폴더 삭제에 실패했습니다.");
    }
}

function showEmpty(message) {
    const table = document.getElementById("fileTable");
    const empty = document.getElementById("fileEmpty");

    if (!table || !empty) {
        return;
    }

    table.style.display = "none";
    empty.style.display = "flex";
    empty.innerHTML = `
        <div class="files-empty-icon">📁</div>
        <h3>${escapeHtml(message)}</h3>
        <p>잠시 후 다시 시도해주세요.</p>
    `;
}

function getResponseBody(response) {
    if (!response) {
        return null;
    }

    return response.data
        || response.result
        || response.body
        || response;
}

function getGroupsFromResponse(response) {
    const body = getResponseBody(response);
    return Array.isArray(body?.groups) ? body.groups : [];
}

function getItemsFromResponse(response) {
    const body = getResponseBody(response);

    if (!body) {
        return [];
    }

    const folders = Array.isArray(body.folders) ? body.folders : [];
    const files = Array.isArray(body.files) ? body.files : [];

    return [
        ...folders.map((folder) => ({ ...folder, itemType: "FOLDER" })),
        ...files.map((file) => ({ ...file, itemType: "FILE" }))
    ];
}

function getFolderFromResponse(response) {
    const body = getResponseBody(response);
    return body && !Array.isArray(body) ? body : null;
}

function getCurrentFolderFromResponse(response) {
    const body = getResponseBody(response);

    if (!body || Array.isArray(body)) {
        return null;
    }

    return body.currentFolder
        || body.current_folder
        || body.folder
        || null;
}

function toProjectItem(group) {
    return {
        itemType: "PROJECT",
        groupId: group.group_id ?? group.groupId ?? null,
        name: group.group_name ?? group.groupName ?? "이름 없는 프로젝트",
        deadline: group.deadline ?? "",
        memberCount: group.member_count ?? group.memberCount ?? 0,
        totalSize: 0
    };
}

async function buildProjectItem(group) {
    const projectItem = toProjectItem(group);
    const groupId = projectItem.groupId;

    if (!groupId) {
        return projectItem;
    }

    try {
        const rootResponse = await requestAuthApi(`/todo-groups/${groupId}/folders/root`);
        const rootFolder = getFolderFromResponse(rootResponse);
        return {
            ...projectItem,
            totalSize: rootFolder?.totalSize ?? rootFolder?.total_size ?? 0
        };
    } catch (error) {
        console.error(`프로젝트 루트 폴더 크기를 불러오지 못했습니다. groupId=${groupId}`, error);
        return projectItem;
    }
}

function toFolderNode(folder) {
    return {
        id: folder.folderId ?? folder.folder_id ?? folder.id,
        name: folder.folderName ?? folder.folder_name ?? "이름 없는 폴더",
        parentFolderId: folder.parentFolderId ?? folder.parent_folder_id ?? null
    };
}

function getProjectGroupId(item) {
    return item.groupId ?? item.group_id ?? null;
}

function getItemKind(item) {
    const rawKind = item.itemType || item.item_type || item.type || item.kind;

    if (rawKind) {
        const normalized = String(rawKind).toUpperCase();

        if (normalized.includes("PROJECT")) {
            return "PROJECT";
        }

        if (normalized.includes("FOLDER")) {
            return "FOLDER";
        }

        return "FILE";
    }

    if (item.groupId || item.group_id) {
        return "PROJECT";
    }

    return item.folderId || item.folder_id ? "FOLDER" : "FILE";
}

function getItemId(item) {
    return item.folderId
        || item.folder_id
        || item.fileId
        || item.file_id
        || item.id
        || null;
}

function getItemName(item) {
    return item.name
        || item.folderName
        || item.folder_name
        || item.originalName
        || item.original_name
        || item.fileName
        || item.file_name
        || "이름 없음";
}

function getItemDate(item) {
    if (getItemKind(item) === "PROJECT") {
        return "";
    }

    return item.createdAt
        || item.created_at
        || item.uploadedAt
        || item.uploaded_at
        || item.updatedAt
        || item.updated_at
        || "";
}

function getItemSize(item) {
    const kind = getItemKind(item);

    if (kind === "PROJECT" || kind === "FOLDER") {
        const totalSize = item.totalSize ?? item.total_size ?? 0;
        return formatFileSize(totalSize);
    }

    if (kind !== "FILE") {
        return "-";
    }

    const size = item.fileSize || item.file_size || item.size || 0;

    return formatFileSize(size);
}

function formatFileSize(size) {
    if (!size) {
        return "0 B";
    }

    if (size < 1024) {
        return `${size} B`;
    }

    if (size < 1024 * 1024) {
        return `${(size / 1024).toFixed(1)} KB`;
    }

    return `${(size / 1024 / 1024).toFixed(1)} MB`;
}

function formatDate(value) {
    if (!value) {
        return "-";
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return `${year}.${month}.${day}`;
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}
