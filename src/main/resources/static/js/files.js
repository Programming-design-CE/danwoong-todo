let currentGroupId = null;
let rootFolderId = null;
let currentFolderId = null;
let currentItems = [];

document.addEventListener("DOMContentLoaded", function () {
    initFilesPage();
    bindFilesEvents();
});

function getAccessToken() {
    return localStorage.getItem("accessToken");
}

function getGroupId() {
    const params = new URLSearchParams(window.location.search);

    return params.get("groupId")
        || localStorage.getItem("currentGroupId")
        || localStorage.getItem("groupId");
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
        "Authorization": `Bearer ${accessToken}`,
        ...(options.headers || {})
    };

    /**
     * 파일 업로드 FormData는 Content-Type을 직접 넣으면 안 됨.
     * 브라우저가 multipart/form-data boundary를 자동으로 넣어야 함.
     */
    if (!isFormData) {
        headers["Content-Type"] = "application/json";
    }

    const response = await fetch(url, {
        ...options,
        headers
    });

    if (response.status === 401 || response.status === 403) {
        alert("로그인이 만료되었습니다. 다시 로그인해주세요.");
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        window.location.href = "/login";
        return null;
    }

    if (response.status === 204) {
        return null;
    }

    if (!response.ok) {
        const errorText = await response.text();

        console.error("API 실패 상태코드:", response.status);
        console.error("API 실패 응답:", errorText);
        console.error("요청 URL:", url);
        console.error("요청 options:", options);

        throw new Error(`API 요청 실패: ${response.status}`);
    }

    const contentType = response.headers.get("content-type");

    if (contentType && contentType.includes("application/json")) {
        return await response.json();
    }

    return await response.blob();
}

async function initFilesPage() {
    currentGroupId = getGroupId();

    console.log("currentGroupId:", currentGroupId);

    if (!currentGroupId) {
        showEmpty("그룹 ID를 찾을 수 없습니다.");
        return;
    }

    await loadRootFolder();
}

function bindFilesEvents() {
    const createFolderBtn = document.getElementById("createFolderBtn");
    const uploadFileBtn = document.getElementById("uploadFileBtn");
    const fileInput = document.getElementById("fileInput");
    const fileTypeFilter = document.getElementById("fileTypeFilter");
    const sortOption = document.getElementById("sortOption");

    if (!createFolderBtn || !uploadFileBtn || !fileInput) {
        console.warn("파일 페이지 HTML 요소를 찾지 못했습니다.");
        return;
    }

    createFolderBtn.addEventListener("click", function () {
        addFolderInputRow();
    });

    uploadFileBtn.addEventListener("click", function () {
        fileInput.click();
    });

    fileInput.addEventListener("change", function (event) {
        const file = event.target.files[0];

        if (file) {
            uploadFile(file);
        }
    });

    fileTypeFilter.addEventListener("change", function () {
        renderItems(currentItems);
    });

    sortOption.addEventListener("change", function () {
        renderItems(currentItems);
    });
}

/**
 * GET /todo-groups/{groupId}/folders/root
 */
async function loadRootFolder() {
    try {
        const data = await requestAuthApi(`/todo-groups/${currentGroupId}/folders/root`);

        console.log("루트 폴더 응답:", data);

        if (!data) {
            return;
        }

        rootFolderId = getFolderId(data);
        currentFolderId = getFolderId(data);

        console.log("rootFolderId:", rootFolderId);
        console.log("currentFolderId:", currentFolderId);

        if (!currentFolderId) {
            alert("루트 폴더 ID를 찾을 수 없습니다. 백엔드 응답 필드명을 확인해주세요.");
            return;
        }

        await loadFolderItems(currentFolderId);

    } catch (error) {
        console.error(error);
        showEmpty("루트 폴더를 불러오지 못했습니다.");
    }
}

/**
 * GET /todo-groups/{groupId}/folders/{folderId}/items
 */
async function loadFolderItems(folderId) {
    if (!currentGroupId || !folderId) {
        console.error("loadFolderItems 실패 - groupId/folderId 없음");
        console.log("currentGroupId:", currentGroupId);
        console.log("folderId:", folderId);
        return;
    }

    try {
        const data = await requestAuthApi(`/todo-groups/${currentGroupId}/folders/${folderId}/items`);

        console.log("폴더 내부 항목 응답:", data);

        if (!data) {
            return;
        }

        currentFolderId = folderId;
        currentItems = data.items || [];

        renderItems(currentItems);

    } catch (error) {
        console.error(error);
        showEmpty("파일 목록을 불러오지 못했습니다.");
    }
}

function renderItems(items) {
    const table = document.getElementById("fileTable");
    const tbody = document.getElementById("fileTableBody");
    const empty = document.getElementById("fileEmpty");

    tbody.innerHTML = "";

    let result = [...items];

    const typeFilter = document.getElementById("fileTypeFilter").value;
    const sortOption = document.getElementById("sortOption").value;

    if (typeFilter !== "ALL") {
        result = result.filter(item => getItemKind(item) === typeFilter);
    }

    if (sortOption === "NAME") {
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
        return;
    }

    table.style.display = "table";
    empty.style.display = "none";

    result.forEach(function (item) {
        const kind = getItemKind(item);
        const id = getItemId(item);
        const name = getItemName(item);
        const date = formatDate(getItemDate(item));
        const size = getItemSize(item);

        const tr = document.createElement("tr");

        tr.innerHTML = `
            <td>
                <button class="file-name-btn" type="button">
                    <span class="file-icon">${kind === "FOLDER" ? "📁" : "📄"}</span>
                    <span>${name}</span>
                </button>
            </td>
            <td>${date}</td>
            <td>${size}</td>
            <td>
                <button class="file-more-btn" type="button">⋮</button>
            </td>
        `;

        tr.querySelector(".file-name-btn").addEventListener("click", function () {
            if (kind === "FOLDER") {
                loadFolderItems(id);
            } else {
                openFile(id);
            }
        });

        tr.querySelector(".file-more-btn").addEventListener("click", function (event) {
            event.stopPropagation();

            if (kind === "FOLDER") {
                deleteFolder(id);
            } else {
                deleteFile(id);
            }
        });

        tbody.appendChild(tr);
    });
}

function addFolderInputRow() {
    const table = document.getElementById("fileTable");
    const tbody = document.getElementById("fileTableBody");
    const empty = document.getElementById("fileEmpty");

    if (!currentGroupId || !currentFolderId) {
        alert("그룹 ID 또는 현재 폴더 ID가 없습니다.");
        console.log("currentGroupId:", currentGroupId);
        console.log("currentFolderId:", currentFolderId);
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

    tbody.appendChild(tr);

    const input = tr.querySelector("#newFolderInput");
    input.focus();
    input.select();

    tr.querySelector(".inline-confirm-btn").addEventListener("click", function () {
        createFolder(input.value.trim(), tr);
    });

    tr.querySelector(".inline-cancel-btn").addEventListener("click", function () {
        tr.remove();
        renderItems(currentItems);
    });

    input.addEventListener("keydown", function (event) {
        if (event.key === "Enter") {
            createFolder(input.value.trim(), tr);
        }

        if (event.key === "Escape") {
            tr.remove();
            renderItems(currentItems);
        }
    });
}

/**
 * POST /todo-groups/{groupId}/folders/{folderId}/folders
 */
async function createFolder(folderName, rowElement) {
    if (!folderName) {
        alert("폴더명을 입력해주세요.");
        return;
    }

    if (!currentGroupId || !currentFolderId) {
        alert("그룹 ID 또는 현재 폴더 ID가 없습니다.");
        console.log("currentGroupId:", currentGroupId);
        console.log("currentFolderId:", currentFolderId);
        return;
    }

    try {
        console.log("폴더 생성 요청 URL:", `/todo-groups/${currentGroupId}/folders/${currentFolderId}/folders`);
        console.log("폴더명:", folderName);

        await requestAuthApi(`/todo-groups/${currentGroupId}/folders/${currentFolderId}/folders`, {
            method: "POST",
            body: JSON.stringify({
                folder_name: folderName
            })
        });

        rowElement.remove();
        await loadFolderItems(currentFolderId);

    } catch (error) {
        console.error(error);
        alert("폴더 생성에 실패했습니다.");
    }
}

/**
 * POST /todo-groups/{groupId}/folders/{folderId}/files
 */
async function uploadFile(file) {
    const fileInput = document.getElementById("fileInput");

    if (!currentGroupId || !currentFolderId) {
        alert("그룹 ID 또는 현재 폴더 ID가 없습니다.");
        console.log("currentGroupId:", currentGroupId);
        console.log("currentFolderId:", currentFolderId);
        return;
    }

    try {
        const formData = new FormData();

        /**
         * 백엔드가 @RequestParam("file") MultipartFile file 이면 이게 맞음.
         * 만약 백엔드가 @RequestParam("files")면 아래를 files로 바꿔야 함.
         */
        formData.append("file", file);

        console.log("업로드 요청 URL:", `/todo-groups/${currentGroupId}/folders/${currentFolderId}/files`);
        console.log("업로드 파일:", file);

        await requestAuthApi(`/todo-groups/${currentGroupId}/folders/${currentFolderId}/files`, {
            method: "POST",
            body: formData
        });

        fileInput.value = "";
        await loadFolderItems(currentFolderId);

    } catch (error) {
        console.error(error);
        alert("파일 업로드에 실패했습니다.");
    }
}

/**
 * GET /files/{fileId}
 */
async function openFile(fileId) {
    try {
        const accessToken = getAccessToken();

        if (!accessToken) {
            alert("로그인이 필요합니다.");
            window.location.href = "/login";
            return;
        }

        const response = await fetch(`/files/${fileId}`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            console.error("파일 열람 실패:", response.status, errorText);
            throw new Error("파일 열람 실패");
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);

        window.open(url, "_blank");

    } catch (error) {
        console.error(error);
        alert("파일을 열 수 없습니다.");
    }
}

/**
 * DELETE /files/{fileId}
 */
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
        alert("파일 삭제에 실패했습니다.");
    }
}

/**
 * DELETE /folders/{folderId}
 */
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
        alert("폴더 삭제에 실패했습니다.");
    }
}

function showEmpty(message) {
    const table = document.getElementById("fileTable");
    const empty = document.getElementById("fileEmpty");

    table.style.display = "none";
    empty.style.display = "flex";

    empty.innerHTML = `
        <div class="files-empty-icon">📁</div>
        <h3>${message}</h3>
        <p>잠시 후 다시 시도해주세요.</p>
    `;
}

/**
 * 백엔드 응답 필드 보정
 */
function getFolderId(folder) {
    return folder.folder_id
        || folder.folderId
        || folder.id;
}

function getItemKind(item) {
    return item.item_type
        || item.itemType
        || item.type
        || item.kind
        || (item.folder_id || item.folderId ? "FOLDER" : "FILE");
}

function getItemId(item) {
    return item.folder_id
        || item.folderId
        || item.file_id
        || item.fileId
        || item.item_id
        || item.itemId
        || item.id;
}

function getItemName(item) {
    return item.folder_name
        || item.folderName
        || item.original_name
        || item.originalName
        || item.file_name
        || item.fileName
        || item.name
        || "이름 없음";
}

function getItemDate(item) {
    return item.uploaded_at
        || item.uploadedAt
        || item.created_at
        || item.createdAt
        || item.modified_at
        || item.modifiedAt
        || item.updated_at
        || item.updatedAt
        || "";
}

function getItemSize(item) {
    if (getItemKind(item) === "FOLDER") {
        return "-";
    }

    const size = item.size
        || item.file_size
        || item.fileSize
        || item.size_bytes
        || item.sizeBytes;

    if (!size) {
        return "-";
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